/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.effect.StatusEffect;
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
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextParameter;

public class SetStewEffectLootFunction
extends ConditionalLootFunction {
    private static final Codec<List<StewEffect>> STEW_EFFECT_LIST_CODEC = StewEffect.CODEC.listOf().validate(stewEffects -> {
        ObjectOpenHashSet set = new ObjectOpenHashSet();
        for (StewEffect stewEffect : stewEffects) {
            if (set.add(stewEffect.effect())) continue;
            return DataResult.error(() -> "Encountered duplicate mob effect: '" + String.valueOf(stewEffect.effect()) + "'");
        }
        return DataResult.success((Object)stewEffects);
    });
    public static final MapCodec<SetStewEffectLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetStewEffectLootFunction.addConditionsField(instance).and((App)STEW_EFFECT_LIST_CODEC.optionalFieldOf("effects", List.of()).forGetter(function -> function.stewEffects)).apply((Applicative)instance, SetStewEffectLootFunction::new));
    private final List<StewEffect> stewEffects;

    SetStewEffectLootFunction(List<LootCondition> conditions, List<StewEffect> stewEffects) {
        super(conditions);
        this.stewEffects = stewEffects;
    }

    public LootFunctionType<SetStewEffectLootFunction> getType() {
        return LootFunctionTypes.SET_STEW_EFFECT;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return (Set)this.stewEffects.stream().flatMap(stewEffect -> stewEffect.duration().getAllowedParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (!stack.isOf(Items.SUSPICIOUS_STEW) || this.stewEffects.isEmpty()) {
            return stack;
        }
        StewEffect stewEffect = Util.getRandom(this.stewEffects, context.getRandom());
        RegistryEntry<StatusEffect> registryEntry = stewEffect.effect();
        int i = stewEffect.duration().nextInt(context);
        if (!registryEntry.value().isInstant()) {
            i *= 20;
        }
        SuspiciousStewEffectsComponent.StewEffect stewEffect2 = new SuspiciousStewEffectsComponent.StewEffect(registryEntry, i);
        stack.apply(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffectsComponent.DEFAULT, stewEffect2, SuspiciousStewEffectsComponent::with);
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    record StewEffect(RegistryEntry<StatusEffect> effect, LootNumberProvider duration) {
        public static final Codec<StewEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)StatusEffect.ENTRY_CODEC.fieldOf("type").forGetter(StewEffect::effect), (App)LootNumberProviderTypes.CODEC.fieldOf("duration").forGetter(StewEffect::duration)).apply((Applicative)instance, StewEffect::new));
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final ImmutableList.Builder<StewEffect> map = ImmutableList.builder();

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder withEffect(RegistryEntry<StatusEffect> effect, LootNumberProvider durationRange) {
            this.map.add((Object)new StewEffect(effect, durationRange));
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetStewEffectLootFunction(this.getConditions(), (List<StewEffect>)this.map.build());
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

