/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record PlayerInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint) {
    private static final byte FORWARD = 1;
    private static final byte BACKWARD = 2;
    private static final byte LEFT = 4;
    private static final byte RIGHT = 8;
    private static final byte JUMP = 16;
    private static final byte SNEAK = 32;
    private static final byte SPRINT = 64;
    public static final PacketCodec<PacketByteBuf, PlayerInput> PACKET_CODEC = new PacketCodec<PacketByteBuf, PlayerInput>(){

        @Override
        public void encode(PacketByteBuf packetByteBuf, PlayerInput playerInput) {
            byte b = 0;
            b = (byte)(b | (playerInput.forward() ? 1 : 0));
            b = (byte)(b | (playerInput.backward() ? 2 : 0));
            b = (byte)(b | (playerInput.left() ? 4 : 0));
            b = (byte)(b | (playerInput.right() ? 8 : 0));
            b = (byte)(b | (playerInput.jump() ? 16 : 0));
            b = (byte)(b | (playerInput.sneak() ? 32 : 0));
            b = (byte)(b | (playerInput.sprint() ? 64 : 0));
            packetByteBuf.writeByte(b);
        }

        @Override
        public PlayerInput decode(PacketByteBuf packetByteBuf) {
            byte b = packetByteBuf.readByte();
            boolean bl = (b & 1) != 0;
            boolean bl2 = (b & 2) != 0;
            boolean bl3 = (b & 4) != 0;
            boolean bl4 = (b & 8) != 0;
            boolean bl5 = (b & 0x10) != 0;
            boolean bl6 = (b & 0x20) != 0;
            boolean bl7 = (b & 0x40) != 0;
            return new PlayerInput(bl, bl2, bl3, bl4, bl5, bl6, bl7);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((PacketByteBuf)((Object)object), (PlayerInput)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((PacketByteBuf)((Object)object));
        }
    };
    public static PlayerInput DEFAULT = new PlayerInput(false, false, false, false, false, false, false);

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PlayerInput.class, "forward;backward;left;right;jump;shift;sprint", "forward", "backward", "left", "right", "jump", "sneak", "sprint"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PlayerInput.class, "forward;backward;left;right;jump;shift;sprint", "forward", "backward", "left", "right", "jump", "sneak", "sprint"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PlayerInput.class, "forward;backward;left;right;jump;shift;sprint", "forward", "backward", "left", "right", "jump", "sneak", "sprint"}, this, object);
    }
}

