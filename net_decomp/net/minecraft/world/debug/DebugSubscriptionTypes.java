/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug;

import java.util.List;
import net.minecraft.entity.EntityBlockIntersectionType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.debug.data.BeeDebugData;
import net.minecraft.world.debug.data.BeeHiveDebugData;
import net.minecraft.world.debug.data.BrainDebugData;
import net.minecraft.world.debug.data.BreezeDebugData;
import net.minecraft.world.debug.data.EntityPathDebugData;
import net.minecraft.world.debug.data.GameEventDebugData;
import net.minecraft.world.debug.data.GameEventListenerDebugData;
import net.minecraft.world.debug.data.GoalSelectorDebugData;
import net.minecraft.world.debug.data.PoiDebugData;
import net.minecraft.world.debug.data.StructureDebugData;

public class DebugSubscriptionTypes<T> {
    public static final DebugSubscriptionType<?> DEDICATED_SERVER_TICK_TIME = DebugSubscriptionTypes.register("dedicated_server_tick_time");
    public static final DebugSubscriptionType<BeeDebugData> BEES = DebugSubscriptionTypes.register("bees", BeeDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<BrainDebugData> BRAINS = DebugSubscriptionTypes.register("brains", BrainDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<BreezeDebugData> BREEZES = DebugSubscriptionTypes.register("breezes", BreezeDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<GoalSelectorDebugData> GOAL_SELECTORS = DebugSubscriptionTypes.register("goal_selectors", GoalSelectorDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<EntityPathDebugData> ENTITY_PATHS = DebugSubscriptionTypes.register("entity_paths", EntityPathDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<EntityBlockIntersectionType> ENTITY_BLOCK_INTERSECTIONS = DebugSubscriptionTypes.register("entity_block_intersections", EntityBlockIntersectionType.PACKET_CODEC, 100);
    public static final DebugSubscriptionType<BeeHiveDebugData> BEE_HIVES = DebugSubscriptionTypes.register("bee_hives", BeeHiveDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<PoiDebugData> POIS = DebugSubscriptionTypes.register("pois", PoiDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<WireOrientation> REDSTONE_WIRE_ORIENTATIONS = DebugSubscriptionTypes.register("redstone_wire_orientations", WireOrientation.PACKET_CODEC, 200);
    public static final DebugSubscriptionType<Unit> VILLAGE_SECTIONS = DebugSubscriptionTypes.register("village_sections", Unit.PACKET_CODEC);
    public static final DebugSubscriptionType<List<BlockPos>> RAIDS = DebugSubscriptionTypes.register("raids", BlockPos.PACKET_CODEC.collect(PacketCodecs.toList()));
    public static final DebugSubscriptionType<List<StructureDebugData>> STRUCTURES = DebugSubscriptionTypes.register("structures", StructureDebugData.PACKET_CODEC.collect(PacketCodecs.toList()));
    public static final DebugSubscriptionType<GameEventListenerDebugData> GAME_EVENT_LISTENERS = DebugSubscriptionTypes.register("game_event_listeners", GameEventListenerDebugData.PACKET_CODEC);
    public static final DebugSubscriptionType<BlockPos> NEIGHBOR_UPDATES = DebugSubscriptionTypes.register("neighbor_updates", BlockPos.PACKET_CODEC, 200);
    public static final DebugSubscriptionType<GameEventDebugData> GAME_EVENTS = DebugSubscriptionTypes.register("game_events", GameEventDebugData.PACKET_CODEC, 60);

    public static DebugSubscriptionType<?> registerAndGetDefault(Registry<DebugSubscriptionType<?>> registry) {
        return DEDICATED_SERVER_TICK_TIME;
    }

    private static DebugSubscriptionType<?> register(String id) {
        return Registry.register(Registries.DEBUG_SUBSCRIPTION, Identifier.ofVanilla(id), new DebugSubscriptionType(null));
    }

    private static <T> DebugSubscriptionType<T> register(String id, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
        return Registry.register(Registries.DEBUG_SUBSCRIPTION, Identifier.ofVanilla(id), new DebugSubscriptionType<T>(packetCodec));
    }

    private static <T> DebugSubscriptionType<T> register(String id, PacketCodec<? super RegistryByteBuf, T> packetCodec, int expiry) {
        return Registry.register(Registries.DEBUG_SUBSCRIPTION, Identifier.ofVanilla(id), new DebugSubscriptionType<T>(packetCodec, expiry));
    }
}

