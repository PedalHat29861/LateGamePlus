/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.thread.LockHelper;
import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PaletteType;
import net.minecraft.world.chunk.ReadableContainer;
import org.jspecify.annotations.Nullable;

public class PalettedContainer<T>
implements PaletteResizeListener<T>,
ReadableContainer<T> {
    private static final int field_34557 = 0;
    private volatile Data<T> data;
    private final PaletteProvider<T> paletteProvider;
    private final LockHelper lockHelper = new LockHelper("PalettedContainer");

    public void lock() {
        this.lockHelper.lock();
    }

    public void unlock() {
        this.lockHelper.unlock();
    }

    public static <T> Codec<PalettedContainer<T>> createPalettedContainerCodec(Codec<T> entryCodec, PaletteProvider<T> provider, T defaultValue) {
        ReadableContainer.Reader reader = PalettedContainer::read;
        return PalettedContainer.createCodec(entryCodec, provider, defaultValue, reader);
    }

    public static <T> Codec<ReadableContainer<T>> createReadableContainerCodec(Codec<T> entryCodec, PaletteProvider<T> provider, T defaultValue) {
        ReadableContainer.Reader reader = (paletteProvider, serialized) -> PalettedContainer.read(paletteProvider, serialized).map(result -> result);
        return PalettedContainer.createCodec(entryCodec, provider, defaultValue, reader);
    }

    private static <T, C extends ReadableContainer<T>> Codec<C> createCodec(Codec<T> entryCodec, PaletteProvider<T> provider, T defaultValue, ReadableContainer.Reader<T, C> reader) {
        return RecordCodecBuilder.create(instance -> instance.group((App)entryCodec.mapResult(Codecs.orElsePartial(defaultValue)).listOf().fieldOf("palette").forGetter(ReadableContainer.Serialized::paletteEntries), (App)Codec.LONG_STREAM.lenientOptionalFieldOf("data").forGetter(ReadableContainer.Serialized::storage)).apply((Applicative)instance, ReadableContainer.Serialized::new)).comapFlatMap(serialized -> reader.read(provider, (ReadableContainer.Serialized)serialized), container -> container.serialize(provider));
    }

    private PalettedContainer(PaletteProvider<T> paletteProvider, PaletteType type, PaletteStorage storage, Palette<T> palette) {
        this.paletteProvider = paletteProvider;
        this.data = new Data<T>(type, storage, palette);
    }

    private PalettedContainer(PalettedContainer<T> container) {
        this.paletteProvider = container.paletteProvider;
        this.data = container.data.copy();
    }

    public PalettedContainer(T defaultValue, PaletteProvider<T> paletteProvider) {
        this.paletteProvider = paletteProvider;
        this.data = this.getCompatibleData(null, 0);
        this.data.palette.index(defaultValue, this);
    }

    private Data<T> getCompatibleData(@Nullable Data<T> previousData, int bits) {
        PaletteType paletteType = this.paletteProvider.createType(bits);
        if (previousData != null && paletteType.equals(previousData.configuration())) {
            return previousData;
        }
        PaletteStorage paletteStorage = paletteType.bitsInMemory() == 0 ? new EmptyPaletteStorage(this.paletteProvider.getSize()) : new PackedIntegerArray(paletteType.bitsInMemory(), this.paletteProvider.getSize());
        Palette<T> palette = paletteType.createPalette(this.paletteProvider, List.of());
        return new Data<T>(paletteType, paletteStorage, palette);
    }

    @Override
    public int onResize(int i, T object) {
        Data<T> data = this.data;
        Data data2 = this.getCompatibleData(data, i);
        data2.importFrom(data.palette, data.storage);
        this.data = data2;
        return data2.palette.index(object, PaletteResizeListener.throwing());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T swap(int x, int y, int z, T value) {
        this.lock();
        try {
            T t = this.swap(this.paletteProvider.computeIndex(x, y, z), value);
            return t;
        }
        finally {
            this.unlock();
        }
    }

    public T swapUnsafe(int x, int y, int z, T value) {
        return this.swap(this.paletteProvider.computeIndex(x, y, z), value);
    }

    private T swap(int index, T value) {
        int i = this.data.palette.index(value, this);
        int j = this.data.storage.swap(index, i);
        return this.data.palette.get(j);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set(int x, int y, int z, T value) {
        this.lock();
        try {
            this.set(this.paletteProvider.computeIndex(x, y, z), value);
        }
        finally {
            this.unlock();
        }
    }

    private void set(int index, T value) {
        int i = this.data.palette.index(value, this);
        this.data.storage.set(index, i);
    }

    @Override
    public T get(int x, int y, int z) {
        return this.get(this.paletteProvider.computeIndex(x, y, z));
    }

    protected T get(int index) {
        Data<T> data = this.data;
        return data.palette.get(data.storage.get(index));
    }

    @Override
    public void forEachValue(Consumer<T> action) {
        Palette palette = this.data.palette();
        IntArraySet intSet = new IntArraySet();
        this.data.storage.forEach(arg_0 -> ((IntSet)intSet).add(arg_0));
        intSet.forEach(id -> action.accept(palette.get(id)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void readPacket(PacketByteBuf buf) {
        this.lock();
        try {
            byte i = buf.readByte();
            Data<T> data = this.getCompatibleData(this.data, i);
            data.palette.readPacket(buf, this.paletteProvider.getIdList());
            buf.readFixedLengthLongArray(data.storage.getData());
            this.data = data;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void writePacket(PacketByteBuf buf) {
        this.lock();
        try {
            this.data.writePacket(buf, this.paletteProvider.getIdList());
        }
        finally {
            this.unlock();
        }
    }

    @VisibleForTesting
    public static <T> DataResult<PalettedContainer<T>> read(PaletteProvider<T> provider, ReadableContainer.Serialized<T> serialized) {
        PaletteStorage paletteStorage;
        Palette<T> palette;
        List<T> list = serialized.paletteEntries();
        int i = provider.getSize();
        PaletteType paletteType = provider.createTypeFromSize(list.size());
        int j = paletteType.bitsInStorage();
        if (serialized.bitsPerEntry() != -1 && j != serialized.bitsPerEntry()) {
            return DataResult.error(() -> "Invalid bit count, calculated " + j + ", but container declared " + serialized.bitsPerEntry());
        }
        if (paletteType.bitsInMemory() == 0) {
            palette = paletteType.createPalette(provider, list);
            paletteStorage = new EmptyPaletteStorage(i);
        } else {
            Optional<LongStream> optional = serialized.storage();
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Missing values for non-zero storage");
            }
            long[] ls = optional.get().toArray();
            try {
                if (paletteType.shouldRepack() || paletteType.bitsInMemory() != j) {
                    BiMapPalette<T> palette2 = new BiMapPalette<T>(j, list);
                    PackedIntegerArray packedIntegerArray = new PackedIntegerArray(j, i, ls);
                    Palette<T> palette3 = paletteType.createPalette(provider, list);
                    int[] is = PalettedContainer.repack(packedIntegerArray, palette2, palette3);
                    palette = palette3;
                    paletteStorage = new PackedIntegerArray(paletteType.bitsInMemory(), i, is);
                } else {
                    palette = paletteType.createPalette(provider, list);
                    paletteStorage = new PackedIntegerArray(paletteType.bitsInMemory(), i, ls);
                }
            }
            catch (PackedIntegerArray.InvalidLengthException invalidLengthException) {
                return DataResult.error(() -> "Failed to read PalettedContainer: " + invalidLengthException.getMessage());
            }
        }
        return DataResult.success(new PalettedContainer<T>(provider, paletteType, paletteStorage, palette));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public ReadableContainer.Serialized<T> serialize(PaletteProvider<T> provider) {
        this.lock();
        try {
            Optional<LongStream> optional;
            PaletteStorage paletteStorage = this.data.storage;
            Palette palette = this.data.palette;
            BiMapPalette biMapPalette = new BiMapPalette(paletteStorage.getElementBits());
            int i = provider.getSize();
            int[] is = PalettedContainer.repack(paletteStorage, palette, biMapPalette);
            PaletteType paletteType = provider.createTypeFromSize(biMapPalette.getSize());
            int j = paletteType.bitsInStorage();
            if (j != 0) {
                PackedIntegerArray packedIntegerArray = new PackedIntegerArray(j, i, is);
                optional = Optional.of(Arrays.stream(packedIntegerArray.getData()));
            } else {
                optional = Optional.empty();
            }
            ReadableContainer.Serialized serialized = new ReadableContainer.Serialized(biMapPalette.getElements(), optional, j);
            return serialized;
        }
        finally {
            this.unlock();
        }
    }

    private static <T> int[] repack(PaletteStorage storage, Palette<T> oldPalette, Palette<T> newPalette) {
        int[] is = new int[storage.getSize()];
        storage.writePaletteIndices(is);
        PaletteResizeListener paletteResizeListener = PaletteResizeListener.throwing();
        int i = -1;
        int j = -1;
        for (int k = 0; k < is.length; ++k) {
            int l = is[k];
            if (l != i) {
                i = l;
                j = newPalette.index(oldPalette.get(l), paletteResizeListener);
            }
            is[k] = j;
        }
        return is;
    }

    @Override
    public int getPacketSize() {
        return this.data.getPacketSize(this.paletteProvider.getIdList());
    }

    @Override
    public int getElementBits() {
        return this.data.storage().getElementBits();
    }

    @Override
    public boolean hasAny(Predicate<T> predicate) {
        return this.data.palette.hasAny(predicate);
    }

    @Override
    public PalettedContainer<T> copy() {
        return new PalettedContainer<T>(this);
    }

    @Override
    public PalettedContainer<T> slice() {
        return new PalettedContainer(this.data.palette.get(0), this.paletteProvider);
    }

    @Override
    public void count(Counter<T> counter) {
        if (this.data.palette.getSize() == 1) {
            counter.accept(this.data.palette.get(0), this.data.storage.getSize());
            return;
        }
        Int2IntOpenHashMap int2IntOpenHashMap = new Int2IntOpenHashMap();
        this.data.storage.forEach(key -> int2IntOpenHashMap.addTo(key, 1));
        int2IntOpenHashMap.int2IntEntrySet().forEach(entry -> counter.accept(this.data.palette.get(entry.getIntKey()), entry.getIntValue()));
    }

    static final class Data<T>
    extends Record {
        private final PaletteType configuration;
        final PaletteStorage storage;
        final Palette<T> palette;

        Data(PaletteType configuration, PaletteStorage storage, Palette<T> palette) {
            this.configuration = configuration;
            this.storage = storage;
            this.palette = palette;
        }

        public void importFrom(Palette<T> palette, PaletteStorage storage) {
            PaletteResizeListener paletteResizeListener = PaletteResizeListener.throwing();
            for (int i = 0; i < storage.getSize(); ++i) {
                T object = palette.get(storage.get(i));
                this.storage.set(i, this.palette.index(object, paletteResizeListener));
            }
        }

        public int getPacketSize(IndexedIterable<T> idList) {
            return 1 + this.palette.getPacketSize(idList) + this.storage.getData().length * 8;
        }

        public void writePacket(PacketByteBuf buf, IndexedIterable<T> idList) {
            buf.writeByte(this.storage.getElementBits());
            this.palette.writePacket(buf, idList);
            buf.writeFixedLengthLongArray(this.storage.getData());
        }

        public Data<T> copy() {
            return new Data<T>(this.configuration, this.storage.copy(), this.palette.copy());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "configuration;storage;palette", "configuration", "storage", "palette"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "configuration;storage;palette", "configuration", "storage", "palette"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "configuration;storage;palette", "configuration", "storage", "palette"}, this, object);
        }

        public PaletteType configuration() {
            return this.configuration;
        }

        public PaletteStorage storage() {
            return this.storage;
        }

        public Palette<T> palette() {
            return this.palette;
        }
    }

    @FunctionalInterface
    public static interface Counter<T> {
        public void accept(T var1, int var2);
    }
}

