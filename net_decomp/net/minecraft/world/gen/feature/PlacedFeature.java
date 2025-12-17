/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.feature.util.FeatureDebugLogger;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(RegistryEntry<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placementModifiers) {
    public static final Codec<PlacedFeature> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ConfiguredFeature.REGISTRY_CODEC.fieldOf("feature").forGetter(feature -> feature.feature), (App)PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(feature -> feature.placementModifiers)).apply((Applicative)instance, PlacedFeature::new));
    public static final Codec<RegistryEntry<PlacedFeature>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.PLACED_FEATURE, CODEC);
    public static final Codec<RegistryEntryList<PlacedFeature>> LIST_CODEC = RegistryCodecs.entryList(RegistryKeys.PLACED_FEATURE, CODEC);
    public static final Codec<List<RegistryEntryList<PlacedFeature>>> LISTS_CODEC = RegistryCodecs.entryList(RegistryKeys.PLACED_FEATURE, CODEC, true).listOf();

    public boolean generateUnregistered(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos pos) {
        return this.generate(new FeaturePlacementContext(world, generator, Optional.empty()), random, pos);
    }

    public boolean generate(StructureWorldAccess world, ChunkGenerator generator, Random random, BlockPos pos) {
        return this.generate(new FeaturePlacementContext(world, generator, Optional.of(this)), random, pos);
    }

    private boolean generate(FeaturePlacementContext context, Random random, BlockPos pos) {
        Stream<BlockPos> stream = Stream.of(pos);
        for (PlacementModifier placementModifier : this.placementModifiers) {
            stream = stream.flatMap(posx -> placementModifier.getPositions(context, random, (BlockPos)posx));
        }
        ConfiguredFeature<?, ?> configuredFeature = this.feature.value();
        MutableBoolean mutableBoolean = new MutableBoolean();
        stream.forEach(placedPos -> {
            if (configuredFeature.generate(context.getWorld(), context.getChunkGenerator(), random, (BlockPos)placedPos)) {
                mutableBoolean.setTrue();
                if (SharedConstants.FEATURE_COUNT) {
                    FeatureDebugLogger.incrementFeatureCount(context.getWorld().toServerWorld(), configuredFeature, context.getPlacedFeature());
                }
            }
        });
        return mutableBoolean.isTrue();
    }

    public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
        return this.feature.value().getDecoratedFeatures();
    }

    @Override
    public String toString() {
        return "Placed " + String.valueOf(this.feature);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlacedFeature.class, "feature;placement", "feature", "placementModifiers"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlacedFeature.class, "feature;placement", "feature", "placementModifiers"}, this, object);
    }
}

