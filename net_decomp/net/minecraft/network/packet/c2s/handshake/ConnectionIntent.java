/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.handshake;

public final class ConnectionIntent
extends Enum<ConnectionIntent> {
    public static final /* enum */ ConnectionIntent STATUS = new ConnectionIntent();
    public static final /* enum */ ConnectionIntent LOGIN = new ConnectionIntent();
    public static final /* enum */ ConnectionIntent TRANSFER = new ConnectionIntent();
    private static final int STATUS_ID = 1;
    private static final int LOGIN_ID = 2;
    private static final int TRANSFER_ID = 3;
    private static final /* synthetic */ ConnectionIntent[] field_44978;

    public static ConnectionIntent[] values() {
        return (ConnectionIntent[])field_44978.clone();
    }

    public static ConnectionIntent valueOf(String string) {
        return Enum.valueOf(ConnectionIntent.class, string);
    }

    public static ConnectionIntent byId(int id) {
        return switch (id) {
            case 1 -> STATUS;
            case 2 -> LOGIN;
            case 3 -> TRANSFER;
            default -> throw new IllegalArgumentException("Unknown connection intent: " + id);
        };
    }

    public int getId() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
        };
    }

    private static /* synthetic */ ConnectionIntent[] method_52286() {
        return new ConnectionIntent[]{STATUS, LOGIN, TRANSFER};
    }

    static {
        field_44978 = ConnectionIntent.method_52286();
    }
}

