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
import net.minecraft.resource.PackVersion;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.dynamic.Range;

public record PackResourceMetadata(Text description, Range<PackVersion> supportedFormats) {
    private static final Codec<PackResourceMetadata> DESCRIPTION_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("description").forGetter(PackResourceMetadata::description)).apply((Applicative)instance, description -> new PackResourceMetadata((Text)description, new Range<PackVersion>(PackVersion.of(Integer.MAX_VALUE)))));
    public static final ResourceMetadataSerializer<PackResourceMetadata> CLIENT_RESOURCES_SERIALIZER = new ResourceMetadataSerializer<PackResourceMetadata>("pack", PackResourceMetadata.createCodec(ResourceType.CLIENT_RESOURCES));
    public static final ResourceMetadataSerializer<PackResourceMetadata> SERVER_DATA_SERIALIZER = new ResourceMetadataSerializer<PackResourceMetadata>("pack", PackResourceMetadata.createCodec(ResourceType.SERVER_DATA));
    public static final ResourceMetadataSerializer<PackResourceMetadata> DESCRIPTION_SERIALIZER = new ResourceMetadataSerializer<PackResourceMetadata>("pack", DESCRIPTION_CODEC);

    private static Codec<PackResourceMetadata> createCodec(ResourceType type) {
        return RecordCodecBuilder.create(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("description").forGetter(PackResourceMetadata::description), (App)PackVersion.createRangeCodec(type).forGetter(PackResourceMetadata::supportedFormats)).apply((Applicative)instance, PackResourceMetadata::new));
    }

    public static ResourceMetadataSerializer<PackResourceMetadata> getSerializerFor(ResourceType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case ResourceType.CLIENT_RESOURCES -> CLIENT_RESOURCES_SERIALIZER;
            case ResourceType.SERVER_DATA -> SERVER_DATA_SERIALIZER;
        };
    }
}

