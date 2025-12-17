/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.command;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

public interface CommandRegistryAccess
extends RegistryWrapper.WrapperLookup {
    public static CommandRegistryAccess of(final RegistryWrapper.WrapperLookup registries, final FeatureSet enabledFeatures) {
        return new CommandRegistryAccess(){

            @Override
            public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
                return registries.streamAllRegistryKeys();
            }

            public <T> Optional<RegistryWrapper.Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return registries.getOptional(registryRef).map(wrapper -> wrapper.withFeatureFilter(enabledFeatures));
            }

            @Override
            public FeatureSet getEnabledFeatures() {
                return enabledFeatures;
            }
        };
    }

    public FeatureSet getEnabledFeatures();
}

