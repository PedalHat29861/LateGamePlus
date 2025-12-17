/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
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
import net.minecraft.util.Identifier;

public class CommonPackets {
    public static final PacketType<ClearDialogS2CPacket> CLEAR_DIALOG = CommonPackets.s2c("clear_dialog");
    public static final PacketType<CustomPayloadS2CPacket> CUSTOM_PAYLOAD_S2C = CommonPackets.s2c("custom_payload");
    public static final PacketType<CustomReportDetailsS2CPacket> CUSTOM_REPORT_DETAILS = CommonPackets.s2c("custom_report_details");
    public static final PacketType<DisconnectS2CPacket> DISCONNECT = CommonPackets.s2c("disconnect");
    public static final PacketType<KeepAliveS2CPacket> KEEP_ALIVE_S2C = CommonPackets.s2c("keep_alive");
    public static final PacketType<CommonPingS2CPacket> PING = CommonPackets.s2c("ping");
    public static final PacketType<ResourcePackRemoveS2CPacket> RESOURCE_PACK_POP = CommonPackets.s2c("resource_pack_pop");
    public static final PacketType<ResourcePackSendS2CPacket> RESOURCE_PACK_PUSH = CommonPackets.s2c("resource_pack_push");
    public static final PacketType<ServerLinksS2CPacket> SERVER_LINKS = CommonPackets.s2c("server_links");
    public static final PacketType<ShowDialogS2CPacket> SHOW_DIALOG = CommonPackets.s2c("show_dialog");
    public static final PacketType<StoreCookieS2CPacket> STORE_COOKIE = CommonPackets.s2c("store_cookie");
    public static final PacketType<ServerTransferS2CPacket> TRANSFER = CommonPackets.s2c("transfer");
    public static final PacketType<SynchronizeTagsS2CPacket> UPDATE_TAGS = CommonPackets.s2c("update_tags");
    public static final PacketType<ClientOptionsC2SPacket> CLIENT_INFORMATION = CommonPackets.c2s("client_information");
    public static final PacketType<CustomPayloadC2SPacket> CUSTOM_PAYLOAD_C2S = CommonPackets.c2s("custom_payload");
    public static final PacketType<KeepAliveC2SPacket> KEEP_ALIVE_C2S = CommonPackets.c2s("keep_alive");
    public static final PacketType<CommonPongC2SPacket> PONG = CommonPackets.c2s("pong");
    public static final PacketType<ResourcePackStatusC2SPacket> RESOURCE_PACK = CommonPackets.c2s("resource_pack");
    public static final PacketType<CustomClickActionC2SPacket> CUSTOM_CLICK_ACTION = CommonPackets.c2s("custom_click_action");

    private static <T extends Packet<ClientCommonPacketListener>> PacketType<T> s2c(String id) {
        return new PacketType(NetworkSide.CLIENTBOUND, Identifier.ofVanilla(id));
    }

    private static <T extends Packet<ServerCommonPacketListener>> PacketType<T> c2s(String id) {
        return new PacketType(NetworkSide.SERVERBOUND, Identifier.ofVanilla(id));
    }
}

