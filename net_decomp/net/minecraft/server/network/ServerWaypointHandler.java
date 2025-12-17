/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.google.common.collect.Table
 *  com.google.common.collect.Tables
 */
package net.minecraft.server.network;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.waypoint.ServerWaypoint;
import net.minecraft.world.waypoint.Waypoint;
import net.minecraft.world.waypoint.WaypointHandler;

public class ServerWaypointHandler
implements WaypointHandler<ServerWaypoint> {
    private final Set<ServerWaypoint> waypoints = new HashSet<ServerWaypoint>();
    private final Set<ServerPlayerEntity> players = new HashSet<ServerPlayerEntity>();
    private final Table<ServerPlayerEntity, ServerWaypoint, ServerWaypoint.WaypointTracker> trackers = HashBasedTable.create();

    @Override
    public void onTrack(ServerWaypoint serverWaypoint) {
        this.waypoints.add(serverWaypoint);
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            this.refreshTracking(serverPlayerEntity, serverWaypoint);
        }
    }

    @Override
    public void onUpdate(ServerWaypoint serverWaypoint) {
        if (!this.waypoints.contains(serverWaypoint)) {
            return;
        }
        Map map = Tables.transpose(this.trackers).row((Object)serverWaypoint);
        Sets.SetView setView = Sets.difference(this.players, map.keySet());
        for (Map.Entry entry : ImmutableSet.copyOf(map.entrySet())) {
            this.refreshTracking((ServerPlayerEntity)entry.getKey(), serverWaypoint, (ServerWaypoint.WaypointTracker)entry.getValue());
        }
        for (ServerPlayerEntity serverPlayerEntity : setView) {
            this.refreshTracking(serverPlayerEntity, serverWaypoint);
        }
    }

    @Override
    public void onUntrack(ServerWaypoint serverWaypoint) {
        this.trackers.column((Object)serverWaypoint).forEach((player, tracker) -> tracker.untrack());
        Tables.transpose(this.trackers).row((Object)serverWaypoint).clear();
        this.waypoints.remove(serverWaypoint);
    }

    public void addPlayer(ServerPlayerEntity player) {
        this.players.add(player);
        for (ServerWaypoint serverWaypoint : this.waypoints) {
            this.refreshTracking(player, serverWaypoint);
        }
        if (player.hasWaypoint()) {
            this.onTrack(player);
        }
    }

    public void updatePlayerPos(ServerPlayerEntity player) {
        Map map = this.trackers.row((Object)player);
        Sets.SetView setView = Sets.difference(this.waypoints, map.keySet());
        for (Map.Entry entry : ImmutableSet.copyOf(map.entrySet())) {
            this.refreshTracking(player, (ServerWaypoint)entry.getKey(), (ServerWaypoint.WaypointTracker)entry.getValue());
        }
        for (ServerWaypoint serverWaypoint : setView) {
            this.refreshTracking(player, serverWaypoint);
        }
    }

    public void removePlayer(ServerPlayerEntity player) {
        this.trackers.row((Object)player).values().removeIf(tracker -> {
            tracker.untrack();
            return true;
        });
        this.onUntrack(player);
        this.players.remove(player);
    }

    public void clear() {
        this.trackers.values().forEach(ServerWaypoint.WaypointTracker::untrack);
        this.trackers.clear();
    }

    public void refreshTracking(ServerWaypoint waypoint) {
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            this.refreshTracking(serverPlayerEntity, waypoint);
        }
    }

    public Set<ServerWaypoint> getWaypoints() {
        return this.waypoints;
    }

    private static boolean isLocatorBarEnabled(ServerPlayerEntity player) {
        return player.getEntityWorld().getGameRules().getValue(GameRules.LOCATOR_BAR);
    }

    private void refreshTracking(ServerPlayerEntity player, ServerWaypoint waypoint) {
        if (player == waypoint) {
            return;
        }
        if (!ServerWaypointHandler.isLocatorBarEnabled(player)) {
            return;
        }
        waypoint.createTracker(player).ifPresentOrElse(tracker -> {
            this.trackers.put((Object)player, (Object)waypoint, tracker);
            tracker.track();
        }, () -> {
            ServerWaypoint.WaypointTracker waypointTracker = (ServerWaypoint.WaypointTracker)this.trackers.remove((Object)player, (Object)waypoint);
            if (waypointTracker != null) {
                waypointTracker.untrack();
            }
        });
    }

    private void refreshTracking(ServerPlayerEntity player, ServerWaypoint waypoint, ServerWaypoint.WaypointTracker tracker) {
        if (player == waypoint) {
            return;
        }
        if (!ServerWaypointHandler.isLocatorBarEnabled(player)) {
            return;
        }
        if (!tracker.isInvalid()) {
            tracker.update();
            return;
        }
        waypoint.createTracker(player).ifPresentOrElse(newTracker -> {
            newTracker.track();
            this.trackers.put((Object)player, (Object)waypoint, newTracker);
        }, () -> {
            tracker.untrack();
            this.trackers.remove((Object)player, (Object)waypoint);
        });
    }

    @Override
    public /* synthetic */ void onUntrack(Waypoint waypoint) {
        this.onUntrack((ServerWaypoint)waypoint);
    }

    @Override
    public /* synthetic */ void onTrack(Waypoint waypoint) {
        this.onTrack((ServerWaypoint)waypoint);
    }
}

