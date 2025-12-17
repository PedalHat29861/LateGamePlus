/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.collection;

import org.jspecify.annotations.Nullable;

public interface IndexedIterable<T>
extends Iterable<T> {
    public static final int ABSENT_RAW_ID = -1;

    public int getRawId(T var1);

    public @Nullable T get(int var1);

    default public T getOrThrow(int index) {
        T object = this.get(index);
        if (object == null) {
            throw new IllegalArgumentException("No value with id " + index);
        }
        return object;
    }

    default public int getRawIdOrThrow(T value) {
        int i = this.getRawId(value);
        if (i == -1) {
            throw new IllegalArgumentException("Can't find id for '" + String.valueOf(value) + "' in map " + String.valueOf(this));
        }
        return i;
    }

    public int size();
}

