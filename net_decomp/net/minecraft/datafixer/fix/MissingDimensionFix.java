/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.FieldFinder
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.CompoundList$CompoundListType
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.StructureSeparationDataFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class MissingDimensionFix
extends DataFix {
    public MissingDimensionFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> extract1(String field, Type<A> type) {
        return DSL.and((Type)DSL.field((String)field, type), (Type)DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> extract1Opt(String field, Type<A> type) {
        return DSL.and((Type)DSL.optional((Type)DSL.field((String)field, type)), (Type)DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> extract2Opt(String field1, Type<A1> type1, String field2, Type<A2> type2) {
        return DSL.and((Type)DSL.optional((Type)DSL.field((String)field1, type1)), (Type)DSL.optional((Type)DSL.field((String)field2, type2)), (Type)DSL.remainderType());
    }

    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type type = DSL.taggedChoiceType((String)"type", (Type)DSL.string(), (Map)ImmutableMap.of((Object)"minecraft:debug", (Object)DSL.remainderType(), (Object)"minecraft:flat", MissingDimensionFix.flatGeneratorType(schema), (Object)"minecraft:noise", MissingDimensionFix.extract2Opt("biome_source", DSL.taggedChoiceType((String)"type", (Type)DSL.string(), (Map)ImmutableMap.of((Object)"minecraft:fixed", MissingDimensionFix.extract1("biome", schema.getType(TypeReferences.BIOME)), (Object)"minecraft:multi_noise", (Object)DSL.list(MissingDimensionFix.extract1("biome", schema.getType(TypeReferences.BIOME))), (Object)"minecraft:checkerboard", MissingDimensionFix.extract1("biomes", DSL.list((Type)schema.getType(TypeReferences.BIOME))), (Object)"minecraft:vanilla_layered", (Object)DSL.remainderType(), (Object)"minecraft:the_end", (Object)DSL.remainderType())), "settings", DSL.or((Type)DSL.string(), MissingDimensionFix.extract2Opt("default_block", schema.getType(TypeReferences.BLOCK_NAME), "default_fluid", schema.getType(TypeReferences.BLOCK_NAME))))));
        CompoundList.CompoundListType compoundListType = DSL.compoundList(IdentifierNormalizingSchema.getIdentifierType(), MissingDimensionFix.extract1("generator", type));
        Type type2 = DSL.and((Type)compoundListType, (Type)DSL.remainderType());
        Type type3 = schema.getType(TypeReferences.WORLD_GEN_SETTINGS);
        FieldFinder fieldFinder = new FieldFinder("dimensions", type2);
        if (!type3.findFieldType("dimensions").equals((Object)type2)) {
            throw new IllegalStateException();
        }
        OpticFinder opticFinder = compoundListType.finder();
        return this.fixTypeEverywhereTyped("MissingDimensionFix", type3, worldGenSettingsTyped -> worldGenSettingsTyped.updateTyped((OpticFinder)fieldFinder, dimensionsTyped -> dimensionsTyped.updateTyped(opticFinder, dimensionsListTyped -> {
            if (!(dimensionsListTyped.getValue() instanceof List)) {
                throw new IllegalStateException("List exptected");
            }
            if (((List)dimensionsListTyped.getValue()).isEmpty()) {
                Dynamic dynamic = (Dynamic)worldGenSettingsTyped.get(DSL.remainderFinder());
                Dynamic dynamic2 = this.method_29912(dynamic);
                return (Typed)DataFixUtils.orElse(compoundListType.readTyped(dynamic2).result().map(Pair::getFirst), (Object)dimensionsListTyped);
            }
            return dimensionsListTyped;
        })));
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatGeneratorType(Schema schema) {
        return MissingDimensionFix.extract1Opt("settings", MissingDimensionFix.extract2Opt("biome", schema.getType(TypeReferences.BIOME), "layers", DSL.list(MissingDimensionFix.extract1Opt("block", schema.getType(TypeReferences.BLOCK_NAME)))));
    }

    private <T> Dynamic<T> method_29912(Dynamic<T> worldGenSettingsDynamic) {
        long l = worldGenSettingsDynamic.get("seed").asLong(0L);
        return new Dynamic(worldGenSettingsDynamic.getOps(), StructureSeparationDataFix.createDimensionSettings(worldGenSettingsDynamic, l, StructureSeparationDataFix.createDefaultOverworldGeneratorSettings(worldGenSettingsDynamic, l), false));
    }
}

