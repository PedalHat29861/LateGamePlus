/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.context.ContextParameter;

public record ValueCheckLootCondition(LootNumberProvider value, BoundedIntUnaryOperator range) implements LootCondition
{
    public static final MapCodec<ValueCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootNumberProviderTypes.CODEC.fieldOf("value").forGetter(ValueCheckLootCondition::value), (App)BoundedIntUnaryOperator.CODEC.fieldOf("range").forGetter(ValueCheckLootCondition::range)).apply((Applicative)instance, ValueCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.VALUE_CHECK;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Sets.union(this.value.getAllowedParameters(), this.range.getRequiredParameters());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return this.range.test(lootContext, this.value.nextInt(lootContext));
    }

    public static LootCondition.Builder builder(LootNumberProvider value, BoundedIntUnaryOperator range) {
        return () -> new ValueCheckLootCondition(value, range);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ValueCheckLootCondition.class, "provider;range", "value", "range"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ValueCheckLootCondition.class, "provider;range", "value", "range"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ValueCheckLootCondition.class, "provider;range", "value", "range"}, this, object);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

