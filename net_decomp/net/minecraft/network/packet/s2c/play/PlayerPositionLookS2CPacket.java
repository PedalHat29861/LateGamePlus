/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Set;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.PositionFlag;

public record PlayerPositionLookS2CPacket(int teleportId, EntityPosition change, Set<PositionFlag> relatives) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, PlayerPositionLookS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, PlayerPositionLookS2CPacket::teleportId, EntityPosition.PACKET_CODEC, PlayerPositionLookS2CPacket::change, PositionFlag.PACKET_CODEC, PlayerPositionLookS2CPacket::relatives, PlayerPositionLookS2CPacket::new);

    public static PlayerPositionLookS2CPacket of(int teleportId, EntityPosition pos, Set<PositionFlag> flags) {
        return new PlayerPositionLookS2CPacket(teleportId, pos, flags);
    }

    @Override
    public PacketType<PlayerPositionLookS2CPacket> getPacketType() {
        return PlayPackets.PLAYER_POSITION;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onPlayerPositionLook(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlayerPositionLookS2CPacket.class, "id;change;relatives", "teleportId", "change", "relatives"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlayerPositionLookS2CPacket.class, "id;change;relatives", "teleportId", "change", "relatives"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlayerPositionLookS2CPacket.class, "id;change;relatives", "teleportId", "change", "relatives"}, this, object);
    }
}

