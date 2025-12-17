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
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.EntryCombiner;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.util.ErrorReporter;

public abstract class CombinedEntry
extends LootPoolEntry {
    public static final ErrorReporter.Error EMPTY_CHILDREN_LIST_ERROR = new ErrorReporter.Error(){

        @Override
        public String getMessage() {
            return "Empty children list";
        }
    };
    protected final List<LootPoolEntry> children;
    private final EntryCombiner predicate;

    protected CombinedEntry(List<LootPoolEntry> terms, List<LootCondition> conditions) {
        super(conditions);
        this.children = terms;
        this.predicate = this.combine(terms);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        if (this.children.isEmpty()) {
            reporter.report(EMPTY_CHILDREN_LIST_ERROR);
        }
        for (int i = 0; i < this.children.size(); ++i) {
            this.children.get(i).validate(reporter.makeChild(new ErrorReporter.NamedListElementContext("children", i)));
        }
    }

    protected abstract EntryCombiner combine(List<? extends EntryCombiner> var1);

    @Override
    public final boolean expand(LootContext lootContext, Consumer<LootChoice> consumer) {
        if (!this.test(lootContext)) {
            return false;
        }
        return this.predicate.expand(lootContext, consumer);
    }

    public static <T extends CombinedEntry> MapCodec<T> createCodec(Factory<T> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootPoolEntryTypes.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(entry -> entry.children)).and(CombinedEntry.addConditionsField(instance).t1()).apply((Applicative)instance, factory::create));
    }

    @FunctionalInterface
    public static interface Factory<T extends CombinedEntry> {
        public T create(List<LootPoolEntry> var1, List<LootCondition> var2);
    }
}

