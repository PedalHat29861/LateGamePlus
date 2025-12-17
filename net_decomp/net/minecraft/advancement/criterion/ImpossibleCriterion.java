/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.predicate.entity.LootContextPredicateValidator;

public class ImpossibleCriterion
implements Criterion<Conditions> {
    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<Conditions> conditions) {
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<Conditions> conditions) {
    }

    @Override
    public void endTracking(PlayerAdvancementTracker tracker) {
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public record Conditions() implements CriterionConditions
    {
        public static final Codec<Conditions> CODEC = MapCodec.unitCodec((Object)new Conditions());

        @Override
        public void validate(LootContextPredicateValidator validator) {
        }
    }
}

