/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 *  com.google.gson.Strictness
 *  com.google.gson.stream.JsonReader
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.logging;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import org.jspecify.annotations.Nullable;

public interface LogReader<T>
extends Closeable {
    public static <T> LogReader<T> create(final Codec<T> codec, Reader reader) {
        final JsonReader jsonReader = new JsonReader(reader);
        jsonReader.setStrictness(Strictness.LENIENT);
        return new LogReader<T>(){

            @Override
            public @Nullable T read() throws IOException {
                try {
                    if (!jsonReader.hasNext()) {
                        return null;
                    }
                    JsonElement jsonElement = JsonParser.parseReader((JsonReader)jsonReader);
                    return codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(IOException::new);
                }
                catch (JsonParseException jsonParseException) {
                    throw new IOException(jsonParseException);
                }
                catch (EOFException eOFException) {
                    return null;
                }
            }

            @Override
            public void close() throws IOException {
                jsonReader.close();
            }
        };
    }

    public @Nullable T read() throws IOException;
}

