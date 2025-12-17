/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeAccess;
import net.minecraft.world.attribute.EnvironmentAttributeFunction;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.WeatherAttributes;
import net.minecraft.world.attribute.WeightedAttributeList;
import net.minecraft.world.attribute.timeline.Timeline;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

public class WorldEnvironmentAttributeAccess
implements EnvironmentAttributeAccess {
    private final Map<EnvironmentAttribute<?>, Entry<?>> entries = new Reference2ObjectOpenHashMap();

    WorldEnvironmentAttributeAccess(Map<EnvironmentAttribute<?>, List<EnvironmentAttributeFunction<?>>> modificationsByAttribute) {
        modificationsByAttribute.forEach((attribute, mods) -> this.entries.put((EnvironmentAttribute<?>)attribute, this.computeEntry((EnvironmentAttribute)attribute, (List<? extends EnvironmentAttributeFunction<?>>)mods)));
    }

    private <Value> Entry<Value> computeEntry(EnvironmentAttribute<Value> attribute, List<? extends EnvironmentAttributeFunction<?>> mods) {
        Object e;
        ArrayList list = new ArrayList(mods);
        Value object = attribute.getDefaultValue();
        while (!list.isEmpty() && (e = list.getFirst()) instanceof EnvironmentAttributeFunction.Constant) {
            EnvironmentAttributeFunction.Constant constant = (EnvironmentAttributeFunction.Constant)e;
            object = constant.applyConstant(object);
            list.removeFirst();
        }
        boolean bl = list.stream().anyMatch(function -> function instanceof EnvironmentAttributeFunction.Positional);
        return new Entry<Value>(attribute, object, List.copyOf(list), bl);
    }

    public static Builder builder() {
        return new Builder();
    }

    static void addModifiersFromWorld(Builder builder, World world) {
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();
        BiomeAccess biomeAccess = world.getBiomeAccess();
        LongSupplier longSupplier = world::getTimeOfDay;
        WorldEnvironmentAttributeAccess.addModifiersFromDimension(builder, world.getDimension());
        WorldEnvironmentAttributeAccess.addModifiersFromBiomes(builder, dynamicRegistryManager.getOrThrow(RegistryKeys.BIOME), biomeAccess);
        world.getDimension().timelines().forEach(attribute -> builder.addFromTimeline((RegistryEntry<Timeline>)attribute, longSupplier));
        if (world.canHaveWeather()) {
            WeatherAttributes.addWeatherAttributes(builder, WeatherAttributes.WeatherAccess.ofWorld(world));
        }
    }

    private static void addModifiersFromDimension(Builder builder, DimensionType dimensionType) {
        builder.addFromMap(dimensionType.attributes());
    }

    private static void addModifiersFromBiomes(Builder builder, RegistryWrapper<Biome> biome2, BiomeAccess biomeAccess) {
        Stream stream = biome2.streamEntries().flatMap(biome -> ((Biome)biome.value()).getEnvironmentAttributes().keySet().stream()).distinct();
        stream.forEach(attribute -> WorldEnvironmentAttributeAccess.addModifiersFromBiomes(builder, attribute, biomeAccess));
    }

    private static <Value> void addModifiersFromBiomes(Builder builder, EnvironmentAttribute<Value> attribute, BiomeAccess biomeAccess) {
        builder.positional(attribute, (value, pos, weightedAttributeList) -> {
            if (weightedAttributeList != null && attribute.isInterpolated()) {
                return weightedAttributeList.interpolate(attribute, value);
            }
            RegistryEntry<Biome> registryEntry = biomeAccess.getBiomeForNoiseGen(pos.x, pos.y, pos.z);
            return registryEntry.value().getEnvironmentAttributes().apply(attribute, value);
        });
    }

    public void tick() {
        this.entries.values().forEach(Entry::tick);
    }

    private <Value> @Nullable Entry<Value> getEntry(EnvironmentAttribute<Value> attribute) {
        return this.entries.get(attribute);
    }

    @Override
    public <Value> Value getAttributeValue(EnvironmentAttribute<Value> attribute) {
        if (SharedConstants.isDevelopment && attribute.isPositional()) {
            throw new IllegalStateException("Position must always be provided for positional attribute " + String.valueOf(attribute));
        }
        Entry<Value> entry = this.getEntry(attribute);
        if (entry == null) {
            return attribute.getDefaultValue();
        }
        return entry.get();
    }

    @Override
    public <Value> Value getAttributeValue(EnvironmentAttribute<Value> attribute, Vec3d pos, @Nullable WeightedAttributeList pool) {
        Entry<Value> entry = this.getEntry(attribute);
        if (entry == null) {
            return attribute.getDefaultValue();
        }
        return entry.getAt(pos, pool);
    }

    @VisibleForTesting
    <Value> Value getDefaultValue(EnvironmentAttribute<Value> attribute) {
        Entry<Value> entry = this.getEntry(attribute);
        return entry != null ? entry.defaultValue : attribute.getDefaultValue();
    }

    @VisibleForTesting
    boolean isPositional(EnvironmentAttribute<?> attribute) {
        Entry<?> entry = this.getEntry(attribute);
        return entry != null && entry.positional;
    }

    static class Entry<Value> {
        private final EnvironmentAttribute<Value> attribute;
        final Value defaultValue;
        private final List<EnvironmentAttributeFunction<Value>> modifications;
        final boolean positional;
        private @Nullable Value cachedValue;
        private int age;

        Entry(EnvironmentAttribute<Value> attribute, Value defaultValue, List<EnvironmentAttributeFunction<Value>> modifications, boolean positional) {
            this.attribute = attribute;
            this.defaultValue = defaultValue;
            this.modifications = modifications;
            this.positional = positional;
        }

        public void tick() {
            this.cachedValue = null;
            ++this.age;
        }

        public Value get() {
            if (this.cachedValue != null) {
                return this.cachedValue;
            }
            Value object = this.compute();
            this.cachedValue = object;
            return object;
        }

        public Value getAt(Vec3d pos, @Nullable WeightedAttributeList weightedAttributeList) {
            if (!this.positional) {
                return this.get();
            }
            return this.computeAt(pos, weightedAttributeList);
        }

        private Value computeAt(Vec3d pos, @Nullable WeightedAttributeList weightedAttributeList) {
            Value object = this.defaultValue;
            for (EnvironmentAttributeFunction<Value> environmentAttributeFunction : this.modifications) {
                EnvironmentAttributeFunction<Value> environmentAttributeFunction2;
                Objects.requireNonNull(environmentAttributeFunction);
                int n = 0;
                object = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EnvironmentAttributeFunction.Constant.class, EnvironmentAttributeFunction.TimeBased.class, EnvironmentAttributeFunction.Positional.class}, environmentAttributeFunction2, n)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        EnvironmentAttributeFunction.Constant constant = (EnvironmentAttributeFunction.Constant)environmentAttributeFunction2;
                        yield constant.applyConstant(object);
                    }
                    case 1 -> {
                        EnvironmentAttributeFunction.TimeBased timeBased = (EnvironmentAttributeFunction.TimeBased)environmentAttributeFunction2;
                        yield timeBased.applyTimeBased(object, this.age);
                    }
                    case 2 -> {
                        EnvironmentAttributeFunction.Positional positional = (EnvironmentAttributeFunction.Positional)environmentAttributeFunction2;
                        yield positional.applyPositional(object, Objects.requireNonNull(pos), weightedAttributeList);
                    }
                };
            }
            return this.attribute.clamp(object);
        }

        private Value compute() {
            Value object = this.defaultValue;
            for (EnvironmentAttributeFunction<Value> environmentAttributeFunction : this.modifications) {
                EnvironmentAttributeFunction<Value> environmentAttributeFunction2;
                Objects.requireNonNull(environmentAttributeFunction);
                int n = 0;
                object = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EnvironmentAttributeFunction.Constant.class, EnvironmentAttributeFunction.TimeBased.class, EnvironmentAttributeFunction.Positional.class}, environmentAttributeFunction2, n)) {
                    default -> throw new MatchException(null, null);
                    case 0 -> {
                        EnvironmentAttributeFunction.Constant constant = (EnvironmentAttributeFunction.Constant)environmentAttributeFunction2;
                        yield constant.applyConstant(object);
                    }
                    case 1 -> {
                        EnvironmentAttributeFunction.TimeBased timeBased = (EnvironmentAttributeFunction.TimeBased)environmentAttributeFunction2;
                        yield timeBased.applyTimeBased(object, this.age);
                    }
                    case 2 -> {
                        EnvironmentAttributeFunction.Positional positional = (EnvironmentAttributeFunction.Positional)environmentAttributeFunction2;
                        yield object;
                    }
                };
            }
            return this.attribute.clamp(object);
        }
    }

    public static class Builder {
        private final Map<EnvironmentAttribute<?>, List<EnvironmentAttributeFunction<?>>> modifications = new HashMap();

        Builder() {
        }

        public Builder world(World world) {
            WorldEnvironmentAttributeAccess.addModifiersFromWorld(this, world);
            return this;
        }

        public Builder addFromMap(EnvironmentAttributeMap attributes) {
            for (EnvironmentAttribute<?> environmentAttribute : attributes.keySet()) {
                this.addFromMap(environmentAttribute, attributes);
            }
            return this;
        }

        private <Value> Builder addFromMap(EnvironmentAttribute<Value> attribute, EnvironmentAttributeMap attributeMap) {
            EnvironmentAttributeMap.Entry<Value, ?> entry = attributeMap.getEntry(attribute);
            if (entry == null) {
                throw new IllegalArgumentException("Missing attribute " + String.valueOf(attribute));
            }
            return this.constant(attribute, entry::apply);
        }

        public <Value> Builder constant(EnvironmentAttribute<Value> attribute, EnvironmentAttributeFunction.Constant<Value> mod) {
            return this.addModification(attribute, mod);
        }

        public <Value> Builder timeBased(EnvironmentAttribute<Value> attribute, EnvironmentAttributeFunction.TimeBased<Value> mod) {
            return this.addModification(attribute, mod);
        }

        public <Value> Builder positional(EnvironmentAttribute<Value> attribute, EnvironmentAttributeFunction.Positional<Value> mod) {
            return this.addModification(attribute, mod);
        }

        private <Value> Builder addModification(EnvironmentAttribute<Value> attribute, EnvironmentAttributeFunction<Value> mod) {
            this.modifications.computeIfAbsent(attribute, attr -> new ArrayList()).add(mod);
            return this;
        }

        public Builder addFromTimeline(RegistryEntry<Timeline> timeline, LongSupplier timeSupplier) {
            for (EnvironmentAttribute<?> environmentAttribute : timeline.value().getAttributes()) {
                this.addModificationFromTimeline(timeline, environmentAttribute, timeSupplier);
            }
            return this;
        }

        private <Value> void addModificationFromTimeline(RegistryEntry<Timeline> timeline, EnvironmentAttribute<Value> attribute, LongSupplier timeSupplier) {
            this.timeBased(attribute, timeline.value().getModification(attribute, timeSupplier));
        }

        public WorldEnvironmentAttributeAccess build() {
            return new WorldEnvironmentAttributeAccess(this.modifications);
        }
    }
}

