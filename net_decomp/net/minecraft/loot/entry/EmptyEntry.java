/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.entry;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;

public class EmptyEntry
extends LeafEntry {
    public static final MapCodec<EmptyEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> EmptyEntry.addLeafFields(instance).apply((Applicative)instance, EmptyEntry::new));

    private EmptyEntry(int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.EMPTY;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
    }

    public static LeafEntry.Builder<?> builder() {
        return EmptyEntry.builder(EmptyEntry::new);
    }
}

