/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class EntityS2CPacket
implements Packet<ClientPlayPacketListener> {
    protected final int id;
    protected final short deltaX;
    protected final short deltaY;
    protected final short deltaZ;
    protected final byte yaw;
    protected final byte pitch;
    protected final boolean onGround;
    protected final boolean rotate;
    protected final boolean positionChanged;

    protected EntityS2CPacket(int entityId, short deltaX, short deltaY, short deltaZ, byte yaw, byte pitch, boolean onGround, boolean rotate, boolean positionChanged) {
        this.id = entityId;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.rotate = rotate;
        this.positionChanged = positionChanged;
    }

    @Override
    public abstract PacketType<? extends EntityS2CPacket> getPacketType();

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onEntity(this);
    }

    public String toString() {
        return "Entity_" + super.toString();
    }

    public @Nullable Entity getEntity(World world) {
        return world.getEntityById(this.id);
    }

    public short getDeltaX() {
        return this.deltaX;
    }

    public short getDeltaY() {
        return this.deltaY;
    }

    public short getDeltaZ() {
        return this.deltaZ;
    }

    public float getYaw() {
        return MathHelper.unpackDegrees(this.yaw);
    }

    public float getPitch() {
        return MathHelper.unpackDegrees(this.pitch);
    }

    public boolean hasRotation() {
        return this.rotate;
    }

    public boolean isPositionChanged() {
        return this.positionChanged;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public static class Rotate
    extends EntityS2CPacket {
        public static final PacketCodec<PacketByteBuf, Rotate> CODEC = Packet.createCodec(Rotate::write, Rotate::read);

        public Rotate(int entityId, byte yaw, byte pitch, boolean onGround) {
            super(entityId, (short)0, (short)0, (short)0, yaw, pitch, onGround, true, false);
        }

        private static Rotate read(PacketByteBuf buf) {
            int i = buf.readVarInt();
            byte b = buf.readByte();
            byte c = buf.readByte();
            boolean bl = buf.readBoolean();
            return new Rotate(i, b, c, bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeVarInt(this.id);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }

        @Override
        public PacketType<Rotate> getPacketType() {
            return PlayPackets.MOVE_ENTITY_ROT;
        }
    }

    public static class MoveRelative
    extends EntityS2CPacket {
        public static final PacketCodec<PacketByteBuf, MoveRelative> CODEC = Packet.createCodec(MoveRelative::write, MoveRelative::read);

        public MoveRelative(int entityId, short deltaX, short deltaY, short deltaZ, boolean onGround) {
            super(entityId, deltaX, deltaY, deltaZ, (byte)0, (byte)0, onGround, false, true);
        }

        private static MoveRelative read(PacketByteBuf buf) {
            int i = buf.readVarInt();
            short s = buf.readShort();
            short t = buf.readShort();
            short u = buf.readShort();
            boolean bl = buf.readBoolean();
            return new MoveRelative(i, s, t, u, bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeVarInt(this.id);
            buf.writeShort(this.deltaX);
            buf.writeShort(this.deltaY);
            buf.writeShort(this.deltaZ);
            buf.writeBoolean(this.onGround);
        }

        @Override
        public PacketType<MoveRelative> getPacketType() {
            return PlayPackets.MOVE_ENTITY_POS;
        }
    }

    public static class RotateAndMoveRelative
    extends EntityS2CPacket {
        public static final PacketCodec<PacketByteBuf, RotateAndMoveRelative> CODEC = Packet.createCodec(RotateAndMoveRelative::write, RotateAndMoveRelative::read);

        public RotateAndMoveRelative(int entityId, short deltaX, short deltaY, short deltaZ, byte yaw, byte pitch, boolean onGround) {
            super(entityId, deltaX, deltaY, deltaZ, yaw, pitch, onGround, true, true);
        }

        private static RotateAndMoveRelative read(PacketByteBuf buf) {
            int i = buf.readVarInt();
            short s = buf.readShort();
            short t = buf.readShort();
            short u = buf.readShort();
            byte b = buf.readByte();
            byte c = buf.readByte();
            boolean bl = buf.readBoolean();
            return new RotateAndMoveRelative(i, s, t, u, b, c, bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeVarInt(this.id);
            buf.writeShort(this.deltaX);
            buf.writeShort(this.deltaY);
            buf.writeShort(this.deltaZ);
            buf.writeByte(this.yaw);
            buf.writeByte(this.pitch);
            buf.writeBoolean(this.onGround);
        }

        @Override
        public PacketType<RotateAndMoveRelative> getPacketType() {
            return PlayPackets.MOVE_ENTITY_POS_ROT;
        }
    }
}

