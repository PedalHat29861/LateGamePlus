/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import org.jspecify.annotations.Nullable;

public class ScoreboardObjective {
    private final Scoreboard scoreboard;
    private final String name;
    private final ScoreboardCriterion criterion;
    private Text displayName;
    private Text bracketedDisplayName;
    private ScoreboardCriterion.RenderType renderType;
    private boolean displayAutoUpdate;
    private @Nullable NumberFormat numberFormat;

    public ScoreboardObjective(Scoreboard scoreboard, String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, @Nullable NumberFormat numberFormat) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.criterion = criterion;
        this.displayName = displayName;
        this.bracketedDisplayName = this.generateBracketedDisplayName();
        this.renderType = renderType;
        this.displayAutoUpdate = displayAutoUpdate;
        this.numberFormat = numberFormat;
    }

    public Packed pack() {
        return new Packed(this.name, this.criterion, this.displayName, this.renderType, this.displayAutoUpdate, Optional.ofNullable(this.numberFormat));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public String getName() {
        return this.name;
    }

    public ScoreboardCriterion getCriterion() {
        return this.criterion;
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public boolean shouldDisplayAutoUpdate() {
        return this.displayAutoUpdate;
    }

    public @Nullable NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    public NumberFormat getNumberFormatOr(NumberFormat format) {
        return Objects.requireNonNullElse(this.numberFormat, format);
    }

    private Text generateBracketedDisplayName() {
        return Texts.bracketed(this.displayName.copy().styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.literal(this.name)))));
    }

    public Text toHoverableText() {
        return this.bracketedDisplayName;
    }

    public void setDisplayName(Text name) {
        this.displayName = name;
        this.bracketedDisplayName = this.generateBracketedDisplayName();
        this.scoreboard.updateExistingObjective(this);
    }

    public ScoreboardCriterion.RenderType getRenderType() {
        return this.renderType;
    }

    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        this.renderType = renderType;
        this.scoreboard.updateExistingObjective(this);
    }

    public void setDisplayAutoUpdate(boolean displayAutoUpdate) {
        this.displayAutoUpdate = displayAutoUpdate;
        this.scoreboard.updateExistingObjective(this);
    }

    public void setNumberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        this.scoreboard.updateExistingObjective(this);
    }

    public record Packed(String name, ScoreboardCriterion criteria, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, Optional<NumberFormat> numberFormat) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("Name").forGetter(Packed::name), (App)ScoreboardCriterion.CODEC.optionalFieldOf("CriteriaName", (Object)ScoreboardCriterion.DUMMY).forGetter(Packed::criteria), (App)TextCodecs.CODEC.fieldOf("DisplayName").forGetter(Packed::displayName), (App)ScoreboardCriterion.RenderType.CODEC.optionalFieldOf("RenderType", ScoreboardCriterion.RenderType.INTEGER).forGetter(Packed::renderType), (App)Codec.BOOL.optionalFieldOf("display_auto_update", (Object)false).forGetter(Packed::displayAutoUpdate), (App)NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(Packed::numberFormat)).apply((Applicative)instance, Packed::new));
    }
}

