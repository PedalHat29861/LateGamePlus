/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.datafixer.TypeReferences;

public class BlendingDataRemoveFromNetherEndFix
extends DataFix {
    public BlendingDataRemoveFromNetherEndFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(TypeReferences.CHUNK);
        return this.fixTypeEverywhereTyped("BlendingDataRemoveFromNetherEndFix", type, typed -> typed.update(DSL.remainderFinder(), chunk -> BlendingDataRemoveFromNetherEndFix.removeInapplicableBlendingData(chunk, chunk.get("__context"))));
    }

    private static Dynamic<?> removeInapplicableBlendingData(Dynamic<?> chunk, OptionalDynamic<?> context) {
        boolean bl = "minecraft:overworld".equals(context.get("dimension").asString().result().orElse(""));
        return bl ? chunk : chunk.remove("blending_data");
    }
}

