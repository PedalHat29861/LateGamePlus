/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class OptionsAddTextBackgroundFix
extends DataFix {
    public OptionsAddTextBackgroundFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsAddTextBackgroundFix", this.getInputSchema().getType(TypeReferences.OPTIONS), optionsTyped -> optionsTyped.update(DSL.remainderFinder(), optionsDynamic -> (Dynamic)DataFixUtils.orElse((Optional)optionsDynamic.get("chatOpacity").asString().map(string -> {
            double d = this.convertToTextBackgroundOpacity((String)string);
            return optionsDynamic.set("textBackgroundOpacity", optionsDynamic.createString(String.valueOf(d)));
        }).result(), (Object)optionsDynamic)));
    }

    private double convertToTextBackgroundOpacity(String chatOpacity) {
        try {
            double d = 0.9 * Double.parseDouble(chatOpacity) + 0.1;
            return d / 2.0;
        }
        catch (NumberFormatException numberFormatException) {
            return 0.5;
        }
    }
}

