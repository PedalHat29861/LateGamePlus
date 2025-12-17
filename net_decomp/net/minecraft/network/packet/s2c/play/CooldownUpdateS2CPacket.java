/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.Identifier;

public record CooldownUpdateS2CPacket(Identifier cooldownGroup, int cooldown) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, CooldownUpdateS2CPacket> CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, CooldownUpdateS2CPacket::cooldownGroup, PacketCodecs.VAR_INT, CooldownUpdateS2CPacket::cooldown, CooldownUpdateS2CPacket::new);

    @Override
    public PacketType<CooldownUpdateS2CPacket> getPacketType() {
        return PlayPackets.COOLDOWN;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onCooldownUpdate(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CooldownUpdateS2CPacket.class, "cooldownGroup;duration", "cooldownGroup", "cooldown"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CooldownUpdateS2CPacket.class, "cooldownGroup;duration", "cooldownGroup", "cooldown"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CooldownUpdateS2CPacket.class, "cooldownGroup;duration", "cooldownGroup", "cooldown"}, this, object);
    }
}

