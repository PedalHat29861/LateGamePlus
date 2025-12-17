/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.dispenser;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class MinecartDispenserBehavior
extends ItemDispenserBehavior {
    private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();
    private final EntityType<? extends AbstractMinecartEntity> minecartEntityType;

    public MinecartDispenserBehavior(EntityType<? extends AbstractMinecartEntity> minecartEntityType) {
        this.minecartEntityType = minecartEntityType;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        double g;
        Direction direction = pointer.state().get(DispenserBlock.FACING);
        ServerWorld serverWorld = pointer.world();
        Vec3d vec3d = pointer.centerPos();
        double d = vec3d.getX() + (double)direction.getOffsetX() * 1.125;
        double e = Math.floor(vec3d.getY()) + (double)direction.getOffsetY();
        double f = vec3d.getZ() + (double)direction.getOffsetZ() * 1.125;
        BlockPos blockPos = pointer.pos().offset(direction);
        BlockState blockState = serverWorld.getBlockState(blockPos);
        if (blockState.isIn(BlockTags.RAILS)) {
            g = MinecartDispenserBehavior.getRailShape(blockState).isAscending() ? 0.6 : 0.1;
        } else {
            if (!blockState.isAir()) return this.fallbackBehavior.dispense(pointer, stack);
            BlockState blockState2 = serverWorld.getBlockState(blockPos.down());
            if (!blockState2.isIn(BlockTags.RAILS)) return this.fallbackBehavior.dispense(pointer, stack);
            g = direction == Direction.DOWN || !MinecartDispenserBehavior.getRailShape(blockState2).isAscending() ? -0.9 : -0.4;
        }
        Vec3d vec3d2 = new Vec3d(d, e + g, f);
        AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(serverWorld, vec3d2.x, vec3d2.y, vec3d2.z, this.minecartEntityType, SpawnReason.DISPENSER, stack, null);
        if (abstractMinecartEntity == null) return stack;
        serverWorld.spawnEntity(abstractMinecartEntity);
        stack.decrement(1);
        return stack;
    }

    private static RailShape getRailShape(BlockState state) {
        RailShape railShape;
        Block block = state.getBlock();
        if (block instanceof AbstractRailBlock) {
            AbstractRailBlock abstractRailBlock = (AbstractRailBlock)block;
            railShape = state.get(abstractRailBlock.getShapeProperty());
        } else {
            railShape = RailShape.NORTH_SOUTH;
        }
        return railShape;
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        pointer.world().syncWorldEvent(1000, pointer.pos(), 0);
    }
}

