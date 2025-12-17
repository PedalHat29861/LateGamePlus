/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.JavaOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.jspecify.annotations.Nullable;

public class RegistryCloner<T> {
    private final Codec<T> elementCodec;

    RegistryCloner(Codec<T> elementCodec) {
        this.elementCodec = elementCodec;
    }

    public T clone(T value, RegistryWrapper.WrapperLookup subsetRegistry, RegistryWrapper.WrapperLookup fullRegistry) {
        RegistryOps dynamicOps = subsetRegistry.getOps(JavaOps.INSTANCE);
        RegistryOps dynamicOps2 = fullRegistry.getOps(JavaOps.INSTANCE);
        Object object = this.elementCodec.encodeStart(dynamicOps, value).getOrThrow(error -> new IllegalStateException("Failed to encode: " + error));
        return (T)this.elementCodec.parse(dynamicOps2, object).getOrThrow(error -> new IllegalStateException("Failed to decode: " + error));
    }

    public static class CloneableRegistries {
        private final Map<RegistryKey<? extends Registry<?>>, RegistryCloner<?>> registries = new HashMap();

        public <T> CloneableRegistries add(RegistryKey<? extends Registry<? extends T>> registryRef, Codec<T> elementCodec) {
            this.registries.put(registryRef, new RegistryCloner<T>(elementCodec));
            return this;
        }

        public <T> @Nullable RegistryCloner<T> get(RegistryKey<? extends Registry<? extends T>> registryRef) {
            return this.registries.get(registryRef);
        }
    }
}

