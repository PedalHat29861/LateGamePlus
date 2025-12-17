/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.ShortList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.StructureDebugData;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.SimpleGameEventDispatcher;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class WorldChunk
extends Chunk
implements DebugTrackable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockEntityTickInvoker EMPTY_BLOCK_ENTITY_TICKER = new BlockEntityTickInvoker(){

        @Override
        public void tick() {
        }

        @Override
        public boolean isRemoved() {
            return true;
        }

        @Override
        public BlockPos getPos() {
            return BlockPos.ORIGIN;
        }

        @Override
        public String getName() {
            return "<null>";
        }
    };
    private final Map<BlockPos, WrappedBlockEntityTickInvoker> blockEntityTickers = Maps.newHashMap();
    private boolean loadedToWorld;
    final World world;
    private @Nullable Supplier<ChunkLevelType> levelTypeProvider;
    private @Nullable EntityLoader entityLoader;
    private final Int2ObjectMap<GameEventDispatcher> gameEventDispatchers;
    private final ChunkTickScheduler<Block> blockTickScheduler;
    private final ChunkTickScheduler<Fluid> fluidTickScheduler;
    private UnsavedListener unsavedListener = pos -> {};

    public WorldChunk(World world, ChunkPos pos) {
        this(world, pos, UpgradeData.NO_UPGRADE_DATA, new ChunkTickScheduler<Block>(), new ChunkTickScheduler<Fluid>(), 0L, null, null, null);
    }

    public WorldChunk(World world, ChunkPos pos2, UpgradeData upgradeData, ChunkTickScheduler<Block> blockTickScheduler, ChunkTickScheduler<Fluid> fluidTickScheduler, long inhabitedTime, ChunkSection @Nullable [] sectionArrayInitializer, @Nullable EntityLoader entityLoader, @Nullable BlendingData blendingData) {
        super(pos2, upgradeData, world, world.getPalettesFactory(), inhabitedTime, sectionArrayInitializer, blendingData);
        this.world = world;
        this.gameEventDispatchers = new Int2ObjectOpenHashMap();
        for (Heightmap.Type type : Heightmap.Type.values()) {
            if (!ChunkStatus.FULL.getHeightmapTypes().contains(type)) continue;
            this.heightmaps.put(type, new Heightmap(this, type));
        }
        this.entityLoader = entityLoader;
        this.blockTickScheduler = blockTickScheduler;
        this.fluidTickScheduler = fluidTickScheduler;
    }

    public WorldChunk(ServerWorld world, ProtoChunk protoChunk, @Nullable EntityLoader entityLoader) {
        this(world, protoChunk.getPos(), protoChunk.getUpgradeData(), protoChunk.getBlockProtoTickScheduler(), protoChunk.getFluidProtoTickScheduler(), protoChunk.getInhabitedTime(), protoChunk.getSectionArray(), entityLoader, protoChunk.getBlendingData());
        if (!Collections.disjoint(protoChunk.blockEntityNbts.keySet(), protoChunk.blockEntities.keySet())) {
            LOGGER.error("Chunk at {} contains duplicated block entities", (Object)protoChunk.getPos());
        }
        for (BlockEntity blockEntity : protoChunk.getBlockEntities().values()) {
            this.setBlockEntity(blockEntity);
        }
        this.blockEntityNbts.putAll(protoChunk.getBlockEntityNbts());
        for (int i = 0; i < protoChunk.getPostProcessingLists().length; ++i) {
            this.postProcessingLists[i] = protoChunk.getPostProcessingLists()[i];
        }
        this.setStructureStarts(protoChunk.getStructureStarts());
        this.setStructureReferences(protoChunk.getStructureReferences());
        for (Map.Entry<Heightmap.Type, Heightmap> entry : protoChunk.getHeightmaps()) {
            if (!ChunkStatus.FULL.getHeightmapTypes().contains(entry.getKey())) continue;
            this.setHeightmap(entry.getKey(), entry.getValue().asLongArray());
        }
        this.chunkSkyLight = protoChunk.chunkSkyLight;
        this.setLightOn(protoChunk.isLightOn());
        this.markNeedsSaving();
    }

    public void setUnsavedListener(UnsavedListener unsavedListener) {
        this.unsavedListener = unsavedListener;
        if (this.needsSaving()) {
            unsavedListener.setUnsaved(this.pos);
        }
    }

    @Override
    public void markNeedsSaving() {
        boolean bl = this.needsSaving();
        super.markNeedsSaving();
        if (!bl) {
            this.unsavedListener.setUnsaved(this.pos);
        }
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
    public GameEventDispatcher getGameEventDispatcher(int ySectionCoord) {
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            return (GameEventDispatcher)this.gameEventDispatchers.computeIfAbsent(ySectionCoord, sectionCoord -> new SimpleGameEventDispatcher(serverWorld, ySectionCoord, this::removeGameEventDispatcher));
        }
        return super.getGameEventDispatcher(ySectionCoord);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.world.isDebugWorld()) {
            BlockState blockState = null;
            if (j == 60) {
                blockState = Blocks.BARRIER.getDefaultState();
            }
            if (j == 70) {
                blockState = DebugChunkGenerator.getBlockState(i, k);
            }
            return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
        }
        try {
            ChunkSection chunkSection;
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sectionArray.length && !(chunkSection = this.sectionArray[l]).isEmpty()) {
                return chunkSection.getBlockState(i & 0xF, j & 0xF, k & 0xF);
            }
            return Blocks.AIR.getDefaultState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Getting block state");
            CrashReportSection crashReportSection = crashReport.addElement("Block being got");
            crashReportSection.add("Location", () -> CrashReportSection.createPositionString((HeightLimitView)this, i, j, k));
            throw new CrashException(crashReport);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    public FluidState getFluidState(int x, int y, int z) {
        try {
            ChunkSection chunkSection;
            int i = this.getSectionIndex(y);
            if (i >= 0 && i < this.sectionArray.length && !(chunkSection = this.sectionArray[i]).isEmpty()) {
                return chunkSection.getFluidState(x & 0xF, y & 0xF, z & 0xF);
            }
            return Fluids.EMPTY.getDefaultState();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Getting fluid state");
            CrashReportSection crashReportSection = crashReport.addElement("Block being got");
            crashReportSection.add("Location", () -> CrashReportSection.createPositionString((HeightLimitView)this, x, y, z));
            throw new CrashException(crashReport);
        }
    }

    @Override
    public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, @Block.SetBlockStateFlag int flags) {
        World world;
        BlockEntity blockEntity;
        boolean bl5;
        int l;
        int k;
        int i = pos.getY();
        ChunkSection chunkSection = this.getSection(this.getSectionIndex(i));
        boolean bl = chunkSection.isEmpty();
        if (bl && state.isAir()) {
            return null;
        }
        int j = pos.getX() & 0xF;
        BlockState blockState = chunkSection.setBlockState(j, k = i & 0xF, l = pos.getZ() & 0xF, state);
        if (blockState == state) {
            return null;
        }
        Block block = state.getBlock();
        ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING)).trackUpdate(j, i, l, state);
        ((Heightmap)this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES)).trackUpdate(j, i, l, state);
        ((Heightmap)this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR)).trackUpdate(j, i, l, state);
        ((Heightmap)this.heightmaps.get(Heightmap.Type.WORLD_SURFACE)).trackUpdate(j, i, l, state);
        boolean bl2 = chunkSection.isEmpty();
        if (bl != bl2) {
            this.world.getChunkManager().getLightingProvider().setSectionStatus(pos, bl2);
            this.world.getChunkManager().onSectionStatusChanged(this.pos.x, ChunkSectionPos.getSectionCoord(i), this.pos.z, bl2);
        }
        if (ChunkLightProvider.needsLightUpdate(blockState, state)) {
            Profiler profiler = Profilers.get();
            profiler.push("updateSkyLightSources");
            this.chunkSkyLight.isSkyLightAccessible(this, j, i, l);
            profiler.swap("queueCheckLight");
            this.world.getChunkManager().getLightingProvider().checkBlock(pos);
            profiler.pop();
        }
        boolean bl3 = !blockState.isOf(block);
        boolean bl4 = (flags & 0x40) != 0;
        boolean bl6 = bl5 = (flags & 0x100) == 0;
        if (bl3 && blockState.hasBlockEntity() && !state.keepBlockEntityWhenReplacedWith(blockState)) {
            if (!this.world.isClient() && bl5 && (blockEntity = this.world.getBlockEntity(pos)) != null) {
                blockEntity.onBlockReplaced(pos, blockState);
            }
            this.removeBlockEntity(pos);
        }
        if ((bl3 || block instanceof AbstractRailBlock) && (world = this.world) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if ((flags & 1) != 0 || bl4) {
                blockState.onStateReplaced(serverWorld, pos, bl4);
            }
        }
        if (!chunkSection.getBlockState(j, k, l).isOf(block)) {
            return null;
        }
        if (!this.world.isClient() && (flags & 0x200) == 0) {
            state.onBlockAdded(this.world, pos, blockState, bl4);
        }
        if (state.hasBlockEntity()) {
            blockEntity = this.getBlockEntity(pos, CreationType.CHECK);
            if (blockEntity != null && !blockEntity.supports(state)) {
                LOGGER.warn("Found mismatched block entity @ {}: type = {}, state = {}", new Object[]{pos, blockEntity.getType().getRegistryEntry().registryKey().getValue(), state});
                this.removeBlockEntity(pos);
                blockEntity = null;
            }
            if (blockEntity == null) {
                blockEntity = ((BlockEntityProvider)((Object)block)).createBlockEntity(pos, state);
                if (blockEntity != null) {
                    this.addBlockEntity(blockEntity);
                }
            } else {
                blockEntity.setCachedState(state);
                this.updateTicker(blockEntity);
            }
        }
        this.markNeedsSaving();
        return blockState;
    }

    @Override
    @Deprecated
    public void addEntity(Entity entity) {
    }

    private @Nullable BlockEntity createBlockEntity(BlockPos pos) {
        BlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            return null;
        }
        return ((BlockEntityProvider)((Object)blockState.getBlock())).createBlockEntity(pos, blockState);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return this.getBlockEntity(pos, CreationType.CHECK);
    }

    public @Nullable BlockEntity getBlockEntity(BlockPos pos, CreationType creationType) {
        BlockEntity blockEntity2;
        NbtCompound nbtCompound;
        BlockEntity blockEntity = (BlockEntity)this.blockEntities.get(pos);
        if (blockEntity == null && (nbtCompound = (NbtCompound)this.blockEntityNbts.remove(pos)) != null && (blockEntity2 = this.loadBlockEntity(pos, nbtCompound)) != null) {
            return blockEntity2;
        }
        if (blockEntity == null) {
            if (creationType == CreationType.IMMEDIATE && (blockEntity = this.createBlockEntity(pos)) != null) {
                this.addBlockEntity(blockEntity);
            }
        } else if (blockEntity.isRemoved()) {
            this.blockEntities.remove(pos);
            return null;
        }
        return blockEntity;
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
        if (this.canTickBlockEntities()) {
            World world = this.world;
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.updateGameEventListener(blockEntity, serverWorld);
            }
            this.world.loadBlockEntity(blockEntity);
            this.updateTicker(blockEntity);
        }
    }

    private boolean canTickBlockEntities() {
        return this.loadedToWorld || this.world.isClient();
    }

    boolean canTickBlockEntity(BlockPos pos) {
        if (!this.world.getWorldBorder().contains(pos)) {
            return false;
        }
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            return this.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING) && serverWorld.isChunkLoaded(ChunkPos.toLong(pos));
        }
        return true;
    }

    @Override
    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getPos();
        BlockState blockState = this.getBlockState(blockPos);
        if (!blockState.hasBlockEntity()) {
            LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", new Object[]{blockEntity, blockPos, blockState});
            return;
        }
        BlockState blockState2 = blockEntity.getCachedState();
        if (blockState != blockState2) {
            if (!blockEntity.getType().supports(blockState)) {
                LOGGER.warn("Trying to set block entity {} at position {}, but state {} does not allow it", new Object[]{blockEntity, blockPos, blockState});
                return;
            }
            if (blockState.getBlock() != blockState2.getBlock()) {
                LOGGER.warn("Block state mismatch on block entity {} in position {}, {} != {}, updating", new Object[]{blockEntity, blockPos, blockState, blockState2});
            }
            blockEntity.setCachedState(blockState);
        }
        blockEntity.setWorld(this.world);
        blockEntity.cancelRemoval();
        BlockEntity blockEntity2 = this.blockEntities.put(blockPos.toImmutable(), blockEntity);
        if (blockEntity2 != null && blockEntity2 != blockEntity) {
            blockEntity2.markRemoved();
        }
    }

    @Override
    public @Nullable NbtCompound getPackedBlockEntityNbt(BlockPos pos, RegistryWrapper.WrapperLookup registries) {
        BlockEntity blockEntity = this.getBlockEntity(pos);
        if (blockEntity != null && !blockEntity.isRemoved()) {
            NbtCompound nbtCompound = blockEntity.createNbtWithIdentifyingData(this.world.getRegistryManager());
            nbtCompound.putBoolean("keepPacked", false);
            return nbtCompound;
        }
        NbtCompound nbtCompound = (NbtCompound)this.blockEntityNbts.get(pos);
        if (nbtCompound != null) {
            nbtCompound = nbtCompound.copy();
            nbtCompound.putBoolean("keepPacked", true);
        }
        return nbtCompound;
    }

    @Override
    public void removeBlockEntity(BlockPos pos) {
        BlockEntity blockEntity;
        if (this.canTickBlockEntities() && (blockEntity = (BlockEntity)this.blockEntities.remove(pos)) != null) {
            World world = this.world;
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.removeGameEventListener(blockEntity, serverWorld);
                serverWorld.getSubscriptionTracker().untrackBlockEntity(pos);
            }
            blockEntity.markRemoved();
        }
        this.removeBlockEntityTicker(pos);
    }

    private <T extends BlockEntity> void removeGameEventListener(T blockEntity, ServerWorld world) {
        GameEventListener gameEventListener;
        Block block = blockEntity.getCachedState().getBlock();
        if (block instanceof BlockEntityProvider && (gameEventListener = ((BlockEntityProvider)((Object)block)).getGameEventListener(world, blockEntity)) != null) {
            int i = ChunkSectionPos.getSectionCoord(blockEntity.getPos().getY());
            GameEventDispatcher gameEventDispatcher = this.getGameEventDispatcher(i);
            gameEventDispatcher.removeListener(gameEventListener);
        }
    }

    private void removeGameEventDispatcher(int ySectionCoord) {
        this.gameEventDispatchers.remove(ySectionCoord);
    }

    private void removeBlockEntityTicker(BlockPos pos) {
        WrappedBlockEntityTickInvoker wrappedBlockEntityTickInvoker = this.blockEntityTickers.remove(pos);
        if (wrappedBlockEntityTickInvoker != null) {
            wrappedBlockEntityTickInvoker.setWrapped(EMPTY_BLOCK_ENTITY_TICKER);
        }
    }

    public void loadEntities() {
        if (this.entityLoader != null) {
            this.entityLoader.run(this);
            this.entityLoader = null;
        }
    }

    public boolean isEmpty() {
        return false;
    }

    public void loadFromPacket(PacketByteBuf buf, Map<Heightmap.Type, long[]> heightmaps, Consumer<ChunkData.BlockEntityVisitor> blockEntityVisitorConsumer) {
        this.clear();
        for (ChunkSection chunkSection : this.sectionArray) {
            chunkSection.readDataPacket(buf);
        }
        heightmaps.forEach(this::setHeightmap);
        this.refreshSurfaceY();
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.getErrorReporterContext(), LOGGER);){
            blockEntityVisitorConsumer.accept((pos, blockEntityType, nbt) -> {
                BlockEntity blockEntity = this.getBlockEntity(pos, CreationType.IMMEDIATE);
                if (blockEntity != null && nbt != null && blockEntity.getType() == blockEntityType) {
                    blockEntity.read(NbtReadView.create(logging.makeChild(blockEntity.getReporterContext()), this.world.getRegistryManager(), nbt));
                }
            });
        }
    }

    public void loadBiomeFromPacket(PacketByteBuf buf) {
        for (ChunkSection chunkSection : this.sectionArray) {
            chunkSection.readBiomePacket(buf);
        }
    }

    public void setLoadedToWorld(boolean loadedToWorld) {
        this.loadedToWorld = loadedToWorld;
    }

    public World getWorld() {
        return this.world;
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void runPostProcessing(ServerWorld world) {
        ChunkPos chunkPos = this.getPos();
        for (int i = 0; i < this.postProcessingLists.length; ++i) {
            ShortList shortList = this.postProcessingLists[i];
            if (shortList == null) continue;
            for (Short short_ : shortList) {
                BlockState blockState2;
                BlockPos blockPos = ProtoChunk.joinBlockPos(short_, this.sectionIndexToCoord(i), chunkPos);
                BlockState blockState = this.getBlockState(blockPos);
                FluidState fluidState = blockState.getFluidState();
                if (!fluidState.isEmpty()) {
                    fluidState.onScheduledTick(world, blockPos, blockState);
                }
                if (blockState.getBlock() instanceof FluidBlock || (blockState2 = Block.postProcessState(blockState, world, blockPos)) == blockState) continue;
                world.setBlockState(blockPos, blockState2, 276);
            }
            shortList.clear();
        }
        for (BlockPos blockPos2 : ImmutableList.copyOf(this.blockEntityNbts.keySet())) {
            this.getBlockEntity(blockPos2);
        }
        this.blockEntityNbts.clear();
        this.upgradeData.upgrade(this);
    }

    private @Nullable BlockEntity loadBlockEntity(BlockPos pos, NbtCompound nbt) {
        BlockEntity blockEntity;
        BlockState blockState = this.getBlockState(pos);
        if ("DUMMY".equals(nbt.getString("id", ""))) {
            if (blockState.hasBlockEntity()) {
                blockEntity = ((BlockEntityProvider)((Object)blockState.getBlock())).createBlockEntity(pos, blockState);
            } else {
                blockEntity = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", (Object)pos, (Object)blockState);
            }
        } else {
            blockEntity = BlockEntity.createFromNbt(pos, blockState, nbt, this.world.getRegistryManager());
        }
        if (blockEntity != null) {
            blockEntity.setWorld(this.world);
            this.addBlockEntity(blockEntity);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", (Object)blockState, (Object)pos);
        }
        return blockEntity;
    }

    public void disableTickSchedulers(long time) {
        this.blockTickScheduler.disable(time);
        this.fluidTickScheduler.disable(time);
    }

    public void addChunkTickSchedulers(ServerWorld world) {
        ((WorldTickScheduler)world.getBlockTickScheduler()).addChunkTickScheduler(this.pos, this.blockTickScheduler);
        ((WorldTickScheduler)world.getFluidTickScheduler()).addChunkTickScheduler(this.pos, this.fluidTickScheduler);
    }

    public void removeChunkTickSchedulers(ServerWorld world) {
        ((WorldTickScheduler)world.getBlockTickScheduler()).removeChunkTickScheduler(this.pos);
        ((WorldTickScheduler)world.getFluidTickScheduler()).removeChunkTickScheduler(this.pos);
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        if (!this.getStructureStarts().isEmpty()) {
            tracker.track(DebugSubscriptionTypes.STRUCTURES, () -> {
                ArrayList<StructureDebugData> list = new ArrayList<StructureDebugData>();
                for (StructureStart structureStart : this.getStructureStarts().values()) {
                    BlockBox blockBox = structureStart.getBoundingBox();
                    List<StructurePiece> list2 = structureStart.getChildren();
                    ArrayList<StructureDebugData.Piece> list3 = new ArrayList<StructureDebugData.Piece>(list2.size());
                    for (int i = 0; i < list2.size(); ++i) {
                        boolean bl = i == 0;
                        list3.add(new StructureDebugData.Piece(list2.get(i).getBoundingBox(), bl));
                    }
                    list.add(new StructureDebugData(blockBox, list3));
                }
                return list;
            });
        }
        tracker.track(DebugSubscriptionTypes.RAIDS, () -> world.getRaidManager().getRaidCenters(this.pos));
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public ChunkLevelType getLevelType() {
        if (this.levelTypeProvider == null) {
            return ChunkLevelType.FULL;
        }
        return this.levelTypeProvider.get();
    }

    public void setLevelTypeProvider(Supplier<ChunkLevelType> levelTypeProvider) {
        this.levelTypeProvider = levelTypeProvider;
    }

    public void clear() {
        this.blockEntities.values().forEach(BlockEntity::markRemoved);
        this.blockEntities.clear();
        this.blockEntityTickers.values().forEach(ticker -> ticker.setWrapped(EMPTY_BLOCK_ENTITY_TICKER));
        this.blockEntityTickers.clear();
    }

    public void updateAllBlockEntities() {
        this.blockEntities.values().forEach(blockEntity -> {
            World world = this.world;
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                this.updateGameEventListener(blockEntity, serverWorld);
            }
            this.world.loadBlockEntity((BlockEntity)blockEntity);
            this.updateTicker(blockEntity);
        });
    }

    private <T extends BlockEntity> void updateGameEventListener(T blockEntity, ServerWorld world) {
        GameEventListener gameEventListener;
        Block block = blockEntity.getCachedState().getBlock();
        if (block instanceof BlockEntityProvider && (gameEventListener = ((BlockEntityProvider)((Object)block)).getGameEventListener(world, blockEntity)) != null) {
            this.getGameEventDispatcher(ChunkSectionPos.getSectionCoord(blockEntity.getPos().getY())).addListener(gameEventListener);
        }
    }

    private <T extends BlockEntity> void updateTicker(T blockEntity) {
        BlockState blockState = blockEntity.getCachedState();
        BlockEntityTicker<?> blockEntityTicker = blockState.getBlockEntityTicker(this.world, blockEntity.getType());
        if (blockEntityTicker == null) {
            this.removeBlockEntityTicker(blockEntity.getPos());
        } else {
            this.blockEntityTickers.compute(blockEntity.getPos(), (pos, ticker) -> {
                BlockEntityTickInvoker blockEntityTickInvoker = this.wrapTicker(blockEntity, blockEntityTicker);
                if (ticker != null) {
                    ticker.setWrapped(blockEntityTickInvoker);
                    return ticker;
                }
                if (this.canTickBlockEntities()) {
                    WrappedBlockEntityTickInvoker wrappedBlockEntityTickInvoker = new WrappedBlockEntityTickInvoker(blockEntityTickInvoker);
                    this.world.addBlockEntityTicker(wrappedBlockEntityTickInvoker);
                    return wrappedBlockEntityTickInvoker;
                }
                return null;
            });
        }
    }

    private <T extends BlockEntity> BlockEntityTickInvoker wrapTicker(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
        return new DirectBlockEntityTickInvoker(this, blockEntity, blockEntityTicker);
    }

    @FunctionalInterface
    public static interface EntityLoader {
        public void run(WorldChunk var1);
    }

    @FunctionalInterface
    public static interface UnsavedListener {
        public void setUnsaved(ChunkPos var1);
    }

    public static final class CreationType
    extends Enum<CreationType> {
        public static final /* enum */ CreationType IMMEDIATE = new CreationType();
        public static final /* enum */ CreationType QUEUED = new CreationType();
        public static final /* enum */ CreationType CHECK = new CreationType();
        private static final /* synthetic */ CreationType[] field_12862;

        public static CreationType[] values() {
            return (CreationType[])field_12862.clone();
        }

        public static CreationType valueOf(String string) {
            return Enum.valueOf(CreationType.class, string);
        }

        private static /* synthetic */ CreationType[] method_36742() {
            return new CreationType[]{IMMEDIATE, QUEUED, CHECK};
        }

        static {
            field_12862 = CreationType.method_36742();
        }
    }

    static class WrappedBlockEntityTickInvoker
    implements BlockEntityTickInvoker {
        private BlockEntityTickInvoker wrapped;

        WrappedBlockEntityTickInvoker(BlockEntityTickInvoker wrapped) {
            this.wrapped = wrapped;
        }

        void setWrapped(BlockEntityTickInvoker wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void tick() {
            this.wrapped.tick();
        }

        @Override
        public boolean isRemoved() {
            return this.wrapped.isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return this.wrapped.getPos();
        }

        @Override
        public String getName() {
            return this.wrapped.getName();
        }

        public String toString() {
            return String.valueOf(this.wrapped) + " <wrapped>";
        }
    }

    static class DirectBlockEntityTickInvoker<T extends BlockEntity>
    implements BlockEntityTickInvoker {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean hasWarned;
        final /* synthetic */ WorldChunk worldChunk;

        DirectBlockEntityTickInvoker(T blockEntity, BlockEntityTicker<T> ticker) {
            this.worldChunk = worldChunk;
            this.blockEntity = blockEntity;
            this.ticker = ticker;
        }

        @Override
        public void tick() {
            BlockPos blockPos;
            if (!((BlockEntity)this.blockEntity).isRemoved() && ((BlockEntity)this.blockEntity).hasWorld() && this.worldChunk.canTickBlockEntity(blockPos = ((BlockEntity)this.blockEntity).getPos())) {
                try {
                    Profiler profiler = Profilers.get();
                    profiler.push(this::getName);
                    BlockState blockState = this.worldChunk.getBlockState(blockPos);
                    if (((BlockEntity)this.blockEntity).getType().supports(blockState)) {
                        this.ticker.tick(this.worldChunk.world, ((BlockEntity)this.blockEntity).getPos(), blockState, this.blockEntity);
                        this.hasWarned = false;
                    } else if (!this.hasWarned) {
                        this.hasWarned = true;
                        LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", new Object[]{LogUtils.defer(this::getName), LogUtils.defer(this::getPos), blockState});
                    }
                    profiler.pop();
                }
                catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.create(throwable, "Ticking block entity");
                    CrashReportSection crashReportSection = crashReport.addElement("Block entity being ticked");
                    ((BlockEntity)this.blockEntity).populateCrashReport(crashReportSection);
                    throw new CrashException(crashReport);
                }
            }
        }

        @Override
        public boolean isRemoved() {
            return ((BlockEntity)this.blockEntity).isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return ((BlockEntity)this.blockEntity).getPos();
        }

        @Override
        public String getName() {
            return BlockEntityType.getId(((BlockEntity)this.blockEntity).getType()).toString();
        }

        public String toString() {
            return "Level ticker for " + this.getName() + "@" + String.valueOf(this.getPos());
        }
    }
}

