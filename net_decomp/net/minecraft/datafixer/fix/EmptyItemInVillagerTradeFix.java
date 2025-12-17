/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class EmptyItemInVillagerTradeFix
extends DataFix {
    public EmptyItemInVillagerTradeFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.VILLAGER_TRADE);
        return this.writeFixAndRead("EmptyItemInVillagerTradeFix", type, type, villagerTradeDynamic -> {
            Dynamic dynamic = villagerTradeDynamic.get("buyB").orElseEmptyMap();
            String string = IdentifierNormalizingSchema.normalize(dynamic.get("id").asString("minecraft:air"));
            int i = dynamic.get("count").asInt(0);
            if (string.equals("minecraft:air") || i == 0) {
                return villagerTradeDynamic.remove("buyB");
            }
            return villagerTradeDynamic;
        });
    }
}

