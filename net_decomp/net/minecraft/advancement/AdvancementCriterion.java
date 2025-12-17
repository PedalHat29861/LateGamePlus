/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.util.dynamic.Codecs;

public record AdvancementCriterion<T extends CriterionConditions>(Criterion<T> trigger, T conditions) {
    private static final MapCodec<AdvancementCriterion<?>> MAP_CODEC = Codecs.parameters("trigger", "conditions", Criteria.CODEC, AdvancementCriterion::trigger, AdvancementCriterion::getCodec);
    public static final Codec<AdvancementCriterion<?>> CODEC = MAP_CODEC.codec();

    private static <T extends CriterionConditions> Codec<AdvancementCriterion<T>> getCodec(Criterion<T> criterion) {
        return criterion.getConditionsCodec().xmap(conditions -> new AdvancementCriterion<CriterionConditions>(criterion, (CriterionConditions)conditions), AdvancementCriterion::conditions);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{AdvancementCriterion.class, "trigger;triggerInstance", "trigger", "conditions"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{AdvancementCriterion.class, "trigger;triggerInstance", "trigger", "conditions"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{AdvancementCriterion.class, "trigger;triggerInstance", "trigger", "conditions"}, this, object);
    }
}

