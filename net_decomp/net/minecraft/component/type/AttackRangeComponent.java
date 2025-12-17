/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public record AttackRangeComponent(float minRange, float maxRange, float minCreativeRange, float maxCreativeRange, float hitboxMargin, float mobFactor) {
    public static final Codec<AttackRangeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.rangedInclusiveFloat(0.0f, 64.0f).optionalFieldOf("min_reach", (Object)Float.valueOf(0.0f)).forGetter(AttackRangeComponent::minRange), (App)Codecs.rangedInclusiveFloat(0.0f, 64.0f).optionalFieldOf("max_reach", (Object)Float.valueOf(3.0f)).forGetter(AttackRangeComponent::maxRange), (App)Codecs.rangedInclusiveFloat(0.0f, 64.0f).optionalFieldOf("min_creative_reach", (Object)Float.valueOf(0.0f)).forGetter(AttackRangeComponent::minCreativeRange), (App)Codecs.rangedInclusiveFloat(0.0f, 64.0f).optionalFieldOf("max_creative_reach", (Object)Float.valueOf(5.0f)).forGetter(AttackRangeComponent::maxCreativeRange), (App)Codecs.rangedInclusiveFloat(0.0f, 1.0f).optionalFieldOf("hitbox_margin", (Object)Float.valueOf(0.3f)).forGetter(AttackRangeComponent::hitboxMargin), (App)Codec.floatRange((float)0.0f, (float)2.0f).optionalFieldOf("mob_factor", (Object)Float.valueOf(1.0f)).forGetter(AttackRangeComponent::mobFactor)).apply((Applicative)instance, AttackRangeComponent::new));
    public static final PacketCodec<ByteBuf, AttackRangeComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, AttackRangeComponent::minRange, PacketCodecs.FLOAT, AttackRangeComponent::maxRange, PacketCodecs.FLOAT, AttackRangeComponent::minCreativeRange, PacketCodecs.FLOAT, AttackRangeComponent::maxCreativeRange, PacketCodecs.FLOAT, AttackRangeComponent::hitboxMargin, PacketCodecs.FLOAT, AttackRangeComponent::mobFactor, AttackRangeComponent::new);

    public static AttackRangeComponent defaultForEntity(LivingEntity entity) {
        return new AttackRangeComponent(0.0f, (float)entity.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE), 0.0f, (float)entity.getAttributeValue(EntityAttributes.ENTITY_INTERACTION_RANGE), 0.0f, 1.0f);
    }

    public HitResult getHitResult(Entity entity, float tickProgress, Predicate<Entity> hitPredicate) {
        Either<BlockHitResult, Collection<EntityHitResult>> either = ProjectileUtil.collectPiercingCollisions(entity, this, hitPredicate, RaycastContext.ShapeType.OUTLINE);
        if (either.left().isPresent()) {
            return (HitResult)either.left().get();
        }
        Collection collection = (Collection)either.right().get();
        EntityHitResult entityHitResult = null;
        Vec3d vec3d = entity.getCameraPosVec(tickProgress);
        double d = Double.MAX_VALUE;
        for (EntityHitResult entityHitResult2 : collection) {
            double e = vec3d.squaredDistanceTo(entityHitResult2.getPos());
            if (!(e < d)) continue;
            d = e;
            entityHitResult = entityHitResult2;
        }
        if (entityHitResult != null) {
            return entityHitResult;
        }
        Vec3d vec3d2 = entity.getHeadRotationVector();
        Vec3d vec3d3 = entity.getCameraPosVec(tickProgress).add(vec3d2);
        return BlockHitResult.createMissed(vec3d3, Direction.getFacing(vec3d2), BlockPos.ofFloored(vec3d3));
    }

    public float getEffectiveMinRange(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            if (playerEntity.isSpectator()) {
                return 0.0f;
            }
            return playerEntity.isCreative() ? this.minCreativeRange : this.minRange;
        }
        return this.minRange * this.mobFactor;
    }

    public float getEffectiveMaxRange(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            return playerEntity.isCreative() ? this.maxCreativeRange : this.maxRange;
        }
        return this.maxRange * this.mobFactor;
    }

    public boolean isWithinRange(LivingEntity entity, Vec3d pos) {
        return this.isWithinRange(entity, pos::squaredDistanceTo, 0.0);
    }

    public boolean isWithinRange(LivingEntity entity, Box box, double extraHitboxMargin) {
        return this.isWithinRange(entity, box::squaredMagnitude, extraHitboxMargin);
    }

    private boolean isWithinRange(LivingEntity entity, ToDoubleFunction<Vec3d> squaredDistanceFunction, double extraHitboxMargin) {
        double d = Math.sqrt(squaredDistanceFunction.applyAsDouble(entity.getEyePos()));
        double e = (double)(this.getEffectiveMinRange(entity) - this.hitboxMargin) - extraHitboxMargin;
        double f = (double)(this.getEffectiveMaxRange(entity) + this.hitboxMargin) + extraHitboxMargin;
        return d >= e && d <= f;
    }
}

