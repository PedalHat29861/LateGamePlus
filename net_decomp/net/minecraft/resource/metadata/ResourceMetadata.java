/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.resource.metadata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.JsonHelper;

public interface ResourceMetadata {
    public static final ResourceMetadata NONE = new ResourceMetadata(){

        @Override
        public <T> Optional<T> decode(ResourceMetadataSerializer<T> serializer) {
            return Optional.empty();
        }
    };
    public static final InputSupplier<ResourceMetadata> NONE_SUPPLIER = () -> NONE;

    public static ResourceMetadata create(InputStream stream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));){
            final JsonObject jsonObject = JsonHelper.deserialize(bufferedReader);
            ResourceMetadata resourceMetadata = new ResourceMetadata(){

                @Override
                public <T> Optional<T> decode(ResourceMetadataSerializer<T> serializer) {
                    String string = serializer.name();
                    if (jsonObject.has(string)) {
                        Object object = serializer.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonObject.get(string)).getOrThrow(JsonParseException::new);
                        return Optional.of(object);
                    }
                    return Optional.empty();
                }
            };
            return resourceMetadata;
        }
    }

    public <T> Optional<T> decode(ResourceMetadataSerializer<T> var1);

    default public <T> Optional<ResourceMetadataSerializer.Value<T>> decodeAsValue(ResourceMetadataSerializer<T> additionalMetadata) {
        return this.decode(additionalMetadata).map(additionalMetadata::value);
    }

    default public List<ResourceMetadataSerializer.Value<?>> decode(Collection<ResourceMetadataSerializer<?>> serializers) {
        return serializers.stream().map(this::decodeAsValue).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
    }
}

