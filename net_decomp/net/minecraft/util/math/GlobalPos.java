/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.util.math;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record GlobalPos(RegistryKey<World> dimension, BlockPos pos) {
    public static final MapCodec<GlobalPos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)World.CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), (App)BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)).apply((Applicative)instance, GlobalPos::create));
    public static final Codec<GlobalPos> CODEC = MAP_CODEC.codec();
    public static final PacketCodec<ByteBuf, GlobalPos> PACKET_CODEC = PacketCodec.tuple(RegistryKey.createPacketCodec(RegistryKeys.WORLD), GlobalPos::dimension, BlockPos.PACKET_CODEC, GlobalPos::pos, GlobalPos::create);

    public static GlobalPos create(RegistryKey<World> dimension, BlockPos pos) {
        return new GlobalPos(dimension, pos);
    }

    @Override
    public String toString() {
        return String.valueOf(this.dimension) + " " + String.valueOf(this.pos);
    }

    public boolean isWithinRange(RegistryKey<World> dimension, BlockPos otherPos, int maxDistance) {
        return this.dimension.equals(dimension) && this.pos.getChebyshevDistance(otherPos) <= maxDistance;
    }
}

