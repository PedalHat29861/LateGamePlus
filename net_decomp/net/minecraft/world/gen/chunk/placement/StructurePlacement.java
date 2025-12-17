/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P5
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.gen.chunk.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public abstract class StructurePlacement {
    public static final Codec<StructurePlacement> TYPE_CODEC = Registries.STRUCTURE_PLACEMENT.getCodec().dispatch(StructurePlacement::getType, StructurePlacementType::codec);
    private static final int ARBITRARY_SALT = 10387320;
    private final Vec3i locateOffset;
    private final FrequencyReductionMethod frequencyReductionMethod;
    private final float frequency;
    private final int salt;
    private final Optional<ExclusionZone> exclusionZone;

    protected static <S extends StructurePlacement> Products.P5<RecordCodecBuilder.Mu<S>, Vec3i, FrequencyReductionMethod, Float, Integer, Optional<ExclusionZone>> buildCodec(RecordCodecBuilder.Instance<S> instance) {
        return instance.group((App)Vec3i.createOffsetCodec(16).optionalFieldOf("locate_offset", (Object)Vec3i.ZERO).forGetter(StructurePlacement::getLocateOffset), (App)FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", (Object)FrequencyReductionMethod.DEFAULT).forGetter(StructurePlacement::getFrequencyReductionMethod), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("frequency", (Object)Float.valueOf(1.0f)).forGetter(StructurePlacement::getFrequency), (App)Codecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StructurePlacement::getSalt), (App)ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::getExclusionZone));
    }

    protected StructurePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<ExclusionZone> exclusionZone) {
        this.locateOffset = locateOffset;
        this.frequencyReductionMethod = frequencyReductionMethod;
        this.frequency = frequency;
        this.salt = salt;
        this.exclusionZone = exclusionZone;
    }

    protected Vec3i getLocateOffset() {
        return this.locateOffset;
    }

    protected FrequencyReductionMethod getFrequencyReductionMethod() {
        return this.frequencyReductionMethod;
    }

    protected float getFrequency() {
        return this.frequency;
    }

    protected int getSalt() {
        return this.salt;
    }

    protected Optional<ExclusionZone> getExclusionZone() {
        return this.exclusionZone;
    }

    public boolean shouldGenerate(StructurePlacementCalculator calculator, int chunkX, int chunkZ) {
        return this.isStartChunk(calculator, chunkX, chunkZ) && this.applyFrequencyReduction(chunkX, chunkZ, calculator.getStructureSeed()) && this.applyExclusionZone(calculator, chunkX, chunkZ);
    }

    public boolean applyFrequencyReduction(int chunkX, int chunkZ, long seed) {
        return !(this.frequency < 1.0f) || this.frequencyReductionMethod.shouldGenerate(seed, this.salt, chunkX, chunkZ, this.frequency);
    }

    public boolean applyExclusionZone(StructurePlacementCalculator calculator, int centerChunkX, int centerChunkZ) {
        return !this.exclusionZone.isPresent() || !this.exclusionZone.get().shouldExclude(calculator, centerChunkX, centerChunkZ);
    }

    protected abstract boolean isStartChunk(StructurePlacementCalculator var1, int var2, int var3);

    public BlockPos getLocatePos(ChunkPos chunkPos) {
        return new BlockPos(chunkPos.getStartX(), 0, chunkPos.getStartZ()).add(this.getLocateOffset());
    }

    public abstract StructurePlacementType<?> getType();

    private static boolean defaultShouldGenerate(long seed, int salt, int chunkX, int chunkZ, float frequency) {
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setRegionSeed(seed, salt, chunkX, chunkZ);
        return chunkRandom.nextFloat() < frequency;
    }

    private static boolean legacyType3ShouldGenerate(long seed, int salt, int chunkX, int chunkZ, float frequency) {
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setCarverSeed(seed, chunkX, chunkZ);
        return chunkRandom.nextDouble() < (double)frequency;
    }

    private static boolean legacyType2ShouldGenerate(long seed, int salt, int chunkX, int chunkZ, float frequency) {
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setRegionSeed(seed, chunkX, chunkZ, 10387320);
        return chunkRandom.nextFloat() < frequency;
    }

    private static boolean legacyType1ShouldGenerate(long seed, int salt, int chunkX, int chunkZ, float frequency) {
        int i = chunkX >> 4;
        int j = chunkZ >> 4;
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
        chunkRandom.setSeed((long)(i ^ j << 4) ^ seed);
        chunkRandom.nextInt();
        return chunkRandom.nextInt((int)(1.0f / frequency)) == 0;
    }

    public static final class FrequencyReductionMethod
    extends Enum<FrequencyReductionMethod>
    implements StringIdentifiable {
        public static final /* enum */ FrequencyReductionMethod DEFAULT = new FrequencyReductionMethod("default", StructurePlacement::defaultShouldGenerate);
        public static final /* enum */ FrequencyReductionMethod LEGACY_TYPE_1 = new FrequencyReductionMethod("legacy_type_1", StructurePlacement::legacyType1ShouldGenerate);
        public static final /* enum */ FrequencyReductionMethod LEGACY_TYPE_2 = new FrequencyReductionMethod("legacy_type_2", StructurePlacement::legacyType2ShouldGenerate);
        public static final /* enum */ FrequencyReductionMethod LEGACY_TYPE_3 = new FrequencyReductionMethod("legacy_type_3", StructurePlacement::legacyType3ShouldGenerate);
        public static final Codec<FrequencyReductionMethod> CODEC;
        private final String name;
        private final GenerationPredicate generationPredicate;
        private static final /* synthetic */ FrequencyReductionMethod[] field_37789;

        public static FrequencyReductionMethod[] values() {
            return (FrequencyReductionMethod[])field_37789.clone();
        }

        public static FrequencyReductionMethod valueOf(String string) {
            return Enum.valueOf(FrequencyReductionMethod.class, string);
        }

        private FrequencyReductionMethod(String name, GenerationPredicate generationPredicate) {
            this.name = name;
            this.generationPredicate = generationPredicate;
        }

        public boolean shouldGenerate(long seed, int salt, int chunkX, int chunkZ, float chance) {
            return this.generationPredicate.shouldGenerate(seed, salt, chunkX, chunkZ, chance);
        }

        @Override
        public String asString() {
            return this.name;
        }

        private static /* synthetic */ FrequencyReductionMethod[] method_41649() {
            return new FrequencyReductionMethod[]{DEFAULT, LEGACY_TYPE_1, LEGACY_TYPE_2, LEGACY_TYPE_3};
        }

        static {
            field_37789 = FrequencyReductionMethod.method_41649();
            CODEC = StringIdentifiable.createCodec(FrequencyReductionMethod::values);
        }
    }

    @Deprecated
    public record ExclusionZone(RegistryEntry<StructureSet> otherSet, int chunkCount) {
        public static final Codec<ExclusionZone> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryElementCodec.of(RegistryKeys.STRUCTURE_SET, StructureSet.CODEC, false).fieldOf("other_set").forGetter(ExclusionZone::otherSet), (App)Codec.intRange((int)1, (int)16).fieldOf("chunk_count").forGetter(ExclusionZone::chunkCount)).apply((Applicative)instance, ExclusionZone::new));

        boolean shouldExclude(StructurePlacementCalculator calculator, int centerChunkX, int centerChunkZ) {
            return calculator.canGenerate(this.otherSet, centerChunkX, centerChunkZ, this.chunkCount);
        }
    }

    @FunctionalInterface
    public static interface GenerationPredicate {
        public boolean shouldGenerate(long var1, int var3, int var4, int var5, float var6);
    }
}

