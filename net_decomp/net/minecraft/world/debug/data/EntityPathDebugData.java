/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.data;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record EntityPathDebugData(Path path, float maxNodeDistance) {
    public static final PacketCodec<PacketByteBuf, EntityPathDebugData> PACKET_CODEC = PacketCodec.tuple(Path.PACKET_CODEC, EntityPathDebugData::path, PacketCodecs.FLOAT, EntityPathDebugData::maxNodeDistance, EntityPathDebugData::new);
}

