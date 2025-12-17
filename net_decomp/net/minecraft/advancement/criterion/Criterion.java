/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancement.criterion;

import com.mojang.serialization.Codec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionConditions;

public interface Criterion<T extends CriterionConditions> {
    public void beginTrackingCondition(PlayerAdvancementTracker var1, ConditionsContainer<T> var2);

    public void endTrackingCondition(PlayerAdvancementTracker var1, ConditionsContainer<T> var2);

    public void endTracking(PlayerAdvancementTracker var1);

    public Codec<T> getConditionsCodec();

    default public AdvancementCriterion<T> create(T conditions) {
        return new AdvancementCriterion<T>(this, conditions);
    }

    public record ConditionsContainer<T extends CriterionConditions>(T conditions, AdvancementEntry advancement, String id) {
        public void grant(PlayerAdvancementTracker tracker) {
            tracker.grantCriterion(this.advancement, this.id);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ConditionsContainer.class, "trigger;advancement;criterion", "conditions", "advancement", "id"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ConditionsContainer.class, "trigger;advancement;criterion", "conditions", "advancement", "id"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ConditionsContainer.class, "trigger;advancement;criterion", "conditions", "advancement", "id"}, this, o);
        }
    }
}

