/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.entry;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.entry.RegistryEntry;

public class ItemEntry
extends LeafEntry {
    public static final MapCodec<ItemEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Item.ENTRY_CODEC.fieldOf("name").forGetter(entry -> entry.item)).and(ItemEntry.addLeafFields(instance)).apply((Applicative)instance, ItemEntry::new));
    private final RegistryEntry<Item> item;

    private ItemEntry(RegistryEntry<Item> item, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.item = item;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.ITEM;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        lootConsumer.accept(new ItemStack(this.item));
    }

    public static LeafEntry.Builder<?> builder(ItemConvertible drop) {
        return ItemEntry.builder((int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) -> new ItemEntry(drop.asItem().getRegistryEntry(), weight, quality, conditions, functions));
    }
}

