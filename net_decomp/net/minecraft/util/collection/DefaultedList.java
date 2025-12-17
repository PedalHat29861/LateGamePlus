/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.collection;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public class DefaultedList<E>
extends AbstractList<E> {
    private final List<E> delegate;
    private final @Nullable E initialElement;

    public static <E> DefaultedList<E> of() {
        return new DefaultedList<Object>(Lists.newArrayList(), null);
    }

    public static <E> DefaultedList<E> ofSize(int size) {
        return new DefaultedList<Object>(Lists.newArrayListWithCapacity((int)size), null);
    }

    public static <E> DefaultedList<E> ofSize(int size, E defaultValue) {
        Objects.requireNonNull(defaultValue);
        Object[] objects = new Object[size];
        Arrays.fill(objects, defaultValue);
        return new DefaultedList<Object>(Arrays.asList(objects), defaultValue);
    }

    @SafeVarargs
    public static <E> DefaultedList<E> copyOf(E defaultValue, E ... values) {
        return new DefaultedList<E>(Arrays.asList(values), defaultValue);
    }

    protected DefaultedList(List<E> delegate, @Nullable E initialElement) {
        this.delegate = delegate;
        this.initialElement = initialElement;
    }

    @Override
    public E get(int index) {
        return this.delegate.get(index);
    }

    @Override
    public E set(int index, E element) {
        Objects.requireNonNull(element);
        return this.delegate.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        Objects.requireNonNull(element);
        this.delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        return this.delegate.remove(index);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public void clear() {
        if (this.initialElement == null) {
            super.clear();
        } else {
            for (int i = 0; i < this.size(); ++i) {
                this.set(i, this.initialElement);
            }
        }
    }
}

