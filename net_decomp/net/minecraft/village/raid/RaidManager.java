/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.village.raid;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class RaidManager
extends PersistentState {
    private static final String RAIDS = "raids";
    public static final Codec<RaidManager> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RaidWithId.CODEC.listOf().optionalFieldOf(RAIDS, List.of()).forGetter(raidManager -> raidManager.raids.int2ObjectEntrySet().stream().map(RaidWithId::fromMapEntry).toList()), (App)Codec.INT.fieldOf("next_id").forGetter(raidManager -> raidManager.nextAvailableId), (App)Codec.INT.fieldOf("tick").forGetter(raidManager -> raidManager.currentTime)).apply((Applicative)instance, RaidManager::new));
    public static final PersistentStateType<RaidManager> STATE_TYPE = new PersistentStateType<RaidManager>("raids", RaidManager::new, CODEC, DataFixTypes.SAVED_DATA_RAIDS);
    public static final PersistentStateType<RaidManager> END_STATE_TYPE = new PersistentStateType<RaidManager>("raids_end", RaidManager::new, CODEC, DataFixTypes.SAVED_DATA_RAIDS);
    private final Int2ObjectMap<Raid> raids = new Int2ObjectOpenHashMap();
    private int nextAvailableId = 1;
    private int currentTime;

    public static PersistentStateType<RaidManager> getPersistentStateType(RegistryEntry<DimensionType> dimensionType) {
        if (dimensionType.matchesKey(DimensionTypes.THE_END)) {
            return END_STATE_TYPE;
        }
        return STATE_TYPE;
    }

    public RaidManager() {
        this.markDirty();
    }

    private RaidManager(List<RaidWithId> raids, int nextAvailableId, int currentTime) {
        for (RaidWithId raidWithId : raids) {
            this.raids.put(raidWithId.id, (Object)raidWithId.raid);
        }
        this.nextAvailableId = nextAvailableId;
        this.currentTime = currentTime;
    }

    public @Nullable Raid getRaid(int id) {
        return (Raid)this.raids.get(id);
    }

    public OptionalInt getRaidId(Raid raid) {
        for (Int2ObjectMap.Entry entry : this.raids.int2ObjectEntrySet()) {
            if (entry.getValue() != raid) continue;
            return OptionalInt.of(entry.getIntKey());
        }
        return OptionalInt.empty();
    }

    public void tick(ServerWorld world) {
        ++this.currentTime;
        ObjectIterator iterator = this.raids.values().iterator();
        while (iterator.hasNext()) {
            Raid raid = (Raid)iterator.next();
            if (!world.getGameRules().getValue(GameRules.DISABLE_RAIDS).booleanValue()) {
                raid.invalidate();
            }
            if (raid.hasStopped()) {
                iterator.remove();
                this.markDirty();
                continue;
            }
            raid.tick(world);
        }
        if (this.currentTime % 200 == 0) {
            this.markDirty();
        }
    }

    public static boolean isValidRaiderFor(RaiderEntity raider) {
        return raider.isAlive() && raider.canJoinRaid() && raider.getDespawnCounter() <= 2400;
    }

    public @Nullable Raid startRaid(ServerPlayerEntity player, BlockPos pos) {
        BlockPos blockPos2;
        if (player.isSpectator()) {
            return null;
        }
        ServerWorld serverWorld = player.getEntityWorld();
        if (!serverWorld.getGameRules().getValue(GameRules.DISABLE_RAIDS).booleanValue()) {
            return null;
        }
        if (!serverWorld.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.CAN_START_RAID_GAMEPLAY, pos).booleanValue()) {
            return null;
        }
        List<PointOfInterest> list = serverWorld.getPointOfInterestStorage().getInCircle(poiType -> poiType.isIn(PointOfInterestTypeTags.VILLAGE), pos, 64, PointOfInterestStorage.OccupationStatus.IS_OCCUPIED).toList();
        int i = 0;
        Vec3d vec3d = Vec3d.ZERO;
        for (PointOfInterest pointOfInterest : list) {
            BlockPos blockPos = pointOfInterest.getPos();
            vec3d = vec3d.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            ++i;
        }
        if (i > 0) {
            vec3d = vec3d.multiply(1.0 / (double)i);
            blockPos2 = BlockPos.ofFloored(vec3d);
        } else {
            blockPos2 = pos;
        }
        Raid raid = this.getOrCreateRaid(serverWorld, blockPos2);
        if (!raid.hasStarted() && !this.raids.containsValue((Object)raid)) {
            this.raids.put(this.nextId(), (Object)raid);
        }
        if (!raid.hasStarted() || raid.getBadOmenLevel() < raid.getMaxAcceptableBadOmenLevel()) {
            raid.start(player);
        }
        this.markDirty();
        return raid;
    }

    private Raid getOrCreateRaid(ServerWorld world, BlockPos pos) {
        Raid raid = world.getRaidAt(pos);
        return raid != null ? raid : new Raid(pos, world.getDifficulty());
    }

    public static RaidManager fromNbt(NbtCompound nbt) {
        return CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)nbt).resultOrPartial().orElseGet(RaidManager::new);
    }

    private int nextId() {
        return ++this.nextAvailableId;
    }

    public @Nullable Raid getRaidAt(BlockPos pos, int searchDistance) {
        Raid raid = null;
        double d = searchDistance;
        for (Raid raid2 : this.raids.values()) {
            double e = raid2.getCenter().getSquaredDistance(pos);
            if (!raid2.isActive() || !(e < d)) continue;
            raid = raid2;
            d = e;
        }
        return raid;
    }

    @Debug
    public List<BlockPos> getRaidCenters(ChunkPos chunkPos) {
        return this.raids.values().stream().map(Raid::getCenter).filter(chunkPos::contains).toList();
    }

    static final class RaidWithId
    extends Record {
        final int id;
        final Raid raid;
        public static final Codec<RaidWithId> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("id").forGetter(RaidWithId::id), (App)Raid.CODEC.forGetter(RaidWithId::raid)).apply((Applicative)instance, RaidWithId::new));

        private RaidWithId(int id, Raid raid) {
            this.id = id;
            this.raid = raid;
        }

        public static RaidWithId fromMapEntry(Int2ObjectMap.Entry<Raid> entry) {
            return new RaidWithId(entry.getIntKey(), (Raid)entry.getValue());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RaidWithId.class, "id;raid", "id", "raid"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RaidWithId.class, "id;raid", "id", "raid"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RaidWithId.class, "id;raid", "id", "raid"}, this, object);
        }

        public int id() {
            return this.id;
        }

        public Raid raid() {
            return this.raid;
        }
    }
}

