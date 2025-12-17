/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.ai.brain;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.annotation.Debug;

public class Memory<T> {
    private final T value;
    private long expiry;

    public Memory(T value, long expiry) {
        this.value = value;
        this.expiry = expiry;
    }

    public void tick() {
        if (this.isTimed()) {
            --this.expiry;
        }
    }

    public static <T> Memory<T> permanent(T value) {
        return new Memory<T>(value, Long.MAX_VALUE);
    }

    public static <T> Memory<T> timed(T value, long expiry) {
        return new Memory<T>(value, expiry);
    }

    public long getExpiry() {
        return this.expiry;
    }

    public T getValue() {
        return this.value;
    }

    public boolean isExpired() {
        return this.expiry <= 0L;
    }

    public String toString() {
        return String.valueOf(this.value) + (String)(this.isTimed() ? " (ttl: " + this.expiry + ")" : "");
    }

    @Debug
    public boolean isTimed() {
        return this.expiry != Long.MAX_VALUE;
    }

    public static <T> Codec<Memory<T>> createCodec(Codec<T> codec) {
        return RecordCodecBuilder.create(instance -> instance.group((App)codec.fieldOf("value").forGetter(memory -> memory.value), (App)Codec.LONG.lenientOptionalFieldOf("ttl").forGetter(memory -> memory.isTimed() ? Optional.of(memory.expiry) : Optional.empty())).apply((Applicative)instance, (value, expiry) -> new Memory<Object>(value, expiry.orElse(Long.MAX_VALUE))));
    }
}

