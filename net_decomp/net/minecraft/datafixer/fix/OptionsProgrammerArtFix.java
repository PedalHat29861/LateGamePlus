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

public class OptionsProgrammerArtFix
extends DataFix {
    public OptionsProgrammerArtFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsProgrammerArtFix", this.getInputSchema().getType(TypeReferences.OPTIONS), typed -> typed.update(DSL.remainderFinder(), options -> options.update("resourcePacks", this::replaceTypo).update("incompatibleResourcePacks", this::replaceTypo)));
    }

    private <T> Dynamic<T> replaceTypo(Dynamic<T> option) {
        return option.asString().result().map(value -> option.createString(value.replace("\"programer_art\"", "\"programmer_art\""))).orElse(option);
    }
}

