/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.rule;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRuleVisitor;
import net.minecraft.world.rule.ServerGameRules;
import org.jspecify.annotations.Nullable;

public class GameRules {
    public static final GameRule<Boolean> ADVANCE_TIME = GameRules.registerBooleanRule("advance_time", GameRuleCategory.UPDATES, !SharedConstants.WORLD_RECREATE);
    public static final GameRule<Boolean> ADVANCE_WEATHER = GameRules.registerBooleanRule("advance_weather", GameRuleCategory.UPDATES, !SharedConstants.WORLD_RECREATE);
    public static final GameRule<Boolean> ALLOW_ENTERING_NETHER_USING_PORTALS = GameRules.registerBooleanRule("allow_entering_nether_using_portals", GameRuleCategory.MISC, true);
    public static final GameRule<Boolean> DO_TILE_DROPS = GameRules.registerBooleanRule("block_drops", GameRuleCategory.DROPS, true);
    public static final GameRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY = GameRules.registerBooleanRule("block_explosion_drop_decay", GameRuleCategory.DROPS, true);
    public static final GameRule<Boolean> COMMAND_BLOCKS_WORK = GameRules.registerBooleanRule("command_blocks_work", GameRuleCategory.MISC, true);
    public static final GameRule<Boolean> COMMAND_BLOCK_OUTPUT = GameRules.registerBooleanRule("command_block_output", GameRuleCategory.CHAT, true);
    public static final GameRule<Boolean> DROWNING_DAMAGE = GameRules.registerBooleanRule("drowning_damage", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> ELYTRA_MOVEMENT_CHECK = GameRules.registerBooleanRule("elytra_movement_check", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH = GameRules.registerBooleanRule("ender_pearls_vanish_on_death", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> ENTITY_DROPS = GameRules.registerBooleanRule("entity_drops", GameRuleCategory.DROPS, true);
    public static final GameRule<Boolean> FALL_DAMAGE = GameRules.registerBooleanRule("fall_damage", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> FIRE_DAMAGE = GameRules.registerBooleanRule("fire_damage", GameRuleCategory.PLAYER, true);
    public static final GameRule<Integer> FIRE_SPREAD_RADIUS_AROUND_PLAYER = GameRules.registerIntRule("fire_spread_radius_around_player", GameRuleCategory.UPDATES, 128, -1);
    public static final GameRule<Boolean> FORGIVE_DEAD_PLAYERS = GameRules.registerBooleanRule("forgive_dead_players", GameRuleCategory.MOBS, true);
    public static final GameRule<Boolean> FREEZE_DAMAGE = GameRules.registerBooleanRule("freeze_damage", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> GLOBAL_SOUND_EVENTS = GameRules.registerBooleanRule("global_sound_events", GameRuleCategory.MISC, true);
    public static final GameRule<Boolean> DO_IMMEDIATE_RESPAWN = GameRules.registerBooleanRule("immediate_respawn", GameRuleCategory.PLAYER, false);
    public static final GameRule<Boolean> KEEP_INVENTORY = GameRules.registerBooleanRule("keep_inventory", GameRuleCategory.PLAYER, false);
    public static final GameRule<Boolean> LAVA_SOURCE_CONVERSION = GameRules.registerBooleanRule("lava_source_conversion", GameRuleCategory.UPDATES, false);
    public static final GameRule<Boolean> LIMITED_CRAFTING = GameRules.registerBooleanRule("limited_crafting", GameRuleCategory.PLAYER, false);
    public static final GameRule<Boolean> LOCATOR_BAR = GameRules.registerBooleanRule("locator_bar", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> LOG_ADMIN_COMMANDS = GameRules.registerBooleanRule("log_admin_commands", GameRuleCategory.CHAT, true);
    public static final GameRule<Integer> MAX_BLOCK_MODIFICATIONS = GameRules.registerIntRule("max_block_modifications", GameRuleCategory.MISC, 32768, 1);
    public static final GameRule<Integer> MAX_COMMAND_FORKS = GameRules.registerIntRule("max_command_forks", GameRuleCategory.MISC, 65536, 0);
    public static final GameRule<Integer> MAX_COMMAND_SEQUENCE_LENGTH = GameRules.registerIntRule("max_command_sequence_length", GameRuleCategory.MISC, 65536, 0);
    public static final GameRule<Integer> MAX_ENTITY_CRAMMING = GameRules.registerIntRule("max_entity_cramming", GameRuleCategory.MOBS, 24, 0);
    public static final GameRule<Integer> MAX_MINECART_SPEED = GameRules.registerIntRule("max_minecart_speed", GameRuleCategory.MISC, 8, 1, 1000, FeatureSet.of(FeatureFlags.MINECART_IMPROVEMENTS));
    public static final GameRule<Integer> MAX_SNOW_ACCUMULATION_HEIGHT = GameRules.registerIntRule("max_snow_accumulation_height", GameRuleCategory.UPDATES, 1, 0, 8);
    public static final GameRule<Boolean> DO_MOB_LOOT = GameRules.registerBooleanRule("mob_drops", GameRuleCategory.DROPS, true);
    public static final GameRule<Boolean> MOB_EXPLOSION_DROP_DECAY = GameRules.registerBooleanRule("mob_explosion_drop_decay", GameRuleCategory.DROPS, true);
    public static final GameRule<Boolean> DO_MOB_GRIEFING = GameRules.registerBooleanRule("mob_griefing", GameRuleCategory.MOBS, true);
    public static final GameRule<Boolean> NATURAL_HEALTH_REGENERATION = GameRules.registerBooleanRule("natural_health_regeneration", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> PLAYER_MOVEMENT_CHECK = GameRules.registerBooleanRule("player_movement_check", GameRuleCategory.PLAYER, true);
    public static final GameRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = GameRules.registerIntRule("players_nether_portal_creative_delay", GameRuleCategory.PLAYER, 0, 0);
    public static final GameRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = GameRules.registerIntRule("players_nether_portal_default_delay", GameRuleCategory.PLAYER, 80, 0);
    public static final GameRule<Integer> PLAYERS_SLEEPING_PERCENTAGE = GameRules.registerIntRule("players_sleeping_percentage", GameRuleCategory.PLAYER, 100, 0);
    public static final GameRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS = GameRules.registerBooleanRule("projectiles_can_break_blocks", GameRuleCategory.DROPS, true);
    public static final GameRule<Boolean> PVP = GameRules.registerBooleanRule("pvp", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> DISABLE_RAIDS = GameRules.registerBooleanRule("raids", GameRuleCategory.MOBS, true);
    public static final GameRule<Integer> RANDOM_TICK_SPEED = GameRules.registerIntRule("random_tick_speed", GameRuleCategory.UPDATES, 3, 0);
    public static final GameRule<Boolean> REDUCED_DEBUG_INFO = GameRules.registerBooleanRule("reduced_debug_info", GameRuleCategory.MISC, false);
    public static final GameRule<Integer> RESPAWN_RADIUS = GameRules.registerIntRule("respawn_radius", GameRuleCategory.PLAYER, 10, 0);
    public static final GameRule<Boolean> SEND_COMMAND_FEEDBACK = GameRules.registerBooleanRule("send_command_feedback", GameRuleCategory.CHAT, true);
    public static final GameRule<Boolean> ANNOUNCE_ADVANCEMENTS = GameRules.registerBooleanRule("show_advancement_messages", GameRuleCategory.CHAT, true);
    public static final GameRule<Boolean> SHOW_DEATH_MESSAGES = GameRules.registerBooleanRule("show_death_messages", GameRuleCategory.CHAT, true);
    public static final GameRule<Boolean> SPAWNER_BLOCKS_WORK = GameRules.registerBooleanRule("spawner_blocks_work", GameRuleCategory.MISC, true);
    public static final GameRule<Boolean> DO_MOB_SPAWNING = GameRules.registerBooleanRule("spawn_mobs", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SPAWN_MONSTERS = GameRules.registerBooleanRule("spawn_monsters", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SPAWN_PATROLS = GameRules.registerBooleanRule("spawn_patrols", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SPAWN_PHANTOMS = GameRules.registerBooleanRule("spawn_phantoms", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SPAWN_WANDERING_TRADERS = GameRules.registerBooleanRule("spawn_wandering_traders", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SPAWN_WARDENS = GameRules.registerBooleanRule("spawn_wardens", GameRuleCategory.SPAWNING, true);
    public static final GameRule<Boolean> SPECTATORS_GENERATE_CHUNKS = GameRules.registerBooleanRule("spectators_generate_chunks", GameRuleCategory.PLAYER, true);
    public static final GameRule<Boolean> SPREAD_VINES = GameRules.registerBooleanRule("spread_vines", GameRuleCategory.UPDATES, true);
    public static final GameRule<Boolean> TNT_EXPLODES = GameRules.registerBooleanRule("tnt_explodes", GameRuleCategory.MISC, true);
    public static final GameRule<Boolean> TNT_EXPLOSION_DROP_DECAY = GameRules.registerBooleanRule("tnt_explosion_drop_decay", GameRuleCategory.DROPS, false);
    public static final GameRule<Boolean> UNIVERSAL_ANGER = GameRules.registerBooleanRule("universal_anger", GameRuleCategory.MOBS, false);
    public static final GameRule<Boolean> WATER_SOURCE_CONVERSION = GameRules.registerBooleanRule("water_source_conversion", GameRuleCategory.UPDATES, true);
    private final ServerGameRules rules;

    public static Codec<GameRules> createCodec(FeatureSet featureSet) {
        return ServerGameRules.CODEC.xmap(rules -> new GameRules(featureSet, (ServerGameRules)rules), gameRules -> gameRules.rules);
    }

    public GameRules(FeatureSet enabledFeatures, ServerGameRules rules) {
        this(enabledFeatures);
        this.rules.copyFrom(rules, this.rules::contains);
    }

    public GameRules(FeatureSet enabledFeatures) {
        this.rules = ServerGameRules.ofDefault(Registries.GAME_RULE.withFeatureFilter(enabledFeatures).streamEntries().map(RegistryEntry::value));
    }

    public Stream<GameRule<?>> streamRules() {
        return this.rules.keySet().stream();
    }

    public <T> T getValue(GameRule<T> rule) {
        T object = this.rules.get(rule);
        if (object == null) {
            throw new IllegalArgumentException("Tried to access invalid game rule");
        }
        return object;
    }

    public <T> void setValue(GameRule<T> rule, T value, @Nullable MinecraftServer server) {
        if (!this.rules.contains(rule)) {
            throw new IllegalArgumentException("Tried to set invalid game rule");
        }
        this.rules.put(rule, value);
        if (server != null) {
            server.onGameRuleUpdated(rule, value);
        }
    }

    public GameRules withEnabledFeatures(FeatureSet enabledFeatures) {
        return new GameRules(enabledFeatures, this.rules);
    }

    public void copyFrom(GameRules rules, @Nullable MinecraftServer server) {
        this.copyFrom(rules.rules, server);
    }

    public void copyFrom(ServerGameRules rules, @Nullable MinecraftServer server) {
        rules.keySet().forEach(rule -> this.copyFrom(rules, (GameRule)rule, server));
    }

    private <T> void copyFrom(ServerGameRules rules, GameRule<T> rule, @Nullable MinecraftServer server) {
        this.setValue(rule, Objects.requireNonNull(rules.get(rule)), server);
    }

    public void accept(GameRuleVisitor visitor) {
        this.rules.keySet().forEach(rule -> {
            visitor.visit(rule);
            rule.accept(visitor);
        });
    }

    private static GameRule<Boolean> registerBooleanRule(String name, GameRuleCategory category, boolean defaultValue) {
        return GameRules.register(name, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, FeatureSet.empty(), GameRuleVisitor::visitBoolean, value -> value != false ? 1 : 0);
    }

    private static GameRule<Integer> registerIntRule(String name, GameRuleCategory category, int defaultValue, int minValue) {
        return GameRules.registerIntRule(name, category, defaultValue, minValue, Integer.MAX_VALUE, FeatureSet.empty());
    }

    private static GameRule<Integer> registerIntRule(String name, GameRuleCategory category, int defaultValue, int minValue, int maxValue) {
        return GameRules.registerIntRule(name, category, defaultValue, minValue, maxValue, FeatureSet.empty());
    }

    private static GameRule<Integer> registerIntRule(String name, GameRuleCategory category, int defaultValue, int minValue, int maxValue, FeatureSet requiredFeatures) {
        return GameRules.register(name, category, GameRuleType.INT, IntegerArgumentType.integer((int)minValue, (int)maxValue), Codec.intRange((int)minValue, (int)maxValue), defaultValue, requiredFeatures, GameRuleVisitor::visitInt, value -> value);
    }

    private static <T> GameRule<T> register(String name, GameRuleCategory category, GameRuleType type, ArgumentType<T> argumentType, Codec<T> codec, T defaultValue, FeatureSet requiredFeatures, Acceptor<T> acceptor, ToIntFunction<T> commandResultSupplier) {
        return Registry.register(Registries.GAME_RULE, name, new GameRule<T>(category, type, argumentType, acceptor, codec, commandResultSupplier, defaultValue, requiredFeatures));
    }

    public static GameRule<?> registerAndGetDefault(Registry<GameRule<?>> registry) {
        return ADVANCE_TIME;
    }

    public <T> String getRuleValueName(GameRule<T> rule) {
        return rule.getValueName(this.getValue(rule));
    }

    public static interface Acceptor<T> {
        public void call(GameRuleVisitor var1, GameRule<T> var2);
    }
}

