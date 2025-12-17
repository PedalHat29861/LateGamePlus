/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema4290
extends IdentifierNormalizingSchema {
    public Schema4290(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.TEXT_COMPONENT, () -> DSL.or((TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.string()), (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))), (TypeTemplate)DSL.optionalFields((String)"extra", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)), (String)"separator", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"hoverEvent", (TypeTemplate)DSL.taggedChoice((String)"action", (Type)DSL.string(), Map.of("show_text", DSL.optionalFields((String)"contents", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)), "show_item", DSL.optionalFields((String)"contents", (TypeTemplate)DSL.or((TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (TypeTemplate)TypeReferences.ITEM_NAME.in(schema))), "show_entity", DSL.optionalFields((String)"type", (TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (String)"name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)))))));
    }
}

