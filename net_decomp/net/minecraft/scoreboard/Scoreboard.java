/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import net.minecraft.scoreboard.Scores;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Scoreboard {
    public static final String field_47542 = "#";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<String, ScoreboardObjective> objectives = new Object2ObjectOpenHashMap(16, 0.5f);
    private final Reference2ObjectMap<ScoreboardCriterion, List<ScoreboardObjective>> objectivesByCriterion = new Reference2ObjectOpenHashMap();
    private final Map<String, Scores> scores = new Object2ObjectOpenHashMap(16, 0.5f);
    private final Map<ScoreboardDisplaySlot, ScoreboardObjective> objectiveSlots = new EnumMap<ScoreboardDisplaySlot, ScoreboardObjective>(ScoreboardDisplaySlot.class);
    private final Object2ObjectMap<String, Team> teams = new Object2ObjectOpenHashMap();
    private final Object2ObjectMap<String, Team> teamsByScoreHolder = new Object2ObjectOpenHashMap();

    public @Nullable ScoreboardObjective getNullableObjective(@Nullable String name) {
        return (ScoreboardObjective)this.objectives.get((Object)name);
    }

    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, @Nullable NumberFormat numberFormat) {
        if (this.objectives.containsKey((Object)name)) {
            throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
        }
        ScoreboardObjective scoreboardObjective = new ScoreboardObjective(this, name, criterion, displayName, renderType, displayAutoUpdate, numberFormat);
        ((List)this.objectivesByCriterion.computeIfAbsent((Object)criterion, criterion2 -> Lists.newArrayList())).add(scoreboardObjective);
        this.objectives.put((Object)name, (Object)scoreboardObjective);
        this.updateObjective(scoreboardObjective);
        return scoreboardObjective;
    }

    public final void forEachScore(ScoreboardCriterion criterion, ScoreHolder scoreHolder, Consumer<ScoreAccess> action) {
        ((List)this.objectivesByCriterion.getOrDefault((Object)criterion, Collections.emptyList())).forEach(objective -> action.accept(this.getOrCreateScore(scoreHolder, (ScoreboardObjective)objective, true)));
    }

    private Scores getScores(String scoreHolderName) {
        return this.scores.computeIfAbsent(scoreHolderName, name -> new Scores());
    }

    public ScoreAccess getOrCreateScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        return this.getOrCreateScore(scoreHolder, objective, false);
    }

    public ScoreAccess getOrCreateScore(final ScoreHolder scoreHolder, final ScoreboardObjective objective, boolean forceWritable) {
        final boolean bl = forceWritable || !objective.getCriterion().isReadOnly();
        Scores scores = this.getScores(scoreHolder.getNameForScoreboard());
        final MutableBoolean mutableBoolean = new MutableBoolean();
        final ScoreboardScore scoreboardScore = scores.getOrCreate(objective, score -> mutableBoolean.setTrue());
        return new ScoreAccess(){

            @Override
            public int getScore() {
                return scoreboardScore.getScore();
            }

            @Override
            public void setScore(int score) {
                Text text;
                if (!bl) {
                    throw new IllegalStateException("Cannot modify read-only score");
                }
                boolean bl2 = mutableBoolean.isTrue();
                if (objective.shouldDisplayAutoUpdate() && (text = scoreHolder.getDisplayName()) != null && !text.equals(scoreboardScore.getDisplayText())) {
                    scoreboardScore.setDisplayText(text);
                    bl2 = true;
                }
                if (score != scoreboardScore.getScore()) {
                    scoreboardScore.setScore(score);
                    bl2 = true;
                }
                if (bl2) {
                    this.update();
                }
            }

            @Override
            public @Nullable Text getDisplayText() {
                return scoreboardScore.getDisplayText();
            }

            @Override
            public void setDisplayText(@Nullable Text text) {
                if (mutableBoolean.isTrue() || !Objects.equals(text, scoreboardScore.getDisplayText())) {
                    scoreboardScore.setDisplayText(text);
                    this.update();
                }
            }

            @Override
            public void setNumberFormat(@Nullable NumberFormat numberFormat) {
                scoreboardScore.setNumberFormat(numberFormat);
                this.update();
            }

            @Override
            public boolean isLocked() {
                return scoreboardScore.isLocked();
            }

            @Override
            public void unlock() {
                this.setLocked(false);
            }

            @Override
            public void lock() {
                this.setLocked(true);
            }

            private void setLocked(boolean locked) {
                scoreboardScore.setLocked(locked);
                if (mutableBoolean.isTrue()) {
                    this.update();
                }
                Scoreboard.this.resetScore(scoreHolder, objective);
            }

            private void update() {
                Scoreboard.this.updateScore(scoreHolder, objective, scoreboardScore);
                mutableBoolean.setFalse();
            }
        };
    }

    public @Nullable ReadableScoreboardScore getScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        Scores scores = this.scores.get(scoreHolder.getNameForScoreboard());
        if (scores != null) {
            return scores.get(objective);
        }
        return null;
    }

    public Collection<ScoreboardEntry> getScoreboardEntries(ScoreboardObjective objective) {
        ArrayList<ScoreboardEntry> list = new ArrayList<ScoreboardEntry>();
        this.scores.forEach((scoreHolderName, scores) -> {
            ScoreboardScore scoreboardScore = scores.get(objective);
            if (scoreboardScore != null) {
                list.add(new ScoreboardEntry((String)scoreHolderName, scoreboardScore.getScore(), scoreboardScore.getDisplayText(), scoreboardScore.getNumberFormat()));
            }
        });
        return list;
    }

    public Collection<ScoreboardObjective> getObjectives() {
        return this.objectives.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectives.keySet();
    }

    public Collection<ScoreHolder> getKnownScoreHolders() {
        return this.scores.keySet().stream().map(ScoreHolder::fromName).toList();
    }

    public void removeScores(ScoreHolder scoreHolder) {
        Scores scores = this.scores.remove(scoreHolder.getNameForScoreboard());
        if (scores != null) {
            this.onScoreHolderRemoved(scoreHolder);
        }
    }

    public void removeScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
        Scores scores = this.scores.get(scoreHolder.getNameForScoreboard());
        if (scores != null) {
            boolean bl = scores.remove(objective);
            if (!scores.hasScores()) {
                Scores scores2 = this.scores.remove(scoreHolder.getNameForScoreboard());
                if (scores2 != null) {
                    this.onScoreHolderRemoved(scoreHolder);
                }
            } else if (bl) {
                this.onScoreRemoved(scoreHolder, objective);
            }
        }
    }

    public Object2IntMap<ScoreboardObjective> getScoreHolderObjectives(ScoreHolder scoreHolder) {
        Scores scores = this.scores.get(scoreHolder.getNameForScoreboard());
        return scores != null ? scores.getScoresAsIntMap() : Object2IntMaps.emptyMap();
    }

    public void removeObjective(ScoreboardObjective objective) {
        this.objectives.remove((Object)objective.getName());
        for (ScoreboardDisplaySlot scoreboardDisplaySlot : ScoreboardDisplaySlot.values()) {
            if (this.getObjectiveForSlot(scoreboardDisplaySlot) != objective) continue;
            this.setObjectiveSlot(scoreboardDisplaySlot, null);
        }
        List list = (List)this.objectivesByCriterion.get((Object)objective.getCriterion());
        if (list != null) {
            list.remove(objective);
        }
        for (Scores scores : this.scores.values()) {
            scores.remove(objective);
        }
        this.updateRemovedObjective(objective);
    }

    public void setObjectiveSlot(ScoreboardDisplaySlot slot, @Nullable ScoreboardObjective objective) {
        this.objectiveSlots.put(slot, objective);
    }

    public @Nullable ScoreboardObjective getObjectiveForSlot(ScoreboardDisplaySlot slot) {
        return this.objectiveSlots.get(slot);
    }

    public @Nullable Team getTeam(String name) {
        return (Team)this.teams.get((Object)name);
    }

    public Team addTeam(String name) {
        Team team = this.getTeam(name);
        if (team != null) {
            LOGGER.warn("Requested creation of existing team '{}'", (Object)name);
            return team;
        }
        team = new Team(this, name);
        this.teams.put((Object)name, (Object)team);
        this.updateScoreboardTeamAndPlayers(team);
        return team;
    }

    public void removeTeam(Team team) {
        this.teams.remove((Object)team.getName());
        for (String string : team.getPlayerList()) {
            this.teamsByScoreHolder.remove((Object)string);
        }
        this.updateRemovedTeam(team);
    }

    public boolean addScoreHolderToTeam(String scoreHolderName, Team team) {
        if (this.getScoreHolderTeam(scoreHolderName) != null) {
            this.clearTeam(scoreHolderName);
        }
        this.teamsByScoreHolder.put((Object)scoreHolderName, (Object)team);
        return team.getPlayerList().add(scoreHolderName);
    }

    public boolean clearTeam(String scoreHolderName) {
        Team team = this.getScoreHolderTeam(scoreHolderName);
        if (team != null) {
            this.removeScoreHolderFromTeam(scoreHolderName, team);
            return true;
        }
        return false;
    }

    public void removeScoreHolderFromTeam(String scoreHolderName, Team team) {
        if (this.getScoreHolderTeam(scoreHolderName) != team) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + team.getName() + "'.");
        }
        this.teamsByScoreHolder.remove((Object)scoreHolderName);
        team.getPlayerList().remove(scoreHolderName);
    }

    public Collection<String> getTeamNames() {
        return this.teams.keySet();
    }

    public Collection<Team> getTeams() {
        return this.teams.values();
    }

    public @Nullable Team getScoreHolderTeam(String scoreHolderName) {
        return (Team)this.teamsByScoreHolder.get((Object)scoreHolderName);
    }

    public void updateObjective(ScoreboardObjective objective) {
    }

    public void updateExistingObjective(ScoreboardObjective objective) {
    }

    public void updateRemovedObjective(ScoreboardObjective objective) {
    }

    protected void updateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score) {
    }

    protected void resetScore(ScoreHolder scoreHolder, ScoreboardObjective objective) {
    }

    public void onScoreHolderRemoved(ScoreHolder scoreHolder) {
    }

    public void onScoreRemoved(ScoreHolder scoreHolder, ScoreboardObjective objective) {
    }

    public void updateScoreboardTeamAndPlayers(Team team) {
    }

    public void updateScoreboardTeam(Team team) {
    }

    public void updateRemovedTeam(Team team) {
    }

    public void clearDeadEntity(Entity entity) {
        if (entity instanceof PlayerEntity || entity.isAlive()) {
            return;
        }
        this.removeScores(entity);
        this.clearTeam(entity.getNameForScoreboard());
    }

    protected List<PackedEntry> pack() {
        return this.scores.entrySet().stream().flatMap(entry -> {
            String string = (String)entry.getKey();
            return ((Scores)entry.getValue()).getScores().entrySet().stream().map(entryx -> new PackedEntry(string, ((ScoreboardObjective)entryx.getKey()).getName(), ((ScoreboardScore)entryx.getValue()).toPacked()));
        }).toList();
    }

    protected void addEntry(PackedEntry packedEntry) {
        ScoreboardObjective scoreboardObjective = this.getNullableObjective(packedEntry.objective);
        if (scoreboardObjective == null) {
            LOGGER.error("Unknown objective {} for name {}, ignoring", (Object)packedEntry.objective, (Object)packedEntry.owner);
            return;
        }
        this.getScores(packedEntry.owner).put(scoreboardObjective, new ScoreboardScore(packedEntry.score));
    }

    protected List<Team.Packed> getPackedTeams() {
        return this.getTeams().stream().map(Team::pack).toList();
    }

    protected void addTeam(Team.Packed packedTeam) {
        Team team = this.addTeam(packedTeam.name());
        packedTeam.displayName().ifPresent(team::setDisplayName);
        packedTeam.color().ifPresent(team::setColor);
        team.setFriendlyFireAllowed(packedTeam.allowFriendlyFire());
        team.setShowFriendlyInvisibles(packedTeam.seeFriendlyInvisibles());
        team.setPrefix(packedTeam.memberNamePrefix());
        team.setSuffix(packedTeam.memberNameSuffix());
        team.setNameTagVisibilityRule(packedTeam.nameTagVisibility());
        team.setDeathMessageVisibilityRule(packedTeam.deathMessageVisibility());
        team.setCollisionRule(packedTeam.collisionRule());
        for (String string : packedTeam.players()) {
            this.addScoreHolderToTeam(string, team);
        }
    }

    protected List<ScoreboardObjective.Packed> getPackedObjectives() {
        return this.getObjectives().stream().map(ScoreboardObjective::pack).toList();
    }

    protected void addObjective(ScoreboardObjective.Packed packedObjective) {
        this.addObjective(packedObjective.name(), packedObjective.criteria(), packedObjective.displayName(), packedObjective.renderType(), packedObjective.displayAutoUpdate(), packedObjective.numberFormat().orElse(null));
    }

    protected Map<ScoreboardDisplaySlot, String> getObjectivesBySlots() {
        EnumMap<ScoreboardDisplaySlot, String> map = new EnumMap<ScoreboardDisplaySlot, String>(ScoreboardDisplaySlot.class);
        for (ScoreboardDisplaySlot scoreboardDisplaySlot : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective scoreboardObjective = this.getObjectiveForSlot(scoreboardDisplaySlot);
            if (scoreboardObjective == null) continue;
            map.put(scoreboardDisplaySlot, scoreboardObjective.getName());
        }
        return map;
    }

    public static final class PackedEntry
    extends Record {
        final String owner;
        final String objective;
        final ScoreboardScore.Packed score;
        public static final Codec<PackedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("Name").forGetter(PackedEntry::owner), (App)Codec.STRING.fieldOf("Objective").forGetter(PackedEntry::objective), (App)ScoreboardScore.Packed.CODEC.forGetter(PackedEntry::score)).apply((Applicative)instance, PackedEntry::new));

        public PackedEntry(String owner, String objective, ScoreboardScore.Packed score) {
            this.owner = owner;
            this.objective = objective;
            this.score = score;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PackedEntry.class, "owner;objective;score", "owner", "objective", "score"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PackedEntry.class, "owner;objective;score", "owner", "objective", "score"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PackedEntry.class, "owner;objective;score", "owner", "objective", "score"}, this, object);
        }

        public String owner() {
            return this.owner;
        }

        public String objective() {
            return this.objective;
        }

        public ScoreboardScore.Packed score() {
            return this.score;
        }
    }
}

