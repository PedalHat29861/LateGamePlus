/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import java.util.Objects;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.MutableText;
import org.jspecify.annotations.Nullable;

public interface ReadableScoreboardScore {
    public int getScore();

    public boolean isLocked();

    public @Nullable NumberFormat getNumberFormat();

    default public MutableText getFormattedScore(NumberFormat fallbackFormat) {
        return Objects.requireNonNullElse(this.getNumberFormat(), fallbackFormat).format(this.getScore());
    }

    public static MutableText getFormattedScore(@Nullable ReadableScoreboardScore score, NumberFormat fallbackFormat) {
        return score != null ? score.getFormattedScore(fallbackFormat) : fallbackFormat.format(0);
    }
}

