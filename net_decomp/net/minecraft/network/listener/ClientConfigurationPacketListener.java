/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.s2c.config.CodeOfConductS2CPacket;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.config.ResetChatS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;

public interface ClientConfigurationPacketListener
extends ClientCommonPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.CONFIGURATION;
    }

    public void onCodeOfConduct(CodeOfConductS2CPacket var1);

    public void onReady(ReadyS2CPacket var1);

    public void onDynamicRegistries(DynamicRegistriesS2CPacket var1);

    public void onFeatures(FeaturesS2CPacket var1);

    public void onSelectKnownPacks(SelectKnownPacksS2CPacket var1);

    public void onResetChat(ResetChatS2CPacket var1);
}

