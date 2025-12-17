/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.attribute.timeline;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.Interpolator;
import net.minecraft.world.attribute.timeline.EasingType;
import net.minecraft.world.attribute.timeline.Keyframe;
import net.minecraft.world.attribute.timeline.Track;

public class TrackEvaluator<T> {
    private final Optional<Integer> period;
    private final Interpolator<T> interpolator;
    private final List<Segment<T>> segments;

    TrackEvaluator(Track<T> track, Optional<Integer> period, Interpolator<T> interpolator) {
        this.period = period;
        this.interpolator = interpolator;
        this.segments = TrackEvaluator.convertToSegments(track, period);
    }

    private static <T> List<Segment<T>> convertToSegments(Track<T> track, Optional<Integer> period) {
        List<Keyframe<T>> list = track.keyframes();
        if (list.size() == 1) {
            T object = list.getFirst().value();
            return List.of(new Segment<T>(EasingType.CONSTANT, object, 0, object, 0));
        }
        ArrayList<Segment<T>> list2 = new ArrayList<Segment<T>>();
        if (period.isPresent()) {
            Keyframe<T> keyframe = list.getFirst();
            Keyframe<T> keyframe2 = list.getLast();
            list2.add(new Segment<T>(track, keyframe2, keyframe2.ticks() - period.get(), keyframe, keyframe.ticks()));
            TrackEvaluator.addSegmentsOfKeyframe(track, list, list2);
            list2.add(new Segment<T>(track, keyframe2, keyframe2.ticks(), keyframe, keyframe.ticks() + period.get()));
        } else {
            TrackEvaluator.addSegmentsOfKeyframe(track, list, list2);
        }
        return List.copyOf(list2);
    }

    private static <T> void addSegmentsOfKeyframe(Track<T> track, List<Keyframe<T>> keyframes, List<Segment<T>> segmentsOut) {
        for (int i = 0; i < keyframes.size() - 1; ++i) {
            Keyframe<T> keyframe = keyframes.get(i);
            Keyframe<T> keyframe2 = keyframes.get(i + 1);
            segmentsOut.add(new Segment<T>(track, keyframe, keyframe.ticks(), keyframe2, keyframe2.ticks()));
        }
    }

    public T get(long time) {
        long l = this.periodize(time);
        Segment<T> segment = this.getSegmentForTime(l);
        if (l <= (long)segment.fromTicks) {
            return segment.fromValue;
        }
        if (l >= (long)segment.toTicks) {
            return segment.toValue;
        }
        float f = (float)(l - (long)segment.fromTicks) / (float)(segment.toTicks - segment.fromTicks);
        float g = segment.easing.apply(f);
        return this.interpolator.apply(g, segment.fromValue, segment.toValue);
    }

    private Segment<T> getSegmentForTime(long time) {
        for (Segment<T> segment : this.segments) {
            if (time >= (long)segment.toTicks) continue;
            return segment;
        }
        return this.segments.getLast();
    }

    private long periodize(long time) {
        if (this.period.isPresent()) {
            return Math.floorMod(time, (int)this.period.get());
        }
        return time;
    }

    static final class Segment<T>
    extends Record {
        final EasingType easing;
        final T fromValue;
        final int fromTicks;
        final T toValue;
        final int toTicks;

        public Segment(Track<T> track, Keyframe<T> fromKeyframe, int fromTicks, Keyframe<T> toKeyframe, int toTicks) {
            this(track.easingType(), fromKeyframe.value(), fromTicks, toKeyframe.value(), toTicks);
        }

        Segment(EasingType easing, T fromValue, int fromTicks, T toValue, int toTicks) {
            this.easing = easing;
            this.fromValue = fromValue;
            this.fromTicks = fromTicks;
            this.toValue = toValue;
            this.toTicks = toTicks;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Segment.class, "easing;fromValue;fromTicks;toValue;toTicks", "easing", "fromValue", "fromTicks", "toValue", "toTicks"}, this, object);
        }

        public EasingType easing() {
            return this.easing;
        }

        public T fromValue() {
            return this.fromValue;
        }

        public int fromTicks() {
            return this.fromTicks;
        }

        public T toValue() {
            return this.toValue;
        }

        public int toTicks() {
            return this.toTicks;
        }
    }
}

