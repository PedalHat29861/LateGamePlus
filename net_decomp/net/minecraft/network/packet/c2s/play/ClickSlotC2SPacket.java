/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.network.packet.c2s.play;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;

public record ClickSlotC2SPacket(int syncId, int revision, short slot, byte button, SlotActionType actionType, Int2ObjectMap<ItemStackHash> modifiedStacks, ItemStackHash cursor) implements Packet<ServerPlayPacketListener>
{
    private static final int MAX_MODIFIED_STACKS = 128;
    private static final PacketCodec<RegistryByteBuf, Int2ObjectMap<ItemStackHash>> STACK_MAP_CODEC = PacketCodecs.map(Int2ObjectOpenHashMap::new, PacketCodecs.SHORT.xmap(Short::intValue, Integer::shortValue), ItemStackHash.PACKET_CODEC, 128);
    public static final PacketCodec<RegistryByteBuf, ClickSlotC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.SYNC_ID, ClickSlotC2SPacket::syncId, PacketCodecs.VAR_INT, ClickSlotC2SPacket::revision, PacketCodecs.SHORT, ClickSlotC2SPacket::slot, PacketCodecs.BYTE, ClickSlotC2SPacket::button, SlotActionType.PACKET_CODEC, ClickSlotC2SPacket::actionType, STACK_MAP_CODEC, ClickSlotC2SPacket::modifiedStacks, ItemStackHash.PACKET_CODEC, ClickSlotC2SPacket::cursor, ClickSlotC2SPacket::new);

    public ClickSlotC2SPacket {
        modifiedStacks = Int2ObjectMaps.unmodifiable(modifiedStacks);
    }

    @Override
    public PacketType<ClickSlotC2SPacket> getPacketType() {
        return PlayPackets.CONTAINER_CLICK;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onClickSlot(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ClickSlotC2SPacket.class, "containerId;stateId;slotNum;buttonNum;clickType;changedSlots;carriedItem", "syncId", "revision", "slot", "button", "actionType", "modifiedStacks", "cursor"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ClickSlotC2SPacket.class, "containerId;stateId;slotNum;buttonNum;clickType;changedSlots;carriedItem", "syncId", "revision", "slot", "button", "actionType", "modifiedStacks", "cursor"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ClickSlotC2SPacket.class, "containerId;stateId;slotNum;buttonNum;clickType;changedSlots;carriedItem", "syncId", "revision", "slot", "button", "actionType", "modifiedStacks", "cursor"}, this, object);
    }
}

