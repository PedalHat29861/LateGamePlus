/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import java.util.function.Consumer;
import net.minecraft.network.packet.Packet;

public interface ServerPlayerConfigurationTask {
    public void sendPacket(Consumer<Packet<?>> var1);

    default public boolean hasFinished() {
        return false;
    }

    public Key getKey();

    public record Key(String id) {
        @Override
        public String toString() {
            return this.id;
        }
    }
}

