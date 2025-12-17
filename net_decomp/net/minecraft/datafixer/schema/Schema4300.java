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

public class Schema4300
extends IdentifierNormalizingSchema {
    public Schema4300(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map map = super.registerEntities(schema);
        schema.register(map, "minecraft:llama", string -> Schema4300.method_66634(schema));
        schema.register(map, "minecraft:trader_llama", string -> Schema4300.method_66634(schema));
        schema.register(map, "minecraft:donkey", string -> Schema4300.method_66634(schema));
        schema.register(map, "minecraft:mule", string -> Schema4300.method_66634(schema));
        schema.registerSimple(map, "minecraft:horse");
        schema.registerSimple(map, "minecraft:skeleton_horse");
        schema.registerSimple(map, "minecraft:zombie_horse");
        return map;
    }

    private static TypeTemplate method_66634(Schema schema) {
        return DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
    }
}

