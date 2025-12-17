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
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class VillagerTradeFix
extends DataFix {
    public VillagerTradeFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.VILLAGER_TRADE);
        OpticFinder opticFinder = type.findField("buy");
        OpticFinder opticFinder2 = type.findField("buyB");
        OpticFinder opticFinder3 = type.findField("sell");
        OpticFinder opticFinder4 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        Function<Typed, Typed> function = itemTyped -> this.fixPumpkinTrade((OpticFinder<Pair<String, String>>)opticFinder4, (Typed<?>)itemTyped);
        return this.fixTypeEverywhereTyped("Villager trade fix", type, villagerTradeTyped -> villagerTradeTyped.updateTyped(opticFinder, function).updateTyped(opticFinder2, function).updateTyped(opticFinder3, function));
    }

    private Typed<?> fixPumpkinTrade(OpticFinder<Pair<String, String>> idOpticFinder, Typed<?> itemTyped) {
        return itemTyped.update(idOpticFinder, entry -> entry.mapSecond(id -> Objects.equals(id, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : id));
    }
}

