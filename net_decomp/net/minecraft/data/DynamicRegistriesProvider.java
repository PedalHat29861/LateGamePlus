/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

public class DynamicRegistriesProvider
implements DataProvider {
    private final DataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;

    public DynamicRegistriesProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        this.registriesFuture = registriesFuture;
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return this.registriesFuture.thenCompose(registries -> {
            RegistryOps dynamicOps = registries.getOps(JsonOps.INSTANCE);
            return CompletableFuture.allOf((CompletableFuture[])RegistryLoader.DYNAMIC_REGISTRIES.stream().flatMap(entry -> this.writeRegistryEntries(writer, (RegistryWrapper.WrapperLookup)registries, dynamicOps, (RegistryLoader.Entry)entry).stream()).toArray(CompletableFuture[]::new));
        });
    }

    private <T> Optional<CompletableFuture<?>> writeRegistryEntries(DataWriter writer, RegistryWrapper.WrapperLookup registries, DynamicOps<JsonElement> ops, RegistryLoader.Entry<T> registry) {
        RegistryKey registryKey = registry.key();
        return registries.getOptional(registryKey).map(wrapper -> {
            DataOutput.PathResolver pathResolver = this.output.getResolver(registryKey);
            return CompletableFuture.allOf((CompletableFuture[])wrapper.streamEntries().map(entry -> DynamicRegistriesProvider.writeToPath(pathResolver.resolveJson(entry.registryKey().getValue()), writer, ops, registry.elementCodec(), entry.value())).toArray(CompletableFuture[]::new));
        });
    }

    private static <E> CompletableFuture<?> writeToPath(Path path, DataWriter cache, DynamicOps<JsonElement> json, Encoder<E> encoder, E value) {
        return (CompletableFuture)encoder.encodeStart(json, value).mapOrElse(jsonElement -> DataProvider.writeToPath(cache, jsonElement, path), error -> CompletableFuture.failedFuture(new IllegalStateException("Couldn't generate file '" + String.valueOf(path) + "': " + error.message())));
    }

    @Override
    public final String getName() {
        return "Registries";
    }
}

