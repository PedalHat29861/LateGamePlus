/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;

public class WorldBorderWarningTimeFix
extends DataFix {
    public WorldBorderWarningTimeFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("WorldBorderWarningTimeFix", this.getInputSchema().getType(TypeReferences.WORLD_BORDER_SAVED_DATA), this.getOutputSchema().getType(TypeReferences.WORLD_BORDER_SAVED_DATA), dynamic2 -> dynamic2.update("data", dynamic -> dynamic.update("warning_time", dynamic2 -> dynamic.createInt(dynamic2.asInt(15) * 20))));
    }
}

