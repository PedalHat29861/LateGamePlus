/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;

public interface ServerCookieResponsePacketListener
extends ServerCrashSafePacketListener {
    public void onCookieResponse(CookieResponseC2SPacket var1);
}

