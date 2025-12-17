/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;

public class CustomModelDataExpansionFix
extends DataFix {
    public CustomModelDataExpansionFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.DATA_COMPONENTS);
        return this.fixTypeEverywhereTyped("Custom Model Data expansion", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("minecraft:custom_model_data", customModelDataDynamic -> {
            float f = customModelDataDynamic.asNumber((Number)Float.valueOf(0.0f)).floatValue();
            return customModelDataDynamic.createMap(Map.of(customModelDataDynamic.createString("floats"), customModelDataDynamic.createList(Stream.of(customModelDataDynamic.createFloat(f)))));
        })));
    }
}

