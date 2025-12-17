/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.TextFixes;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.util.Util;

public class EntityCustomNameToTextFix
extends DataFix {
    public EntityCustomNameToTextFix(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.ENTITY);
        Type type2 = this.getOutputSchema().getType(TypeReferences.ENTITY);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", IdentifierNormalizingSchema.getIdentifierType());
        OpticFinder opticFinder2 = type.findField("CustomName");
        Type type3 = type2.findFieldType("CustomName");
        return this.fixTypeEverywhereTyped("EntityCustomNameToComponentFix", type, type2, typed -> EntityCustomNameToTextFix.method_66064(typed, type2, (OpticFinder<String>)opticFinder, (OpticFinder<String>)opticFinder2, type3));
    }

    private static <T> Typed<?> method_66064(Typed<?> typed, Type<?> type, OpticFinder<String> opticFinder, OpticFinder<String> opticFinder2, Type<T> type2) {
        Optional optional = typed.getOptional(opticFinder2);
        if (optional.isEmpty()) {
            return FixUtil.withType(type, typed);
        }
        if (((String)optional.get()).isEmpty()) {
            return Util.apply(typed, type, dynamic -> dynamic.remove("CustomName"));
        }
        String string = typed.getOptional(opticFinder).orElse("");
        Dynamic<T> dynamic2 = EntityCustomNameToTextFix.method_66066(typed.getOps(), (String)optional.get(), string);
        return typed.set(opticFinder2, Util.readTyped(type2, dynamic2));
    }

    private static <T> Dynamic<T> method_66066(DynamicOps<T> dynamicOps, String string, String string2) {
        if ("minecraft:commandblock_minecart".equals(string2)) {
            return new Dynamic(dynamicOps, dynamicOps.createString(string));
        }
        return TextFixes.text(dynamicOps, string);
    }
}

