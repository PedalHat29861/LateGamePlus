/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public final class PositionFlag
extends Enum<PositionFlag> {
    public static final /* enum */ PositionFlag X = new PositionFlag(0);
    public static final /* enum */ PositionFlag Y = new PositionFlag(1);
    public static final /* enum */ PositionFlag Z = new PositionFlag(2);
    public static final /* enum */ PositionFlag Y_ROT = new PositionFlag(3);
    public static final /* enum */ PositionFlag X_ROT = new PositionFlag(4);
    public static final /* enum */ PositionFlag DELTA_X = new PositionFlag(5);
    public static final /* enum */ PositionFlag DELTA_Y = new PositionFlag(6);
    public static final /* enum */ PositionFlag DELTA_Z = new PositionFlag(7);
    public static final /* enum */ PositionFlag ROTATE_DELTA = new PositionFlag(8);
    public static final Set<PositionFlag> VALUES;
    public static final Set<PositionFlag> ROT;
    public static final Set<PositionFlag> DELTA;
    public static final PacketCodec<ByteBuf, Set<PositionFlag>> PACKET_CODEC;
    private final int shift;
    private static final /* synthetic */ PositionFlag[] field_12402;

    public static PositionFlag[] values() {
        return (PositionFlag[])field_12402.clone();
    }

    public static PositionFlag valueOf(String string) {
        return Enum.valueOf(PositionFlag.class, string);
    }

    @SafeVarargs
    public static Set<PositionFlag> combine(Set<PositionFlag> ... sets) {
        HashSet<PositionFlag> hashSet = new HashSet<PositionFlag>();
        for (Set<PositionFlag> set : sets) {
            hashSet.addAll(set);
        }
        return hashSet;
    }

    public static Set<PositionFlag> ofRot(boolean yRot, boolean xRot) {
        EnumSet<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
        if (yRot) {
            set.add(Y_ROT);
        }
        if (xRot) {
            set.add(X_ROT);
        }
        return set;
    }

    public static Set<PositionFlag> ofPos(boolean x, boolean y, boolean z) {
        EnumSet<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
        if (x) {
            set.add(X);
        }
        if (y) {
            set.add(Y);
        }
        if (z) {
            set.add(Z);
        }
        return set;
    }

    public static Set<PositionFlag> ofDeltaPos(boolean x, boolean y, boolean z) {
        EnumSet<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
        if (x) {
            set.add(DELTA_X);
        }
        if (y) {
            set.add(DELTA_Y);
        }
        if (z) {
            set.add(DELTA_Z);
        }
        return set;
    }

    private PositionFlag(int shift) {
        this.shift = shift;
    }

    private int getMask() {
        return 1 << this.shift;
    }

    private boolean isSet(int mask) {
        return (mask & this.getMask()) == this.getMask();
    }

    public static Set<PositionFlag> getFlags(int mask) {
        EnumSet<PositionFlag> set = EnumSet.noneOf(PositionFlag.class);
        for (PositionFlag positionFlag : PositionFlag.values()) {
            if (!positionFlag.isSet(mask)) continue;
            set.add(positionFlag);
        }
        return set;
    }

    public static int getBitfield(Set<PositionFlag> flags) {
        int i = 0;
        for (PositionFlag positionFlag : flags) {
            i |= positionFlag.getMask();
        }
        return i;
    }

    private static /* synthetic */ PositionFlag[] method_36952() {
        return new PositionFlag[]{X, Y, Z, Y_ROT, X_ROT, DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA};
    }

    static {
        field_12402 = PositionFlag.method_36952();
        VALUES = Set.of(PositionFlag.values());
        ROT = Set.of(X_ROT, Y_ROT);
        DELTA = Set.of(DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA);
        PACKET_CODEC = PacketCodecs.INTEGER.xmap(PositionFlag::getFlags, PositionFlag::getBitfield);
    }
}

