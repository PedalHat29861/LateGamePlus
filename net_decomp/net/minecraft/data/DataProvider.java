/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  com.google.common.hash.HashingOutputStream
 *  com.google.gson.JsonElement
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public interface DataProvider {
    public static final ToIntFunction<String> JSON_KEY_SORT_ORDER = (ToIntFunction)Util.make(new Object2IntOpenHashMap(), map -> {
        map.put((Object)"type", 0);
        map.put((Object)"parent", 1);
        map.defaultReturnValue(2);
    });
    public static final Comparator<String> JSON_KEY_SORTING_COMPARATOR = Comparator.comparingInt(JSON_KEY_SORT_ORDER).thenComparing(key -> key);
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompletableFuture<?> run(DataWriter var1);

    public String getName();

    public static <T> CompletableFuture<?> writeAllToPath(DataWriter writer, Codec<T> codec, DataOutput.PathResolver pathResolver, Map<Identifier, T> idsToValues) {
        return DataProvider.writeAllToPath(writer, codec, pathResolver::resolveJson, idsToValues);
    }

    public static <T, E> CompletableFuture<?> writeAllToPath(DataWriter writer, Codec<E> codec, Function<T, Path> pathResolver, Map<T, E> idsToValues) {
        return DataProvider.writeAllToPath(writer, (E value) -> (JsonElement)codec.encodeStart((DynamicOps)JsonOps.INSTANCE, value).getOrThrow(), pathResolver, idsToValues);
    }

    public static <T, E> CompletableFuture<?> writeAllToPath(DataWriter writer, Function<E, JsonElement> serializer, Function<T, Path> pathResolver, Map<T, E> idsToValues) {
        return CompletableFuture.allOf((CompletableFuture[])idsToValues.entrySet().stream().map(entry -> {
            Path path = (Path)pathResolver.apply(entry.getKey());
            JsonElement jsonElement = (JsonElement)serializer.apply(entry.getValue());
            return DataProvider.writeToPath(writer, jsonElement, path);
        }).toArray(CompletableFuture[]::new));
    }

    public static <T> CompletableFuture<?> writeCodecToPath(DataWriter writer, RegistryWrapper.WrapperLookup registries, Codec<T> codec, T value, Path path) {
        RegistryOps<JsonElement> registryOps = registries.getOps(JsonOps.INSTANCE);
        return DataProvider.writeCodecToPath(writer, registryOps, codec, value, path);
    }

    public static <T> CompletableFuture<?> writeCodecToPath(DataWriter writer, Codec<T> codec, T value, Path path) {
        return DataProvider.writeCodecToPath(writer, (DynamicOps<JsonElement>)JsonOps.INSTANCE, codec, value, path);
    }

    private static <T> CompletableFuture<?> writeCodecToPath(DataWriter writer, DynamicOps<JsonElement> ops, Codec<T> codec, T value, Path path) {
        JsonElement jsonElement = (JsonElement)codec.encodeStart(ops, value).getOrThrow();
        return DataProvider.writeToPath(writer, jsonElement, path);
    }

    public static CompletableFuture<?> writeToPath(DataWriter writer, JsonElement json, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), (OutputStream)byteArrayOutputStream);
                try (JsonWriter jsonWriter = new JsonWriter((Writer)new OutputStreamWriter((OutputStream)hashingOutputStream, StandardCharsets.UTF_8));){
                    jsonWriter.setSerializeNulls(false);
                    jsonWriter.setIndent("  ");
                    JsonHelper.writeSorted(jsonWriter, json, JSON_KEY_SORTING_COMPARATOR);
                }
                writer.write(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
            }
            catch (IOException iOException) {
                LOGGER.error("Failed to save file to {}", (Object)path, (Object)iOException);
            }
        }, Util.getMainWorkerExecutor().named("saveStable"));
    }

    @FunctionalInterface
    public static interface Factory<T extends DataProvider> {
        public T create(DataOutput var1);
    }
}

