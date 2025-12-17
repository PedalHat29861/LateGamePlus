/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.inventory;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public interface LootableInventory
extends Inventory {
    public static final String LOOT_TABLE_KEY = "LootTable";
    public static final String LOOT_TABLE_SEED_KEY = "LootTableSeed";

    public @Nullable RegistryKey<LootTable> getLootTable();

    public void setLootTable(@Nullable RegistryKey<LootTable> var1);

    default public void setLootTable(RegistryKey<LootTable> lootTableId, long lootTableSeed) {
        this.setLootTable(lootTableId);
        this.setLootTableSeed(lootTableSeed);
    }

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public BlockPos getPos();

    public @Nullable World getWorld();

    public static void setLootTable(BlockView world, Random random, BlockPos pos, RegistryKey<LootTable> lootTableId) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LootableInventory) {
            LootableInventory lootableInventory = (LootableInventory)((Object)blockEntity);
            lootableInventory.setLootTable(lootTableId, random.nextLong());
        }
    }

    default public boolean readLootTable(ReadView view) {
        RegistryKey registryKey = view.read(LOOT_TABLE_KEY, LootTable.TABLE_KEY).orElse(null);
        this.setLootTable(registryKey);
        this.setLootTableSeed(view.getLong(LOOT_TABLE_SEED_KEY, 0L));
        return registryKey != null;
    }

    default public boolean writeLootTable(WriteView view) {
        RegistryKey<LootTable> registryKey = this.getLootTable();
        if (registryKey == null) {
            return false;
        }
        view.put(LOOT_TABLE_KEY, LootTable.TABLE_KEY, registryKey);
        long l = this.getLootTableSeed();
        if (l != 0L) {
            view.putLong(LOOT_TABLE_SEED_KEY, l);
        }
        return true;
    }

    default public void generateLoot(@Nullable PlayerEntity player) {
        World world = this.getWorld();
        BlockPos blockPos = this.getPos();
        RegistryKey<LootTable> registryKey = this.getLootTable();
        if (registryKey != null && world != null && world.getServer() != null) {
            LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(registryKey);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, registryKey);
            }
            this.setLootTable(null);
            LootWorldContext.Builder builder = new LootWorldContext.Builder((ServerWorld)world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockPos));
            if (player != null) {
                builder.luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player);
            }
            lootTable.supplyInventory(this, builder.build(LootContextTypes.CHEST), this.getLootTableSeed());
        }
    }
}

