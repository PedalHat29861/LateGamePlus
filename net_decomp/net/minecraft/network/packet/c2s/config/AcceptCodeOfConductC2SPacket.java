/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.c2s.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.packet.ConfigPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record AcceptCodeOfConductC2SPacket() implements Packet<ServerConfigurationPacketListener>
{
    public static final AcceptCodeOfConductC2SPacket INSTANCE = new AcceptCodeOfConductC2SPacket();
    public static final PacketCodec<ByteBuf, AcceptCodeOfConductC2SPacket> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public PacketType<AcceptCodeOfConductC2SPacket> getPacketType() {
        return ConfigPackets.ACCEPT_CODE_OF_CONDUCT;
    }

    @Override
    public void apply(ServerConfigurationPacketListener serverConfigurationPacketListener) {
        serverConfigurationPacketListener.onAcceptCodeOfConduct(this);
    }
}

