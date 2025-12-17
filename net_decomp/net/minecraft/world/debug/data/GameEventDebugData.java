/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.data;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;

public record GameEventDebugData(RegistryEntry<GameEvent> event, Vec3d pos) {
    public static final PacketCodec<RegistryByteBuf, GameEventDebugData> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.registryEntry(RegistryKeys.GAME_EVENT), GameEventDebugData::event, Vec3d.PACKET_CODEC, GameEventDebugData::pos, GameEventDebugData::new);
}

