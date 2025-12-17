/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;

public class ChunkLevelTagRenameFix
extends DataFix {
    public ChunkLevelTagRenameFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.CHUNK);
        OpticFinder opticFinder = type.findField("Level");
        OpticFinder opticFinder2 = opticFinder.type().findField("Structures");
        Type type2 = this.getOutputSchema().getType(TypeReferences.CHUNK);
        Type type3 = type2.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", type, type2, chunkTyped -> {
            Typed typed = chunkTyped.getTyped(opticFinder);
            Typed<?> typed2 = ChunkLevelTagRenameFix.labelWithChunk(typed);
            typed2 = typed2.set(DSL.remainderFinder(), ChunkLevelTagRenameFix.method_39270(chunkTyped, (Dynamic)typed.get(DSL.remainderFinder())));
            typed2 = ChunkLevelTagRenameFix.rename(typed2, "TileEntities", "block_entities");
            typed2 = ChunkLevelTagRenameFix.rename(typed2, "TileTicks", "block_ticks");
            typed2 = ChunkLevelTagRenameFix.rename(typed2, "Entities", "entities");
            typed2 = ChunkLevelTagRenameFix.rename(typed2, "Sections", "sections");
            typed2 = typed2.updateTyped(opticFinder2, type3, structuresTyped -> ChunkLevelTagRenameFix.rename(structuresTyped, "Starts", "starts"));
            typed2 = ChunkLevelTagRenameFix.rename(typed2, "Structures", "structures");
            return typed2.update(DSL.remainderFinder(), dynamic -> dynamic.remove("Level"));
        });
    }

    private static Typed<?> rename(Typed<?> typed, String oldKey, String newKey) {
        return ChunkLevelTagRenameFix.rename(typed, oldKey, newKey, typed.getType().findFieldType(oldKey)).update(DSL.remainderFinder(), dynamic -> dynamic.remove(oldKey));
    }

    private static <A> Typed<?> rename(Typed<?> typed, String oldKey, String newKey, Type<A> type) {
        Type type2 = DSL.optional((Type)DSL.field((String)oldKey, type));
        Type type3 = DSL.optional((Type)DSL.field((String)newKey, type));
        return typed.update(type2.finder(), type3, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> labelWithChunk(Typed<A> outputTyped) {
        return new Typed(DSL.named((String)"chunk", (Type)outputTyped.getType()), outputTyped.getOps(), (Object)Pair.of((Object)"chunk", (Object)outputTyped.getValue()));
    }

    private static <T> Dynamic<T> method_39270(Typed<?> chunkTyped, Dynamic<T> chunkDynamic) {
        DynamicOps dynamicOps = chunkDynamic.getOps();
        Dynamic dynamic = ((Dynamic)chunkTyped.get(DSL.remainderFinder())).convert(dynamicOps);
        DataResult dataResult = dynamicOps.getMap(chunkDynamic.getValue()).flatMap(mapLike -> dynamicOps.mergeToMap(dynamic.getValue(), mapLike));
        return dataResult.result().map(object -> new Dynamic(dynamicOps, object)).orElse(chunkDynamic);
    }
}

