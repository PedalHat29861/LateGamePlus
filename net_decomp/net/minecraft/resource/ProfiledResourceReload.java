/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.resource;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.SimpleResourceReload;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.slf4j.Logger;

public class ProfiledResourceReload
extends SimpleResourceReload<Summary> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Stopwatch reloadTimer = Stopwatch.createUnstarted();

    public static ResourceReload start(ResourceManager manager, List<ResourceReloader> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage) {
        ProfiledResourceReload profiledResourceReload = new ProfiledResourceReload(reloaders);
        profiledResourceReload.start(prepareExecutor, applyExecutor, manager, reloaders, (store, reloadSynchronizer, reloader, prepare, apply) -> {
            AtomicLong atomicLong = new AtomicLong();
            AtomicLong atomicLong2 = new AtomicLong();
            AtomicLong atomicLong3 = new AtomicLong();
            AtomicLong atomicLong4 = new AtomicLong();
            CompletableFuture<Void> completableFuture = reloader.reload(store, ProfiledResourceReload.getProfiledExecutor(prepare, atomicLong, atomicLong2, reloader.getName()), reloadSynchronizer, ProfiledResourceReload.getProfiledExecutor(apply, atomicLong3, atomicLong4, reloader.getName()));
            return completableFuture.thenApplyAsync(v -> {
                LOGGER.debug("Finished reloading {}", (Object)reloader.getName());
                return new Summary(reloader.getName(), atomicLong, atomicLong2, atomicLong3, atomicLong4);
            }, applyExecutor);
        }, initialStage);
        return profiledResourceReload;
    }

    private ProfiledResourceReload(List<ResourceReloader> waitingReloaders) {
        super(waitingReloaders);
        this.reloadTimer.start();
    }

    @Override
    protected CompletableFuture<List<Summary>> startAsync(Executor prepareExecutor, Executor applyExecutor, ResourceManager manager, List<ResourceReloader> reloaders, SimpleResourceReload.Factory<Summary> factory, CompletableFuture<?> initialStage) {
        return super.startAsync(prepareExecutor, applyExecutor, manager, reloaders, factory, initialStage).thenApplyAsync(this::finish, applyExecutor);
    }

    private static Executor getProfiledExecutor(Executor executor, AtomicLong output, AtomicLong counter, String name) {
        return runnable -> executor.execute(() -> {
            Profiler profiler = Profilers.get();
            profiler.push(name);
            long l = Util.getMeasuringTimeNano();
            runnable.run();
            output.addAndGet(Util.getMeasuringTimeNano() - l);
            counter.incrementAndGet();
            profiler.pop();
        });
    }

    private List<Summary> finish(List<Summary> summaries) {
        this.reloadTimer.stop();
        long l = 0L;
        LOGGER.info("Resource reload finished after {} ms", (Object)this.reloadTimer.elapsed(TimeUnit.MILLISECONDS));
        for (Summary summary : summaries) {
            long m = TimeUnit.NANOSECONDS.toMillis(summary.prepareTimeMs.get());
            long n = summary.preparationCount.get();
            long o = TimeUnit.NANOSECONDS.toMillis(summary.applyTimeMs.get());
            long p = summary.reloadCount.get();
            long q = m + o;
            long r = n + p;
            String string = summary.name;
            LOGGER.info("{} took approximately {} tasks/{} ms ({} tasks/{} ms preparing, {} tasks/{} ms applying)", new Object[]{string, r, q, n, m, p, o});
            l += o;
        }
        LOGGER.info("Total blocking time: {} ms", (Object)l);
        return summaries;
    }

    public static final class Summary
    extends Record {
        final String name;
        final AtomicLong prepareTimeMs;
        final AtomicLong preparationCount;
        final AtomicLong applyTimeMs;
        final AtomicLong reloadCount;

        public Summary(String name, AtomicLong prepareTimeMs, AtomicLong preparationCount, AtomicLong applyTimeMs, AtomicLong reloadCount) {
            this.name = name;
            this.prepareTimeMs = prepareTimeMs;
            this.preparationCount = preparationCount;
            this.applyTimeMs = applyTimeMs;
            this.reloadCount = reloadCount;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Summary.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "prepareTimeMs", "preparationCount", "applyTimeMs", "reloadCount"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Summary.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "prepareTimeMs", "preparationCount", "applyTimeMs", "reloadCount"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Summary.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "prepareTimeMs", "preparationCount", "applyTimeMs", "reloadCount"}, this, object);
        }

        public String name() {
            return this.name;
        }

        public AtomicLong prepareTimeMs() {
            return this.prepareTimeMs;
        }

        public AtomicLong preparationCount() {
            return this.preparationCount;
        }

        public AtomicLong applyTimeMs() {
            return this.applyTimeMs;
        }

        public AtomicLong reloadCount() {
            return this.reloadCount;
        }
    }
}

