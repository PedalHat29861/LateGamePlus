/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.world.Difficulty;

public record DifficultyS2CPacket(Difficulty difficulty, boolean difficultyLocked) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, DifficultyS2CPacket> CODEC = PacketCodec.tuple(Difficulty.PACKET_CODEC, DifficultyS2CPacket::difficulty, PacketCodecs.BOOLEAN, DifficultyS2CPacket::difficultyLocked, DifficultyS2CPacket::new);

    @Override
    public PacketType<DifficultyS2CPacket> getPacketType() {
        return PlayPackets.CHANGE_DIFFICULTY_S2C;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onDifficulty(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{DifficultyS2CPacket.class, "difficulty;locked", "difficulty", "difficultyLocked"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DifficultyS2CPacket.class, "difficulty;locked", "difficulty", "difficultyLocked"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DifficultyS2CPacket.class, "difficulty;locked", "difficulty", "difficultyLocked"}, this, object);
    }
}

