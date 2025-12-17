/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.registry;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

public class RegistryOps<T>
extends ForwardingDynamicOps<T> {
    private final RegistryInfoGetter registryInfoGetter;

    public static <T> RegistryOps<T> of(DynamicOps<T> delegate, RegistryWrapper.WrapperLookup registries) {
        return RegistryOps.of(delegate, new CachedRegistryInfoGetter(registries));
    }

    public static <T> RegistryOps<T> of(DynamicOps<T> delegate, RegistryInfoGetter registryInfoGetter) {
        return new RegistryOps<T>(delegate, registryInfoGetter);
    }

    public static <T> Dynamic<T> withRegistry(Dynamic<T> dynamic, RegistryWrapper.WrapperLookup registries) {
        return new Dynamic(registries.getOps(dynamic.getOps()), dynamic.getValue());
    }

    private RegistryOps(DynamicOps<T> delegate, RegistryInfoGetter registryInfoGetter) {
        super(delegate);
        this.registryInfoGetter = registryInfoGetter;
    }

    public <U> RegistryOps<U> withDelegate(DynamicOps<U> delegate) {
        if (delegate == this.delegate) {
            return this;
        }
        return new RegistryOps<U>(delegate, this.registryInfoGetter);
    }

    public <E> Optional<RegistryEntryOwner<E>> getOwner(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return this.registryInfoGetter.getRegistryInfo(registryRef).map(RegistryInfo::owner);
    }

    public <E> Optional<RegistryEntryLookup<E>> getEntryLookup(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return this.registryInfoGetter.getRegistryInfo(registryRef).map(RegistryInfo::entryLookup);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RegistryOps registryOps = (RegistryOps)o;
        return this.delegate.equals((Object)registryOps.delegate) && this.registryInfoGetter.equals(registryOps.registryInfoGetter);
    }

    public int hashCode() {
        return this.delegate.hashCode() * 31 + this.registryInfoGetter.hashCode();
    }

    public static <E, O> RecordCodecBuilder<O, RegistryEntryLookup<E>> getEntryLookupCodec(RegistryKey<? extends Registry<? extends E>> registryRef) {
        return Codecs.createContextRetrievalCodec(ops -> {
            if (ops instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)ops;
                return registryOps.registryInfoGetter.getRegistryInfo(registryRef).map(info -> DataResult.success(info.entryLookup(), (Lifecycle)info.elementsLifecycle())).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + String.valueOf(registryRef)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter(object -> null);
    }

    public static <E, O> RecordCodecBuilder<O, RegistryEntry.Reference<E>> getEntryCodec(RegistryKey<E> key) {
        RegistryKey registryKey = RegistryKey.ofRegistry(key.getRegistry());
        return Codecs.createContextRetrievalCodec(ops -> {
            if (ops instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)ops;
                return registryOps.registryInfoGetter.getRegistryInfo(registryKey).flatMap(info -> info.entryLookup().getOptional(key)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Can't find value: " + String.valueOf(key)));
            }
            return DataResult.error(() -> "Not a registry ops");
        }).forGetter(object -> null);
    }

    static final class CachedRegistryInfoGetter
    implements RegistryInfoGetter {
        private final RegistryWrapper.WrapperLookup registries;
        private final Map<RegistryKey<? extends Registry<?>>, Optional<? extends RegistryInfo<?>>> cache = new ConcurrentHashMap();

        public CachedRegistryInfoGetter(RegistryWrapper.WrapperLookup registries) {
            this.registries = registries;
        }

        public <E> Optional<RegistryInfo<E>> getRegistryInfo(RegistryKey<? extends Registry<? extends E>> registryRef) {
            return this.cache.computeIfAbsent(registryRef, this::compute);
        }

        private Optional<RegistryInfo<Object>> compute(RegistryKey<? extends Registry<?>> registryRef) {
            return this.registries.getOptional(registryRef).map(RegistryInfo::fromWrapper);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CachedRegistryInfoGetter)) return false;
            CachedRegistryInfoGetter cachedRegistryInfoGetter = (CachedRegistryInfoGetter)o;
            if (!this.registries.equals(cachedRegistryInfoGetter.registries)) return false;
            return true;
        }

        public int hashCode() {
            return this.registries.hashCode();
        }
    }

    public static interface RegistryInfoGetter {
        public <T> Optional<RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> var1);
    }

    public record RegistryInfo<T>(RegistryEntryOwner<T> owner, RegistryEntryLookup<T> entryLookup, Lifecycle elementsLifecycle) {
        public static <T> RegistryInfo<T> fromWrapper(RegistryWrapper.Impl<T> wrapper) {
            return new RegistryInfo<T>(wrapper, wrapper, wrapper.getLifecycle());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryInfo.class, "owner;getter;elementsLifecycle", "owner", "entryLookup", "elementsLifecycle"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryInfo.class, "owner;getter;elementsLifecycle", "owner", "entryLookup", "elementsLifecycle"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryInfo.class, "owner;getter;elementsLifecycle", "owner", "entryLookup", "elementsLifecycle"}, this, object);
        }
    }
}

