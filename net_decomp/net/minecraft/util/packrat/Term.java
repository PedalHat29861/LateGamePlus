/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Symbol;

public interface Term<S> {
    public boolean matches(ParsingState<S> var1, ParseResults var2, Cut var3);

    public static <S, T> Term<S> always(Symbol<T> symbol, T value) {
        return new AlwaysTerm(symbol, value);
    }

    @SafeVarargs
    public static <S> Term<S> sequence(Term<S> ... terms) {
        return new SequenceTerm<S>(terms);
    }

    @SafeVarargs
    public static <S> Term<S> anyOf(Term<S> ... terms) {
        return new AnyOfTerm<S>(terms);
    }

    public static <S> Term<S> optional(Term<S> term) {
        return new OptionalTerm<S>(term);
    }

    public static <S, T> Term<S> repeated(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName) {
        return Term.repeated(element, listName, 0);
    }

    public static <S, T> Term<S> repeated(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, int minRepetitions) {
        return new RepeatedTerm<S, T>(element, listName, minRepetitions);
    }

    public static <S, T> Term<S> repeatWithPossiblyTrailingSeparator(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, Term<S> separator) {
        return Term.repeatWithPossiblyTrailingSeparator(element, listName, separator, 0);
    }

    public static <S, T> Term<S> repeatWithPossiblyTrailingSeparator(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, Term<S> separator, int minRepetitions) {
        return new RepeatWithSeparatorTerm<S, T>(element, listName, separator, minRepetitions, true);
    }

    public static <S, T> Term<S> repeatWithSeparator(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, Term<S> separator) {
        return Term.repeatWithSeparator(element, listName, separator, 0);
    }

    public static <S, T> Term<S> repeatWithSeparator(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, Term<S> separator, int minRepetitions) {
        return new RepeatWithSeparatorTerm<S, T>(element, listName, separator, minRepetitions, false);
    }

    public static <S> Term<S> positiveLookahead(Term<S> term) {
        return new LookaheadTerm<S>(term, true);
    }

    public static <S> Term<S> negativeLookahead(Term<S> term) {
        return new LookaheadTerm<S>(term, false);
    }

    public static <S> Term<S> cutting() {
        return new Term<S>(){

            @Override
            public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
                cut.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    public static <S> Term<S> epsilon() {
        return new Term<S>(){

            @Override
            public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public static <S> Term<S> fail(final Object reason) {
        return new Term<S>(){

            @Override
            public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
                state.getErrors().add(state.getCursor(), reason);
                return false;
            }

            public String toString() {
                return "fail";
            }
        };
    }

    public record AlwaysTerm<S, T>(Symbol<T> name, T value) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            results.put(this.name, this.value);
            return true;
        }
    }

    public record SequenceTerm<S>(Term<S>[] elements) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int i = state.getCursor();
            for (Term<S> term : this.elements) {
                if (term.matches(state, results, cut)) continue;
                state.setCursor(i);
                return false;
            }
            return true;
        }
    }

    public record AnyOfTerm<S>(Term<S>[] elements) implements Term<S>
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            Cut cut2 = state.pushCutter();
            try {
                int i = state.getCursor();
                results.duplicateFrames();
                for (Term<S> term : this.elements) {
                    if (term.matches(state, results, cut2)) {
                        results.chooseCurrentFrame();
                        boolean bl = true;
                        return bl;
                    }
                    results.clearFrameValues();
                    state.setCursor(i);
                    if (cut2.isCut()) break;
                }
                results.popFrame();
                boolean bl = false;
                return bl;
            }
            finally {
                state.popCutter();
            }
        }
    }

    public record OptionalTerm<S>(Term<S> term) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int i = state.getCursor();
            if (!this.term.matches(state, results, cut)) {
                state.setCursor(i);
            }
            return true;
        }
    }

    public record RepeatedTerm<S, T>(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, int minRepetitions) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int j;
            int i = state.getCursor();
            ArrayList<T> list = new ArrayList<T>(this.minRepetitions);
            while (true) {
                j = state.getCursor();
                T object = state.parse(this.element);
                if (object == null) break;
                list.add(object);
            }
            state.setCursor(j);
            if (list.size() < this.minRepetitions) {
                state.setCursor(i);
                return false;
            }
            results.put(this.listName, list);
            return true;
        }
    }

    public record RepeatWithSeparatorTerm<S, T>(ParsingRuleEntry<S, T> element, Symbol<List<T>> listName, Term<S> separator, int minRepetitions, boolean allowTrailingSeparator) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int i = state.getCursor();
            ArrayList<T> list = new ArrayList<T>(this.minRepetitions);
            boolean bl = true;
            while (true) {
                int j = state.getCursor();
                if (!bl && !this.separator.matches(state, results, cut)) {
                    state.setCursor(j);
                    break;
                }
                int k = state.getCursor();
                T object = state.parse(this.element);
                if (object == null) {
                    if (bl) {
                        state.setCursor(k);
                        break;
                    }
                    if (this.allowTrailingSeparator) {
                        state.setCursor(k);
                        break;
                    }
                    state.setCursor(i);
                    return false;
                }
                list.add(object);
                bl = false;
            }
            if (list.size() < this.minRepetitions) {
                state.setCursor(i);
                return false;
            }
            results.put(this.listName, list);
            return true;
        }
    }

    public record LookaheadTerm<S>(Term<S> term, boolean positive) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            int i = state.getCursor();
            boolean bl = this.term.matches(state.getErrorSuppressingState(), results, cut);
            state.setCursor(i);
            return this.positive == bl;
        }
    }
}

