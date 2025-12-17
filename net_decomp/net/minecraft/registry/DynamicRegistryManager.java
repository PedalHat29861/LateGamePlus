/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;

public interface DynamicRegistryManager
extends RegistryWrapper.WrapperLookup {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Immutable EMPTY = new ImmutableImpl(Map.of()).toImmutable();

    public <E> Optional<Registry<E>> getOptional(RegistryKey<? extends Registry<? extends E>> var1);

    default public <E> Registry<E> getOrThrow(RegistryKey<? extends Registry<? extends E>> key) {
        return this.getOptional(key).orElseThrow(() -> new IllegalStateException("Missing registry: " + String.valueOf(key)));
    }

    public Stream<Entry<?>> streamAllRegistries();

    @Override
    default public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
        return this.streamAllRegistries().map(registry -> registry.key);
    }

    public static Immutable of(final Registry<? extends Registry<?>> registries) {
        return new Immutable(){

            public <T> Optional<Registry<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
                Registry registry = registries;
                return registry.getOptionalValue(registryRef);
            }

            @Override
            public Stream<Entry<?>> streamAllRegistries() {
                return registries.getEntrySet().stream().map(Entry::of);
            }

            @Override
            public Immutable toImmutable() {
                return this;
            }
        };
    }

    default public Immutable toImmutable() {
        class Immutablized
        extends ImmutableImpl
        implements Immutable {
            protected Immutablized(DynamicRegistryManager dynamicRegistryManager, Stream<Entry<?>> entryStream) {
                super(entryStream);
            }
        }
        return new Immutablized(this, this.streamAllRegistries().map(Entry::freeze));
    }

    @Override
    default public /* synthetic */ RegistryWrapper.Impl getOrThrow(RegistryKey registryRef) {
        return this.getOrThrow(registryRef);
    }

    @Override
    default public /* synthetic */ RegistryEntryLookup getOrThrow(RegistryKey registryRef) {
        return this.getOrThrow(registryRef);
    }

    public static final class Entry<T>
    extends Record {
        final RegistryKey<? extends Registry<T>> key;
        private final Registry<T> value;

        public Entry(RegistryKey<? extends Registry<T>> key, Registry<T> value) {
            this.key = key;
            this.value = value;
        }

        private static <T, R extends Registry<? extends T>> Entry<T> of(Map.Entry<? extends RegistryKey<? extends Registry<?>>, R> entry) {
            return Entry.of(entry.getKey(), (Registry)entry.getValue());
        }

        private static <T> Entry<T> of(RegistryKey<? extends Registry<?>> key, Registry<?> value) {
            return new Entry(key, value);
        }

        private Entry<T> freeze() {
            return new Entry<T>(this.key, this.value.freeze());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this, object);
        }

        public RegistryKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Registry<T> value() {
            return this.value;
        }
    }

    public static class ImmutableImpl
    implements DynamicRegistryManager {
        private final Map<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableImpl(List<? extends Registry<?>> registries) {
            this.registries = registries.stream().collect(Collectors.toUnmodifiableMap(Registry::getKey, registry -> registry));
        }

        public ImmutableImpl(Map<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> registries) {
            this.registries = Map.copyOf(registries);
        }

        public ImmutableImpl(Stream<Entry<?>> entryStream) {
            this.registries = (Map)entryStream.collect(ImmutableMap.toImmutableMap(Entry::key, Entry::value));
        }

        @Override
        public <E> Optional<Registry<E>> getOptional(RegistryKey<? extends Registry<? extends E>> registryRef) {
            return Optional.ofNullable(this.registries.get(registryRef)).map(registry -> registry);
        }

        @Override
        public Stream<Entry<?>> streamAllRegistries() {
            return this.registries.entrySet().stream().map(Entry::of);
        }
    }

    public static interface Immutable
    extends DynamicRegistryManager {
    }
}

