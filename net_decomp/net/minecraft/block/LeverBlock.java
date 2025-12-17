/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jspecify.annotations.Nullable;

public class LeverBlock
extends WallMountedBlock {
    public static final MapCodec<LeverBlock> CODEC = LeverBlock.createCodec(LeverBlock::new);
    public static final BooleanProperty POWERED = Properties.POWERED;
    private final Function<BlockState, VoxelShape> shapeFunction;

    public MapCodec<LeverBlock> getCodec() {
        return CODEC;
    }

    protected LeverBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false)).with(FACE, BlockFace.WALL));
        this.shapeFunction = this.createShapeFunction();
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        Map<BlockFace, Map<Direction, VoxelShape>> map = VoxelShapes.createBlockFaceHorizontalFacingShapeMap(Block.createCuboidZShape(6.0, 8.0, 10.0, 16.0));
        return this.createShapeFunction(state -> (VoxelShape)((Map)map.get(state.get(FACE))).get(state.get(FACING)), POWERED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            BlockState blockState = (BlockState)state.cycle(POWERED);
            if (blockState.get(POWERED).booleanValue()) {
                LeverBlock.spawnParticles(blockState, world, pos, 1.0f);
            }
        } else {
            this.togglePower(state, world, pos, null);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (explosion.canTriggerBlocks()) {
            this.togglePower(state, world, pos, null);
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    public void togglePower(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player) {
        state = (BlockState)state.cycle(POWERED);
        world.setBlockState(pos, state, 3);
        this.updateNeighbors(state, world, pos);
        LeverBlock.playClickSound(player, world, pos, state);
        world.emitGameEvent((Entity)player, state.get(POWERED) != false ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
    }

    protected static void playClickSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos, BlockState state) {
        float f = state.get(POWERED) != false ? 0.6f : 0.5f;
        world.playSound(player, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3f, f);
    }

    private static void spawnParticles(BlockState state, WorldAccess world, BlockPos pos, float alpha) {
        Direction direction = ((Direction)state.get(FACING)).getOpposite();
        Direction direction2 = LeverBlock.getDirection(state).getOpposite();
        double d = (double)pos.getX() + 0.5 + 0.1 * (double)direction.getOffsetX() + 0.2 * (double)direction2.getOffsetX();
        double e = (double)pos.getY() + 0.5 + 0.1 * (double)direction.getOffsetY() + 0.2 * (double)direction2.getOffsetY();
        double f = (double)pos.getZ() + 0.5 + 0.1 * (double)direction.getOffsetZ() + 0.2 * (double)direction2.getOffsetZ();
        world.addParticleClient(new DustParticleEffect(0xFF0000, alpha), d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(POWERED).booleanValue() && random.nextFloat() < 0.25f) {
            LeverBlock.spawnParticles(state, world, pos, 0.5f);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved && state.get(POWERED).booleanValue()) {
            this.updateNeighbors(state, world, pos);
        }
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) != false ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(POWERED).booleanValue() && LeverBlock.getDirection(state) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        Direction direction;
        WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(world, direction, (direction = LeverBlock.getDirection(state).getOpposite()).getAxis().isHorizontal() ? Direction.UP : (Direction)state.get(FACING));
        world.updateNeighborsAlways(pos, this, wireOrientation);
        world.updateNeighborsAlways(pos.offset(direction), this, wireOrientation);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, POWERED);
    }
}

