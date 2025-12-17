/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class EntityFallDistanceFloatToDoubleFix
extends DataFix {
    private final DSL.TypeReference typeReference;

    public EntityFallDistanceFloatToDoubleFix(Schema outputSchema, DSL.TypeReference typeReference) {
        super(outputSchema, false);
        this.typeReference = typeReference;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityFallDistanceFloatToDoubleFixFor" + this.typeReference.typeName(), this.getOutputSchema().getType(this.typeReference), EntityFallDistanceFloatToDoubleFix::fixFallDistance);
    }

    private static Typed<?> fixFallDistance(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.renameAndFixField("FallDistance", "fall_distance", dynamic -> dynamic.createDouble((double)dynamic.asFloat(0.0f))));
    }
}

