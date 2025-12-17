/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;

public record ConnectedClientData(GameProfile gameProfile, int latency, SyncedClientOptions syncedOptions, boolean transferred) {
    public static ConnectedClientData createDefault(GameProfile profile, boolean bl) {
        return new ConnectedClientData(profile, 0, SyncedClientOptions.createDefault(), bl);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ConnectedClientData.class, "gameProfile;latency;clientInformation;transferred", "gameProfile", "latency", "syncedOptions", "transferred"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ConnectedClientData.class, "gameProfile;latency;clientInformation;transferred", "gameProfile", "latency", "syncedOptions", "transferred"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ConnectedClientData.class, "gameProfile;latency;clientInformation;transferred", "gameProfile", "latency", "syncedOptions", "transferred"}, this, object);
    }
}

