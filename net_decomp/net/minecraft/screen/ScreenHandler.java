/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.screen;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.Property;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.screen.sync.TrackedSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ScreenHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int EMPTY_SPACE_SLOT_INDEX = -999;
    public static final int field_30731 = 0;
    public static final int field_30732 = 1;
    public static final int field_30733 = 2;
    public static final int field_30734 = 0;
    public static final int field_30735 = 1;
    public static final int field_30736 = 2;
    public static final int field_30737 = Integer.MAX_VALUE;
    public static final int field_52557 = 9;
    public static final int field_52558 = 18;
    private final DefaultedList<ItemStack> trackedStacks = DefaultedList.of();
    public final DefaultedList<Slot> slots = DefaultedList.of();
    private final List<Property> properties = Lists.newArrayList();
    private ItemStack cursorStack = ItemStack.EMPTY;
    private final DefaultedList<TrackedSlot> trackedSlots = DefaultedList.of();
    private final IntList trackedPropertyValues = new IntArrayList();
    private TrackedSlot trackedCursorSlot = TrackedSlot.ALWAYS_IN_SYNC;
    private int revision;
    private final @Nullable ScreenHandlerType<?> type;
    public final int syncId;
    private int quickCraftButton = -1;
    private int quickCraftStage;
    private final Set<Slot> quickCraftSlots = Sets.newHashSet();
    private final List<ScreenHandlerListener> listeners = Lists.newArrayList();
    private @Nullable ScreenHandlerSyncHandler syncHandler;
    private boolean disableSync;

    protected ScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        this.type = type;
        this.syncId = syncId;
    }

    protected void addPlayerHotbarSlots(Inventory playerInventory, int left, int y) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, left + i * 18, y));
        }
    }

    protected void addPlayerInventorySlots(Inventory playerInventory, int left, int top) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, left + j * 18, top + i * 18));
            }
        }
    }

    protected void addPlayerSlots(Inventory playerInventory, int left, int top) {
        this.addPlayerInventorySlots(playerInventory, left, top);
        int i = 4;
        int j = 58;
        this.addPlayerHotbarSlots(playerInventory, left, top + 58);
    }

    protected static boolean canUse(ScreenHandlerContext context, PlayerEntity player, Block block) {
        return context.get((world, pos) -> {
            if (!world.getBlockState((BlockPos)pos).isOf(block)) {
                return false;
            }
            return player.canInteractWithBlockAt((BlockPos)pos, 4.0);
        }, true);
    }

    public ScreenHandlerType<?> getType() {
        if (this.type == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.type;
    }

    protected static void checkSize(Inventory inventory, int expectedSize) {
        int i = inventory.size();
        if (i < expectedSize) {
            throw new IllegalArgumentException("Container size " + i + " is smaller than expected " + expectedSize);
        }
    }

    protected static void checkDataCount(PropertyDelegate data, int expectedCount) {
        int i = data.size();
        if (i < expectedCount) {
            throw new IllegalArgumentException("Container data count " + i + " is smaller than expected " + expectedCount);
        }
    }

    public boolean isValid(int slot) {
        return slot == -1 || slot == -999 || slot < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.id = this.slots.size();
        this.slots.add(slot);
        this.trackedStacks.add(ItemStack.EMPTY);
        this.trackedSlots.add(this.syncHandler != null ? this.syncHandler.createTrackedSlot() : TrackedSlot.ALWAYS_IN_SYNC);
        return slot;
    }

    protected Property addProperty(Property property) {
        this.properties.add(property);
        this.trackedPropertyValues.add(0);
        return property;
    }

    protected void addProperties(PropertyDelegate propertyDelegate) {
        for (int i = 0; i < propertyDelegate.size(); ++i) {
            this.addProperty(Property.create(propertyDelegate, i));
        }
    }

    public void addListener(ScreenHandlerListener listener) {
        if (this.listeners.contains(listener)) {
            return;
        }
        this.listeners.add(listener);
        this.sendContentUpdates();
    }

    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        this.syncHandler = handler;
        this.trackedCursorSlot = handler.createTrackedSlot();
        this.trackedSlots.replaceAll(slot -> handler.createTrackedSlot());
        this.syncState();
    }

    public void syncState() {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>(this.slots.size());
        int j = this.slots.size();
        for (int i = 0; i < j; ++i) {
            ItemStack itemStack = this.slots.get(i).getStack();
            list.add(itemStack.copy());
            this.trackedSlots.get(i).setReceivedStack(itemStack);
        }
        ItemStack itemStack2 = this.getCursorStack();
        this.trackedCursorSlot.setReceivedStack(itemStack2);
        int k = this.properties.size();
        for (j = 0; j < k; ++j) {
            this.trackedPropertyValues.set(j, this.properties.get(j).get());
        }
        if (this.syncHandler != null) {
            this.syncHandler.updateState(this, list, itemStack2.copy(), this.trackedPropertyValues.toIntArray());
        }
    }

    public void removeListener(ScreenHandlerListener listener) {
        this.listeners.remove(listener);
    }

    public DefaultedList<ItemStack> getStacks() {
        DefaultedList<ItemStack> defaultedList = DefaultedList.of();
        for (Slot slot : this.slots) {
            defaultedList.add(slot.getStack());
        }
        return defaultedList;
    }

    public void sendContentUpdates() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getStack();
            com.google.common.base.Supplier supplier = Suppliers.memoize(itemStack::copy);
            this.updateTrackedSlot(i, itemStack, (Supplier<ItemStack>)supplier);
            this.checkSlotUpdates(i, itemStack, (Supplier<ItemStack>)supplier);
        }
        this.checkCursorStackUpdates();
        for (i = 0; i < this.properties.size(); ++i) {
            Property property = this.properties.get(i);
            int j = property.get();
            if (property.hasChanged()) {
                this.notifyPropertyUpdate(i, j);
            }
            this.checkPropertyUpdates(i, j);
        }
    }

    public void updateToClient() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack itemStack = this.slots.get(i).getStack();
            this.updateTrackedSlot(i, itemStack, itemStack::copy);
        }
        for (i = 0; i < this.properties.size(); ++i) {
            Property property = this.properties.get(i);
            if (!property.hasChanged()) continue;
            this.notifyPropertyUpdate(i, property.get());
        }
        this.syncState();
    }

    private void notifyPropertyUpdate(int index, int value) {
        for (ScreenHandlerListener screenHandlerListener : this.listeners) {
            screenHandlerListener.onPropertyUpdate(this, index, value);
        }
    }

    private void updateTrackedSlot(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        ItemStack itemStack = this.trackedStacks.get(slot);
        if (!ItemStack.areEqual(itemStack, stack)) {
            ItemStack itemStack2 = copySupplier.get();
            this.trackedStacks.set(slot, itemStack2);
            for (ScreenHandlerListener screenHandlerListener : this.listeners) {
                screenHandlerListener.onSlotUpdate(this, slot, itemStack2);
            }
        }
    }

    private void checkSlotUpdates(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        if (this.disableSync) {
            return;
        }
        TrackedSlot trackedSlot = this.trackedSlots.get(slot);
        if (!trackedSlot.isInSync(stack)) {
            trackedSlot.setReceivedStack(stack);
            if (this.syncHandler != null) {
                this.syncHandler.updateSlot(this, slot, copySupplier.get());
            }
        }
    }

    private void checkPropertyUpdates(int id, int value) {
        if (this.disableSync) {
            return;
        }
        int i = this.trackedPropertyValues.getInt(id);
        if (i != value) {
            this.trackedPropertyValues.set(id, value);
            if (this.syncHandler != null) {
                this.syncHandler.updateProperty(this, id, value);
            }
        }
    }

    private void checkCursorStackUpdates() {
        if (this.disableSync) {
            return;
        }
        ItemStack itemStack = this.getCursorStack();
        if (!this.trackedCursorSlot.isInSync(itemStack)) {
            this.trackedCursorSlot.setReceivedStack(itemStack);
            if (this.syncHandler != null) {
                this.syncHandler.updateCursorStack(this, itemStack.copy());
            }
        }
    }

    public void setReceivedStack(int slot, ItemStack stack) {
        this.trackedSlots.get(slot).setReceivedStack(stack);
    }

    public void setReceivedHash(int slot, ItemStackHash hash) {
        if (slot < 0 || slot >= this.trackedSlots.size()) {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", (Object)slot, (Object)this.trackedSlots.size());
            return;
        }
        this.trackedSlots.get(slot).setReceivedHash(hash);
    }

    public void setReceivedCursorHash(ItemStackHash cursorStackHash) {
        this.trackedCursorSlot.setReceivedHash(cursorStackHash);
    }

    public boolean onButtonClick(PlayerEntity player, int id) {
        return false;
    }

    public Slot getSlot(int index) {
        return this.slots.get(index);
    }

    public abstract ItemStack quickMove(PlayerEntity var1, int var2);

    public void selectBundleStack(int slot, int selectedStack) {
        if (slot >= 0 && slot < this.slots.size()) {
            ItemStack itemStack = this.slots.get(slot).getStack();
            BundleItem.setSelectedStackIndex(itemStack, selectedStack);
        }
    }

    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        try {
            this.internalOnSlotClick(slotIndex, button, actionType, player);
        }
        catch (Exception exception) {
            CrashReport crashReport = CrashReport.create(exception, "Container click");
            CrashReportSection crashReportSection = crashReport.addElement("Click info");
            crashReportSection.add("Menu Type", () -> this.type != null ? Registries.SCREEN_HANDLER.getId(this.type).toString() : "<no type>");
            crashReportSection.add("Menu Class", () -> this.getClass().getCanonicalName());
            crashReportSection.add("Slot Count", this.slots.size());
            crashReportSection.add("Slot", slotIndex);
            crashReportSection.add("Button", button);
            crashReportSection.add("Type", (Object)actionType);
            throw new CrashException(crashReport);
        }
    }

    private void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        block40: {
            block52: {
                int j;
                block51: {
                    block47: {
                        ItemStack itemStack;
                        Slot slot;
                        ItemStack itemStack5;
                        PlayerInventory playerInventory;
                        block50: {
                            block49: {
                                block48: {
                                    block45: {
                                        ClickType clickType;
                                        block46: {
                                            block44: {
                                                block38: {
                                                    block43: {
                                                        ItemStack itemStack2;
                                                        block42: {
                                                            block41: {
                                                                block39: {
                                                                    playerInventory = player.getInventory();
                                                                    if (actionType != SlotActionType.QUICK_CRAFT) break block38;
                                                                    int i = this.quickCraftStage;
                                                                    this.quickCraftStage = ScreenHandler.unpackQuickCraftStage(button);
                                                                    if (i == 1 && this.quickCraftStage == 2 || i == this.quickCraftStage) break block39;
                                                                    this.endQuickCraft();
                                                                    break block40;
                                                                }
                                                                if (!this.getCursorStack().isEmpty()) break block41;
                                                                this.endQuickCraft();
                                                                break block40;
                                                            }
                                                            if (this.quickCraftStage != 0) break block42;
                                                            this.quickCraftButton = ScreenHandler.unpackQuickCraftButton(button);
                                                            if (ScreenHandler.shouldQuickCraftContinue(this.quickCraftButton, player)) {
                                                                this.quickCraftStage = 1;
                                                                this.quickCraftSlots.clear();
                                                            } else {
                                                                this.endQuickCraft();
                                                            }
                                                            break block40;
                                                        }
                                                        if (this.quickCraftStage != 1) break block43;
                                                        Slot slot2 = this.slots.get(slotIndex);
                                                        if (!ScreenHandler.canInsertItemIntoSlot(slot2, itemStack2 = this.getCursorStack(), true) || !slot2.canInsert(itemStack2) || this.quickCraftButton != 2 && itemStack2.getCount() <= this.quickCraftSlots.size() || !this.canInsertIntoSlot(slot2)) break block40;
                                                        this.quickCraftSlots.add(slot2);
                                                        break block40;
                                                    }
                                                    if (this.quickCraftStage == 2) {
                                                        if (!this.quickCraftSlots.isEmpty()) {
                                                            if (this.quickCraftSlots.size() == 1) {
                                                                int j2 = this.quickCraftSlots.iterator().next().id;
                                                                this.endQuickCraft();
                                                                this.internalOnSlotClick(j2, this.quickCraftButton, SlotActionType.PICKUP, player);
                                                                return;
                                                            }
                                                            ItemStack itemStack2 = this.getCursorStack().copy();
                                                            if (itemStack2.isEmpty()) {
                                                                this.endQuickCraft();
                                                                return;
                                                            }
                                                            int k = this.getCursorStack().getCount();
                                                            for (Slot slot2 : this.quickCraftSlots) {
                                                                ItemStack itemStack3 = this.getCursorStack();
                                                                if (slot2 == null || !ScreenHandler.canInsertItemIntoSlot(slot2, itemStack3, true) || !slot2.canInsert(itemStack3) || this.quickCraftButton != 2 && itemStack3.getCount() < this.quickCraftSlots.size() || !this.canInsertIntoSlot(slot2)) continue;
                                                                int l = slot2.hasStack() ? slot2.getStack().getCount() : 0;
                                                                int m = Math.min(itemStack2.getMaxCount(), slot2.getMaxItemCount(itemStack2));
                                                                int n = Math.min(ScreenHandler.calculateStackSize(this.quickCraftSlots, this.quickCraftButton, itemStack2) + l, m);
                                                                k -= n - l;
                                                                slot2.setStack(itemStack2.copyWithCount(n));
                                                            }
                                                            itemStack2.setCount(k);
                                                            this.setCursorStack(itemStack2);
                                                        }
                                                        this.endQuickCraft();
                                                    } else {
                                                        this.endQuickCraft();
                                                    }
                                                    break block40;
                                                }
                                                if (this.quickCraftStage == 0) break block44;
                                                this.endQuickCraft();
                                                break block40;
                                            }
                                            if (actionType != SlotActionType.PICKUP && actionType != SlotActionType.QUICK_MOVE || button != 0 && button != 1) break block45;
                                            ClickType clickType2 = clickType = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
                                            if (slotIndex != -999) break block46;
                                            if (this.getCursorStack().isEmpty()) break block40;
                                            if (clickType == ClickType.LEFT) {
                                                player.dropItem(this.getCursorStack(), true);
                                                this.setCursorStack(ItemStack.EMPTY);
                                            } else {
                                                player.dropItem(this.getCursorStack().split(1), true);
                                            }
                                            break block40;
                                        }
                                        if (actionType == SlotActionType.QUICK_MOVE) {
                                            if (slotIndex < 0) {
                                                return;
                                            }
                                            Slot slot3 = this.slots.get(slotIndex);
                                            if (!slot3.canTakeItems(player)) {
                                                return;
                                            }
                                            ItemStack itemStack3 = this.quickMove(player, slotIndex);
                                            while (!itemStack3.isEmpty() && ItemStack.areItemsEqual(slot3.getStack(), itemStack3)) {
                                                itemStack3 = this.quickMove(player, slotIndex);
                                            }
                                        } else {
                                            if (slotIndex < 0) {
                                                return;
                                            }
                                            Slot slot4 = this.slots.get(slotIndex);
                                            ItemStack itemStack4 = slot4.getStack();
                                            ItemStack itemStack42 = this.getCursorStack();
                                            player.onPickupSlotClick(itemStack42, slot4.getStack(), clickType);
                                            if (!this.handleSlotClick(player, clickType, slot4, itemStack4, itemStack42)) {
                                                if (itemStack4.isEmpty()) {
                                                    if (!itemStack42.isEmpty()) {
                                                        int o = clickType == ClickType.LEFT ? itemStack42.getCount() : 1;
                                                        this.setCursorStack(slot4.insertStack(itemStack42, o));
                                                    }
                                                } else if (slot4.canTakeItems(player)) {
                                                    if (itemStack42.isEmpty()) {
                                                        int o = clickType == ClickType.LEFT ? itemStack4.getCount() : (itemStack4.getCount() + 1) / 2;
                                                        Optional<ItemStack> optional = slot4.tryTakeStackRange(o, Integer.MAX_VALUE, player);
                                                        optional.ifPresent(stack -> {
                                                            this.setCursorStack((ItemStack)stack);
                                                            slot4.onTakeItem(player, (ItemStack)stack);
                                                        });
                                                    } else if (slot4.canInsert(itemStack42)) {
                                                        if (ItemStack.areItemsAndComponentsEqual(itemStack4, itemStack42)) {
                                                            int o = clickType == ClickType.LEFT ? itemStack42.getCount() : 1;
                                                            this.setCursorStack(slot4.insertStack(itemStack42, o));
                                                        } else if (itemStack42.getCount() <= slot4.getMaxItemCount(itemStack42)) {
                                                            this.setCursorStack(itemStack4);
                                                            slot4.setStack(itemStack42);
                                                        }
                                                    } else if (ItemStack.areItemsAndComponentsEqual(itemStack4, itemStack42)) {
                                                        Optional<ItemStack> optional2 = slot4.tryTakeStackRange(itemStack4.getCount(), itemStack42.getMaxCount() - itemStack42.getCount(), player);
                                                        optional2.ifPresent(stack -> {
                                                            itemStack42.increment(stack.getCount());
                                                            slot4.onTakeItem(player, (ItemStack)stack);
                                                        });
                                                    }
                                                }
                                            }
                                            slot4.markDirty();
                                        }
                                        break block40;
                                    }
                                    if (actionType != SlotActionType.SWAP || (button < 0 || button >= 9) && button != 40) break block47;
                                    itemStack5 = playerInventory.getStack(button);
                                    slot = this.slots.get(slotIndex);
                                    itemStack = slot.getStack();
                                    if (itemStack5.isEmpty() && itemStack.isEmpty()) break block40;
                                    if (!itemStack5.isEmpty()) break block48;
                                    if (!slot.canTakeItems(player)) break block40;
                                    playerInventory.setStack(button, itemStack);
                                    slot.onTake(itemStack.getCount());
                                    slot.setStack(ItemStack.EMPTY);
                                    slot.onTakeItem(player, itemStack);
                                    break block40;
                                }
                                if (!itemStack.isEmpty()) break block49;
                                if (!slot.canInsert(itemStack5)) break block40;
                                int p = slot.getMaxItemCount(itemStack5);
                                if (itemStack5.getCount() > p) {
                                    slot.setStack(itemStack5.split(p));
                                } else {
                                    playerInventory.setStack(button, ItemStack.EMPTY);
                                    slot.setStack(itemStack5);
                                }
                                break block40;
                            }
                            if (!slot.canTakeItems(player) || !slot.canInsert(itemStack5)) break block40;
                            int p = slot.getMaxItemCount(itemStack5);
                            if (itemStack5.getCount() <= p) break block50;
                            slot.setStack(itemStack5.split(p));
                            slot.onTakeItem(player, itemStack);
                            if (playerInventory.insertStack(itemStack)) break block40;
                            player.dropItem(itemStack, true);
                            break block40;
                        }
                        playerInventory.setStack(button, itemStack);
                        slot.setStack(itemStack5);
                        slot.onTakeItem(player, itemStack);
                        break block40;
                    }
                    if (actionType != SlotActionType.CLONE || !player.isInCreativeMode() || !this.getCursorStack().isEmpty() || slotIndex < 0) break block51;
                    Slot slot3 = this.slots.get(slotIndex);
                    if (!slot3.hasStack()) break block40;
                    ItemStack itemStack2 = slot3.getStack();
                    this.setCursorStack(itemStack2.copyWithCount(itemStack2.getMaxCount()));
                    break block40;
                }
                if (actionType != SlotActionType.THROW || !this.getCursorStack().isEmpty() || slotIndex < 0) break block52;
                Slot slot3 = this.slots.get(slotIndex);
                int n = j = button == 0 ? 1 : slot3.getStack().getCount();
                if (!player.canDropItems()) {
                    return;
                }
                ItemStack itemStack = slot3.takeStackRange(j, Integer.MAX_VALUE, player);
                player.dropItem(itemStack, true);
                player.dropCreativeStack(itemStack);
                if (button != 1) break block40;
                while (!itemStack.isEmpty() && ItemStack.areItemsEqual(slot3.getStack(), itemStack)) {
                    if (!player.canDropItems()) {
                        return;
                    }
                    itemStack = slot3.takeStackRange(j, Integer.MAX_VALUE, player);
                    player.dropItem(itemStack, true);
                    player.dropCreativeStack(itemStack);
                }
                break block40;
            }
            if (actionType == SlotActionType.PICKUP_ALL && slotIndex >= 0) {
                Slot slot3 = this.slots.get(slotIndex);
                ItemStack itemStack2 = this.getCursorStack();
                if (!(itemStack2.isEmpty() || slot3.hasStack() && slot3.canTakeItems(player))) {
                    int k = button == 0 ? 0 : this.slots.size() - 1;
                    int p = button == 0 ? 1 : -1;
                    for (int o = 0; o < 2; ++o) {
                        for (int q = k; q >= 0 && q < this.slots.size() && itemStack2.getCount() < itemStack2.getMaxCount(); q += p) {
                            Slot slot4 = this.slots.get(q);
                            if (!slot4.hasStack() || !ScreenHandler.canInsertItemIntoSlot(slot4, itemStack2, true) || !slot4.canTakeItems(player) || !this.canInsertIntoSlot(itemStack2, slot4)) continue;
                            ItemStack itemStack6 = slot4.getStack();
                            if (o == 0 && itemStack6.getCount() == itemStack6.getMaxCount()) continue;
                            ItemStack itemStack7 = slot4.takeStackRange(itemStack6.getCount(), itemStack2.getMaxCount() - itemStack2.getCount(), player);
                            itemStack2.increment(itemStack7.getCount());
                        }
                    }
                }
            }
        }
    }

    private boolean handleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack, ItemStack cursorStack) {
        FeatureSet featureSet = player.getEntityWorld().getEnabledFeatures();
        if (cursorStack.isItemEnabled(featureSet) && cursorStack.onStackClicked(slot, clickType, player)) {
            return true;
        }
        return stack.isItemEnabled(featureSet) && stack.onClicked(cursorStack, slot, clickType, player, this.getCursorStackReference());
    }

    private StackReference getCursorStackReference() {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return ScreenHandler.this.getCursorStack();
            }

            @Override
            public boolean set(ItemStack stack) {
                ScreenHandler.this.setCursorStack(stack);
                return true;
            }
        };
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return true;
    }

    public void onClosed(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }
        ItemStack itemStack = this.getCursorStack();
        if (!itemStack.isEmpty()) {
            ScreenHandler.offerOrDropStack(player, itemStack);
            this.setCursorStack(ItemStack.EMPTY);
        }
    }

    private static void offerOrDropStack(PlayerEntity player, ItemStack stack) {
        ServerPlayerEntity serverPlayerEntity;
        boolean bl2;
        boolean bl = player.isRemoved() && player.getRemovalReason() != Entity.RemovalReason.CHANGED_DIMENSION;
        boolean bl3 = bl2 = player instanceof ServerPlayerEntity && (serverPlayerEntity = (ServerPlayerEntity)player).isDisconnected();
        if (bl || bl2) {
            player.dropItem(stack, false);
        } else if (player instanceof ServerPlayerEntity) {
            player.getInventory().offerOrDrop(stack);
        }
    }

    protected void dropInventory(PlayerEntity player, Inventory inventory) {
        for (int i = 0; i < inventory.size(); ++i) {
            ScreenHandler.offerOrDropStack(player, inventory.removeStack(i));
        }
    }

    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }

    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        this.getSlot(slot).setStackNoCallbacks(stack);
        this.revision = revision;
    }

    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        for (int i = 0; i < stacks.size(); ++i) {
            this.getSlot(i).setStackNoCallbacks(stacks.get(i));
        }
        this.cursorStack = cursorStack;
        this.revision = revision;
    }

    public void setProperty(int id, int value) {
        this.properties.get(id).set(value);
    }

    public abstract boolean canUse(PlayerEntity var1);

    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        int j;
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (!itemStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, itemStack)) {
                    int k;
                    j = itemStack.getCount() + stack.getCount();
                    if (j <= (k = slot.getMaxItemCount(itemStack))) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < k) {
                        stack.decrement(k - itemStack.getCount());
                        itemStack.setCount(k);
                        slot.markDirty();
                        bl = true;
                    }
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)) {
                    j = slot.getMaxItemCount(stack);
                    slot.setStack(stack.split(Math.min(stack.getCount(), j)));
                    slot.markDirty();
                    bl = true;
                    break;
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return bl;
    }

    public static int unpackQuickCraftButton(int quickCraftData) {
        return quickCraftData >> 2 & 3;
    }

    public static int unpackQuickCraftStage(int quickCraftData) {
        return quickCraftData & 3;
    }

    public static int packQuickCraftData(int quickCraftStage, int buttonId) {
        return quickCraftStage & 3 | (buttonId & 3) << 2;
    }

    public static boolean shouldQuickCraftContinue(int stage, PlayerEntity player) {
        if (stage == 0) {
            return true;
        }
        if (stage == 1) {
            return true;
        }
        return stage == 2 && player.isInCreativeMode();
    }

    protected void endQuickCraft() {
        this.quickCraftStage = 0;
        this.quickCraftSlots.clear();
    }

    public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl;
        boolean bl2 = bl = slot == null || !slot.hasStack();
        if (!bl && ItemStack.areItemsAndComponentsEqual(stack, slot.getStack())) {
            return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxCount();
        }
        return bl;
    }

    public static int calculateStackSize(Set<Slot> slots, int mode, ItemStack stack) {
        return switch (mode) {
            case 0 -> MathHelper.floor((float)stack.getCount() / (float)slots.size());
            case 1 -> 1;
            case 2 -> stack.getMaxCount();
            default -> stack.getCount();
        };
    }

    public boolean canInsertIntoSlot(Slot slot) {
        return true;
    }

    public static int calculateComparatorOutput(@Nullable BlockEntity entity) {
        if (entity instanceof Inventory) {
            return ScreenHandler.calculateComparatorOutput((Inventory)((Object)entity));
        }
        return 0;
    }

    public static int calculateComparatorOutput(@Nullable Inventory inventory) {
        if (inventory == null) {
            return 0;
        }
        float f = 0.0f;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isEmpty()) continue;
            f += (float)itemStack.getCount() / (float)inventory.getMaxCount(itemStack);
        }
        return MathHelper.lerpPositive(f /= (float)inventory.size(), 0, 15);
    }

    public void setCursorStack(ItemStack stack) {
        this.cursorStack = stack;
    }

    public ItemStack getCursorStack() {
        return this.cursorStack;
    }

    public void disableSyncing() {
        this.disableSync = true;
    }

    public void enableSyncing() {
        this.disableSync = false;
    }

    public void copySharedSlots(ScreenHandler handler) {
        Slot slot;
        int i;
        HashBasedTable table = HashBasedTable.create();
        for (i = 0; i < handler.slots.size(); ++i) {
            slot = handler.slots.get(i);
            table.put((Object)slot.inventory, (Object)slot.getIndex(), (Object)i);
        }
        for (i = 0; i < this.slots.size(); ++i) {
            slot = this.slots.get(i);
            Integer integer = (Integer)table.get((Object)slot.inventory, (Object)slot.getIndex());
            if (integer == null) continue;
            this.trackedStacks.set(i, handler.trackedStacks.get(integer));
            TrackedSlot trackedSlot = handler.trackedSlots.get(integer);
            TrackedSlot trackedSlot2 = this.trackedSlots.get(i);
            if (!(trackedSlot instanceof TrackedSlot.Impl)) continue;
            TrackedSlot.Impl impl = (TrackedSlot.Impl)trackedSlot;
            if (!(trackedSlot2 instanceof TrackedSlot.Impl)) continue;
            TrackedSlot.Impl impl2 = (TrackedSlot.Impl)trackedSlot2;
            impl2.copyFrom(impl);
        }
    }

    public OptionalInt getSlotIndex(Inventory inventory, int index) {
        for (int i = 0; i < this.slots.size(); ++i) {
            Slot slot = this.slots.get(i);
            if (slot.inventory != inventory || index != slot.getIndex()) continue;
            return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    public int getRevision() {
        return this.revision;
    }

    public int nextRevision() {
        this.revision = this.revision + 1 & Short.MAX_VALUE;
        return this.revision;
    }
}

