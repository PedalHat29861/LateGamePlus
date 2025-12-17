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

public class Schema3325
extends IdentifierNormalizingSchema {
    public Schema3325(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map map = super.registerEntities(schema);
        schema.register(map, "minecraft:item_display", string -> DSL.optionalFields((String)"item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:block_display", string -> DSL.optionalFields((String)"block_state", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        schema.register(map, "minecraft:text_display", () -> DSL.optionalFields((String)"text", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        return map;
    }
}

