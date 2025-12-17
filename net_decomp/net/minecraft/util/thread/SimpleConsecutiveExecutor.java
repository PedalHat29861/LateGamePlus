/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.util.thread.TaskQueue;

public class SimpleConsecutiveExecutor
extends ConsecutiveExecutor<Runnable> {
    public SimpleConsecutiveExecutor(Executor executor, String name) {
        super(new TaskQueue.Simple(new ConcurrentLinkedQueue<Runnable>()), executor, name);
    }

    @Override
    public Runnable createTask(Runnable runnable) {
        return runnable;
    }
}

