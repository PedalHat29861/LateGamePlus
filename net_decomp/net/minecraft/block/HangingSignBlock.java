/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public class HangingSignBlock
extends AbstractSignBlock {
    public static final MapCodec<HangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WoodType.CODEC.fieldOf("wood_type").forGetter(AbstractSignBlock::getWoodType), HangingSignBlock.createSettingsCodec()).apply((Applicative)instance, HangingSignBlock::new));
    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    private static final VoxelShape DEFAULT_SHAPE = Block.createColumnShape(10.0, 0.0, 16.0);
    private static final Map<Integer, VoxelShape> SHAPES_BY_ROTATION = VoxelShapes.createHorizontalFacingShapeMap(Block.createColumnShape(14.0, 2.0, 0.0, 10.0)).entrySet().stream().collect(Collectors.toMap(entry -> RotationPropertyHelper.fromDirection((Direction)entry.getKey()), Map.Entry::getValue));

    public MapCodec<HangingSignBlock> getCodec() {
        return CODEC;
    }

    public HangingSignBlock(WoodType woodType, AbstractBlock.Settings settings) {
        super(woodType, settings.sounds(woodType.hangingSignSoundType()));
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ROTATION, 0)).with(ATTACHED, false)).with(WATERLOGGED, false));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        SignBlockEntity signBlockEntity;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity && this.shouldTryAttaching(player, hit, signBlockEntity = (SignBlockEntity)blockEntity, stack)) {
            return ActionResult.PASS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    private boolean shouldTryAttaching(PlayerEntity player, BlockHitResult hitResult, SignBlockEntity sign, ItemStack stack) {
        return !sign.canRunCommandClickEvent(sign.isPlayerFacingFront(player), player) && stack.getItem() instanceof HangingSignItem && hitResult.getSide().equals(Direction.DOWN);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.up()).isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.CENTER);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean bl2;
        World world = ctx.getWorld();
        FluidState fluidState = world.getFluidState(ctx.getBlockPos());
        BlockPos blockPos = ctx.getBlockPos().up();
        BlockState blockState = world.getBlockState(blockPos);
        boolean bl = blockState.isIn(BlockTags.ALL_HANGING_SIGNS);
        Direction direction = Direction.fromHorizontalDegrees(ctx.getPlayerYaw());
        boolean bl3 = bl2 = !Block.isFaceFullSquare(blockState.getCollisionShape(world, blockPos), Direction.DOWN) || ctx.shouldCancelInteraction();
        if (bl && !ctx.shouldCancelInteraction()) {
            Optional<Direction> optional;
            if (blockState.contains(WallHangingSignBlock.FACING)) {
                Direction direction2 = blockState.get(WallHangingSignBlock.FACING);
                if (direction2.getAxis().test(direction)) {
                    bl2 = false;
                }
            } else if (blockState.contains(ROTATION) && (optional = RotationPropertyHelper.toDirection(blockState.get(ROTATION))).isPresent() && optional.get().getAxis().test(direction)) {
                bl2 = false;
            }
        }
        int i = !bl2 ? RotationPropertyHelper.fromDirection(direction.getOpposite()) : RotationPropertyHelper.fromYaw(ctx.getPlayerYaw() + 180.0f);
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(ATTACHED, bl2)).with(ROTATION, i)).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_ROTATION.getOrDefault(state.get(ROTATION), DEFAULT_SHAPE);
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return this.getOutlineShape(state, world, pos, ShapeContext.absent());
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.UP && !this.canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public float getRotationDegrees(BlockState state) {
        return RotationPropertyHelper.toDegrees(state.get(ROTATION));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(ROTATION, rotation.rotate(state.get(ROTATION), 16));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)state.with(ROTATION, mirror.mirror(state.get(ROTATION), 16));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, ATTACHED, WATERLOGGED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HangingSignBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return HangingSignBlock.validateTicker(type, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
    }
}

