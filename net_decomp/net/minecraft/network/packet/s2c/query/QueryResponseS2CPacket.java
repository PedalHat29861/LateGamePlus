/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.JsonOps
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.query;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.StatusPackets;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.ServerMetadata;

public record QueryResponseS2CPacket(ServerMetadata metadata) implements Packet<ClientQueryPacketListener>
{
    private static final RegistryOps<JsonElement> OPS = DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE);
    public static final PacketCodec<ByteBuf, QueryResponseS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.lenientJson(Short.MAX_VALUE).collect(PacketCodecs.fromCodec(OPS, ServerMetadata.CODEC)), QueryResponseS2CPacket::metadata, QueryResponseS2CPacket::new);

    @Override
    public PacketType<QueryResponseS2CPacket> getPacketType() {
        return StatusPackets.STATUS_RESPONSE;
    }

    @Override
    public void apply(ClientQueryPacketListener clientQueryPacketListener) {
        clientQueryPacketListener.onResponse(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{QueryResponseS2CPacket.class, "status", "metadata"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{QueryResponseS2CPacket.class, "status", "metadata"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{QueryResponseS2CPacket.class, "status", "metadata"}, this, object);
    }
}

