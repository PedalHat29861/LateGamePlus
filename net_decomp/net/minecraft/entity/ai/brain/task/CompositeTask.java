/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.WeightedList;

public class CompositeTask<E extends LivingEntity>
implements Task<E> {
    private final Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState;
    private final Set<MemoryModuleType<?>> memoriesToForgetWhenStopped;
    private final Order order;
    private final RunMode runMode;
    private final WeightedList<Task<? super E>> tasks = new WeightedList();
    private MultiTickTask.Status status = MultiTickTask.Status.STOPPED;

    public CompositeTask(Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState, Set<MemoryModuleType<?>> memoriesToForgetWhenStopped, Order order, RunMode runMode, List<Pair<? extends Task<? super E>, Integer>> tasks) {
        this.requiredMemoryState = requiredMemoryState;
        this.memoriesToForgetWhenStopped = memoriesToForgetWhenStopped;
        this.order = order;
        this.runMode = runMode;
        tasks.forEach(task -> this.tasks.add((Task)task.getFirst(), (Integer)task.getSecond()));
    }

    @Override
    public MultiTickTask.Status getStatus() {
        return this.status;
    }

    private boolean shouldStart(E entity) {
        for (Map.Entry<MemoryModuleType<?>, MemoryModuleState> entry : this.requiredMemoryState.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            MemoryModuleState memoryModuleState = entry.getValue();
            if (((LivingEntity)entity).getBrain().isMemoryInState(memoryModuleType, memoryModuleState)) continue;
            return false;
        }
        return true;
    }

    @Override
    public final boolean tryStarting(ServerWorld world, E entity, long time) {
        if (this.shouldStart(entity)) {
            this.status = MultiTickTask.Status.RUNNING;
            this.order.apply(this.tasks);
            this.runMode.run(this.tasks.stream(), world, entity, time);
            return true;
        }
        return false;
    }

    @Override
    public final void tick(ServerWorld world, E entity, long time) {
        this.tasks.stream().filter(task -> task.getStatus() == MultiTickTask.Status.RUNNING).forEach(task -> task.tick(world, entity, time));
        if (this.tasks.stream().noneMatch(task -> task.getStatus() == MultiTickTask.Status.RUNNING)) {
            this.stop(world, entity, time);
        }
    }

    @Override
    public final void stop(ServerWorld world, E entity, long time) {
        this.status = MultiTickTask.Status.STOPPED;
        this.tasks.stream().filter(task -> task.getStatus() == MultiTickTask.Status.RUNNING).forEach(task -> task.stop(world, entity, time));
        this.memoriesToForgetWhenStopped.forEach(((LivingEntity)entity).getBrain()::forget);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public String toString() {
        Set set = this.tasks.stream().filter(task -> task.getStatus() == MultiTickTask.Status.RUNNING).collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + String.valueOf(set);
    }

    public static final class Order
    extends Enum<Order> {
        public static final /* enum */ Order ORDERED = new Order(list -> {});
        public static final /* enum */ Order SHUFFLED = new Order(WeightedList::shuffle);
        private final Consumer<WeightedList<?>> listModifier;
        private static final /* synthetic */ Order[] field_18351;

        public static Order[] values() {
            return (Order[])field_18351.clone();
        }

        public static Order valueOf(String string) {
            return Enum.valueOf(Order.class, string);
        }

        private Order(Consumer<WeightedList<?>> listModifier) {
            this.listModifier = listModifier;
        }

        public void apply(WeightedList<?> list) {
            this.listModifier.accept(list);
        }

        private static /* synthetic */ Order[] method_36617() {
            return new Order[]{ORDERED, SHUFFLED};
        }

        static {
            field_18351 = Order.method_36617();
        }
    }

    public static abstract sealed class RunMode
    extends Enum<RunMode> {
        public static final /* enum */ RunMode RUN_ONE = new RunMode(){

            @Override
            public <E extends LivingEntity> void run(Stream<Task<? super E>> tasks, ServerWorld world, E entity, long time) {
                tasks.filter(task -> task.getStatus() == MultiTickTask.Status.STOPPED).filter(task -> task.tryStarting(world, entity, time)).findFirst();
            }
        };
        public static final /* enum */ RunMode TRY_ALL = new RunMode(){

            @Override
            public <E extends LivingEntity> void run(Stream<Task<? super E>> tasks, ServerWorld world, E entity, long time) {
                tasks.filter(task -> task.getStatus() == MultiTickTask.Status.STOPPED).forEach(task -> task.tryStarting(world, entity, time));
            }
        };
        private static final /* synthetic */ RunMode[] field_18857;

        public static RunMode[] values() {
            return (RunMode[])field_18857.clone();
        }

        public static RunMode valueOf(String string) {
            return Enum.valueOf(RunMode.class, string);
        }

        public abstract <E extends LivingEntity> void run(Stream<Task<? super E>> var1, ServerWorld var2, E var3, long var4);

        private static /* synthetic */ RunMode[] method_36618() {
            return new RunMode[]{RUN_ONE, TRY_ALL};
        }

        static {
            field_18857 = RunMode.method_36618();
        }
    }
}

