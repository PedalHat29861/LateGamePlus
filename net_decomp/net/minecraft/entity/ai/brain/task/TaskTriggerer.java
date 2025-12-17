/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.kinds.Applicative$Mu
 *  com.mojang.datafixers.kinds.Const$Mu
 *  com.mojang.datafixers.kinds.IdF
 *  com.mojang.datafixers.kinds.IdF$Mu
 *  com.mojang.datafixers.kinds.K1
 *  com.mojang.datafixers.kinds.OptionalBox
 *  com.mojang.datafixers.kinds.OptionalBox$Mu
 *  com.mojang.datafixers.util.Function3
 *  com.mojang.datafixers.util.Function4
 *  com.mojang.datafixers.util.Unit
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.MemoryQuery;
import net.minecraft.entity.ai.brain.MemoryQueryResult;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskRunnable;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.Nullable;

public class TaskTriggerer<E extends LivingEntity, M>
implements App<K1<E>, M> {
    private final TaskFunction<E, M> function;

    public static <E extends LivingEntity, M> TaskTriggerer<E, M> cast(App<K1<E>, M> app) {
        return (TaskTriggerer)app;
    }

    public static <E extends LivingEntity> TaskContext<E> newContext() {
        return new TaskContext();
    }

    public static <E extends LivingEntity> SingleTickTask<E> task(Function<TaskContext<E>, ? extends App<K1<E>, TaskRunnable<E>>> creator) {
        final TaskFunction<E, TaskRunnable<E>> taskFunction = TaskTriggerer.getFunction(creator.apply(TaskTriggerer.newContext()));
        return new SingleTickTask<E>(){

            @Override
            public boolean trigger(ServerWorld serverWorld, E livingEntity, long l) {
                TaskRunnable taskRunnable = (TaskRunnable)taskFunction.run(serverWorld, livingEntity, l);
                if (taskRunnable == null) {
                    return false;
                }
                return taskRunnable.trigger(serverWorld, livingEntity, l);
            }

            @Override
            public String getName() {
                return "OneShot[" + taskFunction.asString() + "]";
            }

            public String toString() {
                return this.getName();
            }
        };
    }

    public static <E extends LivingEntity> SingleTickTask<E> runIf(TaskRunnable<? super E> predicate, TaskRunnable<? super E> task) {
        return TaskTriggerer.task(context -> context.group(context.trigger(predicate)).apply((Applicative)context, unit -> task::trigger));
    }

    public static <E extends LivingEntity> SingleTickTask<E> runIf(Predicate<E> predicate, SingleTickTask<? super E> task) {
        return TaskTriggerer.runIf(TaskTriggerer.predicate(predicate), task);
    }

    public static <E extends LivingEntity> SingleTickTask<E> predicate(Predicate<E> predicate) {
        return TaskTriggerer.task(context -> context.point((world, entity, time) -> predicate.test(entity)));
    }

    public static <E extends LivingEntity> SingleTickTask<E> predicate(BiPredicate<ServerWorld, E> predicate) {
        return TaskTriggerer.task(context -> context.point((world, entity, time) -> predicate.test(world, entity)));
    }

    static <E extends LivingEntity, M> TaskFunction<E, M> getFunction(App<K1<E>, M> app) {
        return TaskTriggerer.cast(app).function;
    }

    TaskTriggerer(TaskFunction<E, M> function) {
        this.function = function;
    }

    static <E extends LivingEntity, M> TaskTriggerer<E, M> of(TaskFunction<E, M> function) {
        return new TaskTriggerer<E, M>(function);
    }

    public static final class TaskContext<E extends LivingEntity>
    implements Applicative<K1<E>, Mu<E>> {
        public <Value> Optional<Value> getOptionalValue(MemoryQueryResult<OptionalBox.Mu, Value> result) {
            return OptionalBox.unbox(result.getValue());
        }

        public <Value> Value getValue(MemoryQueryResult<IdF.Mu, Value> result) {
            return (Value)IdF.get(result.getValue());
        }

        public <Value> TaskTriggerer<E, MemoryQueryResult<OptionalBox.Mu, Value>> queryMemoryOptional(MemoryModuleType<Value> type) {
            return new QueryMemory(new MemoryQuery.Optional<Value>(type));
        }

        public <Value> TaskTriggerer<E, MemoryQueryResult<IdF.Mu, Value>> queryMemoryValue(MemoryModuleType<Value> type) {
            return new QueryMemory(new MemoryQuery.Value<Value>(type));
        }

        public <Value> TaskTriggerer<E, MemoryQueryResult<Const.Mu<Unit>, Value>> queryMemoryAbsent(MemoryModuleType<Value> type) {
            return new QueryMemory(new MemoryQuery.Absent<Value>(type));
        }

        public TaskTriggerer<E, Unit> trigger(TaskRunnable<? super E> runnable) {
            return new Trigger<E>(runnable);
        }

        public <A> TaskTriggerer<E, A> point(A object) {
            return new Supply(object);
        }

        public <A> TaskTriggerer<E, A> supply(Supplier<String> nameSupplier, A value) {
            return new Supply(value, nameSupplier);
        }

        public <A, R> Function<App<K1<E>, A>, App<K1<E>, R>> lift1(App<K1<E>, Function<A, R>> app) {
            return app2 -> {
                final TaskFunction taskFunction = TaskTriggerer.getFunction(app2);
                final TaskFunction taskFunction2 = TaskTriggerer.getFunction(app);
                return TaskTriggerer.of(new TaskFunction<E, R>(this){

                    @Override
                    public R run(ServerWorld world, E entity, long time) {
                        Object object = taskFunction.run(world, entity, time);
                        if (object == null) {
                            return null;
                        }
                        Function function = (Function)taskFunction2.run(world, entity, time);
                        if (function == null) {
                            return null;
                        }
                        return function.apply(object);
                    }

                    @Override
                    public String asString() {
                        return taskFunction2.asString() + " * " + taskFunction.asString();
                    }

                    public String toString() {
                        return this.asString();
                    }
                });
            };
        }

        public <T, R> TaskTriggerer<E, R> map(final Function<? super T, ? extends R> function, App<K1<E>, T> app) {
            final TaskFunction<E, T> taskFunction = TaskTriggerer.getFunction(app);
            return TaskTriggerer.of(new TaskFunction<E, R>(this){

                @Override
                public R run(ServerWorld world, E entity, long time) {
                    Object object = taskFunction.run(world, entity, time);
                    if (object == null) {
                        return null;
                    }
                    return function.apply(object);
                }

                @Override
                public String asString() {
                    return taskFunction.asString() + ".map[" + String.valueOf(function) + "]";
                }

                public String toString() {
                    return this.asString();
                }
            });
        }

        public <A, B, R> TaskTriggerer<E, R> ap2(App<K1<E>, BiFunction<A, B, R>> app, App<K1<E>, A> app2, App<K1<E>, B> app3) {
            final TaskFunction<E, A> taskFunction = TaskTriggerer.getFunction(app2);
            final TaskFunction<E, B> taskFunction2 = TaskTriggerer.getFunction(app3);
            final TaskFunction<E, BiFunction<A, B, R>> taskFunction3 = TaskTriggerer.getFunction(app);
            return TaskTriggerer.of(new TaskFunction<E, R>(this){

                @Override
                public R run(ServerWorld world, E entity, long time) {
                    Object object = taskFunction.run(world, entity, time);
                    if (object == null) {
                        return null;
                    }
                    Object object2 = taskFunction2.run(world, entity, time);
                    if (object2 == null) {
                        return null;
                    }
                    BiFunction biFunction = (BiFunction)taskFunction3.run(world, entity, time);
                    if (biFunction == null) {
                        return null;
                    }
                    return biFunction.apply(object, object2);
                }

                @Override
                public String asString() {
                    return taskFunction3.asString() + " * " + taskFunction.asString() + " * " + taskFunction2.asString();
                }

                public String toString() {
                    return this.asString();
                }
            });
        }

        public <T1, T2, T3, R> TaskTriggerer<E, R> ap3(App<K1<E>, Function3<T1, T2, T3, R>> app, App<K1<E>, T1> app2, App<K1<E>, T2> app3, App<K1<E>, T3> app4) {
            final TaskFunction<E, T1> taskFunction = TaskTriggerer.getFunction(app2);
            final TaskFunction<E, T2> taskFunction2 = TaskTriggerer.getFunction(app3);
            final TaskFunction<E, T3> taskFunction3 = TaskTriggerer.getFunction(app4);
            final TaskFunction<E, Function3<T1, T2, T3, R>> taskFunction4 = TaskTriggerer.getFunction(app);
            return TaskTriggerer.of(new TaskFunction<E, R>(this){

                @Override
                public R run(ServerWorld world, E entity, long time) {
                    Object object = taskFunction.run(world, entity, time);
                    if (object == null) {
                        return null;
                    }
                    Object object2 = taskFunction2.run(world, entity, time);
                    if (object2 == null) {
                        return null;
                    }
                    Object object3 = taskFunction3.run(world, entity, time);
                    if (object3 == null) {
                        return null;
                    }
                    Function3 function3 = (Function3)taskFunction4.run(world, entity, time);
                    if (function3 == null) {
                        return null;
                    }
                    return function3.apply(object, object2, object3);
                }

                @Override
                public String asString() {
                    return taskFunction4.asString() + " * " + taskFunction.asString() + " * " + taskFunction2.asString() + " * " + taskFunction3.asString();
                }

                public String toString() {
                    return this.asString();
                }
            });
        }

        public <T1, T2, T3, T4, R> TaskTriggerer<E, R> ap4(App<K1<E>, Function4<T1, T2, T3, T4, R>> app, App<K1<E>, T1> app2, App<K1<E>, T2> app3, App<K1<E>, T3> app4, App<K1<E>, T4> app5) {
            final TaskFunction<E, T1> taskFunction = TaskTriggerer.getFunction(app2);
            final TaskFunction<E, T2> taskFunction2 = TaskTriggerer.getFunction(app3);
            final TaskFunction<E, T3> taskFunction3 = TaskTriggerer.getFunction(app4);
            final TaskFunction<E, T4> taskFunction4 = TaskTriggerer.getFunction(app5);
            final TaskFunction<E, Function4<T1, T2, T3, T4, R>> taskFunction5 = TaskTriggerer.getFunction(app);
            return TaskTriggerer.of(new TaskFunction<E, R>(this){

                @Override
                public R run(ServerWorld world, E entity, long time) {
                    Object object = taskFunction.run(world, entity, time);
                    if (object == null) {
                        return null;
                    }
                    Object object2 = taskFunction2.run(world, entity, time);
                    if (object2 == null) {
                        return null;
                    }
                    Object object3 = taskFunction3.run(world, entity, time);
                    if (object3 == null) {
                        return null;
                    }
                    Object object4 = taskFunction4.run(world, entity, time);
                    if (object4 == null) {
                        return null;
                    }
                    Function4 function4 = (Function4)taskFunction5.run(world, entity, time);
                    if (function4 == null) {
                        return null;
                    }
                    return function4.apply(object, object2, object3, object4);
                }

                @Override
                public String asString() {
                    return taskFunction5.asString() + " * " + taskFunction.asString() + " * " + taskFunction2.asString() + " * " + taskFunction3.asString() + " * " + taskFunction4.asString();
                }

                public String toString() {
                    return this.asString();
                }
            });
        }

        public /* synthetic */ App ap4(App app, App function1, App function2, App function3, App function4) {
            return this.ap4(app, function1, function2, function3, function4);
        }

        public /* synthetic */ App ap3(App app, App function1, App function2, App function3) {
            return this.ap3(app, function1, function2, function3);
        }

        public /* synthetic */ App ap2(App app, App function1, App function2) {
            return this.ap2(app, function1, function2);
        }

        public /* synthetic */ App point(Object value) {
            return this.point(value);
        }

        public /* synthetic */ App map(Function function, App app) {
            return this.map(function, app);
        }

        static final class Mu<E extends LivingEntity>
        implements Applicative.Mu {
            private Mu() {
            }
        }
    }

    static interface TaskFunction<E extends LivingEntity, R> {
        public @Nullable R run(ServerWorld var1, E var2, long var3);

        public String asString();
    }

    static final class Trigger<E extends LivingEntity>
    extends TaskTriggerer<E, Unit> {
        Trigger(final TaskRunnable<? super E> taskRunnable) {
            super(new TaskFunction<E, Unit>(){

                @Override
                public @Nullable Unit run(ServerWorld serverWorld, E livingEntity, long l) {
                    return taskRunnable.trigger(serverWorld, livingEntity, l) ? Unit.INSTANCE : null;
                }

                @Override
                public String asString() {
                    return "T[" + String.valueOf(taskRunnable) + "]";
                }

                @Override
                public /* synthetic */ @Nullable Object run(ServerWorld world, LivingEntity entity, long time) {
                    return this.run(world, (Object)entity, time);
                }
            });
        }
    }

    static final class Supply<E extends LivingEntity, A>
    extends TaskTriggerer<E, A> {
        Supply(A value) {
            this(value, () -> "C[" + String.valueOf(value) + "]");
        }

        Supply(final A value, final Supplier<String> nameSupplier) {
            super(new TaskFunction<E, A>(){

                @Override
                public A run(ServerWorld world, E entity, long time) {
                    return value;
                }

                @Override
                public String asString() {
                    return (String)nameSupplier.get();
                }

                public String toString() {
                    return this.asString();
                }
            });
        }
    }

    static final class QueryMemory<E extends LivingEntity, F extends com.mojang.datafixers.kinds.K1, Value>
    extends TaskTriggerer<E, MemoryQueryResult<F, Value>> {
        QueryMemory(final MemoryQuery<F, Value> query) {
            super(new TaskFunction<E, MemoryQueryResult<F, Value>>(){

                @Override
                public @Nullable MemoryQueryResult<F, Value> run(ServerWorld serverWorld, E livingEntity, long l) {
                    Brain<?> brain = ((LivingEntity)livingEntity).getBrain();
                    Optional optional = brain.getOptionalMemory(query.memory());
                    if (optional == null) {
                        return null;
                    }
                    return query.toQueryResult(brain, optional);
                }

                @Override
                public String asString() {
                    return "M[" + String.valueOf(query) + "]";
                }

                public String toString() {
                    return this.asString();
                }

                @Override
                public /* synthetic */ @Nullable Object run(ServerWorld world, LivingEntity entity, long time) {
                    return this.run(world, entity, time);
                }
            });
        }
    }

    public static final class K1<E extends LivingEntity>
    implements com.mojang.datafixers.kinds.K1 {
    }
}

