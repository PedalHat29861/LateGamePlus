/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.chunk;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.SimpleTickScheduler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ProtoChunk
extends Chunk {
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile @Nullable LightingProvider lightingProvider;
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private final List<NbtCompound> entities = Lists.newArrayList();
    private @Nullable CarvingMask carvingMask;
    private @Nullable BelowZeroRetrogen belowZeroRetrogen;
    private final SimpleTickScheduler<Block> blockTickScheduler;
    private final SimpleTickScheduler<Fluid> fluidTickScheduler;

    public ProtoChunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView world, PalettesFactory palettesFactory, @Nullable BlendingData blendingData) {
        this(pos, upgradeData, null, new SimpleTickScheduler<Block>(), new SimpleTickScheduler<Fluid>(), world, palettesFactory, blendingData);
    }

    public ProtoChunk(ChunkPos pos, UpgradeData upgradeData, ChunkSection @Nullable [] sections, SimpleTickScheduler<Block> blockTickScheduler, SimpleTickScheduler<Fluid> fluidTickScheduler, HeightLimitView world, PalettesFactory palettesFactory, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, world, palettesFactory, 0L, sections, blendingData);
        this.blockTickScheduler = blockTickScheduler;
        this.fluidTickScheduler = fluidTickScheduler;
    }

    @Override
    public BasicTickScheduler<Block> getBlockTickScheduler() {
        return this.blockTickScheduler;
    }

    @Override
    public BasicTickScheduler<Fluid> getFluidTickScheduler() {
        return this.fluidTickScheduler;
    }

    @Override
    public Chunk.TickSchedulers getTickSchedulers(long time) {
        return new Chunk.TickSchedulers(this.blockTickScheduler.collectTicks(time), this.fluidTickScheduler.collectTicks(time));
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getY();
        if (this.isOutOfHeightLimit(i)) {
            return Blocks.VOID_AIR.getDefaultState();
        }
        ChunkSection chunkSection = this.getSection(this.getSectionIndex(i));
        if (chunkSection.isEmpty()) {
            return Blocks.AIR.getDefaultState();
        }
        return chunkSection.getBlockState(pos.getX() & 0xF, i & 0xF, pos.getZ() & 0xF);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        int i = pos.getY();
        if (this.isOutOfHeightLimit(i)) {
            return Fluids.EMPTY.getDefaultState();
        }
        ChunkSection chunkSection = this.getSection(this.getSectionIndex(i));
        if (chunkSection.isEmpty()) {
            return Fluids.EMPTY.getDefaultState();
        }
        return chunkSection.getFluidState(pos.getX() & 0xF, i & 0xF, pos.getZ() & 0xF);
    }

    @Override
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, @Block.SetBlockStateFlag int flags) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.isOutOfHeightLimit(j)) {
            return Blocks.VOID_AIR.getDefaultState();
        }
        int l = this.getSectionIndex(j);
        ChunkSection chunkSection = this.getSection(l);
        boolean bl = chunkSection.isEmpty();
        if (bl && state.isOf(Blocks.AIR)) {
            return state;
        }
        int m = ChunkSectionPos.getLocalCoord(i);
        int n = ChunkSectionPos.getLocalCoord(j);
        int o = ChunkSectionPos.getLocalCoord(k);
        BlockState blockState = chunkSection.setBlockState(m, n, o, state);
        if (this.status.isAtLeast(ChunkStatus.INITIALIZE_LIGHT)) {
            boolean bl2 = chunkSection.isEmpty();
            if (bl2 != bl) {
                this.lightingProvider.setSectionStatus(pos, bl2);
            }
            if (ChunkLightProvider.needsLightUpdate(blockState, state)) {
                this.chunkSkyLight.isSkyLightAccessible(this, m, j, o);
                this.lightingProvider.checkBlock(pos);
            }
        }
        EnumSet<Heightmap.Type> enumSet = this.getStatus().getHeightmapTypes();
        EnumSet<Heightmap.Type> enumSet2 = null;
        for (Heightmap.Type type : enumSet) {
            Heightmap heightmap = (Heightmap)this.heightmaps.get(type);
            if (heightmap != null) continue;
            if (enumSet2 == null) {
                enumSet2 = EnumSet.noneOf(Heightmap.Type.class);
            }
            enumSet2.add(type);
        }
        if (enumSet2 != null) {
            Heightmap.populateHeightmaps(this, enumSet2);
        }
        for (Heightmap.Type type : enumSet) {
            ((Heightmap)this.heightmaps.get(type)).trackUpdate(m, j, o, state);
        }
        return blockState;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        this.blockEntityNbts.remove(blockEntity.getPos());
        this.blockEntities.put(blockEntity.getPos(), blockEntity);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return (BlockEntity)this.blockEntities.get(pos);
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(NbtCompound entityNbt) {
        this.entities.add(entityNbt);
    }

    @Override
    public void addEntity(Entity entity) {
        if (entity.hasVehicle()) {
            return;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, entity.getRegistryManager());
            entity.saveData(nbtWriteView);
            this.addEntity(nbtWriteView.getNbt());
        }
    }

    @Override
    public void setStructureStart(Structure structure, StructureStart start) {
        BelowZeroRetrogen belowZeroRetrogen = this.getBelowZeroRetrogen();
        if (belowZeroRetrogen != null && start.hasChildren()) {
            BlockBox blockBox = start.getBoundingBox();
            HeightLimitView heightLimitView = this.getHeightLimitView();
            if (blockBox.getMinY() < heightLimitView.getBottomY() || blockBox.getMaxY() > heightLimitView.getTopYInclusive()) {
                return;
            }
        }
        super.setStructureStart(structure, start);
    }

    public List<NbtCompound> getEntities() {
        return this.entities;
    }

    @Override
    public ChunkStatus getStatus() {
        return this.status;
    }

    public void setStatus(ChunkStatus status) {
        this.status = status;
        if (this.belowZeroRetrogen != null && status.isAtLeast(this.belowZeroRetrogen.getTargetStatus())) {
            this.setBelowZeroRetrogen(null);
        }
        this.markNeedsSaving();
    }

    @Override
    public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        if (this.getMaxStatus().isAtLeast(ChunkStatus.BIOMES)) {
            return super.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
        }
        throw new IllegalStateException("Asking for biomes before we have biomes");
    }

    public static short getPackedSectionRelative(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        int l = i & 0xF;
        int m = j & 0xF;
        int n = k & 0xF;
        return (short)(l | m << 4 | n << 8);
    }

    public static BlockPos joinBlockPos(short sectionRel, int sectionY, ChunkPos chunkPos) {
        int i = ChunkSectionPos.getOffsetPos(chunkPos.x, sectionRel & 0xF);
        int j = ChunkSectionPos.getOffsetPos(sectionY, sectionRel >>> 4 & 0xF);
        int k = ChunkSectionPos.getOffsetPos(chunkPos.z, sectionRel >>> 8 & 0xF);
        return new BlockPos(i, j, k);
    }

    @Override
    public void markBlockForPostProcessing(BlockPos pos) {
        if (!this.isOutOfHeightLimit(pos)) {
            Chunk.getList(this.postProcessingLists, this.getSectionIndex(pos.getY())).add(ProtoChunk.getPackedSectionRelative(pos));
        }
    }

    @Override
    public void markBlocksForPostProcessing(ShortList packedPositions, int index) {
        Chunk.getList(this.postProcessingLists, index).addAll(packedPositions);
    }

    public Map<BlockPos, NbtCompound> getBlockEntityNbts() {
        return Collections.unmodifiableMap(this.blockEntityNbts);
    }

    @Override
    public @Nullable NbtCompound getPackedBlockEntityNbt(BlockPos pos, RegistryWrapper.WrapperLookup registries) {
        BlockEntity blockEntity = this.getBlockEntity(pos);
        if (blockEntity != null) {
            return blockEntity.createNbtWithIdentifyingData(registries);
        }
        return (NbtCompound)this.blockEntityNbts.get(pos);
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        this.blockEntities.remove(pos);
        this.blockEntityNbts.remove(pos);
    }

    public @Nullable CarvingMask getCarvingMask() {
        return this.carvingMask;
    }

    public CarvingMask getOrCreateCarvingMask() {
        if (this.carvingMask == null) {
            this.carvingMask = new CarvingMask(this.getHeight(), this.getBottomY());
        }
        return this.carvingMask;
    }

    public void setCarvingMask(CarvingMask carvingMask) {
        this.carvingMask = carvingMask;
    }

    public void setLightingProvider(LightingProvider lightingProvider) {
        this.lightingProvider = lightingProvider;
    }

    public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen) {
        this.belowZeroRetrogen = belowZeroRetrogen;
    }

    @Override
    public @Nullable BelowZeroRetrogen getBelowZeroRetrogen() {
        return this.belowZeroRetrogen;
    }

    private static <T> ChunkTickScheduler<T> createProtoTickScheduler(SimpleTickScheduler<T> tickScheduler) {
        return new ChunkTickScheduler<T>(tickScheduler.getTicks());
    }

    public ChunkTickScheduler<Block> getBlockProtoTickScheduler() {
        return ProtoChunk.createProtoTickScheduler(this.blockTickScheduler);
    }

    public ChunkTickScheduler<Fluid> getFluidProtoTickScheduler() {
        return ProtoChunk.createProtoTickScheduler(this.fluidTickScheduler);
    }

    @Override
    public HeightLimitView getHeightLimitView() {
        if (this.hasBelowZeroRetrogen()) {
            return BelowZeroRetrogen.BELOW_ZERO_VIEW;
        }
        return this;
    }
}

