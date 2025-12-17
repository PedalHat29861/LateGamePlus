/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ClientCookieRequestPacketListener;
import net.minecraft.network.packet.s2c.common.ClearDialogS2CPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;

public interface ClientCommonPacketListener
extends ClientCookieRequestPacketListener {
    public void onKeepAlive(KeepAliveS2CPacket var1);

    public void onPing(CommonPingS2CPacket var1);

    public void onCustomPayload(CustomPayloadS2CPacket var1);

    public void onDisconnect(DisconnectS2CPacket var1);

    public void onResourcePackSend(ResourcePackSendS2CPacket var1);

    public void onResourcePackRemove(ResourcePackRemoveS2CPacket var1);

    public void onSynchronizeTags(SynchronizeTagsS2CPacket var1);

    public void onStoreCookie(StoreCookieS2CPacket var1);

    public void onServerTransfer(ServerTransferS2CPacket var1);

    public void onCustomReportDetails(CustomReportDetailsS2CPacket var1);

    public void onServerLinks(ServerLinksS2CPacket var1);

    public void onClearDialog(ClearDialogS2CPacket var1);

    public void onShowDialog(ShowDialogS2CPacket var1);
}

