/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import net.minecraft.datafixer.TypeReferences;

public class DropChancesFormatFix
extends DataFix {
    private static final List<String> field_55634 = List.of("feet", "legs", "chest", "head");
    private static final List<String> field_55635 = List.of("mainhand", "offhand");
    private static final float field_55636 = 0.085f;

    public DropChancesFormatFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DropChancesFormatFix", this.getInputSchema().getType(TypeReferences.ENTITY), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            List<Float> list = DropChancesFormatFix.method_66058(dynamic.get("ArmorDropChances"));
            List<Float> list2 = DropChancesFormatFix.method_66058(dynamic.get("HandDropChances"));
            float f = dynamic.get("body_armor_drop_chance").asNumber().result().map(Number::floatValue).orElse(Float.valueOf(0.085f)).floatValue();
            dynamic = dynamic.remove("ArmorDropChances").remove("HandDropChances").remove("body_armor_drop_chance");
            Dynamic dynamic2 = dynamic.emptyMap();
            dynamic2 = DropChancesFormatFix.method_66057(dynamic2, list, field_55634);
            dynamic2 = DropChancesFormatFix.method_66057(dynamic2, list2, field_55635);
            if (f != 0.085f) {
                dynamic2 = dynamic2.set("body", dynamic.createFloat(f));
            }
            if (!dynamic2.equals((Object)dynamic.emptyMap())) {
                return dynamic.set("drop_chances", dynamic2);
            }
            return dynamic;
        }));
    }

    private static Dynamic<?> method_66057(Dynamic<?> dynamic, List<Float> list, List<String> list2) {
        for (int i = 0; i < list2.size() && i < list.size(); ++i) {
            String string = list2.get(i);
            float f = list.get(i).floatValue();
            if (f == 0.085f) continue;
            dynamic = dynamic.set(string, dynamic.createFloat(f));
        }
        return dynamic;
    }

    private static List<Float> method_66058(OptionalDynamic<?> optionalDynamic) {
        return optionalDynamic.asStream().map(dynamic -> Float.valueOf(dynamic.asFloat(0.085f))).toList();
    }
}

