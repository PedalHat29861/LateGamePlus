/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.datafixer.schema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.datafixer.schema.Schema1451v6;
import net.minecraft.datafixer.schema.Schema1458;
import net.minecraft.datafixer.schema.Schema705;
import net.minecraft.datafixer.schema.Schema99;

public class Schema1460
extends IdentifierNormalizingSchema {
    public Schema1460(int i, Schema schema) {
        super(i, schema);
    }

    protected static void targetEntityItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.registerSimple(map, entityId);
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> Schema1458.itemsAndCustomName(schema));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap map = Maps.newHashMap();
        schema.register((Map)map, "minecraft:area_effect_cloud", string -> DSL.optionalFields((String)"Particle", (TypeTemplate)TypeReferences.PARTICLE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:armor_stand");
        schema.register((Map)map, "minecraft:arrow", name -> DSL.optionalFields((String)"inBlockState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:bat");
        Schema1460.targetEntityItems(schema, map, "minecraft:blaze");
        schema.registerSimple((Map)map, "minecraft:boat");
        Schema1460.targetEntityItems(schema, map, "minecraft:cave_spider");
        schema.register((Map)map, "minecraft:chest_minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        Schema1460.targetEntityItems(schema, map, "minecraft:chicken");
        schema.register((Map)map, "minecraft:commandblock_minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema), (String)"LastOutput", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:cow");
        Schema1460.targetEntityItems(schema, map, "minecraft:creeper");
        schema.register((Map)map, "minecraft:donkey", name -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)map, "minecraft:dragon_fireball");
        schema.registerSimple((Map)map, "minecraft:egg");
        Schema1460.targetEntityItems(schema, map, "minecraft:elder_guardian");
        schema.registerSimple((Map)map, "minecraft:ender_crystal");
        Schema1460.targetEntityItems(schema, map, "minecraft:ender_dragon");
        schema.register((Map)map, "minecraft:enderman", name -> DSL.optionalFields((String)"carriedBlockState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:endermite");
        schema.registerSimple((Map)map, "minecraft:ender_pearl");
        schema.registerSimple((Map)map, "minecraft:evocation_fangs");
        Schema1460.targetEntityItems(schema, map, "minecraft:evocation_illager");
        schema.registerSimple((Map)map, "minecraft:eye_of_ender_signal");
        schema.register((Map)map, "minecraft:falling_block", name -> DSL.optionalFields((String)"BlockState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema), (String)"TileEntityData", (TypeTemplate)TypeReferences.BLOCK_ENTITY.in(schema)));
        schema.registerSimple((Map)map, "minecraft:fireball");
        schema.register((Map)map, "minecraft:fireworks_rocket", name -> DSL.optionalFields((String)"FireworksItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.register((Map)map, "minecraft:furnace_minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:ghast");
        Schema1460.targetEntityItems(schema, map, "minecraft:giant");
        Schema1460.targetEntityItems(schema, map, "minecraft:guardian");
        schema.register((Map)map, "minecraft:hopper_minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        schema.register((Map)map, "minecraft:horse", string -> DSL.optionalFields((String)"ArmorItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:husk");
        Schema1460.targetEntityItems(schema, map, "minecraft:illusion_illager");
        schema.register((Map)map, "minecraft:item", name -> DSL.optionalFields((String)"Item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.register((Map)map, "minecraft:item_frame", name -> DSL.optionalFields((String)"Item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)map, "minecraft:leash_knot");
        schema.register((Map)map, "minecraft:llama", name -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"DecorItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)map, "minecraft:llama_spit");
        Schema1460.targetEntityItems(schema, map, "minecraft:magma_cube");
        schema.register((Map)map, "minecraft:minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:mooshroom");
        schema.register((Map)map, "minecraft:mule", name -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:ocelot");
        schema.registerSimple((Map)map, "minecraft:painting");
        Schema1460.targetEntityItems(schema, map, "minecraft:parrot");
        Schema1460.targetEntityItems(schema, map, "minecraft:pig");
        Schema1460.targetEntityItems(schema, map, "minecraft:polar_bear");
        schema.register((Map)map, "minecraft:potion", name -> DSL.optionalFields((String)"Potion", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:rabbit");
        Schema1460.targetEntityItems(schema, map, "minecraft:sheep");
        Schema1460.targetEntityItems(schema, map, "minecraft:shulker");
        schema.registerSimple((Map)map, "minecraft:shulker_bullet");
        Schema1460.targetEntityItems(schema, map, "minecraft:silverfish");
        Schema1460.targetEntityItems(schema, map, "minecraft:skeleton");
        schema.register((Map)map, "minecraft:skeleton_horse", string -> DSL.optionalFields((String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:slime");
        schema.registerSimple((Map)map, "minecraft:small_fireball");
        schema.registerSimple((Map)map, "minecraft:snowball");
        Schema1460.targetEntityItems(schema, map, "minecraft:snowman");
        schema.register((Map)map, "minecraft:spawner_minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema), (TypeTemplate)TypeReferences.UNTAGGED_SPAWNER.in(schema)));
        schema.register((Map)map, "minecraft:spectral_arrow", name -> DSL.optionalFields((String)"inBlockState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:spider");
        Schema1460.targetEntityItems(schema, map, "minecraft:squid");
        Schema1460.targetEntityItems(schema, map, "minecraft:stray");
        schema.registerSimple((Map)map, "minecraft:tnt");
        schema.register((Map)map, "minecraft:tnt_minecart", name -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:vex");
        schema.register((Map)map, "minecraft:villager", name -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.VILLAGER_TRADE.in(schema)))));
        Schema1460.targetEntityItems(schema, map, "minecraft:villager_golem");
        Schema1460.targetEntityItems(schema, map, "minecraft:vindication_illager");
        Schema1460.targetEntityItems(schema, map, "minecraft:witch");
        Schema1460.targetEntityItems(schema, map, "minecraft:wither");
        Schema1460.targetEntityItems(schema, map, "minecraft:wither_skeleton");
        schema.registerSimple((Map)map, "minecraft:wither_skull");
        Schema1460.targetEntityItems(schema, map, "minecraft:wolf");
        schema.registerSimple((Map)map, "minecraft:xp_bottle");
        schema.registerSimple((Map)map, "minecraft:xp_orb");
        Schema1460.targetEntityItems(schema, map, "minecraft:zombie");
        schema.register((Map)map, "minecraft:zombie_horse", string -> DSL.optionalFields((String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.targetEntityItems(schema, map, "minecraft:zombie_pigman");
        schema.register((Map)map, "minecraft:zombie_villager", string -> DSL.optionalFields((String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.VILLAGER_TRADE.in(schema)))));
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap map = Maps.newHashMap();
        Schema1460.registerInventory(schema, map, "minecraft:furnace");
        Schema1460.registerInventory(schema, map, "minecraft:chest");
        Schema1460.registerInventory(schema, map, "minecraft:trapped_chest");
        schema.registerSimple((Map)map, "minecraft:ender_chest");
        schema.register((Map)map, "minecraft:jukebox", name -> DSL.optionalFields((String)"RecordItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema1460.registerInventory(schema, map, "minecraft:dispenser");
        Schema1460.registerInventory(schema, map, "minecraft:dropper");
        schema.register((Map)map, "minecraft:sign", () -> Schema99.method_66194(schema));
        schema.register((Map)map, "minecraft:mob_spawner", name -> TypeReferences.UNTAGGED_SPAWNER.in(schema));
        schema.register((Map)map, "minecraft:piston", name -> DSL.optionalFields((String)"blockState", (TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)));
        Schema1460.registerInventory(schema, map, "minecraft:brewing_stand");
        schema.register((Map)map, "minecraft:enchanting_table", () -> Schema1458.customName(schema));
        schema.registerSimple((Map)map, "minecraft:end_portal");
        schema.register((Map)map, "minecraft:beacon", () -> Schema1458.customName(schema));
        schema.register((Map)map, "minecraft:skull", () -> DSL.optionalFields((String)"custom_name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)map, "minecraft:daylight_detector");
        Schema1460.registerInventory(schema, map, "minecraft:hopper");
        schema.registerSimple((Map)map, "minecraft:comparator");
        schema.register((Map)map, "minecraft:banner", () -> Schema1458.customName(schema));
        schema.registerSimple((Map)map, "minecraft:structure_block");
        schema.registerSimple((Map)map, "minecraft:end_gateway");
        schema.register((Map)map, "minecraft:command_block", () -> DSL.optionalFields((String)"LastOutput", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        Schema1460.registerInventory(schema, map, "minecraft:shulker_box");
        schema.registerSimple((Map)map, "minecraft:bed");
        return map;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        schema.registerType(false, TypeReferences.LEVEL, () -> DSL.optionalFields((String)"CustomBossEvents", (TypeTemplate)DSL.compoundList((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))), (TypeTemplate)TypeReferences.LIGHTWEIGHT_LEVEL.in(schema)));
        schema.registerType(false, TypeReferences.LIGHTWEIGHT_LEVEL, DSL::remainder);
        schema.registerType(false, TypeReferences.RECIPE, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.PLAYER, () -> DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"RootVehicle", (Object)DSL.optionalFields((String)"Entity", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), Pair.of((Object)"ender_pearls", (Object)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), Pair.of((Object)"Inventory", (Object)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), Pair.of((Object)"EnderItems", (Object)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), Pair.of((Object)"ShoulderEntityLeft", (Object)TypeReferences.ENTITY_TREE.in(schema)), Pair.of((Object)"ShoulderEntityRight", (Object)TypeReferences.ENTITY_TREE.in(schema)), Pair.of((Object)"recipeBook", (Object)DSL.optionalFields((String)"recipes", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.RECIPE.in(schema)), (String)"toBeDisplayed", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.RECIPE.in(schema))))}));
        schema.registerType(false, TypeReferences.CHUNK, () -> DSL.fields((String)"Level", (TypeTemplate)DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema)), (String)"TileEntities", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)TypeReferences.BLOCK_ENTITY.in(schema), (TypeTemplate)DSL.remainder())), (String)"TileTicks", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"i", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema))), (String)"Sections", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"Palette", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.BLOCK_STATE.in(schema)))))));
        schema.registerType(true, TypeReferences.BLOCK_ENTITY, () -> DSL.optionalFields((String)"components", (TypeTemplate)TypeReferences.DATA_COMPONENTS.in(schema), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", Schema1460.getIdentifierType(), (Map)blockEntityTypes)));
        schema.registerType(true, TypeReferences.ENTITY_TREE, () -> DSL.optionalFields((String)"Passengers", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema)), (TypeTemplate)TypeReferences.ENTITY.in(schema)));
        schema.registerType(true, TypeReferences.ENTITY, () -> DSL.and((TypeTemplate)TypeReferences.ENTITY_EQUIPMENT.in(schema), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", Schema1460.getIdentifierType(), (Map)entityTypes))));
        schema.registerType(true, TypeReferences.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)TypeReferences.ITEM_NAME.in(schema), (String)"tag", (TypeTemplate)Schema99.method_66195(schema)), (Hook.HookFunction)Schema705.field_5746, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
        schema.registerType(false, TypeReferences.HOTBAR, () -> DSL.compoundList((TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        schema.registerType(false, TypeReferences.OPTIONS, DSL::remainder);
        schema.registerType(false, TypeReferences.STRUCTURE, () -> DSL.optionalFields((String)"entities", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), (String)"blocks", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)TypeReferences.BLOCK_ENTITY.in(schema))), (String)"palette", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.BLOCK_STATE.in(schema))));
        schema.registerType(false, TypeReferences.BLOCK_NAME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.ITEM_NAME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, TypeReferences.FLAT_BLOCK_STATE, DSL::remainder);
        Supplier<TypeTemplate> supplier = () -> DSL.compoundList((TypeTemplate)TypeReferences.ITEM_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()));
        schema.registerType(false, TypeReferences.STATS, () -> DSL.optionalFields((String)"stats", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"minecraft:mined", (Object)DSL.compoundList((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:crafted", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:used", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:broken", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:picked_up", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:dropped", (Object)((TypeTemplate)supplier.get())), Pair.of((Object)"minecraft:killed", (Object)DSL.compoundList((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:killed_by", (Object)DSL.compoundList((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:custom", (Object)DSL.compoundList((TypeTemplate)DSL.constType(Schema1460.getIdentifierType()), (TypeTemplate)DSL.constType((Type)DSL.intType())))})));
        schema.registerType(false, TypeReferences.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
        schema.registerType(false, TypeReferences.TICKETS_SAVED_DATA, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_MAP_DATA, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"banners", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, TypeReferences.SAVED_DATA_IDCOUNTS, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_RAIDS, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_SCOREBOARD, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"Objectives", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.OBJECTIVE.in(schema)), (String)"Teams", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.TEAM.in(schema)), (String)"PlayerScores", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"display", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, TypeReferences.STOPWATCHES_SAVED_DATA, DSL::remainder);
        schema.registerType(false, TypeReferences.SAVED_DATA_STRUCTURE_FEATURE_INDICES, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"Features", (TypeTemplate)DSL.compoundList((TypeTemplate)TypeReferences.STRUCTURE_FEATURE.in(schema)))));
        schema.registerType(false, TypeReferences.WORLD_BORDER_SAVED_DATA, DSL::remainder);
        schema.registerType(false, TypeReferences.DEBUG_PROFILE, DSL::remainder);
        schema.registerType(false, TypeReferences.STRUCTURE_FEATURE, DSL::remainder);
        Map<String, Supplier<TypeTemplate>> map = Schema1451v6.method_37389(schema);
        schema.registerType(false, TypeReferences.OBJECTIVE, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"CriteriaType", (TypeTemplate)DSL.taggedChoiceLazy((String)"type", (Type)DSL.string(), (Map)map), (String)"DisplayName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)), (Hook.HookFunction)Schema1451v6.field_34014, (Hook.HookFunction)Schema1451v6.field_34015));
        schema.registerType(false, TypeReferences.TEAM, () -> DSL.optionalFields((String)"MemberNamePrefix", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"MemberNameSuffix", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"DisplayName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerType(true, TypeReferences.UNTAGGED_SPAWNER, () -> DSL.optionalFields((String)"SpawnPotentials", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"Entity", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), (String)"SpawnData", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema)));
        schema.registerType(false, TypeReferences.ADVANCEMENTS, () -> DSL.optionalFields((String)"minecraft:adventure/adventuring_time", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)TypeReferences.BIOME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.string()))), (String)"minecraft:adventure/kill_a_mob", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.string()))), (String)"minecraft:adventure/kill_all_mobs", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.string()))), (String)"minecraft:husbandry/bred_all_animals", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)TypeReferences.ENTITY_NAME.in(schema), (TypeTemplate)DSL.constType((Type)DSL.string())))));
        schema.registerType(false, TypeReferences.BIOME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.ENTITY_NAME, () -> DSL.constType(Schema1460.getIdentifierType()));
        schema.registerType(false, TypeReferences.POI_CHUNK, DSL::remainder);
        schema.registerType(false, TypeReferences.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, TypeReferences.ENTITY_CHUNK, () -> DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))));
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, DSL::remainder);
        schema.registerType(true, TypeReferences.VILLAGER_TRADE, () -> DSL.optionalFields((String)"buy", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"buyB", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"sell", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerType(true, TypeReferences.PARTICLE, () -> DSL.constType((Type)DSL.string()));
        schema.registerType(true, TypeReferences.TEXT_COMPONENT, () -> DSL.constType((Type)DSL.string()));
        schema.registerType(true, TypeReferences.ENTITY_EQUIPMENT, () -> DSL.and((TypeTemplate)DSL.optional((TypeTemplate)DSL.field((String)"ArmorItems", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))), (TypeTemplate[])new TypeTemplate[]{DSL.optional((TypeTemplate)DSL.field((String)"HandItems", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))), DSL.optional((TypeTemplate)DSL.field((String)"body_armor_item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), DSL.optional((TypeTemplate)DSL.field((String)"saddle", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))}));
    }
}

