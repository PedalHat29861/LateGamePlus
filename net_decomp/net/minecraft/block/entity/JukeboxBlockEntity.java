/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.jukebox.JukeboxManager;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class JukeboxBlockEntity
extends BlockEntity
implements SingleStackInventory.SingleStackBlockEntityInventory {
    public static final String RECORD_ITEM_NBT_KEY = "RecordItem";
    public static final String TICKS_SINCE_SONG_STARTED_NBT_KEY = "ticks_since_song_started";
    private ItemStack recordStack = ItemStack.EMPTY;
    private final JukeboxManager manager = new JukeboxManager(this::onManagerChange, this.getPos());

    public JukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.JUKEBOX, pos, state);
    }

    public JukeboxManager getManager() {
        return this.manager;
    }

    public void onManagerChange() {
        this.world.updateNeighbors(this.getPos(), this.getCachedState().getBlock());
        this.markDirty();
    }

    private void onRecordStackChanged(boolean hasRecord) {
        if (this.world == null || this.world.getBlockState(this.getPos()) != this.getCachedState()) {
            return;
        }
        this.world.setBlockState(this.getPos(), (BlockState)this.getCachedState().with(JukeboxBlock.HAS_RECORD, hasRecord), 2);
        this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, this.getPos(), GameEvent.Emitter.of(this.getCachedState()));
    }

    public void dropRecord() {
        if (this.world == null || this.world.isClient()) {
            return;
        }
        BlockPos blockPos = this.getPos();
        ItemStack itemStack = this.getStack();
        if (itemStack.isEmpty()) {
            return;
        }
        this.emptyStack();
        Vec3d vec3d = Vec3d.add(blockPos, 0.5, 1.01, 0.5).addHorizontalRandom(this.world.random, 0.7f);
        ItemStack itemStack2 = itemStack.copy();
        ItemEntity itemEntity = new ItemEntity(this.world, vec3d.getX(), vec3d.getY(), vec3d.getZ(), itemStack2);
        itemEntity.setToDefaultPickupDelay();
        this.world.spawnEntity(itemEntity);
        this.onManagerChange();
    }

    public static void tick(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity) {
        blockEntity.manager.tick(world, state);
    }

    public int getComparatorOutput() {
        return JukeboxSong.getSongEntryFromStack(this.world.getRegistryManager(), this.recordStack).map(RegistryEntry::value).map(JukeboxSong::comparatorOutput).orElse(0);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        ItemStack itemStack = view.read(RECORD_ITEM_NBT_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (!this.recordStack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(itemStack, this.recordStack)) {
            this.manager.stopPlaying(this.world, this.getCachedState());
        }
        this.recordStack = itemStack;
        view.getOptionalLong(TICKS_SINCE_SONG_STARTED_NBT_KEY).ifPresent(ticksSinceSongStarted -> JukeboxSong.getSongEntryFromStack(view.getRegistries(), this.recordStack).ifPresent(song -> this.manager.setValues((RegistryEntry<JukeboxSong>)song, (long)ticksSinceSongStarted)));
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.getStack().isEmpty()) {
            view.put(RECORD_ITEM_NBT_KEY, ItemStack.CODEC, this.getStack());
        }
        if (this.manager.getSong() != null) {
            view.putLong(TICKS_SINCE_SONG_STARTED_NBT_KEY, this.manager.getTicksSinceSongStarted());
        }
    }

    @Override
    public ItemStack getStack() {
        return this.recordStack;
    }

    @Override
    public ItemStack decreaseStack(int count) {
        ItemStack itemStack = this.recordStack;
        this.setStack(ItemStack.EMPTY);
        return itemStack;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.recordStack = stack;
        boolean bl = !this.recordStack.isEmpty();
        Optional<RegistryEntry<JukeboxSong>> optional = JukeboxSong.getSongEntryFromStack(this.world.getRegistryManager(), this.recordStack);
        this.onRecordStackChanged(bl);
        if (bl && optional.isPresent()) {
            this.manager.startPlaying(this.world, optional.get());
        } else {
            this.manager.stopPlaying(this.world, this.getCachedState());
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        this.world.emitGameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getPos(), GameEvent.Emitter.of(this.getCachedState()));
        this.world.syncWorldEvent(1011, this.getPos(), 0);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE) && this.getStack(slot).isEmpty();
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return hopperInventory.containsAny(ItemStack::isEmpty);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        this.dropRecord();
    }

    @VisibleForTesting
    public void setDisc(ItemStack stack) {
        this.recordStack = stack;
        JukeboxSong.getSongEntryFromStack(this.world.getRegistryManager(), stack).ifPresent(song -> this.manager.setValues((RegistryEntry<JukeboxSong>)song, 0L));
        this.world.updateNeighbors(this.getPos(), this.getCachedState().getBlock());
        this.markDirty();
    }

    @VisibleForTesting
    public void reloadDisc() {
        JukeboxSong.getSongEntryFromStack(this.world.getRegistryManager(), this.getStack()).ifPresent(song -> this.manager.startPlaying(this.world, (RegistryEntry<JukeboxSong>)song));
    }
}

