/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.EncoderException
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package net.minecraft.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.encoding.VarInts;

@ChannelHandler.Sharable
public class SizePrepender
extends MessageToByteEncoder<ByteBuf> {
    public static final int MAX_PREPEND_LENGTH = 3;

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        int i = byteBuf.readableBytes();
        int j = VarInts.getSizeInBytes(i);
        if (j > 3) {
            throw new EncoderException("Packet too large: size " + i + " is over 8");
        }
        byteBuf2.ensureWritable(j + i);
        VarInts.write(byteBuf2, i);
        byteBuf2.writeBytes(byteBuf, byteBuf.readerIndex(), i);
    }

    protected /* synthetic */ void encode(ChannelHandlerContext ctx, Object input, ByteBuf output) throws Exception {
        this.encode(ctx, (ByteBuf)input, output);
    }
}

