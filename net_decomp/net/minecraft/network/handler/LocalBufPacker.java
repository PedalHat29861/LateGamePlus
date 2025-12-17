/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPromise
 */
package net.minecraft.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.OpaqueByteBufHolder;

public class LocalBufPacker
extends ChannelOutboundHandlerAdapter {
    public void write(ChannelHandlerContext context, Object buf, ChannelPromise channelPromise) {
        context.write(OpaqueByteBufHolder.pack(buf), channelPromise);
    }
}

