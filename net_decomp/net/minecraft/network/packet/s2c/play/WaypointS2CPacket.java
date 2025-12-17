/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.waypoint.TrackedWaypoint;
import net.minecraft.world.waypoint.TrackedWaypointHandler;
import net.minecraft.world.waypoint.Waypoint;
import net.minecraft.world.waypoint.WaypointHandler;

public record WaypointS2CPacket(Operation operation, TrackedWaypoint waypoint) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, WaypointS2CPacket> CODEC = PacketCodec.tuple(Operation.PACKET_CODEC, WaypointS2CPacket::operation, TrackedWaypoint.PACKET_CODEC, WaypointS2CPacket::waypoint, WaypointS2CPacket::new);

    public static WaypointS2CPacket untrack(UUID source) {
        return new WaypointS2CPacket(Operation.UNTRACK, TrackedWaypoint.empty(source));
    }

    public static WaypointS2CPacket trackPos(UUID source, Waypoint.Config config, Vec3i pos) {
        return new WaypointS2CPacket(Operation.TRACK, TrackedWaypoint.ofPos(source, config, pos));
    }

    public static WaypointS2CPacket updatePos(UUID source, Waypoint.Config config, Vec3i pos) {
        return new WaypointS2CPacket(Operation.UPDATE, TrackedWaypoint.ofPos(source, config, pos));
    }

    public static WaypointS2CPacket trackChunk(UUID source, Waypoint.Config config, ChunkPos chunkPos) {
        return new WaypointS2CPacket(Operation.TRACK, TrackedWaypoint.ofChunk(source, config, chunkPos));
    }

    public static WaypointS2CPacket updateChunk(UUID source, Waypoint.Config config, ChunkPos chunkPos) {
        return new WaypointS2CPacket(Operation.UPDATE, TrackedWaypoint.ofChunk(source, config, chunkPos));
    }

    public static WaypointS2CPacket trackAzimuth(UUID source, Waypoint.Config config, float azimuth) {
        return new WaypointS2CPacket(Operation.TRACK, TrackedWaypoint.ofAzimuth(source, config, azimuth));
    }

    public static WaypointS2CPacket updateAzimuth(UUID source, Waypoint.Config config, float azimuth) {
        return new WaypointS2CPacket(Operation.UPDATE, TrackedWaypoint.ofAzimuth(source, config, azimuth));
    }

    @Override
    public PacketType<WaypointS2CPacket> getPacketType() {
        return PlayPackets.WAYPOINT;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onWaypoint(this);
    }

    @Override
    public void apply(TrackedWaypointHandler handler) {
        this.operation.handler.accept(handler, this.waypoint);
    }

    static final class Operation
    extends Enum<Operation> {
        public static final /* enum */ Operation TRACK = new Operation(WaypointHandler::onTrack);
        public static final /* enum */ Operation UNTRACK = new Operation(WaypointHandler::onUntrack);
        public static final /* enum */ Operation UPDATE = new Operation(WaypointHandler::onUpdate);
        final BiConsumer<TrackedWaypointHandler, TrackedWaypoint> handler;
        public static final IntFunction<Operation> BY_INDEX;
        public static final PacketCodec<ByteBuf, Operation> PACKET_CODEC;
        private static final /* synthetic */ Operation[] field_59619;

        public static Operation[] values() {
            return (Operation[])field_59619.clone();
        }

        public static Operation valueOf(String string) {
            return Enum.valueOf(Operation.class, string);
        }

        private Operation(BiConsumer<TrackedWaypointHandler, TrackedWaypoint> handler) {
            this.handler = handler;
        }

        private static /* synthetic */ Operation[] method_70591() {
            return new Operation[]{TRACK, UNTRACK, UPDATE};
        }

        static {
            field_59619 = Operation.method_70591();
            BY_INDEX = ValueLists.createIndexToValueFunction(Enum::ordinal, Operation.values(), ValueLists.OutOfBoundsHandling.WRAP);
            PACKET_CODEC = PacketCodecs.indexed(BY_INDEX, Enum::ordinal);
        }
    }
}

