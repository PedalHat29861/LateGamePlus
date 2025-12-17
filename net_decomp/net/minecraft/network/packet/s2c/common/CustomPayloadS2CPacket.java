/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.network.packet.s2c.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record CustomPayloadS2CPacket(CustomPayload payload) implements Packet<ClientCommonPacketListener>
{
    private static final int MAX_PAYLOAD_SIZE = 0x100000;
    public static final PacketCodec<RegistryByteBuf, CustomPayloadS2CPacket> PLAY_CODEC = CustomPayload.createCodec((Identifier id) -> UnknownCustomPayload.createCodec(id, 0x100000), Util.make(Lists.newArrayList((Object[])new CustomPayload.Type[]{new CustomPayload.Type<PacketByteBuf, BrandCustomPayload>(BrandCustomPayload.ID, BrandCustomPayload.CODEC)}), types -> {})).xmap(CustomPayloadS2CPacket::new, CustomPayloadS2CPacket::payload);
    public static final PacketCodec<PacketByteBuf, CustomPayloadS2CPacket> CONFIGURATION_CODEC = CustomPayload.createCodec((Identifier id) -> UnknownCustomPayload.createCodec(id, 0x100000), List.of(new CustomPayload.Type<PacketByteBuf, BrandCustomPayload>(BrandCustomPayload.ID, BrandCustomPayload.CODEC))).xmap(CustomPayloadS2CPacket::new, CustomPayloadS2CPacket::payload);

    @Override
    public PacketType<CustomPayloadS2CPacket> getPacketType() {
        return CommonPackets.CUSTOM_PAYLOAD_S2C;
    }

    @Override
    public void apply(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.onCustomPayload(this);
    }
}

