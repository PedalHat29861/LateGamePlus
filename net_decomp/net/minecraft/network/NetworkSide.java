/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

public final class NetworkSide
extends Enum<NetworkSide> {
    public static final /* enum */ NetworkSide SERVERBOUND = new NetworkSide("serverbound");
    public static final /* enum */ NetworkSide CLIENTBOUND = new NetworkSide("clientbound");
    private final String name;
    private static final /* synthetic */ NetworkSide[] field_11940;

    public static NetworkSide[] values() {
        return (NetworkSide[])field_11940.clone();
    }

    public static NetworkSide valueOf(String string) {
        return Enum.valueOf(NetworkSide.class, string);
    }

    private NetworkSide(String name) {
        this.name = name;
    }

    public NetworkSide getOpposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }

    public String getName() {
        return this.name;
    }

    private static /* synthetic */ NetworkSide[] method_36947() {
        return new NetworkSide[]{SERVERBOUND, CLIENTBOUND};
    }

    static {
        field_11940 = NetworkSide.method_36947();
    }
}

