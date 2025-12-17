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

public class DebugProfileOverlayReferenceFix
extends DataFix {
    public DebugProfileOverlayReferenceFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DebugProfileOverlayReferenceFix", this.getInputSchema().getType(TypeReferences.DEBUG_PROFILE), typed -> typed.update(DSL.remainderFinder(), profile -> profile.update("custom", map -> map.updateMapValues(pair -> pair.mapSecond(value -> {
            if (value.asString("").equals("inF3")) {
                return value.createString("inOverlay");
            }
            return value;
        })))));
    }
}

