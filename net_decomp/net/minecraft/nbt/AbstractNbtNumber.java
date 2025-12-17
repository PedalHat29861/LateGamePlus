/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.util.Optional;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtPrimitive;
import net.minecraft.nbt.NbtShort;

public sealed interface AbstractNbtNumber
extends NbtPrimitive
permits NbtByte, NbtShort, NbtInt, NbtLong, NbtFloat, NbtDouble {
    public byte byteValue();

    public short shortValue();

    public int intValue();

    public long longValue();

    public float floatValue();

    public double doubleValue();

    public Number numberValue();

    @Override
    default public Optional<Number> asNumber() {
        return Optional.of(this.numberValue());
    }

    @Override
    default public Optional<Byte> asByte() {
        return Optional.of(this.byteValue());
    }

    @Override
    default public Optional<Short> asShort() {
        return Optional.of(this.shortValue());
    }

    @Override
    default public Optional<Integer> asInt() {
        return Optional.of(this.intValue());
    }

    @Override
    default public Optional<Long> asLong() {
        return Optional.of(this.longValue());
    }

    @Override
    default public Optional<Float> asFloat() {
        return Optional.of(Float.valueOf(this.floatValue()));
    }

    @Override
    default public Optional<Double> asDouble() {
        return Optional.of(this.doubleValue());
    }

    @Override
    default public Optional<Boolean> asBoolean() {
        return Optional.of(this.byteValue() != 0);
    }
}

