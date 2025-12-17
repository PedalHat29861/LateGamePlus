/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;

public record PalettesFactory(PaletteProvider<BlockState> blockStatesStrategy, BlockState defaultBlockState, Codec<PalettedContainer<BlockState>> blockStatesContainerCodec, PaletteProvider<RegistryEntry<Biome>> biomeStrategy, RegistryEntry<Biome> defaultBiome, Codec<ReadableContainer<RegistryEntry<Biome>>> biomeContainerCodec) {
    public static PalettesFactory fromRegistryManager(DynamicRegistryManager registryManager) {
        PaletteProvider<BlockState> paletteProvider = PaletteProvider.forBlockStates(Block.STATE_IDS);
        BlockState blockState = Blocks.AIR.getDefaultState();
        RegistryWrapper.Impl registry = registryManager.getOrThrow(RegistryKeys.BIOME);
        PaletteProvider<RegistryEntry<Biome>> paletteProvider2 = PaletteProvider.forBiomes(registry.getIndexedEntries());
        RegistryEntry.Reference reference = registry.getOrThrow(BiomeKeys.PLAINS);
        return new PalettesFactory(paletteProvider, blockState, PalettedContainer.createPalettedContainerCodec(BlockState.CODEC, paletteProvider, blockState), paletteProvider2, reference, PalettedContainer.createReadableContainerCodec(registry.getEntryCodec(), paletteProvider2, reference));
    }

    public PalettedContainer<BlockState> getBlockStateContainer() {
        return new PalettedContainer<BlockState>(this.defaultBlockState, this.blockStatesStrategy);
    }

    public PalettedContainer<RegistryEntry<Biome>> getBiomeContainer() {
        return new PalettedContainer<RegistryEntry<Biome>>(this.defaultBiome, this.biomeStrategy);
    }
}

