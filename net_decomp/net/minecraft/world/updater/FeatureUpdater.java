/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFixer
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.updater;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkUpdateState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.updater.ChunkUpdater;
import org.jspecify.annotations.Nullable;

public class FeatureUpdater
implements ChunkUpdater {
    public static final int TARGET_DATA_VERSION = 1493;
    private static final Map<String, String> OLD_TO_NEW = Util.make(Maps.newHashMap(), map -> {
        map.put("Village", "Village");
        map.put("Mineshaft", "Mineshaft");
        map.put("Mansion", "Mansion");
        map.put("Igloo", "Temple");
        map.put("Desert_Pyramid", "Temple");
        map.put("Jungle_Pyramid", "Temple");
        map.put("Swamp_Hut", "Temple");
        map.put("Stronghold", "Stronghold");
        map.put("Monument", "Monument");
        map.put("Fortress", "Fortress");
        map.put("EndCity", "EndCity");
    });
    private static final Map<String, String> ANCIENT_TO_OLD = Util.make(Maps.newHashMap(), map -> {
        map.put("Iglu", "Igloo");
        map.put("TeDP", "Desert_Pyramid");
        map.put("TeJP", "Jungle_Pyramid");
        map.put("TeSH", "Swamp_Hut");
    });
    private static final Set<String> NEW_STRUCTURE_NAMES = Set.of("pillager_outpost", "mineshaft", "mansion", "jungle_pyramid", "desert_pyramid", "igloo", "ruined_portal", "shipwreck", "swamp_hut", "stronghold", "monument", "ocean_ruin", "fortress", "endcity", "buried_treasure", "village", "nether_fossil", "bastion_remnant");
    private final boolean needsUpdate;
    private final Map<String, Long2ObjectMap<NbtCompound>> featureIdToChunkNbt = Maps.newHashMap();
    private final Map<String, ChunkUpdateState> updateStates = Maps.newHashMap();
    private final @Nullable PersistentStateManager persistentStateManager;
    private final List<String> oldNames;
    private final List<String> newNames;
    private final DataFixer dataFixer;
    private boolean initialized;

    public FeatureUpdater(@Nullable PersistentStateManager persistentStateManager, List<String> oldNames, List<String> newNames, DataFixer dataFixer) {
        this.persistentStateManager = persistentStateManager;
        this.oldNames = oldNames;
        this.newNames = newNames;
        this.dataFixer = dataFixer;
        boolean bl = false;
        for (String string : this.newNames) {
            bl |= this.featureIdToChunkNbt.get(string) != null;
        }
        this.needsUpdate = bl;
    }

    @Override
    public void markChunkDone(ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        for (String string : this.oldNames) {
            ChunkUpdateState chunkUpdateState = this.updateStates.get(string);
            if (chunkUpdateState == null || !chunkUpdateState.isRemaining(l)) continue;
            chunkUpdateState.markResolved(l);
        }
    }

    @Override
    public int targetDataVersion() {
        return 1493;
    }

    @Override
    public NbtCompound applyFix(NbtCompound nbtCompound) {
        int i;
        if (!this.initialized && this.persistentStateManager != null) {
            this.init(this.persistentStateManager);
        }
        if ((i = NbtHelper.getDataVersion(nbtCompound)) < 1493 && (nbtCompound = DataFixTypes.CHUNK.update(this.dataFixer, nbtCompound, i, 1493)).getCompound("Level").flatMap(levelTag -> levelTag.getBoolean("hasLegacyStructureData")).orElse(false).booleanValue()) {
            nbtCompound = this.getUpdatedReferences(nbtCompound);
        }
        return nbtCompound;
    }

    private NbtCompound getUpdatedReferences(NbtCompound nbt) {
        NbtCompound nbtCompound = nbt.getCompoundOrEmpty("Level");
        ChunkPos chunkPos = new ChunkPos(nbtCompound.getInt("xPos", 0), nbtCompound.getInt("zPos", 0));
        if (this.needsUpdate(chunkPos.x, chunkPos.z)) {
            nbt = this.getUpdatedStarts(nbt, chunkPos);
        }
        NbtCompound nbtCompound2 = nbtCompound.getCompoundOrEmpty("Structures");
        NbtCompound nbtCompound3 = nbtCompound2.getCompoundOrEmpty("References");
        for (String string : this.newNames) {
            boolean bl = NEW_STRUCTURE_NAMES.contains(string.toLowerCase(Locale.ROOT));
            if (nbtCompound3.getLongArray(string).isPresent() || !bl) continue;
            int i = 8;
            LongArrayList longList = new LongArrayList();
            for (int j = chunkPos.x - 8; j <= chunkPos.x + 8; ++j) {
                for (int k = chunkPos.z - 8; k <= chunkPos.z + 8; ++k) {
                    if (!this.needsUpdate(j, k, string)) continue;
                    longList.add(ChunkPos.toLong(j, k));
                }
            }
            nbtCompound3.putLongArray(string, longList.toLongArray());
        }
        nbtCompound2.put("References", nbtCompound3);
        nbtCompound.put("Structures", nbtCompound2);
        nbt.put("Level", nbtCompound);
        return nbt;
    }

    private boolean needsUpdate(int chunkX, int chunkZ, String id) {
        if (!this.needsUpdate) {
            return false;
        }
        return this.featureIdToChunkNbt.get(id) != null && this.updateStates.get(OLD_TO_NEW.get(id)).contains(ChunkPos.toLong(chunkX, chunkZ));
    }

    private boolean needsUpdate(int chunkX, int chunkZ) {
        if (!this.needsUpdate) {
            return false;
        }
        for (String string : this.newNames) {
            if (this.featureIdToChunkNbt.get(string) == null || !this.updateStates.get(OLD_TO_NEW.get(string)).isRemaining(ChunkPos.toLong(chunkX, chunkZ))) continue;
            return true;
        }
        return false;
    }

    private NbtCompound getUpdatedStarts(NbtCompound nbt, ChunkPos pos) {
        NbtCompound nbtCompound = nbt.getCompoundOrEmpty("Level");
        NbtCompound nbtCompound2 = nbtCompound.getCompoundOrEmpty("Structures");
        NbtCompound nbtCompound3 = nbtCompound2.getCompoundOrEmpty("Starts");
        for (String string : this.newNames) {
            NbtCompound nbtCompound4;
            Long2ObjectMap<NbtCompound> long2ObjectMap = this.featureIdToChunkNbt.get(string);
            if (long2ObjectMap == null) continue;
            long l = pos.toLong();
            if (!this.updateStates.get(OLD_TO_NEW.get(string)).isRemaining(l) || (nbtCompound4 = (NbtCompound)long2ObjectMap.get(l)) == null) continue;
            nbtCompound3.put(string, nbtCompound4);
        }
        nbtCompound2.put("Starts", nbtCompound3);
        nbtCompound.put("Structures", nbtCompound2);
        nbt.put("Level", nbtCompound);
        return nbt;
    }

    private synchronized void init(PersistentStateManager persistentStateManager) {
        if (this.initialized) {
            return;
        }
        for (String string : this.oldNames) {
            NbtCompound nbtCompound = new NbtCompound();
            try {
                nbtCompound = persistentStateManager.readNbt(string, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES, 1493).getCompoundOrEmpty("data").getCompoundOrEmpty("Features");
                if (nbtCompound.isEmpty()) {
                    continue;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            nbtCompound.forEach((key, nbt) -> {
                if (!(nbt instanceof NbtCompound)) {
                    return;
                }
                NbtCompound nbtCompound = (NbtCompound)nbt;
                long l = ChunkPos.toLong(nbtCompound.getInt("ChunkX", 0), nbtCompound.getInt("ChunkZ", 0));
                NbtList nbtList = nbtCompound.getListOrEmpty("Children");
                if (!nbtList.isEmpty()) {
                    Optional<String> optional = nbtList.getCompound(0).flatMap(child -> child.getString("id"));
                    optional.map(ANCIENT_TO_OLD::get).ifPresent(id -> nbtCompound.putString("id", (String)id));
                }
                nbtCompound.getString("id").ifPresent(id -> this.featureIdToChunkNbt.computeIfAbsent((String)id, featureId -> new Long2ObjectOpenHashMap()).put(l, (Object)nbtCompound));
            });
            String string2 = string + "_index";
            ChunkUpdateState chunkUpdateState = persistentStateManager.getOrCreate(ChunkUpdateState.createStateType(string2));
            if (chunkUpdateState.getAll().isEmpty()) {
                ChunkUpdateState chunkUpdateState2 = new ChunkUpdateState();
                this.updateStates.put(string, chunkUpdateState2);
                nbtCompound.forEach((key, nbt) -> {
                    if (nbt instanceof NbtCompound) {
                        NbtCompound nbtCompound = (NbtCompound)nbt;
                        chunkUpdateState2.add(ChunkPos.toLong(nbtCompound.getInt("ChunkX", 0), nbtCompound.getInt("ChunkZ", 0)));
                    }
                });
                continue;
            }
            this.updateStates.put(string, chunkUpdateState);
        }
        this.initialized = true;
    }

    public static Supplier<ChunkUpdater> create(RegistryKey<World> world, Supplier<@Nullable PersistentStateManager> persistentStateManagerSupplier, DataFixer dataFixer) {
        if (world == World.OVERWORLD) {
            return () -> new FeatureUpdater((PersistentStateManager)persistentStateManagerSupplier.get(), (List<String>)ImmutableList.of((Object)"Monument", (Object)"Stronghold", (Object)"Village", (Object)"Mineshaft", (Object)"Temple", (Object)"Mansion"), (List<String>)ImmutableList.of((Object)"Village", (Object)"Mineshaft", (Object)"Mansion", (Object)"Igloo", (Object)"Desert_Pyramid", (Object)"Jungle_Pyramid", (Object)"Swamp_Hut", (Object)"Stronghold", (Object)"Monument"), dataFixer);
        }
        if (world == World.NETHER) {
            ImmutableList list = ImmutableList.of((Object)"Fortress");
            return () -> FeatureUpdater.method_76204(persistentStateManagerSupplier, (List)list, dataFixer);
        }
        if (world == World.END) {
            ImmutableList list = ImmutableList.of((Object)"EndCity");
            return () -> FeatureUpdater.method_76203(persistentStateManagerSupplier, (List)list, dataFixer);
        }
        return ChunkUpdater.PASSTHROUGH_FACTORY;
    }

    private static /* synthetic */ ChunkUpdater method_76203(Supplier supplier, List list, DataFixer dataFixer) {
        return new FeatureUpdater((PersistentStateManager)supplier.get(), list, list, dataFixer);
    }

    private static /* synthetic */ ChunkUpdater method_76204(Supplier supplier, List list, DataFixer dataFixer) {
        return new FeatureUpdater((PersistentStateManager)supplier.get(), list, list, dataFixer);
    }
}

