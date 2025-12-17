/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Queues
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  javax.annotation.CheckReturnValue
 *  org.slf4j.Logger
 */
package net.minecraft.util.thread;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.CheckReturnValue;
import net.minecraft.SharedConstants;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;
import net.minecraft.util.thread.ExecutorSampling;
import net.minecraft.util.thread.SampleableExecutor;
import net.minecraft.util.thread.TaskExecutor;
import org.slf4j.Logger;

public abstract class ThreadExecutor<R extends Runnable>
implements SampleableExecutor,
TaskExecutor<R>,
Executor {
    public static final long field_52421 = 100000L;
    private final String name;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<R> tasks = Queues.newConcurrentLinkedQueue();
    private int executionsInProgress;

    protected ThreadExecutor(String name) {
        this.name = name;
        ExecutorSampling.INSTANCE.add(this);
    }

    protected abstract boolean canExecute(R var1);

    public boolean isOnThread() {
        return Thread.currentThread() == this.getThread();
    }

    protected abstract Thread getThread();

    protected boolean shouldExecuteAsync() {
        return !this.isOnThread();
    }

    public int getTaskCount() {
        return this.tasks.size();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public <V> CompletableFuture<V> submit(Supplier<V> task) {
        if (this.shouldExecuteAsync()) {
            return CompletableFuture.supplyAsync(task, this);
        }
        return CompletableFuture.completedFuture(task.get());
    }

    private CompletableFuture<Void> submitAsync(Runnable runnable) {
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return null;
        }, this);
    }

    @CheckReturnValue
    public CompletableFuture<Void> submit(Runnable task) {
        if (this.shouldExecuteAsync()) {
            return this.submitAsync(task);
        }
        task.run();
        return CompletableFuture.completedFuture(null);
    }

    public void submitAndJoin(Runnable runnable) {
        if (!this.isOnThread()) {
            this.submitAsync(runnable).join();
        } else {
            runnable.run();
        }
    }

    @Override
    public void send(R runnable) {
        this.tasks.add(runnable);
        LockSupport.unpark(this.getThread());
    }

    @Override
    public void execute(Runnable runnable) {
        Object runnable2 = this.createTask(runnable);
        if (this.shouldExecuteAsync()) {
            this.send(runnable2);
        } else {
            this.executeTask(runnable2);
        }
    }

    public void executeSync(Runnable runnable) {
        this.execute(runnable);
    }

    protected void cancelTasks() {
        this.tasks.clear();
    }

    protected void runTasks() {
        while (this.runTask()) {
        }
    }

    protected boolean isExecutionInProgress() {
        return this.executionsInProgress > 0;
    }

    public boolean runTask() {
        Runnable runnable = (Runnable)this.tasks.peek();
        if (runnable == null) {
            return false;
        }
        if (!this.isExecutionInProgress() && !this.canExecute(runnable)) {
            return false;
        }
        this.executeTask((Runnable)this.tasks.remove());
        return true;
    }

    public void runTasks(BooleanSupplier stopCondition) {
        ++this.executionsInProgress;
        try {
            while (!stopCondition.getAsBoolean()) {
                if (this.runTask()) continue;
                this.waitForTasks();
            }
        }
        finally {
            --this.executionsInProgress;
        }
    }

    protected void waitForTasks() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void executeTask(R task) {
        block8: {
            try (Zone zone = TracyClient.beginZone((String)"Task", (boolean)SharedConstants.isDevelopment);){
                task.run();
            }
            catch (Exception exception) {
                LOGGER.error(LogUtils.FATAL_MARKER, "Error executing task on {}", (Object)this.getName(), (Object)exception);
                if (!ThreadExecutor.isMemoryError(exception)) break block8;
                throw exception;
            }
        }
    }

    @Override
    public List<Sampler> createSamplers() {
        return ImmutableList.of((Object)Sampler.create(this.name + "-pending-tasks", SampleType.EVENT_LOOPS, this::getTaskCount));
    }

    public static boolean isMemoryError(Throwable exception) {
        if (exception instanceof CrashException) {
            CrashException crashException = (CrashException)exception;
            return ThreadExecutor.isMemoryError(crashException.getCause());
        }
        return exception instanceof OutOfMemoryError || exception instanceof StackOverflowError;
    }
}

