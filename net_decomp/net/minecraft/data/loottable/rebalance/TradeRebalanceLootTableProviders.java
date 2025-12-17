/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable.rebalance;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.loottable.LootTableProvider;
import net.minecraft.data.loottable.rebalance.TradeRebalanceChestLootTableGenerator;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryWrapper;

public class TradeRebalanceLootTableProviders {
    public static LootTableProvider createTradeRebalanceProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        return new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.LootTypeGenerator(TradeRebalanceChestLootTableGenerator::new, LootContextTypes.CHEST)), registriesFuture);
    }
}

