/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.ai.brain;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Brain<E extends LivingEntity> {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Supplier<Codec<Brain<E>>> codecSupplier;
    private static final int ACTIVITY_REFRESH_COOLDOWN = 20;
    private final Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks = Maps.newTreeMap();
    private @Nullable EnvironmentAttribute<Activity> schedule;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleState>>> requiredActivityMemories = Maps.newHashMap();
    private final Map<Activity, Set<MemoryModuleType<?>>> forgettingActivityMemories = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> possibleActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long activityStartTime = -9999L;

    public static <E extends LivingEntity> Profile<E> createProfile(Collection<? extends MemoryModuleType<?>> memoryModules, Collection<? extends SensorType<? extends Sensor<? super E>>> sensors) {
        return new Profile(memoryModules, sensors);
    }

    public static <E extends LivingEntity> Codec<Brain<E>> createBrainCodec(final Collection<? extends MemoryModuleType<?>> memoryModules, final Collection<? extends SensorType<? extends Sensor<? super E>>> sensors) {
        final MutableObject mutableObject = new MutableObject();
        mutableObject.setValue((Object)new MapCodec<Brain<E>>(){

            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return memoryModules.stream().flatMap(memoryType -> memoryType.getCodec().map(codec -> Registries.MEMORY_MODULE_TYPE.getId((MemoryModuleType<?>)memoryType)).stream()).map(id -> ops.createString(id.toString()));
            }

            public <T> DataResult<Brain<E>> decode(DynamicOps<T> ops, MapLike<T> map) {
                MutableObject mutableObject2 = new MutableObject((Object)DataResult.success((Object)ImmutableList.builder()));
                map.entries().forEach(pair -> {
                    DataResult dataResult = Registries.MEMORY_MODULE_TYPE.getCodec().parse(ops, pair.getFirst());
                    DataResult dataResult2 = dataResult.flatMap(memoryType -> this.parse((MemoryModuleType)memoryType, ops, (Object)pair.getSecond()));
                    mutableObject2.setValue((Object)((DataResult)mutableObject2.get()).apply2(ImmutableList.Builder::add, dataResult2));
                });
                ImmutableList immutableList = ((DataResult)mutableObject2.get()).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).map(ImmutableList.Builder::build).orElseGet(ImmutableList::of);
                return DataResult.success(new Brain(memoryModules, sensors, (ImmutableList<MemoryEntry<?>>)immutableList, mutableObject));
            }

            private <T, U> DataResult<MemoryEntry<U>> parse(MemoryModuleType<U> memoryType, DynamicOps<T> ops, T value) {
                return memoryType.getCodec().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No codec for memory: " + String.valueOf(memoryType))).flatMap(codec -> codec.parse(ops, value)).map(data -> new MemoryEntry(memoryType, Optional.of(data)));
            }

            public <T> RecordBuilder<T> encode(Brain<E> brain, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                brain.streamMemories().forEach(entry -> entry.serialize(dynamicOps, recordBuilder));
                return recordBuilder;
            }

            public /* synthetic */ RecordBuilder encode(Object brain, DynamicOps ops, RecordBuilder recordBuilder) {
                return this.encode((Brain)brain, ops, recordBuilder);
            }
        }.fieldOf("memories").codec());
        return (Codec)mutableObject.get();
    }

    public Brain(Collection<? extends MemoryModuleType<?>> memories, Collection<? extends SensorType<? extends Sensor<? super E>>> sensors, ImmutableList<MemoryEntry<?>> memoryEntries, Supplier<Codec<Brain<E>>> codecSupplier) {
        this.codecSupplier = codecSupplier;
        for (MemoryModuleType<?> memoryModuleType : memories) {
            this.memories.put(memoryModuleType, Optional.empty());
        }
        for (SensorType sensorType : sensors) {
            this.sensors.put(sensorType, (Sensor<E>)sensorType.create());
        }
        for (Sensor sensor : this.sensors.values()) {
            for (MemoryModuleType<?> memoryModuleType2 : sensor.getOutputMemoryModules()) {
                this.memories.put(memoryModuleType2, Optional.empty());
            }
        }
        for (MemoryEntry memoryEntry : memoryEntries) {
            memoryEntry.apply(this);
        }
    }

    public <T> DataResult<T> encode(DynamicOps<T> ops) {
        return this.codecSupplier.get().encodeStart(ops, (Object)this);
    }

    Stream<MemoryEntry<?>> streamMemories() {
        return this.memories.entrySet().stream().map(entry -> MemoryEntry.of((MemoryModuleType)entry.getKey(), (Optional)entry.getValue()));
    }

    public boolean hasMemoryModule(MemoryModuleType<?> type) {
        return this.isMemoryInState(type, MemoryModuleState.VALUE_PRESENT);
    }

    public void forgetAll() {
        this.memories.keySet().forEach(type -> this.memories.put((MemoryModuleType<?>)type, Optional.empty()));
    }

    public <U> void forget(MemoryModuleType<U> type) {
        this.remember(type, Optional.empty());
    }

    public <U> void remember(MemoryModuleType<U> type, @Nullable U value) {
        this.remember(type, Optional.ofNullable(value));
    }

    public <U> void remember(MemoryModuleType<U> type, U value, long expiry) {
        this.setMemory(type, Optional.of(Memory.timed(value, expiry)));
    }

    public <U> void remember(MemoryModuleType<U> type, Optional<? extends U> value) {
        this.setMemory(type, value.map(Memory::permanent));
    }

    <U> void setMemory(MemoryModuleType<U> type, Optional<? extends Memory<?>> memory) {
        if (this.memories.containsKey(type)) {
            if (memory.isPresent() && this.isEmptyCollection(memory.get().getValue())) {
                this.forget(type);
            } else {
                this.memories.put(type, memory);
            }
        }
    }

    public <U> Optional<U> getOptionalRegisteredMemory(MemoryModuleType<U> type) {
        Optional<Memory<?>> optional = this.memories.get(type);
        if (optional == null) {
            throw new IllegalStateException("Unregistered memory fetched: " + String.valueOf(type));
        }
        return optional.map(Memory::getValue);
    }

    public <U> @Nullable Optional<U> getOptionalMemory(MemoryModuleType<U> type) {
        Optional<Memory<?>> optional = this.memories.get(type);
        if (optional == null) {
            return null;
        }
        return optional.map(Memory::getValue);
    }

    public <U> long getMemoryExpiry(MemoryModuleType<U> type) {
        Optional<Memory<?>> optional = this.memories.get(type);
        return optional.map(Memory::getExpiry).orElse(0L);
    }

    @Deprecated
    @Debug
    public Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> getMemories() {
        return this.memories;
    }

    public <U> boolean hasMemoryModuleWithValue(MemoryModuleType<U> type, U value) {
        if (!this.hasMemoryModule(type)) {
            return false;
        }
        return this.getOptionalRegisteredMemory(type).filter(memoryValue -> memoryValue.equals(value)).isPresent();
    }

    public boolean isMemoryInState(MemoryModuleType<?> type, MemoryModuleState state) {
        Optional<Memory<?>> optional = this.memories.get(type);
        if (optional == null) {
            return false;
        }
        return state == MemoryModuleState.REGISTERED || state == MemoryModuleState.VALUE_PRESENT && optional.isPresent() || state == MemoryModuleState.VALUE_ABSENT && optional.isEmpty();
    }

    public void setSchedule(EnvironmentAttribute<Activity> schedule) {
        this.schedule = schedule;
    }

    public void setCoreActivities(Set<Activity> coreActivities) {
        this.coreActivities = coreActivities;
    }

    @Deprecated
    @Debug
    public Set<Activity> getPossibleActivities() {
        return this.possibleActivities;
    }

    @Deprecated
    @Debug
    public List<Task<? super E>> getRunningTasks() {
        ObjectArrayList list = new ObjectArrayList();
        for (Map<Activity, Set<Task<E>>> map : this.tasks.values()) {
            for (Set<Task<E>> set : map.values()) {
                for (Task<E> task : set) {
                    if (task.getStatus() != MultiTickTask.Status.RUNNING) continue;
                    list.add(task);
                }
            }
        }
        return list;
    }

    public void resetPossibleActivities() {
        this.resetPossibleActivities(this.defaultActivity);
    }

    public Optional<Activity> getFirstPossibleNonCoreActivity() {
        for (Activity activity : this.possibleActivities) {
            if (this.coreActivities.contains(activity)) continue;
            return Optional.of(activity);
        }
        return Optional.empty();
    }

    public void doExclusively(Activity activity) {
        if (this.canDoActivity(activity)) {
            this.resetPossibleActivities(activity);
        } else {
            this.resetPossibleActivities();
        }
    }

    private void resetPossibleActivities(Activity except) {
        if (this.hasActivity(except)) {
            return;
        }
        this.forgetIrrelevantMemories(except);
        this.possibleActivities.clear();
        this.possibleActivities.addAll(this.coreActivities);
        this.possibleActivities.add(except);
    }

    private void forgetIrrelevantMemories(Activity except) {
        for (Activity activity : this.possibleActivities) {
            Set<MemoryModuleType<?>> set;
            if (activity == except || (set = this.forgettingActivityMemories.get(activity)) == null) continue;
            for (MemoryModuleType<?> memoryModuleType : set) {
                this.forget(memoryModuleType);
            }
        }
    }

    public void refreshActivities(WorldEnvironmentAttributeAccess attributeAccess, long time, Vec3d pos) {
        if (time - this.activityStartTime > 20L) {
            Activity activity;
            this.activityStartTime = time;
            Activity activity2 = activity = this.schedule != null ? attributeAccess.getAttributeValue(this.schedule, pos) : Activity.IDLE;
            if (!this.possibleActivities.contains(activity)) {
                this.doExclusively(activity);
            }
        }
    }

    public void resetPossibleActivities(List<Activity> activities) {
        for (Activity activity : activities) {
            if (!this.canDoActivity(activity)) continue;
            this.resetPossibleActivities(activity);
            break;
        }
    }

    public void setDefaultActivity(Activity activity) {
        this.defaultActivity = activity;
    }

    public void setTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> list) {
        this.setTaskList(activity, this.indexTaskList(begin, list));
    }

    public void setTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> tasks, MemoryModuleType<?> memoryType) {
        ImmutableSet set = ImmutableSet.of((Object)Pair.of(memoryType, (Object)((Object)MemoryModuleState.VALUE_PRESENT)));
        ImmutableSet set2 = ImmutableSet.of(memoryType);
        this.setTaskList(activity, (ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>>)this.indexTaskList(begin, tasks), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)set, (Set<MemoryModuleType<?>>)set2);
    }

    public void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks) {
        this.setTaskList(activity, indexedTasks, (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of(), Sets.newHashSet());
    }

    public void setTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> tasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories) {
        this.setTaskList(activity, this.indexTaskList(begin, tasks), requiredMemories);
    }

    public void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories) {
        this.setTaskList(activity, indexedTasks, requiredMemories, Sets.newHashSet());
    }

    public void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories) {
        this.requiredActivityMemories.put(activity, requiredMemories);
        if (!forgettingMemories.isEmpty()) {
            this.forgettingActivityMemories.put(activity, forgettingMemories);
        }
        for (Pair pair : indexedTasks) {
            this.tasks.computeIfAbsent((Integer)pair.getFirst(), index -> Maps.newHashMap()).computeIfAbsent(activity, activity2 -> Sets.newLinkedHashSet()).add((Task)pair.getSecond());
        }
    }

    @VisibleForTesting
    public void clear() {
        this.tasks.clear();
    }

    public boolean hasActivity(Activity activity) {
        return this.possibleActivities.contains(activity);
    }

    public Brain<E> copy() {
        Brain<E> brain = new Brain<E>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codecSupplier);
        for (Map.Entry<MemoryModuleType<?>, Optional<Memory<?>>> entry : this.memories.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            if (!entry.getValue().isPresent()) continue;
            brain.memories.put(memoryModuleType, entry.getValue());
        }
        return brain;
    }

    public void tick(ServerWorld world, E entity) {
        this.tickMemories();
        this.tickSensors(world, entity);
        this.startTasks(world, entity);
        this.updateTasks(world, entity);
    }

    private void tickSensors(ServerWorld world, E entity) {
        for (Sensor<E> sensor : this.sensors.values()) {
            sensor.tick(world, entity);
        }
    }

    private void tickMemories() {
        for (Map.Entry<MemoryModuleType<?>, Optional<Memory<?>>> entry : this.memories.entrySet()) {
            if (!entry.getValue().isPresent()) continue;
            Memory<?> memory = entry.getValue().get();
            if (memory.isExpired()) {
                this.forget(entry.getKey());
            }
            memory.tick();
        }
    }

    public void stopAllTasks(ServerWorld world, E entity) {
        long l = ((Entity)entity).getEntityWorld().getTime();
        for (Task<E> task : this.getRunningTasks()) {
            task.stop(world, entity, l);
        }
    }

    private void startTasks(ServerWorld world, E entity) {
        long l = world.getTime();
        for (Map<Activity, Set<Task<E>>> map : this.tasks.values()) {
            for (Map.Entry<Activity, Set<Task<E>>> entry : map.entrySet()) {
                Activity activity = entry.getKey();
                if (!this.possibleActivities.contains(activity)) continue;
                Set<Task<E>> set = entry.getValue();
                for (Task<E> task : set) {
                    if (task.getStatus() != MultiTickTask.Status.STOPPED) continue;
                    task.tryStarting(world, entity, l);
                }
            }
        }
    }

    private void updateTasks(ServerWorld world, E entity) {
        long l = world.getTime();
        for (Task<E> task : this.getRunningTasks()) {
            task.tick(world, entity, l);
        }
    }

    private boolean canDoActivity(Activity activity) {
        if (!this.requiredActivityMemories.containsKey(activity)) {
            return false;
        }
        for (Pair<MemoryModuleType<?>, MemoryModuleState> pair : this.requiredActivityMemories.get(activity)) {
            MemoryModuleState memoryModuleState;
            MemoryModuleType memoryModuleType = (MemoryModuleType)pair.getFirst();
            if (this.isMemoryInState(memoryModuleType, memoryModuleState = (MemoryModuleState)((Object)pair.getSecond()))) continue;
            return false;
        }
        return true;
    }

    private boolean isEmptyCollection(Object value) {
        return value instanceof Collection && ((Collection)value).isEmpty();
    }

    ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexTaskList(int begin, ImmutableList<? extends Task<? super E>> tasks) {
        int i = begin;
        ImmutableList.Builder builder = ImmutableList.builder();
        for (Task task : tasks) {
            builder.add((Object)Pair.of((Object)i++, (Object)task));
        }
        return builder.build();
    }

    public boolean isEmpty() {
        return this.memories.isEmpty() && this.sensors.isEmpty() && this.tasks.isEmpty();
    }

    public static final class Profile<E extends LivingEntity> {
        private final Collection<? extends MemoryModuleType<?>> memoryModules;
        private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensors;
        private final Codec<Brain<E>> codec;

        Profile(Collection<? extends MemoryModuleType<?>> memoryModules, Collection<? extends SensorType<? extends Sensor<? super E>>> sensors) {
            this.memoryModules = memoryModules;
            this.sensors = sensors;
            this.codec = Brain.createBrainCodec(memoryModules, sensors);
        }

        public Brain<E> deserialize(Dynamic<?> data) {
            return this.codec.parse(data).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElseGet(() -> new Brain(this.memoryModules, this.sensors, ImmutableList.of(), () -> this.codec));
        }
    }

    static final class MemoryEntry<U> {
        private final MemoryModuleType<U> type;
        private final Optional<? extends Memory<U>> data;

        static <U> MemoryEntry<U> of(MemoryModuleType<U> type, Optional<? extends Memory<?>> data) {
            return new MemoryEntry<U>(type, data);
        }

        MemoryEntry(MemoryModuleType<U> type, Optional<? extends Memory<U>> data) {
            this.type = type;
            this.data = data;
        }

        void apply(Brain<?> brain) {
            brain.setMemory(this.type, this.data);
        }

        public <T> void serialize(DynamicOps<T> ops, RecordBuilder<T> builder) {
            this.type.getCodec().ifPresent(codec -> this.data.ifPresent(data -> builder.add(Registries.MEMORY_MODULE_TYPE.getCodec().encodeStart(ops, this.type), codec.encodeStart(ops, data))));
        }
    }
}

