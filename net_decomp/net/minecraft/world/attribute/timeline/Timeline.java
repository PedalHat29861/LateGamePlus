/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.timeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.timeline.TimelineEntry;
import net.minecraft.world.attribute.timeline.Track;
import net.minecraft.world.attribute.timeline.TrackAttributeModification;

public class Timeline {
    public static final Codec<RegistryEntry<Timeline>> REGISTRY_CODEC = RegistryFixedCodec.of(RegistryKeys.TIMELINE);
    private static final Codec<Map<EnvironmentAttribute<?>, TimelineEntry<?, ?>>> TRACKS_BY_ATTRIBUTE_CODEC = Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(TimelineEntry::createCodec));
    public static final Codec<Timeline> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.POSITIVE_INT.optionalFieldOf("period_ticks").forGetter(timeline -> timeline.periodTicks), (App)TRACKS_BY_ATTRIBUTE_CODEC.optionalFieldOf("tracks", Map.of()).forGetter(timeline -> timeline.tracks)).apply((Applicative)instance, Timeline::new)).validate(Timeline::validate);
    public static final Codec<Timeline> NETWORK_CODEC = CODEC.xmap(Timeline::retainSyncedAttributes, Timeline::retainSyncedAttributes);
    private final Optional<Integer> periodTicks;
    private final Map<EnvironmentAttribute<?>, TimelineEntry<?, ?>> tracks;

    private static Timeline retainSyncedAttributes(Timeline timeline) {
        Map<EnvironmentAttribute<?>, TimelineEntry<?, ?>> map = Map.copyOf(Maps.filterKeys(timeline.tracks, EnvironmentAttribute::isSynced));
        return new Timeline(timeline.periodTicks, map);
    }

    Timeline(Optional<Integer> periodTicks, Map<EnvironmentAttribute<?>, TimelineEntry<?, ?>> entries) {
        this.periodTicks = periodTicks;
        this.tracks = entries;
    }

    private static DataResult<Timeline> validate(Timeline timeline2) {
        if (timeline2.periodTicks.isEmpty()) {
            return DataResult.success((Object)timeline2);
        }
        int i = timeline2.periodTicks.get();
        DataResult dataResult = DataResult.success((Object)timeline2);
        for (TimelineEntry<?, ?> timelineEntry2 : timeline2.tracks.values()) {
            dataResult = dataResult.apply2stable((timeline, timelineEntry) -> timeline, TimelineEntry.validateKeyframesInPeriod(timelineEntry2, i));
        }
        return dataResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getEffectiveTimeOfDay(World world) {
        long l = this.getRawTimeOfDay(world);
        if (this.periodTicks.isEmpty()) {
            return l;
        }
        return l % (long)this.periodTicks.get().intValue();
    }

    public long getRawTimeOfDay(World world) {
        return world.getTimeOfDay();
    }

    public Optional<Integer> getPeriod() {
        return this.periodTicks;
    }

    public Set<EnvironmentAttribute<?>> getAttributes() {
        return this.tracks.keySet();
    }

    public <Value> TrackAttributeModification<Value, ?> getModification(EnvironmentAttribute<Value> attribute, LongSupplier timeSupplier) {
        TimelineEntry<?, ?> timelineEntry = this.tracks.get(attribute);
        if (timelineEntry == null) {
            throw new IllegalStateException("Timeline has no track for " + String.valueOf(attribute));
        }
        return timelineEntry.toModification(attribute, this.periodTicks, timeSupplier);
    }

    public static class Builder {
        private Optional<Integer> periodTicks = Optional.empty();
        private final ImmutableMap.Builder<EnvironmentAttribute<?>, TimelineEntry<?, ?>> entries = ImmutableMap.builder();

        Builder() {
        }

        public Builder period(int periodTicks) {
            this.periodTicks = Optional.of(periodTicks);
            return this;
        }

        public <Value, Argument> Builder entry(EnvironmentAttribute<Value> attribute, EnvironmentAttributeModifier<Value, Argument> modifier, Consumer<Track.Builder<Argument>> builderCallback) {
            attribute.getType().validate(modifier);
            Track.Builder builder = new Track.Builder();
            builderCallback.accept(builder);
            this.entries.put(attribute, new TimelineEntry<Value, Argument>(modifier, builder.build()));
            return this;
        }

        public <Value> Builder entry(EnvironmentAttribute<Value> attribute, Consumer<Track.Builder<Value>> builderCallback) {
            return this.entry(attribute, EnvironmentAttributeModifier.override(), builderCallback);
        }

        public Timeline build() {
            return new Timeline(this.periodTicks, (Map<EnvironmentAttribute<?>, TimelineEntry<?, ?>>)this.entries.build());
        }
    }
}

