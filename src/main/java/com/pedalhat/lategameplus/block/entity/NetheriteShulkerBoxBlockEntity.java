package com.pedalhat.lategameplus.block.entity;

import com.pedalhat.lategameplus.config.ConfigManager;
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
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(getSize(), ItemStack.EMPTY);

    public NetheriteShulkerBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NETHERITE_SHULKER_BOX_ENTITY, pos, state);
    }

    public static int getRows() {
        int rows = ConfigManager.get().netheriteShulkerRows;
        // Default to 5 when unset (0) or explicitly 5; otherwise 4
        return (rows == 0 || rows == 5) ? 5 : 4;
    }

    public static int getSize() {
        return 9 * getRows();
    }

    private void ensureSize() {
        int size = getSize();
        if (this.items.size() != size) {
            DefaultedList<ItemStack> resized = DefaultedList.ofSize(size, ItemStack.EMPTY);
            for (int i = 0; i < Math.min(size, this.items.size()); i++) {
                resized.set(i, this.items.get(i));
            }
            this.items = resized;
            markDirty();
        }
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.lategameplus.netherite_shulker_box");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        ensureSize();
        return items;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> items) {
        int size = getSize();
        if (items.size() != size) {
            DefaultedList<ItemStack> resized = DefaultedList.ofSize(size, ItemStack.EMPTY);
            for (int i = 0; i < Math.min(size, items.size()); i++) {
                resized.set(i, items.get(i));
            }
            this.items = resized;
        } else {
            this.items = items;
        }
    }

    @Override
    public int size() {
        ensureSize();
        return getSize();
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        int rows = getRows();
        ScreenHandlerType<GenericContainerScreenHandler> type = rows == 5 ? ScreenHandlerType.GENERIC_9X5 : ScreenHandlerType.GENERIC_9X4;
        return new GenericContainerScreenHandler(type, syncId, playerInventory, this, rows);
    }

    // Sided inventory
    @Override
    public int[] getAvailableSlots(Direction side) {
        int size = getSize();
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) slots[i] = i;
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

