/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;

public interface ClientPacketListener
extends PacketListener {
    @Override
    default public NetworkSide getSide() {
        return NetworkSide.CLIENTBOUND;
    }
}

