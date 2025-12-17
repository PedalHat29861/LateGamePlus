/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.Vec3d;

public abstract class PlayerMoveC2SPacket
implements Packet<ServerPlayPacketListener> {
    private static final int CHANGE_POSITION_FLAG = 1;
    private static final int CHANGE_LOOK_FLAG = 2;
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float yaw;
    protected final float pitch;
    protected final boolean onGround;
    protected final boolean horizontalCollision;
    protected final boolean changePosition;
    protected final boolean changeLook;

    static int toFlag(boolean changePosition, boolean changeLook) {
        int i = 0;
        if (changePosition) {
            i |= 1;
        }
        if (changeLook) {
            i |= 2;
        }
        return i;
    }

    static boolean changePosition(int flag) {
        return (flag & 1) != 0;
    }

    static boolean changeLook(int flag) {
        return (flag & 2) != 0;
    }

    protected PlayerMoveC2SPacket(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean horizontalCollision, boolean changePosition, boolean changeLook) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.horizontalCollision = horizontalCollision;
        this.changePosition = changePosition;
        this.changeLook = changeLook;
    }

    @Override
    public abstract PacketType<? extends PlayerMoveC2SPacket> getPacketType();

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onPlayerMove(this);
    }

    public double getX(double currentX) {
        return this.changePosition ? this.x : currentX;
    }

    public double getY(double currentY) {
        return this.changePosition ? this.y : currentY;
    }

    public double getZ(double currentZ) {
        return this.changePosition ? this.z : currentZ;
    }

    public float getYaw(float currentYaw) {
        return this.changeLook ? this.yaw : currentYaw;
    }

    public float getPitch(float currentPitch) {
        return this.changeLook ? this.pitch : currentPitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean horizontalCollision() {
        return this.horizontalCollision;
    }

    public boolean changesPosition() {
        return this.changePosition;
    }

    public boolean changesLook() {
        return this.changeLook;
    }

    public static class OnGroundOnly
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, OnGroundOnly> CODEC = Packet.createCodec(OnGroundOnly::write, OnGroundOnly::read);

        public OnGroundOnly(boolean onGround, boolean horizontalCollision) {
            super(0.0, 0.0, 0.0, 0.0f, 0.0f, onGround, horizontalCollision, false, false);
        }

        private static OnGroundOnly read(PacketByteBuf buf) {
            short s = buf.readUnsignedByte();
            boolean bl = PlayerMoveC2SPacket.changePosition(s);
            boolean bl2 = PlayerMoveC2SPacket.changeLook(s);
            return new OnGroundOnly(bl, bl2);
        }

        private void write(PacketByteBuf buf) {
            buf.writeByte(PlayerMoveC2SPacket.toFlag(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<OnGroundOnly> getPacketType() {
            return PlayPackets.MOVE_PLAYER_STATUS_ONLY;
        }
    }

    public static class LookAndOnGround
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, LookAndOnGround> CODEC = Packet.createCodec(LookAndOnGround::write, LookAndOnGround::read);

        public LookAndOnGround(float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
            super(0.0, 0.0, 0.0, yaw, pitch, onGround, horizontalCollision, false, true);
        }

        private static LookAndOnGround read(PacketByteBuf buf) {
            float f = buf.readFloat();
            float g = buf.readFloat();
            short s = buf.readUnsignedByte();
            boolean bl = PlayerMoveC2SPacket.changePosition(s);
            boolean bl2 = PlayerMoveC2SPacket.changeLook(s);
            return new LookAndOnGround(f, g, bl, bl2);
        }

        private void write(PacketByteBuf buf) {
            buf.writeFloat(this.yaw);
            buf.writeFloat(this.pitch);
            buf.writeByte(PlayerMoveC2SPacket.toFlag(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<LookAndOnGround> getPacketType() {
            return PlayPackets.MOVE_PLAYER_ROT;
        }
    }

    public static class PositionAndOnGround
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, PositionAndOnGround> CODEC = Packet.createCodec(PositionAndOnGround::write, PositionAndOnGround::read);

        public PositionAndOnGround(Vec3d pos, boolean onGround, boolean horizontalCollision) {
            super(pos.x, pos.y, pos.z, 0.0f, 0.0f, onGround, horizontalCollision, true, false);
        }

        public PositionAndOnGround(double x, double y, double z, boolean onGround, boolean horizontalCollision) {
            super(x, y, z, 0.0f, 0.0f, onGround, horizontalCollision, true, false);
        }

        private static PositionAndOnGround read(PacketByteBuf buf) {
            double d = buf.readDouble();
            double e = buf.readDouble();
            double f = buf.readDouble();
            short s = buf.readUnsignedByte();
            boolean bl = PlayerMoveC2SPacket.changePosition(s);
            boolean bl2 = PlayerMoveC2SPacket.changeLook(s);
            return new PositionAndOnGround(d, e, f, bl, bl2);
        }

        private void write(PacketByteBuf buf) {
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeByte(PlayerMoveC2SPacket.toFlag(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<PositionAndOnGround> getPacketType() {
            return PlayPackets.MOVE_PLAYER_POS;
        }
    }

    public static class Full
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, Full> CODEC = Packet.createCodec(Full::write, Full::read);

        public Full(Vec3d pos, float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
            super(pos.x, pos.y, pos.z, yaw, pitch, onGround, horizontalCollision, true, true);
        }

        public Full(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean horizontalCollision) {
            super(x, y, z, yaw, pitch, onGround, horizontalCollision, true, true);
        }

        private static Full read(PacketByteBuf buf) {
            double d = buf.readDouble();
            double e = buf.readDouble();
            double f = buf.readDouble();
            float g = buf.readFloat();
            float h = buf.readFloat();
            short s = buf.readUnsignedByte();
            boolean bl = PlayerMoveC2SPacket.changePosition(s);
            boolean bl2 = PlayerMoveC2SPacket.changeLook(s);
            return new Full(d, e, f, g, h, bl, bl2);
        }

        private void write(PacketByteBuf buf) {
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeFloat(this.yaw);
            buf.writeFloat(this.pitch);
            buf.writeByte(PlayerMoveC2SPacket.toFlag(this.onGround, this.horizontalCollision));
        }

        @Override
        public PacketType<Full> getPacketType() {
            return PlayPackets.MOVE_PLAYER_POS_ROT;
        }
    }
}

