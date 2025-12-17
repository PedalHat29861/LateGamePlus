/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.fix;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.function.Supplier;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityTransformFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ThrownPotionSplitFix
extends EntityTransformFix {
    private final Supplier<class_10681> field_56251 = Suppliers.memoize(() -> {
        Type type = this.getInputSchema().getChoiceType(TypeReferences.ENTITY, "minecraft:potion");
        Type<?> type2 = FixUtil.withTypeChanged(type, this.getInputSchema().getType(TypeReferences.ENTITY), this.getOutputSchema().getType(TypeReferences.ENTITY));
        OpticFinder opticFinder = type2.findField("Item");
        OpticFinder opticFinder2 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        return new class_10681(opticFinder, (OpticFinder<Pair<String, String>>)opticFinder2);
    });

    public ThrownPotionSplitFix(Schema schema) {
        super("ThrownPotionSplitFix", schema, true);
    }

    @Override
    protected Pair<String, Typed<?>> transform(String choice, Typed<?> entityTyped) {
        if (!choice.equals("minecraft:potion")) {
            return Pair.of((Object)choice, entityTyped);
        }
        String string = this.field_56251.get().method_67102(entityTyped);
        if ("minecraft:lingering_potion".equals(string)) {
            return Pair.of((Object)"minecraft:lingering_potion", entityTyped);
        }
        return Pair.of((Object)"minecraft:splash_potion", entityTyped);
    }

    record class_10681(OpticFinder<?> itemFinder, OpticFinder<Pair<String, String>> itemIdFinder) {
        public String method_67102(Typed<?> typed2) {
            return typed2.getOptionalTyped(this.itemFinder).flatMap(typed -> typed.getOptional(this.itemIdFinder)).map(Pair::getSecond).map(IdentifierNormalizingSchema::normalize).orElse("");
        }
    }
}

