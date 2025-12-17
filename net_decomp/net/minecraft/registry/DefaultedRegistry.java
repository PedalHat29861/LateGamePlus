/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.NonNull
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface DefaultedRegistry<T>
extends Registry<T> {
    @Override
    public @NonNull Identifier getId(T var1);

    @Override
    public @NonNull T get(@Nullable Identifier var1);

    @Override
    public @NonNull T get(int var1);

    public Identifier getDefaultId();
}

