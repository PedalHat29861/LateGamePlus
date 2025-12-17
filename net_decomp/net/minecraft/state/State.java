/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.state.property.Property;
import org.jspecify.annotations.Nullable;

public abstract class State<O, S> {
    public static final String NAME = "Name";
    public static final String PROPERTIES = "Properties";
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAP_PRINTER = new Function<Map.Entry<Property<?>, Comparable<?>>, String>(){

        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            }
            Property<?> property = entry.getKey();
            return property.getName() + "=" + this.nameValue(property, entry.getValue());
        }

        private <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
            return property.name(value);
        }

        @Override
        public /* synthetic */ Object apply(@Nullable Object entry) {
            return this.apply((Map.Entry)entry);
        }
    };
    protected final O owner;
    private final Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap;
    private Map<Property<?>, S[]> withMap;
    protected final MapCodec<S> codec;

    protected State(O owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<S> codec) {
        this.owner = owner;
        this.propertyMap = propertyMap;
        this.codec = codec;
    }

    public <T extends Comparable<T>> S cycle(Property<T> property) {
        return this.with(property, (Comparable)State.getNext(property.getValues(), this.get(property)));
    }

    protected static <T> T getNext(List<T> values, T value) {
        int i = values.indexOf(value) + 1;
        return i == values.size() ? values.getFirst() : values.get(i);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.owner);
        if (!this.getEntries().isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(this.getEntries().entrySet().stream().map(PROPERTY_MAP_PRINTER).collect(Collectors.joining(",")));
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public final boolean equals(Object object) {
        return super.equals(object);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.propertyMap.keySet());
    }

    public boolean contains(Property<?> property) {
        return this.propertyMap.containsKey(property);
    }

    public <T extends Comparable<T>> T get(Property<T> property) {
        Comparable comparable = (Comparable)this.propertyMap.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + String.valueOf(property) + " as it does not exist in " + String.valueOf(this.owner));
        }
        return (T)((Comparable)property.getType().cast(comparable));
    }

    public <T extends Comparable<T>> Optional<T> getOrEmpty(Property<T> property) {
        return Optional.ofNullable(this.getNullable(property));
    }

    public <T extends Comparable<T>> T get(Property<T> property, T fallback) {
        return (T)((Comparable)Objects.requireNonNullElse(this.getNullable(property), fallback));
    }

    private <T extends Comparable<T>> @Nullable T getNullable(Property<T> property) {
        Comparable comparable = (Comparable)this.propertyMap.get(property);
        if (comparable == null) {
            return null;
        }
        return (T)((Comparable)property.getType().cast(comparable));
    }

    public <T extends Comparable<T>, V extends T> S with(Property<T> property, V value) {
        Comparable comparable = (Comparable)this.propertyMap.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " as it does not exist in " + String.valueOf(this.owner));
        }
        return this.with(property, value, comparable);
    }

    public <T extends Comparable<T>, V extends T> S withIfExists(Property<T> property, V value) {
        Comparable comparable = (Comparable)this.propertyMap.get(property);
        if (comparable == null) {
            return (S)this;
        }
        return this.with(property, value, comparable);
    }

    private <T extends Comparable<T>, V extends T> S with(Property<T> property, V newValue, Comparable<?> oldValue) {
        if (oldValue.equals(newValue)) {
            return (S)this;
        }
        int i = property.ordinal(newValue);
        if (i < 0) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " to " + String.valueOf(newValue) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
        }
        return this.withMap.get(property)[i];
    }

    public void createWithMap(Map<Map<Property<?>, Comparable<?>>, S> states) {
        if (this.withMap != null) {
            throw new IllegalStateException();
        }
        Reference2ObjectArrayMap map = new Reference2ObjectArrayMap(this.propertyMap.size());
        for (Map.Entry entry : this.propertyMap.entrySet()) {
            Property property = (Property)entry.getKey();
            map.put(property, property.getValues().stream().map(value -> states.get(this.toMapWith(property, (Comparable<?>)value))).toArray());
        }
        this.withMap = map;
    }

    private Map<Property<?>, Comparable<?>> toMapWith(Property<?> property, Comparable<?> value) {
        Reference2ObjectArrayMap map = new Reference2ObjectArrayMap(this.propertyMap);
        map.put(property, value);
        return map;
    }

    public Map<Property<?>, Comparable<?>> getEntries() {
        return this.propertyMap;
    }

    protected static <O, S extends State<O, S>> Codec<S> createCodec(Codec<O> codec, Function<O, S> ownerToStateFunction) {
        return codec.dispatch(NAME, state -> state.owner, owner -> {
            State state2 = (State)ownerToStateFunction.apply(owner);
            if (state2.getEntries().isEmpty()) {
                return MapCodec.unit((Object)state2);
            }
            return state2.codec.codec().lenientOptionalFieldOf(PROPERTIES).xmap(state -> state.orElse(state2), Optional::of);
        });
    }
}

