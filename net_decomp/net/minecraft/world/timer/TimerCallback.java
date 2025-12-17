/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.timer;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.timer.Timer;

public interface TimerCallback<T> {
    public void call(T var1, Timer<T> var2, long var3);

    public MapCodec<? extends TimerCallback<T>> getCodec();
}

