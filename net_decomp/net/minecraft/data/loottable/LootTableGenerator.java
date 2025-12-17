/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable;

import java.util.function.BiConsumer;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

@FunctionalInterface
public interface LootTableGenerator {
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> var1);
}

