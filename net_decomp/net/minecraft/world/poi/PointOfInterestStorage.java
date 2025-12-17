/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.world.ChunkErrorHandler;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.SectionDistanceLevelPropagator;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.debug.data.PoiDebugData;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.jspecify.annotations.Nullable;

public class PointOfInterestStorage
extends SerializingRegionBasedStorage<PointOfInterestSet, PointOfInterestSet.Serialized> {
    public static final int field_30265 = 6;
    public static final int field_30266 = 1;
    private final PointOfInterestDistanceTracker pointOfInterestDistanceTracker;
    private final LongSet preloadedChunks = new LongOpenHashSet();

    public PointOfInterestStorage(StorageKey storageKey, Path directory, DataFixer dataFixer, boolean dsync, DynamicRegistryManager registryManager, ChunkErrorHandler errorHandler, HeightLimitView world) {
        super(new VersionedChunkStorage(storageKey, directory, dataFixer, dsync, DataFixTypes.POI_CHUNK), PointOfInterestSet.Serialized.CODEC, PointOfInterestSet::toSerialized, PointOfInterestSet.Serialized::toPointOfInterestSet, PointOfInterestSet::new, registryManager, errorHandler, world);
        this.pointOfInterestDistanceTracker = new PointOfInterestDistanceTracker();
    }

    public @Nullable PointOfInterest add(BlockPos pos, RegistryEntry<PointOfInterestType> type) {
        return ((PointOfInterestSet)this.getOrCreate(ChunkSectionPos.toLong(pos))).add(pos, type);
    }

    public void remove(BlockPos pos) {
        this.get(ChunkSectionPos.toLong(pos)).ifPresent(poiSet -> poiSet.remove(pos));
    }

    public long count(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).count();
    }

    public boolean hasTypeAt(RegistryKey<PointOfInterestType> type, BlockPos pos) {
        return this.test(pos, entry -> entry.matchesKey(type));
    }

    public Stream<PointOfInterest> getInSquare(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        int i = Math.floorDiv(radius, 16) + 1;
        return ChunkPos.stream(new ChunkPos(pos), i).flatMap(chunkPos -> this.getInChunk(typePredicate, (ChunkPos)chunkPos, occupationStatus)).filter(poi -> {
            BlockPos blockPos2 = poi.getPos();
            return Math.abs(blockPos2.getX() - pos.getX()) <= radius && Math.abs(blockPos2.getZ() - pos.getZ()) <= radius;
        });
    }

    public Stream<PointOfInterest> getInCircle(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        int i = radius * radius;
        return this.getInSquare(typePredicate, pos, radius, occupationStatus).filter(poi -> poi.getPos().getSquaredDistance(pos) <= (double)i);
    }

    @Debug
    public Stream<PointOfInterest> getInChunk(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, ChunkPos chunkPos, OccupationStatus occupationStatus) {
        return IntStream.rangeClosed(this.world.getBottomSectionCoord(), this.world.getTopSectionCoord()).boxed().map(coord -> this.get(ChunkSectionPos.from(chunkPos, coord).asLong())).filter(Optional::isPresent).flatMap(poiSet -> ((PointOfInterestSet)poiSet.get()).get(typePredicate, occupationStatus));
    }

    public Stream<BlockPos> getPositions(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).map(PointOfInterest::getPos).filter(posPredicate);
    }

    public Stream<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getTypesAndPositions(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).filter(poi -> posPredicate.test(poi.getPos())).map(poi -> Pair.of(poi.getType(), (Object)poi.getPos()));
    }

    public Stream<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getSortedTypesAndPositions(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getTypesAndPositions(typePredicate, posPredicate, pos, radius, occupationStatus).sorted(Comparator.comparingDouble(pair -> ((BlockPos)pair.getSecond()).getSquaredDistance(pos)));
    }

    public Optional<BlockPos> getPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getPositions(typePredicate, posPredicate, pos, radius, occupationStatus).findFirst();
    }

    public Optional<BlockPos> getNearestPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).map(PointOfInterest::getPos).min(Comparator.comparingDouble(poiPos -> poiPos.getSquaredDistance(pos)));
    }

    public Optional<Pair<RegistryEntry<PointOfInterestType>, BlockPos>> getNearestTypeAndPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).min(Comparator.comparingDouble(poi -> poi.getPos().getSquaredDistance(pos))).map(poi -> Pair.of(poi.getType(), (Object)poi.getPos()));
    }

    public Optional<BlockPos> getNearestPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> posPredicate, BlockPos pos, int radius, OccupationStatus occupationStatus) {
        return this.getInCircle(typePredicate, pos, radius, occupationStatus).map(PointOfInterest::getPos).filter(posPredicate).min(Comparator.comparingDouble(poiPos -> poiPos.getSquaredDistance(pos)));
    }

    public Optional<BlockPos> getPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, BiPredicate<RegistryEntry<PointOfInterestType>, BlockPos> posPredicate, BlockPos pos, int radius) {
        return this.getInCircle(typePredicate, pos, radius, OccupationStatus.HAS_SPACE).filter(poi -> posPredicate.test(poi.getType(), poi.getPos())).findFirst().map(poi -> {
            poi.reserveTicket();
            return poi.getPos();
        });
    }

    public Optional<BlockPos> getPosition(Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> positionPredicate, OccupationStatus occupationStatus, BlockPos pos, int radius, Random random) {
        List<PointOfInterest> list = Util.copyShuffled(this.getInCircle(typePredicate, pos, radius, occupationStatus), random);
        return list.stream().filter(poi -> positionPredicate.test(poi.getPos())).findFirst().map(PointOfInterest::getPos);
    }

    public boolean releaseTicket(BlockPos pos) {
        return this.get(ChunkSectionPos.toLong(pos)).map(poiSet -> poiSet.releaseTicket(pos)).orElseThrow(() -> Util.getFatalOrPause(new IllegalStateException("POI never registered at " + String.valueOf(pos))));
    }

    public boolean test(BlockPos pos, Predicate<RegistryEntry<PointOfInterestType>> predicate) {
        return this.get(ChunkSectionPos.toLong(pos)).map(poiSet -> poiSet.test(pos, predicate)).orElse(false);
    }

    public Optional<RegistryEntry<PointOfInterestType>> getType(BlockPos pos) {
        return this.get(ChunkSectionPos.toLong(pos)).flatMap(poiSet -> poiSet.getType(pos));
    }

    @Debug
    public @Nullable PoiDebugData getDebugData(BlockPos pos) {
        return this.get(ChunkSectionPos.toLong(pos)).flatMap(poiSet -> poiSet.getDebugData(pos)).orElse(null);
    }

    public int getDistanceFromNearestOccupied(ChunkSectionPos pos) {
        this.pointOfInterestDistanceTracker.update();
        return this.pointOfInterestDistanceTracker.getLevel(pos.asLong());
    }

    boolean isOccupied(long pos) {
        Optional optional = this.getIfLoaded(pos);
        if (optional == null) {
            return false;
        }
        return optional.map(poiSet -> poiSet.get(entry -> entry.isIn(PointOfInterestTypeTags.VILLAGE), OccupationStatus.IS_OCCUPIED).findAny().isPresent()).orElse(false);
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        super.tick(shouldKeepTicking);
        this.pointOfInterestDistanceTracker.update();
    }

    @Override
    protected void onUpdate(long pos) {
        super.onUpdate(pos);
        this.pointOfInterestDistanceTracker.update(pos, this.pointOfInterestDistanceTracker.getInitialLevel(pos), false);
    }

    @Override
    protected void onLoad(long pos) {
        this.pointOfInterestDistanceTracker.update(pos, this.pointOfInterestDistanceTracker.getInitialLevel(pos), false);
    }

    public void initForPalette(ChunkSectionPos sectionPos, ChunkSection chunkSection) {
        Util.ifPresentOrElse(this.get(sectionPos.asLong()), poiSet -> poiSet.updatePointsOfInterest(populator -> {
            if (PointOfInterestStorage.shouldScan(chunkSection)) {
                this.scanAndPopulate(chunkSection, sectionPos, (BiConsumer<BlockPos, RegistryEntry<PointOfInterestType>>)populator);
            }
        }), () -> {
            if (PointOfInterestStorage.shouldScan(chunkSection)) {
                PointOfInterestSet pointOfInterestSet = (PointOfInterestSet)this.getOrCreate(sectionPos.asLong());
                this.scanAndPopulate(chunkSection, sectionPos, pointOfInterestSet::add);
            }
        });
    }

    private static boolean shouldScan(ChunkSection chunkSection) {
        return chunkSection.hasAny(PointOfInterestTypes::isPointOfInterest);
    }

    private void scanAndPopulate(ChunkSection chunkSection, ChunkSectionPos sectionPos, BiConsumer<BlockPos, RegistryEntry<PointOfInterestType>> populator) {
        sectionPos.streamBlocks().forEach(pos -> {
            BlockState blockState = chunkSection.getBlockState(ChunkSectionPos.getLocalCoord(pos.getX()), ChunkSectionPos.getLocalCoord(pos.getY()), ChunkSectionPos.getLocalCoord(pos.getZ()));
            PointOfInterestTypes.getTypeForState(blockState).ifPresent(poiType -> populator.accept((BlockPos)pos, (RegistryEntry<PointOfInterestType>)poiType));
        });
    }

    public void preloadChunks(WorldView world, BlockPos pos, int radius) {
        ChunkSectionPos.stream(new ChunkPos(pos), Math.floorDiv(radius, 16), this.world.getBottomSectionCoord(), this.world.getTopSectionCoord()).map(sectionPos -> Pair.of((Object)sectionPos, this.get(sectionPos.asLong()))).filter(pair -> ((Optional)pair.getSecond()).map(PointOfInterestSet::isValid).orElse(false) == false).map(pair -> ((ChunkSectionPos)pair.getFirst()).toChunkPos()).filter(chunkPos -> this.preloadedChunks.add(chunkPos.toLong())).forEach(chunkPos -> world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.EMPTY));
    }

    final class PointOfInterestDistanceTracker
    extends SectionDistanceLevelPropagator {
        private final Long2ByteMap distances;

        protected PointOfInterestDistanceTracker() {
            super(7, 16, 256);
            this.distances = new Long2ByteOpenHashMap();
            this.distances.defaultReturnValue((byte)7);
        }

        @Override
        protected int getInitialLevel(long id) {
            return PointOfInterestStorage.this.isOccupied(id) ? 0 : 7;
        }

        @Override
        protected int getLevel(long id) {
            return this.distances.get(id);
        }

        @Override
        protected void setLevel(long id, int level) {
            if (level > 6) {
                this.distances.remove(id);
            } else {
                this.distances.put(id, (byte)level);
            }
        }

        public void update() {
            super.applyPendingUpdates(Integer.MAX_VALUE);
        }
    }

    public static final class OccupationStatus
    extends Enum<OccupationStatus> {
        public static final /* enum */ OccupationStatus HAS_SPACE = new OccupationStatus(PointOfInterest::hasSpace);
        public static final /* enum */ OccupationStatus IS_OCCUPIED = new OccupationStatus(PointOfInterest::isOccupied);
        public static final /* enum */ OccupationStatus ANY = new OccupationStatus(poi -> true);
        private final Predicate<? super PointOfInterest> predicate;
        private static final /* synthetic */ OccupationStatus[] field_18491;

        public static OccupationStatus[] values() {
            return (OccupationStatus[])field_18491.clone();
        }

        public static OccupationStatus valueOf(String string) {
            return Enum.valueOf(OccupationStatus.class, string);
        }

        private OccupationStatus(Predicate<? super PointOfInterest> predicate) {
            this.predicate = predicate;
        }

        public Predicate<? super PointOfInterest> getPredicate() {
            return this.predicate;
        }

        private static /* synthetic */ OccupationStatus[] method_36629() {
            return new OccupationStatus[]{HAS_SPACE, IS_OCCUPIED, ANY};
        }

        static {
            field_18491 = OccupationStatus.method_36629();
        }
    }
}

