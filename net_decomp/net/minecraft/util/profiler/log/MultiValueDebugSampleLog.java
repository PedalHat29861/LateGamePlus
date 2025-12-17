/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler.log;

public interface MultiValueDebugSampleLog {
    public int getDimension();

    public int getLength();

    public long get(int var1);

    public long get(int var1, int var2);

    public void clear();
}

