/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.ParsedSelector;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jspecify.annotations.Nullable;

public record ScoreTextContent(Either<ParsedSelector, String> name, String objective) implements TextContent
{
    public static final MapCodec<ScoreTextContent> INNER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.either(ParsedSelector.CODEC, (Codec)Codec.STRING).fieldOf("name").forGetter(ScoreTextContent::name), (App)Codec.STRING.fieldOf("objective").forGetter(ScoreTextContent::objective)).apply((Applicative)instance, ScoreTextContent::new));
    public static final MapCodec<ScoreTextContent> CODEC = INNER_CODEC.fieldOf("score");

    public MapCodec<ScoreTextContent> getCodec() {
        return CODEC;
    }

    private ScoreHolder getScoreHolder(ServerCommandSource source) throws CommandSyntaxException {
        Optional optional = this.name.left();
        if (optional.isPresent()) {
            List<? extends Entity> list = ((ParsedSelector)optional.get()).comp_3068().getEntities(source);
            if (!list.isEmpty()) {
                if (list.size() != 1) {
                    throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
                }
                return list.getFirst();
            }
            return ScoreHolder.fromName(((ParsedSelector)optional.get()).comp_3067());
        }
        return ScoreHolder.fromName((String)this.name.right().orElseThrow());
    }

    private MutableText getScore(ScoreHolder scoreHolder, ServerCommandSource source) {
        ReadableScoreboardScore readableScoreboardScore;
        ServerScoreboard scoreboard;
        ScoreboardObjective scoreboardObjective;
        MinecraftServer minecraftServer = source.getServer();
        if (minecraftServer != null && (scoreboardObjective = (scoreboard = minecraftServer.getScoreboard()).getNullableObjective(this.objective)) != null && (readableScoreboardScore = scoreboard.getScore(scoreHolder, scoreboardObjective)) != null) {
            return readableScoreboardScore.getFormattedScore(scoreboardObjective.getNumberFormatOr(StyledNumberFormat.EMPTY));
        }
        return Text.empty();
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        if (source == null) {
            return Text.empty();
        }
        ScoreHolder scoreHolder = this.getScoreHolder(source);
        ScoreHolder scoreHolder2 = sender != null && scoreHolder.equals(ScoreHolder.WILDCARD) ? sender : scoreHolder;
        return this.getScore(scoreHolder2, source);
    }

    @Override
    public String toString() {
        return "score{name='" + String.valueOf(this.name) + "', objective='" + this.objective + "'}";
    }
}

