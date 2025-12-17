/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package net.minecraft.network.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;
import net.minecraft.network.encryption.PacketEncryptionManager;

public class PacketEncryptor
extends MessageToByteEncoder<ByteBuf> {
    private final PacketEncryptionManager manager;

    public PacketEncryptor(Cipher cipher) {
        this.manager = new PacketEncryptionManager(cipher);
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
        this.manager.encrypt(byteBuf, byteBuf2);
    }

    protected /* synthetic */ void encode(ChannelHandlerContext context, Object buf, ByteBuf result) throws Exception {
        this.encode(context, (ByteBuf)buf, result);
    }
}

