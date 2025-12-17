/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.chunk.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public class RandomSpreadStructurePlacement
extends StructurePlacement {
    public static final MapCodec<RandomSpreadStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> RandomSpreadStructurePlacement.buildCodec(instance).and(instance.group((App)Codec.intRange((int)0, (int)4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::getSpacing), (App)Codec.intRange((int)0, (int)4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::getSeparation), (App)SpreadType.CODEC.optionalFieldOf("spread_type", (Object)SpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::getSpreadType))).apply((Applicative)instance, RandomSpreadStructurePlacement::new)).validate(RandomSpreadStructurePlacement::validate);
    private final int spacing;
    private final int separation;
    private final SpreadType spreadType;

    private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement structurePlacement) {
        if (structurePlacement.spacing <= structurePlacement.separation) {
            return DataResult.error(() -> "Spacing has to be larger than separation");
        }
        return DataResult.success((Object)structurePlacement);
    }

    public RandomSpreadStructurePlacement(Vec3i locateOffset, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<StructurePlacement.ExclusionZone> exclusionZone, int spacing, int separation, SpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;
    }

    public RandomSpreadStructurePlacement(int spacing, int separation, SpreadType spreadType, int salt) {
        this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f, salt, Optional.empty(), spacing, separation, spreadType);
    }

    public int getSpacing() {
        return this.spacing;
    }

    public int getSeparation() {
        return this.separation;
    }

    public SpreadType getSpreadType() {
        return this.spreadType;
    }

    public ChunkPos getStartChunk(long seed, int chunkX, int chunkZ) {
        int i = Math.floorDiv(chunkX, this.spacing);
        int j = Math.floorDiv(chunkZ, this.spacing);
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setRegionSeed(seed, i, j, this.getSalt());
        int k = this.spacing - this.separation;
        int l = this.spreadType.get(chunkRandom, k);
        int m = this.spreadType.get(chunkRandom, k);
        return new ChunkPos(i * this.spacing + l, j * this.spacing + m);
    }

    @Override
    protected boolean isStartChunk(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
        ChunkPos chunkPos = this.getStartChunk(calculator.getStructureSeed(), chunkX, chunkZ);
        return chunkPos.x == chunkX && chunkPos.z == chunkZ;
    }

    @Override
    public StructurePlacementType<?> getType() {
        return StructurePlacementType.RANDOM_SPREAD;
    }
}

