/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import org.jspecify.annotations.Nullable;

public class EntityShapeContext
implements ShapeContext {
    private final boolean descending;
    private final double minY;
    private final boolean placement;
    private final ItemStack heldItem;
    private final boolean shouldTreatFluidAsCube;
    private final @Nullable Entity entity;

    protected EntityShapeContext(boolean descending, boolean placement, double minY, ItemStack heldItem, boolean shouldTreatFluidAsCube, @Nullable Entity entity) {
        this.descending = descending;
        this.placement = placement;
        this.minY = minY;
        this.heldItem = heldItem;
        this.shouldTreatFluidAsCube = shouldTreatFluidAsCube;
        this.entity = entity;
    }

    @Deprecated
    protected EntityShapeContext(Entity entity, boolean shouldTreatFluidAsCube, boolean placement) {
        ItemStack itemStack;
        boolean bl = entity.isDescending();
        double d = entity.getY();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            itemStack = livingEntity.getMainHandStack();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        this(bl, placement, d, itemStack, shouldTreatFluidAsCube, entity);
    }

    @Override
    public boolean isHolding(Item item) {
        return this.heldItem.isOf(item);
    }

    @Override
    public boolean shouldTreatFluidAsCube() {
        return this.shouldTreatFluidAsCube;
    }

    @Override
    public boolean canWalkOnFluid(FluidState stateAbove, FluidState state) {
        Entity entity = this.entity;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity.canWalkOnFluid(state) && !stateAbove.getFluid().matchesType(state.getFluid());
        }
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, CollisionView world, BlockPos pos) {
        return state.getCollisionShape(world, pos, this);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return this.minY > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - (double)1.0E-5f;
    }

    public @Nullable Entity getEntity() {
        return this.entity;
    }

    @Override
    public boolean isPlacement() {
        return this.placement;
    }

    protected static class Absent
    extends EntityShapeContext {
        protected static final ShapeContext INSTANCE = new Absent(false);
        protected static final ShapeContext TREAT_FLUID_AS_CUBE = new Absent(true);

        public Absent(boolean shouldTreatFluidAsCube) {
            super(false, false, -1.7976931348623157E308, ItemStack.EMPTY, shouldTreatFluidAsCube, null);
        }

        @Override
        public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return defaultValue;
        }
    }
}

