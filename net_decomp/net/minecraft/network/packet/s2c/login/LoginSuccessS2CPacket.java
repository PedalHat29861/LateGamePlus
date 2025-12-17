/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.login;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record LoginSuccessS2CPacket(GameProfile profile) implements Packet<ClientLoginPacketListener>
{
    public static final PacketCodec<ByteBuf, LoginSuccessS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.GAME_PROFILE, LoginSuccessS2CPacket::profile, LoginSuccessS2CPacket::new);

    @Override
    public PacketType<LoginSuccessS2CPacket> getPacketType() {
        return LoginPackets.LOGIN_FINISHED;
    }

    @Override
    public void apply(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.onSuccess(this);
    }

    @Override
    public boolean transitionsNetworkState() {
        return true;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoginSuccessS2CPacket.class, "gameProfile", "profile"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoginSuccessS2CPacket.class, "gameProfile", "profile"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoginSuccessS2CPacket.class, "gameProfile", "profile"}, this, object);
    }
}

