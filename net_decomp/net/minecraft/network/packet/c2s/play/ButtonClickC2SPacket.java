/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record ButtonClickC2SPacket(int syncId, int buttonId) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ButtonClickC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.SYNC_ID, ButtonClickC2SPacket::syncId, PacketCodecs.VAR_INT, ButtonClickC2SPacket::buttonId, ButtonClickC2SPacket::new);

    @Override
    public PacketType<ButtonClickC2SPacket> getPacketType() {
        return PlayPackets.CONTAINER_BUTTON_CLICK;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onButtonClick(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ButtonClickC2SPacket.class, "containerId;buttonId", "syncId", "buttonId"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ButtonClickC2SPacket.class, "containerId;buttonId", "syncId", "buttonId"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ButtonClickC2SPacket.class, "containerId;buttonId", "syncId", "buttonId"}, this, object);
    }
}

