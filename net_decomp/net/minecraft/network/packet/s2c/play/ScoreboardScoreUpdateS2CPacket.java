/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record ScoreboardScoreUpdateS2CPacket(String scoreHolderName, String objectiveName, int score, Optional<Text> display, Optional<NumberFormat> numberFormat) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, ScoreboardScoreUpdateS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ScoreboardScoreUpdateS2CPacket::scoreHolderName, PacketCodecs.STRING, ScoreboardScoreUpdateS2CPacket::objectiveName, PacketCodecs.VAR_INT, ScoreboardScoreUpdateS2CPacket::score, TextCodecs.OPTIONAL_UNLIMITED_REGISTRY_PACKET_CODEC, ScoreboardScoreUpdateS2CPacket::display, NumberFormatTypes.OPTIONAL_PACKET_CODEC, ScoreboardScoreUpdateS2CPacket::numberFormat, ScoreboardScoreUpdateS2CPacket::new);

    @Override
    public PacketType<ScoreboardScoreUpdateS2CPacket> getPacketType() {
        return PlayPackets.SET_SCORE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onScoreboardScoreUpdate(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ScoreboardScoreUpdateS2CPacket.class, "owner;objectiveName;score;display;numberFormat", "scoreHolderName", "objectiveName", "score", "display", "numberFormat"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ScoreboardScoreUpdateS2CPacket.class, "owner;objectiveName;score;display;numberFormat", "scoreHolderName", "objectiveName", "score", "display", "numberFormat"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ScoreboardScoreUpdateS2CPacket.class, "owner;objectiveName;score;display;numberFormat", "scoreHolderName", "objectiveName", "score", "display", "numberFormat"}, this, object);
    }
}

