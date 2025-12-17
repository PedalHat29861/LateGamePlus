/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.resource;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.JsonHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractFileResourcePack
implements ResourcePack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourcePackInfo info;

    protected AbstractFileResourcePack(ResourcePackInfo info) {
        this.info = info;
    }

    @Override
    public <T> @Nullable T parseMetadata(ResourceMetadataSerializer<T> metadataSerializer) throws IOException {
        InputSupplier<InputStream> inputSupplier = this.openRoot("pack.mcmeta");
        if (inputSupplier == null) {
            return null;
        }
        try (InputStream inputStream = inputSupplier.get();){
            T t = AbstractFileResourcePack.parseMetadata(metadataSerializer, inputStream, this.info);
            return t;
        }
    }

    public static <T> @Nullable T parseMetadata(ResourceMetadataSerializer<T> resourceMetadataSerializer, InputStream inputStream, ResourcePackInfo resourcePackInfo) {
        JsonObject jsonObject;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
            jsonObject = JsonHelper.deserialize(bufferedReader);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{resourcePackInfo.id(), resourceMetadataSerializer.name(), exception.getMessage()});
            return null;
        }
        if (!jsonObject.has(resourceMetadataSerializer.name())) {
            return null;
        }
        return resourceMetadataSerializer.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonObject.get(resourceMetadataSerializer.name())).ifError(error -> LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{resourcePackInfo.id(), resourceMetadataSerializer.name(), error.message()})).result().orElse(null);
    }

    @Override
    public ResourcePackInfo getInfo() {
        return this.info;
    }
}

