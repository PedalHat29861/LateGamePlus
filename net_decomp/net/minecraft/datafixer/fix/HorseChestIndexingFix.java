/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;

public class HorseChestIndexingFix
extends DataFix {
    public HorseChestIndexingFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.typeFinder((Type)this.getInputSchema().getType(TypeReferences.ITEM_STACK));
        Type type = this.getInputSchema().getType(TypeReferences.ENTITY);
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixIndexing(opticFinder, type, "minecraft:llama"), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixIndexing(opticFinder, type, "minecraft:trader_llama"), this.fixIndexing(opticFinder, type, "minecraft:mule"), this.fixIndexing(opticFinder, type, "minecraft:donkey")});
    }

    private TypeRewriteRule fixIndexing(OpticFinder<Pair<String, Pair<Either<Pair<String, String>, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>> itemStackOpticFinder, Type<?> entityType, String entityId) {
        Type type = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, entityId);
        OpticFinder opticFinder = DSL.namedChoice((String)entityId, (Type)type);
        OpticFinder opticFinder2 = type.findField("Items");
        return this.fixTypeEverywhereTyped("Fix non-zero indexing in chest horse type " + entityId, entityType, entityTyped -> entityTyped.updateTyped(opticFinder, specificEntityTyped -> specificEntityTyped.updateTyped(opticFinder2, entityItemsTyped -> entityItemsTyped.update(itemStackOpticFinder, itemStackEntry -> itemStackEntry.mapSecond(pair2 -> pair2.mapSecond(pair -> pair.mapSecond(itemStackDynamic -> itemStackDynamic.update("Slot", slotDynamic -> slotDynamic.createByte((byte)(slotDynamic.asInt(2) - 2))))))))));
    }
}

