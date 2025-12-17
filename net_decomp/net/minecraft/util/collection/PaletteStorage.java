/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.collection;

import java.util.function.IntConsumer;

public interface PaletteStorage {
    public int swap(int var1, int var2);

    public void set(int var1, int var2);

    public int get(int var1);

    public long[] getData();

    public int getSize();

    public int getElementBits();

    public void forEach(IntConsumer var1);

    public void writePaletteIndices(int[] var1);

    public PaletteStorage copy();
}

