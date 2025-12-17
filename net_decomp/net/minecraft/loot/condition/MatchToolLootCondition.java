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
import java.util.Optional;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.context.ContextParameter;

public record MatchToolLootCondition(Optional<ItemPredicate> predicate) implements LootCondition
{
    public static final MapCodec<MatchToolLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(MatchToolLootCondition::predicate)).apply((Applicative)instance, MatchToolLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.MATCH_TOOL;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.TOOL);
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.get(LootContextParameters.TOOL);
        return itemStack != null && (this.predicate.isEmpty() || this.predicate.get().test(itemStack));
    }

    public static LootCondition.Builder builder(ItemPredicate.Builder predicate) {
        return () -> new MatchToolLootCondition(Optional.of(predicate.build()));
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

