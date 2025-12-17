/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.function.SetNameLootFunction;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.collection.ListOperation;
import net.minecraft.util.context.ContextParameter;
import org.jspecify.annotations.Nullable;

public class SetLoreLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetLoreLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetLoreLootFunction.addConditionsField(instance).and(instance.group((App)TextCodecs.CODEC.sizeLimitedListOf(256).fieldOf("lore").forGetter(function -> function.lore), (App)ListOperation.createCodec(256).forGetter(function -> function.operation), (App)LootContext.EntityReference.CODEC.optionalFieldOf("entity").forGetter(function -> function.entity))).apply((Applicative)instance, SetLoreLootFunction::new));
    private final List<Text> lore;
    private final ListOperation operation;
    private final Optional<LootContext.EntityReference> entity;

    public SetLoreLootFunction(List<LootCondition> conditions, List<Text> lore, ListOperation operation, Optional<LootContext.EntityReference> entity) {
        super(conditions);
        this.lore = List.copyOf(lore);
        this.operation = operation;
        this.entity = entity;
    }

    public LootFunctionType<SetLoreLootFunction> getType() {
        return LootFunctionTypes.SET_LORE;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.entity.map(entity -> Set.of(entity.contextParam())).orElseGet(Set::of);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> new LoreComponent(this.getNewLoreTexts((LoreComponent)component, context)));
        return stack;
    }

    private List<Text> getNewLoreTexts(@Nullable LoreComponent current, LootContext context) {
        if (current == null && this.lore.isEmpty()) {
            return List.of();
        }
        UnaryOperator<Text> unaryOperator = SetNameLootFunction.applySourceEntity(context, this.entity.orElse(null));
        List list = this.lore.stream().map(unaryOperator).toList();
        return this.operation.apply(current.lines(), list, 256);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private Optional<LootContext.EntityReference> target = Optional.empty();
        private final ImmutableList.Builder<Text> lore = ImmutableList.builder();
        private ListOperation operation = ListOperation.Append.INSTANCE;

        public Builder operation(ListOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder target(LootContext.EntityReference target) {
            this.target = Optional.of(target);
            return this;
        }

        public Builder lore(Text lore) {
            this.lore.add((Object)lore);
            return this;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetLoreLootFunction(this.getConditions(), (List<Text>)this.lore.build(), this.operation, this.target);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

