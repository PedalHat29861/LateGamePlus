/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.scoreboard;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class ScoreboardState
extends PersistentState {
    public static final PersistentStateType<ScoreboardState> TYPE = new PersistentStateType<ScoreboardState>("scoreboard", ScoreboardState::new, Packed.CODEC.xmap(ScoreboardState::new, ScoreboardState::getPackedState), DataFixTypes.SAVED_DATA_SCOREBOARD);
    private Packed packedState;

    private ScoreboardState() {
        this(Packed.EMPTY);
    }

    public ScoreboardState(Packed packedState) {
        this.packedState = packedState;
    }

    public Packed getPackedState() {
        return this.packedState;
    }

    public void set(Packed packed) {
        if (!packed.equals(this.packedState)) {
            this.packedState = packed;
            this.markDirty();
        }
    }

    public record Packed(List<ScoreboardObjective.Packed> objectives, List<Scoreboard.PackedEntry> scores, Map<ScoreboardDisplaySlot, String> displaySlots, List<Team.Packed> teams) {
        public static final Packed EMPTY = new Packed(List.of(), List.of(), Map.of(), List.of());
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ScoreboardObjective.Packed.CODEC.listOf().optionalFieldOf("Objectives", List.of()).forGetter(Packed::objectives), (App)Scoreboard.PackedEntry.CODEC.listOf().optionalFieldOf("PlayerScores", List.of()).forGetter(Packed::scores), (App)Codec.unboundedMap(ScoreboardDisplaySlot.CODEC, (Codec)Codec.STRING).optionalFieldOf("DisplaySlots", Map.of()).forGetter(Packed::displaySlots), (App)Team.Packed.CODEC.listOf().optionalFieldOf("Teams", List.of()).forGetter(Packed::teams)).apply((Applicative)instance, Packed::new));
    }
}

