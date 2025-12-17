/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.Plot
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiler;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.SampleType;
import org.slf4j.Logger;

public class TracyProfiler
implements Profiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 5);
    private final List<Zone> zones = new ArrayList<Zone>();
    private final Map<String, Marker> markers = new HashMap<String, Marker>();
    private final String threadName = Thread.currentThread().getName();

    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
        for (Marker marker : this.markers.values()) {
            marker.setCount(0);
        }
    }

    @Override
    public void push(String location) {
        Optional optional;
        String string = "";
        String string2 = "";
        int i = 0;
        if (SharedConstants.isDevelopment && (optional = STACK_WALKER.walk(stream -> stream.filter(frame -> frame.getDeclaringClass() != TracyProfiler.class && frame.getDeclaringClass() != Profiler.UnionProfiler.class).findFirst())).isPresent()) {
            StackWalker.StackFrame stackFrame = (StackWalker.StackFrame)optional.get();
            string = stackFrame.getMethodName();
            string2 = stackFrame.getFileName();
            i = stackFrame.getLineNumber();
        }
        Zone zone = TracyClient.beginZone((String)location, (String)string, (String)string2, (int)i);
        this.zones.add(zone);
    }

    @Override
    public void push(Supplier<String> locationGetter) {
        this.push(locationGetter.get());
    }

    @Override
    public void pop() {
        if (this.zones.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        Zone zone = this.zones.removeLast();
        zone.close();
    }

    @Override
    public void swap(String location) {
        this.pop();
        this.push(location);
    }

    @Override
    public void swap(Supplier<String> locationGetter) {
        this.pop();
        this.push(locationGetter.get());
    }

    @Override
    public void markSampleType(SampleType type) {
    }

    @Override
    public void visit(String marker, int num) {
        this.markers.computeIfAbsent(marker, markerName -> new Marker(this.threadName + " " + marker)).increment(num);
    }

    @Override
    public void visit(Supplier<String> markerGetter, int num) {
        this.visit(markerGetter.get(), num);
    }

    private Zone getCurrentZone() {
        return this.zones.getLast();
    }

    @Override
    public void addZoneText(String label) {
        this.getCurrentZone().addText(label);
    }

    @Override
    public void addZoneValue(long value) {
        this.getCurrentZone().addValue(value);
    }

    @Override
    public void setZoneColor(int color) {
        this.getCurrentZone().setColor(color);
    }

    static final class Marker {
        private final Plot plot;
        private int count;

        Marker(String name) {
            this.plot = TracyClient.createPlot((String)name);
            this.count = 0;
        }

        void setCount(int count) {
            this.count = count;
            this.plot.setValue((double)count);
        }

        void increment(int count) {
            this.setCount(this.count + count);
        }
    }
}

