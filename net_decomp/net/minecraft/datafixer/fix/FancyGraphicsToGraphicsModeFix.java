/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class FancyGraphicsToGraphicsModeFix
extends DataFix {
    public FancyGraphicsToGraphicsModeFix(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("fancyGraphics to graphicsMode", this.getInputSchema().getType(TypeReferences.OPTIONS), typed -> typed.update(DSL.remainderFinder(), options -> options.renameAndFixField("fancyGraphics", "graphicsMode", FancyGraphicsToGraphicsModeFix::fx)));
    }

    private static <T> Dynamic<T> fx(Dynamic<T> value) {
        if ("true".equals(value.asString("true"))) {
            return value.createString("1");
        }
        return value.createString("0");
    }
}

