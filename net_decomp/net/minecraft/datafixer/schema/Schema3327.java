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

public class Schema3327
extends IdentifierNormalizingSchema {
    public Schema3327(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:decorated_pot", () -> DSL.optionalFields((String)"shards", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_NAME.in(schema)), (String)"item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:suspicious_sand", () -> DSL.optionalFields((String)"item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        return map;
    }
}

