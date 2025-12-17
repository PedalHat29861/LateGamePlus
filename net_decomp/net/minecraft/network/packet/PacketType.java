/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public record PacketType<T extends Packet<?>>(NetworkSide side, Identifier id) {
    @Override
    public String toString() {
        return this.side.getName() + "/" + String.valueOf(this.id);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PacketType.class, "flow;id", "side", "id"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PacketType.class, "flow;id", "side", "id"}, this, object);
    }
}

