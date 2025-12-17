/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class SetGraphicsPresetToCustomFix
extends DataFix {
    public SetGraphicsPresetToCustomFix(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("graphicsPreset set to \"custom\"", this.getInputSchema().getType(TypeReferences.OPTIONS), typed -> typed.update(DSL.remainderFinder(), options -> options.set("graphicsPreset", options.createString("custom"))));
    }
}

