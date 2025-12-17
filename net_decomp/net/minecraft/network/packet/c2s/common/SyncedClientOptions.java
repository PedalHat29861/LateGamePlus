/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.common;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.Arm;

public record SyncedClientOptions(String language, int viewDistance, ChatVisibility chatVisibility, boolean chatColorsEnabled, int playerModelParts, Arm mainArm, boolean filtersText, boolean allowsServerListing, ParticlesMode particleStatus) {
    public static final int MAX_LANGUAGE_CODE_LENGTH = 16;

    public SyncedClientOptions(PacketByteBuf buf) {
        this(buf.readString(16), buf.readByte(), buf.readEnumConstant(ChatVisibility.class), buf.readBoolean(), buf.readUnsignedByte(), buf.readEnumConstant(Arm.class), buf.readBoolean(), buf.readBoolean(), buf.readEnumConstant(ParticlesMode.class));
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(this.language);
        buf.writeByte(this.viewDistance);
        buf.writeEnumConstant(this.chatVisibility);
        buf.writeBoolean(this.chatColorsEnabled);
        buf.writeByte(this.playerModelParts);
        buf.writeEnumConstant(this.mainArm);
        buf.writeBoolean(this.filtersText);
        buf.writeBoolean(this.allowsServerListing);
        buf.writeEnumConstant(this.particleStatus);
    }

    public static SyncedClientOptions createDefault() {
        return new SyncedClientOptions("en_us", 2, ChatVisibility.FULL, true, 0, PlayerEntity.MAIN_ARM, false, false, ParticlesMode.ALL);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{SyncedClientOptions.class, "language;viewDistance;chatVisibility;chatColors;modelCustomisation;mainHand;textFilteringEnabled;allowsListing;particleStatus", "language", "viewDistance", "chatVisibility", "chatColorsEnabled", "playerModelParts", "mainArm", "filtersText", "allowsServerListing", "particleStatus"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SyncedClientOptions.class, "language;viewDistance;chatVisibility;chatColors;modelCustomisation;mainHand;textFilteringEnabled;allowsListing;particleStatus", "language", "viewDistance", "chatVisibility", "chatColorsEnabled", "playerModelParts", "mainArm", "filtersText", "allowsServerListing", "particleStatus"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SyncedClientOptions.class, "language;viewDistance;chatVisibility;chatColors;modelCustomisation;mainHand;textFilteringEnabled;allowsListing;particleStatus", "language", "viewDistance", "chatVisibility", "chatColorsEnabled", "playerModelParts", "mainArm", "filtersText", "allowsServerListing", "particleStatus"}, this, object);
    }
}

