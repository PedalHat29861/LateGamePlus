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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema3818_3
extends IdentifierNormalizingSchema {
    public Schema3818_3(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> method_63573(Schema schema) {
        LinkedHashMap<String, Supplier<TypeTemplate>> sequencedMap = new LinkedHashMap<String, Supplier<TypeTemplate>>();
        sequencedMap.put("minecraft:bees", () -> DSL.list((TypeTemplate)DSL.optionalFields((String)"entity_data", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))));
        sequencedMap.put("minecraft:block_entity_data", () -> TypeReferences.BLOCK_ENTITY.in(schema));
        sequencedMap.put("minecraft:bundle_contents", () -> DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        sequencedMap.put("minecraft:can_break", () -> DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)))))));
        sequencedMap.put("minecraft:can_place_on", () -> DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)))))));
        sequencedMap.put("minecraft:charged_projectiles", () -> DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        sequencedMap.put("minecraft:container", () -> DSL.list((TypeTemplate)DSL.optionalFields((String)"item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        sequencedMap.put("minecraft:entity_data", () -> TypeReferences.ENTITY_TREE.in(schema));
        sequencedMap.put("minecraft:pot_decorations", () -> DSL.list((TypeTemplate)TypeReferences.ITEM_NAME.in(schema)));
        sequencedMap.put("minecraft:food", () -> DSL.optionalFields((String)"using_converts_to", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        sequencedMap.put("minecraft:custom_name", () -> TypeReferences.TEXT_COMPONENT.in(schema));
        sequencedMap.put("minecraft:item_name", () -> TypeReferences.TEXT_COMPONENT.in(schema));
        sequencedMap.put("minecraft:lore", () -> DSL.list((TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        sequencedMap.put("minecraft:written_book_content", () -> DSL.optionalFields((String)"pages", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)DSL.optionalFields((String)"raw", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"filtered", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)), (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)))));
        return sequencedMap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(Schema3818_3.method_63573(schema)));
    }
}

