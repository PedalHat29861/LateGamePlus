/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CollisionView;
import org.jspecify.annotations.Nullable;

public class ExperimentalMinecartShapeContext
extends EntityShapeContext {
    private @Nullable BlockPos belowPos;
    private @Nullable BlockPos ascendingPos;

    protected ExperimentalMinecartShapeContext(AbstractMinecartEntity minecart, boolean collidesWithFluid) {
        super(minecart, collidesWithFluid, false);
        this.setIgnoredPositions(minecart);
    }

    private void setIgnoredPositions(AbstractMinecartEntity minecart) {
        BlockPos blockPos = minecart.getRailOrMinecartPos();
        BlockState blockState = minecart.getEntityWorld().getBlockState(blockPos);
        boolean bl = AbstractRailBlock.isRail(blockState);
        if (bl) {
            this.belowPos = blockPos.down();
            RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
            if (railShape.isAscending()) {
                this.ascendingPos = switch (railShape) {
                    case RailShape.ASCENDING_EAST -> blockPos.east();
                    case RailShape.ASCENDING_WEST -> blockPos.west();
                    case RailShape.ASCENDING_NORTH -> blockPos.north();
                    case RailShape.ASCENDING_SOUTH -> blockPos.south();
                    default -> null;
                };
            }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, CollisionView world, BlockPos pos) {
        if (pos.equals(this.belowPos) || pos.equals(this.ascendingPos)) {
            return VoxelShapes.empty();
        }
        return super.getCollisionShape(state, world, pos);
    }
}

