/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;

public class DisplayNameFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference typeReference;

    public DisplayNameFix(Schema outputSchema, String name, DSL.TypeReference typeReference) {
        super(outputSchema, false);
        this.name = name;
        this.typeReference = typeReference;
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(this.typeReference);
        OpticFinder opticFinder = type.findField("DisplayName");
        OpticFinder opticFinder2 = DSL.typeFinder((Type)this.getInputSchema().getType(TypeReferences.TEXT_COMPONENT));
        return this.fixTypeEverywhereTyped(this.name, type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(opticFinder2, pair -> pair.mapSecond(TextFixes::text))));
    }
}

