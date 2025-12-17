/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.MinecraftVersion;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.PackFeatureSetMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;

public class MetadataProvider
implements DataProvider {
    private final DataOutput output;
    private final Map<String, Supplier<JsonElement>> metadata = new HashMap<String, Supplier<JsonElement>>();

    public MetadataProvider(DataOutput output) {
        this.output = output;
    }

    public <T> MetadataProvider add(ResourceMetadataSerializer<T> serializer, T metadata) {
        this.metadata.put(serializer.name(), () -> ((JsonElement)serializer.codec().encodeStart((DynamicOps)JsonOps.INSTANCE, metadata).getOrThrow(IllegalArgumentException::new)).getAsJsonObject());
        return this;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        JsonObject jsonObject = new JsonObject();
        this.metadata.forEach((key, jsonSupplier) -> jsonObject.add(key, (JsonElement)jsonSupplier.get()));
        return DataProvider.writeToPath(writer, (JsonElement)jsonObject, this.output.getPath().resolve("pack.mcmeta"));
    }

    @Override
    public final String getName() {
        return "Pack Metadata";
    }

    public static MetadataProvider create(DataOutput output, Text description) {
        return new MetadataProvider(output).add(PackResourceMetadata.SERVER_DATA_SERIALIZER, new PackResourceMetadata(description, MinecraftVersion.DEVELOPMENT.packVersion(ResourceType.SERVER_DATA).majorRange()));
    }

    public static MetadataProvider create(DataOutput output, Text description, FeatureSet requiredFeatures) {
        return MetadataProvider.create(output, description).add(PackFeatureSetMetadata.SERIALIZER, new PackFeatureSetMetadata(requiredFeatures));
    }
}

