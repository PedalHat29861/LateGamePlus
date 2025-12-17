/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import org.jspecify.annotations.Nullable;

public record ScoreboardScoreResetS2CPacket(String scoreHolderName, @Nullable String objectiveName) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ScoreboardScoreResetS2CPacket> CODEC = Packet.createCodec(ScoreboardScoreResetS2CPacket::write, ScoreboardScoreResetS2CPacket::new);

    private ScoreboardScoreResetS2CPacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readNullable(PacketByteBuf::readString));
    }

    private void write(PacketByteBuf buf) {
        buf.writeString(this.scoreHolderName);
        buf.writeNullable(this.objectiveName, PacketByteBuf::writeString);
    }

    @Override
    public PacketType<ScoreboardScoreResetS2CPacket> getPacketType() {
        return PlayPackets.RESET_SCORE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onScoreboardScoreReset(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ScoreboardScoreResetS2CPacket.class, "owner;objectiveName", "scoreHolderName", "objectiveName"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ScoreboardScoreResetS2CPacket.class, "owner;objectiveName", "scoreHolderName", "objectiveName"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ScoreboardScoreResetS2CPacket.class, "owner;objectiveName", "scoreHolderName", "objectiveName"}, this, object);
    }
}

