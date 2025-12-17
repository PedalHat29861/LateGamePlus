/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.resource.metadata;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resource.metadata.BlockEntry;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

public class ResourceFilter {
    private static final Codec<ResourceFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.list(BlockEntry.CODEC).fieldOf("block").forGetter(filter -> filter.blocks)).apply((Applicative)instance, ResourceFilter::new));
    public static final ResourceMetadataSerializer<ResourceFilter> SERIALIZER = new ResourceMetadataSerializer<ResourceFilter>("filter", CODEC);
    private final List<BlockEntry> blocks;

    public ResourceFilter(List<BlockEntry> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public boolean isNamespaceBlocked(String namespace) {
        return this.blocks.stream().anyMatch(block -> block.getNamespacePredicate().test(namespace));
    }

    public boolean isPathBlocked(String namespace) {
        return this.blocks.stream().anyMatch(block -> block.getPathPredicate().test(namespace));
    }
}

