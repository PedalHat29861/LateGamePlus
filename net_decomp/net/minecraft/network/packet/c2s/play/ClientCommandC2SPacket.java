/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class ClientCommandC2SPacket
implements Packet<ServerPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, ClientCommandC2SPacket> CODEC = Packet.createCodec(ClientCommandC2SPacket::write, ClientCommandC2SPacket::new);
    private final int entityId;
    private final Mode mode;
    private final int mountJumpHeight;

    public ClientCommandC2SPacket(Entity entity, Mode mode) {
        this(entity, mode, 0);
    }

    public ClientCommandC2SPacket(Entity entity, Mode mode, int mountJumpHeight) {
        this.entityId = entity.getId();
        this.mode = mode;
        this.mountJumpHeight = mountJumpHeight;
    }

    private ClientCommandC2SPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.mode = buf.readEnumConstant(Mode.class);
        this.mountJumpHeight = buf.readVarInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeEnumConstant(this.mode);
        buf.writeVarInt(this.mountJumpHeight);
    }

    @Override
    public PacketType<ClientCommandC2SPacket> getPacketType() {
        return PlayPackets.PLAYER_COMMAND;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onClientCommand(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Mode getMode() {
        return this.mode;
    }

    public int getMountJumpHeight() {
        return this.mountJumpHeight;
    }

    public static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode STOP_SLEEPING = new Mode();
        public static final /* enum */ Mode START_SPRINTING = new Mode();
        public static final /* enum */ Mode STOP_SPRINTING = new Mode();
        public static final /* enum */ Mode START_RIDING_JUMP = new Mode();
        public static final /* enum */ Mode STOP_RIDING_JUMP = new Mode();
        public static final /* enum */ Mode OPEN_INVENTORY = new Mode();
        public static final /* enum */ Mode START_FALL_FLYING = new Mode();
        private static final /* synthetic */ Mode[] field_12983;

        public static Mode[] values() {
            return (Mode[])field_12983.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private static /* synthetic */ Mode[] method_36958() {
            return new Mode[]{STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, START_RIDING_JUMP, STOP_RIDING_JUMP, OPEN_INVENTORY, START_FALL_FLYING};
        }

        static {
            field_12983 = Mode.method_36958();
        }
    }
}

