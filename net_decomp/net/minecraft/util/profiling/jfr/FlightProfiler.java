/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import java.nio.file.Path;
import jdk.jfr.FlightRecorder;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.PacketType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.function.Finishable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiling.jfr.InstanceType;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.storage.ChunkCompressionFormat;
import net.minecraft.world.storage.StorageKey;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public interface FlightProfiler {
    public static final FlightProfiler INSTANCE = Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent() && FlightRecorder.isAvailable() ? JfrProfiler.getInstance() : new NoopProfiler();

    public boolean start(InstanceType var1);

    public Path stop();

    public boolean isProfiling();

    public boolean isAvailable();

    public void onTick(float var1);

    public void onClientFps(int var1);

    public void onPacketReceived(NetworkPhase var1, PacketType<?> var2, SocketAddress var3, int var4);

    public void onPacketSent(NetworkPhase var1, PacketType<?> var2, SocketAddress var3, int var4);

    public void onChunkRegionRead(StorageKey var1, ChunkPos var2, ChunkCompressionFormat var3, int var4);

    public void onChunkRegionWrite(StorageKey var1, ChunkPos var2, ChunkCompressionFormat var3, int var4);

    public @Nullable Finishable startWorldLoadProfiling();

    public @Nullable Finishable startChunkGenerationProfiling(ChunkPos var1, RegistryKey<World> var2, String var3);

    public @Nullable Finishable startStructureGenerationProfiling(ChunkPos var1, RegistryKey<World> var2, RegistryEntry<Structure> var3);

    public static class NoopProfiler
    implements FlightProfiler {
        private static final Logger LOGGER = LogUtils.getLogger();
        static final Finishable NOOP = success -> {};

        @Override
        public boolean start(InstanceType instanceType) {
            LOGGER.warn("Attempted to start Flight Recorder, but it's not supported on this JVM");
            return false;
        }

        @Override
        public Path stop() {
            throw new IllegalStateException("Attempted to stop Flight Recorder, but it's not supported on this JVM");
        }

        @Override
        public boolean isProfiling() {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void onPacketReceived(NetworkPhase state, PacketType<?> type, SocketAddress remoteAddress, int bytes) {
        }

        @Override
        public void onPacketSent(NetworkPhase state, PacketType<?> type, SocketAddress remoteAddress, int bytes) {
        }

        @Override
        public void onChunkRegionRead(StorageKey key, ChunkPos chunkPos, ChunkCompressionFormat format, int bytes) {
        }

        @Override
        public void onChunkRegionWrite(StorageKey key, ChunkPos chunkPos, ChunkCompressionFormat format, int bytes) {
        }

        @Override
        public void onTick(float tickTime) {
        }

        @Override
        public void onClientFps(int fps) {
        }

        @Override
        public Finishable startWorldLoadProfiling() {
            return NOOP;
        }

        @Override
        public @Nullable Finishable startChunkGenerationProfiling(ChunkPos chunkPos, RegistryKey<World> world, String targetStatus) {
            return null;
        }

        @Override
        public Finishable startStructureGenerationProfiling(ChunkPos chunkPos, RegistryKey<World> world, RegistryEntry<Structure> structure) {
            return NOOP;
        }
    }
}

