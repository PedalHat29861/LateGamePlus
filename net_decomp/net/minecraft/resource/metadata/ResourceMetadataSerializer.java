/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.resource.metadata;

import com.mojang.serialization.Codec;
import java.util.Optional;

public record ResourceMetadataSerializer<T>(String name, Codec<T> codec) {
    public Value<T> value(T value) {
        return new Value<T>(this, value);
    }

    public record Value<T>(ResourceMetadataSerializer<T> type, T value) {
        public <U> Optional<U> getValueIfMatching(ResourceMetadataSerializer<U> o) {
            return o == this.type ? Optional.of(this.value) : Optional.empty();
        }
    }
}

