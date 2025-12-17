/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.datafixer.TypeReferences;

public class RenameEntityAttributesFix
extends DataFix {
    private final String description;
    private final UnaryOperator<String> renames;

    public RenameEntityAttributesFix(Schema outputSchema, String description, UnaryOperator<String> renames) {
        super(outputSchema, false);
        this.description = description;
        this.renames = renames;
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        OpticFinder opticFinder = type.findField("tag");
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped(this.description + " (ItemStack)", type, itemStackTyped -> itemStackTyped.updateTyped(opticFinder, this::updateAttributeModifiers)), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped(this.description + " (Entity)", this.getInputSchema().getType(TypeReferences.ENTITY), this::updateEntityAttributes), this.fixTypeEverywhereTyped(this.description + " (Player)", this.getInputSchema().getType(TypeReferences.PLAYER), this::updateEntityAttributes)});
    }

    private Dynamic<?> updateAttributeName(Dynamic<?> attributeNameDynamic) {
        return (Dynamic)DataFixUtils.orElse(attributeNameDynamic.asString().result().map(this.renames).map(arg_0 -> attributeNameDynamic.createString(arg_0)), attributeNameDynamic);
    }

    private Typed<?> updateAttributeModifiers(Typed<?> tagTyped) {
        return tagTyped.update(DSL.remainderFinder(), tagDynamic -> tagDynamic.update("AttributeModifiers", attributeModifiersDynamic -> (Dynamic)DataFixUtils.orElse(attributeModifiersDynamic.asStreamOpt().result().map(attributeModifiers -> attributeModifiers.map(attributeModifierDynamic -> attributeModifierDynamic.update("AttributeName", this::updateAttributeName))).map(arg_0 -> ((Dynamic)attributeModifiersDynamic).createList(arg_0)), (Object)attributeModifiersDynamic)));
    }

    private Typed<?> updateEntityAttributes(Typed<?> entityTyped) {
        return entityTyped.update(DSL.remainderFinder(), entityDynamic -> entityDynamic.update("Attributes", attributesDynamic -> (Dynamic)DataFixUtils.orElse(attributesDynamic.asStreamOpt().result().map(attributes -> attributes.map(attributeDynamic -> attributeDynamic.update("Name", this::updateAttributeName))).map(arg_0 -> ((Dynamic)attributesDynamic).createList(arg_0)), (Object)attributesDynamic)));
    }
}

