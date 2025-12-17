/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.Queues
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.collection;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Deque;
import org.jspecify.annotations.Nullable;

public final class PriorityIterator<T>
extends AbstractIterator<T> {
    private static final int LOWEST_PRIORITY = Integer.MIN_VALUE;
    private @Nullable Deque<T> maxPriorityQueue = null;
    private int maxPriority = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap();

    public void enqueue(T value, int priority) {
        if (priority == this.maxPriority && this.maxPriorityQueue != null) {
            this.maxPriorityQueue.addLast(value);
            return;
        }
        Deque deque = (Deque)this.queuesByPriority.computeIfAbsent(priority, p -> Queues.newArrayDeque());
        deque.addLast(value);
        if (priority >= this.maxPriority) {
            this.maxPriorityQueue = deque;
            this.maxPriority = priority;
        }
    }

    protected @Nullable T computeNext() {
        if (this.maxPriorityQueue == null) {
            return (T)this.endOfData();
        }
        T object = this.maxPriorityQueue.removeFirst();
        if (object == null) {
            return (T)this.endOfData();
        }
        if (this.maxPriorityQueue.isEmpty()) {
            this.refreshMaxPriority();
        }
        return object;
    }

    private void refreshMaxPriority() {
        int i = Integer.MIN_VALUE;
        Deque deque = null;
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(this.queuesByPriority)) {
            Deque deque2 = (Deque)entry.getValue();
            int j = entry.getIntKey();
            if (j <= i || deque2.isEmpty()) continue;
            i = j;
            deque = deque2;
            if (j != this.maxPriority - 1) continue;
            break;
        }
        this.maxPriority = i;
        this.maxPriorityQueue = deque;
    }
}

