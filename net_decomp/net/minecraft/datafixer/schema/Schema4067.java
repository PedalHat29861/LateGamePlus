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

public class Schema4067
extends IdentifierNormalizingSchema {
    public Schema4067(int i, Schema schema) {
        super(i, schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map map = super.registerEntities(schema);
        map.remove("minecraft:boat");
        map.remove("minecraft:chest_boat");
        this.registerSimple(map, "minecraft:oak_boat");
        this.registerSimple(map, "minecraft:spruce_boat");
        this.registerSimple(map, "minecraft:birch_boat");
        this.registerSimple(map, "minecraft:jungle_boat");
        this.registerSimple(map, "minecraft:acacia_boat");
        this.registerSimple(map, "minecraft:cherry_boat");
        this.registerSimple(map, "minecraft:dark_oak_boat");
        this.registerSimple(map, "minecraft:mangrove_boat");
        this.registerSimple(map, "minecraft:bamboo_raft");
        this.registerChestBoatFix(map, "minecraft:oak_chest_boat");
        this.registerChestBoatFix(map, "minecraft:spruce_chest_boat");
        this.registerChestBoatFix(map, "minecraft:birch_chest_boat");
        this.registerChestBoatFix(map, "minecraft:jungle_chest_boat");
        this.registerChestBoatFix(map, "minecraft:acacia_chest_boat");
        this.registerChestBoatFix(map, "minecraft:cherry_chest_boat");
        this.registerChestBoatFix(map, "minecraft:dark_oak_chest_boat");
        this.registerChestBoatFix(map, "minecraft:mangrove_chest_boat");
        this.registerChestBoatFix(map, "minecraft:bamboo_chest_raft");
        return map;
    }

    private void registerChestBoatFix(Map<String, Supplier<TypeTemplate>> map, String entityId) {
        this.register(map, entityId, string -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in((Schema)this))));
    }
}

