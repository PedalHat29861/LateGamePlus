/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.shorts.ShortArrayList
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.chunk;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtShort;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadableContainer;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import net.minecraft.world.tick.Tick;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record SerializedChunk(PalettesFactory containerFactory, ChunkPos chunkPos, int minSectionY, long lastUpdateTime, long inhabitedTime, ChunkStatus chunkStatus, @Nullable BlendingData.Serialized blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen, UpgradeData upgradeData, long @Nullable [] carvingMask, Map<Heightmap.Type, long[]> heightmaps, Chunk.TickSchedulers packedTicks, @Nullable ShortList[] postProcessingSections, boolean lightCorrect, List<SectionData> sectionData, List<NbtCompound> entities, List<NbtCompound> blockEntities, NbtCompound structureData) {
    private static final Codec<List<Tick<Block>>> BLOCK_TICKS_CODEC = Tick.createCodec(Registries.BLOCK.getCodec()).listOf();
    private static final Codec<List<Tick<Fluid>>> FLUID_TICKS_CODEC = Tick.createCodec(Registries.FLUID.getCodec()).listOf();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String UPGRADE_DATA_KEY = "UpgradeData";
    private static final String BLOCK_TICKS = "block_ticks";
    private static final String FLUID_TICKS = "fluid_ticks";
    public static final String X_POS_KEY = "xPos";
    public static final String Z_POS_KEY = "zPos";
    public static final String HEIGHTMAPS_KEY = "Heightmaps";
    public static final String IS_LIGHT_ON_KEY = "isLightOn";
    public static final String SECTIONS_KEY = "sections";
    public static final String BLOCK_LIGHT_KEY = "BlockLight";
    public static final String SKY_LIGHT_KEY = "SkyLight";

    public static @Nullable SerializedChunk fromNbt(HeightLimitView world, PalettesFactory palettesFactory, NbtCompound nbt) {
        if (nbt.getString("Status").isEmpty()) {
            return null;
        }
        ChunkPos chunkPos = new ChunkPos(nbt.getInt(X_POS_KEY, 0), nbt.getInt(Z_POS_KEY, 0));
        long l = nbt.getLong("LastUpdate", 0L);
        long m = nbt.getLong("InhabitedTime", 0L);
        ChunkStatus chunkStatus = nbt.get("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
        UpgradeData upgradeData2 = nbt.getCompound(UPGRADE_DATA_KEY).map(upgradeData -> new UpgradeData((NbtCompound)upgradeData, world)).orElse(UpgradeData.NO_UPGRADE_DATA);
        boolean bl = nbt.getBoolean(IS_LIGHT_ON_KEY, false);
        BlendingData.Serialized serialized = nbt.get("blending_data", BlendingData.Serialized.CODEC).orElse(null);
        BelowZeroRetrogen belowZeroRetrogen = nbt.get("below_zero_retrogen", BelowZeroRetrogen.CODEC).orElse(null);
        long[] ls = nbt.getLongArray("carving_mask").orElse(null);
        EnumMap<Heightmap.Type, long[]> map = new EnumMap<Heightmap.Type, long[]>(Heightmap.Type.class);
        nbt.getCompound(HEIGHTMAPS_KEY).ifPresent(heightmaps -> {
            for (Heightmap.Type type : chunkStatus.getHeightmapTypes()) {
                heightmaps.getLongArray(type.getId()).ifPresent(heightmapType -> map.put(type, (long[])heightmapType));
            }
        });
        List<Tick<Block>> list = Tick.filter(nbt.get(BLOCK_TICKS, BLOCK_TICKS_CODEC).orElse(List.of()), chunkPos);
        List<Tick<Fluid>> list2 = Tick.filter(nbt.get(FLUID_TICKS, FLUID_TICKS_CODEC).orElse(List.of()), chunkPos);
        Chunk.TickSchedulers tickSchedulers = new Chunk.TickSchedulers(list, list2);
        NbtList nbtList = nbt.getListOrEmpty("PostProcessing");
        @Nullable ShortList[] shortLists = new ShortList[nbtList.size()];
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtList nbtList2 = nbtList.getList(i).orElse(null);
            if (nbtList2 == null || nbtList2.isEmpty()) continue;
            ShortArrayList shortList = new ShortArrayList(nbtList2.size());
            for (int j = 0; j < nbtList2.size(); ++j) {
                shortList.add(nbtList2.getShort(j, (short)0));
            }
            shortLists[i] = shortList;
        }
        List<NbtCompound> list3 = nbt.getList("entities").stream().flatMap(NbtList::streamCompounds).toList();
        List<NbtCompound> list4 = nbt.getList("block_entities").stream().flatMap(NbtList::streamCompounds).toList();
        NbtCompound nbtCompound = nbt.getCompoundOrEmpty("structures");
        NbtList nbtList3 = nbt.getListOrEmpty(SECTIONS_KEY);
        ArrayList<SectionData> list5 = new ArrayList<SectionData>(nbtList3.size());
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec = palettesFactory.biomeContainerCodec();
        Codec<PalettedContainer<BlockState>> codec2 = palettesFactory.blockStatesContainerCodec();
        for (int k = 0; k < nbtList3.size(); ++k) {
            ChunkSection chunkSection;
            Optional<NbtCompound> optional = nbtList3.getCompound(k);
            if (optional.isEmpty()) continue;
            NbtCompound nbtCompound2 = optional.get();
            byte n = nbtCompound2.getByte("Y", (byte)0);
            if (n >= world.getBottomSectionCoord() && n <= world.getTopSectionCoord()) {
                PalettedContainer palettedContainer = nbtCompound2.getCompound("block_states").map(blockStates -> (PalettedContainer)codec2.parse((DynamicOps)NbtOps.INSTANCE, blockStates).promotePartial(error -> SerializedChunk.logRecoverableError(chunkPos, n, error)).getOrThrow(ChunkLoadingException::new)).orElseGet(palettesFactory::getBlockStateContainer);
                ReadableContainer readableContainer = nbtCompound2.getCompound("biomes").map(biomes -> (ReadableContainer)codec.parse((DynamicOps)NbtOps.INSTANCE, biomes).promotePartial(error -> SerializedChunk.logRecoverableError(chunkPos, n, error)).getOrThrow(ChunkLoadingException::new)).orElseGet(palettesFactory::getBiomeContainer);
                chunkSection = new ChunkSection(palettedContainer, readableContainer);
            } else {
                chunkSection = null;
            }
            ChunkNibbleArray chunkNibbleArray = nbtCompound2.getByteArray(BLOCK_LIGHT_KEY).map(ChunkNibbleArray::new).orElse(null);
            ChunkNibbleArray chunkNibbleArray2 = nbtCompound2.getByteArray(SKY_LIGHT_KEY).map(ChunkNibbleArray::new).orElse(null);
            list5.add(new SectionData(n, chunkSection, chunkNibbleArray, chunkNibbleArray2));
        }
        return new SerializedChunk(palettesFactory, chunkPos, world.getBottomSectionCoord(), l, m, chunkStatus, serialized, belowZeroRetrogen, upgradeData2, ls, map, tickSchedulers, shortLists, bl, list5, list3, list4, nbtCompound);
    }

    public ProtoChunk convert(ServerWorld world, PointOfInterestStorage poiStorage, StorageKey key, ChunkPos expectedPos) {
        Chunk chunk;
        if (!Objects.equals(expectedPos, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{expectedPos, expectedPos, this.chunkPos});
            world.getServer().onChunkMisplacement(this.chunkPos, expectedPos, key);
        }
        int i = world.countVerticalSections();
        ChunkSection[] chunkSections = new ChunkSection[i];
        boolean bl = world.getDimension().hasSkyLight();
        ServerChunkManager chunkManager = world.getChunkManager();
        LightingProvider lightingProvider = ((ChunkManager)chunkManager).getLightingProvider();
        PalettesFactory palettesFactory = world.getPalettesFactory();
        boolean bl2 = false;
        for (SectionData sectionData : this.sectionData) {
            boolean bl4;
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(expectedPos, sectionData.y);
            if (sectionData.chunkSection != null) {
                chunkSections[world.sectionCoordToIndex((int)sectionData.y)] = sectionData.chunkSection;
                poiStorage.initForPalette(chunkSectionPos, sectionData.chunkSection);
            }
            boolean bl3 = sectionData.blockLight != null;
            boolean bl5 = bl4 = bl && sectionData.skyLight != null;
            if (!bl3 && !bl4) continue;
            if (!bl2) {
                lightingProvider.setRetainData(expectedPos, true);
                bl2 = true;
            }
            if (bl3) {
                lightingProvider.enqueueSectionData(LightType.BLOCK, chunkSectionPos, sectionData.blockLight);
            }
            if (!bl4) continue;
            lightingProvider.enqueueSectionData(LightType.SKY, chunkSectionPos, sectionData.skyLight);
        }
        ChunkType chunkType = this.chunkStatus.getChunkType();
        if (chunkType == ChunkType.LEVELCHUNK) {
            ChunkTickScheduler<Block> chunkTickScheduler = new ChunkTickScheduler<Block>(this.packedTicks.blocks());
            ChunkTickScheduler<Fluid> chunkTickScheduler2 = new ChunkTickScheduler<Fluid>(this.packedTicks.fluids());
            chunk = new WorldChunk(world.toServerWorld(), expectedPos, this.upgradeData, chunkTickScheduler, chunkTickScheduler2, this.inhabitedTime, chunkSections, SerializedChunk.getEntityLoadingCallback(world, this.entities, this.blockEntities), BlendingData.fromSerialized(this.blendingData));
        } else {
            SimpleTickScheduler<Block> simpleTickScheduler = SimpleTickScheduler.tick(this.packedTicks.blocks());
            SimpleTickScheduler<Fluid> simpleTickScheduler2 = SimpleTickScheduler.tick(this.packedTicks.fluids());
            ProtoChunk protoChunk = new ProtoChunk(expectedPos, this.upgradeData, chunkSections, simpleTickScheduler, simpleTickScheduler2, world, palettesFactory, BlendingData.fromSerialized(this.blendingData));
            chunk = protoChunk;
            chunk.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protoChunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }
            protoChunk.setStatus(this.chunkStatus);
            if (this.chunkStatus.isAtLeast(ChunkStatus.INITIALIZE_LIGHT)) {
                protoChunk.setLightingProvider(lightingProvider);
            }
        }
        chunk.setLightOn(this.lightCorrect);
        EnumSet<Heightmap.Type> enumSet = EnumSet.noneOf(Heightmap.Type.class);
        for (Heightmap.Type type : chunk.getStatus().getHeightmapTypes()) {
            long[] ls = this.heightmaps.get(type);
            if (ls != null) {
                chunk.setHeightmap(type, ls);
                continue;
            }
            enumSet.add(type);
        }
        Heightmap.populateHeightmaps(chunk, enumSet);
        chunk.setStructureStarts(SerializedChunk.readStructureStarts(StructureContext.from(world), this.structureData, world.getSeed()));
        chunk.setStructureReferences(SerializedChunk.readStructureReferences(world.getRegistryManager(), expectedPos, this.structureData));
        for (int j = 0; j < this.postProcessingSections.length; ++j) {
            ShortList shortList = this.postProcessingSections[j];
            if (shortList == null) continue;
            chunk.markBlocksForPostProcessing(shortList, j);
        }
        if (chunkType == ChunkType.LEVELCHUNK) {
            return new WrapperProtoChunk((WorldChunk)chunk, false);
        }
        ProtoChunk protoChunk2 = (ProtoChunk)chunk;
        for (NbtCompound nbtCompound : this.entities) {
            protoChunk2.addEntity(nbtCompound);
        }
        for (NbtCompound nbtCompound : this.blockEntities) {
            protoChunk2.addPendingBlockEntityNbt(nbtCompound);
        }
        if (this.carvingMask != null) {
            protoChunk2.setCarvingMask(new CarvingMask(this.carvingMask, chunk.getBottomY()));
        }
        return protoChunk2;
    }

    private static void logRecoverableError(ChunkPos chunkPos, int y, String message) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{chunkPos.x, y, chunkPos.z, message});
    }

    public static SerializedChunk fromChunk(ServerWorld world, Chunk chunk) {
        if (!chunk.isSerializable()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + String.valueOf(chunk));
        }
        ChunkPos chunkPos = chunk.getPos();
        ArrayList<SectionData> list = new ArrayList<SectionData>();
        ChunkSection[] chunkSections = chunk.getSectionArray();
        ServerLightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
        for (int i = lightingProvider.getBottomY(); i < lightingProvider.getTopY(); ++i) {
            ChunkNibbleArray chunkNibbleArray4;
            int j = chunk.sectionCoordToIndex(i);
            boolean bl = j >= 0 && j < chunkSections.length;
            ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(chunkPos, i));
            ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightSection(ChunkSectionPos.from(chunkPos, i));
            ChunkNibbleArray chunkNibbleArray3 = chunkNibbleArray != null && !chunkNibbleArray.isUninitialized() ? chunkNibbleArray.copy() : null;
            ChunkNibbleArray chunkNibbleArray5 = chunkNibbleArray4 = chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized() ? chunkNibbleArray2.copy() : null;
            if (!bl && chunkNibbleArray3 == null && chunkNibbleArray4 == null) continue;
            ChunkSection chunkSection = bl ? chunkSections[j].copy() : null;
            list.add(new SectionData(i, chunkSection, chunkNibbleArray3, chunkNibbleArray4));
        }
        ArrayList<NbtCompound> list2 = new ArrayList<NbtCompound>(chunk.getBlockEntityPositions().size());
        for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
            NbtCompound nbtCompound = chunk.getPackedBlockEntityNbt(blockPos, world.getRegistryManager());
            if (nbtCompound == null) continue;
            list2.add(nbtCompound);
        }
        ArrayList<NbtCompound> list3 = new ArrayList<NbtCompound>();
        long[] ls = null;
        if (chunk.getStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            ProtoChunk protoChunk = (ProtoChunk)chunk;
            list3.addAll(protoChunk.getEntities());
            CarvingMask carvingMask = protoChunk.getCarvingMask();
            if (carvingMask != null) {
                ls = carvingMask.getMask();
            }
        }
        EnumMap<Heightmap.Type, long[]> map = new EnumMap<Heightmap.Type, long[]>(Heightmap.Type.class);
        for (Map.Entry entry : chunk.getHeightmaps()) {
            if (!chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) continue;
            long[] ms = ((Heightmap)entry.getValue()).asLongArray();
            map.put((Heightmap.Type)entry.getKey(), (long[])ms.clone());
        }
        Chunk.TickSchedulers tickSchedulers = chunk.getTickSchedulers(world.getTime());
        @Nullable ShortList[] shortListArray = (ShortList[])Arrays.stream(chunk.getPostProcessingLists()).map(postProcessings -> postProcessings != null && !postProcessings.isEmpty() ? new ShortArrayList(postProcessings) : null).toArray(ShortList[]::new);
        NbtCompound nbtCompound2 = SerializedChunk.writeStructures(StructureContext.from(world), chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences());
        return new SerializedChunk(world.getPalettesFactory(), chunkPos, chunk.getBottomSectionCoord(), world.getTime(), chunk.getInhabitedTime(), chunk.getStatus(), Nullables.map(chunk.getBlendingData(), BlendingData::toSerialized), chunk.getBelowZeroRetrogen(), chunk.getUpgradeData().copy(), ls, map, tickSchedulers, shortListArray, chunk.isLightOn(), list, list3, list2, nbtCompound2);
    }

    public NbtCompound serialize() {
        NbtCompound nbtCompound = NbtHelper.putDataVersion(new NbtCompound());
        nbtCompound.putInt(X_POS_KEY, this.chunkPos.x);
        nbtCompound.putInt("yPos", this.minSectionY);
        nbtCompound.putInt(Z_POS_KEY, this.chunkPos.z);
        nbtCompound.putLong("LastUpdate", this.lastUpdateTime);
        nbtCompound.putLong("InhabitedTime", this.inhabitedTime);
        nbtCompound.putString("Status", Registries.CHUNK_STATUS.getId(this.chunkStatus).toString());
        nbtCompound.putNullable("blending_data", BlendingData.Serialized.CODEC, this.blendingData);
        nbtCompound.putNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isDone()) {
            nbtCompound.put(UPGRADE_DATA_KEY, this.upgradeData.toNbt());
        }
        NbtList nbtList = new NbtList();
        Codec<PalettedContainer<BlockState>> codec = this.containerFactory.blockStatesContainerCodec();
        Codec<ReadableContainer<RegistryEntry<Biome>>> codec2 = this.containerFactory.biomeContainerCodec();
        for (SectionData sectionData : this.sectionData) {
            NbtCompound nbtCompound2 = new NbtCompound();
            ChunkSection chunkSection = sectionData.chunkSection;
            if (chunkSection != null) {
                nbtCompound2.put("block_states", codec, chunkSection.getBlockStateContainer());
                nbtCompound2.put("biomes", codec2, chunkSection.getBiomeContainer());
            }
            if (sectionData.blockLight != null) {
                nbtCompound2.putByteArray(BLOCK_LIGHT_KEY, sectionData.blockLight.asByteArray());
            }
            if (sectionData.skyLight != null) {
                nbtCompound2.putByteArray(SKY_LIGHT_KEY, sectionData.skyLight.asByteArray());
            }
            if (nbtCompound2.isEmpty()) continue;
            nbtCompound2.putByte("Y", (byte)sectionData.y);
            nbtList.add(nbtCompound2);
        }
        nbtCompound.put(SECTIONS_KEY, nbtList);
        if (this.lightCorrect) {
            nbtCompound.putBoolean(IS_LIGHT_ON_KEY, true);
        }
        NbtList nbtList2 = new NbtList();
        nbtList2.addAll(this.blockEntities);
        nbtCompound.put("block_entities", nbtList2);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            NbtList nbtList3 = new NbtList();
            nbtList3.addAll(this.entities);
            nbtCompound.put("entities", nbtList3);
            if (this.carvingMask != null) {
                nbtCompound.putLongArray("carving_mask", this.carvingMask);
            }
        }
        SerializedChunk.serializeTicks(nbtCompound, this.packedTicks);
        nbtCompound.put("PostProcessing", SerializedChunk.toNbt(this.postProcessingSections));
        NbtCompound nbtCompound3 = new NbtCompound();
        this.heightmaps.forEach((type, values) -> nbtCompound3.put(type.getId(), new NbtLongArray((long[])values)));
        nbtCompound.put(HEIGHTMAPS_KEY, nbtCompound3);
        nbtCompound.put("structures", this.structureData);
        return nbtCompound;
    }

    private static void serializeTicks(NbtCompound nbt, Chunk.TickSchedulers schedulers) {
        nbt.put(BLOCK_TICKS, BLOCK_TICKS_CODEC, schedulers.blocks());
        nbt.put(FLUID_TICKS, FLUID_TICKS_CODEC, schedulers.fluids());
    }

    public static ChunkStatus getChunkStatus(@Nullable NbtCompound nbt) {
        return nbt != null ? nbt.get("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
    }

    private static @Nullable WorldChunk.EntityLoader getEntityLoadingCallback(ServerWorld world, List<NbtCompound> entities, List<NbtCompound> blockEntities) {
        if (entities.isEmpty() && blockEntities.isEmpty()) {
            return null;
        }
        return chunk -> {
            if (!entities.isEmpty()) {
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(chunk.getErrorReporterContext(), LOGGER);){
                    world.loadEntities(EntityType.streamFromData(NbtReadView.createList(logging, world.getRegistryManager(), entities), world, SpawnReason.LOAD));
                }
            }
            for (NbtCompound nbtCompound : blockEntities) {
                boolean bl = nbtCompound.getBoolean("keepPacked", false);
                if (bl) {
                    chunk.addPendingBlockEntityNbt(nbtCompound);
                    continue;
                }
                BlockPos blockPos = BlockEntity.posFromNbt(chunk.getPos(), nbtCompound);
                BlockEntity blockEntity = BlockEntity.createFromNbt(blockPos, chunk.getBlockState(blockPos), nbtCompound, world.getRegistryManager());
                if (blockEntity == null) continue;
                chunk.setBlockEntity(blockEntity);
            }
        };
    }

    private static NbtCompound writeStructures(StructureContext context, ChunkPos pos, Map<Structure, StructureStart> starts, Map<Structure, LongSet> references) {
        NbtCompound nbtCompound = new NbtCompound();
        NbtCompound nbtCompound2 = new NbtCompound();
        RegistryWrapper.Impl registry = context.registryManager().getOrThrow(RegistryKeys.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            Identifier identifier = registry.getId(entry.getKey());
            nbtCompound2.put(identifier.toString(), entry.getValue().toNbt(context, pos));
        }
        nbtCompound.put("starts", nbtCompound2);
        NbtCompound nbtCompound3 = new NbtCompound();
        for (Map.Entry<Structure, LongSet> entry2 : references.entrySet()) {
            if (entry2.getValue().isEmpty()) continue;
            Identifier identifier2 = registry.getId(entry2.getKey());
            nbtCompound3.putLongArray(identifier2.toString(), entry2.getValue().toLongArray());
        }
        nbtCompound.put("References", nbtCompound3);
        return nbtCompound;
    }

    private static Map<Structure, StructureStart> readStructureStarts(StructureContext context, NbtCompound nbt, long worldSeed) {
        HashMap map = Maps.newHashMap();
        RegistryWrapper.Impl registry = context.registryManager().getOrThrow(RegistryKeys.STRUCTURE);
        NbtCompound nbtCompound = nbt.getCompoundOrEmpty("starts");
        for (String string : nbtCompound.getKeys()) {
            Identifier identifier = Identifier.tryParse(string);
            Structure structure = (Structure)registry.get(identifier);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", (Object)identifier);
                continue;
            }
            StructureStart structureStart = StructureStart.fromNbt(context, nbtCompound.getCompoundOrEmpty(string), worldSeed);
            if (structureStart == null) continue;
            map.put(structure, structureStart);
        }
        return map;
    }

    private static Map<Structure, LongSet> readStructureReferences(DynamicRegistryManager registryManager, ChunkPos pos, NbtCompound nbt) {
        HashMap map = Maps.newHashMap();
        RegistryWrapper.Impl registry = registryManager.getOrThrow(RegistryKeys.STRUCTURE);
        NbtCompound nbtCompound = nbt.getCompoundOrEmpty("References");
        nbtCompound.forEach((arg_0, arg_1) -> SerializedChunk.method_68295((Registry)registry, pos, map, arg_0, arg_1));
        return map;
    }

    private static NbtList toNbt(@Nullable ShortList[] lists) {
        NbtList nbtList = new NbtList();
        for (ShortList shortList : lists) {
            NbtList nbtList2 = new NbtList();
            if (shortList != null) {
                for (int i = 0; i < shortList.size(); ++i) {
                    nbtList2.add(NbtShort.of(shortList.getShort(i)));
                }
            }
            nbtList.add(nbtList2);
        }
        return nbtList;
    }

    private static /* synthetic */ void method_68295(Registry registry, ChunkPos chunkPos2, Map map, String id, NbtElement chunkPos) {
        Identifier identifier = Identifier.tryParse(id);
        Structure structure = (Structure)registry.get(identifier);
        if (structure == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)identifier, (Object)chunkPos2);
            return;
        }
        Optional<long[]> optional = chunkPos.asLongArray();
        if (optional.isEmpty()) {
            return;
        }
        map.put(structure, new LongOpenHashSet(Arrays.stream(optional.get()).filter(packedPos -> {
            ChunkPos chunkPos2 = new ChunkPos(packedPos);
            if (chunkPos2.getChebyshevDistance(chunkPos2) > 8) {
                LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{identifier, chunkPos2, chunkPos2});
                return false;
            }
            return true;
        }).toArray()));
    }

    public static final class SectionData
    extends Record {
        final int y;
        final @Nullable ChunkSection chunkSection;
        final @Nullable ChunkNibbleArray blockLight;
        final @Nullable ChunkNibbleArray skyLight;

        public SectionData(int y, @Nullable ChunkSection chunkSection, @Nullable ChunkNibbleArray blockLight, @Nullable ChunkNibbleArray skyLight) {
            this.y = y;
            this.chunkSection = chunkSection;
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SectionData.class, "y;chunkSection;blockLight;skyLight", "y", "chunkSection", "blockLight", "skyLight"}, this, object);
        }

        public int y() {
            return this.y;
        }

        public @Nullable ChunkSection chunkSection() {
            return this.chunkSection;
        }

        public @Nullable ChunkNibbleArray blockLight() {
            return this.blockLight;
        }

        public @Nullable ChunkNibbleArray skyLight() {
            return this.skyLight;
        }
    }

    public static class ChunkLoadingException
    extends NbtException {
        public ChunkLoadingException(String string) {
            super(string);
        }
    }
}

