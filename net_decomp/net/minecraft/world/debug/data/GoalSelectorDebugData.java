/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.debug.data;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record GoalSelectorDebugData(List<Goal> goals) {
    public static final PacketCodec<ByteBuf, GoalSelectorDebugData> PACKET_CODEC = PacketCodec.tuple(Goal.PACKET_CODEC.collect(PacketCodecs.toList()), GoalSelectorDebugData::goals, GoalSelectorDebugData::new);

    public record Goal(int priority, boolean isRunning, String name) {
        public static final PacketCodec<ByteBuf, Goal> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, Goal::priority, PacketCodecs.BOOLEAN, Goal::isRunning, PacketCodecs.string(255), Goal::name, Goal::new);
    }
}

