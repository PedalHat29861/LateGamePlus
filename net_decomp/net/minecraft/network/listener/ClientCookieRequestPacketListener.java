/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;

public interface ClientCookieRequestPacketListener
extends ClientPacketListener {
    public void onCookieRequest(CookieRequestS2CPacket var1);
}

