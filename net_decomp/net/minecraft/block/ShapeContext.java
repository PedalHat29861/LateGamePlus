/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ExperimentalMinecartShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import org.jspecify.annotations.Nullable;

public interface ShapeContext {
    public static ShapeContext absent() {
        return EntityShapeContext.Absent.INSTANCE;
    }

    public static ShapeContext absentTreatingFluidAsCube() {
        return EntityShapeContext.Absent.TREAT_FLUID_AS_CUBE;
    }

    public static ShapeContext of(Entity entity) {
        Entity entity2 = entity;
        Objects.requireNonNull(entity2);
        Entity entity3 = entity2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractMinecartEntity.class}, (Object)entity3, n)) {
            case 0 -> {
                AbstractMinecartEntity abstractMinecartEntity = (AbstractMinecartEntity)entity3;
                if (AbstractMinecartEntity.areMinecartImprovementsEnabled(abstractMinecartEntity.getEntityWorld())) {
                    yield new ExperimentalMinecartShapeContext(abstractMinecartEntity, false);
                }
                yield new EntityShapeContext(entity, false, false);
            }
            default -> new EntityShapeContext(entity, false, false);
        };
    }

    public static ShapeContext of(Entity entity, boolean shouldTreatFluidAsCube) {
        return new EntityShapeContext(entity, shouldTreatFluidAsCube, false);
    }

    public static ShapeContext ofPlacement(@Nullable PlayerEntity player) {
        ItemStack itemStack;
        boolean bl = player != null ? player.isDescending() : false;
        double d = player != null ? player.getY() : -1.7976931348623157E308;
        if (player instanceof LivingEntity) {
            PlayerEntity livingEntity = player;
            itemStack = livingEntity.getMainHandStack();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        return new EntityShapeContext(bl, true, d, itemStack, false, player);
    }

    public static ShapeContext ofCollision(@Nullable Entity entity, double y) {
        ItemStack itemStack;
        boolean bl = entity != null ? entity.isDescending() : false;
        double d = entity != null ? y : -1.7976931348623157E308;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            itemStack = livingEntity.getMainHandStack();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        return new EntityShapeContext(bl, true, d, itemStack, false, entity);
    }

    public boolean isDescending();

    public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

    public boolean isHolding(Item var1);

    public boolean shouldTreatFluidAsCube();

    public boolean canWalkOnFluid(FluidState var1, FluidState var2);

    public VoxelShape getCollisionShape(BlockState var1, CollisionView var2, BlockPos var3);

    default public boolean isPlacement() {
        return false;
    }
}

