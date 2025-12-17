/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.timeline;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.timeline.EasingType;
import net.minecraft.world.attribute.timeline.Keyframe;
import net.minecraft.world.attribute.timeline.TrackEvaluator;

public record Track<T>(List<Keyframe<T>> keyframes, EasingType easingType) {
    public Track {
        if (keyframes.isEmpty()) {
            throw new IllegalArgumentException("Track has no keyframes");
        }
    }

    public static <T> MapCodec<Track<T>> createCodec(Codec<T> valueCodec) {
        Codec codec = Keyframe.createCodec(valueCodec).listOf().validate(Track::validateKeyframes);
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)codec.fieldOf("keyframes").forGetter(Track::keyframes), (App)EasingType.CODEC.optionalFieldOf("ease", (Object)EasingType.LINEAR).forGetter(Track::easingType)).apply((Applicative)instance, Track::new));
    }

    static <T> DataResult<List<Keyframe<T>>> validateKeyframes(List<Keyframe<T>> keyframes) {
        if (keyframes.isEmpty()) {
            return DataResult.error(() -> "Keyframes must not be empty");
        }
        if (!Comparators.isInOrder(keyframes, Comparator.comparingInt(Keyframe::ticks))) {
            return DataResult.error(() -> "Keyframes must be ordered by ticks field");
        }
        if (keyframes.size() > 1) {
            int i = 0;
            int j = keyframes.getLast().ticks();
            for (Keyframe keyframe : keyframes) {
                if (keyframe.ticks() == j) {
                    if (++i > 2) {
                        return DataResult.error(() -> "More than 2 keyframes on same tick: " + keyframe.ticks());
                    }
                } else {
                    i = 0;
                }
                j = keyframe.ticks();
            }
        }
        return DataResult.success(keyframes);
    }

    public static DataResult<Track<?>> validateKeyframesInPeriod(Track<?> track, int period) {
        for (Keyframe<?> keyframe : track.keyframes()) {
            int i = keyframe.ticks();
            if (i >= 0 && i <= period) continue;
            return DataResult.error(() -> "Keyframe at tick " + keyframe.ticks() + " must be in range [0; " + period + "]");
        }
        return DataResult.success(track);
    }

    public TrackEvaluator<T> createEvaluator(Optional<Integer> period, Interpolator<T> interpolator) {
        return new TrackEvaluator<T>(this, period, interpolator);
    }

    public static class Builder<T> {
        private final ImmutableList.Builder<Keyframe<T>> keyframes = ImmutableList.builder();
        private EasingType easingType = EasingType.LINEAR;

        public Builder<T> keyframe(int ticks, T value) {
            this.keyframes.add(new Keyframe<T>(ticks, value));
            return this;
        }

        public Builder<T> easingType(EasingType easingType) {
            this.easingType = easingType;
            return this;
        }

        public Track<T> build() {
            List list = (List)Track.validateKeyframes(this.keyframes.build()).getOrThrow();
            return new Track(list, this.easingType);
        }
    }
}

