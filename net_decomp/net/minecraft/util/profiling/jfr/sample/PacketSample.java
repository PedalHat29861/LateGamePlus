/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.sample;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import jdk.jfr.consumer.RecordedEvent;

public record PacketSample(String side, String protocolId, String packetId) {
    public static PacketSample fromEvent(RecordedEvent event) {
        return new PacketSample(event.getString("packetDirection"), event.getString("protocolId"), event.getString("packetId"));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PacketSample.class, "direction;protocolId;packetId", "side", "protocolId", "packetId"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PacketSample.class, "direction;protocolId;packetId", "side", "protocolId", "packetId"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PacketSample.class, "direction;protocolId;packetId", "side", "protocolId", "packetId"}, this, object);
    }
}

