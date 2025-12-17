/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;

public class Schema100
extends Schema {
    public Schema100(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(true, TypeReferences.ENTITY_EQUIPMENT, () -> DSL.and((TypeTemplate)DSL.optional((TypeTemplate)DSL.field((String)"ArmorItems", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))), (TypeTemplate[])new TypeTemplate[]{DSL.optional((TypeTemplate)DSL.field((String)"HandItems", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))), DSL.optional((TypeTemplate)DSL.field((String)"body_armor_item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), DSL.optional((TypeTemplate)DSL.field((String)"saddle", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))}));
    }
}

