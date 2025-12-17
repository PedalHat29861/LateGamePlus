/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute.timeline;

import java.util.Optional;
import java.util.function.LongSupplier;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.EnvironmentAttributeFunction;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;
import net.minecraft.world.attribute.timeline.Track;
import net.minecraft.world.attribute.timeline.TrackEvaluator;
import org.jspecify.annotations.Nullable;

public class TrackAttributeModification<Value, Argument>
implements EnvironmentAttributeFunction.TimeBased<Value> {
    private final EnvironmentAttributeModifier<Value, Argument> modifiers;
    private final TrackEvaluator<Argument> evaluator;
    private final LongSupplier timeSupplier;
    private int lastComputedTime;
    private @Nullable Argument lastComputedValue;

    public TrackAttributeModification(Optional<Integer> period, EnvironmentAttributeModifier<Value, Argument> modifiers, Track<Argument> track, Interpolator<Argument> interpolator, LongSupplier timeSupplier) {
        this.modifiers = modifiers;
        this.timeSupplier = timeSupplier;
        this.evaluator = track.createEvaluator(period, interpolator);
    }

    @Override
    public Value applyTimeBased(Value object, int i) {
        if (this.lastComputedValue == null || i != this.lastComputedTime) {
            this.lastComputedTime = i;
            this.lastComputedValue = this.evaluator.get(this.timeSupplier.getAsLong());
        }
        return this.modifiers.apply(object, this.lastComputedValue);
    }
}

