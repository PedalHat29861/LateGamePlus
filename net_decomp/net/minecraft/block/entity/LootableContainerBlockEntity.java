/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;

public abstract class LootableContainerBlockEntity
extends LockableContainerBlockEntity
implements LootableInventory {
    protected @Nullable RegistryKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;

    protected LootableContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
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
    public boolean isEmpty() {
        this.generateLoot(null);
        return super.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        this.generateLoot(null);
        return super.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.generateLoot(null);
        return super.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.generateLoot(null);
        return super.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.generateLoot(null);
        super.setStack(slot, stack);
    }

    @Override
    public boolean checkUnlocked(PlayerEntity player) {
        return super.checkUnlocked(player) && (this.lootTable == null || !player.isSpectator());
    }

    @Override
    public @Nullable ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        if (this.checkUnlocked(playerEntity)) {
            this.generateLoot(playerInventory.player);
            return this.createScreenHandler(i, playerInventory);
        }
        LockableContainerBlockEntity.handleLocked(this.getPos().toCenterPos(), playerEntity, this.getDisplayName());
        return null;
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        ContainerLootComponent containerLootComponent = components.get(DataComponentTypes.CONTAINER_LOOT);
        if (containerLootComponent != null) {
            this.lootTable = containerLootComponent.lootTable();
            this.lootTableSeed = containerLootComponent.seed();
        }
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        if (this.lootTable != null) {
            builder.add(DataComponentTypes.CONTAINER_LOOT, new ContainerLootComponent(this.lootTable, this.lootTableSeed));
        }
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("LootTable");
        view.remove("LootTableSeed");
    }
}

