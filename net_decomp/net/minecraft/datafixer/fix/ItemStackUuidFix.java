/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.AbstractUuidFix;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class ItemStackUuidFix
extends AbstractUuidFix {
    public ItemStackUuidFix(Schema outputSchema) {
        super(outputSchema, TypeReferences.ITEM_STACK);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)TypeReferences.ITEM_NAME.typeName(), IdentifierNormalizingSchema.getIdentifierType()));
        return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), itemStackTyped -> {
            OpticFinder opticFinder2 = itemStackTyped.getType().findField("tag");
            return itemStackTyped.updateTyped(opticFinder2, tagTyped -> tagTyped.update(DSL.remainderFinder(), tagDynamic -> {
                tagDynamic = this.fixAttributeModifiers((Dynamic<?>)tagDynamic);
                if (itemStackTyped.getOptional(opticFinder).map(id -> "minecraft:player_head".equals(id.getSecond())).orElse(false).booleanValue()) {
                    tagDynamic = this.fixSkullOwner((Dynamic<?>)tagDynamic);
                }
                return tagDynamic;
            }));
        });
    }

    private Dynamic<?> fixAttributeModifiers(Dynamic<?> tagDynamic) {
        return tagDynamic.update("AttributeModifiers", attributeModifiersDynamic -> tagDynamic.createList(attributeModifiersDynamic.asStream().map(attributeModifier -> ItemStackUuidFix.updateRegularMostLeast(attributeModifier, "UUID", "UUID").orElse((Dynamic<?>)attributeModifier))));
    }

    private Dynamic<?> fixSkullOwner(Dynamic<?> tagDynamic) {
        return tagDynamic.update("SkullOwner", skullOwner -> ItemStackUuidFix.updateStringUuid(skullOwner, "Id", "Id").orElse((Dynamic<?>)skullOwner));
    }
}

