/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.attribute.timeline;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.LongSupplier;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;
import net.minecraft.world.attribute.timeline.Track;
import net.minecraft.world.attribute.timeline.TrackAttributeModification;

public record TimelineEntry<Value, Argument>(EnvironmentAttributeModifier<Value, Argument> modifier, Track<Argument> argumentTrack) {
    public static <Value> Codec<TimelineEntry<Value, ?>> createCodec(EnvironmentAttribute<Value> attribute) {
        MapCodec mapCodec = attribute.getType().modifierCodec().optionalFieldOf("modifier", EnvironmentAttributeModifier.override());
        return mapCodec.dispatch(TimelineEntry::modifier, Util.memoize(modifier -> TimelineEntry.createMapCodec(attribute, modifier)));
    }

    private static <Value, Argument> MapCodec<TimelineEntry<Value, Argument>> createMapCodec(EnvironmentAttribute<Value> attribute, EnvironmentAttributeModifier<Value, Argument> modifier) {
        return Track.createCodec(modifier.argumentCodec(attribute)).xmap(argumentTrack -> new TimelineEntry(modifier, argumentTrack), TimelineEntry::argumentTrack);
    }

    public TrackAttributeModification<Value, Argument> toModification(EnvironmentAttribute<Value> attribute, Optional<Integer> period, LongSupplier timeSupplier) {
        return new TrackAttributeModification<Value, Argument>(period, this.modifier, this.argumentTrack, this.modifier.argumentKeyframeLerp(attribute), timeSupplier);
    }

    public static DataResult<TimelineEntry<?, ?>> validateKeyframesInPeriod(TimelineEntry<?, ?> entry, int period) {
        return Track.validateKeyframesInPeriod(entry.argumentTrack(), period).map(track -> entry);
    }
}

