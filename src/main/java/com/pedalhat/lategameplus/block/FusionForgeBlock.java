package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FusionForgeBlock extends BaseEntityBlock {
    public static final MapCodec<FusionForgeBlock> CODEC = BlockBehaviour.simpleCodec(FusionForgeBlock::new);
    @SuppressWarnings("null")
    public static final EnumProperty<FusionForgeState> STATE = EnumProperty.create("state", FusionForgeState.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FusionForgeBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any()
            .setValue(STATE, FusionForgeState.DISABLED)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE, FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusionForgeBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(STATE, FusionForgeState.DISABLED)
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (world.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, com.pedalhat.lategameplus.registry.ModBlockEntities.FUSION_FORGE,
            FusionForgeBlockEntity::tick);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof FusionForgeBlockEntity fusionForge ? fusionForge : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
                                 BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider factory = getMenuProvider(state, world, pos);
            if (factory != null) {
                serverPlayer.openMenu(factory);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (state.getBlock() != world.getBlockState(pos).getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FusionForgeBlockEntity fusionForge) {
                Containers.dropContents(world, pos, fusionForge);
            }
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        FusionForgeState forgeState = state.getValue(STATE);
        if (forgeState != FusionForgeState.NETHER_WORKING && forgeState != FusionForgeState.WORKING) {
            return;
        }
        double baseX = pos.getX() + 0.5;
        double baseY = pos.getY();
        double baseZ = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            world.playLocalSound(baseX, baseY, baseZ, SoundEvents.BLASTFURNACE_FIRE_CRACKLE,
                SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double pixel = 1.0 / 16.0;
        double offset = 0.52 - 2.0 * pixel;
        double xOffset = axis == Direction.Axis.X ? direction.getStepX() * offset : 0.0;
        double zOffset = axis == Direction.Axis.Z ? direction.getStepZ() * offset : 0.0;
        double spread = 8.0 * pixel;
        double jitterX = (random.nextDouble() - 0.5) * spread;
        double jitterZ = (random.nextDouble() - 0.5) * spread;
        double yOffset = random.nextDouble() * 6.0 / 16.0 + 3.0 * pixel;

        world.addParticle(ParticleTypes.SMOKE, baseX + xOffset + jitterX, baseY + yOffset, baseZ + zOffset + jitterZ, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.FLAME, baseX + xOffset + jitterX, baseY + yOffset, baseZ + zOffset + jitterZ, 0.0, 0.0, 0.0);

        if (random.nextFloat() < 0.6f) {
            double extraY = random.nextDouble() * 4.0 / 16.0 + 3.0 * pixel;
            world.addParticle(ParticleTypes.SMALL_FLAME, baseX + xOffset + jitterX, baseY + extraY, baseZ + zOffset + jitterZ, 0.0, 0.0, 0.0);
        }
    }
}
