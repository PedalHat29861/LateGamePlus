/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record WorldTimeUpdateS2CPacket(long time, long timeOfDay, boolean tickDayTime) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, WorldTimeUpdateS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.LONG, WorldTimeUpdateS2CPacket::time, PacketCodecs.LONG, WorldTimeUpdateS2CPacket::timeOfDay, PacketCodecs.BOOLEAN, WorldTimeUpdateS2CPacket::tickDayTime, WorldTimeUpdateS2CPacket::new);

    @Override
    public PacketType<WorldTimeUpdateS2CPacket> getPacketType() {
        return PlayPackets.SET_TIME;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onWorldTimeUpdate(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{WorldTimeUpdateS2CPacket.class, "gameTime;dayTime;tickDayTime", "time", "timeOfDay", "tickDayTime"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{WorldTimeUpdateS2CPacket.class, "gameTime;dayTime;tickDayTime", "time", "timeOfDay", "tickDayTime"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{WorldTimeUpdateS2CPacket.class, "gameTime;dayTime;tickDayTime", "time", "timeOfDay", "tickDayTime"}, this, object);
    }
}

