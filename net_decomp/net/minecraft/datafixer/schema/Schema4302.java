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
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema4302
extends IdentifierNormalizingSchema {
    public Schema4302(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map map = super.registerBlockEntities(schema);
        schema.registerSimple(map, "minecraft:test_block");
        schema.register(map, "minecraft:test_instance_block", () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"error_message", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)), (String)"errors", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"text", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)))));
        return map;
    }
}

