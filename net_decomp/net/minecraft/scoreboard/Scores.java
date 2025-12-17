/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.jspecify.annotations.Nullable;

class Scores {
    private final Reference2ObjectOpenHashMap<ScoreboardObjective, ScoreboardScore> scores = new Reference2ObjectOpenHashMap(16, 0.5f);

    Scores() {
    }

    public @Nullable ScoreboardScore get(ScoreboardObjective objective) {
        return (ScoreboardScore)this.scores.get((Object)objective);
    }

    public ScoreboardScore getOrCreate(ScoreboardObjective objective, Consumer<ScoreboardScore> scoreConsumer) {
        return (ScoreboardScore)this.scores.computeIfAbsent((Object)objective, objective2 -> {
            ScoreboardScore scoreboardScore = new ScoreboardScore();
            scoreConsumer.accept(scoreboardScore);
            return scoreboardScore;
        });
    }

    public boolean remove(ScoreboardObjective objective) {
        return this.scores.remove((Object)objective) != null;
    }

    public boolean hasScores() {
        return !this.scores.isEmpty();
    }

    public Object2IntMap<ScoreboardObjective> getScoresAsIntMap() {
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        this.scores.forEach((arg_0, arg_1) -> Scores.method_55392((Object2IntMap)object2IntMap, arg_0, arg_1));
        return object2IntMap;
    }

    void put(ScoreboardObjective objective, ScoreboardScore score) {
        this.scores.put((Object)objective, (Object)score);
    }

    Map<ScoreboardObjective, ScoreboardScore> getScores() {
        return Collections.unmodifiableMap(this.scores);
    }

    private static /* synthetic */ void method_55392(Object2IntMap object2IntMap, ScoreboardObjective objective, ScoreboardScore score) {
        object2IntMap.put((Object)objective, score.getScore());
    }
}

