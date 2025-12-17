/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.MathHelper;

public class SetEnchantmentsLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetEnchantmentsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetEnchantmentsLootFunction.addConditionsField(instance).and(instance.group((App)Codec.unboundedMap(Enchantment.ENTRY_CODEC, LootNumberProviderTypes.CODEC).optionalFieldOf("enchantments", Map.of()).forGetter(function -> function.enchantments), (App)Codec.BOOL.fieldOf("add").orElse((Object)false).forGetter(function -> function.add))).apply((Applicative)instance, SetEnchantmentsLootFunction::new));
    private final Map<RegistryEntry<Enchantment>, LootNumberProvider> enchantments;
    private final boolean add;

    SetEnchantmentsLootFunction(List<LootCondition> conditions, Map<RegistryEntry<Enchantment>, LootNumberProvider> enchantments, boolean add) {
        super(conditions);
        this.enchantments = Map.copyOf(enchantments);
        this.add = add;
    }

    public LootFunctionType<SetEnchantmentsLootFunction> getType() {
        return LootFunctionTypes.SET_ENCHANTMENTS;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return (Set)this.enchantments.values().stream().flatMap(numberProvider -> numberProvider.getAllowedParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isOf(Items.BOOK)) {
            stack = stack.withItem(Items.ENCHANTED_BOOK);
        }
        EnchantmentHelper.apply(stack, builder -> {
            if (this.add) {
                this.enchantments.forEach((enchantment, level) -> builder.set((RegistryEntry<Enchantment>)enchantment, MathHelper.clamp(builder.getLevel((RegistryEntry<Enchantment>)enchantment) + level.nextInt(context), 0, 255)));
            } else {
                this.enchantments.forEach((enchantment, level) -> builder.set((RegistryEntry<Enchantment>)enchantment, MathHelper.clamp(level.nextInt(context), 0, 255)));
            }
        });
        return stack;
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ImmutableMap.Builder<RegistryEntry<Enchantment>, LootNumberProvider> enchantments = ImmutableMap.builder();
        private final boolean add;

        public Builder() {
            this(false);
        }

        public Builder(boolean add) {
            this.add = add;
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder enchantment(RegistryEntry<Enchantment> enchantment, LootNumberProvider level) {
            this.enchantments.put(enchantment, (Object)level);
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetEnchantmentsLootFunction(this.getConditions(), (Map<RegistryEntry<Enchantment>, LootNumberProvider>)this.enchantments.build(), this.add);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

