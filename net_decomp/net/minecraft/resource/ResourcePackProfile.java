/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.resource;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.resource.PackVersion;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.metadata.PackFeatureSetMetadata;
import net.minecraft.resource.metadata.PackOverlaysMetadata;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ResourcePackProfile {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourcePackInfo info;
    private final PackFactory packFactory;
    private final Metadata metaData;
    private final ResourcePackPosition position;

    public static @Nullable ResourcePackProfile create(ResourcePackInfo info, PackFactory packFactory, ResourceType type, ResourcePackPosition position) {
        PackVersion packVersion = SharedConstants.getGameVersion().packVersion(type);
        Metadata metadata = ResourcePackProfile.loadMetadata(info, packFactory, packVersion, type);
        return metadata != null ? new ResourcePackProfile(info, packFactory, metadata, position) : null;
    }

    public ResourcePackProfile(ResourcePackInfo info, PackFactory packFactory, Metadata metaData, ResourcePackPosition position) {
        this.info = info;
        this.packFactory = packFactory;
        this.metaData = metaData;
        this.position = position;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static @Nullable Metadata loadMetadata(ResourcePackInfo info, PackFactory packFactory, PackVersion version, ResourceType type) {
        try (ResourcePack resourcePack = packFactory.open(info);){
            PackResourceMetadata packResourceMetadata = resourcePack.parseMetadata(PackResourceMetadata.getSerializerFor(type));
            if (packResourceMetadata == null) {
                packResourceMetadata = resourcePack.parseMetadata(PackResourceMetadata.DESCRIPTION_SERIALIZER);
            }
            if (packResourceMetadata == null) {
                LOGGER.warn("Missing metadata in pack {}", (Object)info.id());
                Metadata metadata = null;
                return metadata;
            }
            PackFeatureSetMetadata packFeatureSetMetadata = resourcePack.parseMetadata(PackFeatureSetMetadata.SERIALIZER);
            FeatureSet featureSet = packFeatureSetMetadata != null ? packFeatureSetMetadata.flags() : FeatureSet.empty();
            ResourcePackCompatibility resourcePackCompatibility = ResourcePackCompatibility.from(packResourceMetadata.supportedFormats(), version);
            PackOverlaysMetadata packOverlaysMetadata = resourcePack.parseMetadata(PackOverlaysMetadata.getSerializerFor(type));
            List<String> list = packOverlaysMetadata != null ? packOverlaysMetadata.getAppliedOverlays(version) : List.of();
            Metadata metadata = new Metadata(packResourceMetadata.description(), resourcePackCompatibility, featureSet, list);
            return metadata;
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to read pack {} metadata", (Object)info.id(), (Object)exception);
            return null;
        }
    }

    public ResourcePackInfo getInfo() {
        return this.info;
    }

    public Text getDisplayName() {
        return this.info.title();
    }

    public Text getDescription() {
        return this.metaData.description();
    }

    public Text getInformationText(boolean enabled) {
        return this.info.getInformationText(enabled, this.metaData.description);
    }

    public ResourcePackCompatibility getCompatibility() {
        return this.metaData.compatibility();
    }

    public FeatureSet getRequestedFeatures() {
        return this.metaData.requestedFeatures();
    }

    public ResourcePack createResourcePack() {
        return this.packFactory.openWithOverlays(this.info, this.metaData);
    }

    public String getId() {
        return this.info.id();
    }

    public ResourcePackPosition getPosition() {
        return this.position;
    }

    public boolean isRequired() {
        return this.position.required();
    }

    public boolean isPinned() {
        return this.position.fixedPosition();
    }

    public InsertionPosition getInitialPosition() {
        return this.position.defaultPosition();
    }

    public ResourcePackSource getSource() {
        return this.info.source();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourcePackProfile)) {
            return false;
        }
        ResourcePackProfile resourcePackProfile = (ResourcePackProfile)o;
        return this.info.equals(resourcePackProfile.info);
    }

    public int hashCode() {
        return this.info.hashCode();
    }

    public static interface PackFactory {
        public ResourcePack open(ResourcePackInfo var1);

        public ResourcePack openWithOverlays(ResourcePackInfo var1, Metadata var2);
    }

    public static final class Metadata
    extends Record {
        final Text description;
        private final ResourcePackCompatibility compatibility;
        private final FeatureSet requestedFeatures;
        private final List<String> overlays;

        public Metadata(Text description, ResourcePackCompatibility compatibility, FeatureSet requestedFeatures, List<String> overlays) {
            this.description = description;
            this.compatibility = compatibility;
            this.requestedFeatures = requestedFeatures;
            this.overlays = overlays;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this, object);
        }

        public Text description() {
            return this.description;
        }

        public ResourcePackCompatibility compatibility() {
            return this.compatibility;
        }

        public FeatureSet requestedFeatures() {
            return this.requestedFeatures;
        }

        public List<String> overlays() {
            return this.overlays;
        }
    }

    public static final class InsertionPosition
    extends Enum<InsertionPosition> {
        public static final /* enum */ InsertionPosition TOP = new InsertionPosition();
        public static final /* enum */ InsertionPosition BOTTOM = new InsertionPosition();
        private static final /* synthetic */ InsertionPosition[] field_14282;

        public static InsertionPosition[] values() {
            return (InsertionPosition[])field_14282.clone();
        }

        public static InsertionPosition valueOf(String string) {
            return Enum.valueOf(InsertionPosition.class, string);
        }

        public <T> int insert(List<T> items, T item, Function<T, ResourcePackPosition> profileGetter, boolean listInverted) {
            ResourcePackPosition resourcePackPosition;
            int i;
            InsertionPosition insertionPosition;
            InsertionPosition insertionPosition2 = insertionPosition = listInverted ? this.inverse() : this;
            if (insertionPosition == BOTTOM) {
                ResourcePackPosition resourcePackPosition2;
                int i2;
                for (i2 = 0; i2 < items.size() && (resourcePackPosition2 = profileGetter.apply(items.get(i2))).fixedPosition() && resourcePackPosition2.defaultPosition() == this; ++i2) {
                }
                items.add(i2, item);
                return i2;
            }
            for (i = items.size() - 1; i >= 0 && (resourcePackPosition = profileGetter.apply(items.get(i))).fixedPosition() && resourcePackPosition.defaultPosition() == this; --i) {
            }
            items.add(i + 1, item);
            return i + 1;
        }

        public InsertionPosition inverse() {
            return this == TOP ? BOTTOM : TOP;
        }

        private static /* synthetic */ InsertionPosition[] method_36583() {
            return new InsertionPosition[]{TOP, BOTTOM};
        }

        static {
            field_14282 = InsertionPosition.method_36583();
        }
    }
}

