/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.DSL;

public class TypeReferences {
    public static final DSL.TypeReference LEVEL = TypeReferences.create("level");
    public static final DSL.TypeReference LIGHTWEIGHT_LEVEL = TypeReferences.create("lightweight_level");
    public static final DSL.TypeReference PLAYER = TypeReferences.create("player");
    public static final DSL.TypeReference CHUNK = TypeReferences.create("chunk");
    public static final DSL.TypeReference HOTBAR = TypeReferences.create("hotbar");
    public static final DSL.TypeReference OPTIONS = TypeReferences.create("options");
    public static final DSL.TypeReference STRUCTURE = TypeReferences.create("structure");
    public static final DSL.TypeReference STATS = TypeReferences.create("stats");
    public static final DSL.TypeReference SAVED_DATA_COMMAND_STORAGE = TypeReferences.create("saved_data/command_storage");
    public static final DSL.TypeReference TICKETS_SAVED_DATA = TypeReferences.create("saved_data/tickets");
    public static final DSL.TypeReference SAVED_DATA_MAP_DATA = TypeReferences.create("saved_data/map_data");
    public static final DSL.TypeReference SAVED_DATA_IDCOUNTS = TypeReferences.create("saved_data/idcounts");
    public static final DSL.TypeReference SAVED_DATA_RAIDS = TypeReferences.create("saved_data/raids");
    public static final DSL.TypeReference SAVED_DATA_RANDOM_SEQUENCES = TypeReferences.create("saved_data/random_sequences");
    public static final DSL.TypeReference SAVED_DATA_SCOREBOARD = TypeReferences.create("saved_data/scoreboard");
    public static final DSL.TypeReference STOPWATCHES_SAVED_DATA = TypeReferences.create("saved_data/stopwatches");
    public static final DSL.TypeReference SAVED_DATA_STRUCTURE_FEATURE_INDICES = TypeReferences.create("saved_data/structure_feature_indices");
    public static final DSL.TypeReference WORLD_BORDER_SAVED_DATA = TypeReferences.create("saved_data/world_border");
    public static final DSL.TypeReference ADVANCEMENTS = TypeReferences.create("advancements");
    public static final DSL.TypeReference POI_CHUNK = TypeReferences.create("poi_chunk");
    public static final DSL.TypeReference ENTITY_CHUNK = TypeReferences.create("entity_chunk");
    public static final DSL.TypeReference DEBUG_PROFILE = TypeReferences.create("debug_profile");
    public static final DSL.TypeReference BLOCK_ENTITY = TypeReferences.create("block_entity");
    public static final DSL.TypeReference ITEM_STACK = TypeReferences.create("item_stack");
    public static final DSL.TypeReference BLOCK_STATE = TypeReferences.create("block_state");
    public static final DSL.TypeReference FLAT_BLOCK_STATE = TypeReferences.create("flat_block_state");
    public static final DSL.TypeReference DATA_COMPONENTS = TypeReferences.create("data_components");
    public static final DSL.TypeReference VILLAGER_TRADE = TypeReferences.create("villager_trade");
    public static final DSL.TypeReference PARTICLE = TypeReferences.create("particle");
    public static final DSL.TypeReference TEXT_COMPONENT = TypeReferences.create("text_component");
    public static final DSL.TypeReference ENTITY_EQUIPMENT = TypeReferences.create("entity_equipment");
    public static final DSL.TypeReference ENTITY_NAME = TypeReferences.create("entity_name");
    public static final DSL.TypeReference ENTITY_TREE = TypeReferences.create("entity_tree");
    public static final DSL.TypeReference ENTITY = TypeReferences.create("entity");
    public static final DSL.TypeReference BLOCK_NAME = TypeReferences.create("block_name");
    public static final DSL.TypeReference ITEM_NAME = TypeReferences.create("item_name");
    public static final DSL.TypeReference GAME_EVENT_NAME = TypeReferences.create("game_event_name");
    public static final DSL.TypeReference UNTAGGED_SPAWNER = TypeReferences.create("untagged_spawner");
    public static final DSL.TypeReference STRUCTURE_FEATURE = TypeReferences.create("structure_feature");
    public static final DSL.TypeReference OBJECTIVE = TypeReferences.create("objective");
    public static final DSL.TypeReference TEAM = TypeReferences.create("team");
    public static final DSL.TypeReference RECIPE = TypeReferences.create("recipe");
    public static final DSL.TypeReference BIOME = TypeReferences.create("biome");
    public static final DSL.TypeReference MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST = TypeReferences.create("multi_noise_biome_source_parameter_list");
    public static final DSL.TypeReference WORLD_GEN_SETTINGS = TypeReferences.create("world_gen_settings");

    public static DSL.TypeReference create(final String typeName) {
        return new DSL.TypeReference(){

            public String typeName() {
                return typeName;
            }

            public String toString() {
                return "@" + typeName;
            }
        };
    }
}

