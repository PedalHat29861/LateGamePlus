/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.state;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.state.NetworkState;

public interface ContextAwareNetworkStateFactory<T extends PacketListener, B extends ByteBuf, C>
extends NetworkState.Factory {
    public NetworkState<T> bind(Function<ByteBuf, B> var1, C var2);
}

