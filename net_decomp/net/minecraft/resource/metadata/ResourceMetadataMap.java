/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource.metadata;

import java.util.Map;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

public class ResourceMetadataMap {
    private static final ResourceMetadataMap EMPTY = new ResourceMetadataMap(Map.of());
    private final Map<ResourceMetadataSerializer<?>, ?> values;

    private ResourceMetadataMap(Map<ResourceMetadataSerializer<?>, ?> values) {
        this.values = values;
    }

    public <T> T get(ResourceMetadataSerializer<T> serializer) {
        return (T)this.values.get(serializer);
    }

    public static ResourceMetadataMap of() {
        return EMPTY;
    }

    public static <T> ResourceMetadataMap of(ResourceMetadataSerializer<T> serializer, T value) {
        return new ResourceMetadataMap(Map.of(serializer, value));
    }

    public static <T1, T2> ResourceMetadataMap of(ResourceMetadataSerializer<T1> serializer, T1 value, ResourceMetadataSerializer<T2> serializer2, T2 value2) {
        return new ResourceMetadataMap(Map.of(serializer, value, serializer2, value2));
    }
}

