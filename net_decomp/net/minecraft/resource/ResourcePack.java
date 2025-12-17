/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public interface ResourcePack
extends AutoCloseable {
    public static final String METADATA_PATH_SUFFIX = ".mcmeta";
    public static final String PACK_METADATA_NAME = "pack.mcmeta";

    public @Nullable InputSupplier<InputStream> openRoot(String ... var1);

    public @Nullable InputSupplier<InputStream> open(ResourceType var1, Identifier var2);

    public void findResources(ResourceType var1, String var2, String var3, ResultConsumer var4);

    public Set<String> getNamespaces(ResourceType var1);

    public <T> @Nullable T parseMetadata(ResourceMetadataSerializer<T> var1) throws IOException;

    public ResourcePackInfo getInfo();

    default public String getId() {
        return this.getInfo().id();
    }

    default public Optional<VersionedIdentifier> getKnownPackInfo() {
        return this.getInfo().knownPackInfo();
    }

    @Override
    public void close();

    @FunctionalInterface
    public static interface ResultConsumer
    extends BiConsumer<Identifier, InputSupplier<InputStream>> {
    }
}

