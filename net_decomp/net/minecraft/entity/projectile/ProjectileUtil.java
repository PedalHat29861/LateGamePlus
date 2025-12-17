/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public final class ProjectileUtil {
    public static final float DEFAULT_MARGIN = 0.3f;

    public static HitResult getCollision(Entity entity, Predicate<Entity> predicate) {
        Vec3d vec3d = entity.getVelocity();
        World world = entity.getEntityWorld();
        Vec3d vec3d2 = entity.getEntityPos();
        return ProjectileUtil.getCollision(vec3d2, entity, predicate, vec3d, world, ProjectileUtil.getToleranceMargin(entity), RaycastContext.ShapeType.COLLIDER);
    }

    public static Either<BlockHitResult, Collection<EntityHitResult>> collectPiercingCollisions(Entity entity, AttackRangeComponent attackRange, Predicate<Entity> hitPredicate, RaycastContext.ShapeType shapeType) {
        Vec3d vec3d = entity.getHeadRotationVector();
        Vec3d vec3d2 = entity.getEyePos();
        Vec3d vec3d3 = vec3d2.add(vec3d.multiply(attackRange.getEffectiveMinRange(entity)));
        double d = entity.getMovement().dotProduct(vec3d);
        Vec3d vec3d4 = vec3d2.add(vec3d.multiply((double)attackRange.getEffectiveMaxRange(entity) + Math.max(0.0, d)));
        return ProjectileUtil.collectPiercingCollisions(entity, vec3d2, vec3d3, hitPredicate, vec3d4, attackRange.hitboxMargin(), shapeType);
    }

    public static HitResult getCollision(Entity entity, Predicate<Entity> predicate, RaycastContext.ShapeType raycastShapeType) {
        Vec3d vec3d = entity.getVelocity();
        World world = entity.getEntityWorld();
        Vec3d vec3d2 = entity.getEntityPos();
        return ProjectileUtil.getCollision(vec3d2, entity, predicate, vec3d, world, ProjectileUtil.getToleranceMargin(entity), raycastShapeType);
    }

    public static HitResult getCollision(Entity entity, Predicate<Entity> predicate, double range) {
        Vec3d vec3d = entity.getRotationVec(0.0f).multiply(range);
        World world = entity.getEntityWorld();
        Vec3d vec3d2 = entity.getEyePos();
        return ProjectileUtil.getCollision(vec3d2, entity, predicate, vec3d, world, 0.0f, RaycastContext.ShapeType.COLLIDER);
    }

    private static HitResult getCollision(Vec3d pos, Entity entity, Predicate<Entity> predicate, Vec3d velocity, World world, float margin, RaycastContext.ShapeType raycastShapeType) {
        EntityHitResult hitResult2;
        Vec3d vec3d = pos.add(velocity);
        HitResult hitResult = world.getCollisionsIncludingWorldBorder(new RaycastContext(pos, vec3d, raycastShapeType, RaycastContext.FluidHandling.NONE, entity));
        if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
            vec3d = hitResult.getPos();
        }
        if ((hitResult2 = ProjectileUtil.getEntityCollision(world, entity, pos, vec3d, entity.getBoundingBox().stretch(velocity).expand(1.0), predicate, margin)) != null) {
            hitResult = hitResult2;
        }
        return hitResult;
    }

    private static Either<BlockHitResult, Collection<EntityHitResult>> collectPiercingCollisions(Entity entity, Vec3d pos, Vec3d minReach, Predicate<Entity> hitPredicate, Vec3d maxReach, float hitboxMargin, RaycastContext.ShapeType shapeType) {
        World world = entity.getEntityWorld();
        BlockHitResult blockHitResult = world.getCollisionsIncludingWorldBorder(new RaycastContext(pos, maxReach, shapeType, RaycastContext.FluidHandling.NONE, entity));
        if (blockHitResult.getType() != HitResult.Type.MISS && pos.squaredDistanceTo(maxReach = blockHitResult.getPos()) < pos.squaredDistanceTo(minReach)) {
            return Either.left((Object)blockHitResult);
        }
        Box box = Box.of(minReach, hitboxMargin, hitboxMargin, hitboxMargin).stretch(maxReach.subtract(minReach)).expand(1.0);
        Collection<EntityHitResult> collection = ProjectileUtil.collectPiercingCollisions(world, entity, minReach, maxReach, box, hitPredicate, hitboxMargin, shapeType, true);
        if (!collection.isEmpty()) {
            return Either.right(collection);
        }
        return Either.left((Object)blockHitResult);
    }

    public static @Nullable EntityHitResult raycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance) {
        World world = entity.getEntityWorld();
        double d = maxDistance;
        Entity entity2 = null;
        Vec3d vec3d = null;
        for (Entity entity3 : world.getOtherEntities(entity, box, predicate)) {
            Vec3d vec3d2;
            double e;
            Box box2 = entity3.getBoundingBox().expand(entity3.getTargetingMargin());
            Optional<Vec3d> optional = box2.raycast(min, max);
            if (box2.contains(min)) {
                if (!(d >= 0.0)) continue;
                entity2 = entity3;
                vec3d = optional.orElse(min);
                d = 0.0;
                continue;
            }
            if (!optional.isPresent() || !((e = min.squaredDistanceTo(vec3d2 = optional.get())) < d) && d != 0.0) continue;
            if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                if (d != 0.0) continue;
                entity2 = entity3;
                vec3d = vec3d2;
                continue;
            }
            entity2 = entity3;
            vec3d = vec3d2;
            d = e;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec3d);
    }

    public static @Nullable EntityHitResult getEntityCollision(World world, ProjectileEntity projectile, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate) {
        return ProjectileUtil.getEntityCollision(world, projectile, min, max, box, predicate, ProjectileUtil.getToleranceMargin(projectile));
    }

    public static float getToleranceMargin(Entity entity) {
        return Math.max(0.0f, Math.min(0.3f, (float)(entity.age - 2) / 20.0f));
    }

    public static @Nullable EntityHitResult getEntityCollision(World world, Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, float margin) {
        double d = Double.MAX_VALUE;
        Optional<Object> optional = Optional.empty();
        Entity entity2 = null;
        for (Entity entity3 : world.getOtherEntities(entity, box, predicate)) {
            double e;
            Box box2 = entity3.getBoundingBox().expand(margin);
            Optional<Vec3d> optional2 = box2.raycast(min, max);
            if (!optional2.isPresent() || !((e = min.squaredDistanceTo(optional2.get())) < d)) continue;
            entity2 = entity3;
            d = e;
            optional = optional2;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, (Vec3d)optional.get());
    }

    public static Collection<EntityHitResult> collectPiercingCollisions(World world, Entity entity, Vec3d from, Vec3d to, Box box, Predicate<Entity> hitPredicate, boolean skipRaycast) {
        return ProjectileUtil.collectPiercingCollisions(world, entity, from, to, box, hitPredicate, ProjectileUtil.getToleranceMargin(entity), RaycastContext.ShapeType.COLLIDER, skipRaycast);
    }

    public static Collection<EntityHitResult> collectPiercingCollisions(World world, Entity entity, Vec3d from, Vec3d to, Box box, Predicate<Entity> hitPredicate, float hitboxMargin, RaycastContext.ShapeType shapeType, boolean bl) {
        ArrayList<EntityHitResult> list = new ArrayList<EntityHitResult>();
        for (Entity entity2 : world.getOtherEntities(entity, box, hitPredicate)) {
            Optional<Vec3d> optional3;
            Vec3d vec3d2;
            Optional<Vec3d> optional2;
            Box box2 = entity2.getBoundingBox();
            if (bl && box2.contains(from)) {
                list.add(new EntityHitResult(entity2, from));
                continue;
            }
            Optional<Vec3d> optional = box2.raycast(from, to);
            if (optional.isPresent()) {
                list.add(new EntityHitResult(entity2, optional.get()));
                continue;
            }
            if ((double)hitboxMargin <= 0.0 || (optional2 = box2.expand(hitboxMargin).raycast(from, to)).isEmpty()) continue;
            Vec3d vec3d = optional2.get();
            BlockHitResult blockHitResult = world.getCollisionsIncludingWorldBorder(new RaycastContext(vec3d, vec3d2 = box2.getCenter(), shapeType, RaycastContext.FluidHandling.NONE, entity));
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                vec3d2 = blockHitResult.getPos();
            }
            if (!(optional3 = entity2.getBoundingBox().raycast(vec3d, vec3d2)).isPresent()) continue;
            list.add(new EntityHitResult(entity2, optional3.get()));
        }
        return list;
    }

    public static void setRotationFromVelocity(Entity entity, float tickProgress) {
        Vec3d vec3d = entity.getVelocity();
        if (vec3d.lengthSquared() == 0.0) {
            return;
        }
        double d = vec3d.horizontalLength();
        entity.setYaw((float)(MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875) + 90.0f);
        entity.setPitch((float)(MathHelper.atan2(d, vec3d.y) * 57.2957763671875) - 90.0f);
        while (entity.getPitch() - entity.lastPitch < -180.0f) {
            entity.lastPitch -= 360.0f;
        }
        while (entity.getPitch() - entity.lastPitch >= 180.0f) {
            entity.lastPitch += 360.0f;
        }
        while (entity.getYaw() - entity.lastYaw < -180.0f) {
            entity.lastYaw -= 360.0f;
        }
        while (entity.getYaw() - entity.lastYaw >= 180.0f) {
            entity.lastYaw += 360.0f;
        }
        entity.setPitch(MathHelper.lerp(tickProgress, entity.lastPitch, entity.getPitch()));
        entity.setYaw(MathHelper.lerp(tickProgress, entity.lastYaw, entity.getYaw()));
    }

    public static Hand getHandPossiblyHolding(LivingEntity entity, Item item) {
        return entity.getMainHandStack().isOf(item) ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public static PersistentProjectileEntity createArrowProjectile(LivingEntity entity, ItemStack stack, float damageModifier, @Nullable ItemStack bow) {
        ArrowItem arrowItem = (ArrowItem)(stack.getItem() instanceof ArrowItem ? stack.getItem() : Items.ARROW);
        PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(entity.getEntityWorld(), stack, entity, bow);
        persistentProjectileEntity.applyDamageModifier(damageModifier);
        return persistentProjectileEntity;
    }
}

