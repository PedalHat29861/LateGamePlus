/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.dynamic;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtElement;

public class CodecCache {
    final LoadingCache<Key<?, ?>, DataResult<?>> cache;

    public CodecCache(int size) {
        this.cache = CacheBuilder.newBuilder().maximumSize((long)size).concurrencyLevel(1).softValues().build(new CacheLoader<Key<?, ?>, DataResult<?>>(this){

            public DataResult<?> load(Key<?, ?> key) {
                return key.encode();
            }

            public /* synthetic */ Object load(Object key) throws Exception {
                return this.load((Key)key);
            }
        });
    }

    public <A> Codec<A> wrap(final Codec<A> codec) {
        return new Codec<A>(){

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                return codec.decode(ops, input);
            }

            public <T> DataResult<T> encode(A value, DynamicOps<T> ops, T prefix) {
                return ((DataResult)CodecCache.this.cache.getUnchecked(new Key(codec, value, ops))).map(object -> {
                    if (object instanceof NbtElement) {
                        NbtElement nbtElement = (NbtElement)object;
                        return nbtElement.copy();
                    }
                    return object;
                });
            }
        };
    }

    record Key<A, T>(Codec<A> codec, A value, DynamicOps<T> ops) {
        public DataResult<T> encode() {
            return this.codec.encodeStart(this.ops, this.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Key) {
                Key key = (Key)o;
                return this.codec == key.codec && this.value.equals(key.value) && this.ops.equals(key.ops);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int i = System.identityHashCode(this.codec);
            i = 31 * i + this.value.hashCode();
            i = 31 * i + this.ops.hashCode();
            return i;
        }
    }
}

