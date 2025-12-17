/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.jspecify.annotations.Nullable;

public interface EntityView {
    public List<Entity> getOtherEntities(@Nullable Entity var1, Box var2, Predicate<? super Entity> var3);

    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> var1, Box var2, Predicate<? super T> var3);

    default public <T extends Entity> List<T> getEntitiesByClass(Class<T> entityClass, Box box, Predicate<? super T> predicate) {
        return this.getEntitiesByType(TypeFilter.instanceOf(entityClass), box, predicate);
    }

    public List<? extends PlayerEntity> getPlayers();

    default public List<Entity> getOtherEntities(@Nullable Entity except, Box box) {
        return this.getOtherEntities(except, box, EntityPredicates.EXCEPT_SPECTATOR);
    }

    default public boolean doesNotIntersectEntities(@Nullable Entity except, VoxelShape shape) {
        if (shape.isEmpty()) {
            return true;
        }
        for (Entity entity : this.getOtherEntities(except, shape.getBoundingBox())) {
            if (entity.isRemoved() || !entity.intersectionChecked || except != null && entity.isConnectedThroughVehicle(except) || !VoxelShapes.matchesAnywhere(shape, VoxelShapes.cuboid(entity.getBoundingBox()), BooleanBiFunction.AND)) continue;
            return false;
        }
        return true;
    }

    default public <T extends Entity> List<T> getNonSpectatingEntities(Class<T> entityClass, Box box) {
        return this.getEntitiesByClass(entityClass, box, EntityPredicates.EXCEPT_SPECTATOR);
    }

    default public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        if (box.getAverageSideLength() < 1.0E-7) {
            return List.of();
        }
        Predicate<Entity> predicate = entity == null ? EntityPredicates.CAN_COLLIDE : EntityPredicates.EXCEPT_SPECTATOR.and(entity::collidesWith);
        List<Entity> list = this.getOtherEntities(entity, box.expand(1.0E-7), predicate);
        if (list.isEmpty()) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)list.size());
        for (Entity entity2 : list) {
            builder.add((Object)VoxelShapes.cuboid(entity2.getBoundingBox()));
        }
        return builder.build();
    }

    default public @Nullable PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, @Nullable Predicate<Entity> targetPredicate) {
        double d = -1.0;
        PlayerEntity playerEntity = null;
        for (PlayerEntity playerEntity2 : this.getPlayers()) {
            if (targetPredicate != null && !targetPredicate.test(playerEntity2)) continue;
            double e = playerEntity2.squaredDistanceTo(x, y, z);
            if (!(maxDistance < 0.0) && !(e < maxDistance * maxDistance) || d != -1.0 && !(e < d)) continue;
            d = e;
            playerEntity = playerEntity2;
        }
        return playerEntity;
    }

    default public @Nullable PlayerEntity getClosestPlayer(Entity entity, double maxDistance) {
        return this.getClosestPlayer(entity.getX(), entity.getY(), entity.getZ(), maxDistance, false);
    }

    default public @Nullable PlayerEntity getClosestPlayer(double x, double y, double z, double maxDistance, boolean ignoreCreative) {
        Predicate<Entity> predicate = ignoreCreative ? EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR : EntityPredicates.EXCEPT_SPECTATOR;
        return this.getClosestPlayer(x, y, z, maxDistance, predicate);
    }

    default public boolean isPlayerInRange(double x, double y, double z, double range) {
        for (PlayerEntity playerEntity : this.getPlayers()) {
            if (!EntityPredicates.EXCEPT_SPECTATOR.test(playerEntity) || !EntityPredicates.VALID_LIVING_ENTITY.test(playerEntity)) continue;
            double d = playerEntity.squaredDistanceTo(x, y, z);
            if (!(range < 0.0) && !(d < range * range)) continue;
            return true;
        }
        return false;
    }

    default public @Nullable PlayerEntity getPlayerByUuid(UUID uuid) {
        for (int i = 0; i < this.getPlayers().size(); ++i) {
            PlayerEntity playerEntity = this.getPlayers().get(i);
            if (!uuid.equals(playerEntity.getUuid())) continue;
            return playerEntity;
        }
        return null;
    }
}

