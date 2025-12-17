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
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

public record PackFeatureSetMetadata(FeatureSet flags) {
    private static final Codec<PackFeatureSetMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)FeatureFlags.CODEC.fieldOf("enabled").forGetter(PackFeatureSetMetadata::flags)).apply((Applicative)instance, PackFeatureSetMetadata::new));
    public static final ResourceMetadataSerializer<PackFeatureSetMetadata> SERIALIZER = new ResourceMetadataSerializer<PackFeatureSetMetadata>("features", CODEC);
}

