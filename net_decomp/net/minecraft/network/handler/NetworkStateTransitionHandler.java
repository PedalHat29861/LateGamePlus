/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 */
package net.minecraft.network.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.handler.NetworkStateTransitions;
import net.minecraft.network.packet.Packet;

public interface NetworkStateTransitionHandler {
    public static void onDecoded(ChannelHandlerContext context, Packet<?> packet) {
        if (packet.transitionsNetworkState()) {
            context.channel().config().setAutoRead(false);
            context.pipeline().addBefore(context.name(), "inbound_config", (ChannelHandler)new NetworkStateTransitions.InboundConfigurer());
            context.pipeline().remove(context.name());
        }
    }

    public static void onEncoded(ChannelHandlerContext context, Packet<?> packet) {
        if (packet.transitionsNetworkState()) {
            context.pipeline().addAfter(context.name(), "outbound_config", (ChannelHandler)new NetworkStateTransitions.OutboundConfigurer());
            context.pipeline().remove(context.name());
        }
    }
}

