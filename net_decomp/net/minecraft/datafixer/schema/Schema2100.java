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

public class Schema2100
extends IdentifierNormalizingSchema {
    public Schema2100(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerEntity(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, String name) {
        schema.registerSimple(entityTypes, name);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map map = super.registerEntities(schema);
        Schema2100.registerEntity(schema, map, "minecraft:bee");
        Schema2100.registerEntity(schema, map, "minecraft:bee_stinger");
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:beehive", () -> DSL.optionalFields((String)"Bees", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"EntityData", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema)))));
        return map;
    }
}

