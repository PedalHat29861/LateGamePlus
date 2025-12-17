/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.tick;

import java.util.List;
import net.minecraft.world.tick.Tick;

public interface SerializableTickScheduler<T> {
    public List<Tick<T>> collectTicks(long var1);
}

