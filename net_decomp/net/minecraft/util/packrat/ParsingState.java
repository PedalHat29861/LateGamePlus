/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import java.util.Optional;
import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseErrorList;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import org.jspecify.annotations.Nullable;

public interface ParsingState<S> {
    public ParseResults getResults();

    public ParseErrorList<S> getErrors();

    default public <T> Optional<T> startParsing(ParsingRuleEntry<S, T> rule) {
        T object = this.parse(rule);
        if (object != null) {
            this.getErrors().setCursor(this.getCursor());
        }
        if (!this.getResults().areFramesPlacedCorrectly()) {
            throw new IllegalStateException("Malformed scope: " + String.valueOf(this.getResults()));
        }
        return Optional.ofNullable(object);
    }

    public <T> @Nullable T parse(ParsingRuleEntry<S, T> var1);

    public S getReader();

    public int getCursor();

    public void setCursor(int var1);

    public Cut pushCutter();

    public void popCutter();

    public ParsingState<S> getErrorSuppressingState();
}

