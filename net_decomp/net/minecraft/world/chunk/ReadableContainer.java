/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PalettedContainer;

public interface ReadableContainer<T> {
    public T get(int var1, int var2, int var3);

    public void forEachValue(Consumer<T> var1);

    public void writePacket(PacketByteBuf var1);

    public int getPacketSize();

    @VisibleForTesting
    public int getElementBits();

    public boolean hasAny(Predicate<T> var1);

    public void count(PalettedContainer.Counter<T> var1);

    public PalettedContainer<T> copy();

    public PalettedContainer<T> slice();

    public Serialized<T> serialize(PaletteProvider<T> var1);

    public static interface Reader<T, C extends ReadableContainer<T>> {
        public DataResult<C> read(PaletteProvider<T> var1, Serialized<T> var2);
    }

    public record Serialized<T>(List<T> paletteEntries, Optional<LongStream> storage, int bitsPerEntry) {
        public static final int MISSING_BITS_PER_ENTRY = -1;

        public Serialized(List<T> paletteEntries, Optional<LongStream> storage) {
            this(paletteEntries, storage, -1);
        }
    }
}

