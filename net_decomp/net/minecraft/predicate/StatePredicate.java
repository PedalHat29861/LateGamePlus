/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;

public record StatePredicate(List<Condition> conditions) {
    private static final Codec<List<Condition>> CONDITION_LIST_CODEC = Codec.unboundedMap((Codec)Codec.STRING, ValueMatcher.CODEC).xmap(states -> states.entrySet().stream().map(state -> new Condition((String)state.getKey(), (ValueMatcher)state.getValue())).toList(), conditions -> conditions.stream().collect(Collectors.toMap(Condition::key, Condition::valueMatcher)));
    public static final Codec<StatePredicate> CODEC = CONDITION_LIST_CODEC.xmap(StatePredicate::new, StatePredicate::conditions);
    public static final PacketCodec<ByteBuf, StatePredicate> PACKET_CODEC = Condition.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(StatePredicate::new, StatePredicate::conditions);

    public <S extends State<?, S>> boolean test(StateManager<?, S> stateManager, S container) {
        for (Condition condition : this.conditions) {
            if (condition.test(stateManager, container)) continue;
            return false;
        }
        return true;
    }

    public boolean test(BlockState state) {
        return this.test(state.getBlock().getStateManager(), state);
    }

    public boolean test(FluidState state) {
        return this.test(state.getFluid().getStateManager(), state);
    }

    public Optional<String> findMissing(StateManager<?, ?> stateManager) {
        for (Condition condition : this.conditions) {
            Optional<String> optional = condition.reportMissing(stateManager);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{StatePredicate.class, "properties", "conditions"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StatePredicate.class, "properties", "conditions"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StatePredicate.class, "properties", "conditions"}, this, object);
    }

    record Condition(String key, ValueMatcher valueMatcher) {
        public static final PacketCodec<ByteBuf, Condition> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, Condition::key, ValueMatcher.PACKET_CODEC, Condition::valueMatcher, Condition::new);

        public <S extends State<?, S>> boolean test(StateManager<?, S> stateManager, S state) {
            Property<?> property = stateManager.getProperty(this.key);
            return property != null && this.valueMatcher.test(state, property);
        }

        public Optional<String> reportMissing(StateManager<?, ?> factory) {
            Property<?> property = factory.getProperty(this.key);
            return property != null ? Optional.empty() : Optional.of(this.key);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Condition.class, "name;valueMatcher", "key", "valueMatcher"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Condition.class, "name;valueMatcher", "key", "valueMatcher"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Condition.class, "name;valueMatcher", "key", "valueMatcher"}, this, object);
        }
    }

    static interface ValueMatcher {
        public static final Codec<ValueMatcher> CODEC = Codec.either(ExactValueMatcher.CODEC, RangedValueMatcher.CODEC).xmap(Either::unwrap, valueMatcher -> {
            if (valueMatcher instanceof ExactValueMatcher) {
                ExactValueMatcher exactValueMatcher = (ExactValueMatcher)valueMatcher;
                return Either.left((Object)exactValueMatcher);
            }
            if (valueMatcher instanceof RangedValueMatcher) {
                RangedValueMatcher rangedValueMatcher = (RangedValueMatcher)valueMatcher;
                return Either.right((Object)rangedValueMatcher);
            }
            throw new UnsupportedOperationException();
        });
        public static final PacketCodec<ByteBuf, ValueMatcher> PACKET_CODEC = PacketCodecs.either(ExactValueMatcher.PACKET_CODEC, RangedValueMatcher.PACKET_CODEC).xmap(Either::unwrap, valueMatcher -> {
            if (valueMatcher instanceof ExactValueMatcher) {
                ExactValueMatcher exactValueMatcher = (ExactValueMatcher)valueMatcher;
                return Either.left((Object)exactValueMatcher);
            }
            if (valueMatcher instanceof RangedValueMatcher) {
                RangedValueMatcher rangedValueMatcher = (RangedValueMatcher)valueMatcher;
                return Either.right((Object)rangedValueMatcher);
            }
            throw new UnsupportedOperationException();
        });

        public <T extends Comparable<T>> boolean test(State<?, ?> var1, Property<T> var2);
    }

    public static class Builder {
        private final ImmutableList.Builder<Condition> conditions = ImmutableList.builder();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder exactMatch(Property<?> property, String valueName) {
            this.conditions.add((Object)new Condition(property.getName(), new ExactValueMatcher(valueName)));
            return this;
        }

        public Builder exactMatch(Property<Integer> property, int value) {
            return this.exactMatch((Property)property, (Comparable<T> & StringIdentifiable)Integer.toString(value));
        }

        public Builder exactMatch(Property<Boolean> property, boolean value) {
            return this.exactMatch((Property)property, (Comparable<T> & StringIdentifiable)Boolean.toString(value));
        }

        public <T extends Comparable<T> & StringIdentifiable> Builder exactMatch(Property<T> property, T value) {
            return this.exactMatch(property, (T)((StringIdentifiable)value).asString());
        }

        public Optional<StatePredicate> build() {
            return Optional.of(new StatePredicate((List<Condition>)this.conditions.build()));
        }
    }

    record RangedValueMatcher(Optional<String> min, Optional<String> max) implements ValueMatcher
    {
        public static final Codec<RangedValueMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.optionalFieldOf("min").forGetter(RangedValueMatcher::min), (App)Codec.STRING.optionalFieldOf("max").forGetter(RangedValueMatcher::max)).apply((Applicative)instance, RangedValueMatcher::new));
        public static final PacketCodec<ByteBuf, RangedValueMatcher> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.optional(PacketCodecs.STRING), RangedValueMatcher::min, PacketCodecs.optional(PacketCodecs.STRING), RangedValueMatcher::max, RangedValueMatcher::new);

        @Override
        public <T extends Comparable<T>> boolean test(State<?, ?> state, Property<T> property) {
            Optional<T> optional;
            Comparable comparable = state.get(property);
            if (this.min.isPresent() && ((optional = property.parse(this.min.get())).isEmpty() || comparable.compareTo((Comparable)((Comparable)optional.get())) < 0)) {
                return false;
            }
            return !this.max.isPresent() || !(optional = property.parse(this.max.get())).isEmpty() && comparable.compareTo((Comparable)((Comparable)optional.get())) <= 0;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RangedValueMatcher.class, "minValue;maxValue", "min", "max"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RangedValueMatcher.class, "minValue;maxValue", "min", "max"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RangedValueMatcher.class, "minValue;maxValue", "min", "max"}, this, object);
        }
    }

    record ExactValueMatcher(String value) implements ValueMatcher
    {
        public static final Codec<ExactValueMatcher> CODEC = Codec.STRING.xmap(ExactValueMatcher::new, ExactValueMatcher::value);
        public static final PacketCodec<ByteBuf, ExactValueMatcher> PACKET_CODEC = PacketCodecs.STRING.xmap(ExactValueMatcher::new, ExactValueMatcher::value);

        @Override
        public <T extends Comparable<T>> boolean test(State<?, ?> state, Property<T> property) {
            Comparable comparable = state.get(property);
            Optional<T> optional = property.parse(this.value);
            return optional.isPresent() && comparable.compareTo((Comparable)((Comparable)optional.get())) == 0;
        }
    }
}

