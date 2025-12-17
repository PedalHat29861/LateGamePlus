/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;

@FunctionalInterface
public interface ResourceReloader {
    public CompletableFuture<Void> reload(Store var1, Executor var2, Synchronizer var3, Executor var4);

    default public void prepareSharedState(Store store) {
    }

    default public String getName() {
        return this.getClass().getSimpleName();
    }

    public static final class Store {
        private final ResourceManager resourceManager;
        private final Map<Key<?>, Object> store = new IdentityHashMap();

        public Store(ResourceManager resourceManager) {
            this.resourceManager = resourceManager;
        }

        public ResourceManager getResourceManager() {
            return this.resourceManager;
        }

        public <T> void put(Key<T> key, T value) {
            this.store.put(key, value);
        }

        public <T> T getOrThrow(Key<T> key) {
            return (T)Objects.requireNonNull(this.store.get(key));
        }
    }

    public static final class Key<T> {
    }

    @FunctionalInterface
    public static interface Synchronizer {
        public <T> CompletableFuture<T> whenPrepared(T var1);
    }
}

