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

public class Schema4312
extends IdentifierNormalizingSchema {
    public Schema4312(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(false, TypeReferences.PLAYER, () -> DSL.and((TypeTemplate)TypeReferences.ENTITY_EQUIPMENT.in(schema), (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"RootVehicle", (Object)DSL.optionalFields((String)"Entity", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), Pair.of((Object)"ender_pearls", (Object)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), Pair.of((Object)"Inventory", (Object)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), Pair.of((Object)"EnderItems", (Object)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), Pair.of((Object)"ShoulderEntityLeft", (Object)TypeReferences.ENTITY_TREE.in(schema)), Pair.of((Object)"ShoulderEntityRight", (Object)TypeReferences.ENTITY_TREE.in(schema)), Pair.of((Object)"recipeBook", (Object)DSL.optionalFields((String)"recipes", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.RECIPE.in(schema)), (String)"toBeDisplayed", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.RECIPE.in(schema))))})));
    }
}

