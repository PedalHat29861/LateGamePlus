/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;

public interface ServerQueryPingPacketListener
extends PacketListener {
    public void onQueryPing(QueryPingC2SPacket var1);
}

