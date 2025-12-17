/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 *  org.slf4j.Logger
 */
package net.minecraft.network.handler;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.handler.NetworkStateTransitionHandler;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.state.NetworkState;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class EncoderHandler<T extends PacketListener>
extends MessageToByteEncoder<Packet<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NetworkState<T> state;

    public EncoderHandler(NetworkState<T> state) {
        this.state = state;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<T> packet, ByteBuf byteBuf) throws Exception {
        PacketType<Packet<T>> packetType = packet.getPacketType();
        try {
            this.state.codec().encode(byteBuf, packet);
            int i = byteBuf.readableBytes();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(ClientConnection.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", new Object[]{this.state.id().getId(), packetType, packet.getClass().getName(), i});
            }
            FlightProfiler.INSTANCE.onPacketSent(this.state.id(), packetType, channelHandlerContext.channel().remoteAddress(), i);
        }
        catch (Throwable throwable) {
            LOGGER.error("Error sending packet {}", packetType, (Object)throwable);
            if (packet.isWritingErrorSkippable()) {
                throw new PacketEncoderException(throwable);
            }
            throw throwable;
        }
        finally {
            NetworkStateTransitionHandler.onEncoded(channelHandlerContext, packet);
        }
    }

    protected /* synthetic */ void encode(ChannelHandlerContext context, Object packet, ByteBuf out) throws Exception {
        this.encode(context, (Packet)packet, out);
    }
}

