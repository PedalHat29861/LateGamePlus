/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

public abstract class JsonDataLoader<T>
extends SinglePreparationResourceReloader<Map<Identifier, T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DynamicOps<JsonElement> ops;
    private final Codec<T> codec;
    private final ResourceFinder finder;

    protected JsonDataLoader(RegistryWrapper.WrapperLookup registries, Codec<T> codec, RegistryKey<? extends Registry<T>> registryRef) {
        this(registries.getOps(JsonOps.INSTANCE), codec, ResourceFinder.json(registryRef));
    }

    protected JsonDataLoader(Codec<T> codec, ResourceFinder finder) {
        this((DynamicOps<JsonElement>)JsonOps.INSTANCE, codec, finder);
    }

    private JsonDataLoader(DynamicOps<JsonElement> ops, Codec<T> codec, ResourceFinder finder) {
        this.ops = ops;
        this.codec = codec;
        this.finder = finder;
    }

    @Override
    protected Map<Identifier, T> prepare(ResourceManager resourceManager, Profiler profiler) {
        HashMap map = new HashMap();
        JsonDataLoader.load(resourceManager, this.finder, this.ops, this.codec, map);
        return map;
    }

    public static <T> void load(ResourceManager manager, RegistryKey<? extends Registry<T>> registryRef, DynamicOps<JsonElement> ops, Codec<T> codec, Map<Identifier, T> results) {
        JsonDataLoader.load(manager, ResourceFinder.json(registryRef), ops, codec, results);
    }

    public static <T> void load(ResourceManager manager, ResourceFinder finder, DynamicOps<JsonElement> ops, Codec<T> codec, Map<Identifier, T> results) {
        for (Map.Entry<Identifier, Resource> entry : finder.findResources(manager).entrySet()) {
            Identifier identifier = entry.getKey();
            Identifier identifier2 = finder.toResourceId(identifier);
            try {
                BufferedReader reader = entry.getValue().getReader();
                try {
                    codec.parse(ops, (Object)StrictJsonParser.parse(reader)).ifSuccess(value -> {
                        if (results.putIfAbsent(identifier2, value) != null) {
                            throw new IllegalStateException("Duplicate data file ignored with ID " + String.valueOf(identifier2));
                        }
                    }).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{identifier2, identifier, error}));
                }
                finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            }
            catch (JsonParseException | IOException | IllegalArgumentException exception) {
                LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{identifier2, identifier, exception});
            }
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager manager, Profiler profiler) {
        return this.prepare(manager, profiler);
    }
}

