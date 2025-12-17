/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class WireConnection
extends Enum<WireConnection>
implements StringIdentifiable {
    public static final /* enum */ WireConnection UP = new WireConnection("up");
    public static final /* enum */ WireConnection SIDE = new WireConnection("side");
    public static final /* enum */ WireConnection NONE = new WireConnection("none");
    private final String name;
    private static final /* synthetic */ WireConnection[] field_12688;

    public static WireConnection[] values() {
        return (WireConnection[])field_12688.clone();
    }

    public static WireConnection valueOf(String string) {
        return Enum.valueOf(WireConnection.class, string);
    }

    private WireConnection(String name) {
        this.name = name;
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this.name;
    }

    public boolean isConnected() {
        return this != NONE;
    }

    private static /* synthetic */ WireConnection[] method_36733() {
        return new WireConnection[]{UP, SIDE, NONE};
    }

    static {
        field_12688 = WireConnection.method_36733();
    }
}

