/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

public class CachedMapper<K, V> {
    private final Function<K, V> mapper;
    private @Nullable K cachedInput = null;
    private @Nullable V cachedOutput;

    public CachedMapper(Function<K, V> mapper) {
        this.mapper = mapper;
    }

    public V map(K input) {
        if (this.cachedOutput == null || !Objects.equals(this.cachedInput, input)) {
            this.cachedOutput = this.mapper.apply(input);
            this.cachedInput = input;
        }
        return this.cachedOutput;
    }
}

