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

public class OptionsMenuBlurrinessFix
extends DataFix {
    public OptionsMenuBlurrinessFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsMenuBlurrinessFix", this.getInputSchema().getType(TypeReferences.OPTIONS), optionsTyped -> optionsTyped.update(DSL.remainderFinder(), optionsDynamic -> optionsDynamic.update("menuBackgroundBlurriness", dynamic -> {
            int i = this.update(dynamic.asString("0.5"));
            return dynamic.createString(String.valueOf(i));
        })));
    }

    private int update(String value) {
        try {
            return Math.round(Float.parseFloat(value) * 10.0f);
        }
        catch (NumberFormatException numberFormatException) {
            return 5;
        }
    }
}

