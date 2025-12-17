/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.entity.ContainerUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public abstract class AbstractChestBoatEntity
extends AbstractBoatEntity
implements RideableInventory,
VehicleInventory {
    private static final int INVENTORY_SIZE = 27;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    private @Nullable RegistryKey<LootTable> lootTable;
    private long lootTableSeed;

    public AbstractChestBoatEntity(EntityType<? extends AbstractChestBoatEntity> entityType, World world, Supplier<Item> supplier) {
        super(entityType, world, supplier);
    }

    @Override
    protected float getPassengerHorizontalOffset() {
        return 0.15f;
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        this.writeInventoryToData(view);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.readInventoryFromData(view);
    }

    @Override
    public void killAndDropSelf(ServerWorld world, DamageSource damageSource) {
        this.killAndDropItem(world, this.asItem());
        this.onBroken(damageSource, world, this);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.getEntityWorld().isClient() && reason.shouldDestroy()) {
            ItemScatterer.spawn(this.getEntityWorld(), this, (Inventory)this);
        }
        super.remove(reason);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interact(player, hand);
        if (actionResult != ActionResult.PASS) {
            return actionResult;
        }
        if (!this.canAddPassenger(player) || player.shouldCancelInteraction()) {
            World world;
            ActionResult actionResult2 = this.open(player);
            if (actionResult2.isAccepted() && (world = player.getEntityWorld()) instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
                PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
            }
            return actionResult2;
        }
        return ActionResult.PASS;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        player.openHandledScreen(this);
        World world = player.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
            PiglinBrain.onGuardedBlockInteracted(serverWorld, player, true);
        }
    }

    @Override
    public void clear() {
        this.clearInventory();
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.getInventoryStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.removeInventoryStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.removeInventoryStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.setInventoryStack(slot, stack);
    }

    @Override
    public StackReference getStackReference(int slot) {
        return this.getInventoryStackReference(slot);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.canPlayerAccess(player);
    }

    @Override
    public @Nullable ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (this.lootTable == null || !playerEntity.isSpectator()) {
            this.generateLoot(playerInventory.player);
            return GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, this);
        }
        return null;
    }

    public void generateLoot(@Nullable PlayerEntity player) {
        this.generateInventoryLoot(player);
    }

    @Override
    public @Nullable RegistryKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable RegistryKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    @Override
    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    @Override
    public void resetInventory() {
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
    }

    @Override
    public void onClose(ContainerUser user) {
        this.getEntityWorld().emitGameEvent(GameEvent.CONTAINER_CLOSE, this.getEntityPos(), GameEvent.Emitter.of(user.asLivingEntity()));
    }
}

