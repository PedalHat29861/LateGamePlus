/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WorldGenSettingsHeightAndBiomeFix
extends DataFix {
    private static final String NAME = "WorldGenSettingsHeightAndBiomeFix";
    public static final String HAS_INCREASED_HEIGHT_ALREADY_KEY = "has_increased_height_already";

    public WorldGenSettingsHeightAndBiomeFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.WORLD_GEN_SETTINGS);
        OpticFinder opticFinder = type.findField("dimensions");
        Type type2 = this.getOutputSchema().getType(TypeReferences.WORLD_GEN_SETTINGS);
        Type type3 = type2.findFieldType("dimensions");
        return this.fixTypeEverywhereTyped(NAME, type, type2, worldGenSettingsTyped -> {
            OptionalDynamic optionalDynamic = ((Dynamic)worldGenSettingsTyped.get(DSL.remainderFinder())).get(HAS_INCREASED_HEIGHT_ALREADY_KEY);
            boolean bl = optionalDynamic.result().isEmpty();
            boolean bl2 = optionalDynamic.asBoolean(true);
            return worldGenSettingsTyped.update(DSL.remainderFinder(), worldGenSettingsDynamic -> worldGenSettingsDynamic.remove(HAS_INCREASED_HEIGHT_ALREADY_KEY)).updateTyped(opticFinder, type3, dimensionsTyped -> Util.apply(dimensionsTyped, type3, dimensionsDynamic -> dimensionsDynamic.update("minecraft:overworld", overworldDimensionDynamic -> overworldDimensionDynamic.update("generator", overworldGeneratorDynamic -> {
                String string = overworldGeneratorDynamic.get("type").asString("");
                if ("minecraft:noise".equals(string)) {
                    MutableBoolean mutableBoolean = new MutableBoolean();
                    overworldGeneratorDynamic = overworldGeneratorDynamic.update("biome_source", overworldBiomeSourceDynamic -> {
                        String string = overworldBiomeSourceDynamic.get("type").asString("");
                        if ("minecraft:vanilla_layered".equals(string) || bl && "minecraft:multi_noise".equals(string)) {
                            if (overworldBiomeSourceDynamic.get("large_biomes").asBoolean(false)) {
                                mutableBoolean.setTrue();
                            }
                            return overworldBiomeSourceDynamic.createMap((Map)ImmutableMap.of((Object)overworldBiomeSourceDynamic.createString("preset"), (Object)overworldBiomeSourceDynamic.createString("minecraft:overworld"), (Object)overworldBiomeSourceDynamic.createString("type"), (Object)overworldBiomeSourceDynamic.createString("minecraft:multi_noise")));
                        }
                        return overworldBiomeSourceDynamic;
                    });
                    if (mutableBoolean.booleanValue()) {
                        return overworldGeneratorDynamic.update("settings", overworldGeneratorSettingsDynamic -> {
                            if ("minecraft:overworld".equals(overworldGeneratorSettingsDynamic.asString(""))) {
                                return overworldGeneratorSettingsDynamic.createString("minecraft:large_biomes");
                            }
                            return overworldGeneratorSettingsDynamic;
                        });
                    }
                    return overworldGeneratorDynamic;
                }
                if ("minecraft:flat".equals(string)) {
                    if (bl2) {
                        return overworldGeneratorDynamic;
                    }
                    return overworldGeneratorDynamic.update("settings", overworldGeneratorSettingsDynamic -> overworldGeneratorSettingsDynamic.update("layers", WorldGenSettingsHeightAndBiomeFix::fillWithAir));
                }
                return overworldGeneratorDynamic;
            }))));
        });
    }

    private static Dynamic<?> fillWithAir(Dynamic<?> dynamic) {
        Dynamic dynamic2 = dynamic.createMap((Map)ImmutableMap.of((Object)dynamic.createString("height"), (Object)dynamic.createInt(64), (Object)dynamic.createString("block"), (Object)dynamic.createString("minecraft:air")));
        return dynamic.createList(Stream.concat(Stream.of(dynamic2), dynamic.asStream()));
    }
}

