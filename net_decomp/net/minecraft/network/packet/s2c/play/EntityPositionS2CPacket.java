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

public record EntityPositionS2CPacket(int entityId, EntityPosition change, Set<PositionFlag> relatives, boolean onGround) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, EntityPositionS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, EntityPositionS2CPacket::entityId, EntityPosition.PACKET_CODEC, EntityPositionS2CPacket::change, PositionFlag.PACKET_CODEC, EntityPositionS2CPacket::relatives, PacketCodecs.BOOLEAN, EntityPositionS2CPacket::onGround, EntityPositionS2CPacket::new);

    public static EntityPositionS2CPacket create(int entityId, EntityPosition change, Set<PositionFlag> relatives, boolean onGround) {
        return new EntityPositionS2CPacket(entityId, change, relatives, onGround);
    }

    @Override
    public PacketType<EntityPositionS2CPacket> getPacketType() {
        return PlayPackets.TELEPORT_ENTITY;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onEntityPosition(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityPositionS2CPacket.class, "id;change;relatives;onGround", "entityId", "change", "relatives", "onGround"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityPositionS2CPacket.class, "id;change;relatives;onGround", "entityId", "change", "relatives", "onGround"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityPositionS2CPacket.class, "id;change;relatives;onGround", "entityId", "change", "relatives", "onGround"}, this, object);
    }
}

