/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;

public class PacketCodecDispatcher<B extends ByteBuf, V, T>
implements PacketCodec<B, V> {
    private static final int UNKNOWN_PACKET_INDEX = -1;
    private final Function<V, ? extends T> packetIdGetter;
    private final List<PacketType<B, V, T>> packetTypes;
    private final Object2IntMap<T> typeToIndex;

    PacketCodecDispatcher(Function<V, ? extends T> packetIdGetter, List<PacketType<B, V, T>> packetTypes, Object2IntMap<T> typeToIndex) {
        this.packetIdGetter = packetIdGetter;
        this.packetTypes = packetTypes;
        this.typeToIndex = typeToIndex;
    }

    @Override
    public V decode(B byteBuf) {
        int i = VarInts.read(byteBuf);
        if (i < 0 || i >= this.packetTypes.size()) {
            throw new DecoderException("Received unknown packet id " + i);
        }
        PacketType<B, V, T> packetType = this.packetTypes.get(i);
        try {
            return (V)packetType.codec.decode(byteBuf);
        }
        catch (Exception exception) {
            if (exception instanceof UndecoratedException) {
                throw exception;
            }
            throw new DecoderException("Failed to decode packet '" + String.valueOf(packetType.id) + "'", (Throwable)exception);
        }
    }

    @Override
    public void encode(B byteBuf, V object) {
        T object2 = this.packetIdGetter.apply(object);
        int i = this.typeToIndex.getOrDefault(object2, -1);
        if (i == -1) {
            throw new EncoderException("Sending unknown packet '" + String.valueOf(object2) + "'");
        }
        VarInts.write(byteBuf, i);
        PacketType<B, V, T> packetType = this.packetTypes.get(i);
        try {
            PacketCodec packetCodec = packetType.codec;
            packetCodec.encode(byteBuf, object);
        }
        catch (Exception exception) {
            if (exception instanceof UndecoratedException) {
                throw exception;
            }
            throw new EncoderException("Failed to encode packet '" + String.valueOf(object2) + "'", (Throwable)exception);
        }
    }

    public static <B extends ByteBuf, V, T> Builder<B, V, T> builder(Function<V, ? extends T> packetIdGetter) {
        return new Builder(packetIdGetter);
    }

    @Override
    public /* synthetic */ void encode(Object object, Object object2) {
        this.encode((B)((ByteBuf)object), (V)object2);
    }

    @Override
    public /* synthetic */ Object decode(Object object) {
        return this.decode((B)((ByteBuf)object));
    }

    static final class PacketType<B, V, T>
    extends Record {
        final PacketCodec<? super B, ? extends V> codec;
        final T id;

        PacketType(PacketCodec<? super B, ? extends V> codec, T id) {
            this.codec = codec;
            this.id = id;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PacketType.class, "serializer;type", "codec", "id"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PacketType.class, "serializer;type", "codec", "id"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PacketType.class, "serializer;type", "codec", "id"}, this, object);
        }

        public PacketCodec<? super B, ? extends V> codec() {
            return this.codec;
        }

        public T id() {
            return this.id;
        }
    }

    public static interface UndecoratedException {
    }

    public static class Builder<B extends ByteBuf, V, T> {
        private final List<PacketType<B, V, T>> packetTypes = new ArrayList<PacketType<B, V, T>>();
        private final Function<V, ? extends T> packetIdGetter;

        Builder(Function<V, ? extends T> packetIdGetter) {
            this.packetIdGetter = packetIdGetter;
        }

        public Builder<B, V, T> add(T id, PacketCodec<? super B, ? extends V> codec) {
            this.packetTypes.add(new PacketType<B, V, T>(codec, id));
            return this;
        }

        public PacketCodecDispatcher<B, V, T> build() {
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            object2IntOpenHashMap.defaultReturnValue(-2);
            for (PacketType<B, V, T> packetType : this.packetTypes) {
                int i = object2IntOpenHashMap.size();
                int j = object2IntOpenHashMap.putIfAbsent(packetType.id, i);
                if (j == -2) continue;
                throw new IllegalStateException("Duplicate registration for type " + String.valueOf(packetType.id));
            }
            return new PacketCodecDispatcher<B, V, T>(this.packetIdGetter, List.copyOf(this.packetTypes), object2IntOpenHashMap);
        }
    }
}

