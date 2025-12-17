/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;

public interface ClientPingResultPacketListener
extends PacketListener {
    public void onPingResult(PingResultS2CPacket var1);
}

