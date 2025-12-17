/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.Nullable;

public class AsyncHelper {
    private static final int MAX_TASKS = 16;

    public static <K, U, V> CompletableFuture<Map<K, V>> mapValues(Map<K, U> futures, BiFunction<K, U, @Nullable V> function, int batchSize, Executor executor) {
        int i = futures.size();
        if (i == 0) {
            return CompletableFuture.completedFuture(Map.of());
        }
        if (i == 1) {
            Map.Entry<K, U> entry = futures.entrySet().iterator().next();
            Object object = entry.getKey();
            Object object2 = entry.getValue();
            return CompletableFuture.supplyAsync(() -> {
                Object object3 = function.apply(object, object2);
                return object3 != null ? Map.of(object, object3) : Map.of();
            }, executor);
        }
        Batcher batcher = i <= batchSize ? new Single<K, U, V>(function, i) : new Batch<K, U, V>(function, i, batchSize);
        return batcher.mapAsync(futures, executor);
    }

    public static <K, U, V> CompletableFuture<Map<K, V>> mapValues(Map<K, U> futures, BiFunction<K, U, @Nullable V> function, Executor executor) {
        int i = Util.getAvailableBackgroundThreads() * 16;
        return AsyncHelper.mapValues(futures, function, i, executor);
    }

    static class Single<K, U, V>
    extends Batcher<K, U, V> {
        Single(BiFunction<K, U, V> function, int size) {
            super(function, size, size);
        }

        @Override
        protected int getLastIndex(int batch) {
            return 1;
        }

        @Override
        protected CompletableFuture<?> newBatch(Future<K, U, V> futures, int size, int maxCount, Executor executor) {
            assert (size + 1 == maxCount);
            return CompletableFuture.runAsync(() -> futures.apply(size), executor);
        }

        @Override
        protected CompletableFuture<Map<K, V>> addLastTask(CompletableFuture<?> future, Future<K, U, V> entry) {
            return future.thenApply(obj -> {
                HashMap map = new HashMap(entry.keySize());
                for (int i = 0; i < entry.keySize(); ++i) {
                    entry.copy(i, map);
                }
                return map;
            });
        }
    }

    static class Batch<K, U, V>
    extends Batcher<K, U, V> {
        private final Map<K, V> entries;
        private final int size;
        private final int start;

        Batch(BiFunction<K, U, V> biFunction, int i, int j) {
            super(biFunction, i, j);
            this.entries = new HashMap(i);
            this.size = MathHelper.ceilDiv(i, j);
            int k = this.size * j;
            int l = k - i;
            this.start = j - l;
            assert (this.start > 0 && this.start <= j);
        }

        @Override
        protected CompletableFuture<?> newBatch(Future<K, U, V> futures, int size, int maxCount, Executor executor) {
            int i = maxCount - size;
            assert (i == this.size || i == this.size - 1);
            return CompletableFuture.runAsync(Batch.newTask(this.entries, size, maxCount, futures), executor);
        }

        @Override
        protected int getLastIndex(int batch) {
            return batch < this.start ? this.size : this.size - 1;
        }

        private static <K, U, V> Runnable newTask(Map<K, V> futures, int size, int maxCount, Future<K, U, V> entry) {
            return () -> {
                for (int k = size; k < maxCount; ++k) {
                    entry.apply(k);
                }
                Map map2 = futures;
                synchronized (map2) {
                    for (int l = size; l < maxCount; ++l) {
                        entry.copy(l, futures);
                    }
                }
            };
        }

        @Override
        protected CompletableFuture<Map<K, V>> addLastTask(CompletableFuture<?> future, Future<K, U, V> entry) {
            Map map = this.entries;
            return future.thenApply(obj -> map);
        }
    }

    static abstract class Batcher<K, U, V> {
        private int lastBatch;
        private int index;
        private final CompletableFuture<?>[] futures;
        private int batch;
        private final Future<K, U, V> entry;

        Batcher(BiFunction<K, U, V> function, int size, int startAt) {
            this.entry = new Future<K, U, V>(function, size);
            this.futures = new CompletableFuture[startAt];
        }

        private int nextSize() {
            return this.index - this.lastBatch;
        }

        public CompletableFuture<Map<K, V>> mapAsync(Map<K, U> future, Executor executor) {
            future.forEach((key, value) -> {
                this.entry.put(this.index++, key, value);
                if (this.nextSize() == this.getLastIndex(this.batch)) {
                    this.futures[this.batch++] = this.newBatch(this.entry, this.lastBatch, this.index, executor);
                    this.lastBatch = this.index;
                }
            });
            assert (this.index == this.entry.keySize());
            assert (this.lastBatch == this.index);
            assert (this.batch == this.futures.length);
            return this.addLastTask(CompletableFuture.allOf(this.futures), this.entry);
        }

        protected abstract int getLastIndex(int var1);

        protected abstract CompletableFuture<?> newBatch(Future<K, U, V> var1, int var2, int var3, Executor var4);

        protected abstract CompletableFuture<Map<K, V>> addLastTask(CompletableFuture<?> var1, Future<K, U, V> var2);
    }

    record Future<K, U, V>(BiFunction<K, U, V> operation, @Nullable Object[] keys, @Nullable Object[] values) {
        public Future(BiFunction<K, U, V> function, int size) {
            this(function, new Object[size], new Object[size]);
        }

        public void put(int index, K key, U value) {
            this.keys[index] = key;
            this.values[index] = value;
        }

        private @Nullable K getKey(int index) {
            return (K)this.keys[index];
        }

        private @Nullable V getValue(int index) {
            return (V)this.values[index];
        }

        private @Nullable U getUValue(int index) {
            return (U)this.values[index];
        }

        public void apply(int index) {
            this.values[index] = this.operation.apply(this.getKey(index), this.getUValue(index));
        }

        public void copy(int index, Map<K, V> futures) {
            V object = this.getValue(index);
            if (object != null) {
                K object2 = this.getKey(index);
                futures.put(object2, object);
            }
        }

        public int keySize() {
            return this.keys.length;
        }
    }
}

