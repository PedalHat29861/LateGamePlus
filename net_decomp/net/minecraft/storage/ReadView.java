/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryWrapper;

public interface ReadView {
    public <T> Optional<T> read(String var1, Codec<T> var2);

    @Deprecated
    public <T> Optional<T> read(MapCodec<T> var1);

    public Optional<ReadView> getOptionalReadView(String var1);

    public ReadView getReadView(String var1);

    public Optional<ListReadView> getOptionalListReadView(String var1);

    public ListReadView getListReadView(String var1);

    public <T> Optional<TypedListReadView<T>> getOptionalTypedListView(String var1, Codec<T> var2);

    public <T> TypedListReadView<T> getTypedListView(String var1, Codec<T> var2);

    public boolean getBoolean(String var1, boolean var2);

    public byte getByte(String var1, byte var2);

    public int getShort(String var1, short var2);

    public Optional<Integer> getOptionalInt(String var1);

    public int getInt(String var1, int var2);

    public long getLong(String var1, long var2);

    public Optional<Long> getOptionalLong(String var1);

    public float getFloat(String var1, float var2);

    public double getDouble(String var1, double var2);

    public Optional<String> getOptionalString(String var1);

    public String getString(String var1, String var2);

    public Optional<int[]> getOptionalIntArray(String var1);

    @Deprecated
    public RegistryWrapper.WrapperLookup getRegistries();

    public static interface TypedListReadView<T>
    extends Iterable<T> {
        public boolean isEmpty();

        public Stream<T> stream();
    }

    public static interface ListReadView
    extends Iterable<ReadView> {
        public boolean isEmpty();

        public Stream<ReadView> stream();
    }
}

