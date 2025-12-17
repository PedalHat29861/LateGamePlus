/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public abstract class SinglePreparationResourceReloader<T>
implements ResourceReloader {
    @Override
    public final CompletableFuture<Void> reload(ResourceReloader.Store store, Executor executor, ResourceReloader.Synchronizer synchronizer, Executor executor2) {
        ResourceManager resourceManager = store.getResourceManager();
        return ((CompletableFuture)CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, Profilers.get()), executor).thenCompose(synchronizer::whenPrepared)).thenAcceptAsync(prepared -> this.apply(prepared, resourceManager, Profilers.get()), executor2);
    }

    protected abstract T prepare(ResourceManager var1, Profiler var2);

    protected abstract void apply(T var1, ResourceManager var2, Profiler var3);
}

