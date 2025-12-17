/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.ErrorReporter;

public class FilteredLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<FilteredLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FilteredLootFunction.addConditionsField(instance).and(instance.group((App)ItemPredicate.CODEC.fieldOf("item_filter").forGetter(lootFunction -> lootFunction.itemFilter), (App)LootFunctionTypes.CODEC.optionalFieldOf("on_pass").forGetter(lootFunction -> lootFunction.onPass), (App)LootFunctionTypes.CODEC.optionalFieldOf("on_fail").forGetter(lootFunction -> lootFunction.onFail))).apply((Applicative)instance, FilteredLootFunction::new));
    private final ItemPredicate itemFilter;
    private final Optional<LootFunction> onPass;
    private final Optional<LootFunction> onFail;

    FilteredLootFunction(List<LootCondition> conditions, ItemPredicate itemFilter, Optional<LootFunction> onPass, Optional<LootFunction> onFail) {
        super(conditions);
        this.itemFilter = itemFilter;
        this.onPass = onPass;
        this.onFail = onFail;
    }

    public LootFunctionType<FilteredLootFunction> getType() {
        return LootFunctionTypes.FILTERED;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Optional<LootFunction> optional;
        Optional<LootFunction> optional2 = optional = this.itemFilter.test(stack) ? this.onPass : this.onFail;
        if (optional.isPresent()) {
            return (ItemStack)optional.get().apply(stack, context);
        }
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        this.onPass.ifPresent(lootFunction -> lootFunction.validate(reporter.makeChild(new ErrorReporter.MapElementContext("on_pass"))));
        this.onFail.ifPresent(lootFunction -> lootFunction.validate(reporter.makeChild(new ErrorReporter.MapElementContext("on_fail"))));
    }

    public static Builder builder(ItemPredicate itemFilter) {
        return new Builder(itemFilter);
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ItemPredicate itemFilter;
        private Optional<LootFunction> onPass = Optional.empty();
        private Optional<LootFunction> onFail = Optional.empty();

        Builder(ItemPredicate itemFilter) {
            this.itemFilter = itemFilter;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder onPass(Optional<LootFunction> onPass) {
            this.onPass = onPass;
            return this;
        }

        public Builder onFail(Optional<LootFunction> onFail) {
            this.onFail = onFail;
            return this;
        }

        @Override
        public LootFunction build() {
            return new FilteredLootFunction(this.getConditions(), this.itemFilter, this.onPass, this.onFail);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

