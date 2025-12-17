/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 */
package net.minecraft.util.crash;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Queue;
import net.minecraft.util.collection.ArrayListDeque;

public class SuppressedExceptionsTracker {
    private static final int MAX_QUEUE_SIZE = 8;
    private final Queue<Entry> queue = new ArrayListDeque<Entry>();
    private final Object2IntLinkedOpenHashMap<Key> keyToCount = new Object2IntLinkedOpenHashMap();

    private static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public synchronized void onSuppressedException(String location, Throwable exception) {
        long l = SuppressedExceptionsTracker.currentTimeMillis();
        String string = exception.getMessage();
        this.queue.add(new Entry(l, location, exception.getClass(), string));
        while (this.queue.size() > 8) {
            this.queue.remove();
        }
        Key key = new Key(location, exception.getClass());
        int i = this.keyToCount.getInt((Object)key);
        this.keyToCount.putAndMoveToFirst((Object)key, i + 1);
    }

    public synchronized String collect() {
        long l = SuppressedExceptionsTracker.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        if (!this.queue.isEmpty()) {
            stringBuilder.append("\n\t\tLatest entries:\n");
            for (Entry entry : this.queue) {
                stringBuilder.append("\t\t\t").append(entry.location).append(":").append(entry.cls).append(": ").append(entry.message).append(" (").append(l - entry.timestampMs).append("ms ago)").append("\n");
            }
        }
        if (!this.keyToCount.isEmpty()) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append("\t\tEntry counts:\n");
            for (Object2IntMap.Entry entry2 : Object2IntMaps.fastIterable(this.keyToCount)) {
                stringBuilder.append("\t\t\t").append(((Key)entry2.getKey()).location).append(":").append(((Key)entry2.getKey()).cls).append(" x ").append(entry2.getIntValue()).append("\n");
            }
        }
        if (stringBuilder.isEmpty()) {
            return "~~NONE~~";
        }
        return stringBuilder.toString();
    }

    static final class Entry
    extends Record {
        final long timestampMs;
        final String location;
        final Class<? extends Throwable> cls;
        final String message;

        Entry(long timestampMs, String location, Class<? extends Throwable> cls, String message) {
            this.timestampMs = timestampMs;
            this.location = location;
            this.cls = cls;
            this.message = message;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this, object);
        }

        public long timestampMs() {
            return this.timestampMs;
        }

        public String location() {
            return this.location;
        }

        public Class<? extends Throwable> cls() {
            return this.cls;
        }

        public String message() {
            return this.message;
        }
    }

    static final class Key
    extends Record {
        final String location;
        final Class<? extends Throwable> cls;

        Key(String location, Class<? extends Throwable> cls) {
            this.location = location;
            this.cls = cls;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Key.class, "location;cls", "location", "cls"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Key.class, "location;cls", "location", "cls"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Key.class, "location;cls", "location", "cls"}, this, object);
        }

        public String location() {
            return this.location;
        }

        public Class<? extends Throwable> cls() {
            return this.cls;
        }
    }
}

