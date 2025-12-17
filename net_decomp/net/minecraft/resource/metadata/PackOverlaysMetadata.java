/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.resource.metadata;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resource.PackVersion;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.dynamic.Range;

public record PackOverlaysMetadata(List<Entry> overlays) {
    private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");
    public static final ResourceMetadataSerializer<PackOverlaysMetadata> CLIENT_RESOURCES_SERIALIZER = new ResourceMetadataSerializer<PackOverlaysMetadata>("overlays", PackOverlaysMetadata.createCodec(ResourceType.CLIENT_RESOURCES));
    public static final ResourceMetadataSerializer<PackOverlaysMetadata> SERVER_DATA_SERIALIZER = new ResourceMetadataSerializer<PackOverlaysMetadata>("overlays", PackOverlaysMetadata.createCodec(ResourceType.SERVER_DATA));

    private static DataResult<String> validate(String directoryName) {
        if (!DIRECTORY_NAME_PATTERN.matcher(directoryName).matches()) {
            return DataResult.error(() -> directoryName + " is not accepted directory name");
        }
        return DataResult.success((Object)directoryName);
    }

    @VisibleForTesting
    public static Codec<PackOverlaysMetadata> createCodec(ResourceType type) {
        return RecordCodecBuilder.create(instance -> instance.group((App)Entry.createCodec(type).fieldOf("entries").forGetter(PackOverlaysMetadata::overlays)).apply((Applicative)instance, PackOverlaysMetadata::new));
    }

    public static ResourceMetadataSerializer<PackOverlaysMetadata> getSerializerFor(ResourceType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case ResourceType.CLIENT_RESOURCES -> CLIENT_RESOURCES_SERIALIZER;
            case ResourceType.SERVER_DATA -> SERVER_DATA_SERIALIZER;
        };
    }

    public List<String> getAppliedOverlays(PackVersion version) {
        return this.overlays.stream().filter(overlay -> overlay.isValid(version)).map(Entry::overlay).toList();
    }

    public record Entry(Range<PackVersion> format, String overlay) {
        static Codec<List<Entry>> createCodec(ResourceType type) {
            int i = PackVersion.getLastOldPackVersion(type);
            return Holder.CODEC.listOf().flatXmap(holders -> PackVersion.validate(holders, i, (holder, versionRange) -> new Entry((Range<PackVersion>)versionRange, holder.overlay())), entries -> DataResult.success(entries.stream().map(entry -> new Holder(PackVersion.Format.ofRange(entry.format(), i), entry.overlay())).toList()));
        }

        public boolean isValid(PackVersion version) {
            return this.format.contains(version);
        }

        record Holder(PackVersion.Format format, String overlay) implements PackVersion.FormatHolder
        {
            static final Codec<Holder> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)PackVersion.Format.OVERLAY_CODEC.forGetter(Holder::format), (App)Codec.STRING.validate(PackOverlaysMetadata::validate).fieldOf("directory").forGetter(Holder::overlay)).apply((Applicative)instance, Holder::new));

            @Override
            public String toString() {
                return this.overlay;
            }
        }
    }
}

