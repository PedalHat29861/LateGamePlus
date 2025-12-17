/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.datafixer.schema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import org.slf4j.Logger;

public class Schema99
extends Schema {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Map<String, String> BLOCKS_TO_BLOCK_ENTITIES = (Map)DataFixUtils.make((Object)Maps.newHashMap(), map -> {
        map.put("minecraft:furnace", "Furnace");
        map.put("minecraft:lit_furnace", "Furnace");
        map.put("minecraft:chest", "Chest");
        map.put("minecraft:trapped_chest", "Chest");
        map.put("minecraft:ender_chest", "EnderChest");
        map.put("minecraft:jukebox", "RecordPlayer");
        map.put("minecraft:dispenser", "Trap");
        map.put("minecraft:dropper", "Dropper");
        map.put("minecraft:sign", "Sign");
        map.put("minecraft:mob_spawner", "MobSpawner");
        map.put("minecraft:noteblock", "Music");
        map.put("minecraft:brewing_stand", "Cauldron");
        map.put("minecraft:enhanting_table", "EnchantTable");
        map.put("minecraft:command_block", "CommandBlock");
        map.put("minecraft:beacon", "Beacon");
        map.put("minecraft:skull", "Skull");
        map.put("minecraft:daylight_detector", "DLDetector");
        map.put("minecraft:hopper", "Hopper");
        map.put("minecraft:banner", "Banner");
        map.put("minecraft:flower_pot", "FlowerPot");
        map.put("minecraft:repeating_command_block", "CommandBlock");
        map.put("minecraft:chain_command_block", "CommandBlock");
        map.put("minecraft:standing_sign", "Sign");
        map.put("minecraft:wall_sign", "Sign");
        map.put("minecraft:piston_head", "Piston");
        map.put("minecraft:daylight_detector_inverted", "DLDetector");
        map.put("minecraft:unpowered_comparator", "Comparator");
        map.put("minecraft:powered_comparator", "Comparator");
        map.put("minecraft:wall_banner", "Banner");
        map.put("minecraft:standing_banner", "Banner");
        map.put("minecraft:structure_block", "Structure");
        map.put("minecraft:end_portal", "Airportal");
        map.put("minecraft:end_gateway", "EndGateway");
        map.put("minecraft:shield", "Banner");
    });
    public static final Map<String, String> field_49718 = Map.of("minecraft:armor_stand", "ArmorStand", "minecraft:painting", "Painting");
    protected static final Hook.HookFunction field_5747 = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> ops, T value) {
            return Schema99.updateBlockEntityTags(new Dynamic(ops, value), BLOCKS_TO_BLOCK_ENTITIES, field_49718);
        }
    };

    public Schema99(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    protected static void targetInTile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> DSL.optionalFields((String)"inTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)));
    }

    protected static void targetDisplayTile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)));
    }

    protected static void targetItems(Schema schema, Map<String, Supplier<TypeTemplate>> map, String entityId) {
        schema.register(map, entityId, () -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap map = Maps.newHashMap();
        schema.register((Map)map, "Item", name -> DSL.optionalFields((String)"Item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)map, "XPOrb");
        Schema99.targetInTile(schema, map, "ThrownEgg");
        schema.registerSimple((Map)map, "LeashKnot");
        schema.registerSimple((Map)map, "Painting");
        schema.register((Map)map, "Arrow", name -> DSL.optionalFields((String)"inTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)));
        schema.register((Map)map, "TippedArrow", name -> DSL.optionalFields((String)"inTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)));
        schema.register((Map)map, "SpectralArrow", name -> DSL.optionalFields((String)"inTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)));
        Schema99.targetInTile(schema, map, "Snowball");
        Schema99.targetInTile(schema, map, "Fireball");
        Schema99.targetInTile(schema, map, "SmallFireball");
        Schema99.targetInTile(schema, map, "ThrownEnderpearl");
        schema.registerSimple((Map)map, "EyeOfEnderSignal");
        schema.register((Map)map, "ThrownPotion", name -> DSL.optionalFields((String)"inTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (String)"Potion", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema99.targetInTile(schema, map, "ThrownExpBottle");
        schema.register((Map)map, "ItemFrame", name -> DSL.optionalFields((String)"Item", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema99.targetInTile(schema, map, "WitherSkull");
        schema.registerSimple((Map)map, "PrimedTnt");
        schema.register((Map)map, "FallingSand", name -> DSL.optionalFields((String)"Block", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (String)"TileEntityData", (TypeTemplate)TypeReferences.BLOCK_ENTITY.in(schema)));
        schema.register((Map)map, "FireworksRocketEntity", name -> DSL.optionalFields((String)"FireworksItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)map, "Boat");
        schema.register((Map)map, "Minecart", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        Schema99.targetDisplayTile(schema, map, "MinecartRideable");
        schema.register((Map)map, "MinecartChest", name -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        Schema99.targetDisplayTile(schema, map, "MinecartFurnace");
        Schema99.targetDisplayTile(schema, map, "MinecartTNT");
        schema.register((Map)map, "MinecartSpawner", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (TypeTemplate)TypeReferences.UNTAGGED_SPAWNER.in(schema)));
        schema.register((Map)map, "MinecartHopper", name -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        schema.register((Map)map, "MinecartCommandBlock", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema), (String)"LastOutput", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)map, "ArmorStand");
        schema.registerSimple((Map)map, "Creeper");
        schema.registerSimple((Map)map, "Skeleton");
        schema.registerSimple((Map)map, "Spider");
        schema.registerSimple((Map)map, "Giant");
        schema.registerSimple((Map)map, "Zombie");
        schema.registerSimple((Map)map, "Slime");
        schema.registerSimple((Map)map, "Ghast");
        schema.registerSimple((Map)map, "PigZombie");
        schema.register((Map)map, "Enderman", name -> DSL.optionalFields((String)"carried", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema)));
        schema.registerSimple((Map)map, "CaveSpider");
        schema.registerSimple((Map)map, "Silverfish");
        schema.registerSimple((Map)map, "Blaze");
        schema.registerSimple((Map)map, "LavaSlime");
        schema.registerSimple((Map)map, "EnderDragon");
        schema.registerSimple((Map)map, "WitherBoss");
        schema.registerSimple((Map)map, "Bat");
        schema.registerSimple((Map)map, "Witch");
        schema.registerSimple((Map)map, "Endermite");
        schema.registerSimple((Map)map, "Guardian");
        schema.registerSimple((Map)map, "Pig");
        schema.registerSimple((Map)map, "Sheep");
        schema.registerSimple((Map)map, "Cow");
        schema.registerSimple((Map)map, "Chicken");
        schema.registerSimple((Map)map, "Squid");
        schema.registerSimple((Map)map, "Wolf");
        schema.registerSimple((Map)map, "MushroomCow");
        schema.registerSimple((Map)map, "SnowMan");
        schema.registerSimple((Map)map, "Ozelot");
        schema.registerSimple((Map)map, "VillagerGolem");
        schema.register((Map)map, "EntityHorse", name -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"ArmorItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"SaddleItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerSimple((Map)map, "Rabbit");
        schema.register((Map)map, "Villager", name -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.VILLAGER_TRADE.in(schema)))));
        schema.registerSimple((Map)map, "EnderCrystal");
        schema.register((Map)map, "AreaEffectCloud", string -> DSL.optionalFields((String)"Particle", (TypeTemplate)TypeReferences.PARTICLE.in(schema)));
        schema.registerSimple((Map)map, "ShulkerBullet");
        schema.registerSimple((Map)map, "DragonFireball");
        schema.registerSimple((Map)map, "Shulker");
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap map = Maps.newHashMap();
        Schema99.targetItems(schema, map, "Furnace");
        Schema99.targetItems(schema, map, "Chest");
        schema.registerSimple((Map)map, "EnderChest");
        schema.register((Map)map, "RecordPlayer", name -> DSL.optionalFields((String)"RecordItem", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        Schema99.targetItems(schema, map, "Trap");
        Schema99.targetItems(schema, map, "Dropper");
        schema.register((Map)map, "Sign", () -> Schema99.method_66194(schema));
        schema.register((Map)map, "MobSpawner", name -> TypeReferences.UNTAGGED_SPAWNER.in(schema));
        schema.registerSimple((Map)map, "Music");
        schema.registerSimple((Map)map, "Piston");
        Schema99.targetItems(schema, map, "Cauldron");
        schema.registerSimple((Map)map, "EnchantTable");
        schema.registerSimple((Map)map, "Airportal");
        schema.register((Map)map, "Control", () -> DSL.optionalFields((String)"LastOutput", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)map, "Beacon");
        schema.register((Map)map, "Skull", () -> DSL.optionalFields((String)"custom_name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)map, "DLDetector");
        Schema99.targetItems(schema, map, "Hopper");
        schema.registerSimple((Map)map, "Comparator");
        schema.register((Map)map, "FlowerPot", name -> DSL.optionalFields((String)"Item", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)TypeReferences.ITEM_NAME.in(schema))));
        schema.register((Map)map, "Banner", () -> DSL.optionalFields((String)"CustomName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerSimple((Map)map, "Structure");
        schema.registerSimple((Map)map, "EndGateway");
        return map;
    }

    public static TypeTemplate method_66194(Schema schema) {
        return DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"Text1", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"Text2", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"Text3", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"Text4", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText1", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText2", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText3", (Object)TypeReferences.TEXT_COMPONENT.in(schema)), Pair.of((Object)"FilteredText4", (Object)TypeReferences.TEXT_COMPONENT.in(schema))});
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        schema.registerType(false, TypeReferences.LEVEL, () -> DSL.optionalFields((String)"CustomBossEvents", (TypeTemplate)DSL.compoundList((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))), (TypeTemplate)TypeReferences.LIGHTWEIGHT_LEVEL.in(schema)));
        schema.registerType(false, TypeReferences.LIGHTWEIGHT_LEVEL, DSL::remainder);
        schema.registerType(false, TypeReferences.PLAYER, () -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)), (String)"EnderItems", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))));
        schema.registerType(false, TypeReferences.CHUNK, () -> DSL.fields((String)"Level", (TypeTemplate)DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema)), (String)"TileEntities", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)TypeReferences.BLOCK_ENTITY.in(schema), (TypeTemplate)DSL.remainder())), (String)"TileTicks", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"i", (TypeTemplate)TypeReferences.BLOCK_NAME.in(schema))))));
        schema.registerType(true, TypeReferences.BLOCK_ENTITY, () -> DSL.optionalFields((String)"components", (TypeTemplate)TypeReferences.DATA_COMPONENTS.in(schema), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", (Type)DSL.string(), (Map)blockEntityTypes)));
        schema.registerType(true, TypeReferences.ENTITY_TREE, () -> DSL.optionalFields((String)"Riding", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema), (TypeTemplate)TypeReferences.ENTITY.in(schema)));
        schema.registerType(false, TypeReferences.ENTITY_NAME, () -> DSL.constType(IdentifierNormalizingSchema.getIdentifierType()));
        schema.registerType(true, TypeReferences.ENTITY, () -> DSL.and((TypeTemplate)TypeReferences.ENTITY_EQUIPMENT.in(schema), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)DSL.constType((Type)DSL.string()), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", (Type)DSL.string(), (Map)entityTypes))));
        schema.registerType(true, TypeReferences.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)TypeReferences.ITEM_NAME.in(schema)), (String)"tag", (TypeTemplate)Schema99.method_66195(schema)), (Hook.HookFunction)field_5747, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
        schema.registerType(false, TypeReferences.OPTIONS, DSL::remainder);
        schema.registerType(false, TypeReferences.BLOCK_NAME, () -> DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)DSL.constType(IdentifierNormalizingSchema.getIdentifierType())));
        schema.registerType(false, TypeReferences.ITEM_NAME, () -> DSL.constType(IdentifierNormalizingSchema.getIdentifierType()));
        schema.registerType(false, TypeReferences.STATS, DSL::remainder);
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
        schema.registerType(false, TypeReferences.OBJECTIVE, DSL::remainder);
        schema.registerType(false, TypeReferences.TEAM, () -> DSL.optionalFields((String)"MemberNamePrefix", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"MemberNameSuffix", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"DisplayName", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema)));
        schema.registerType(true, TypeReferences.UNTAGGED_SPAWNER, DSL::remainder);
        schema.registerType(false, TypeReferences.POI_CHUNK, DSL::remainder);
        schema.registerType(false, TypeReferences.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, TypeReferences.ENTITY_CHUNK, () -> DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))));
        schema.registerType(true, TypeReferences.DATA_COMPONENTS, DSL::remainder);
        schema.registerType(true, TypeReferences.VILLAGER_TRADE, () -> DSL.optionalFields((String)"buy", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"buyB", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema), (String)"sell", (TypeTemplate)TypeReferences.ITEM_STACK.in(schema)));
        schema.registerType(true, TypeReferences.PARTICLE, () -> DSL.constType((Type)DSL.string()));
        schema.registerType(true, TypeReferences.TEXT_COMPONENT, () -> DSL.constType((Type)DSL.string()));
        schema.registerType(false, TypeReferences.STRUCTURE, () -> DSL.optionalFields((String)"entities", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)TypeReferences.ENTITY_TREE.in(schema))), (String)"blocks", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)TypeReferences.BLOCK_ENTITY.in(schema))), (String)"palette", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.BLOCK_STATE.in(schema))));
        schema.registerType(false, TypeReferences.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, TypeReferences.FLAT_BLOCK_STATE, DSL::remainder);
        schema.registerType(true, TypeReferences.ENTITY_EQUIPMENT, () -> DSL.optional((TypeTemplate)DSL.field((String)"Equipment", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema)))));
    }

    public static TypeTemplate method_66195(Schema schema) {
        return DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"EntityTag", (Object)TypeReferences.ENTITY_TREE.in(schema)), Pair.of((Object)"BlockEntityTag", (Object)TypeReferences.BLOCK_ENTITY.in(schema)), Pair.of((Object)"CanDestroy", (Object)DSL.list((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema))), Pair.of((Object)"CanPlaceOn", (Object)DSL.list((TypeTemplate)TypeReferences.BLOCK_NAME.in(schema))), Pair.of((Object)"Items", (Object)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), Pair.of((Object)"ChargedProjectiles", (Object)DSL.list((TypeTemplate)TypeReferences.ITEM_STACK.in(schema))), Pair.of((Object)"pages", (Object)DSL.list((TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))), Pair.of((Object)"filtered_pages", (Object)DSL.compoundList((TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))), Pair.of((Object)"display", (Object)DSL.optionalFields((String)"Name", (TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema), (String)"Lore", (TypeTemplate)DSL.list((TypeTemplate)TypeReferences.TEXT_COMPONENT.in(schema))))});
    }

    protected static <T> T updateBlockEntityTags(Dynamic<T> stack, Map<String, String> renames, Map<String, String> map) {
        return (T)stack.update("tag", tag -> tag.update("BlockEntityTag", blockEntityTag -> {
            String string = stack.get("id").asString().result().map(IdentifierNormalizingSchema::normalize).orElse("minecraft:air");
            if (!"minecraft:air".equals(string)) {
                String string2 = (String)renames.get(string);
                if (string2 == null) {
                    LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", (Object)string);
                } else {
                    return blockEntityTag.set("id", stack.createString(string2));
                }
            }
            return blockEntityTag;
        }).update("EntityTag", entityTag -> {
            if (entityTag.get("id").result().isPresent()) {
                return entityTag;
            }
            String string = IdentifierNormalizingSchema.normalize(stack.get("id").asString(""));
            String string2 = (String)map.get(string);
            if (string2 != null) {
                return entityTag.set("id", stack.createString(string2));
            }
            return entityTag;
        })).getValue();
    }
}

