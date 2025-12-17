/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.timer.stopwatch;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.timer.stopwatch.Stopwatch;
import org.jspecify.annotations.Nullable;

public class StopwatchPersistentState
extends PersistentState {
    private static final Codec<StopwatchPersistentState> CODEC = Codec.unboundedMap(Identifier.CODEC, (Codec)Codec.LONG).fieldOf("stopwatches").codec().xmap(StopwatchPersistentState::fromElapsedTimes, StopwatchPersistentState::toElapsedTimes);
    public static final PersistentStateType<StopwatchPersistentState> STATE_TYPE = new PersistentStateType<StopwatchPersistentState>("stopwatches", StopwatchPersistentState::new, CODEC, DataFixTypes.SAVED_DATA_STOPWATCHES);
    private final Map<Identifier, Stopwatch> stopwatches = new Object2ObjectOpenHashMap();

    private StopwatchPersistentState() {
    }

    private static StopwatchPersistentState fromElapsedTimes(Map<Identifier, Long> times) {
        StopwatchPersistentState stopwatchPersistentState = new StopwatchPersistentState();
        long l = StopwatchPersistentState.getTimeMs();
        times.forEach((id, time) -> stopwatchPersistentState.stopwatches.put((Identifier)id, new Stopwatch(l, (long)time)));
        return stopwatchPersistentState;
    }

    private Map<Identifier, Long> toElapsedTimes() {
        long l = StopwatchPersistentState.getTimeMs();
        TreeMap<Identifier, Long> map = new TreeMap<Identifier, Long>();
        this.stopwatches.forEach((id, stopwatch) -> map.put((Identifier)id, stopwatch.getElapsedTimeMs(l)));
        return map;
    }

    public @Nullable Stopwatch get(Identifier id) {
        return this.stopwatches.get(id);
    }

    public boolean add(Identifier id, Stopwatch stopwatch) {
        if (this.stopwatches.putIfAbsent(id, stopwatch) == null) {
            this.markDirty();
            return true;
        }
        return false;
    }

    public boolean update(Identifier id, UnaryOperator<Stopwatch> f) {
        if (this.stopwatches.computeIfPresent(id, (id_, stopwatch) -> (Stopwatch)f.apply((Stopwatch)stopwatch)) != null) {
            this.markDirty();
            return true;
        }
        return false;
    }

    public boolean remove(Identifier id) {
        boolean bl;
        boolean bl2 = bl = this.stopwatches.remove(id) != null;
        if (bl) {
            this.markDirty();
        }
        return bl;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || !this.stopwatches.isEmpty();
    }

    public List<Identifier> keys() {
        return List.copyOf(this.stopwatches.keySet());
    }

    public static long getTimeMs() {
        return Util.getMeasuringTimeMs();
    }
}

