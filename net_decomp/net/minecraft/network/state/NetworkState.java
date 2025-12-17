/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.annotation.Debug;
import org.jspecify.annotations.Nullable;

public interface NetworkState<T extends PacketListener> {
    public NetworkPhase id();

    public NetworkSide side();

    public PacketCodec<ByteBuf, Packet<? super T>> codec();

    public @Nullable PacketBundleHandler bundleHandler();

    public static interface Factory {
        public Unbound buildUnbound();
    }

    public static interface Unbound {
        public NetworkPhase phase();

        public NetworkSide side();

        @Debug
        public void forEachPacketType(PacketTypeConsumer var1);

        @FunctionalInterface
        public static interface PacketTypeConsumer {
            public void accept(PacketType<?> var1, int var2);
        }
    }
}

