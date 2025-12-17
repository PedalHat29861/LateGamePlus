/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.sample;

import jdk.jfr.consumer.RecordedEvent;

public record ClientFpsSample(int fps) {
    public static ClientFpsSample fromEvent(RecordedEvent event, String key) {
        return new ClientFpsSample(event.getInt(key));
    }
}

