/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 */
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.Finishable;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.StructureType;

public abstract class Structure {
    public static final Codec<Structure> STRUCTURE_CODEC = Registries.STRUCTURE_TYPE.getCodec().dispatch(Structure::getType, StructureType::codec);
    public static final Codec<RegistryEntry<Structure>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.STRUCTURE, STRUCTURE_CODEC);
    protected final Config config;

    public static <S extends Structure> RecordCodecBuilder<S, Config> configCodecBuilder(RecordCodecBuilder.Instance<S> instance) {
        return Config.CODEC.forGetter(feature -> feature.config);
    }

    public static <S extends Structure> MapCodec<S> createCodec(Function<Config, S> featureCreator) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(Structure.configCodecBuilder(instance)).apply((Applicative)instance, featureCreator));
    }

    protected Structure(Config config) {
        this.config = config;
    }

    public RegistryEntryList<Biome> getValidBiomes() {
        return this.config.biomes;
    }

    public Map<SpawnGroup, StructureSpawns> getStructureSpawns() {
        return this.config.spawnOverrides;
    }

    public GenerationStep.Feature getFeatureGenerationStep() {
        return this.config.step;
    }

    public StructureTerrainAdaptation getTerrainAdaptation() {
        return this.config.terrainAdaptation;
    }

    public BlockBox expandBoxIfShouldAdaptNoise(BlockBox box) {
        if (this.getTerrainAdaptation() != StructureTerrainAdaptation.NONE) {
            return box.expand(12);
        }
        return box;
    }

    public StructureStart createStructureStart(RegistryEntry<Structure> structure, RegistryKey<World> dimension, DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, int references, HeightLimitView world, Predicate<RegistryEntry<Biome>> validBiomes) {
        StructurePiecesCollector structurePiecesCollector;
        StructureStart structureStart;
        Finishable finishable = FlightProfiler.INSTANCE.startStructureGenerationProfiling(chunkPos, dimension, structure);
        Context context = new Context(dynamicRegistryManager, chunkGenerator, biomeSource, noiseConfig, structureTemplateManager, seed, chunkPos, world, validBiomes);
        Optional<StructurePosition> optional = this.getValidStructurePosition(context);
        if (optional.isPresent() && (structureStart = new StructureStart(this, chunkPos, references, (structurePiecesCollector = optional.get().generate()).toList())).hasChildren()) {
            if (finishable != null) {
                finishable.finish(true);
            }
            return structureStart;
        }
        if (finishable != null) {
            finishable.finish(false);
        }
        return StructureStart.DEFAULT;
    }

    protected static Optional<StructurePosition> getStructurePosition(Context context, Heightmap.Type heightmap, Consumer<StructurePiecesCollector> generator) {
        ChunkPos chunkPos = context.chunkPos();
        int i = chunkPos.getCenterX();
        int j = chunkPos.getCenterZ();
        int k = context.chunkGenerator().getHeightInGround(i, j, heightmap, context.world(), context.noiseConfig());
        return Optional.of(new StructurePosition(new BlockPos(i, k, j), generator));
    }

    private static boolean isBiomeValid(StructurePosition result, Context context) {
        BlockPos blockPos = result.position();
        return context.biomePredicate.test(context.chunkGenerator.getBiomeSource().getBiome(BiomeCoords.fromBlock(blockPos.getX()), BiomeCoords.fromBlock(blockPos.getY()), BiomeCoords.fromBlock(blockPos.getZ()), context.noiseConfig.getMultiNoiseSampler()));
    }

    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
    }

    private static int[] getCornerHeights(Context context, int x, int width, int z, int height) {
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        HeightLimitView heightLimitView = context.world();
        NoiseConfig noiseConfig = context.noiseConfig();
        return new int[]{chunkGenerator.getHeightInGround(x, z, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView, noiseConfig), chunkGenerator.getHeightInGround(x, z + height, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView, noiseConfig), chunkGenerator.getHeightInGround(x + width, z, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView, noiseConfig), chunkGenerator.getHeightInGround(x + width, z + height, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView, noiseConfig)};
    }

    public static int getAverageCornerHeights(Context context, int x, int width, int z, int height) {
        int[] is = Structure.getCornerHeights(context, x, width, z, height);
        return (is[0] + is[1] + is[2] + is[3]) / 4;
    }

    protected static int getMinCornerHeight(Context context, int width, int height) {
        ChunkPos chunkPos = context.chunkPos();
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        return Structure.getMinCornerHeight(context, i, j, width, height);
    }

    protected static int getMinCornerHeight(Context context, int x, int z, int width, int height) {
        int[] is = Structure.getCornerHeights(context, x, width, z, height);
        return Math.min(Math.min(is[0], is[1]), Math.min(is[2], is[3]));
    }

    @Deprecated
    protected BlockPos getShiftedPos(Context context, BlockRotation rotation) {
        int i = 5;
        int j = 5;
        if (rotation == BlockRotation.CLOCKWISE_90) {
            i = -5;
        } else if (rotation == BlockRotation.CLOCKWISE_180) {
            i = -5;
            j = -5;
        } else if (rotation == BlockRotation.COUNTERCLOCKWISE_90) {
            j = -5;
        }
        ChunkPos chunkPos = context.chunkPos();
        int k = chunkPos.getOffsetX(7);
        int l = chunkPos.getOffsetZ(7);
        return new BlockPos(k, Structure.getMinCornerHeight(context, k, l, i, j), l);
    }

    protected abstract Optional<StructurePosition> getStructurePosition(Context var1);

    public Optional<StructurePosition> getValidStructurePosition(Context context) {
        return this.getStructurePosition(context).filter(position -> Structure.isBiomeValid(position, context));
    }

    public abstract StructureType<?> getType();

    public static final class Config
    extends Record {
        final RegistryEntryList<Biome> biomes;
        final Map<SpawnGroup, StructureSpawns> spawnOverrides;
        final GenerationStep.Feature step;
        final StructureTerrainAdaptation terrainAdaptation;
        static final Config DEFAULT = new Config(RegistryEntryList.of(new RegistryEntry[0]), Map.of(), GenerationStep.Feature.SURFACE_STRUCTURES, StructureTerrainAdaptation.NONE);
        public static final MapCodec<Config> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.BIOME).fieldOf("biomes").forGetter(Config::biomes), (App)Codec.simpleMap(SpawnGroup.CODEC, StructureSpawns.CODEC, (Keyable)StringIdentifiable.toKeyable(SpawnGroup.values())).fieldOf("spawn_overrides").forGetter(Config::spawnOverrides), (App)GenerationStep.Feature.CODEC.fieldOf("step").forGetter(Config::step), (App)StructureTerrainAdaptation.CODEC.optionalFieldOf("terrain_adaptation", (Object)Config.DEFAULT.terrainAdaptation).forGetter(Config::terrainAdaptation)).apply((Applicative)instance, Config::new));

        public Config(RegistryEntryList<Biome> biomes) {
            this(biomes, Config.DEFAULT.spawnOverrides, Config.DEFAULT.step, Config.DEFAULT.terrainAdaptation);
        }

        public Config(RegistryEntryList<Biome> biomes, Map<SpawnGroup, StructureSpawns> spawnOverrides, GenerationStep.Feature step, StructureTerrainAdaptation terrainAdaptation) {
            this.biomes = biomes;
            this.spawnOverrides = spawnOverrides;
            this.step = step;
            this.terrainAdaptation = terrainAdaptation;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Config.class, "biomes;spawnOverrides;step;terrainAdaptation", "biomes", "spawnOverrides", "step", "terrainAdaptation"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Config.class, "biomes;spawnOverrides;step;terrainAdaptation", "biomes", "spawnOverrides", "step", "terrainAdaptation"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Config.class, "biomes;spawnOverrides;step;terrainAdaptation", "biomes", "spawnOverrides", "step", "terrainAdaptation"}, this, object);
        }

        public RegistryEntryList<Biome> biomes() {
            return this.biomes;
        }

        public Map<SpawnGroup, StructureSpawns> spawnOverrides() {
            return this.spawnOverrides;
        }

        public GenerationStep.Feature step() {
            return this.step;
        }

        public StructureTerrainAdaptation terrainAdaptation() {
            return this.terrainAdaptation;
        }

        public static class Builder {
            private final RegistryEntryList<Biome> biomes;
            private Map<SpawnGroup, StructureSpawns> spawnOverrides;
            private GenerationStep.Feature step;
            private StructureTerrainAdaptation terrainAdaptation;

            public Builder(RegistryEntryList<Biome> biomes) {
                this.spawnOverrides = Config.DEFAULT.spawnOverrides;
                this.step = Config.DEFAULT.step;
                this.terrainAdaptation = Config.DEFAULT.terrainAdaptation;
                this.biomes = biomes;
            }

            public Builder spawnOverrides(Map<SpawnGroup, StructureSpawns> spawnOverrides) {
                this.spawnOverrides = spawnOverrides;
                return this;
            }

            public Builder step(GenerationStep.Feature step) {
                this.step = step;
                return this;
            }

            public Builder terrainAdaptation(StructureTerrainAdaptation terrainAdaptation) {
                this.terrainAdaptation = terrainAdaptation;
                return this;
            }

            public Config build() {
                return new Config(this.biomes, this.spawnOverrides, this.step, this.terrainAdaptation);
            }
        }
    }

    public static final class Context
    extends Record {
        private final DynamicRegistryManager dynamicRegistryManager;
        final ChunkGenerator chunkGenerator;
        private final BiomeSource biomeSource;
        final NoiseConfig noiseConfig;
        private final StructureTemplateManager structureTemplateManager;
        private final ChunkRandom random;
        private final long seed;
        private final ChunkPos chunkPos;
        private final HeightLimitView world;
        final Predicate<RegistryEntry<Biome>> biomePredicate;

        public Context(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, HeightLimitView world, Predicate<RegistryEntry<Biome>> biomePredicate) {
            this(dynamicRegistryManager, chunkGenerator, biomeSource, noiseConfig, structureTemplateManager, Context.createChunkRandom(seed, chunkPos), seed, chunkPos, world, biomePredicate);
        }

        public Context(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, NoiseConfig noiseConfig, StructureTemplateManager structureTemplateManager, ChunkRandom random, long seed, ChunkPos chunkPos, HeightLimitView world, Predicate<RegistryEntry<Biome>> biomePredicate) {
            this.dynamicRegistryManager = dynamicRegistryManager;
            this.chunkGenerator = chunkGenerator;
            this.biomeSource = biomeSource;
            this.noiseConfig = noiseConfig;
            this.structureTemplateManager = structureTemplateManager;
            this.random = random;
            this.seed = seed;
            this.chunkPos = chunkPos;
            this.world = world;
            this.biomePredicate = biomePredicate;
        }

        private static ChunkRandom createChunkRandom(long seed, ChunkPos chunkPos) {
            ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
            chunkRandom.setCarverSeed(seed, chunkPos.x, chunkPos.z);
            return chunkRandom;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Context.class, "registryAccess;chunkGenerator;biomeSource;randomState;structureTemplateManager;random;seed;chunkPos;heightAccessor;validBiome", "dynamicRegistryManager", "chunkGenerator", "biomeSource", "noiseConfig", "structureTemplateManager", "random", "seed", "chunkPos", "world", "biomePredicate"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Context.class, "registryAccess;chunkGenerator;biomeSource;randomState;structureTemplateManager;random;seed;chunkPos;heightAccessor;validBiome", "dynamicRegistryManager", "chunkGenerator", "biomeSource", "noiseConfig", "structureTemplateManager", "random", "seed", "chunkPos", "world", "biomePredicate"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Context.class, "registryAccess;chunkGenerator;biomeSource;randomState;structureTemplateManager;random;seed;chunkPos;heightAccessor;validBiome", "dynamicRegistryManager", "chunkGenerator", "biomeSource", "noiseConfig", "structureTemplateManager", "random", "seed", "chunkPos", "world", "biomePredicate"}, this, object);
        }

        public DynamicRegistryManager dynamicRegistryManager() {
            return this.dynamicRegistryManager;
        }

        public ChunkGenerator chunkGenerator() {
            return this.chunkGenerator;
        }

        public BiomeSource biomeSource() {
            return this.biomeSource;
        }

        public NoiseConfig noiseConfig() {
            return this.noiseConfig;
        }

        public StructureTemplateManager structureTemplateManager() {
            return this.structureTemplateManager;
        }

        public ChunkRandom random() {
            return this.random;
        }

        public long seed() {
            return this.seed;
        }

        public ChunkPos chunkPos() {
            return this.chunkPos;
        }

        public HeightLimitView world() {
            return this.world;
        }

        public Predicate<RegistryEntry<Biome>> biomePredicate() {
            return this.biomePredicate;
        }
    }

    public record StructurePosition(BlockPos position, Either<Consumer<StructurePiecesCollector>, StructurePiecesCollector> generator) {
        public StructurePosition(BlockPos pos, Consumer<StructurePiecesCollector> generator) {
            this(pos, (Either<Consumer<StructurePiecesCollector>, StructurePiecesCollector>)Either.left(generator));
        }

        public StructurePiecesCollector generate() {
            return (StructurePiecesCollector)this.generator.map(generator -> {
                StructurePiecesCollector structurePiecesCollector = new StructurePiecesCollector();
                generator.accept(structurePiecesCollector);
                return structurePiecesCollector;
            }, collector -> collector);
        }
    }
}

