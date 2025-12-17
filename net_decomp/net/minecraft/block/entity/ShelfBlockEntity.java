/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShelfBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ListInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ShelfBlockEntity
extends BlockEntity
implements HeldItemContext,
ListInventory {
    public static final int SLOT_COUNT = 3;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ALIGN_ITEMS_TO_BOTTOM_KEY = "align_items_to_bottom";
    private final DefaultedList<ItemStack> heldStacks = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private boolean alignItemsToBottom;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.SHELF, pos, state);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.heldStacks.clear();
        Inventories.readData(view, this.heldStacks);
        this.alignItemsToBottom = view.getBoolean(ALIGN_ITEMS_TO_BOTTOM_KEY, false);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.heldStacks, true);
        view.putBoolean(ALIGN_ITEMS_TO_BOTTOM_KEY, this.alignItemsToBottom);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.getReporterContext(), LOGGER);){
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, registries);
            Inventories.writeData(nbtWriteView, this.heldStacks, true);
            nbtWriteView.putBoolean(ALIGN_ITEMS_TO_BOTTOM_KEY, this.alignItemsToBottom);
            NbtCompound nbtCompound = nbtWriteView.getNbt();
            return nbtCompound;
        }
    }

    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return this.heldStacks;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    public ItemStack swapStackNoMarkDirty(int slot, ItemStack stack) {
        ItemStack itemStack = this.removeStack(slot);
        this.setStackNoMarkDirty(slot, stack);
        return itemStack;
    }

    public void markDirty( @Nullable RegistryEntry.Reference<GameEvent> gameEvent) {
        super.markDirty();
        if (this.world != null) {
            if (gameEvent != null) {
                this.world.emitGameEvent(gameEvent, this.pos, GameEvent.Emitter.of(this.getCachedState()));
            }
            this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override
    public void markDirty() {
        this.markDirty(GameEvent.BLOCK_ACTIVATE);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(this.heldStacks);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(this.heldStacks));
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        view.remove("Items");
    }

    @Override
    public World getEntityWorld() {
        return this.world;
    }

    @Override
    public Vec3d getEntityPos() {
        return this.getPos().toCenterPos();
    }

    @Override
    public float getBodyYaw() {
        return this.getCachedState().get(ShelfBlock.FACING).getOpposite().getPositiveHorizontalDegrees();
    }

    public boolean shouldAlignItemsToBottom() {
        return this.alignItemsToBottom;
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

