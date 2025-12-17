/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

public final class NetworkPhase
extends Enum<NetworkPhase> {
    public static final /* enum */ NetworkPhase HANDSHAKING = new NetworkPhase("handshake");
    public static final /* enum */ NetworkPhase PLAY = new NetworkPhase("play");
    public static final /* enum */ NetworkPhase STATUS = new NetworkPhase("status");
    public static final /* enum */ NetworkPhase LOGIN = new NetworkPhase("login");
    public static final /* enum */ NetworkPhase CONFIGURATION = new NetworkPhase("configuration");
    private final String id;
    private static final /* synthetic */ NetworkPhase[] field_11694;

    public static NetworkPhase[] values() {
        return (NetworkPhase[])field_11694.clone();
    }

    public static NetworkPhase valueOf(String string) {
        return Enum.valueOf(NetworkPhase.class, string);
    }

    private NetworkPhase(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    private static /* synthetic */ NetworkPhase[] method_36943() {
        return new NetworkPhase[]{HANDSHAKING, PLAY, STATUS, LOGIN, CONFIGURATION};
    }

    static {
        field_11694 = NetworkPhase.method_36943();
    }
}

