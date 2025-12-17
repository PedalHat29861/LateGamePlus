/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record SlotChangedStateC2SPacket(int slotId, int screenHandlerId, boolean newState) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, SlotChangedStateC2SPacket> CODEC = Packet.createCodec(SlotChangedStateC2SPacket::write, SlotChangedStateC2SPacket::new);

    private SlotChangedStateC2SPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readSyncId(), buf.readBoolean());
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.slotId);
        buf.writeSyncId(this.screenHandlerId);
        buf.writeBoolean(this.newState);
    }

    @Override
    public PacketType<SlotChangedStateC2SPacket> getPacketType() {
        return PlayPackets.CONTAINER_SLOT_STATE_CHANGED;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onSlotChangedState(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{SlotChangedStateC2SPacket.class, "slotId;containerId;newState", "slotId", "screenHandlerId", "newState"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SlotChangedStateC2SPacket.class, "slotId;containerId;newState", "slotId", "screenHandlerId", "newState"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SlotChangedStateC2SPacket.class, "slotId;containerId;newState", "slotId", "screenHandlerId", "newState"}, this, object);
    }
}

