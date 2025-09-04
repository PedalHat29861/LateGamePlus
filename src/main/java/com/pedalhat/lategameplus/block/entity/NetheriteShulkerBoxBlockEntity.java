package com.pedalhat.lategameplus.block.entity;

import com.pedalhat.lategameplus.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.util.ItemScatterer;
import org.jetbrains.annotations.Nullable;

public class NetheriteShulkerBoxBlockEntity extends LockableContainerBlockEntity implements SidedInventory {
    private static final int SIZE = 36;
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);

    public NetheriteShulkerBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NETHERITE_SHULKER_BOX_ENTITY, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.lategameplus.netherite_shulker_box");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return items;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> items) {
        if (items.size() != SIZE) {
            DefaultedList<ItemStack> resized = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
            for (int i = 0; i < Math.min(SIZE, items.size()); i++) {
                resized.set(i, items.get(i));
            }
            this.items = resized;
        } else {
            this.items = items;
        }
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, playerInventory, this, 4);
    }

    // Sided inventory
    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] slots = new int[SIZE];
        for (int i = 0; i < SIZE; i++) slots[i] = i;
        return slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) { return true; }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) { return true; }

    // Screen handler
    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, net.minecraft.entity.player.PlayerEntity player) {
        return createScreenHandler(syncId, playerInventory);
    }

    // Helper to drop inventory when block removed
    public void dropInventory(World world, BlockPos pos) {
        ItemScatterer.spawn(world, pos, this);
    }
}
