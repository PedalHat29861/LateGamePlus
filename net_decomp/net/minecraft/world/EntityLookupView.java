/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.EntityView;
import org.jspecify.annotations.Nullable;

public interface EntityLookupView
extends EntityView {
    public ServerWorld toServerWorld();

    default public @Nullable PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity) {
        return this.getClosestEntity(this.getPlayers(), targetPredicate, entity, entity.getX(), entity.getY(), entity.getZ());
    }

    default public @Nullable PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
        return this.getClosestEntity(this.getPlayers(), targetPredicate, entity, x, y, z);
    }

    default public @Nullable PlayerEntity getClosestPlayer(TargetPredicate targetPredicate, double x, double y, double z) {
        return this.getClosestEntity(this.getPlayers(), targetPredicate, null, x, y, z);
    }

    default public <T extends LivingEntity> @Nullable T getClosestEntity(Class<? extends T> clazz, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z, Box box) {
        return (T)this.getClosestEntity(this.getEntitiesByClass(clazz, box, potentialEntity -> true), targetPredicate, entity, x, y, z);
    }

    default public @Nullable LivingEntity getClosestEntity(TagKey<EntityType<?>> type, TargetPredicate predicate2, @Nullable LivingEntity target, double x, double y, double z, Box box) {
        double d = Double.MAX_VALUE;
        LivingEntity livingEntity = null;
        for (LivingEntity livingEntity2 : this.getEntitiesByClass(LivingEntity.class, box, predicate -> predicate.getType().isIn(type))) {
            double e;
            if (!predicate2.test(this.toServerWorld(), target, livingEntity2) || !((e = livingEntity2.squaredDistanceTo(x, y, z)) < d)) continue;
            d = e;
            livingEntity = livingEntity2;
        }
        return livingEntity;
    }

    default public <T extends LivingEntity> @Nullable T getClosestEntity(List<? extends T> entities, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
        double d = -1.0;
        LivingEntity livingEntity = null;
        for (LivingEntity livingEntity2 : entities) {
            if (!targetPredicate.test(this.toServerWorld(), entity, livingEntity2)) continue;
            double e = livingEntity2.squaredDistanceTo(x, y, z);
            if (d != -1.0 && !(e < d)) continue;
            d = e;
            livingEntity = livingEntity2;
        }
        return (T)livingEntity;
    }

    default public List<PlayerEntity> getPlayers(TargetPredicate targetPredicate, LivingEntity entity, Box box) {
        ArrayList<PlayerEntity> list = new ArrayList<PlayerEntity>();
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (!box.contains(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ()) || !targetPredicate.test(this.toServerWorld(), entity, playerEntity)) continue;
            list.add(playerEntity);
        }
        return list;
    }

    default public <T extends LivingEntity> List<T> getTargets(Class<T> clazz, TargetPredicate targetPredicate, LivingEntity entity2, Box box) {
        List<LivingEntity> list = this.getEntitiesByClass(clazz, box, entity -> true);
        ArrayList<LivingEntity> list2 = new ArrayList<LivingEntity>();
        for (LivingEntity livingEntity : list) {
            if (!targetPredicate.test(this.toServerWorld(), entity2, livingEntity)) continue;
            list2.add(livingEntity);
        }
        return list2;
    }
}

