/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network.listener;

import com.mojang.logging.LogUtils;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.crash.CrashException;
import org.slf4j.Logger;

public interface ServerCrashSafePacketListener
extends ServerPacketListener {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    default public void onPacketException(Packet packet, Exception exception) throws CrashException {
        LOGGER.error("Failed to handle packet {}, suppressing error", (Object)packet, (Object)exception);
    }
}

