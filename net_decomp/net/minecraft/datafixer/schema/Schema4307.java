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
import net.minecraft.datafixer.schema.Schema4059;

public class Schema4307
extends IdentifierNormalizingSchema {
    public Schema4307(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> method_67118(Schema schema) {
        SequencedMap<String, Supplier<TypeTemplate>> sequencedMap = Schema4059.method_63584(schema);
        sequencedMap.put("minecraft:can_place_on", () -> Schema4307.method_67119(schema));
        sequencedMap.put("minecraft:can_break", () -> Schema4307.method_67119(schema));
        return sequencedMap;
    }

    private static TypeTemplate method_67119(Schema schema) {
        TypeTemplate typeTemplate = DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema))));
        return DSL.or((TypeTemplate)typeTemplate, (TypeTemplate)DSL.list((TypeTemplate)typeTemplate));
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(Schema4307.method_67118(schema)));
    }
}

