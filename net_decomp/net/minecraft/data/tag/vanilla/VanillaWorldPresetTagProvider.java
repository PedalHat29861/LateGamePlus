/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.WorldPresetTags;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;

public class VanillaWorldPresetTagProvider
extends SimpleTagProvider<WorldPreset> {
    public VanillaWorldPresetTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.WORLD_PRESET, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(WorldPresetTags.NORMAL).add(WorldPresets.DEFAULT).add(WorldPresets.FLAT).add(WorldPresets.LARGE_BIOMES).add(WorldPresets.AMPLIFIED).add(WorldPresets.SINGLE_BIOME_SURFACE);
        this.builder(WorldPresetTags.EXTENDED).addTag(WorldPresetTags.NORMAL).add(WorldPresets.DEBUG_ALL_BLOCK_STATES);
    }
}

