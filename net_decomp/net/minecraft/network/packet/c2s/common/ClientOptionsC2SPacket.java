/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.common;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;

public record ClientOptionsC2SPacket(SyncedClientOptions options) implements Packet<ServerCommonPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ClientOptionsC2SPacket> CODEC = Packet.createCodec(ClientOptionsC2SPacket::write, ClientOptionsC2SPacket::new);

    private ClientOptionsC2SPacket(PacketByteBuf buf) {
        this(new SyncedClientOptions(buf));
    }

    private void write(PacketByteBuf buf) {
        this.options.write(buf);
    }

    @Override
    public PacketType<ClientOptionsC2SPacket> getPacketType() {
        return CommonPackets.CLIENT_INFORMATION;
    }

    @Override
    public void apply(ServerCommonPacketListener serverCommonPacketListener) {
        serverCommonPacketListener.onClientOptions(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ClientOptionsC2SPacket.class, "information", "options"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ClientOptionsC2SPacket.class, "information", "options"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ClientOptionsC2SPacket.class, "information", "options"}, this, object);
    }
}

