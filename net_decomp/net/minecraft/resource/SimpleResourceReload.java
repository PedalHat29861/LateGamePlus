/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resource.ProfiledResourceReload;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class SimpleResourceReload<S>
implements ResourceReload {
    private static final int FIRST_PREPARE_APPLY_WEIGHT = 2;
    private static final int SECOND_PREPARE_APPLY_WEIGHT = 2;
    private static final int RELOADER_WEIGHT = 1;
    final CompletableFuture<Unit> prepareStageFuture = new CompletableFuture();
    private @Nullable CompletableFuture<List<S>> applyStageFuture;
    final Set<ResourceReloader> waitingReloaders;
    private final int reloaderCount;
    private final AtomicInteger toPrepareCount = new AtomicInteger();
    private final AtomicInteger preparedCount = new AtomicInteger();
    private final AtomicInteger toApplyCount = new AtomicInteger();
    private final AtomicInteger appliedCount = new AtomicInteger();

    public static ResourceReload create(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
        SimpleResourceReload<Void> simpleResourceReload = new SimpleResourceReload<Void>(reloaders);
        simpleResourceReload.start(prepareExecutor, applyExecutor, manager, reloaders, Factory.SIMPLE, initialStage);
        return simpleResourceReload;
    }

    protected SimpleResourceReload(List<ResourceReloader> waitingReloaders) {
        this.reloaderCount = waitingReloaders.size();
        this.waitingReloaders = new HashSet<ResourceReloader>(waitingReloaders);
    }

    protected void start(Executor prepareExecutor, Executor applyExecutor, ResourceManager manager, List<ResourceReloader> reloaders, Factory<S> factory, CompletableFuture<?> initialStage) {
        this.applyStageFuture = this.startAsync(prepareExecutor, applyExecutor, manager, reloaders, factory, initialStage);
    }

    protected CompletableFuture<List<S>> startAsync(Executor prepareExecutor, Executor applyExecutor, ResourceManager manager, List<ResourceReloader> reloaders, Factory<S> factory, CompletableFuture<?> initialStage) {
        Executor executor = runnable -> {
            this.toPrepareCount.incrementAndGet();
            prepareExecutor.execute(() -> {
                runnable.run();
                this.preparedCount.incrementAndGet();
            });
        };
        Executor executor2 = runnable -> {
            this.toApplyCount.incrementAndGet();
            applyExecutor.execute(() -> {
                runnable.run();
                this.appliedCount.incrementAndGet();
            });
        };
        this.toPrepareCount.incrementAndGet();
        initialStage.thenRun(this.preparedCount::incrementAndGet);
        ResourceReloader.Store store = new ResourceReloader.Store(manager);
        reloaders.forEach(reloader -> reloader.prepareSharedState(store));
        CompletableFuture<Object> completableFuture = initialStage;
        ArrayList<CompletableFuture<S>> list = new ArrayList<CompletableFuture<S>>();
        for (ResourceReloader resourceReloader : reloaders) {
            ResourceReloader.Synchronizer synchronizer = this.createSynchronizer(resourceReloader, completableFuture, applyExecutor);
            CompletableFuture<S> completableFuture2 = factory.create(store, synchronizer, resourceReloader, executor, executor2);
            list.add(completableFuture2);
            completableFuture = completableFuture2;
        }
        return Util.combine(list);
    }

    private ResourceReloader.Synchronizer createSynchronizer(final ResourceReloader reloader, final CompletableFuture<?> future, final Executor applyExecutor) {
        return new ResourceReloader.Synchronizer(){

            @Override
            public <T> CompletableFuture<T> whenPrepared(T preparedObject) {
                applyExecutor.execute(() -> {
                    SimpleResourceReload.this.waitingReloaders.remove(reloader);
                    if (SimpleResourceReload.this.waitingReloaders.isEmpty()) {
                        SimpleResourceReload.this.prepareStageFuture.complete(Unit.INSTANCE);
                    }
                });
                return SimpleResourceReload.this.prepareStageFuture.thenCombine((CompletionStage)future, (unit, object2) -> preparedObject);
            }
        };
    }

    @Override
    public CompletableFuture<?> whenComplete() {
        return Objects.requireNonNull(this.applyStageFuture, "not started");
    }

    @Override
    public float getProgress() {
        int i = this.reloaderCount - this.waitingReloaders.size();
        float f = SimpleResourceReload.toWeighted(this.preparedCount.get(), this.appliedCount.get(), i);
        float g = SimpleResourceReload.toWeighted(this.toPrepareCount.get(), this.toApplyCount.get(), this.reloaderCount);
        return f / g;
    }

    private static int toWeighted(int prepare, int apply, int total) {
        return prepare * 2 + apply * 2 + total * 1;
    }

    public static ResourceReload start(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, boolean profiled) {
        if (profiled) {
            return ProfiledResourceReload.start(manager, reloaders, prepareExecutor, applyExecutor, initialStage);
        }
        return SimpleResourceReload.create(manager, reloaders, prepareExecutor, applyExecutor, initialStage);
    }

    @FunctionalInterface
    protected static interface Factory<S> {
        public static final Factory<Void> SIMPLE = (store, reloadSynchronizer, reloader, prepareExecutor, applyExecutor) -> reloader.reload(store, prepareExecutor, reloadSynchronizer, applyExecutor);

        public CompletableFuture<S> create(ResourceReloader.Store var1, ResourceReloader.Synchronizer var2, ResourceReloader var3, Executor var4, Executor var5);
    }
}

