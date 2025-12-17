/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.world.chunk.EntryMissingException;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;

public class BiMapPalette<T>
implements Palette<T> {
    private final Int2ObjectBiMap<T> map;
    private final int indexBits;

    public BiMapPalette(int indexBits, List<T> values) {
        this(indexBits);
        values.forEach(this.map::add);
    }

    public BiMapPalette(int indexBits) {
        this(indexBits, Int2ObjectBiMap.create(1 << indexBits));
    }

    private BiMapPalette(int indexBits, Int2ObjectBiMap<T> map) {
        this.indexBits = indexBits;
        this.map = map;
    }

    public static <A> Palette<A> create(int bits, List<A> values) {
        return new BiMapPalette<A>(bits, values);
    }

    @Override
    public int index(T object, PaletteResizeListener<T> listener) {
        int i = this.map.getRawId(object);
        if (i == -1 && (i = this.map.add(object)) >= 1 << this.indexBits) {
            i = listener.onResize(this.indexBits + 1, object);
        }
        return i;
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        for (int i = 0; i < this.getSize(); ++i) {
            if (!predicate.test(this.map.get(i))) continue;
            return true;
        }
        return false;
    }

    @Override
    public T get(int id) {
        T object = this.map.get(id);
        if (object == null) {
            throw new EntryMissingException(id);
        }
        return object;
    }

    @Override
    public void readPacket(PacketByteBuf buf, IndexedIterable<T> idList) {
        this.map.clear();
        int i = buf.readVarInt();
        for (int j = 0; j < i; ++j) {
            this.map.add(idList.getOrThrow(buf.readVarInt()));
        }
    }

    @Override
    public void writePacket(PacketByteBuf buf, IndexedIterable<T> idList) {
        int i = this.getSize();
        buf.writeVarInt(i);
        for (int j = 0; j < i; ++j) {
            buf.writeVarInt(idList.getRawId(this.map.get(j)));
        }
    }

    @Override
    public int getPacketSize(IndexedIterable<T> idList) {
        int i = VarInts.getSizeInBytes(this.getSize());
        for (int j = 0; j < this.getSize(); ++j) {
            i += VarInts.getSizeInBytes(idList.getRawId(this.map.get(j)));
        }
        return i;
    }

    public List<T> getElements() {
        ArrayList arrayList = new ArrayList();
        this.map.iterator().forEachRemaining(arrayList::add);
        return arrayList;
    }

    @Override
    public int getSize() {
        return this.map.size();
    }

    @Override
    public Palette<T> copy() {
        return new BiMapPalette<T>(this.indexBits, this.map.copy());
    }
}

