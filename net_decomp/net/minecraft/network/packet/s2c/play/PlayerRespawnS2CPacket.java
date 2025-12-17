/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;

public record PlayerRespawnS2CPacket(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte flag) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, PlayerRespawnS2CPacket> CODEC = Packet.createCodec(PlayerRespawnS2CPacket::write, PlayerRespawnS2CPacket::new);
    public static final byte KEEP_ATTRIBUTES = 1;
    public static final byte KEEP_TRACKED_DATA = 2;
    public static final byte KEEP_ALL = 3;

    private PlayerRespawnS2CPacket(RegistryByteBuf registryByteBuf) {
        this(new CommonPlayerSpawnInfo(registryByteBuf), registryByteBuf.readByte());
    }

    private void write(RegistryByteBuf registryByteBuf) {
        this.commonPlayerSpawnInfo.write(registryByteBuf);
        registryByteBuf.writeByte(this.flag);
    }

    @Override
    public PacketType<PlayerRespawnS2CPacket> getPacketType() {
        return PlayPackets.RESPAWN;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onPlayerRespawn(this);
    }

    public boolean hasFlag(byte flag) {
        return (this.flag & flag) != 0;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlayerRespawnS2CPacket.class, "commonPlayerSpawnInfo;dataToKeep", "commonPlayerSpawnInfo", "flag"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlayerRespawnS2CPacket.class, "commonPlayerSpawnInfo;dataToKeep", "commonPlayerSpawnInfo", "flag"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlayerRespawnS2CPacket.class, "commonPlayerSpawnInfo;dataToKeep", "commonPlayerSpawnInfo", "flag"}, this, object);
    }
}

