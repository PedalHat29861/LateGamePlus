/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util.function;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.util.math.MathHelper;

public class ValueLists {
    private static <T> IntFunction<T> createIndexToValueFunction(ToIntFunction<T> valueToIndexFunction, T[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        Int2ObjectOpenHashMap int2ObjectMap = new Int2ObjectOpenHashMap();
        for (T object : values) {
            int i = valueToIndexFunction.applyAsInt(object);
            Object object2 = int2ObjectMap.put(i, object);
            if (object2 == null) continue;
            throw new IllegalArgumentException("Duplicate entry on id " + i + ": current=" + String.valueOf(object) + ", previous=" + String.valueOf(object2));
        }
        return int2ObjectMap;
    }

    public static <T> IntFunction<T> createIndexToValueFunction(ToIntFunction<T> valueToIndexFunction, T[] values, T fallback) {
        IntFunction intFunction = ValueLists.createIndexToValueFunction(valueToIndexFunction, values);
        return index -> Objects.requireNonNullElse(intFunction.apply(index), fallback);
    }

    private static <T> T[] validate(ToIntFunction<T> valueToIndexFunction, T[] values) {
        int i = values.length;
        if (i == 0) {
            throw new IllegalArgumentException("Empty value list");
        }
        Object[] objects = (Object[])values.clone();
        Arrays.fill(objects, null);
        for (T object : values) {
            int j = valueToIndexFunction.applyAsInt(object);
            if (j < 0 || j >= i) {
                throw new IllegalArgumentException("Values are not continous, found index " + j + " for value " + String.valueOf(object));
            }
            Object object2 = objects[j];
            if (object2 != null) {
                throw new IllegalArgumentException("Duplicate entry on id " + j + ": current=" + String.valueOf(object) + ", previous=" + String.valueOf(object2));
            }
            objects[j] = object;
        }
        for (int k = 0; k < i; ++k) {
            if (objects[k] != null) continue;
            throw new IllegalArgumentException("Missing value at index: " + k);
        }
        return objects;
    }

    public static <T> IntFunction<T> createIndexToValueFunction(ToIntFunction<T> valueToIndexFunction, T[] values, OutOfBoundsHandling outOfBoundsHandling) {
        Object[] objects = ValueLists.validate(valueToIndexFunction, values);
        int i = objects.length;
        return switch (outOfBoundsHandling.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Object object = objects[0];
                yield index -> index >= 0 && index < i ? objects[index] : object;
            }
            case 1 -> index -> objects[MathHelper.floorMod(index, i)];
            case 2 -> index -> objects[MathHelper.clamp(index, 0, i - 1)];
        };
    }

    public static final class OutOfBoundsHandling
    extends Enum<OutOfBoundsHandling> {
        public static final /* enum */ OutOfBoundsHandling ZERO = new OutOfBoundsHandling();
        public static final /* enum */ OutOfBoundsHandling WRAP = new OutOfBoundsHandling();
        public static final /* enum */ OutOfBoundsHandling CLAMP = new OutOfBoundsHandling();
        private static final /* synthetic */ OutOfBoundsHandling[] field_41667;

        public static OutOfBoundsHandling[] values() {
            return (OutOfBoundsHandling[])field_41667.clone();
        }

        public static OutOfBoundsHandling valueOf(String string) {
            return Enum.valueOf(OutOfBoundsHandling.class, string);
        }

        private static /* synthetic */ OutOfBoundsHandling[] method_47919() {
            return new OutOfBoundsHandling[]{ZERO, WRAP, CLAMP};
        }

        static {
            field_41667 = OutOfBoundsHandling.method_47919();
        }
    }
}

