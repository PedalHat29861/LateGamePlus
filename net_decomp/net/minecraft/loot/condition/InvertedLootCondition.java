/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.context.ContextParameter;

public record InvertedLootCondition(LootCondition term) implements LootCondition
{
    public static final MapCodec<InvertedLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootCondition.CODEC.fieldOf("term").forGetter(InvertedLootCondition::term)).apply((Applicative)instance, InvertedLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.INVERTED;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return !this.term.test(lootContext);
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.term.getAllowedParameters();
    }

    @Override
    public void validate(LootTableReporter reporter) {
        LootCondition.super.validate(reporter);
        this.term.validate(reporter);
    }

    public static LootCondition.Builder builder(LootCondition.Builder term) {
        InvertedLootCondition invertedLootCondition = new InvertedLootCondition(term.build());
        return () -> invertedLootCondition;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

