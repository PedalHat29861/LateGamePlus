/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityLike;
import org.jspecify.annotations.Nullable;

public interface EntityLookup<T extends EntityLike> {
    public @Nullable T get(int var1);

    public @Nullable T get(UUID var1);

    public Iterable<T> iterate();

    public <U extends T> void forEach(TypeFilter<T, U> var1, LazyIterationConsumer<U> var2);

    public void forEachIntersects(Box var1, Consumer<T> var2);

    public <U extends T> void forEachIntersects(TypeFilter<T, U> var1, Box var2, LazyIterationConsumer<U> var3);
}

