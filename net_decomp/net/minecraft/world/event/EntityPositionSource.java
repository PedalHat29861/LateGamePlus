/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.event;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;

public class EntityPositionSource
implements PositionSource {
    public static final MapCodec<EntityPositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Uuids.INT_STREAM_CODEC.fieldOf("source_entity").forGetter(EntityPositionSource::getUuid), (App)Codec.FLOAT.fieldOf("y_offset").orElse((Object)Float.valueOf(0.0f)).forGetter(entityPositionSource -> Float.valueOf(entityPositionSource.yOffset))).apply((Applicative)instance, (uuid, yOffset) -> new EntityPositionSource((Either<Entity, Either<UUID, Integer>>)Either.right((Object)Either.left((Object)uuid)), yOffset.floatValue())));
    public static final PacketCodec<ByteBuf, EntityPositionSource> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, EntityPositionSource::getEntityId, PacketCodecs.FLOAT, source -> Float.valueOf(source.yOffset), (entityId, yOffset) -> new EntityPositionSource((Either<Entity, Either<UUID, Integer>>)Either.right((Object)Either.right((Object)entityId)), yOffset.floatValue()));
    private Either<Entity, Either<UUID, Integer>> source;
    private final float yOffset;

    public EntityPositionSource(Entity entity, float yOffset) {
        this((Either<Entity, Either<UUID, Integer>>)Either.left((Object)entity), yOffset);
    }

    private EntityPositionSource(Either<Entity, Either<UUID, Integer>> source, float yOffset) {
        this.source = source;
        this.yOffset = yOffset;
    }

    @Override
    public Optional<Vec3d> getPos(World world) {
        if (this.source.left().isEmpty()) {
            this.findEntityInWorld(world);
        }
        return this.source.left().map(entity -> entity.getEntityPos().add(0.0, this.yOffset, 0.0));
    }

    private void findEntityInWorld(World world) {
        ((Optional)this.source.map(Optional::of, entityId -> Optional.ofNullable((Entity)entityId.map(uuid -> {
            Entity entity;
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                entity = serverWorld.getEntity((UUID)uuid);
            } else {
                entity = null;
            }
            return entity;
        }, world::getEntityById)))).ifPresent(entity -> {
            this.source = Either.left((Object)entity);
        });
    }

    public UUID getUuid() {
        return (UUID)this.source.map(Entity::getUuid, entityId -> (UUID)entityId.map(Function.identity(), entityIdx -> {
            throw new RuntimeException("Unable to get entityId from uuid");
        }));
    }

    private int getEntityId() {
        return (Integer)this.source.map(Entity::getId, entityId -> (Integer)entityId.map(uuid -> {
            throw new IllegalStateException("Unable to get entityId from uuid");
        }, Function.identity()));
    }

    public PositionSourceType<EntityPositionSource> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type
    implements PositionSourceType<EntityPositionSource> {
        @Override
        public MapCodec<EntityPositionSource> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<ByteBuf, EntityPositionSource> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}

