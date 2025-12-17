/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler.log;

import net.minecraft.network.packet.s2c.play.DebugSampleS2CPacket;
import net.minecraft.server.debug.SubscriberTracker;
import net.minecraft.util.profiler.log.ArrayDebugSampleLog;
import net.minecraft.util.profiler.log.DebugSampleType;

public class SubscribableDebugSampleLog
extends ArrayDebugSampleLog {
    private final SubscriberTracker tracker;
    private final DebugSampleType type;

    public SubscribableDebugSampleLog(int size, SubscriberTracker tracker, DebugSampleType type) {
        this(size, tracker, type, new long[size]);
    }

    public SubscribableDebugSampleLog(int size, SubscriberTracker tracker, DebugSampleType type, long[] defaults) {
        super(size, defaults);
        this.tracker = tracker;
        this.type = type;
    }

    @Override
    protected void onPush() {
        if (this.tracker.hasSubscriber(this.type.getSubscriptionType())) {
            this.tracker.send(this.type.getSubscriptionType(), new DebugSampleS2CPacket((long[])this.values.clone(), this.type));
        }
    }
}

