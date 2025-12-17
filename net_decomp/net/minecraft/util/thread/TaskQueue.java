/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

public interface TaskQueue<T extends Runnable> {
    public @Nullable Runnable poll();

    public boolean add(T var1);

    public boolean isEmpty();

    public int getSize();

    public static final class Prioritized
    implements TaskQueue<PrioritizedTask> {
        private final Queue<Runnable>[] queue;
        private final AtomicInteger queueSize = new AtomicInteger();

        public Prioritized(int priorityCount) {
            this.queue = new Queue[priorityCount];
            for (int i = 0; i < priorityCount; ++i) {
                this.queue[i] = Queues.newConcurrentLinkedQueue();
            }
        }

        @Override
        public @Nullable Runnable poll() {
            for (Queue<Runnable> queue : this.queue) {
                Runnable runnable = queue.poll();
                if (runnable == null) continue;
                this.queueSize.decrementAndGet();
                return runnable;
            }
            return null;
        }

        @Override
        public boolean add(PrioritizedTask prioritizedTask) {
            int i = prioritizedTask.priority;
            if (i >= this.queue.length || i < 0) {
                throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", i, this.queue.length - 1));
            }
            this.queue[i].add(prioritizedTask);
            this.queueSize.incrementAndGet();
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.queueSize.get() == 0;
        }

        @Override
        public int getSize() {
            return this.queueSize.get();
        }
    }

    public static final class PrioritizedTask
    extends Record
    implements Runnable {
        final int priority;
        private final Runnable runnable;

        public PrioritizedTask(int priority, Runnable runnable) {
            this.priority = priority;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PrioritizedTask.class, "priority;task", "priority", "runnable"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PrioritizedTask.class, "priority;task", "priority", "runnable"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PrioritizedTask.class, "priority;task", "priority", "runnable"}, this, object);
        }

        public int priority() {
            return this.priority;
        }

        public Runnable runnable() {
            return this.runnable;
        }
    }

    public static final class Simple
    implements TaskQueue<Runnable> {
        private final Queue<Runnable> queue;

        public Simple(Queue<Runnable> queue) {
            this.queue = queue;
        }

        @Override
        public @Nullable Runnable poll() {
            return this.queue.poll();
        }

        @Override
        public boolean add(Runnable runnable) {
            return this.queue.add(runnable);
        }

        @Override
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override
        public int getSize() {
            return this.queue.size();
        }
    }
}

