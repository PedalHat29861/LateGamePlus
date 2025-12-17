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

public class Schema1458
extends IdentifierNormalizingSchema {
    public Schema1458(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, TypeReferences.ENTITY, () -> DSL.and((TypeTemplate)TypeReferences.ENTITY_EQUIPMENT.in(schema), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", Schema1458.getIdentifierType(), (Map)map))));
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:beacon", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:banner", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:brewing_stand", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:chest", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:trapped_chest", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:dispenser", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:dropper", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:enchanting_table", () -> Schema1458.customName(schema));
        schema.register(map, "minecraft:furnace", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:hopper", () -> Schema1458.itemsAndCustomName(schema));
        schema.register(map, "minecraft:shulker_box", () -> Schema1458.itemsAndCustomName(schema));
        return map;
    }

    public static TypeTemplate itemsAndCustomName(Schema schema) {
        return DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"CustomName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema));
    }

    public static TypeTemplate customName(Schema schema) {
        return DSL.optionalFields((String)"CustomName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema));
    }
}

