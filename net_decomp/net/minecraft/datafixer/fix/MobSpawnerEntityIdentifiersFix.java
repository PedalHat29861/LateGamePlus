/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class MobSpawnerEntityIdentifiersFix
extends DataFix {
    public MobSpawnerEntityIdentifiersFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private Dynamic<?> fixSpawner(Dynamic<?> spawnerDynamic) {
        Optional optional2;
        if (!"MobSpawner".equals(spawnerDynamic.get("id").asString(""))) {
            return spawnerDynamic;
        }
        Optional optional = spawnerDynamic.get("EntityId").asString().result();
        if (optional.isPresent()) {
            Dynamic dynamic = (Dynamic)DataFixUtils.orElse((Optional)spawnerDynamic.get("SpawnData").result(), (Object)spawnerDynamic.emptyMap());
            dynamic = dynamic.set("id", dynamic.createString(((String)optional.get()).isEmpty() ? "Pig" : (String)optional.get()));
            spawnerDynamic = spawnerDynamic.set("SpawnData", dynamic);
            spawnerDynamic = spawnerDynamic.remove("EntityId");
        }
        if ((optional2 = spawnerDynamic.get("SpawnPotentials").asStreamOpt().result()).isPresent()) {
            spawnerDynamic = spawnerDynamic.set("SpawnPotentials", spawnerDynamic.createList(((Stream)optional2.get()).map(spawnPotentialsDynamic -> {
                Optional optional = spawnPotentialsDynamic.get("Type").asString().result();
                if (optional.isPresent()) {
                    Dynamic dynamic = ((Dynamic)DataFixUtils.orElse((Optional)spawnPotentialsDynamic.get("Properties").result(), (Object)spawnPotentialsDynamic.emptyMap())).set("id", spawnPotentialsDynamic.createString((String)optional.get()));
                    return spawnPotentialsDynamic.set("Entity", dynamic).remove("Type").remove("Properties");
                }
                return spawnPotentialsDynamic;
            })));
        }
        return spawnerDynamic;
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(TypeReferences.UNTAGGED_SPAWNER);
        return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(TypeReferences.UNTAGGED_SPAWNER), type, untaggedSpawnerTyped -> {
            Dynamic dynamic = (Dynamic)untaggedSpawnerTyped.get(DSL.remainderFinder());
            DataResult dataResult = type.readTyped(this.fixSpawner(dynamic = dynamic.set("id", dynamic.createString("MobSpawner"))));
            if (dataResult.result().isEmpty()) {
                return untaggedSpawnerTyped;
            }
            return (Typed)((Pair)dataResult.result().get()).getFirst();
        });
    }
}

