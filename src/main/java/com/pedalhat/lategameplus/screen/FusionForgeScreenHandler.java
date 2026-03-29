package com.pedalhat.lategameplus.screen;

import com.pedalhat.lategameplus.block.entity.FusionForgeBlockEntity;
import com.pedalhat.lategameplus.registry.ModScreenHandlers;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.FuelValues;

public class FusionForgeScreenHandler extends AbstractContainerMenu {
    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int CATALYST_SLOT = 3;
    public static final int OUTPUT_SLOT = 4;
    private static final int PROPERTY_COUNT = 6;
    private static final int ARROW_PIXELS = 24;
    private static final int FLAME_PIXELS = 14;

    public static final int SLOT_COUNT = FusionForgeBlockEntity.INVENTORY_SIZE;

    private final Container inventory;
    private final FuelValues fuelRegistry;
    private final ContainerData propertyDelegate;
    private final FusionForgeBlockEntity blockEntity;

    public FusionForgeScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(PROPERTY_COUNT));
    }

    public FusionForgeScreenHandler(int syncId, Inventory playerInventory, Container inventory,
                                    ContainerData propertyDelegate) {
        super(ModScreenHandlers.FUSION_FORGE, syncId);
        checkContainerSize(inventory, SLOT_COUNT);
        checkContainerDataCount(propertyDelegate, PROPERTY_COUNT);
        this.inventory = inventory;
        this.fuelRegistry = playerInventory.player.level().fuelValues();
        this.propertyDelegate = propertyDelegate;
        this.blockEntity = inventory instanceof FusionForgeBlockEntity fusionForge ? fusionForge : null;
        inventory.startOpen(playerInventory.player);
        addDataSlots(propertyDelegate);

        addSlot(new Slot(inventory, INPUT_A_SLOT, 49, 22));
        addSlot(new Slot(inventory, INPUT_B_SLOT, 86, 22));
        addSlot(new Slot(inventory, FUEL_SLOT, 68, 57) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return fuelRegistry.isFuel(stack);
            }
        });
        addSlot(new Slot(inventory, CATALYST_SLOT, 19, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.NETHER_STAR);
            }
        });
        addSlot(new Slot(inventory, OUTPUT_SLOT, 142, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (blockEntity != null) {
                    blockEntity.onOutputTaken(player);
                }
            }
        });

        addStandardInventorySlots(playerInventory, 8, 97);
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        newStack = original.copy();

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(original, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (fuelRegistry.isFuel(original)) {
            if (!this.moveItemStackTo(original, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (original.is(Items.NETHER_STAR)) {
            if (!this.moveItemStackTo(original, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(original, INPUT_A_SLOT, INPUT_B_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (original.getCount() == newStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, original);
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
        int fuelStored = getFuelStoredTicks();
        int fuelCapacity = getFuelCapacityTicks();
        if (fuelCapacity <= 0 || fuelStored <= 0) {
            return 0;
        }
        int progress = (int) ((long) fuelStored * FLAME_PIXELS / fuelCapacity);
        if (progress > FLAME_PIXELS) {
            return FLAME_PIXELS;
        }
        return Math.max(progress, 0);
    }

    public int getFuelStored() {
        return Math.max(0, getFuelStoredTicks());
    }

    public int getFuelCapacity() {
        return Math.max(0, getFuelCapacityTicks());
    }

    private int getFuelStoredTicks() {
        int low = unsignedProperty(2);
        int high = unsignedProperty(3);
        return (high << 16) | low;
    }

    private int getFuelCapacityTicks() {
        int low = unsignedProperty(4);
        int high = unsignedProperty(5);
        return (high << 16) | low;
    }

    private int unsignedProperty(int index) {
        return propertyDelegate.get(index) & 0xFFFF;
    }
}
