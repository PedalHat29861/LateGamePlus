/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.waypoint;

import net.minecraft.world.waypoint.Waypoint;

public interface WaypointHandler<T extends Waypoint> {
    public void onTrack(T var1);

    public void onUpdate(T var1);

    public void onUntrack(T var1);
}

