/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.world.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.EntryMissingException;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.apache.commons.lang3.Validate;

public class ArrayPalette<T>
implements Palette<T> {
    private final T[] array;
    private final int indexBits;
    private int size;

    private ArrayPalette(int indexBits, List<T> values) {
        this.array = new Object[1 << indexBits];
        this.indexBits = indexBits;
        Validate.isTrue((values.size() <= this.array.length ? 1 : 0) != 0, (String)"Can't initialize LinearPalette of size %d with %d entries", (Object[])new Object[]{this.array.length, values.size()});
        for (int i = 0; i < values.size(); ++i) {
            this.array[i] = values.get(i);
        }
        this.size = values.size();
    }

    private ArrayPalette(T[] array, int indexBits, int size) {
        this.array = array;
        this.indexBits = indexBits;
        this.size = size;
    }

    public static <A> Palette<A> create(int bits, List<A> values) {
        return new ArrayPalette<A>(bits, values);
    }

    @Override
    public int index(T object, PaletteResizeListener<T> listener) {
        int i;
        for (i = 0; i < this.size; ++i) {
            if (this.array[i] != object) continue;
            return i;
        }
        if ((i = this.size++) < this.array.length) {
            this.array[i] = object;
            return i;
        }
        return listener.onResize(this.indexBits + 1, object);
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        for (int i = 0; i < this.size; ++i) {
            if (!predicate.test(this.array[i])) continue;
            return true;
        }
        return false;
    }

    @Override
    public T get(int id) {
        if (id >= 0 && id < this.size) {
            return this.array[id];
        }
        throw new EntryMissingException(id);
    }

    @Override
    public void readPacket(PacketByteBuf buf, IndexedIterable<T> idList) {
        this.size = buf.readVarInt();
        for (int i = 0; i < this.size; ++i) {
            this.array[i] = idList.getOrThrow(buf.readVarInt());
        }
    }

    @Override
    public void writePacket(PacketByteBuf buf, IndexedIterable<T> idList) {
        buf.writeVarInt(this.size);
        for (int i = 0; i < this.size; ++i) {
            buf.writeVarInt(idList.getRawId(this.array[i]));
        }
    }

    @Override
    public int getPacketSize(IndexedIterable<T> idList) {
        int i = VarInts.getSizeInBytes(this.getSize());
        for (int j = 0; j < this.getSize(); ++j) {
            i += VarInts.getSizeInBytes(idList.getRawId(this.array[j]));
        }
        return i;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public Palette<T> copy() {
        return new ArrayPalette<Object>((Object[])this.array.clone(), this.indexBits, this.size);
    }
}

