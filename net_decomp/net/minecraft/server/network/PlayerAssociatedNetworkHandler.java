/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.network;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAssociatedNetworkHandler {
    public ServerPlayerEntity getPlayer();

    public void sendPacket(Packet<?> var1);
}

