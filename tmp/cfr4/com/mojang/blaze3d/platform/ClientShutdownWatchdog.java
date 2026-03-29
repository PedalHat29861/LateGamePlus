/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.CrashReport
 *  net.minecraft.server.dedicated.ServerWatchdog
 */
package com.mojang.blaze3d.platform;

import java.io.File;
import java.time.Duration;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.server.dedicated.ServerWatchdog;

public class ClientShutdownWatchdog {
    private static final Duration CRASH_REPORT_PRELOAD_LOAD = Duration.ofSeconds(15L);

    public static void startShutdownWatchdog(Minecraft minecraft, File gameDirectory, long mainThreadId) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(CRASH_REPORT_PRELOAD_LOAD);
            }
            catch (InterruptedException e) {
                return;
            }
            CrashReport report = ServerWatchdog.createWatchdogCrashReport((String)"Client shutdown", (long)mainThreadId);
            minecraft.fillReport(report);
            Minecraft.saveReport(gameDirectory, report);
        });
        thread.setDaemon(true);
        thread.setName("Client shutdown watchdog");
        thread.start();
    }
}

