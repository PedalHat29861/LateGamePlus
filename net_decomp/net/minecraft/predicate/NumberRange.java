/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.BuiltInExceptionProvider
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.predicate;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public interface NumberRange<T extends Number> {
    public static final SimpleCommandExceptionType EXCEPTION_EMPTY = new SimpleCommandExceptionType((Message)Text.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType EXCEPTION_SWAPPED = new SimpleCommandExceptionType((Message)Text.translatable("argument.range.swapped"));

    public Bounds<T> bounds();

    default public Optional<T> getMin() {
        return this.bounds().min;
    }

    default public Optional<T> getMax() {
        return this.bounds().max;
    }

    default public boolean isDummy() {
        return this.bounds().isAny();
    }

    public static final class Bounds<T extends Number>
    extends Record {
        final Optional<T> min;
        final Optional<T> max;

        public Bounds(Optional<T> min, Optional<T> max) {
            this.min = min;
            this.max = max;
        }

        public boolean isAny() {
            return this.min().isEmpty() && this.max().isEmpty();
        }

        public DataResult<Bounds<T>> validate() {
            if (this.isSwapped()) {
                return DataResult.error(() -> "Swapped bounds in range: " + String.valueOf(this.min()) + " is higher than " + String.valueOf(this.max()));
            }
            return DataResult.success((Object)this);
        }

        public boolean isSwapped() {
            return this.min.isPresent() && this.max.isPresent() && ((Comparable)((Object)((Number)this.min.get()))).compareTo((Number)this.max.get()) > 0;
        }

        public Optional<T> getPoint() {
            Optional<T> optional2;
            Optional<T> optional = this.min();
            return optional.equals(optional2 = this.max()) ? optional : Optional.empty();
        }

        public static <T extends Number> Bounds<T> any() {
            return new Bounds(Optional.empty(), Optional.empty());
        }

        public static <T extends Number> Bounds<T> exactly(T value) {
            Optional<T> optional = Optional.of(value);
            return new Bounds<T>(optional, optional);
        }

        public static <T extends Number> Bounds<T> between(T min, T max) {
            return new Bounds<T>(Optional.of(min), Optional.of(max));
        }

        public static <T extends Number> Bounds<T> atLeast(T value) {
            return new Bounds<T>(Optional.of(value), Optional.empty());
        }

        public static <T extends Number> Bounds<T> atMost(T value) {
            return new Bounds(Optional.empty(), Optional.of(value));
        }

        public <U extends Number> Bounds<U> map(Function<T, U> mappingFunction) {
            return new Bounds<U>(this.min.map(mappingFunction), this.max.map(mappingFunction));
        }

        static <T extends Number> Codec<Bounds<T>> createCodec(Codec<T> valueCodec) {
            Codec codec = RecordCodecBuilder.create(instance -> instance.group((App)valueCodec.optionalFieldOf("min").forGetter(Bounds::min), (App)valueCodec.optionalFieldOf("max").forGetter(Bounds::max)).apply((Applicative)instance, Bounds::new));
            return Codec.either((Codec)codec, valueCodec).xmap(either -> (Bounds)either.map(bounds -> bounds, value -> Bounds.exactly((Number)value)), bounds -> {
                Optional optional = bounds.getPoint();
                return optional.isPresent() ? Either.right((Object)((Number)optional.get())) : Either.left((Object)bounds);
            });
        }

        static <B extends ByteBuf, T extends Number> PacketCodec<B, Bounds<T>> createPacketCodec(final PacketCodec<B, T> valuePacketCodec) {
            return new PacketCodec<B, Bounds<T>>(){
                private static final int MIN_PRESENT_FLAG = 1;
                private static final int MAX_PRESENT_FLAG = 2;

                @Override
                public Bounds<T> decode(B byteBuf) {
                    byte b = byteBuf.readByte();
                    Optional optional = (b & 1) != 0 ? Optional.of((Number)valuePacketCodec.decode(byteBuf)) : Optional.empty();
                    Optional optional2 = (b & 2) != 0 ? Optional.of((Number)valuePacketCodec.decode(byteBuf)) : Optional.empty();
                    return new Bounds(optional, optional2);
                }

                @Override
                public void encode(B byteBuf, Bounds<T> bounds) {
                    Optional<Number> optional = bounds.min();
                    Optional<Number> optional2 = bounds.max();
                    byteBuf.writeByte((optional.isPresent() ? 1 : 0) | (optional2.isPresent() ? 2 : 0));
                    optional.ifPresent(min -> valuePacketCodec.encode(byteBuf, min));
                    optional2.ifPresent(max -> valuePacketCodec.encode(byteBuf, max));
                }

                @Override
                public /* synthetic */ void encode(Object object, Object object2) {
                    this.encode((Object)((ByteBuf)object), (Bounds)object2);
                }

                @Override
                public /* synthetic */ Object decode(Object object) {
                    return this.decode((B)((ByteBuf)object));
                }
            };
        }

        public static <T extends Number> Bounds<T> parse(StringReader reader, Function<String, T> parsingFunction, Supplier<DynamicCommandExceptionType> exceptionSupplier) throws CommandSyntaxException {
            if (!reader.canRead()) {
                throw EXCEPTION_EMPTY.createWithContext((ImmutableStringReader)reader);
            }
            int i = reader.getCursor();
            try {
                Optional<T> optional2;
                Optional<T> optional = Bounds.parseNumber(reader, parsingFunction, exceptionSupplier);
                if (reader.canRead(2) && reader.peek() == '.' && reader.peek(1) == '.') {
                    reader.skip();
                    reader.skip();
                    optional2 = Bounds.parseNumber(reader, parsingFunction, exceptionSupplier);
                } else {
                    optional2 = optional;
                }
                if (optional.isEmpty() && optional2.isEmpty()) {
                    throw EXCEPTION_EMPTY.createWithContext((ImmutableStringReader)reader);
                }
                return new Bounds<T>(optional, optional2);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                reader.setCursor(i);
                throw new CommandSyntaxException(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), commandSyntaxException.getInput(), i);
            }
        }

        private static <T extends Number> Optional<T> parseNumber(StringReader reader, Function<String, T> parsingFunction, Supplier<DynamicCommandExceptionType> exceptionSupplier) throws CommandSyntaxException {
            int i = reader.getCursor();
            while (reader.canRead() && Bounds.shouldSkip(reader)) {
                reader.skip();
            }
            String string = reader.getString().substring(i, reader.getCursor());
            if (string.isEmpty()) {
                return Optional.empty();
            }
            try {
                return Optional.of((Number)parsingFunction.apply(string));
            }
            catch (NumberFormatException numberFormatException) {
                throw exceptionSupplier.get().createWithContext((ImmutableStringReader)reader, (Object)string);
            }
        }

        private static boolean shouldSkip(StringReader reader) {
            char c = reader.peek();
            if (c >= '0' && c <= '9' || c == '-') {
                return true;
            }
            if (c == '.') {
                return !reader.canRead(2) || reader.peek(1) != '.';
            }
            return false;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Bounds.class, "min;max", "min", "max"}, this, object);
        }

        public Optional<T> min() {
            return this.min;
        }

        public Optional<T> max() {
            return this.max;
        }
    }

    public record AngleRange(Bounds<Float> bounds) implements NumberRange<Float>
    {
        public static final AngleRange ANY = new AngleRange(Bounds.any());
        public static final Codec<AngleRange> CODEC = Bounds.createCodec(Codec.FLOAT).xmap(AngleRange::new, AngleRange::bounds);
        public static final PacketCodec<ByteBuf, AngleRange> PACKET_CODEC = Bounds.createPacketCodec(PacketCodecs.FLOAT).xmap(AngleRange::new, AngleRange::bounds);

        public static AngleRange parse(StringReader reader) throws CommandSyntaxException {
            Bounds<Float> bounds = Bounds.parse(reader, Float::parseFloat, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidFloat());
            return new AngleRange(bounds);
        }
    }

    public record DoubleRange(Bounds<Double> bounds, Bounds<Double> boundsSqr) implements NumberRange<Double>
    {
        public static final DoubleRange ANY = new DoubleRange(Bounds.any());
        public static final Codec<DoubleRange> CODEC = Bounds.createCodec(Codec.DOUBLE).validate(Bounds::validate).xmap(DoubleRange::new, DoubleRange::bounds);
        public static final PacketCodec<ByteBuf, DoubleRange> PACKET_CODEC = Bounds.createPacketCodec(PacketCodecs.DOUBLE).xmap(DoubleRange::new, DoubleRange::bounds);

        private DoubleRange(Bounds<Double> bounds) {
            this(bounds, bounds.map(MathHelper::square));
        }

        public static DoubleRange exactly(double value) {
            return new DoubleRange(Bounds.exactly(value));
        }

        public static DoubleRange between(double min, double max) {
            return new DoubleRange(Bounds.between(min, max));
        }

        public static DoubleRange atLeast(double value) {
            return new DoubleRange(Bounds.atLeast(value));
        }

        public static DoubleRange atMost(double value) {
            return new DoubleRange(Bounds.atMost(value));
        }

        public boolean test(double value) {
            if (this.bounds.min.isPresent() && (Double)this.bounds.min.get() > value) {
                return false;
            }
            return this.bounds.max.isEmpty() || !((Double)this.bounds.max.get() < value);
        }

        public boolean testSqrt(double value) {
            if (this.boundsSqr.min.isPresent() && (Double)this.boundsSqr.min.get() > value) {
                return false;
            }
            return this.boundsSqr.max.isEmpty() || !((Double)this.boundsSqr.max.get() < value);
        }

        public static DoubleRange parse(StringReader reader) throws CommandSyntaxException {
            int i = reader.getCursor();
            Bounds<Double> bounds = Bounds.parse(reader, Double::parseDouble, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidDouble());
            if (bounds.isSwapped()) {
                reader.setCursor(i);
                throw EXCEPTION_SWAPPED.createWithContext((ImmutableStringReader)reader);
            }
            return new DoubleRange(bounds);
        }
    }

    public record IntRange(Bounds<Integer> bounds, Bounds<Long> boundsSqr) implements NumberRange<Integer>
    {
        public static final IntRange ANY = new IntRange(Bounds.any());
        public static final Codec<IntRange> CODEC = Bounds.createCodec(Codec.INT).validate(Bounds::validate).xmap(IntRange::new, IntRange::bounds);
        public static final PacketCodec<ByteBuf, IntRange> PACKET_CODEC = Bounds.createPacketCodec(PacketCodecs.INTEGER).xmap(IntRange::new, IntRange::bounds);

        private IntRange(Bounds<Integer> bounds) {
            this(bounds, bounds.map(i -> MathHelper.square(i.longValue())));
        }

        public static IntRange exactly(int value) {
            return new IntRange(Bounds.exactly(value));
        }

        public static IntRange between(int min, int max) {
            return new IntRange(Bounds.between(min, max));
        }

        public static IntRange atLeast(int value) {
            return new IntRange(Bounds.atLeast(value));
        }

        public static IntRange atMost(int value) {
            return new IntRange(Bounds.atMost(value));
        }

        public boolean test(int value) {
            if (this.bounds.min.isPresent() && (Integer)this.bounds.min.get() > value) {
                return false;
            }
            return this.bounds.max.isEmpty() || (Integer)this.bounds.max.get() >= value;
        }

        public boolean testSqrt(long value) {
            if (this.boundsSqr.min.isPresent() && (Long)this.boundsSqr.min.get() > value) {
                return false;
            }
            return this.boundsSqr.max.isEmpty() || (Long)this.boundsSqr.max.get() >= value;
        }

        public static IntRange parse(StringReader reader) throws CommandSyntaxException {
            int i = reader.getCursor();
            Bounds<Integer> bounds = Bounds.parse(reader, Integer::parseInt, () -> ((BuiltInExceptionProvider)CommandSyntaxException.BUILT_IN_EXCEPTIONS).readerInvalidInt());
            if (bounds.isSwapped()) {
                reader.setCursor(i);
                throw EXCEPTION_SWAPPED.createWithContext((ImmutableStringReader)reader);
            }
            return new IntRange(bounds);
        }
    }
}

