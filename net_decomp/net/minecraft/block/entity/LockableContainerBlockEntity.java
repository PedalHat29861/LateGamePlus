/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class LockableContainerBlockEntity
extends BlockEntity
implements Inventory,
NamedScreenHandlerFactory,
Nameable {
    private ContainerLock lock = ContainerLock.EMPTY;
    private @Nullable Text customName;

    protected LockableContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.lock = ContainerLock.read(view);
        this.customName = LockableContainerBlockEntity.tryParseCustomName(view, "CustomName");
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        this.lock.write(view);
        view.putNullable("CustomName", TextCodecs.CODEC, this.customName);
    }

    @Override
    public Text getName() {
        if (this.customName != null) {
            return this.customName;
        }
        return this.getContainerName();
    }

    @Override
    public Text getDisplayName() {
        return this.getName();
    }

    @Override
    public @Nullable Text getCustomName() {
        return this.customName;
    }

    protected abstract Text getContainerName();

    public boolean checkUnlocked(PlayerEntity player) {
        return this.lock.checkUnlocked(player);
    }

    public static void handleLocked(Vec3d containerPos, PlayerEntity player, Text name) {
        World world = player.getEntityWorld();
        player.sendMessage(Text.translatable("container.isLocked", name), true);
        if (!world.isClient()) {
            world.playSound(null, containerPos.getX(), containerPos.getY(), containerPos.getZ(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    public boolean isLocked() {
        return !this.lock.equals(ContainerLock.EMPTY);
    }

    protected abstract DefaultedList<ItemStack> getHeldStacks();

    protected abstract void setHeldStacks(DefaultedList<ItemStack> var1);

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.getHeldStacks()) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.getHeldStacks().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.getHeldStacks(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }
        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.getHeldStacks(), slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.getHeldStacks().set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        this.getHeldStacks().clear();
    }

    @Override
    public @Nullable ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (this.checkUnlocked(playerEntity)) {
            return this.createScreenHandler(i, playerInventory);
        }
        LockableContainerBlockEntity.handleLocked(this.getPos().toCenterPos(), playerEntity, this.getDisplayName());
        return null;
    }

    protected abstract ScreenHandler createScreenHandler(int var1, PlayerInventory var2);

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
        this.lock = components.getOrDefault(DataComponentTypes.LOCK, ContainerLock.EMPTY);
        components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(this.getHeldStacks());
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
        if (this.isLocked()) {
            builder.add(DataComponentTypes.LOCK, this.lock);
        }
        builder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(this.getHeldStacks()));
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        view.remove("CustomName");
        view.remove("lock");
        view.remove("Items");
    }
}

