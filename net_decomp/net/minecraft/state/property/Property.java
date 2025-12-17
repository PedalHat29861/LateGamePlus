/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.state.property;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.state.State;
import org.jspecify.annotations.Nullable;

public abstract class Property<T extends Comparable<T>> {
    private final Class<T> type;
    private final String name;
    private @Nullable Integer hashCodeCache;
    private final Codec<T> codec = Codec.STRING.comapFlatMap(value -> this.parse((String)value).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unable to read property: " + String.valueOf(this) + " with value: " + value)), this::name);
    private final Codec<Value<T>> valueCodec = this.codec.xmap(this::createValue, Value::value);

    protected Property(String name, Class<T> type) {
        this.type = type;
        this.name = name;
    }

    public Value<T> createValue(T value) {
        return new Value<T>(this, value);
    }

    public Value<T> createValue(State<?, ?> state) {
        return new Value(this, state.get(this));
    }

    public Stream<Value<T>> stream() {
        return this.getValues().stream().map(this::createValue);
    }

    public Codec<T> getCodec() {
        return this.codec;
    }

    public Codec<Value<T>> getValueCodec() {
        return this.valueCodec;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getType() {
        return this.type;
    }

    public abstract List<T> getValues();

    public abstract String name(T var1);

    public abstract Optional<T> parse(String var1);

    public abstract int ordinal(T var1);

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("name", (Object)this.name).add("clazz", this.type).add("values", this.getValues()).toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Property) {
            Property property = (Property)o;
            return this.type.equals(property.type) && this.name.equals(property.name);
        }
        return false;
    }

    public final int hashCode() {
        if (this.hashCodeCache == null) {
            this.hashCodeCache = this.computeHashCode();
        }
        return this.hashCodeCache;
    }

    public int computeHashCode() {
        return 31 * this.type.hashCode() + this.name.hashCode();
    }

    public <U, S extends State<?, S>> DataResult<S> parse(DynamicOps<U> ops, S state, U input) {
        DataResult dataResult = this.codec.parse(ops, input);
        return dataResult.map(property -> (State)state.with(this, property)).setPartial(state);
    }

    public record Value<T extends Comparable<T>>(Property<T> property, T value) {
        public Value {
            if (!property.getValues().contains(value)) {
                throw new IllegalArgumentException("Value " + String.valueOf(value) + " does not belong to property " + String.valueOf(property));
            }
        }

        @Override
        public String toString() {
            return this.property.getName() + "=" + this.property.name(this.value);
        }
    }
}

