/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;

public class Schema4301
extends IdentifierNormalizingSchema {
    public Schema4301(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.ENTITY_EQUIPMENT, () -> DSL.optional((TypeTemplate)DSL.field((String)"equipment", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"mainhand", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"offhand", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"feet", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"legs", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"chest", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"head", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"body", (Object)TypeReferences.ITEM_STACK.in(schema)), Pair.of((Object)"saddle", (Object)TypeReferences.ITEM_STACK.in(schema))}))));
    }
}

