/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public final class DataFixTypes
extends Enum<DataFixTypes> {
    public static final /* enum */ DataFixTypes LEVEL = new DataFixTypes(TypeReferences.LEVEL);
    public static final /* enum */ DataFixTypes LEVEL_SUMMARY = new DataFixTypes(TypeReferences.LIGHTWEIGHT_LEVEL);
    public static final /* enum */ DataFixTypes PLAYER = new DataFixTypes(TypeReferences.PLAYER);
    public static final /* enum */ DataFixTypes CHUNK = new DataFixTypes(TypeReferences.CHUNK);
    public static final /* enum */ DataFixTypes HOTBAR = new DataFixTypes(TypeReferences.HOTBAR);
    public static final /* enum */ DataFixTypes OPTIONS = new DataFixTypes(TypeReferences.OPTIONS);
    public static final /* enum */ DataFixTypes STRUCTURE = new DataFixTypes(TypeReferences.STRUCTURE);
    public static final /* enum */ DataFixTypes STATS = new DataFixTypes(TypeReferences.STATS);
    public static final /* enum */ DataFixTypes SAVED_DATA_COMMAND_STORAGE = new DataFixTypes(TypeReferences.SAVED_DATA_COMMAND_STORAGE);
    public static final /* enum */ DataFixTypes SAVED_DATA_FORCED_CHUNKS = new DataFixTypes(TypeReferences.TICKETS_SAVED_DATA);
    public static final /* enum */ DataFixTypes SAVED_DATA_MAP_DATA = new DataFixTypes(TypeReferences.SAVED_DATA_MAP_DATA);
    public static final /* enum */ DataFixTypes SAVED_DATA_MAP_INDEX = new DataFixTypes(TypeReferences.SAVED_DATA_IDCOUNTS);
    public static final /* enum */ DataFixTypes SAVED_DATA_RAIDS = new DataFixTypes(TypeReferences.SAVED_DATA_RAIDS);
    public static final /* enum */ DataFixTypes SAVED_DATA_RANDOM_SEQUENCES = new DataFixTypes(TypeReferences.SAVED_DATA_RANDOM_SEQUENCES);
    public static final /* enum */ DataFixTypes SAVED_DATA_SCOREBOARD = new DataFixTypes(TypeReferences.SAVED_DATA_SCOREBOARD);
    public static final /* enum */ DataFixTypes SAVED_DATA_STOPWATCHES = new DataFixTypes(TypeReferences.STOPWATCHES_SAVED_DATA);
    public static final /* enum */ DataFixTypes SAVED_DATA_STRUCTURE_FEATURE_INDICES = new DataFixTypes(TypeReferences.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
    public static final /* enum */ DataFixTypes SAVED_DATA_WORLD_BORDER = new DataFixTypes(TypeReferences.WORLD_BORDER_SAVED_DATA);
    public static final /* enum */ DataFixTypes ADVANCEMENTS = new DataFixTypes(TypeReferences.ADVANCEMENTS);
    public static final /* enum */ DataFixTypes POI_CHUNK = new DataFixTypes(TypeReferences.POI_CHUNK);
    public static final /* enum */ DataFixTypes WORLD_GEN_SETTINGS = new DataFixTypes(TypeReferences.WORLD_GEN_SETTINGS);
    public static final /* enum */ DataFixTypes ENTITY_CHUNK = new DataFixTypes(TypeReferences.ENTITY_CHUNK);
    public static final /* enum */ DataFixTypes DEBUG_PROFILE = new DataFixTypes(TypeReferences.DEBUG_PROFILE);
    public static final Set<DSL.TypeReference> REQUIRED_TYPES;
    private final DSL.TypeReference typeReference;
    private static final /* synthetic */ DataFixTypes[] field_19223;

    public static DataFixTypes[] values() {
        return (DataFixTypes[])field_19223.clone();
    }

    public static DataFixTypes valueOf(String string) {
        return Enum.valueOf(DataFixTypes.class, string);
    }

    private DataFixTypes(DSL.TypeReference typeReference) {
        this.typeReference = typeReference;
    }

    static int getSaveVersionId() {
        return SharedConstants.getGameVersion().dataVersion().id();
    }

    public <A> Codec<A> createDataFixingCodec(final Codec<A> baseCodec, final DataFixer dataFixer, final int currentDataVersion) {
        return new Codec<A>(){

            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return baseCodec.encode(input, ops, prefix).flatMap(encoded -> ops.mergeToMap(encoded, ops.createString("DataVersion"), ops.createInt(DataFixTypes.getSaveVersionId())));
            }

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                int i = ops.get(input, "DataVersion").flatMap(arg_0 -> ops.getNumberValue(arg_0)).map(Number::intValue).result().orElse(currentDataVersion);
                Dynamic dynamic = new Dynamic(ops, ops.remove(input, "DataVersion"));
                Dynamic dynamic2 = DataFixTypes.this.update(dataFixer, dynamic, i);
                return baseCodec.decode(dynamic2);
            }
        };
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int oldVersion, int newVersion) {
        return dataFixer.update(this.typeReference, dynamic, oldVersion, newVersion);
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int oldVersion) {
        return this.update(dataFixer, dynamic, oldVersion, DataFixTypes.getSaveVersionId());
    }

    public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion, int newVersion) {
        return (NbtCompound)this.update(dataFixer, new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)nbt), oldVersion, newVersion).getValue();
    }

    public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion) {
        return this.update(dataFixer, nbt, oldVersion, DataFixTypes.getSaveVersionId());
    }

    private static /* synthetic */ DataFixTypes[] method_36589() {
        return new DataFixTypes[]{LEVEL, LEVEL_SUMMARY, PLAYER, CHUNK, HOTBAR, OPTIONS, STRUCTURE, STATS, SAVED_DATA_COMMAND_STORAGE, SAVED_DATA_FORCED_CHUNKS, SAVED_DATA_MAP_DATA, SAVED_DATA_MAP_INDEX, SAVED_DATA_RAIDS, SAVED_DATA_RANDOM_SEQUENCES, SAVED_DATA_SCOREBOARD, SAVED_DATA_STOPWATCHES, SAVED_DATA_STRUCTURE_FEATURE_INDICES, SAVED_DATA_WORLD_BORDER, ADVANCEMENTS, POI_CHUNK, WORLD_GEN_SETTINGS, ENTITY_CHUNK, DEBUG_PROFILE};
    }

    static {
        field_19223 = DataFixTypes.method_36589();
        REQUIRED_TYPES = Set.of(DataFixTypes.LEVEL_SUMMARY.typeReference);
    }
}

