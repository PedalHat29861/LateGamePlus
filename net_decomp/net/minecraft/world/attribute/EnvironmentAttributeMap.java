/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeModifier;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.jspecify.annotations.Nullable;

public final class EnvironmentAttributeMap {
    public static final EnvironmentAttributeMap EMPTY = new EnvironmentAttributeMap(Map.of());
    public static final Codec<EnvironmentAttributeMap> CODEC = Codec.lazyInitialized(() -> Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(Entry::createCodec)).xmap(EnvironmentAttributeMap::new, map -> map.entries));
    public static final Codec<EnvironmentAttributeMap> NETWORK_CODEC = CODEC.xmap(EnvironmentAttributeMap::retainSyncedAttributes, EnvironmentAttributeMap::retainSyncedAttributes);
    public static final Codec<EnvironmentAttributeMap> POSITIONAL_CODEC = CODEC.validate(map -> {
        List<EnvironmentAttribute> list = map.keySet().stream().filter(attribute -> !attribute.isPositional()).toList();
        if (!list.isEmpty()) {
            return DataResult.error(() -> "The following attributes cannot be positional: " + String.valueOf(list));
        }
        return DataResult.success((Object)map);
    });
    final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries;

    private static EnvironmentAttributeMap retainSyncedAttributes(EnvironmentAttributeMap map) {
        return new EnvironmentAttributeMap(Map.copyOf(Maps.filterKeys(map.entries, EnvironmentAttribute::isSynced)));
    }

    EnvironmentAttributeMap(Map<EnvironmentAttribute<?>, Entry<?, ?>> entries) {
        this.entries = entries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <Value> @Nullable Entry<Value, ?> getEntry(EnvironmentAttribute<Value> key) {
        return this.entries.get(key);
    }

    public <Value> Value apply(EnvironmentAttribute<Value> key, Value value) {
        Entry<Value, ?> entry = this.getEntry(key);
        return entry != null ? entry.apply(value) : value;
    }

    public boolean containsKey(EnvironmentAttribute<?> key) {
        return this.entries.containsKey(key);
    }

    public Set<EnvironmentAttribute<?>> keySet() {
        return this.entries.keySet();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EnvironmentAttributeMap)) return false;
        EnvironmentAttributeMap environmentAttributeMap = (EnvironmentAttributeMap)o;
        if (!this.entries.equals(environmentAttributeMap.entries)) return false;
        return true;
    }

    public int hashCode() {
        return this.entries.hashCode();
    }

    public String toString() {
        return this.entries.toString();
    }

    public static class Builder {
        private final Map<EnvironmentAttribute<?>, Entry<?, ?>> entries = new HashMap();

        Builder() {
        }

        public Builder addAll(EnvironmentAttributeMap map) {
            this.entries.putAll(map.entries);
            return this;
        }

        public <Value, Parameter> Builder with(EnvironmentAttribute<Value> key, EnvironmentAttributeModifier<Value, Parameter> modifier, Parameter param) {
            key.getType().validate(modifier);
            this.entries.put(key, new Entry<Value, Parameter>(param, modifier));
            return this;
        }

        public <Value> Builder with(EnvironmentAttribute<Value> key, Value value) {
            return this.with(key, EnvironmentAttributeModifier.override(), value);
        }

        public EnvironmentAttributeMap build() {
            if (this.entries.isEmpty()) {
                return EMPTY;
            }
            return new EnvironmentAttributeMap(Map.copyOf(this.entries));
        }
    }

    public record Entry<Value, Argument>(Argument argument, EnvironmentAttributeModifier<Value, Argument> modifier) {
        private static <Value> Codec<Entry<Value, ?>> createCodec(EnvironmentAttribute<Value> attribute) {
            Codec codec = attribute.getType().modifierCodec().dispatch("modifier", Entry::modifier, Util.memoize(modifier -> Entry.createModifierDependentCodec(attribute, modifier)));
            return Codec.either(attribute.getCodec(), (Codec)codec).xmap(either -> (Entry)either.map(value -> new Entry(value, EnvironmentAttributeModifier.override()), entry -> entry), entry -> {
                if (entry.modifier == EnvironmentAttributeModifier.override()) {
                    return Either.left(entry.argument());
                }
                return Either.right((Object)entry);
            });
        }

        private static <Value, Argument> MapCodec<Entry<Value, Argument>> createModifierDependentCodec(EnvironmentAttribute<Value> attribute, EnvironmentAttributeModifier<Value, Argument> modifier) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)modifier.argumentCodec(attribute).fieldOf("argument").forGetter(Entry::argument)).apply((Applicative)instance, argument -> new Entry(argument, modifier)));
        }

        public Value apply(Value value) {
            return this.modifier.apply(value, this.argument);
        }
    }
}

