/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public abstract class ConnectingBlock
extends Block {
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty UP = Properties.UP;
    public static final BooleanProperty DOWN = Properties.DOWN;
    public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = ImmutableMap.copyOf((Map)Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST, Direction.UP, UP, Direction.DOWN, DOWN)));
    private final Function<BlockState, VoxelShape> shapeFunction;

    protected ConnectingBlock(float radius, AbstractBlock.Settings settings) {
        super(settings);
        this.shapeFunction = this.createShapeFunction(radius);
    }

    protected abstract MapCodec<? extends ConnectingBlock> getCodec();

    private Function<BlockState, VoxelShape> createShapeFunction(float radius) {
        VoxelShape voxelShape = Block.createCubeShape(radius);
        Map<Direction, VoxelShape> map = VoxelShapes.createFacingShapeMap(Block.createCuboidZShape(radius, 0.0, 8.0));
        return this.createShapeFunction(state -> {
            VoxelShape voxelShape2 = voxelShape;
            for (Map.Entry<Direction, BooleanProperty> entry : FACING_PROPERTIES.entrySet()) {
                if (!((Boolean)state.get(entry.getValue())).booleanValue()) continue;
                voxelShape2 = VoxelShapes.union((VoxelShape)map.get(entry.getKey()), voxelShape2);
            }
            return voxelShape2;
        });
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }
}

