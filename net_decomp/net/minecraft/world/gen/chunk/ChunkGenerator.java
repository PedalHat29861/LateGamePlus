/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructurePresence;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureDebugLogger;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;

public abstract class ChunkGenerator {
    public static final Codec<ChunkGenerator> CODEC = Registries.CHUNK_GENERATOR.getCodec().dispatchStable(ChunkGenerator::getCodec, Function.identity());
    protected final BiomeSource biomeSource;
    private final Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;
    private final Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter;

    public ChunkGenerator(BiomeSource biomeSource) {
        this(biomeSource, biomeEntry -> ((Biome)biomeEntry.value()).getGenerationSettings());
    }

    public ChunkGenerator(BiomeSource biomeSource, Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter) {
        this.biomeSource = biomeSource;
        this.generationSettingsGetter = generationSettingsGetter;
        this.indexedFeaturesListSupplier = Suppliers.memoize(() -> PlacedFeatureIndexer.collectIndexedFeatures(List.copyOf(biomeSource.getBiomes()), biomeEntry -> ((GenerationSettings)generationSettingsGetter.apply((RegistryEntry<Biome>)biomeEntry)).getFeatures(), true));
    }

    public void initializeIndexedFeaturesList() {
        this.indexedFeaturesListSupplier.get();
    }

    protected abstract MapCodec<? extends ChunkGenerator> getCodec();

    public StructurePlacementCalculator createStructurePlacementCalculator(RegistryWrapper<StructureSet> structureSetRegistry, NoiseConfig noiseConfig, long seed) {
        return StructurePlacementCalculator.create(noiseConfig, seed, this.biomeSource, structureSetRegistry);
    }

    public Optional<RegistryKey<MapCodec<? extends ChunkGenerator>>> getCodecKey() {
        return Registries.CHUNK_GENERATOR.getKey(this.getCodec());
    }

    public CompletableFuture<Chunk> populateBiomes(NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(() -> {
            chunk.populateBiomes(this.biomeSource, noiseConfig.getMultiNoiseSampler());
            return chunk;
        }, Util.getMainWorkerExecutor().named("init_biomes"));
    }

    public abstract void carve(ChunkRegion var1, long var2, NoiseConfig var4, BiomeAccess var5, StructureAccessor var6, Chunk var7);

    public @Nullable Pair<BlockPos, RegistryEntry<Structure>> locateStructure(ServerWorld world, RegistryEntryList<Structure> structures, BlockPos center, int radius, boolean skipReferencedStructures) {
        if (SharedConstants.DISABLE_FEATURES) {
            return null;
        }
        StructurePlacementCalculator structurePlacementCalculator = world.getChunkManager().getStructurePlacementCalculator();
        Object2ObjectArrayMap map = new Object2ObjectArrayMap();
        for (RegistryEntry registryEntry : structures) {
            for (StructurePlacement structurePlacement : structurePlacementCalculator.getPlacements(registryEntry)) {
                map.computeIfAbsent(structurePlacement, placement -> new ObjectArraySet()).add(registryEntry);
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        StructureAccessor structureAccessor = world.getStructureAccessor();
        ArrayList list = new ArrayList(map.size());
        for (Map.Entry entry : map.entrySet()) {
            StructurePlacement structurePlacement2 = (StructurePlacement)entry.getKey();
            if (structurePlacement2 instanceof ConcentricRingsStructurePlacement) {
                BlockPos blockPos;
                double e;
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement2;
                Pair<BlockPos, RegistryEntry<Structure>> pair2 = this.locateConcentricRingsStructure((Set)entry.getValue(), world, structureAccessor, center, skipReferencedStructures, concentricRingsStructurePlacement);
                if (pair2 == null || !((e = center.getSquaredDistance(blockPos = (BlockPos)pair2.getFirst())) < d)) continue;
                d = e;
                pair = pair2;
                continue;
            }
            if (!(structurePlacement2 instanceof RandomSpreadStructurePlacement)) continue;
            list.add(entry);
        }
        if (!list.isEmpty()) {
            int i = ChunkSectionPos.getSectionCoord(center.getX());
            int j = ChunkSectionPos.getSectionCoord(center.getZ());
            for (int k = 0; k <= radius; ++k) {
                boolean bl = false;
                for (Map.Entry entry : list) {
                    RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry.getKey();
                    Pair<BlockPos, RegistryEntry<Structure>> pair3 = ChunkGenerator.locateRandomSpreadStructure((Set)entry.getValue(), world, structureAccessor, i, j, k, skipReferencedStructures, structurePlacementCalculator.getStructureSeed(), randomSpreadStructurePlacement);
                    if (pair3 == null) continue;
                    bl = true;
                    double f = center.getSquaredDistance((Vec3i)pair3.getFirst());
                    if (!(f < d)) continue;
                    d = f;
                    pair = pair3;
                }
                if (!bl) continue;
                return pair;
            }
        }
        return pair;
    }

    private @Nullable Pair<BlockPos, RegistryEntry<Structure>> locateConcentricRingsStructure(Set<RegistryEntry<Structure>> structures, ServerWorld world, StructureAccessor structureAccessor, BlockPos center, boolean skipReferencedStructures, ConcentricRingsStructurePlacement placement) {
        List<ChunkPos> list = world.getChunkManager().getStructurePlacementCalculator().getPlacementPositions(placement);
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        Pair<BlockPos, RegistryEntry<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (ChunkPos chunkPos : list) {
            Pair<BlockPos, RegistryEntry<Structure>> pair2;
            mutable.set(ChunkSectionPos.getOffsetPos(chunkPos.x, 8), 32, ChunkSectionPos.getOffsetPos(chunkPos.z, 8));
            double e = mutable.getSquaredDistance(center);
            boolean bl = pair == null || e < d;
            if (!bl || (pair2 = ChunkGenerator.locateStructure(structures, world, structureAccessor, skipReferencedStructures, placement, chunkPos)) == null) continue;
            pair = pair2;
            d = e;
        }
        return pair;
    }

    private static @Nullable Pair<BlockPos, RegistryEntry<Structure>> locateRandomSpreadStructure(Set<RegistryEntry<Structure>> structures, WorldView world, StructureAccessor structureAccessor, int centerChunkX, int centerChunkZ, int radius, boolean skipReferencedStructures, long seed, RandomSpreadStructurePlacement placement) {
        int i = placement.getSpacing();
        for (int j = -radius; j <= radius; ++j) {
            boolean bl = j == -radius || j == radius;
            for (int k = -radius; k <= radius; ++k) {
                int m;
                int l;
                ChunkPos chunkPos;
                Pair<BlockPos, RegistryEntry<Structure>> pair;
                boolean bl2;
                boolean bl3 = bl2 = k == -radius || k == radius;
                if (!bl && !bl2 || (pair = ChunkGenerator.locateStructure(structures, world, structureAccessor, skipReferencedStructures, placement, chunkPos = placement.getStartChunk(seed, l = centerChunkX + i * j, m = centerChunkZ + i * k))) == null) continue;
                return pair;
            }
        }
        return null;
    }

    private static @Nullable Pair<BlockPos, RegistryEntry<Structure>> locateStructure(Set<RegistryEntry<Structure>> structures, WorldView world, StructureAccessor structureAccessor, boolean skipReferencedStructures, StructurePlacement placement, ChunkPos pos) {
        for (RegistryEntry<Structure> registryEntry : structures) {
            StructurePresence structurePresence = structureAccessor.getStructurePresence(pos, registryEntry.value(), placement, skipReferencedStructures);
            if (structurePresence == StructurePresence.START_NOT_PRESENT) continue;
            if (!skipReferencedStructures && structurePresence == StructurePresence.START_PRESENT) {
                return Pair.of((Object)placement.getLocatePos(pos), registryEntry);
            }
            Chunk chunk = world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structureStart = structureAccessor.getStructureStart(ChunkSectionPos.from(chunk), registryEntry.value(), chunk);
            if (structureStart == null || !structureStart.hasChildren() || skipReferencedStructures && !ChunkGenerator.checkNotReferenced(structureAccessor, structureStart)) continue;
            return Pair.of((Object)placement.getLocatePos(structureStart.getPos()), registryEntry);
        }
        return null;
    }

    private static boolean checkNotReferenced(StructureAccessor structureAccessor, StructureStart start) {
        if (start.isNeverReferenced()) {
            structureAccessor.incrementReferences(start);
            return true;
        }
        return false;
    }

    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        ChunkPos chunkPos = chunk.getPos();
        if (SharedConstants.isOutsideGenerationArea(chunkPos)) {
            return;
        }
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
        BlockPos blockPos = chunkSectionPos.getMinPos();
        RegistryWrapper.Impl registry = world.getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE);
        Map<Integer, List<Structure>> map = registry.stream().collect(Collectors.groupingBy(structureType -> structureType.getFeatureGenerationStep().ordinal()));
        List<PlacedFeatureIndexer.IndexedFeatures> list = this.indexedFeaturesListSupplier.get();
        ChunkRandom chunkRandom = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
        long l = chunkRandom.setPopulationSeed(world.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet set = new ObjectArraySet();
        ChunkPos.stream(chunkSectionPos.toChunkPos(), 1).forEach(arg_0 -> ChunkGenerator.method_39787(world, (Set)set, arg_0));
        set.retainAll(this.biomeSource.getBiomes());
        int i = list.size();
        try {
            RegistryWrapper.Impl registry2 = world.getRegistryManager().getOrThrow(RegistryKeys.PLACED_FEATURE);
            int j = Math.max(GenerationStep.Feature.values().length, i);
            for (int k = 0; k < j; ++k) {
                int m = 0;
                if (structureAccessor.shouldGenerateStructures()) {
                    List list2 = map.getOrDefault(k, Collections.emptyList());
                    for (Structure structure : list2) {
                        chunkRandom.setDecoratorSeed(l, m, k);
                        Supplier<String> supplier = () -> ChunkGenerator.method_38272((Registry)registry, structure);
                        try {
                            world.setCurrentlyGeneratingStructureName(supplier);
                            structureAccessor.getStructureStarts(chunkSectionPos, structure).forEach(start -> start.place(world, structureAccessor, this, chunkRandom, ChunkGenerator.getBlockBoxForChunk(chunk), chunkPos));
                        }
                        catch (Exception exception) {
                            CrashReport crashReport = CrashReport.create(exception, "Feature placement");
                            crashReport.addElement("Feature").add("Description", supplier::get);
                            throw new CrashException(crashReport);
                        }
                        ++m;
                    }
                }
                if (k >= i) continue;
                IntArraySet intSet = new IntArraySet();
                for (RegistryEntry registryEntry : set) {
                    List<RegistryEntryList<PlacedFeature>> list3 = this.generationSettingsGetter.apply(registryEntry).getFeatures();
                    if (k >= list3.size()) continue;
                    RegistryEntryList<PlacedFeature> registryEntryList = list3.get(k);
                    PlacedFeatureIndexer.IndexedFeatures indexedFeatures = list.get(k);
                    registryEntryList.stream().map(RegistryEntry::value).forEach(arg_0 -> ChunkGenerator.method_39788((IntSet)intSet, indexedFeatures, arg_0));
                }
                int n = intSet.size();
                int[] is = intSet.toIntArray();
                Arrays.sort(is);
                PlacedFeatureIndexer.IndexedFeatures indexedFeatures2 = list.get(k);
                for (int o = 0; o < n; ++o) {
                    int p = is[o];
                    PlacedFeature placedFeature = indexedFeatures2.features().get(p);
                    Supplier<String> supplier2 = () -> ChunkGenerator.method_38271((Registry)registry2, placedFeature);
                    chunkRandom.setDecoratorSeed(l, p, k);
                    try {
                        world.setCurrentlyGeneratingStructureName(supplier2);
                        placedFeature.generate(world, this, chunkRandom, blockPos);
                        continue;
                    }
                    catch (Exception exception2) {
                        CrashReport crashReport2 = CrashReport.create(exception2, "Feature placement");
                        crashReport2.addElement("Feature").add("Description", supplier2::get);
                        throw new CrashException(crashReport2);
                    }
                }
            }
            world.setCurrentlyGeneratingStructureName(null);
            if (SharedConstants.FEATURE_COUNT) {
                FeatureDebugLogger.incrementTotalChunksCount(world.toServerWorld());
            }
        }
        catch (Exception exception3) {
            CrashReport crashReport3 = CrashReport.create(exception3, "Biome decoration");
            crashReport3.addElement("Generation").add("CenterX", chunkPos.x).add("CenterZ", chunkPos.z).add("Decoration Seed", l);
            throw new CrashException(crashReport3);
        }
    }

    private static BlockBox getBlockBoxForChunk(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        HeightLimitView heightLimitView = chunk.getHeightLimitView();
        int k = heightLimitView.getBottomY() + 1;
        int l = heightLimitView.getTopYInclusive();
        return new BlockBox(i, k, j, i + 15, l, j + 15);
    }

    public abstract void buildSurface(ChunkRegion var1, StructureAccessor var2, NoiseConfig var3, Chunk var4);

    public abstract void populateEntities(ChunkRegion var1);

    public int getSpawnHeight(HeightLimitView world) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.biomeSource;
    }

    public abstract int getWorldHeight();

    public Pool<SpawnSettings.SpawnEntry> getEntitySpawnList(RegistryEntry<Biome> biome, StructureAccessor accessor, SpawnGroup group, BlockPos pos) {
        Map<Structure, LongSet> map = accessor.getStructureReferences(pos);
        for (Map.Entry<Structure, LongSet> entry : map.entrySet()) {
            Structure structure = entry.getKey();
            StructureSpawns structureSpawns = structure.getStructureSpawns().get(group);
            if (structureSpawns == null) continue;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structureSpawns.boundingBox() == StructureSpawns.BoundingBox.PIECE ? start -> accessor.structureContains(pos, (StructureStart)start) : start -> start.getBoundingBox().contains(pos);
            accessor.acceptStructureStarts(structure, entry.getValue(), start -> {
                if (mutableBoolean.isFalse() && predicate.test((StructureStart)start)) {
                    mutableBoolean.setTrue();
                }
            });
            if (!mutableBoolean.isTrue()) continue;
            return structureSpawns.spawns();
        }
        return biome.value().getSpawnSettings().getSpawnEntries(group);
    }

    public void setStructureStarts(DynamicRegistryManager registryManager, StructurePlacementCalculator placementCalculator, StructureAccessor structureAccessor, Chunk chunk, StructureTemplateManager structureTemplateManager, RegistryKey<World> dimension) {
        if (SharedConstants.DISABLE_STRUCTURES) {
            return;
        }
        ChunkPos chunkPos = chunk.getPos();
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunk);
        NoiseConfig noiseConfig = placementCalculator.getNoiseConfig();
        placementCalculator.getStructureSets().forEach(structureSet -> {
            StructurePlacement structurePlacement = ((StructureSet)structureSet.value()).placement();
            List<StructureSet.WeightedEntry> list = ((StructureSet)structureSet.value()).structures();
            for (StructureSet.WeightedEntry weightedEntry : list) {
                StructureStart structureStart = structureAccessor.getStructureStart(chunkSectionPos, weightedEntry.structure().value(), chunk);
                if (structureStart == null || !structureStart.hasChildren()) continue;
                return;
            }
            if (!structurePlacement.shouldGenerate(placementCalculator, chunkPos.x, chunkPos.z)) {
                return;
            }
            if (list.size() == 1) {
                this.trySetStructureStart(list.get(0), structureAccessor, registryManager, noiseConfig, structureTemplateManager, placementCalculator.getStructureSeed(), chunk, chunkPos, chunkSectionPos, dimension);
                return;
            }
            ArrayList<StructureSet.WeightedEntry> arrayList = new ArrayList<StructureSet.WeightedEntry>(list.size());
            arrayList.addAll(list);
            ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0L));
            chunkRandom.setCarverSeed(placementCalculator.getStructureSeed(), chunkPos.x, chunkPos.z);
            int i = 0;
            for (StructureSet.WeightedEntry weightedEntry2 : arrayList) {
                i += weightedEntry2.weight();
            }
            while (!arrayList.isEmpty()) {
                StructureSet.WeightedEntry weightedEntry3;
                int j = chunkRandom.nextInt(i);
                int k = 0;
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext() && (j -= (weightedEntry3 = (StructureSet.WeightedEntry)iterator.next()).weight()) >= 0) {
                    ++k;
                }
                StructureSet.WeightedEntry weightedEntry4 = (StructureSet.WeightedEntry)arrayList.get(k);
                if (this.trySetStructureStart(weightedEntry4, structureAccessor, registryManager, noiseConfig, structureTemplateManager, placementCalculator.getStructureSeed(), chunk, chunkPos, chunkSectionPos, dimension)) {
                    return;
                }
                arrayList.remove(k);
                i -= weightedEntry4.weight();
            }
        });
    }

    private boolean trySetStructureStart(StructureSet.WeightedEntry weightedEntry, StructureAccessor structureAccessor, DynamicRegistryManager dynamicRegistryManager, NoiseConfig noiseConfig, StructureTemplateManager structureManager, long seed, Chunk chunk, ChunkPos pos, ChunkSectionPos sectionPos, RegistryKey<World> dimension) {
        Structure structure = weightedEntry.structure().value();
        int i = ChunkGenerator.getStructureReferences(structureAccessor, chunk, sectionPos, structure);
        RegistryEntryList<Biome> registryEntryList = structure.getValidBiomes();
        Predicate<RegistryEntry<Biome>> predicate = registryEntryList::contains;
        StructureStart structureStart = structure.createStructureStart(weightedEntry.structure(), dimension, dynamicRegistryManager, this, this.biomeSource, noiseConfig, structureManager, seed, pos, i, chunk, predicate);
        if (structureStart.hasChildren()) {
            structureAccessor.setStructureStart(sectionPos, structure, structureStart, chunk);
            return true;
        }
        return false;
    }

    private static int getStructureReferences(StructureAccessor structureAccessor, Chunk chunk, ChunkSectionPos sectionPos, Structure structure) {
        StructureStart structureStart = structureAccessor.getStructureStart(sectionPos, structure, chunk);
        return structureStart != null ? structureStart.getReferences() : 0;
    }

    public void addStructureReferences(StructureWorldAccess world, StructureAccessor structureAccessor, Chunk chunk) {
        int i = 8;
        ChunkPos chunkPos = chunk.getPos();
        int j = chunkPos.x;
        int k = chunkPos.z;
        int l = chunkPos.getStartX();
        int m = chunkPos.getStartZ();
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunk);
        for (int n = j - 8; n <= j + 8; ++n) {
            for (int o = k - 8; o <= k + 8; ++o) {
                long p = ChunkPos.toLong(n, o);
                for (StructureStart structureStart : world.getChunk(n, o).getStructureStarts().values()) {
                    try {
                        if (!structureStart.hasChildren() || !structureStart.getBoundingBox().intersectsXZ(l, m, l + 15, m + 15)) continue;
                        structureAccessor.addStructureReference(chunkSectionPos, structureStart.getStructure(), p, chunk);
                    }
                    catch (Exception exception) {
                        CrashReport crashReport = CrashReport.create(exception, "Generating structure reference");
                        CrashReportSection crashReportSection = crashReport.addElement("Structure");
                        Optional<Registry<Structure>> optional = world.getRegistryManager().getOptional(RegistryKeys.STRUCTURE);
                        crashReportSection.add("Id", () -> optional.map(structureTypeRegistry -> structureTypeRegistry.getId(structureStart.getStructure()).toString()).orElse("UNKNOWN"));
                        crashReportSection.add("Name", () -> Registries.STRUCTURE_TYPE.getId(structureStart.getStructure().getType()).toString());
                        crashReportSection.add("Class", () -> structureStart.getStructure().getClass().getCanonicalName());
                        throw new CrashException(crashReport);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<Chunk> populateNoise(Blender var1, NoiseConfig var2, StructureAccessor var3, Chunk var4);

    public abstract int getSeaLevel();

    public abstract int getMinimumY();

    public abstract int getHeight(int var1, int var2, Heightmap.Type var3, HeightLimitView var4, NoiseConfig var5);

    public abstract VerticalBlockSample getColumnSample(int var1, int var2, HeightLimitView var3, NoiseConfig var4);

    public int getHeightOnGround(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.getHeight(x, z, heightmap, world, noiseConfig);
    }

    public int getHeightInGround(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.getHeight(x, z, heightmap, world, noiseConfig) - 1;
    }

    public abstract void appendDebugHudText(List<String> var1, NoiseConfig var2, BlockPos var3);

    @Deprecated
    public GenerationSettings getGenerationSettings(RegistryEntry<Biome> biomeEntry) {
        return this.generationSettingsGetter.apply(biomeEntry);
    }

    private static /* synthetic */ String method_38271(Registry registry, PlacedFeature placedFeature) {
        return registry.getKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
    }

    private static /* synthetic */ void method_39788(IntSet intSet, PlacedFeatureIndexer.IndexedFeatures indexedFeatures, PlacedFeature feature) {
        intSet.add(indexedFeatures.indexMapping().applyAsInt(feature));
    }

    private static /* synthetic */ String method_38272(Registry registry, Structure structure) {
        return registry.getKey(structure).map(Object::toString).orElseGet(structure::toString);
    }

    private static /* synthetic */ void method_39787(StructureWorldAccess structureWorldAccess, Set set, ChunkPos pos) {
        Chunk chunk = structureWorldAccess.getChunk(pos.x, pos.z);
        for (ChunkSection chunkSection : chunk.getSectionArray()) {
            chunkSection.getBiomeContainer().forEachValue(set::add);
        }
    }
}

