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
import java.util.Locale;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class OptionsLowerCaseLanguageFix
extends DataFix {
    public OptionsLowerCaseLanguageFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsLowerCaseLanguageFix", this.getInputSchema().getType(TypeReferences.OPTIONS), optionsTyped -> optionsTyped.update(DSL.remainderFinder(), optionsDynamic -> {
            Optional optional = optionsDynamic.get("lang").asString().result();
            if (optional.isPresent()) {
                return optionsDynamic.set("lang", optionsDynamic.createString(((String)optional.get()).toLowerCase(Locale.ROOT)));
            }
            return optionsDynamic;
        }));
    }
}

