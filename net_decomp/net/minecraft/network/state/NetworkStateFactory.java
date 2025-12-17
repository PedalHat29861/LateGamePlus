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

public interface NetworkStateFactory<T extends PacketListener, B extends ByteBuf>
extends NetworkState.Factory {
    public NetworkState<T> bind(Function<ByteBuf, B> var1);
}

