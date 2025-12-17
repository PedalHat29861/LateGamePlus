/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntImmutableList
 */
package net.minecraft.state.property;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.state.property.Property;

public final class IntProperty
extends Property<Integer> {
    private final IntImmutableList values;
    private final int min;
    private final int max;

    private IntProperty(String name, int min, int max) {
        super(name, Integer.class);
        if (min < 0) {
            throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
        }
        if (max <= min) {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        }
        this.min = min;
        this.max = max;
        this.values = IntImmutableList.toList((IntStream)IntStream.range(min, max + 1));
    }

    @Override
    public List<Integer> getValues() {
        return this.values;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof IntProperty) {
            IntProperty intProperty = (IntProperty)object;
            if (super.equals(object)) {
                return this.values.equals(intProperty.values);
            }
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        return 31 * super.computeHashCode() + this.values.hashCode();
    }

    public static IntProperty of(String name, int min, int max) {
        return new IntProperty(name, min, max);
    }

    @Override
    public Optional<Integer> parse(String name) {
        try {
            int i = Integer.parseInt(name);
            return i >= this.min && i <= this.max ? Optional.of(i) : Optional.empty();
        }
        catch (NumberFormatException numberFormatException) {
            return Optional.empty();
        }
    }

    @Override
    public String name(Integer integer) {
        return integer.toString();
    }

    @Override
    public int ordinal(Integer integer) {
        if (integer <= this.max) {
            return integer - this.min;
        }
        return -1;
    }

    @Override
    public /* synthetic */ int ordinal(Comparable value) {
        return this.ordinal((Integer)value);
    }

    @Override
    public /* synthetic */ String name(Comparable value) {
        return this.name((Integer)value);
    }
}

