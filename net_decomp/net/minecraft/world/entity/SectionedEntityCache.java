/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectFunction
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongAVLTreeSet
 *  it.unimi.dsi.fastutil.longs.LongBidirectionalIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.longs.LongSortedSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingStatus;
import org.jspecify.annotations.Nullable;

public class SectionedEntityCache<T extends EntityLike> {
    public static final int field_52653 = 2;
    public static final int field_52654 = 4;
    private final Class<T> entityClass;
    private final Long2ObjectFunction<EntityTrackingStatus> posToStatus;
    private final Long2ObjectMap<EntityTrackingSection<T>> trackingSections = new Long2ObjectOpenHashMap();
    private final LongSortedSet trackedPositions = new LongAVLTreeSet();

    public SectionedEntityCache(Class<T> entityClass, Long2ObjectFunction<EntityTrackingStatus> chunkStatusDiscriminator) {
        this.entityClass = entityClass;
        this.posToStatus = chunkStatusDiscriminator;
    }

    public void forEachInBox(Box box, LazyIterationConsumer<EntityTrackingSection<T>> consumer) {
        int i = ChunkSectionPos.getSectionCoord(box.minX - 2.0);
        int j = ChunkSectionPos.getSectionCoord(box.minY - 4.0);
        int k = ChunkSectionPos.getSectionCoord(box.minZ - 2.0);
        int l = ChunkSectionPos.getSectionCoord(box.maxX + 2.0);
        int m = ChunkSectionPos.getSectionCoord(box.maxY + 0.0);
        int n = ChunkSectionPos.getSectionCoord(box.maxZ + 2.0);
        for (int o = i; o <= l; ++o) {
            long p = ChunkSectionPos.asLong(o, 0, 0);
            long q = ChunkSectionPos.asLong(o, -1, -1);
            LongBidirectionalIterator longIterator = this.trackedPositions.subSet(p, q + 1L).iterator();
            while (longIterator.hasNext()) {
                EntityTrackingSection entityTrackingSection;
                long r = longIterator.nextLong();
                int s = ChunkSectionPos.unpackY(r);
                int t = ChunkSectionPos.unpackZ(r);
                if (s < j || s > m || t < k || t > n || (entityTrackingSection = (EntityTrackingSection)this.trackingSections.get(r)) == null || entityTrackingSection.isEmpty() || !entityTrackingSection.getStatus().shouldTrack() || !consumer.accept(entityTrackingSection).shouldAbort()) continue;
                return;
            }
        }
    }

    public LongStream getSections(long chunkPos) {
        int j;
        int i = ChunkPos.getPackedX(chunkPos);
        LongSortedSet longSortedSet = this.getSections(i, j = ChunkPos.getPackedZ(chunkPos));
        if (longSortedSet.isEmpty()) {
            return LongStream.empty();
        }
        LongBidirectionalIterator ofLong = longSortedSet.iterator();
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize((PrimitiveIterator.OfLong)ofLong, 1301), false);
    }

    private LongSortedSet getSections(int chunkX, int chunkZ) {
        long l = ChunkSectionPos.asLong(chunkX, 0, chunkZ);
        long m = ChunkSectionPos.asLong(chunkX, -1, chunkZ);
        return this.trackedPositions.subSet(l, m + 1L);
    }

    public Stream<EntityTrackingSection<T>> getTrackingSections(long chunkPos) {
        return this.getSections(chunkPos).mapToObj(arg_0 -> this.trackingSections.get(arg_0)).filter(Objects::nonNull);
    }

    private static long chunkPosFromSectionPos(long sectionPos) {
        return ChunkPos.toLong(ChunkSectionPos.unpackX(sectionPos), ChunkSectionPos.unpackZ(sectionPos));
    }

    public EntityTrackingSection<T> getTrackingSection(long sectionPos) {
        return (EntityTrackingSection)this.trackingSections.computeIfAbsent(sectionPos, this::addSection);
    }

    public @Nullable EntityTrackingSection<T> findTrackingSection(long sectionPos) {
        return (EntityTrackingSection)this.trackingSections.get(sectionPos);
    }

    private EntityTrackingSection<T> addSection(long sectionPos) {
        long l = SectionedEntityCache.chunkPosFromSectionPos(sectionPos);
        EntityTrackingStatus entityTrackingStatus = (EntityTrackingStatus)((Object)this.posToStatus.get(l));
        this.trackedPositions.add(sectionPos);
        return new EntityTrackingSection<T>(this.entityClass, entityTrackingStatus);
    }

    public LongSet getChunkPositions() {
        LongOpenHashSet longSet = new LongOpenHashSet();
        this.trackingSections.keySet().forEach(arg_0 -> SectionedEntityCache.method_31780((LongSet)longSet, arg_0));
        return longSet;
    }

    public void forEachIntersects(Box box, LazyIterationConsumer<T> consumer) {
        this.forEachInBox(box, section -> section.forEach(box, consumer));
    }

    public <U extends T> void forEachIntersects(TypeFilter<T, U> filter, Box box, LazyIterationConsumer<U> consumer) {
        this.forEachInBox(box, section -> section.forEach(filter, box, consumer));
    }

    public void removeSection(long sectionPos) {
        this.trackingSections.remove(sectionPos);
        this.trackedPositions.remove(sectionPos);
    }

    @Debug
    public int sectionCount() {
        return this.trackedPositions.size();
    }

    private static /* synthetic */ void method_31780(LongSet trackingSection, long sectionPos) {
        trackingSection.add(SectionedEntityCache.chunkPosFromSectionPos(sectionPos));
    }
}

