package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import com.pedalhat.lategameplus.block.entity.NetheriteShulkerBoxBlockEntity;
import com.pedalhat.lategameplus.registry.ModBlocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootWorldContext;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Simple container block with 45-slot inventory. */
public class NetheriteShulkerBoxBlock extends BlockWithEntity {
    public static final MapCodec<NetheriteShulkerBoxBlock> CODEC = createCodec(NetheriteShulkerBoxBlock::new);

    public NetheriteShulkerBoxBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) { }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NetheriteShulkerBoxBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
        if (factory != null) {
            player.openHandledScreen(factory);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof NetheriteShulkerBoxBlockEntity box) {
            int size = ((net.minecraft.inventory.Inventory) box).size();
            List<ItemStack> stacks = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ItemStack s = ((net.minecraft.inventory.Inventory) box).getStack(i);
                stacks.add(s.isEmpty() ? ItemStack.EMPTY : s.copy());
                // Vaciar slot para evitar que otra ruta disperse el contenido
                ((net.minecraft.inventory.Inventory) box).setStack(i, ItemStack.EMPTY);
            }
            ItemStack drop = new ItemStack(ModBlocks.NETHERITE_SHULKER_BOX_ITEM);
            drop.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(stacks));
            if (box.getCustomName() != null) {
                drop.set(DataComponentTypes.CUSTOM_NAME, box.getCustomName());
            }
            net.minecraft.block.Block.dropStack(world, pos, drop);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        // suppress default loot-based drops; 'onBreak' handles a single box with contents
        return List.of();
    }
}
