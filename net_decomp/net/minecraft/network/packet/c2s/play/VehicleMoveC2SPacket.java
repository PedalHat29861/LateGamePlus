/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.Vec3d;

public record VehicleMoveC2SPacket(Vec3d position, float yaw, float pitch, boolean onGround) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, VehicleMoveC2SPacket> CODEC = PacketCodec.tuple(Vec3d.PACKET_CODEC, VehicleMoveC2SPacket::position, PacketCodecs.FLOAT, VehicleMoveC2SPacket::yaw, PacketCodecs.FLOAT, VehicleMoveC2SPacket::pitch, PacketCodecs.BOOLEAN, VehicleMoveC2SPacket::onGround, VehicleMoveC2SPacket::new);

    public static VehicleMoveC2SPacket fromVehicle(Entity vehicle) {
        if (vehicle.isInterpolating()) {
            return new VehicleMoveC2SPacket(vehicle.getInterpolator().getLerpedPos(), vehicle.getInterpolator().getLerpedYaw(), vehicle.getInterpolator().getLerpedPitch(), vehicle.isOnGround());
        }
        return new VehicleMoveC2SPacket(vehicle.getEntityPos(), vehicle.getYaw(), vehicle.getPitch(), vehicle.isOnGround());
    }

    @Override
    public PacketType<VehicleMoveC2SPacket> getPacketType() {
        return PlayPackets.MOVE_VEHICLE_C2S;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onVehicleMove(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{VehicleMoveC2SPacket.class, "position;yRot;xRot;onGround", "position", "yaw", "pitch", "onGround"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{VehicleMoveC2SPacket.class, "position;yRot;xRot;onGround", "position", "yaw", "pitch", "onGround"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{VehicleMoveC2SPacket.class, "position;yRot;xRot;onGround", "position", "yaw", "pitch", "onGround"}, this, object);
    }
}

