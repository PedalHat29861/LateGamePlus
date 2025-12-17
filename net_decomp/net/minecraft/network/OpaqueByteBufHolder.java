/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.util.ReferenceCounted
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCounted;

public record OpaqueByteBufHolder(ByteBuf contents) implements ReferenceCounted
{
    public OpaqueByteBufHolder(ByteBuf buf) {
        this.contents = ByteBufUtil.ensureAccessible((ByteBuf)buf);
    }

    public static Object pack(Object buf) {
        if (buf instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf)buf;
            return new OpaqueByteBufHolder(byteBuf);
        }
        return buf;
    }

    public static Object unpack(Object holder) {
        if (holder instanceof OpaqueByteBufHolder) {
            OpaqueByteBufHolder opaqueByteBufHolder = (OpaqueByteBufHolder)holder;
            return ByteBufUtil.ensureAccessible((ByteBuf)opaqueByteBufHolder.contents);
        }
        return holder;
    }

    public int refCnt() {
        return this.contents.refCnt();
    }

    public OpaqueByteBufHolder retain() {
        this.contents.retain();
        return this;
    }

    public OpaqueByteBufHolder retain(int i) {
        this.contents.retain(i);
        return this;
    }

    public OpaqueByteBufHolder touch() {
        this.contents.touch();
        return this;
    }

    public OpaqueByteBufHolder touch(Object object) {
        this.contents.touch(object);
        return this;
    }

    public boolean release() {
        return this.contents.release();
    }

    public boolean release(int count) {
        return this.contents.release(count);
    }

    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    public /* synthetic */ ReferenceCounted retain(int count) {
        return this.retain(count);
    }

    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}

