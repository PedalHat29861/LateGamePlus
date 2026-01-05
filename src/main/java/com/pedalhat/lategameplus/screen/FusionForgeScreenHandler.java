package com.pedalhat.lategameplus.screen;

import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import com.pedalhat.lategameplus.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.FuelRegistry;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FusionForgeScreenHandler extends ScreenHandler {
    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int CATALYST_SLOT = 3;
    public static final int OUTPUT_SLOT = 4;
    private static final int PROPERTY_COUNT = 4;
    private static final int ARROW_PIXELS = 24;
    private static final int FLAME_PIXELS = 14;

    public static final int SLOT_COUNT = FusionForgeBlockEntity.INVENTORY_SIZE;

    private final Inventory inventory;
    private final FuelRegistry fuelRegistry;
    private final PropertyDelegate propertyDelegate;
    private final FusionForgeBlockEntity blockEntity;

    public FusionForgeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(SLOT_COUNT), new ArrayPropertyDelegate(PROPERTY_COUNT));
    }

    public FusionForgeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
                                    PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.FUSION_FORGE, syncId);
        checkSize(inventory, SLOT_COUNT);
        checkDataCount(propertyDelegate, PROPERTY_COUNT);
        this.inventory = inventory;
        this.fuelRegistry = playerInventory.player.getEntityWorld().getFuelRegistry();
        this.propertyDelegate = propertyDelegate;
        this.blockEntity = inventory instanceof FusionForgeBlockEntity fusionForge ? fusionForge : null;
        inventory.onOpen(playerInventory.player);
        addProperties(propertyDelegate);

        addSlot(new Slot(inventory, INPUT_A_SLOT, 49, 22));
        addSlot(new Slot(inventory, INPUT_B_SLOT, 86, 22));
        addSlot(new Slot(inventory, FUEL_SLOT, 68, 57) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return fuelRegistry.isFuel(stack);
            }
        });
        addSlot(new Slot(inventory, CATALYST_SLOT, 19, 50) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.NETHER_STAR);
            }
        });
        addSlot(new Slot(inventory, OUTPUT_SLOT, 142, 42) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                super.onTakeItem(player, stack);
                if (blockEntity != null) {
                    blockEntity.onOutputTaken(player);
                }
            }
        });

        addPlayerSlots(playerInventory, 8, 97);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getStack();
        newStack = original.copy();

        if (index < SLOT_COUNT) {
            if (!this.insertItem(original, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (fuelRegistry.isFuel(original)) {
            if (!this.insertItem(original, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (original.isOf(Items.NETHER_STAR)) {
            if (!this.insertItem(original, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(original, INPUT_A_SLOT, INPUT_B_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (original.getCount() == newStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, original);
        return newStack;
    }

    public int getCookProgress() {
        int cookTime = propertyDelegate.get(0);
        int cookTimeTotal = propertyDelegate.get(1);
        if (cookTimeTotal <= 0 || cookTime <= 0) {
            return 0;
        }
        int progress = cookTime * ARROW_PIXELS / cookTimeTotal;
        return Math.min(progress, ARROW_PIXELS);
    }

    public int getFuelProgress() {
        int fuelTicks = propertyDelegate.get(2);
        int fuelMaxTicks = propertyDelegate.get(3);
        if (fuelMaxTicks <= 0 || fuelTicks <= 0) {
            return 0;
        }
        int progress = fuelTicks * FLAME_PIXELS / fuelMaxTicks;
        if (progress > FLAME_PIXELS) {
            return FLAME_PIXELS;
        }
        return Math.max(progress, 0);
    }
}
