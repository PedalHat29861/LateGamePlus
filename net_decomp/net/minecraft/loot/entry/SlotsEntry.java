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
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.slot.SlotSource;
import net.minecraft.loot.slot.SlotSources;
import net.minecraft.util.ErrorReporter;

public class SlotsEntry
extends LeafEntry {
    public static final MapCodec<SlotsEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SlotSources.CODEC.fieldOf("slot_source").forGetter(entry -> entry.slotSource)).and(SlotsEntry.addLeafFields(instance)).apply((Applicative)instance, SlotsEntry::new));
    private final SlotSource slotSource;

    private SlotsEntry(SlotSource slotSource, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) {
        super(weight, quality, conditions, functions);
        this.slotSource = slotSource;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntryTypes.SLOTS;
    }

    @Override
    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
        this.slotSource.stream(context).itemCopies().filter(stack -> !stack.isEmpty()).forEach(lootConsumer);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        this.slotSource.validate(reporter.makeChild(new ErrorReporter.MapElementContext("slot_source")));
    }
}

