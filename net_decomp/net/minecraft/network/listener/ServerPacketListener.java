/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;

public interface ServerPacketListener
extends PacketListener {
    @Override
    default public NetworkSide getSide() {
        return NetworkSide.SERVERBOUND;
    }
}

