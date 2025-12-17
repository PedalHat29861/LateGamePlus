/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  io.netty.util.ResourceLeakDetector
 *  io.netty.util.ResourceLeakDetector$Level
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.ResourceLeakDetector;
import java.time.Duration;
import net.minecraft.GameVersion;
import net.minecraft.MinecraftVersion;
import net.minecraft.command.TranslatableBuiltInExceptions;
import net.minecraft.util.annotation.SuppressLinter;
import net.minecraft.util.math.ChunkPos;
import org.jspecify.annotations.Nullable;

@SuppressLinter(reason="System.out needed before bootstrap")
public class SharedConstants {
    @Deprecated
    public static final boolean IS_DEVELOPMENT_VERSION = false;
    @Deprecated
    public static final int WORLD_VERSION = 4671;
    @Deprecated
    public static final String CURRENT_SERIES = "main";
    @Deprecated
    public static final int RELEASE_TARGET_PROTOCOL_VERSION = 774;
    @Deprecated
    public static final int field_29736 = 286;
    public static final int SNBT_TOO_OLD_THRESHOLD = 4650;
    private static final int field_29708 = 30;
    public static final boolean CRASH_ON_UNCAUGHT_THREAD_EXCEPTION = false;
    @Deprecated
    public static final int RESOURCE_PACK_VERSION = 75;
    @Deprecated
    public static final int field_61077 = 0;
    @Deprecated
    public static final int DATA_PACK_VERSION = 94;
    @Deprecated
    public static final int field_61078 = 1;
    public static final String field_62277 = "2.0.0";
    @Deprecated
    public static final int field_39963 = 1;
    public static final int field_39964 = 1;
    public static final String DATA_VERSION_KEY = "DataVersion";
    public static final String DEBUG_PREFIX = "MC_DEBUG_";
    public static final boolean DEBUG_ENABLED = SharedConstants.propertyIsSet(SharedConstants.getDebugPropertyName("ENABLED"));
    private static final boolean DEBUG_PRINT_PROPERTIES = SharedConstants.propertyIsSet(SharedConstants.getDebugPropertyName("PRINT_PROPERTIES"));
    public static final boolean field_29745 = false;
    public static final boolean field_33851 = false;
    public static final boolean OPEN_INCOMPATIBLE_WORLDS = SharedConstants.debugProperty("OPEN_INCOMPATIBLE_WORLDS");
    public static final boolean ALLOW_LOW_SIM_DISTANCE = SharedConstants.debugProperty("ALLOW_LOW_SIM_DISTANCE");
    public static final boolean HOTKEYS = SharedConstants.debugProperty("HOTKEYS");
    public static final boolean UI_NARRATION = SharedConstants.debugProperty("UI_NARRATION");
    public static final boolean SHUFFLE_UI_RENDERING_ORDER = SharedConstants.debugProperty("SHUFFLE_UI_RENDERING_ORDER");
    public static final boolean SHUFFLE_MODELS = SharedConstants.debugProperty("SHUFFLE_MODELS");
    public static final boolean RENDER_UI_LAYERING_RECTANGLES = SharedConstants.debugProperty("RENDER_UI_LAYERING_RECTANGLES");
    public static final boolean PATHFINDING = SharedConstants.debugProperty("PATHFINDING");
    public static final boolean SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES = SharedConstants.debugProperty("SHOW_LOCAL_SERVER_ENTITY_HIT_BOXES");
    public static final boolean SHAPES = SharedConstants.debugProperty("SHAPES");
    public static final boolean NEIGHBORSUPDATE = SharedConstants.debugProperty("NEIGHBORSUPDATE");
    public static final boolean EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER = SharedConstants.debugProperty("EXPERIMENTAL_REDSTONEWIRE_UPDATE_ORDER");
    public static final boolean STRUCTURES = SharedConstants.debugProperty("STRUCTURES");
    public static final boolean GAME_EVENT_LISTENERS = SharedConstants.debugProperty("GAME_EVENT_LISTENERS");
    public static final boolean DUMP_TEXTURE_ATLAS = SharedConstants.debugProperty("DUMP_TEXTURE_ATLAS");
    public static final boolean DUMP_INTERPOLATED_TEXTURE_FRAMES = SharedConstants.debugProperty("DUMP_INTERPOLATED_TEXTURE_FRAMES");
    public static final boolean STRUCTURE_EDIT_MODE = SharedConstants.debugProperty("STRUCTURE_EDIT_MODE");
    public static final boolean SAVE_STRUCTURES_AS_SNBT = SharedConstants.debugProperty("SAVE_STRUCTURES_AS_SNBT");
    public static final boolean SYNCHRONOUS_GL_LOGS = SharedConstants.debugProperty("SYNCHRONOUS_GL_LOGS");
    public static final boolean VERBOSE_SERVER_EVENTS = SharedConstants.debugProperty("VERBOSE_SERVER_EVENTS");
    public static final boolean NAMED_RUNNABLES = SharedConstants.debugProperty("NAMED_RUNNABLES");
    public static final boolean GOAL_SELECTOR = SharedConstants.debugProperty("GOAL_SELECTOR");
    public static final boolean VILLAGE_SECTIONS = SharedConstants.debugProperty("VILLAGE_SECTIONS");
    public static final boolean BRAIN = SharedConstants.debugProperty("BRAIN");
    public static final boolean POI = SharedConstants.debugProperty("POI");
    public static final boolean BEES = SharedConstants.debugProperty("BEES");
    public static final boolean RAIDS = SharedConstants.debugProperty("RAIDS");
    public static final boolean BLOCK_BREAK = SharedConstants.debugProperty("BLOCK_BREAK");
    public static final boolean MONITOR_TICK_TIMES = SharedConstants.debugProperty("MONITOR_TICK_TIMES");
    public static final boolean KEEP_JIGSAW_BLOCKS_DURING_STRUCTURE_GEN = SharedConstants.debugProperty("KEEP_JIGSAW_BLOCKS_DURING_STRUCTURE_GEN");
    public static final boolean DONT_SAVE_WORLD = SharedConstants.debugProperty("DONT_SAVE_WORLD");
    public static final boolean LARGE_DRIPSTONE = SharedConstants.debugProperty("LARGE_DRIPSTONE");
    public static final boolean CARVERS = SharedConstants.debugProperty("CARVERS");
    public static final boolean ORE_VEINS = SharedConstants.debugProperty("ORE_VEINS");
    public static final boolean SCULK_CATALYST = SharedConstants.debugProperty("SCULK_CATALYST");
    public static final boolean BYPASS_REALMS_VERSION_CHECK = SharedConstants.debugProperty("BYPASS_REALMS_VERSION_CHECK");
    public static final boolean SOCIAL_INTERACTIONS = SharedConstants.debugProperty("SOCIAL_INTERACTIONS");
    public static final boolean VALIDATE_RESOURCE_PATH_CASE = SharedConstants.debugProperty("VALIDATE_RESOURCE_PATH_CASE");
    public static final boolean UNLOCK_ALL_TRADES = SharedConstants.debugProperty("UNLOCK_ALL_TRADES");
    public static final boolean BREEZE_MOB = SharedConstants.debugProperty("BREEZE_MOB");
    public static final boolean TRIAL_SPAWNER_DETECTS_SHEEP_AS_PLAYERS = SharedConstants.debugProperty("TRIAL_SPAWNER_DETECTS_SHEEP_AS_PLAYERS");
    public static final boolean VAULT_DETECTS_SHEEP_AS_PLAYERS = SharedConstants.debugProperty("VAULT_DETECTS_SHEEP_AS_PLAYERS");
    public static final boolean FORCE_ONBOARDING_SCREEN = SharedConstants.debugProperty("FORCE_ONBOARDING_SCREEN");
    public static final boolean CURSOR_POS = SharedConstants.debugProperty("CURSOR_POS");
    public static final boolean DEFAULT_SKIN_OVERRIDE = SharedConstants.debugProperty("DEFAULT_SKIN_OVERRIDE");
    public static final boolean PANORAMA_SCREENSHOT = SharedConstants.debugProperty("PANORAMA_SCREENSHOT");
    public static final boolean CHASE_COMMAND = SharedConstants.debugProperty("CHASE_COMMAND");
    public static final boolean VERBOSE_COMMAND_ERRORS = SharedConstants.debugProperty("VERBOSE_COMMAND_ERRORS");
    public static final boolean DEV_COMMANDS = SharedConstants.debugProperty("DEV_COMMANDS");
    public static final boolean ACTIVE_TEXT_AREAS = SharedConstants.debugProperty("ACTIVE_TEXT_AREAS");
    public static final boolean IGNORE_LOCAL_MOB_CAP = SharedConstants.debugProperty("IGNORE_LOCAL_MOB_CAP");
    public static final boolean DISABLE_LIQUID_SPREADING = SharedConstants.debugProperty("DISABLE_LIQUID_SPREADING");
    public static final boolean AQUIFERS = SharedConstants.debugProperty("AQUIFERS");
    public static final boolean JFR_PROFILING_ENABLE_LEVEL_LOADING = SharedConstants.debugProperty("JFR_PROFILING_ENABLE_LEVEL_LOADING");
    public static final boolean ENTITY_BLOCK_INTERSECTION = SharedConstants.debugProperty("ENTITY_BLOCK_INTERSECTION");
    public static boolean DEBUG_BIOME_SOURCE = SharedConstants.debugProperty("GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE");
    public static final boolean ONLY_GENERATE_HALF_THE_WORLD = SharedConstants.debugProperty("ONLY_GENERATE_HALF_THE_WORLD");
    public static final boolean DISABLE_FLUID_GENERATION = SharedConstants.debugProperty("DISABLE_FLUID_GENERATION");
    public static final boolean DISABLE_AQUIFERS = SharedConstants.debugProperty("DISABLE_AQUIFERS");
    public static final boolean DISABLE_SURFACE = SharedConstants.debugProperty("DISABLE_SURFACE");
    public static final boolean DISABLE_CARVERS = SharedConstants.debugProperty("DISABLE_CARVERS");
    public static final boolean DISABLE_STRUCTURES = SharedConstants.debugProperty("DISABLE_STRUCTURES");
    public static final boolean DISABLE_FEATURES = SharedConstants.debugProperty("DISABLE_FEATURES");
    public static final boolean DISABLE_ORE_VEINS = SharedConstants.debugProperty("DISABLE_ORE_VEINS");
    public static final boolean DISABLE_BLENDING = SharedConstants.debugProperty("DISABLE_BLENDING");
    public static final boolean DISABLE_BELOW_ZERO_RETROGENERATION = SharedConstants.debugProperty("DISABLE_BELOW_ZERO_RETROGENERATION");
    public static final int DEFAULT_PORT = 25565;
    public static final boolean SUBTITLES = SharedConstants.debugProperty("SUBTITLES");
    public static final int FAKE_LATENCY_MS = SharedConstants.debugIntProperty("FAKE_LATENCY_MS");
    public static final int FAKE_JITTER_MS = SharedConstants.debugIntProperty("FAKE_JITTER_MS");
    public static final ResourceLeakDetector.Level RESOURCE_LEAK_DETECTOR_DISABLED = ResourceLeakDetector.Level.DISABLED;
    public static final boolean COMMAND_STACK_TRACES = SharedConstants.debugProperty("COMMAND_STACK_TRACES");
    public static final boolean WORLD_RECREATE = SharedConstants.debugProperty("WORLD_RECREATE");
    public static final boolean SHOW_SERVER_DEBUG_VALUES = SharedConstants.debugProperty("SHOW_SERVER_DEBUG_VALUES");
    public static final boolean FEATURE_COUNT = SharedConstants.debugProperty("FEATURE_COUNT");
    public static final boolean FORCE_TELEMETRY = SharedConstants.debugProperty("FORCE_TELEMETRY");
    public static final boolean DONT_SEND_TELEMETRY_TO_BACKEND = SharedConstants.debugProperty("DONT_SEND_TELEMETRY_TO_BACKEND");
    public static final long field_22251 = Duration.ofMillis(300L).toNanos();
    public static final float field_49016 = 3600000.0f;
    public static final boolean field_44583 = false;
    public static final boolean field_49773 = false;
    public static boolean useChoiceTypeRegistrations = true;
    public static boolean isDevelopment;
    public static final int CHUNK_WIDTH = 16;
    public static final int DEFAULT_WORLD_HEIGHT = 256;
    public static final int COMMAND_MAX_LENGTH = 32500;
    public static final int EXPANDED_MACRO_COMMAND_MAX_LENGTH = 2000000;
    public static final int field_49170 = 16;
    public static final int field_38052 = 1000000;
    public static final int field_39898 = 32;
    public static final int field_64254 = 128;
    public static final char[] INVALID_CHARS_LEVEL_NAME;
    public static final int TICKS_PER_SECOND = 20;
    public static final int field_44973 = 50;
    public static final int TICKS_PER_MINUTE = 1200;
    public static final int TICKS_PER_IN_GAME_DAY = 24000;
    public static final int field_64255 = 3;
    public static final float field_29705 = 1365.3334f;
    public static final float field_29706 = 0.87890625f;
    public static final float field_29707 = 17.578125f;
    public static final int field_44922 = 64;
    private static @Nullable GameVersion gameVersion;

    private static String getDebugPropertyName(String name) {
        return DEBUG_PREFIX + name;
    }

    private static boolean propertyIsSet(String name) {
        String string = System.getProperty(name);
        return string != null && (string.isEmpty() || Boolean.parseBoolean(string));
    }

    private static boolean debugProperty(String name) {
        if (!DEBUG_ENABLED) {
            return false;
        }
        String string = SharedConstants.getDebugPropertyName(name);
        if (DEBUG_PRINT_PROPERTIES) {
            System.out.println("Debug property available: " + string + ": bool");
        }
        return SharedConstants.propertyIsSet(string);
    }

    private static int debugIntProperty(String name) {
        if (!DEBUG_ENABLED) {
            return 0;
        }
        String string = SharedConstants.getDebugPropertyName(name);
        if (DEBUG_PRINT_PROPERTIES) {
            System.out.println("Debug property available: " + string + ": int");
        }
        return Integer.parseInt(System.getProperty(string, "0"));
    }

    public static void setGameVersion(GameVersion gameVersion) {
        if (SharedConstants.gameVersion == null) {
            SharedConstants.gameVersion = gameVersion;
        } else if (gameVersion != SharedConstants.gameVersion) {
            throw new IllegalStateException("Cannot override the current game version!");
        }
    }

    public static void createGameVersion() {
        if (gameVersion == null) {
            gameVersion = MinecraftVersion.create();
        }
    }

    public static GameVersion getGameVersion() {
        if (gameVersion == null) {
            throw new IllegalStateException("Game version not set");
        }
        return gameVersion;
    }

    public static int getProtocolVersion() {
        return 774;
    }

    public static boolean isOutsideGenerationArea(ChunkPos pos) {
        int i = pos.getStartX();
        int j = pos.getStartZ();
        if (ONLY_GENERATE_HALF_THE_WORLD) {
            return j < 0;
        }
        if (DEBUG_BIOME_SOURCE) {
            return i > 8192 || i < 0 || j > 1024 || j < 0;
        }
        return false;
    }

    static {
        INVALID_CHARS_LEVEL_NAME = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
        ResourceLeakDetector.setLevel((ResourceLeakDetector.Level)RESOURCE_LEAK_DETECTOR_DISABLED);
        CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = COMMAND_STACK_TRACES;
        CommandSyntaxException.BUILT_IN_EXCEPTIONS = new TranslatableBuiltInExceptions();
    }
}

