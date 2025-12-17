/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.debug.data.PoiDebugData;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PointOfInterestSet {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Short2ObjectMap<PointOfInterest> pointsOfInterestByPos = new Short2ObjectOpenHashMap();
    private final Map<RegistryEntry<PointOfInterestType>, Set<PointOfInterest>> pointsOfInterestByType = Maps.newHashMap();
    private final Runnable updateListener;
    private boolean valid;

    public PointOfInterestSet(Runnable updateListener) {
        this(updateListener, true, (List<PointOfInterest>)ImmutableList.of());
    }

    PointOfInterestSet(Runnable updateListener, boolean valid, List<PointOfInterest> pois) {
        this.updateListener = updateListener;
        this.valid = valid;
        pois.forEach(this::add);
    }

    public Serialized toSerialized() {
        return new Serialized(this.valid, this.pointsOfInterestByPos.values().stream().map(PointOfInterest::toSerialized).toList());
    }

    public Stream<PointOfInterest> get(Predicate<RegistryEntry<PointOfInterestType>> predicate, PointOfInterestStorage.OccupationStatus occupationStatus) {
        return this.pointsOfInterestByType.entrySet().stream().filter(entry -> predicate.test((RegistryEntry)entry.getKey())).flatMap(entry -> ((Set)entry.getValue()).stream()).filter(occupationStatus.getPredicate());
    }

    public @Nullable PointOfInterest add(BlockPos pos, RegistryEntry<PointOfInterestType> type) {
        PointOfInterest pointOfInterest = new PointOfInterest(pos, type, this.updateListener);
        if (this.add(pointOfInterest)) {
            LOGGER.debug("Added POI of type {} @ {}", (Object)type.getIdAsString(), (Object)pos);
            this.updateListener.run();
            return pointOfInterest;
        }
        return null;
    }

    private boolean add(PointOfInterest poi) {
        BlockPos blockPos = poi.getPos();
        RegistryEntry<PointOfInterestType> registryEntry = poi.getType();
        short s = ChunkSectionPos.packLocal(blockPos);
        PointOfInterest pointOfInterest = (PointOfInterest)this.pointsOfInterestByPos.get(s);
        if (pointOfInterest != null) {
            if (registryEntry.equals(pointOfInterest.getType())) {
                return false;
            }
            Util.logErrorOrPause("POI data mismatch: already registered at " + String.valueOf(blockPos));
        }
        this.pointsOfInterestByPos.put(s, (Object)poi);
        this.pointsOfInterestByType.computeIfAbsent(registryEntry, type -> Sets.newHashSet()).add(poi);
        return true;
    }

    public void remove(BlockPos pos) {
        PointOfInterest pointOfInterest = (PointOfInterest)this.pointsOfInterestByPos.remove(ChunkSectionPos.packLocal(pos));
        if (pointOfInterest == null) {
            LOGGER.error("POI data mismatch: never registered at {}", (Object)pos);
            return;
        }
        this.pointsOfInterestByType.get(pointOfInterest.getType()).remove(pointOfInterest);
        LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(pointOfInterest::getType), LogUtils.defer(pointOfInterest::getPos));
        this.updateListener.run();
    }

    @Deprecated
    @Debug
    public int getFreeTickets(BlockPos pos) {
        return this.get(pos).map(PointOfInterest::getFreeTickets).orElse(0);
    }

    public boolean releaseTicket(BlockPos pos) {
        PointOfInterest pointOfInterest = (PointOfInterest)this.pointsOfInterestByPos.get(ChunkSectionPos.packLocal(pos));
        if (pointOfInterest == null) {
            throw Util.getFatalOrPause(new IllegalStateException("POI never registered at " + String.valueOf(pos)));
        }
        boolean bl = pointOfInterest.releaseTicket();
        this.updateListener.run();
        return bl;
    }

    public boolean test(BlockPos pos, Predicate<RegistryEntry<PointOfInterestType>> predicate) {
        return this.getType(pos).filter(predicate).isPresent();
    }

    public Optional<RegistryEntry<PointOfInterestType>> getType(BlockPos pos) {
        return this.get(pos).map(PointOfInterest::getType);
    }

    private Optional<PointOfInterest> get(BlockPos pos) {
        return Optional.ofNullable((PointOfInterest)this.pointsOfInterestByPos.get(ChunkSectionPos.packLocal(pos)));
    }

    public Optional<PoiDebugData> getDebugData(BlockPos pos) {
        return this.get(pos).map(PoiDebugData::new);
    }

    public void updatePointsOfInterest(Consumer<BiConsumer<BlockPos, RegistryEntry<PointOfInterestType>>> updater) {
        if (!this.valid) {
            Short2ObjectOpenHashMap short2ObjectMap = new Short2ObjectOpenHashMap(this.pointsOfInterestByPos);
            this.clear();
            updater.accept((arg_0, arg_1) -> this.method_20352((Short2ObjectMap)short2ObjectMap, arg_0, arg_1));
            this.valid = true;
            this.updateListener.run();
        }
    }

    private void clear() {
        this.pointsOfInterestByPos.clear();
        this.pointsOfInterestByType.clear();
    }

    boolean isValid() {
        return this.valid;
    }

    private /* synthetic */ void method_20352(Short2ObjectMap short2ObjectMap, BlockPos pos, RegistryEntry poiEntry) {
        short s2 = ChunkSectionPos.packLocal(pos);
        PointOfInterest pointOfInterest = (PointOfInterest)short2ObjectMap.computeIfAbsent(s2, s -> new PointOfInterest(pos, poiEntry, this.updateListener));
        this.add(pointOfInterest);
    }

    public record Serialized(boolean isValid, List<PointOfInterest.Serialized> records) {
        public static final Codec<Serialized> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.lenientOptionalFieldOf("Valid", (Object)false).forGetter(Serialized::isValid), (App)PointOfInterest.Serialized.CODEC.listOf().fieldOf("Records").forGetter(Serialized::records)).apply((Applicative)instance, Serialized::new));

        public PointOfInterestSet toPointOfInterestSet(Runnable updateListener) {
            return new PointOfInterestSet(updateListener, this.isValid, this.records.stream().map(serialized -> serialized.toPointOfInterest(updateListener)).toList());
        }
    }
}

