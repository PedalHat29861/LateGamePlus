/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface TaskExecutor<R extends Runnable>
extends AutoCloseable {
    public String getName();

    public void send(R var1);

    @Override
    default public void close() {
    }

    public R createTask(Runnable var1);

    default public <Source> CompletableFuture<Source> executeAsync(Consumer<CompletableFuture<Source>> future) {
        CompletableFuture completableFuture = new CompletableFuture();
        this.send(this.createTask(() -> future.accept(completableFuture)));
        return completableFuture;
    }

    public static TaskExecutor<Runnable> of(final String name, final Executor executor) {
        return new TaskExecutor<Runnable>(){

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void send(Runnable runnable) {
                executor.execute(runnable);
            }

            @Override
            public Runnable createTask(Runnable runnable) {
                return runnable;
            }

            public String toString() {
                return name;
            }
        };
    }
}

