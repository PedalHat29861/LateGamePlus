package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class FusionForgeBlock extends BlockWithEntity {
    public static final MapCodec<FusionForgeBlock> CODEC = AbstractBlock.createCodec(FusionForgeBlock::new);
    public static final EnumProperty<FusionForgeState> STATE = EnumProperty.of("state", FusionForgeState.class);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public FusionForgeBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
            .with(STATE, FusionForgeState.DISABLED)
            .with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE, FACING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FusionForgeBlockEntity(pos, state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState()
            .with(STATE, FusionForgeState.DISABLED)
            .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (world.isClient()) {
            return null;
        }
        return validateTicker(type, com.pedalhat.lategameplus.registry.ModBlockEntities.FUSION_FORGE,
            FusionForgeBlockEntity::tick);
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof FusionForgeBlockEntity fusionForge ? fusionForge : null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                 BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            NamedScreenHandlerFactory factory = createScreenHandlerFactory(state, world, pos);
            if (factory != null) {
                serverPlayer.openHandledScreen(factory);
            }
        }
        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (state.getBlock() != world.getBlockState(pos).getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FusionForgeBlockEntity fusionForge) {
                ItemScatterer.spawn(world, pos, fusionForge);
            }
        }
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        FusionForgeState forgeState = state.get(STATE);
        if (forgeState != FusionForgeState.NETHER_WORKING && forgeState != FusionForgeState.WORKING) {
            return;
        }
        double baseX = pos.getX() + 0.5;
        double baseY = pos.getY();
        double baseZ = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            world.playSoundClient(baseX, baseY, baseZ, SoundEvents.BLOCK_BLASTFURNACE_FIRE_CRACKLE,
                SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        }
        Direction direction = state.get(FACING);
        Direction.Axis axis = direction.getAxis();
        double pixel = 1.0 / 16.0;
        double offset = 0.52 - 2.0 * pixel;
        double xOffset = axis == Direction.Axis.X ? direction.getOffsetX() * offset : 0.0;
        double zOffset = axis == Direction.Axis.Z ? direction.getOffsetZ() * offset : 0.0;
        double spread = 8.0 * pixel;
        double jitterX = (random.nextDouble() - 0.5) * spread;
        double jitterZ = (random.nextDouble() - 0.5) * spread;
        double yOffset = random.nextDouble() * 6.0 / 16.0 + 3.0 * pixel;

        world.addParticleClient(ParticleTypes.SMOKE, baseX + xOffset + jitterX, baseY + yOffset, baseZ + zOffset + jitterZ, 0.0, 0.0, 0.0);
        world.addParticleClient(ParticleTypes.FLAME, baseX + xOffset + jitterX, baseY + yOffset, baseZ + zOffset + jitterZ, 0.0, 0.0, 0.0);

        if (random.nextFloat() < 0.6f) {
            double extraY = random.nextDouble() * 4.0 / 16.0 + 3.0 * pixel;
            world.addParticleClient(ParticleTypes.SMALL_FLAME, baseX + xOffset + jitterX, baseY + extraY, baseZ + zOffset + jitterZ, 0.0, 0.0, 0.0);
        }
    }
}
