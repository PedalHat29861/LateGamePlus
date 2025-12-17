/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class StorageMinecartEntity
extends AbstractMinecartEntity
implements VehicleInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private @Nullable RegistryKey<LootTable> lootTable;
    private long lootTableSeed;

    protected StorageMinecartEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void killAndDropSelf(ServerWorld world, DamageSource damageSource) {
        super.killAndDropSelf(world, damageSource);
        this.onBroken(damageSource, world, this);
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
    public void remove(Entity.RemovalReason reason) {
        if (!this.getEntityWorld().isClient() && reason.shouldDestroy()) {
            ItemScatterer.spawn(this.getEntityWorld(), this, (Inventory)this);
        }
        super.remove(reason);
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
    public ActionResult interact(PlayerEntity player, Hand hand) {
        return this.open(player);
    }

    @Override
    protected Vec3d applySlowdown(Vec3d velocity) {
        float f = 0.98f;
        if (this.lootTable == null) {
            int i = 15 - ScreenHandler.calculateComparatorOutput(this);
            f += (float)i * 0.001f;
        }
        if (this.isTouchingWater()) {
            f *= 0.95f;
        }
        return velocity.multiply(f, 0.0, f);
    }

    @Override
    public void clear() {
        this.clearInventory();
    }

    public void setLootTable(RegistryKey<LootTable> lootTable, long lootSeed) {
        this.lootTable = lootTable;
        this.lootTableSeed = lootSeed;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (this.lootTable == null || !playerEntity.isSpectator()) {
            this.generateInventoryLoot(playerInventory.player);
            return this.getScreenHandler(i, playerInventory);
        }
        return null;
    }

    protected abstract ScreenHandler getScreenHandler(int var1, PlayerInventory var2);

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
}

