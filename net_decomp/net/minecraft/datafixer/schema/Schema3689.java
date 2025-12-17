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

public class Schema3689
extends IdentifierNormalizingSchema {
    public Schema3689(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map map = super.registerEntities(schema);
        schema.registerSimple(map, "minecraft:breeze");
        schema.registerSimple(map, "minecraft:wind_charge");
        schema.registerSimple(map, "minecraft:breeze_wind_charge");
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:trial_spawner", () -> DSL.optionalFields((String)"spawn_potentials", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"data", (TypeTemplate)DSL.fields((String)"entity", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema)))), (String)"spawn_data", (TypeTemplate)DSL.fields((String)"entity", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))));
        return map;
    }
}

