/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record PlayerRotationS2CPacket(float yaw, boolean relativeYaw, float pitch, boolean relativePitch) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, PlayerRotationS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, PlayerRotationS2CPacket::yaw, PacketCodecs.BOOLEAN, PlayerRotationS2CPacket::relativeYaw, PacketCodecs.FLOAT, PlayerRotationS2CPacket::pitch, PacketCodecs.BOOLEAN, PlayerRotationS2CPacket::relativePitch, PlayerRotationS2CPacket::new);

    @Override
    public PacketType<PlayerRotationS2CPacket> getPacketType() {
        return PlayPackets.PLAYER_ROTATION;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onPlayerRotation(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlayerRotationS2CPacket.class, "yRot;relativeY;xRot;relativeX", "yaw", "relativeYaw", "pitch", "relativePitch"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlayerRotationS2CPacket.class, "yRot;relativeY;xRot;relativeX", "yaw", "relativeYaw", "pitch", "relativePitch"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlayerRotationS2CPacket.class, "yRot;relativeY;xRot;relativeX", "yaw", "relativeYaw", "pitch", "relativePitch"}, this, object);
    }
}

