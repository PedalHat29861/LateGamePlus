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
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema3818_3;

public class Schema4059
extends IdentifierNormalizingSchema {
    public Schema4059(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> method_63584(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedMap = Schema3818_3.method_63573(schema);
        sequencedMap.remove("minecraft:food");
        sequencedMap.put("minecraft:use_remainder", () -> TypeReferences.ITEM_STACK.in(schema));
        sequencedMap.put("minecraft:equippable", () -> DSL.optionalFields((String)"allowed_entities", (TypeTemplate)DSL.or((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema)))));
        return sequencedMap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(Schema4059.method_63584(schema)));
    }
}

