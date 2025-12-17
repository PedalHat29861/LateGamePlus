/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketApplyBatcher;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NetworkThreadUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, ServerWorld world) throws OffThreadException {
        NetworkThreadUtils.forceMainThread(packet, listener, world.getServer().getPacketApplyBatcher());
    }

    public static <T extends PacketListener> void forceMainThread(Packet<T> packet, T listener, PacketApplyBatcher batcher) throws OffThreadException {
        if (!batcher.isOnThread()) {
            batcher.add(listener, packet);
            throw OffThreadException.INSTANCE;
        }
    }

    public static <T extends PacketListener> CrashException createCrashException(Exception exception, Packet<T> packet, T listener) {
        if (exception instanceof CrashException) {
            CrashException crashException = (CrashException)exception;
            NetworkThreadUtils.fillCrashReport(crashException.getReport(), listener, packet);
            return crashException;
        }
        CrashReport crashReport = CrashReport.create(exception, "Main thread packet handler");
        NetworkThreadUtils.fillCrashReport(crashReport, listener, packet);
        return new CrashException(crashReport);
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport report, T listener, @Nullable Packet<T> packet) {
        if (packet != null) {
            CrashReportSection crashReportSection = report.addElement("Incoming Packet");
            crashReportSection.add("Type", () -> packet.getPacketType().toString());
            crashReportSection.add("Is Terminal", () -> Boolean.toString(packet.transitionsNetworkState()));
            crashReportSection.add("Is Skippable", () -> Boolean.toString(packet.isWritingErrorSkippable()));
        }
        listener.fillCrashReport(report);
    }
}

