/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.registry;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;

public interface RegistryWrapper<T>
extends RegistryEntryLookup<T> {
    public Stream<RegistryEntry.Reference<T>> streamEntries();

    default public Stream<RegistryKey<T>> streamKeys() {
        return this.streamEntries().map(RegistryEntry.Reference::registryKey);
    }

    public Stream<RegistryEntryList.Named<T>> getTags();

    default public Stream<TagKey<T>> streamTagKeys() {
        return this.getTags().map(RegistryEntryList.Named::getTag);
    }

    public static interface WrapperLookup
    extends RegistryEntryLookup.RegistryLookup {
        public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys();

        default public Stream<Impl<?>> stream() {
            return this.streamAllRegistryKeys().map(registryRef -> this.getOrThrow((RegistryKey)registryRef));
        }

        public <T> Optional<? extends Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> var1);

        default public <T> Impl<T> getOrThrow(RegistryKey<? extends Registry<? extends T>> registryRef) {
            return this.getOptional(registryRef).orElseThrow(() -> new IllegalStateException("Registry " + String.valueOf(registryRef.getValue()) + " not found"));
        }

        default public <V> RegistryOps<V> getOps(DynamicOps<V> delegate) {
            return RegistryOps.of(delegate, this);
        }

        public static WrapperLookup of(Stream<Impl<?>> wrappers) {
            final Map<RegistryKey, Impl> map = wrappers.collect(Collectors.toUnmodifiableMap(Impl::getKey, wrapper -> wrapper));
            return new WrapperLookup(){

                @Override
                public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
                    return map.keySet().stream();
                }

                public <T> Optional<Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
                    return Optional.ofNullable((Impl)map.get(registryRef));
                }
            };
        }

        default public Lifecycle getLifecycle() {
            return this.stream().map(Impl::getLifecycle).reduce(Lifecycle.stable(), Lifecycle::add);
        }
    }

    public static interface Impl<T>
    extends RegistryWrapper<T>,
    RegistryEntryOwner<T> {
        public RegistryKey<? extends Registry<? extends T>> getKey();

        public Lifecycle getLifecycle();

        default public Impl<T> withFeatureFilter(FeatureSet enabledFeatures) {
            if (ToggleableFeature.FEATURE_ENABLED_REGISTRY_KEYS.contains(this.getKey())) {
                return this.withPredicateFilter(feature -> ((ToggleableFeature)feature).isEnabled(enabledFeatures));
            }
            return this;
        }

        default public Impl<T> withPredicateFilter(final Predicate<T> predicate) {
            return new Delegating<T>(){

                @Override
                public Impl<T> getBase() {
                    return this;
                }

                @Override
                public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                    return this.getBase().getOptional(key).filter(entry -> predicate.test(entry.value()));
                }

                @Override
                public Stream<RegistryEntry.Reference<T>> streamEntries() {
                    return this.getBase().streamEntries().filter(entry -> predicate.test(entry.value()));
                }
            };
        }

        public static interface Delegating<T>
        extends Impl<T> {
            public Impl<T> getBase();

            @Override
            default public RegistryKey<? extends Registry<? extends T>> getKey() {
                return this.getBase().getKey();
            }

            @Override
            default public Lifecycle getLifecycle() {
                return this.getBase().getLifecycle();
            }

            @Override
            default public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return this.getBase().getOptional(key);
            }

            @Override
            default public Stream<RegistryEntry.Reference<T>> streamEntries() {
                return this.getBase().streamEntries();
            }

            @Override
            default public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                return this.getBase().getOptional(tag);
            }

            @Override
            default public Stream<RegistryEntryList.Named<T>> getTags() {
                return this.getBase().getTags();
            }
        }
    }
}

