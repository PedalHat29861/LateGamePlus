/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public class StopSoundS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, StopSoundS2CPacket> CODEC = Packet.createCodec(StopSoundS2CPacket::write, StopSoundS2CPacket::new);
    private static final int CATEGORY_MASK = 1;
    private static final int SOUND_ID_MASK = 2;
    private final @Nullable Identifier soundId;
    private final @Nullable SoundCategory category;

    public StopSoundS2CPacket(@Nullable Identifier soundId, @Nullable SoundCategory category) {
        this.soundId = soundId;
        this.category = category;
    }

    private StopSoundS2CPacket(PacketByteBuf buf) {
        byte i = buf.readByte();
        this.category = (i & 1) > 0 ? buf.readEnumConstant(SoundCategory.class) : null;
        this.soundId = (i & 2) > 0 ? buf.readIdentifier() : null;
    }

    private void write(PacketByteBuf buf) {
        if (this.category != null) {
            if (this.soundId != null) {
                buf.writeByte(3);
                buf.writeEnumConstant(this.category);
                buf.writeIdentifier(this.soundId);
            } else {
                buf.writeByte(1);
                buf.writeEnumConstant(this.category);
            }
        } else if (this.soundId != null) {
            buf.writeByte(2);
            buf.writeIdentifier(this.soundId);
        } else {
            buf.writeByte(0);
        }
    }

    @Override
    public PacketType<StopSoundS2CPacket> getPacketType() {
        return PlayPackets.STOP_SOUND;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onStopSound(this);
    }

    public @Nullable Identifier getSoundId() {
        return this.soundId;
    }

    public @Nullable SoundCategory getCategory() {
        return this.category;
    }
}

