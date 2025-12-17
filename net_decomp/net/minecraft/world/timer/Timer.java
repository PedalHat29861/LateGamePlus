/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Table
 *  com.google.common.primitives.UnsignedLong
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.world.timer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.timer.TimerCallback;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.slf4j.Logger;

public class Timer<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CALLBACK_KEY = "Callback";
    private static final String NAME_KEY = "Name";
    private static final String TRIGGER_TIME_KEY = "TriggerTime";
    private final TimerCallbackSerializer<T> callback;
    private final Queue<Event<T>> events = new PriorityQueue<Event<T>>(Timer.createEventComparator());
    private UnsignedLong eventCounter = UnsignedLong.ZERO;
    private final Table<String, Long, Event<T>> eventsByName = HashBasedTable.create();

    private static <T> Comparator<Event<T>> createEventComparator() {
        return Comparator.comparingLong(event -> event.triggerTime).thenComparing(event -> event.id);
    }

    public Timer(TimerCallbackSerializer<T> timerCallbackSerializer, Stream<? extends Dynamic<?>> nbts) {
        this(timerCallbackSerializer);
        this.events.clear();
        this.eventsByName.clear();
        this.eventCounter = UnsignedLong.ZERO;
        nbts.forEach(nbt -> {
            NbtElement nbtElement = (NbtElement)nbt.convert((DynamicOps)NbtOps.INSTANCE).getValue();
            if (nbtElement instanceof NbtCompound) {
                NbtCompound nbtCompound = (NbtCompound)nbtElement;
                this.addEvent(nbtCompound);
            } else {
                LOGGER.warn("Invalid format of events: {}", (Object)nbtElement);
            }
        });
    }

    public Timer(TimerCallbackSerializer<T> timerCallbackSerializer) {
        this.callback = timerCallbackSerializer;
    }

    public void processEvents(T server, long time) {
        Event<T> event;
        while ((event = this.events.peek()) != null && event.triggerTime <= time) {
            this.events.remove();
            this.eventsByName.remove((Object)event.name, (Object)time);
            event.callback.call(server, this, time);
        }
    }

    public void setEventIfAbsent(String name, long triggerTime, TimerCallback<T> callback) {
        if (this.eventsByName.contains((Object)name, (Object)triggerTime)) {
            return;
        }
        this.eventCounter = this.eventCounter.plus(UnsignedLong.ONE);
        Event<T> event = new Event<T>(triggerTime, this.eventCounter, name, callback);
        this.eventsByName.put((Object)name, (Object)triggerTime, event);
        this.events.add(event);
    }

    public int remove(String name) {
        Collection collection = this.eventsByName.row((Object)name).values();
        collection.forEach(this.events::remove);
        int i = collection.size();
        collection.clear();
        return i;
    }

    public Set<String> getEventNames() {
        return Collections.unmodifiableSet(this.eventsByName.rowKeySet());
    }

    private void addEvent(NbtCompound nbt) {
        TimerCallback timerCallback = nbt.get(CALLBACK_KEY, this.callback.getCodec()).orElse(null);
        if (timerCallback != null) {
            String string = nbt.getString(NAME_KEY, "");
            long l = nbt.getLong(TRIGGER_TIME_KEY, 0L);
            this.setEventIfAbsent(string, l, timerCallback);
        }
    }

    private NbtCompound serialize(Event<T> event) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString(NAME_KEY, event.name);
        nbtCompound.putLong(TRIGGER_TIME_KEY, event.triggerTime);
        nbtCompound.put(CALLBACK_KEY, this.callback.getCodec(), event.callback);
        return nbtCompound;
    }

    public NbtList toNbt() {
        NbtList nbtList = new NbtList();
        this.events.stream().sorted(Timer.createEventComparator()).map(this::serialize).forEach(nbtList::add);
        return nbtList;
    }

    public static class Event<T> {
        public final long triggerTime;
        public final UnsignedLong id;
        public final String name;
        public final TimerCallback<T> callback;

        Event(long triggerTime, UnsignedLong id, String name, TimerCallback<T> callback) {
            this.triggerTime = triggerTime;
            this.id = id;
            this.name = name;
            this.callback = callback;
        }
    }
}

