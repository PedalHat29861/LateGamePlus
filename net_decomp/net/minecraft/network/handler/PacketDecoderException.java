/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 */
package net.minecraft.network.handler;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.handler.PacketCodecDispatcher;
import net.minecraft.network.handler.PacketException;

public class PacketDecoderException
extends DecoderException
implements PacketCodecDispatcher.UndecoratedException,
PacketException {
    public PacketDecoderException(String message) {
        super(message);
    }

    public PacketDecoderException(Throwable cause) {
        super(cause);
    }
}

