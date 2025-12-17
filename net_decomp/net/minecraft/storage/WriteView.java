/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.Nullable;

public interface WriteView {
    public <T> void put(String var1, Codec<T> var2, T var3);

    public <T> void putNullable(String var1, Codec<T> var2, @Nullable T var3);

    @Deprecated
    public <T> void put(MapCodec<T> var1, T var2);

    public void putBoolean(String var1, boolean var2);

    public void putByte(String var1, byte var2);

    public void putShort(String var1, short var2);

    public void putInt(String var1, int var2);

    public void putLong(String var1, long var2);

    public void putFloat(String var1, float var2);

    public void putDouble(String var1, double var2);

    public void putString(String var1, String var2);

    public void putIntArray(String var1, int[] var2);

    public WriteView get(String var1);

    public ListView getList(String var1);

    public <T> ListAppender<T> getListAppender(String var1, Codec<T> var2);

    public void remove(String var1);

    public boolean isEmpty();

    public static interface ListAppender<T> {
        public void add(T var1);

        public boolean isEmpty();
    }

    public static interface ListView {
        public WriteView add();

        public void removeLast();

        public boolean isEmpty();
    }
}

