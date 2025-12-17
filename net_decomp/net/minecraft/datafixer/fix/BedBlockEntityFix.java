/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.datafixer.TypeReferences;

public class BedBlockEntityFix
extends DataFix {
    public BedBlockEntityFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(TypeReferences.CHUNK);
        Type type2 = type.findFieldType("Level");
        Type type3 = type2.findFieldType("TileEntities");
        if (!(type3 instanceof List.ListType)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        }
        List.ListType listType = (List.ListType)type3;
        return this.fix(type2, listType);
    }

    private <TE> TypeRewriteRule fix(Type<?> level, List.ListType<TE> blockEntities) {
        Type type = blockEntities.getElement();
        OpticFinder opticFinder = DSL.fieldFinder((String)"Level", level);
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"TileEntities", blockEntities);
        int i = 416;
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhere("InjectBedBlockEntityType", (Type)this.getInputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY), (Type)this.getOutputSchema().findChoiceType(TypeReferences.BLOCK_ENTITY), dynamicOps -> pair -> pair), (TypeRewriteRule)this.fixTypeEverywhereTyped("BedBlockEntityInjecter", this.getOutputSchema().getType(TypeReferences.CHUNK), typed -> {
            Typed typed2 = typed.getTyped(opticFinder);
            Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
            int i = dynamic.get("xPos").asInt(0);
            int j = dynamic.get("zPos").asInt(0);
            ArrayList list = Lists.newArrayList((Iterable)((Iterable)typed2.getOrCreate(opticFinder2)));
            List list2 = dynamic.get("Sections").asList(Function.identity());
            for (Dynamic dynamic2 : list2) {
                int k = dynamic2.get("Y").asInt(0);
                Streams.mapWithIndex((IntStream)dynamic2.get("Blocks").asIntStream(), (blockData, index) -> {
                    if (416 == (blockData & 0xFF) << 4) {
                        int l = (int)index;
                        int m = l & 0xF;
                        int n = l >> 8 & 0xF;
                        int o = l >> 4 & 0xF;
                        HashMap map = Maps.newHashMap();
                        map.put(dynamic2.createString("id"), dynamic2.createString("minecraft:bed"));
                        map.put(dynamic2.createString("x"), dynamic2.createInt(m + (i << 4)));
                        map.put(dynamic2.createString("y"), dynamic2.createInt(n + (k << 4)));
                        map.put(dynamic2.createString("z"), dynamic2.createInt(o + (j << 4)));
                        map.put(dynamic2.createString("color"), dynamic2.createShort((short)14));
                        return map;
                    }
                    return null;
                }).forEachOrdered(map -> {
                    if (map != null) {
                        list.add(((Pair)type.read(dynamic2.createMap(map)).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity."))).getFirst());
                    }
                });
            }
            if (!list.isEmpty()) {
                return typed.set(opticFinder, typed2.set(opticFinder2, (Object)list));
            }
            return typed;
        }));
    }
}

