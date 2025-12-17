/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jspecify.annotations.Nullable;

public class ScoreboardScore
implements ReadableScoreboardScore {
    private int score;
    private boolean locked = true;
    private @Nullable Text displayText;
    private @Nullable NumberFormat numberFormat;

    public ScoreboardScore() {
    }

    public ScoreboardScore(Packed packed) {
        this.score = packed.value;
        this.locked = packed.locked;
        this.displayText = packed.display.orElse(null);
        this.numberFormat = packed.numberFormat.orElse(null);
    }

    public Packed toPacked() {
        return new Packed(this.score, this.locked, Optional.ofNullable(this.displayText), Optional.ofNullable(this.numberFormat));
    }

    @Override
    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public @Nullable Text getDisplayText() {
        return this.displayText;
    }

    public void setDisplayText(@Nullable Text text) {
        this.displayText = text;
    }

    @Override
    public @Nullable NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public void setNumberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public static final class Packed
    extends Record {
        final int value;
        final boolean locked;
        final Optional<Text> display;
        final Optional<NumberFormat> numberFormat;
        public static final MapCodec<Packed> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.optionalFieldOf("Score", (Object)0).forGetter(Packed::value), (App)Codec.BOOL.optionalFieldOf("Locked", (Object)false).forGetter(Packed::locked), (App)TextCodecs.CODEC.optionalFieldOf("display").forGetter(Packed::display), (App)NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(Packed::numberFormat)).apply((Applicative)instance, Packed::new));

        public Packed(int value, boolean locked, Optional<Text> display, Optional<NumberFormat> numberFormat) {
            this.value = value;
            this.locked = locked;
            this.display = display;
            this.numberFormat = numberFormat;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "value;locked;display;numberFormat", "value", "locked", "display", "numberFormat"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "value;locked;display;numberFormat", "value", "locked", "display", "numberFormat"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "value;locked;display;numberFormat", "value", "locked", "display", "numberFormat"}, this, object);
        }

        public int value() {
            return this.value;
        }

        public boolean locked() {
            return this.locked;
        }

        public Optional<Text> display() {
            return this.display;
        }

        public Optional<NumberFormat> numberFormat() {
            return this.numberFormat;
        }
    }
}

