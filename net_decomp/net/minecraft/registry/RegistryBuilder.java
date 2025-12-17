/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCloner;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class RegistryBuilder {
    private final List<RegistryInfo<?>> registries = new ArrayList();

    static <T> RegistryEntryLookup<T> toLookup(final RegistryWrapper.Impl<T> wrapper) {
        return new EntryListCreatingLookup<T>(wrapper){

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return wrapper.getOptional(key);
            }
        };
    }

    static <T> RegistryWrapper.Impl<T> createWrapper(final RegistryKey<? extends Registry<? extends T>> registryRef, final Lifecycle lifecycle, RegistryEntryOwner<T> owner, final Map<RegistryKey<T>, RegistryEntry.Reference<T>> entries) {
        return new UntaggedLookup<T>(owner){

            @Override
            public RegistryKey<? extends Registry<? extends T>> getKey() {
                return registryRef;
            }

            @Override
            public Lifecycle getLifecycle() {
                return lifecycle;
            }

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return Optional.ofNullable((RegistryEntry.Reference)entries.get(key));
            }

            @Override
            public Stream<RegistryEntry.Reference<T>> streamEntries() {
                return entries.values().stream();
            }
        };
    }

    public <T> RegistryBuilder addRegistry(RegistryKey<? extends Registry<T>> registryRef, Lifecycle lifecycle, BootstrapFunction<T> bootstrapFunction) {
        this.registries.add(new RegistryInfo<T>(registryRef, lifecycle, bootstrapFunction));
        return this;
    }

    public <T> RegistryBuilder addRegistry(RegistryKey<? extends Registry<T>> registryRef, BootstrapFunction<T> bootstrapFunction) {
        return this.addRegistry(registryRef, Lifecycle.stable(), bootstrapFunction);
    }

    private Registries createBootstrappedRegistries(DynamicRegistryManager registryManager) {
        Registries registries = Registries.of(registryManager, this.registries.stream().map(RegistryInfo::key));
        this.registries.forEach(registry -> registry.runBootstrap(registries));
        return registries;
    }

    private static RegistryWrapper.WrapperLookup createWrapperLookup(AnyOwner entryOwner, DynamicRegistryManager registryManager, Stream<RegistryWrapper.Impl<?>> wrappers) {
        record WrapperInfoPair<T>(RegistryWrapper.Impl<T> lookup, RegistryOps.RegistryInfo<T> opsInfo) {
            public static <T> WrapperInfoPair<T> of(RegistryWrapper.Impl<T> wrapper) {
                return new WrapperInfoPair<T>(new UntaggedDelegatingLookup<T>(wrapper, wrapper), RegistryOps.RegistryInfo.fromWrapper(wrapper));
            }

            public static <T> WrapperInfoPair<T> of(AnyOwner owner, RegistryWrapper.Impl<T> wrapper) {
                return new WrapperInfoPair(new UntaggedDelegatingLookup(owner.downcast(), wrapper), new RegistryOps.RegistryInfo(owner.downcast(), wrapper, wrapper.getLifecycle()));
            }
        }
        final HashMap map = new HashMap();
        registryManager.streamAllRegistries().forEach(registry -> map.put(registry.key(), WrapperInfoPair.of(registry.value())));
        wrappers.forEach(wrapper -> map.put(wrapper.getKey(), WrapperInfoPair.of(entryOwner, wrapper)));
        return new RegistryWrapper.WrapperLookup(){

            @Override
            public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
                return map.keySet().stream();
            }

            <T> Optional<WrapperInfoPair<T>> get(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((WrapperInfoPair)map.get(registryRef));
            }

            public <T> Optional<RegistryWrapper.Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return this.get(registryRef).map(WrapperInfoPair::lookup);
            }

            @Override
            public <V> RegistryOps<V> getOps(DynamicOps<V> delegate) {
                return RegistryOps.of(delegate, new RegistryOps.RegistryInfoGetter(){

                    @Override
                    public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                        return this.get(registryRef).map(WrapperInfoPair::opsInfo);
                    }
                });
            }
        };
    }

    public RegistryWrapper.WrapperLookup createWrapperLookup(DynamicRegistryManager registryManager) {
        Registries registries = this.createBootstrappedRegistries(registryManager);
        Stream<RegistryWrapper.Impl<?>> stream = this.registries.stream().map(info -> info.init(registries).toWrapper(registries.owner));
        RegistryWrapper.WrapperLookup wrapperLookup = RegistryBuilder.createWrapperLookup(registries.owner, registryManager, stream);
        registries.checkUnreferencedKeys();
        registries.checkOrphanedValues();
        registries.throwErrors();
        return wrapperLookup;
    }

    private RegistryWrapper.WrapperLookup createFullWrapperLookup(DynamicRegistryManager registryManager, RegistryWrapper.WrapperLookup base, RegistryCloner.CloneableRegistries cloneableRegistries, Map<RegistryKey<? extends Registry<?>>, InitializedRegistry<?>> initializedRegistries, RegistryWrapper.WrapperLookup patches) {
        AnyOwner anyOwner = new AnyOwner();
        MutableObject mutableObject = new MutableObject();
        List list = initializedRegistries.keySet().stream().map(registryRef -> this.applyPatches(anyOwner, cloneableRegistries, (RegistryKey)registryRef, patches, base, (MutableObject<RegistryWrapper.WrapperLookup>)mutableObject)).collect(Collectors.toUnmodifiableList());
        RegistryWrapper.WrapperLookup wrapperLookup = RegistryBuilder.createWrapperLookup(anyOwner, registryManager, list.stream());
        mutableObject.setValue((Object)wrapperLookup);
        return wrapperLookup;
    }

    private <T> RegistryWrapper.Impl<T> applyPatches(RegistryEntryOwner<T> owner, RegistryCloner.CloneableRegistries cloneableRegistries, RegistryKey<? extends Registry<? extends T>> registryRef, RegistryWrapper.WrapperLookup patches, RegistryWrapper.WrapperLookup base, MutableObject<RegistryWrapper.WrapperLookup> lazyWrapper) {
        RegistryCloner registryCloner = cloneableRegistries.get(registryRef);
        if (registryCloner == null) {
            throw new NullPointerException("No cloner for " + String.valueOf(registryRef.getValue()));
        }
        HashMap map = new HashMap();
        RegistryEntryLookup impl = patches.getOrThrow(registryRef);
        impl.streamEntries().forEach(entry -> {
            RegistryKey registryKey = entry.registryKey();
            LazyReferenceEntry lazyReferenceEntry = new LazyReferenceEntry(owner, registryKey);
            lazyReferenceEntry.supplier = () -> registryCloner.clone(entry.value(), patches, (RegistryWrapper.WrapperLookup)lazyWrapper.get());
            map.put(registryKey, lazyReferenceEntry);
        });
        RegistryEntryLookup impl2 = base.getOrThrow(registryRef);
        impl2.streamEntries().forEach(entry -> {
            RegistryKey registryKey = entry.registryKey();
            map.computeIfAbsent(registryKey, key -> {
                LazyReferenceEntry lazyReferenceEntry = new LazyReferenceEntry(owner, registryKey);
                lazyReferenceEntry.supplier = () -> registryCloner.clone(entry.value(), base, (RegistryWrapper.WrapperLookup)lazyWrapper.get());
                return lazyReferenceEntry;
            });
        });
        Lifecycle lifecycle = impl.getLifecycle().add(impl2.getLifecycle());
        return RegistryBuilder.createWrapper(registryRef, lifecycle, owner, map);
    }

    public FullPatchesRegistriesPair createWrapperLookup(DynamicRegistryManager baseRegistryManager, RegistryWrapper.WrapperLookup registries, RegistryCloner.CloneableRegistries cloneableRegistries) {
        Registries registries2 = this.createBootstrappedRegistries(baseRegistryManager);
        HashMap map = new HashMap();
        this.registries.stream().map(info -> info.init(registries2)).forEach(registry -> map.put((RegistryKey<Registry<?>>)registry.key, (InitializedRegistry<?>)registry));
        Set set = baseRegistryManager.streamAllRegistryKeys().collect(Collectors.toUnmodifiableSet());
        registries.streamAllRegistryKeys().filter(key -> !set.contains(key)).forEach(key -> map.putIfAbsent((RegistryKey<Registry<?>>)key, new InitializedRegistry(key, Lifecycle.stable(), Map.of())));
        Stream<RegistryWrapper.Impl<?>> stream = map.values().stream().map(registry -> registry.toWrapper(registries.owner));
        RegistryWrapper.WrapperLookup wrapperLookup = RegistryBuilder.createWrapperLookup(registries2.owner, baseRegistryManager, stream);
        registries2.checkOrphanedValues();
        registries2.throwErrors();
        RegistryWrapper.WrapperLookup wrapperLookup2 = this.createFullWrapperLookup(baseRegistryManager, registries, cloneableRegistries, map, wrapperLookup);
        return new FullPatchesRegistriesPair(wrapperLookup2, wrapperLookup);
    }

    record RegistryInfo<T>(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, BootstrapFunction<T> bootstrap) {
        void runBootstrap(Registries registries) {
            this.bootstrap.run(registries.createRegisterable());
        }

        public InitializedRegistry<T> init(Registries registries) {
            HashMap map = new HashMap();
            Iterator<Map.Entry<RegistryKey<?>, RegisteredValue<?>>> iterator = registries.registeredValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<RegistryKey<?>, RegisteredValue<?>> entry = iterator.next();
                RegistryKey<?> registryKey = entry.getKey();
                if (!registryKey.isOf(this.key)) continue;
                RegistryKey<?> registryKey2 = registryKey;
                RegisteredValue<?> registeredValue = entry.getValue();
                RegistryEntry.Reference<Object> reference = registries.lookup.keysToEntries.remove(registryKey);
                map.put(registryKey2, new EntryAssociatedValue(registeredValue, Optional.ofNullable(reference)));
                iterator.remove();
            }
            return new InitializedRegistry(this.key, this.lifecycle, map);
        }
    }

    @FunctionalInterface
    public static interface BootstrapFunction<T> {
        public void run(Registerable<T> var1);
    }

    static final class Registries
    extends Record {
        final AnyOwner owner;
        final StandAloneEntryCreatingLookup lookup;
        final Map<Identifier, RegistryEntryLookup<?>> registries;
        final Map<RegistryKey<?>, RegisteredValue<?>> registeredValues;
        final List<RuntimeException> errors;

        private Registries(AnyOwner owner, StandAloneEntryCreatingLookup lookup, Map<Identifier, RegistryEntryLookup<?>> registries, Map<RegistryKey<?>, RegisteredValue<?>> registeredValues, List<RuntimeException> errors) {
            this.owner = owner;
            this.lookup = lookup;
            this.registries = registries;
            this.registeredValues = registeredValues;
            this.errors = errors;
        }

        public static Registries of(DynamicRegistryManager dynamicRegistryManager, Stream<RegistryKey<? extends Registry<?>>> registryRefs) {
            AnyOwner anyOwner = new AnyOwner();
            ArrayList<RuntimeException> list = new ArrayList<RuntimeException>();
            StandAloneEntryCreatingLookup standAloneEntryCreatingLookup = new StandAloneEntryCreatingLookup(anyOwner);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            dynamicRegistryManager.streamAllRegistries().forEach(entry -> builder.put((Object)entry.key().getValue(), RegistryBuilder.toLookup(entry.value())));
            registryRefs.forEach(registryRef -> builder.put((Object)registryRef.getValue(), (Object)standAloneEntryCreatingLookup));
            return new Registries(anyOwner, standAloneEntryCreatingLookup, (Map<Identifier, RegistryEntryLookup<?>>)builder.build(), new HashMap(), (List<RuntimeException>)list);
        }

        public <T> Registerable<T> createRegisterable() {
            return new Registerable<T>(){

                @Override
                public RegistryEntry.Reference<T> register(RegistryKey<T> key, T value, Lifecycle lifecycle) {
                    RegisteredValue registeredValue = registeredValues.put(key, new RegisteredValue(value, lifecycle));
                    if (registeredValue != null) {
                        errors.add(new IllegalStateException("Duplicate registration for " + String.valueOf(key) + ", new=" + String.valueOf(value) + ", old=" + String.valueOf(registeredValue.value)));
                    }
                    return lookup.getOrCreate(key);
                }

                @Override
                public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
                    return registries.getOrDefault(registryRef.getValue(), lookup);
                }
            };
        }

        public void checkOrphanedValues() {
            this.registeredValues.forEach((key, value) -> this.errors.add(new IllegalStateException("Orpaned value " + String.valueOf(value.value) + " for key " + String.valueOf(key))));
        }

        public void checkUnreferencedKeys() {
            for (RegistryKey<Object> registryKey : this.lookup.keysToEntries.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + String.valueOf(registryKey)));
            }
        }

        public void throwErrors() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");
                for (RuntimeException runtimeException : this.errors) {
                    illegalStateException.addSuppressed(runtimeException);
                }
                throw illegalStateException;
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Registries.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Registries.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Registries.class, "owner;lookup;registries;registeredValues;errors", "owner", "lookup", "registries", "registeredValues", "errors"}, this, object);
        }

        public AnyOwner owner() {
            return this.owner;
        }

        public StandAloneEntryCreatingLookup lookup() {
            return this.lookup;
        }

        public Map<Identifier, RegistryEntryLookup<?>> registries() {
            return this.registries;
        }

        public Map<RegistryKey<?>, RegisteredValue<?>> registeredValues() {
            return this.registeredValues;
        }

        public List<RuntimeException> errors() {
            return this.errors;
        }
    }

    static class AnyOwner
    implements RegistryEntryOwner<Object> {
        AnyOwner() {
        }

        public <T> RegistryEntryOwner<T> downcast() {
            return this;
        }
    }

    public record FullPatchesRegistriesPair(RegistryWrapper.WrapperLookup full, RegistryWrapper.WrapperLookup patches) {
    }

    static final class InitializedRegistry<T>
    extends Record {
        final RegistryKey<? extends Registry<? extends T>> key;
        private final Lifecycle lifecycle;
        private final Map<RegistryKey<T>, EntryAssociatedValue<T>> values;

        InitializedRegistry(RegistryKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<RegistryKey<T>, EntryAssociatedValue<T>> values) {
            this.key = key;
            this.lifecycle = lifecycle;
            this.values = values;
        }

        public RegistryWrapper.Impl<T> toWrapper(AnyOwner anyOwner) {
            Map map = this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> {
                EntryAssociatedValue entryAssociatedValue = (EntryAssociatedValue)entry.getValue();
                RegistryEntry.Reference reference = entryAssociatedValue.entry().orElseGet(() -> RegistryEntry.Reference.standAlone(anyOwner.downcast(), (RegistryKey)entry.getKey()));
                reference.setValue(entryAssociatedValue.value().value());
                return reference;
            }));
            return RegistryBuilder.createWrapper(this.key, this.lifecycle, anyOwner.downcast(), map);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{InitializedRegistry.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{InitializedRegistry.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{InitializedRegistry.class, "key;lifecycle;values", "key", "lifecycle", "values"}, this, object);
        }

        public RegistryKey<? extends Registry<? extends T>> key() {
            return this.key;
        }

        public Lifecycle lifecycle() {
            return this.lifecycle;
        }

        public Map<RegistryKey<T>, EntryAssociatedValue<T>> values() {
            return this.values;
        }
    }

    static class LazyReferenceEntry<T>
    extends RegistryEntry.Reference<T> {
        @Nullable Supplier<T> supplier;

        protected LazyReferenceEntry(RegistryEntryOwner<T> owner, @Nullable RegistryKey<T> key) {
            super(RegistryEntry.Reference.Type.STAND_ALONE, owner, key, null);
        }

        @Override
        protected void setValue(T value) {
            super.setValue(value);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.setValue(this.supplier.get());
            }
            return super.value();
        }
    }

    record EntryAssociatedValue<T>(RegisteredValue<T> value, Optional<RegistryEntry.Reference<T>> entry) {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntryAssociatedValue.class, "value;holder", "value", "entry"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryAssociatedValue.class, "value;holder", "value", "entry"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryAssociatedValue.class, "value;holder", "value", "entry"}, this, object);
        }
    }

    static final class RegisteredValue<T>
    extends Record {
        final T value;
        private final Lifecycle lifecycle;

        RegisteredValue(T value, Lifecycle lifecycle) {
            this.value = value;
            this.lifecycle = lifecycle;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegisteredValue.class, "value;lifecycle", "value", "lifecycle"}, this, object);
        }

        public T value() {
            return this.value;
        }

        public Lifecycle lifecycle() {
            return this.lifecycle;
        }
    }

    static class StandAloneEntryCreatingLookup
    extends EntryListCreatingLookup<Object> {
        final Map<RegistryKey<Object>, RegistryEntry.Reference<Object>> keysToEntries = new HashMap<RegistryKey<Object>, RegistryEntry.Reference<Object>>();

        public StandAloneEntryCreatingLookup(RegistryEntryOwner<Object> registryEntryOwner) {
            super(registryEntryOwner);
        }

        @Override
        public Optional<RegistryEntry.Reference<Object>> getOptional(RegistryKey<Object> key) {
            return Optional.of(this.getOrCreate(key));
        }

        <T> RegistryEntry.Reference<T> getOrCreate(RegistryKey<T> key) {
            return this.keysToEntries.computeIfAbsent(key, key2 -> RegistryEntry.Reference.standAlone(this.entryOwner, key2));
        }
    }

    static class UntaggedDelegatingLookup<T>
    extends UntaggedLookup<T>
    implements RegistryWrapper.Impl.Delegating<T> {
        private final RegistryWrapper.Impl<T> base;

        UntaggedDelegatingLookup(RegistryEntryOwner<T> entryOwner, RegistryWrapper.Impl<T> base) {
            super(entryOwner);
            this.base = base;
        }

        @Override
        public RegistryWrapper.Impl<T> getBase() {
            return this.base;
        }
    }

    static abstract class UntaggedLookup<T>
    extends EntryListCreatingLookup<T>
    implements RegistryWrapper.Impl<T> {
        protected UntaggedLookup(RegistryEntryOwner<T> registryEntryOwner) {
            super(registryEntryOwner);
        }

        @Override
        public Stream<RegistryEntryList.Named<T>> getTags() {
            throw new UnsupportedOperationException("Tags are not available in datagen");
        }
    }

    static abstract class EntryListCreatingLookup<T>
    implements RegistryEntryLookup<T> {
        protected final RegistryEntryOwner<T> entryOwner;

        protected EntryListCreatingLookup(RegistryEntryOwner<T> entryOwner) {
            this.entryOwner = entryOwner;
        }

        @Override
        public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
            return Optional.of(RegistryEntryList.of(this.entryOwner, tag));
        }
    }
}

