/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 */
package net.minecraft.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.OpaqueByteBufHolder;

public class LocalBufUnpacker
extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext context, Object buf) {
        context.fireChannelRead(OpaqueByteBufHolder.unpack(buf));
    }
}

