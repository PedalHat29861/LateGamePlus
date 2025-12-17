/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Term;
import org.jspecify.annotations.Nullable;

public interface ParsingRule<S, T> {
    public @Nullable T parse(ParsingState<S> var1);

    public static <S, T> ParsingRule<S, T> of(Term<S> term, RuleAction<S, T> action) {
        return new SimpleRule<S, T>(action, term);
    }

    public static <S, T> ParsingRule<S, T> of(Term<S> term, StatelessAction<S, T> action) {
        return new SimpleRule<S, T>(action, term);
    }

    public record SimpleRule<S, T>(RuleAction<S, T> action, Term<S> child) implements ParsingRule<S, T>
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public @Nullable T parse(ParsingState<S> state) {
            ParseResults parseResults = state.getResults();
            parseResults.pushFrame();
            try {
                if (this.child.matches(state, parseResults, Cut.NOOP)) {
                    T t = this.action.run(state);
                    return t;
                }
                T t = null;
                return t;
            }
            finally {
                parseResults.popFrame();
            }
        }
    }

    @FunctionalInterface
    public static interface RuleAction<S, T> {
        public @Nullable T run(ParsingState<S> var1);
    }

    @FunctionalInterface
    public static interface StatelessAction<S, T>
    extends RuleAction<S, T> {
        public T run(ParseResults var1);

        @Override
        default public T run(ParsingState<S> parsingState) {
            return this.run(parsingState.getResults());
        }
    }
}

