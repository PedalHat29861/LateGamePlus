/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

public interface QueryableServer {
    public String getServerMotd();

    public String getVersion();

    public int getCurrentPlayerCount();

    public int getMaxPlayerCount();
}

