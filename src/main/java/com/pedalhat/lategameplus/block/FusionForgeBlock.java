package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FusionForgeBlock extends BlockWithEntity {
    public static final MapCodec<FusionForgeBlock> CODEC = AbstractBlock.createCodec(FusionForgeBlock::new);
    public static final EnumProperty<FusionForgeState> STATE = EnumProperty.of("state", FusionForgeState.class);

    public FusionForgeBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(STATE, FusionForgeState.IDLE));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FusionForgeBlockEntity(pos, state);
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
}
