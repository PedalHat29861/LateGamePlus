/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.resource;

import net.minecraft.resource.PackVersion;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Range;

public final class ResourcePackCompatibility
extends Enum<ResourcePackCompatibility> {
    public static final /* enum */ ResourcePackCompatibility TOO_OLD = new ResourcePackCompatibility("old");
    public static final /* enum */ ResourcePackCompatibility TOO_NEW = new ResourcePackCompatibility("new");
    public static final /* enum */ ResourcePackCompatibility UNKNOWN = new ResourcePackCompatibility("unknown");
    public static final /* enum */ ResourcePackCompatibility COMPATIBLE = new ResourcePackCompatibility("compatible");
    public static final int field_61160 = Integer.MAX_VALUE;
    private final Text notification;
    private final Text confirmMessage;
    private static final /* synthetic */ ResourcePackCompatibility[] field_14221;

    public static ResourcePackCompatibility[] values() {
        return (ResourcePackCompatibility[])field_14221.clone();
    }

    public static ResourcePackCompatibility valueOf(String string) {
        return Enum.valueOf(ResourcePackCompatibility.class, string);
    }

    private ResourcePackCompatibility(String translationSuffix) {
        this.notification = Text.translatable("pack.incompatible." + translationSuffix).formatted(Formatting.GRAY);
        this.confirmMessage = Text.translatable("pack.incompatible.confirm." + translationSuffix);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static ResourcePackCompatibility from(Range<PackVersion> range, PackVersion packVersion) {
        if (range.minInclusive().major() == Integer.MAX_VALUE) {
            return UNKNOWN;
        }
        if (range.maxInclusive().compareTo(packVersion) < 0) {
            return TOO_OLD;
        }
        if (packVersion.compareTo(range.minInclusive()) < 0) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public Text getNotification() {
        return this.notification;
    }

    public Text getConfirmMessage() {
        return this.confirmMessage;
    }

    private static /* synthetic */ ResourcePackCompatibility[] method_36584() {
        return new ResourcePackCompatibility[]{TOO_OLD, TOO_NEW, UNKNOWN, COMPATIBLE};
    }

    static {
        field_14221 = ResourcePackCompatibility.method_36584();
    }
}

