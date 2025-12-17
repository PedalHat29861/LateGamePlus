/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler;

public final class ServerTickType
extends Enum<ServerTickType> {
    public static final /* enum */ ServerTickType FULL_TICK = new ServerTickType();
    public static final /* enum */ ServerTickType TICK_SERVER_METHOD = new ServerTickType();
    public static final /* enum */ ServerTickType SCHEDULED_TASKS = new ServerTickType();
    public static final /* enum */ ServerTickType IDLE = new ServerTickType();
    private static final /* synthetic */ ServerTickType[] field_48722;

    public static ServerTickType[] values() {
        return (ServerTickType[])field_48722.clone();
    }

    public static ServerTickType valueOf(String string) {
        return Enum.valueOf(ServerTickType.class, string);
    }

    private static /* synthetic */ ServerTickType[] method_56536() {
        return new ServerTickType[]{FULL_TICK, TICK_SERVER_METHOD, SCHEDULED_TASKS, IDLE};
    }

    static {
        field_48722 = ServerTickType.method_56536();
    }
}

