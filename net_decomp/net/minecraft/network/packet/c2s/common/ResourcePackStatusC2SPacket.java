/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.common;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public record ResourcePackStatusC2SPacket(UUID id, Status status) implements Packet<ServerCommonPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ResourcePackStatusC2SPacket> CODEC = Packet.createCodec(ResourcePackStatusC2SPacket::write, ResourcePackStatusC2SPacket::new);

    private ResourcePackStatusC2SPacket(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readEnumConstant(Status.class));
    }

    private void write(PacketByteBuf buf) {
        buf.writeUuid(this.id);
        buf.writeEnumConstant(this.status);
    }

    @Override
    public PacketType<ResourcePackStatusC2SPacket> getPacketType() {
        return CommonPackets.RESOURCE_PACK;
    }

    @Override
    public void apply(ServerCommonPacketListener serverCommonPacketListener) {
        serverCommonPacketListener.onResourcePackStatus(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResourcePackStatusC2SPacket.class, "id;action", "id", "status"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResourcePackStatusC2SPacket.class, "id;action", "id", "status"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResourcePackStatusC2SPacket.class, "id;action", "id", "status"}, this, object);
    }

    public static final class Status
    extends Enum<Status> {
        public static final /* enum */ Status SUCCESSFULLY_LOADED = new Status();
        public static final /* enum */ Status DECLINED = new Status();
        public static final /* enum */ Status FAILED_DOWNLOAD = new Status();
        public static final /* enum */ Status ACCEPTED = new Status();
        public static final /* enum */ Status DOWNLOADED = new Status();
        public static final /* enum */ Status INVALID_URL = new Status();
        public static final /* enum */ Status FAILED_RELOAD = new Status();
        public static final /* enum */ Status DISCARDED = new Status();
        private static final /* synthetic */ Status[] field_13019;

        public static Status[] values() {
            return (Status[])field_13019.clone();
        }

        public static Status valueOf(String string) {
            return Enum.valueOf(Status.class, string);
        }

        public boolean hasFinished() {
            return this != ACCEPTED && this != DOWNLOADED;
        }

        private static /* synthetic */ Status[] method_36961() {
            return new Status[]{SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED, DOWNLOADED, INVALID_URL, FAILED_RELOAD, DISCARDED};
        }

        static {
            field_13019 = Status.method_36961();
        }
    }
}

