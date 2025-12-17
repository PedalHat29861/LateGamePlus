/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  org.slf4j.Logger
 */
package net.minecraft.network.handler;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.handler.NetworkStateTransitionHandler;
import net.minecraft.network.handler.PacketException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.state.NetworkState;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class DecoderHandler<T extends PacketListener>
extends ByteToMessageDecoder
implements NetworkStateTransitionHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NetworkState<T> state;

    public DecoderHandler(NetworkState<T> state) {
        this.state = state;
    }

    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> objects) throws Exception {
        Packet packet;
        int i = buf.readableBytes();
        try {
            packet = (Packet)this.state.codec().decode(buf);
        }
        catch (Exception exception) {
            if (exception instanceof PacketException) {
                buf.skipBytes(buf.readableBytes());
            }
            throw exception;
        }
        PacketType packetType = packet.getPacketType();
        FlightProfiler.INSTANCE.onPacketReceived(this.state.id(), packetType, context.channel().remoteAddress(), i);
        if (buf.readableBytes() > 0) {
            throw new IOException("Packet " + this.state.id().getId() + "/" + String.valueOf(packetType) + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + buf.readableBytes() + " bytes extra whilst reading packet " + String.valueOf(packetType));
        }
        objects.add(packet);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(ClientConnection.PACKET_RECEIVED_MARKER, " IN: [{}:{}] {} -> {} bytes", new Object[]{this.state.id().getId(), packetType, packet.getClass().getName(), i});
        }
        NetworkStateTransitionHandler.onDecoded(context, packet);
    }
}

