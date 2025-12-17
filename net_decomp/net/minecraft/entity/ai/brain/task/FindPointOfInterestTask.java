/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableLong
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jspecify.annotations.Nullable;

public class FindPointOfInterestTask {
    public static final int POI_SORTING_RADIUS = 48;

    public static Task<PathAwareEntity> create(Predicate<RegistryEntry<PointOfInterestType>> poiPredicate, MemoryModuleType<GlobalPos> poiPosModule, boolean onlyRunIfChild, Optional<Byte> entityStatus, BiPredicate<ServerWorld, BlockPos> worldPosBiPredicate) {
        return FindPointOfInterestTask.create(poiPredicate, poiPosModule, poiPosModule, onlyRunIfChild, entityStatus, worldPosBiPredicate);
    }

    public static Task<PathAwareEntity> create(Predicate<RegistryEntry<PointOfInterestType>> poiPredicate, MemoryModuleType<GlobalPos> poiPosModule, boolean onlyRunIfChild, Optional<Byte> entityStatus) {
        return FindPointOfInterestTask.create(poiPredicate, poiPosModule, poiPosModule, onlyRunIfChild, entityStatus, (world, pos) -> true);
    }

    public static Task<PathAwareEntity> create(Predicate<RegistryEntry<PointOfInterestType>> poiPredicate, MemoryModuleType<GlobalPos> poiPosModule, MemoryModuleType<GlobalPos> potentialPoiPosModule, boolean onlyRunIfChild, Optional<Byte> entityStatus, BiPredicate<ServerWorld, BlockPos> worldPosBiPredicate) {
        int i = 5;
        int j = 20;
        MutableLong mutableLong = new MutableLong(0L);
        Long2ObjectOpenHashMap long2ObjectMap = new Long2ObjectOpenHashMap();
        SingleTickTask<PathAwareEntity> singleTickTask = TaskTriggerer.task(arg_0 -> FindPointOfInterestTask.method_46879(potentialPoiPosModule, onlyRunIfChild, mutableLong, (Long2ObjectMap)long2ObjectMap, poiPredicate, worldPosBiPredicate, entityStatus, arg_0));
        if (potentialPoiPosModule == poiPosModule) {
            return singleTickTask;
        }
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(poiPosModule)).apply((Applicative)context, poiPos -> singleTickTask));
    }

    public static @Nullable Path findPathToPoi(MobEntity entity, Set<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> pois) {
        if (pois.isEmpty()) {
            return null;
        }
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        int i = 1;
        for (Pair<RegistryEntry<PointOfInterestType>, BlockPos> pair : pois) {
            i = Math.max(i, ((PointOfInterestType)((RegistryEntry)pair.getFirst()).value()).searchDistance());
            set.add((BlockPos)pair.getSecond());
        }
        return entity.getNavigation().findPathTo(set, i);
    }

    private static /* synthetic */ App method_46879(MemoryModuleType context, boolean bl, MutableLong mutableLong, Long2ObjectMap long2ObjectMap, Predicate predicate, BiPredicate biPredicate, Optional optional, TaskTriggerer.TaskContext taskContext) {
        return taskContext.group(taskContext.queryMemoryAbsent(context)).apply((Applicative)taskContext, queryResult -> (world, entity, time) -> {
            if (bl && entity.isBaby()) {
                return false;
            }
            if (mutableLong.longValue() == 0L) {
                mutableLong.setValue(world.getTime() + (long)world.random.nextInt(20));
                return false;
            }
            if (world.getTime() < mutableLong.longValue()) {
                return false;
            }
            mutableLong.setValue(time + 20L + (long)world.getRandom().nextInt(20));
            PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
            long2ObjectMap.long2ObjectEntrySet().removeIf(entry -> !((RetryMarker)entry.getValue()).isAttempting(time));
            Predicate<BlockPos> predicate2 = pos -> {
                RetryMarker retryMarker = (RetryMarker)long2ObjectMap.get(pos.asLong());
                if (retryMarker == null) {
                    return true;
                }
                if (!retryMarker.shouldRetry(time)) {
                    return false;
                }
                retryMarker.setAttemptTime(time);
                return true;
            };
            Set<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> set = pointOfInterestStorage.getSortedTypesAndPositions(predicate, predicate2, entity.getBlockPos(), 48, PointOfInterestStorage.OccupationStatus.HAS_SPACE).limit(5L).filter(pair -> biPredicate.test(world, (BlockPos)pair.getSecond())).collect(Collectors.toSet());
            Path path = FindPointOfInterestTask.findPathToPoi(entity, set);
            if (path != null && path.reachesTarget()) {
                BlockPos blockPos = path.getTarget();
                pointOfInterestStorage.getType(blockPos).ifPresent(poiType -> {
                    pointOfInterestStorage.getPosition(predicate, (registryEntry, blockPos2) -> blockPos2.equals(blockPos), blockPos, 1);
                    queryResult.remember(GlobalPos.create(world.getRegistryKey(), blockPos));
                    optional.ifPresent(status -> world.sendEntityStatus(entity, (byte)status));
                    long2ObjectMap.clear();
                    world.getSubscriptionTracker().onPoiUpdated(blockPos);
                });
            } else {
                for (Pair<RegistryEntry<PointOfInterestType>, BlockPos> pair2 : set) {
                    long2ObjectMap.computeIfAbsent(((BlockPos)pair2.getSecond()).asLong(), m -> new RetryMarker(serverWorld.random, time));
                }
            }
            return true;
        });
    }

    static class RetryMarker {
        private static final int MIN_DELAY = 40;
        private static final int MAX_EXTRA_DELAY = 80;
        private static final int ATTEMPT_DURATION = 400;
        private final Random random;
        private long previousAttemptAt;
        private long nextScheduledAttemptAt;
        private int currentDelay;

        RetryMarker(Random random, long time) {
            this.random = random;
            this.setAttemptTime(time);
        }

        public void setAttemptTime(long time) {
            this.previousAttemptAt = time;
            int i = this.currentDelay + this.random.nextInt(40) + 40;
            this.currentDelay = Math.min(i, 400);
            this.nextScheduledAttemptAt = time + (long)this.currentDelay;
        }

        public boolean isAttempting(long time) {
            return time - this.previousAttemptAt < 400L;
        }

        public boolean shouldRetry(long time) {
            return time >= this.nextScheduledAttemptAt;
        }

        public String toString() {
            return "RetryMarker{, previousAttemptAt=" + this.previousAttemptAt + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptAt + ", currentDelay=" + this.currentDelay + "}";
        }
    }
}

