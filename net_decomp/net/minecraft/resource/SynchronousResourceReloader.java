/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

public interface SynchronousResourceReloader
extends ResourceReloader {
    @Override
    default public CompletableFuture<Void> reload(ResourceReloader.Store store, Executor executor, ResourceReloader.Synchronizer synchronizer, Executor executor2) {
        ResourceManager resourceManager = store.getResourceManager();
        return synchronizer.whenPrepared(Unit.INSTANCE).thenRunAsync(() -> {
            Profiler profiler = Profilers.get();
            profiler.push("listener");
            this.reload(resourceManager);
            profiler.pop();
        }, executor2);
    }

    public void reload(ResourceManager var1);
}

