/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record CreativeInventoryActionC2SPacket(short slot, ItemStack stack) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, CreativeInventoryActionC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.SHORT, CreativeInventoryActionC2SPacket::slot, ItemStack.createExtraValidatingPacketCodec(ItemStack.LENGTH_PREPENDED_OPTIONAL_PACKET_CODEC), CreativeInventoryActionC2SPacket::stack, CreativeInventoryActionC2SPacket::new);

    public CreativeInventoryActionC2SPacket(int slot, ItemStack stack) {
        this((short)slot, stack);
    }

    @Override
    public PacketType<CreativeInventoryActionC2SPacket> getPacketType() {
        return PlayPackets.SET_CREATIVE_MODE_SLOT;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onCreativeInventoryAction(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CreativeInventoryActionC2SPacket.class, "slotNum;itemStack", "slot", "stack"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CreativeInventoryActionC2SPacket.class, "slotNum;itemStack", "slot", "stack"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CreativeInventoryActionC2SPacket.class, "slotNum;itemStack", "slot", "stack"}, this, object);
    }
}

