/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.enums.TestBlockMode;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public class TestBlock
extends BlockWithEntity
implements OperatorBlock {
    public static final MapCodec<TestBlock> CODEC = TestBlock.createCodec(TestBlock::new);
    public static final EnumProperty<TestBlockMode> MODE = Properties.TEST_BLOCK_MODE;

    public TestBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TestBlockEntity(pos, state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        TestBlockMode testBlockMode;
        BlockStateComponent blockStateComponent = ctx.getStack().get(DataComponentTypes.BLOCK_STATE);
        BlockState blockState = this.getDefaultState();
        if (blockStateComponent != null && (testBlockMode = blockStateComponent.getValue(MODE)) != null) {
            blockState = (BlockState)blockState.with(MODE, testBlockMode);
        }
        return blockState;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof TestBlockEntity)) {
            return ActionResult.PASS;
        }
        TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
        if (!player.isCreativeLevelTwoOp()) {
            return ActionResult.PASS;
        }
        if (world.isClient()) {
            player.openTestBlockScreen(testBlockEntity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        TestBlockEntity testBlockEntity = TestBlock.getBlockEntityOnServer(world, pos);
        if (testBlockEntity == null) {
            return;
        }
        testBlockEntity.reset();
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        TestBlockEntity testBlockEntity = TestBlock.getBlockEntityOnServer(world, pos);
        if (testBlockEntity == null) {
            return;
        }
        if (testBlockEntity.getMode() == TestBlockMode.START) {
            return;
        }
        boolean bl = world.isReceivingRedstonePower(pos);
        boolean bl2 = testBlockEntity.isPowered();
        if (bl && !bl2) {
            testBlockEntity.setPowered(true);
            testBlockEntity.trigger();
        } else if (!bl && bl2) {
            testBlockEntity.setPowered(false);
        }
    }

    private static @Nullable TestBlockEntity getBlockEntityOnServer(World world, BlockPos pos) {
        ServerWorld serverWorld;
        BlockEntity blockEntity;
        if (world instanceof ServerWorld && (blockEntity = (serverWorld = (ServerWorld)world).getBlockEntity(pos)) instanceof TestBlockEntity) {
            TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
            return testBlockEntity;
        }
        return null;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(MODE) != TestBlockMode.START) {
            return 0;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TestBlockEntity) {
            TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
            return testBlockEntity.isPowered() ? 15 : 0;
        }
        return 0;
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack itemStack = super.getPickStack(world, pos, state, includeData);
        return TestBlock.applyBlockStateToStack(itemStack, state.get(MODE));
    }

    public static ItemStack applyBlockStateToStack(ItemStack stack, TestBlockMode mode) {
        stack.set(DataComponentTypes.BLOCK_STATE, stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT).with(MODE, mode));
        return stack;
    }

    protected MapCodec<TestBlock> getCodec() {
        return CODEC;
    }
}

