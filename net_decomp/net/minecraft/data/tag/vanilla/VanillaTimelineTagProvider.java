/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TimelineTags;
import net.minecraft.world.attribute.timeline.Timeline;
import net.minecraft.world.attribute.timeline.Timelines;

public class VanillaTimelineTagProvider
extends SimpleTagProvider<Timeline> {
    public VanillaTimelineTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.TIMELINE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(TimelineTags.UNIVERSAL).add(Timelines.VILLAGER_SCHEDULE);
        this.builder(TimelineTags.IN_OVERWORLD).addTag(TimelineTags.UNIVERSAL).add(Timelines.DAY, Timelines.MOON, Timelines.EARLY_GAME);
        this.builder(TimelineTags.IN_NETHER).addTag(TimelineTags.UNIVERSAL);
        this.builder(TimelineTags.IN_END).addTag(TimelineTags.UNIVERSAL);
    }
}

