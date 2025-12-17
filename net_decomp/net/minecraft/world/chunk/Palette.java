/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.PaletteResizeListener;

public interface Palette<T> {
    public int index(T var1, PaletteResizeListener<T> var2);

    public boolean hasAny(Predicate<T> var1);

    public T get(int var1);

    public void readPacket(PacketByteBuf var1, IndexedIterable<T> var2);

    public void writePacket(PacketByteBuf var1, IndexedIterable<T> var2);

    public int getPacketSize(IndexedIterable<T> var1);

    public int getSize();

    public Palette<T> copy();

    public static interface Factory {
        public <A> Palette<A> create(int var1, List<A> var2);
    }
}

