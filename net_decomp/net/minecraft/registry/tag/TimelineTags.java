/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry.tag;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.attribute.timeline.Timeline;

public interface TimelineTags {
    public static final TagKey<Timeline> UNIVERSAL = TimelineTags.of("universal");
    public static final TagKey<Timeline> IN_OVERWORLD = TimelineTags.of("in_overworld");
    public static final TagKey<Timeline> IN_NETHER = TimelineTags.of("in_nether");
    public static final TagKey<Timeline> IN_END = TimelineTags.of("in_end");

    private static TagKey<Timeline> of(String name) {
        return TagKey.of(RegistryKeys.TIMELINE, Identifier.ofVanilla(name));
    }
}

