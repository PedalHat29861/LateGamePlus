/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreResetS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.jspecify.annotations.Nullable;

public class ServerScoreboard
extends Scoreboard {
    private final MinecraftServer server;
    private final Set<ScoreboardObjective> syncableObjectives = Sets.newHashSet();
    private boolean dirty;

    public ServerScoreboard(MinecraftServer server) {
        this.server = server;
    }

    public void read(ScoreboardState.Packed packed) {
        packed.objectives().forEach(objective -> this.addObjective((ScoreboardObjective.Packed)objective));
        packed.scores().forEach(score -> this.addEntry((Scoreboard.PackedEntry)score));
        packed.displaySlots().forEach((slot, objective) -> {
            ScoreboardObjective scoreboardObjective = this.getNullableObjective((String)objective);
            this.setObjectiveSlot((ScoreboardDisplaySlot)slot, scoreboardObjective);
        });
        packed.teams().forEach(team -> this.addTeam((Team.Packed)team));
    }

    private ScoreboardState.Packed toPacked() {
        return new ScoreboardState.Packed(this.getPackedObjectives(), this.pack(), this.getObjectivesBySlots(), this.getPackedTeams());
    }

    @Override
    protected void updateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score) {
        super.updateScore(scoreHolder, objective, score);
        if (this.syncableObjectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardScoreUpdateS2CPacket(scoreHolder.getNameForScoreboard(), objective.getName(), score.getScore(), Optional.ofNullable(score.getDisplayText()), Optional.ofNullable(score.getNumberFormat())));
        }
        this.markDirty();
    }

    @Override
    protected void resetScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        super.resetScore(scoreHolder, objective);
        this.markDirty();
    }

    @Override
    public void onScoreHolderRemoved(ScoreHolder scoreHolder) {
        super.onScoreHolderRemoved(scoreHolder);
        this.server.getPlayerManager().sendToAll(new ScoreboardScoreResetS2CPacket(scoreHolder.getNameForScoreboard(), null));
        this.markDirty();
    }

    @Override
    public void onScoreRemoved(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        super.onScoreRemoved(scoreHolder, objective);
        if (this.syncableObjectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardScoreResetS2CPacket(scoreHolder.getNameForScoreboard(), objective.getName()));
        }
        this.markDirty();
    }

    @Override
    public void setObjectiveSlot(ScoreboardDisplaySlot slot, @Nullable ScoreboardObjective objective) {
        ScoreboardObjective scoreboardObjective = this.getObjectiveForSlot(slot);
        super.setObjectiveSlot(slot, objective);
        if (scoreboardObjective != objective && scoreboardObjective != null) {
            if (this.countDisplaySlots(scoreboardObjective) > 0) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.stopSyncing(scoreboardObjective);
            }
        }
        if (objective != null) {
            if (this.syncableObjectives.contains(objective)) {
                this.server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
            } else {
                this.startSyncing(objective);
            }
        }
        this.markDirty();
    }

    @Override
    public boolean addScoreHolderToTeam(String scoreHolderName, Team team) {
        if (super.addScoreHolderToTeam(scoreHolderName, team)) {
            this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.ADD));
            this.refreshWaypointTrackingFor(scoreHolderName);
            this.markDirty();
            return true;
        }
        return false;
    }

    @Override
    public void removeScoreHolderFromTeam(String scoreHolderName, Team team) {
        super.removeScoreHolderFromTeam(scoreHolderName, team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, scoreHolderName, TeamS2CPacket.Operation.REMOVE));
        this.refreshWaypointTrackingFor(scoreHolderName);
        this.markDirty();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
        this.markDirty();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
        if (this.syncableObjectives.contains(objective)) {
            this.server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, 2));
        }
        this.markDirty();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
        if (this.syncableObjectives.contains(objective)) {
            this.stopSyncing(objective);
        }
        this.markDirty();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
        this.markDirty();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false));
        this.refreshWaypointTrackingFor(team);
        this.markDirty();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
        this.server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team));
        this.refreshWaypointTrackingFor(team);
        this.markDirty();
    }

    protected void markDirty() {
        this.dirty = true;
    }

    public void writeTo(ScoreboardState state) {
        if (this.dirty) {
            this.dirty = false;
            state.set(this.toPacked());
        }
    }

    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        ArrayList list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, 0));
        for (ScoreboardDisplaySlot scoreboardDisplaySlot : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(scoreboardDisplaySlot) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(scoreboardDisplaySlot, objective));
        }
        for (ScoreboardEntry scoreboardEntry : this.getScoreboardEntries(objective)) {
            list.add(new ScoreboardScoreUpdateS2CPacket(scoreboardEntry.owner(), objective.getName(), scoreboardEntry.value(), Optional.ofNullable(scoreboardEntry.display()), Optional.ofNullable(scoreboardEntry.numberFormatOverride())));
        }
        return list;
    }

    public void startSyncing(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createChangePackets(objective);
        for (ServerPlayerEntity serverPlayerEntity : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> packet : list) {
                serverPlayerEntity.networkHandler.sendPacket(packet);
            }
        }
        this.syncableObjectives.add(objective);
    }

    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        ArrayList list = Lists.newArrayList();
        list.add(new ScoreboardObjectiveUpdateS2CPacket(objective, 1));
        for (ScoreboardDisplaySlot scoreboardDisplaySlot : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(scoreboardDisplaySlot) != objective) continue;
            list.add(new ScoreboardDisplayS2CPacket(scoreboardDisplaySlot, objective));
        }
        return list;
    }

    public void stopSyncing(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createRemovePackets(objective);
        for (ServerPlayerEntity serverPlayerEntity : this.server.getPlayerManager().getPlayerList()) {
            for (Packet<?> packet : list) {
                serverPlayerEntity.networkHandler.sendPacket(packet);
            }
        }
        this.syncableObjectives.remove(objective);
    }

    public int countDisplaySlots(ScoreboardObjective objective) {
        int i = 0;
        for (ScoreboardDisplaySlot scoreboardDisplaySlot : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(scoreboardDisplaySlot) != objective) continue;
            ++i;
        }
        return i;
    }

    private void refreshWaypointTrackingFor(String playerName) {
        ServerPlayerEntity serverPlayerEntity = this.server.getPlayerManager().getPlayer(playerName);
        if (serverPlayerEntity != null) {
            serverPlayerEntity.getEntityWorld().getWaypointHandler().refreshTracking(serverPlayerEntity);
        }
    }

    private void refreshWaypointTrackingFor(Team team) {
        for (ServerWorld serverWorld : this.server.getWorlds()) {
            team.getPlayerList().stream().map(playerName -> this.server.getPlayerManager().getPlayer((String)playerName)).filter(Objects::nonNull).forEach(player -> serverWorld.getWaypointHandler().refreshTracking((ServerWaypoint)player));
        }
    }
}

