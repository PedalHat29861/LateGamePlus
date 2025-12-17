/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicLike
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.OptionalDynamic
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.math.NumberUtils
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class StructureSeparationDataFix
extends DataFix {
    private static final String VILLAGE_STRUCTURE_ID = "minecraft:village";
    private static final String DESERT_PYRAMID_STRUCTURE_ID = "minecraft:desert_pyramid";
    private static final String IGLOO_STRUCTURE_ID = "minecraft:igloo";
    private static final String JUNGLE_PYRAMID_STRUCTURE_ID = "minecraft:jungle_pyramid";
    private static final String SWAMP_HUT_STRUCTURE_ID = "minecraft:swamp_hut";
    private static final String PILLAGER_OUTPOST_STRUCTURE_ID = "minecraft:pillager_outpost";
    private static final String END_CITY_STRUCTURE_ID = "minecraft:endcity";
    private static final String MANSION_STRUCTURE_ID = "minecraft:mansion";
    private static final String MONUMENT_STRUCTURE_ID = "minecraft:monument";
    private static final ImmutableMap<String, Information> STRUCTURE_SPACING = ImmutableMap.builder().put((Object)"minecraft:village", (Object)new Information(32, 8, 10387312)).put((Object)"minecraft:desert_pyramid", (Object)new Information(32, 8, 14357617)).put((Object)"minecraft:igloo", (Object)new Information(32, 8, 14357618)).put((Object)"minecraft:jungle_pyramid", (Object)new Information(32, 8, 14357619)).put((Object)"minecraft:swamp_hut", (Object)new Information(32, 8, 14357620)).put((Object)"minecraft:pillager_outpost", (Object)new Information(32, 8, 165745296)).put((Object)"minecraft:monument", (Object)new Information(32, 5, 10387313)).put((Object)"minecraft:endcity", (Object)new Information(20, 11, 10387313)).put((Object)"minecraft:mansion", (Object)new Information(80, 20, 10387319)).build();

    public StructureSeparationDataFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("WorldGenSettings building", this.getInputSchema().getType(TypeReferences.WORLD_GEN_SETTINGS), worldGenSettingsTyped -> worldGenSettingsTyped.update(DSL.remainderFinder(), StructureSeparationDataFix::updateWorldGenSettings));
    }

    private static <T> Dynamic<T> createGeneratorSettings(long seed, DynamicLike<T> worldGenSettingsDynamic, Dynamic<T> settingsDynamic, Dynamic<T> biomeSourceDynamic) {
        return worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:noise"), (Object)worldGenSettingsDynamic.createString("biome_source"), biomeSourceDynamic, (Object)worldGenSettingsDynamic.createString("seed"), (Object)worldGenSettingsDynamic.createLong(seed), (Object)worldGenSettingsDynamic.createString("settings"), settingsDynamic));
    }

    private static <T> Dynamic<T> createBiomeSource(Dynamic<T> worldGenSettingsDynamic, long seed, boolean legacyBiomeInitLayer, boolean largeBiomes) {
        ImmutableMap.Builder builder = ImmutableMap.builder().put((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:vanilla_layered")).put((Object)worldGenSettingsDynamic.createString("seed"), (Object)worldGenSettingsDynamic.createLong(seed)).put((Object)worldGenSettingsDynamic.createString("large_biomes"), (Object)worldGenSettingsDynamic.createBoolean(largeBiomes));
        if (legacyBiomeInitLayer) {
            builder.put((Object)worldGenSettingsDynamic.createString("legacy_biome_init_layer"), (Object)worldGenSettingsDynamic.createBoolean(legacyBiomeInitLayer));
        }
        return worldGenSettingsDynamic.createMap((Map)builder.build());
    }

    private static <T> Dynamic<T> updateWorldGenSettings(Dynamic<T> worldGenSettingsDynamic) {
        Dynamic<T> dynamic;
        DynamicOps dynamicOps = worldGenSettingsDynamic.getOps();
        long l = worldGenSettingsDynamic.get("RandomSeed").asLong(0L);
        Optional optional = worldGenSettingsDynamic.get("generatorName").asString().map(generatorName -> generatorName.toLowerCase(Locale.ROOT)).result();
        Optional optional2 = worldGenSettingsDynamic.get("legacy_custom_options").asString().result().map(Optional::of).orElseGet(() -> {
            if (optional.equals(Optional.of("customized"))) {
                return worldGenSettingsDynamic.get("generatorOptions").asString().result();
            }
            return Optional.empty();
        });
        boolean bl = false;
        if (optional.equals(Optional.of("customized"))) {
            dynamic = StructureSeparationDataFix.createDefaultOverworldGeneratorSettings(worldGenSettingsDynamic, l);
        } else if (optional.isEmpty()) {
            dynamic = StructureSeparationDataFix.createDefaultOverworldGeneratorSettings(worldGenSettingsDynamic, l);
        } else {
            switch ((String)optional.get()) {
                case "flat": {
                    OptionalDynamic optionalDynamic = worldGenSettingsDynamic.get("generatorOptions");
                    Map<Dynamic<T>, Dynamic<T>> map = StructureSeparationDataFix.createFlatWorldStructureSettings(dynamicOps, optionalDynamic);
                    dynamic = worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:flat"), (Object)worldGenSettingsDynamic.createString("settings"), (Object)worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("structures"), (Object)worldGenSettingsDynamic.createMap(map), (Object)worldGenSettingsDynamic.createString("layers"), (Object)optionalDynamic.get("layers").result().orElseGet(() -> worldGenSettingsDynamic.createList(Stream.of(worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("height"), (Object)worldGenSettingsDynamic.createInt(1), (Object)worldGenSettingsDynamic.createString("block"), (Object)worldGenSettingsDynamic.createString("minecraft:bedrock"))), worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("height"), (Object)worldGenSettingsDynamic.createInt(2), (Object)worldGenSettingsDynamic.createString("block"), (Object)worldGenSettingsDynamic.createString("minecraft:dirt"))), worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("height"), (Object)worldGenSettingsDynamic.createInt(1), (Object)worldGenSettingsDynamic.createString("block"), (Object)worldGenSettingsDynamic.createString("minecraft:grass_block")))))), (Object)worldGenSettingsDynamic.createString("biome"), (Object)worldGenSettingsDynamic.createString(optionalDynamic.get("biome").asString("minecraft:plains"))))));
                    break;
                }
                case "debug_all_block_states": {
                    dynamic = worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:debug")));
                    break;
                }
                case "buffet": {
                    Dynamic dynamic4;
                    Dynamic dynamic2;
                    OptionalDynamic optionalDynamic2 = worldGenSettingsDynamic.get("generatorOptions");
                    OptionalDynamic optionalDynamic3 = optionalDynamic2.get("chunk_generator");
                    Optional optional3 = optionalDynamic3.get("type").asString().result();
                    if (Objects.equals(optional3, Optional.of("minecraft:caves"))) {
                        dynamic2 = worldGenSettingsDynamic.createString("minecraft:caves");
                        bl = true;
                    } else {
                        dynamic2 = Objects.equals(optional3, Optional.of("minecraft:floating_islands")) ? worldGenSettingsDynamic.createString("minecraft:floating_islands") : worldGenSettingsDynamic.createString("minecraft:overworld");
                    }
                    Dynamic dynamic3 = optionalDynamic2.get("biome_source").result().orElseGet(() -> worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:fixed"))));
                    if (dynamic3.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
                        String string = dynamic3.get("options").get("biomes").asStream().findFirst().flatMap(biomeDynamic -> biomeDynamic.asString().result()).orElse("minecraft:ocean");
                        dynamic4 = dynamic3.remove("options").set("biome", worldGenSettingsDynamic.createString(string));
                    } else {
                        dynamic4 = dynamic3;
                    }
                    dynamic = StructureSeparationDataFix.createGeneratorSettings(l, worldGenSettingsDynamic, dynamic2, dynamic4);
                    break;
                }
                default: {
                    boolean bl2 = ((String)optional.get()).equals("default");
                    boolean bl3 = ((String)optional.get()).equals("default_1_1") || bl2 && worldGenSettingsDynamic.get("generatorVersion").asInt(0) == 0;
                    boolean bl4 = ((String)optional.get()).equals("amplified");
                    boolean bl5 = ((String)optional.get()).equals("largebiomes");
                    dynamic = StructureSeparationDataFix.createGeneratorSettings(l, worldGenSettingsDynamic, worldGenSettingsDynamic.createString(bl4 ? "minecraft:amplified" : "minecraft:overworld"), StructureSeparationDataFix.createBiomeSource(worldGenSettingsDynamic, l, bl3, bl5));
                }
            }
        }
        boolean bl6 = worldGenSettingsDynamic.get("MapFeatures").asBoolean(true);
        boolean bl7 = worldGenSettingsDynamic.get("BonusChest").asBoolean(false);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.put(dynamicOps.createString("seed"), dynamicOps.createLong(l));
        builder.put(dynamicOps.createString("generate_features"), dynamicOps.createBoolean(bl6));
        builder.put(dynamicOps.createString("bonus_chest"), dynamicOps.createBoolean(bl7));
        builder.put(dynamicOps.createString("dimensions"), StructureSeparationDataFix.createDimensionSettings(worldGenSettingsDynamic, l, dynamic, bl));
        optional2.ifPresent(legacyCustomOptions -> builder.put(dynamicOps.createString("legacy_custom_options"), dynamicOps.createString(legacyCustomOptions)));
        return new Dynamic(dynamicOps, dynamicOps.createMap((Map)builder.build()));
    }

    protected static <T> Dynamic<T> createDefaultOverworldGeneratorSettings(Dynamic<T> worldGenSettingsDynamic, long seed) {
        return StructureSeparationDataFix.createGeneratorSettings(seed, worldGenSettingsDynamic, worldGenSettingsDynamic.createString("minecraft:overworld"), StructureSeparationDataFix.createBiomeSource(worldGenSettingsDynamic, seed, false, false));
    }

    protected static <T> T createDimensionSettings(Dynamic<T> worldGenSettingsDynamic, long seed, Dynamic<T> generatorSettingsDynamic, boolean caves) {
        DynamicOps dynamicOps = worldGenSettingsDynamic.getOps();
        return (T)dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString("minecraft:overworld"), (Object)dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString("type"), (Object)dynamicOps.createString("minecraft:overworld" + (caves ? "_caves" : "")), (Object)dynamicOps.createString("generator"), (Object)generatorSettingsDynamic.getValue())), (Object)dynamicOps.createString("minecraft:the_nether"), (Object)dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString("type"), (Object)dynamicOps.createString("minecraft:the_nether"), (Object)dynamicOps.createString("generator"), (Object)StructureSeparationDataFix.createGeneratorSettings(seed, worldGenSettingsDynamic, worldGenSettingsDynamic.createString("minecraft:nether"), worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:multi_noise"), (Object)worldGenSettingsDynamic.createString("seed"), (Object)worldGenSettingsDynamic.createLong(seed), (Object)worldGenSettingsDynamic.createString("preset"), (Object)worldGenSettingsDynamic.createString("minecraft:nether")))).getValue())), (Object)dynamicOps.createString("minecraft:the_end"), (Object)dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString("type"), (Object)dynamicOps.createString("minecraft:the_end"), (Object)dynamicOps.createString("generator"), (Object)StructureSeparationDataFix.createGeneratorSettings(seed, worldGenSettingsDynamic, worldGenSettingsDynamic.createString("minecraft:end"), worldGenSettingsDynamic.createMap((Map)ImmutableMap.of((Object)worldGenSettingsDynamic.createString("type"), (Object)worldGenSettingsDynamic.createString("minecraft:the_end"), (Object)worldGenSettingsDynamic.createString("seed"), (Object)worldGenSettingsDynamic.createLong(seed)))).getValue()))));
    }

    private static <T> Map<Dynamic<T>, Dynamic<T>> createFlatWorldStructureSettings(DynamicOps<T> worldGenSettingsDynamicOps, OptionalDynamic<T> generatorOptionsDynamic) {
        MutableInt mutableInt = new MutableInt(32);
        MutableInt mutableInt2 = new MutableInt(3);
        MutableInt mutableInt3 = new MutableInt(128);
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        HashMap map = Maps.newHashMap();
        if (generatorOptionsDynamic.result().isEmpty()) {
            mutableBoolean.setTrue();
            map.put(VILLAGE_STRUCTURE_ID, (Information)STRUCTURE_SPACING.get((Object)VILLAGE_STRUCTURE_ID));
        }
        generatorOptionsDynamic.get("structures").flatMap(Dynamic::getMapValues).ifSuccess(map2 -> map2.forEach((oldStructureName, dynamic) -> dynamic.getMapValues().result().ifPresent(map2 -> map2.forEach((propertyName, spacing) -> {
            String string = oldStructureName.asString("");
            String string2 = propertyName.asString("");
            String string3 = spacing.asString("");
            if ("stronghold".equals(string)) {
                mutableBoolean.setTrue();
                switch (string2) {
                    case "distance": {
                        mutableInt.setValue(StructureSeparationDataFix.parseInt(string3, mutableInt.intValue(), 1));
                        return;
                    }
                    case "spread": {
                        mutableInt2.setValue(StructureSeparationDataFix.parseInt(string3, mutableInt2.intValue(), 1));
                        return;
                    }
                    case "count": {
                        mutableInt3.setValue(StructureSeparationDataFix.parseInt(string3, mutableInt3.intValue(), 1));
                        return;
                    }
                }
                return;
            }
            switch (string2) {
                case "distance": {
                    switch (string) {
                        case "village": {
                            StructureSeparationDataFix.insertStructureSettings(map, VILLAGE_STRUCTURE_ID, string3, 9);
                            return;
                        }
                        case "biome_1": {
                            StructureSeparationDataFix.insertStructureSettings(map, DESERT_PYRAMID_STRUCTURE_ID, string3, 9);
                            StructureSeparationDataFix.insertStructureSettings(map, IGLOO_STRUCTURE_ID, string3, 9);
                            StructureSeparationDataFix.insertStructureSettings(map, JUNGLE_PYRAMID_STRUCTURE_ID, string3, 9);
                            StructureSeparationDataFix.insertStructureSettings(map, SWAMP_HUT_STRUCTURE_ID, string3, 9);
                            StructureSeparationDataFix.insertStructureSettings(map, PILLAGER_OUTPOST_STRUCTURE_ID, string3, 9);
                            return;
                        }
                        case "endcity": {
                            StructureSeparationDataFix.insertStructureSettings(map, END_CITY_STRUCTURE_ID, string3, 1);
                            return;
                        }
                        case "mansion": {
                            StructureSeparationDataFix.insertStructureSettings(map, MANSION_STRUCTURE_ID, string3, 1);
                            return;
                        }
                    }
                    return;
                }
                case "separation": {
                    if ("oceanmonument".equals(string)) {
                        Information information = map.getOrDefault(MONUMENT_STRUCTURE_ID, (Information)STRUCTURE_SPACING.get((Object)MONUMENT_STRUCTURE_ID));
                        int i = StructureSeparationDataFix.parseInt(string3, information.separation, 1);
                        map.put(MONUMENT_STRUCTURE_ID, new Information(i, information.separation, information.salt));
                    }
                    return;
                }
                case "spacing": {
                    if ("oceanmonument".equals(string)) {
                        StructureSeparationDataFix.insertStructureSettings(map, MONUMENT_STRUCTURE_ID, string3, 1);
                    }
                    return;
                }
            }
        }))));
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.put((Object)generatorOptionsDynamic.createString("structures"), (Object)generatorOptionsDynamic.createMap(map.entrySet().stream().collect(Collectors.toMap(entry -> generatorOptionsDynamic.createString((String)entry.getKey()), entry -> ((Information)entry.getValue()).method_28288(worldGenSettingsDynamicOps)))));
        if (mutableBoolean.isTrue()) {
            builder.put((Object)generatorOptionsDynamic.createString("stronghold"), (Object)generatorOptionsDynamic.createMap((Map)ImmutableMap.of((Object)generatorOptionsDynamic.createString("distance"), (Object)generatorOptionsDynamic.createInt(mutableInt.intValue()), (Object)generatorOptionsDynamic.createString("spread"), (Object)generatorOptionsDynamic.createInt(mutableInt2.intValue()), (Object)generatorOptionsDynamic.createString("count"), (Object)generatorOptionsDynamic.createInt(mutableInt3.intValue()))));
        }
        return builder.build();
    }

    private static int parseInt(String string, int defaultValue) {
        return NumberUtils.toInt((String)string, (int)defaultValue);
    }

    private static int parseInt(String string, int defaultValue, int minValue) {
        return Math.max(minValue, StructureSeparationDataFix.parseInt(string, defaultValue));
    }

    private static void insertStructureSettings(Map<String, Information> map, String structureId, String spacingStr, int minSpacing) {
        Information information = map.getOrDefault(structureId, (Information)STRUCTURE_SPACING.get((Object)structureId));
        int i = StructureSeparationDataFix.parseInt(spacingStr, information.spacing, minSpacing);
        map.put(structureId, new Information(i, information.separation, information.salt));
    }

    static final class Information {
        public static final Codec<Information> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("spacing").forGetter(information -> information.spacing), (App)Codec.INT.fieldOf("separation").forGetter(information -> information.separation), (App)Codec.INT.fieldOf("salt").forGetter(information -> information.salt)).apply((Applicative)instance, Information::new));
        final int spacing;
        final int separation;
        final int salt;

        public Information(int spacing, int separation, int salt) {
            this.spacing = spacing;
            this.separation = separation;
            this.salt = salt;
        }

        public <T> Dynamic<T> method_28288(DynamicOps<T> dynamicOps) {
            return new Dynamic(dynamicOps, CODEC.encodeStart(dynamicOps, (Object)this).result().orElse(dynamicOps.emptyMap()));
        }
    }
}

