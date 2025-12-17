/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.EncoderException
 */
package net.minecraft.network.handler;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.handler.PacketCodecDispatcher;
import net.minecraft.network.handler.PacketException;

public class PacketEncoderException
extends EncoderException
implements PacketCodecDispatcher.UndecoratedException,
PacketException {
    public PacketEncoderException(String message) {
        super(message);
    }

    public PacketEncoderException(Throwable cause) {
        super(cause);
    }
}

