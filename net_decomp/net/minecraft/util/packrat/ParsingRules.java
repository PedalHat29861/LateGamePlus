/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;
import org.jspecify.annotations.Nullable;

public class ParsingRules<S> {
    private final Map<Symbol<?>, RuleEntryImpl<S, ?>> rules = new IdentityHashMap();

    public <T> ParsingRuleEntry<S, T> set(Symbol<T> symbol, ParsingRule<S, T> rule) {
        RuleEntryImpl ruleEntryImpl = this.rules.computeIfAbsent(symbol, RuleEntryImpl::new);
        if (ruleEntryImpl.rule != null) {
            throw new IllegalArgumentException("Trying to override rule: " + String.valueOf(symbol));
        }
        ruleEntryImpl.rule = rule;
        return ruleEntryImpl;
    }

    public <T> ParsingRuleEntry<S, T> set(Symbol<T> symbol, Term<S> term, ParsingRule.RuleAction<S, T> action) {
        return this.set(symbol, ParsingRule.of(term, action));
    }

    public <T> ParsingRuleEntry<S, T> set(Symbol<T> symbol, Term<S> term, ParsingRule.StatelessAction<S, T> action) {
        return this.set(symbol, ParsingRule.of(term, action));
    }

    public void ensureBound() {
        List<Symbol> list = this.rules.entrySet().stream().filter(entry -> ((RuleEntryImpl)entry.getValue()).rule == null).map(Map.Entry::getKey).toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound names: " + String.valueOf(list));
        }
    }

    public <T> ParsingRuleEntry<S, T> get(Symbol<T> symbol) {
        return Objects.requireNonNull(this.rules.get(symbol), () -> "No rule called " + String.valueOf(symbol));
    }

    public <T> ParsingRuleEntry<S, T> getOrCreate(Symbol<T> symbol) {
        return this.getOrCreateInternal(symbol);
    }

    private <T> RuleEntryImpl<S, T> getOrCreateInternal(Symbol<T> symbol) {
        return this.rules.computeIfAbsent(symbol, RuleEntryImpl::new);
    }

    public <T> Term<S> term(Symbol<T> symbol) {
        return new RuleTerm<S, T>(this.getOrCreateInternal(symbol), symbol);
    }

    public <T> Term<S> term(Symbol<T> symbol, Symbol<T> nameToStore) {
        return new RuleTerm<S, T>(this.getOrCreateInternal(symbol), nameToStore);
    }

    static class RuleEntryImpl<S, T>
    implements ParsingRuleEntry<S, T>,
    Supplier<String> {
        private final Symbol<T> symbol;
        @Nullable ParsingRule<S, T> rule;

        private RuleEntryImpl(Symbol<T> symbol) {
            this.symbol = symbol;
        }

        @Override
        public Symbol<T> getSymbol() {
            return this.symbol;
        }

        @Override
        public ParsingRule<S, T> getRule() {
            return Objects.requireNonNull(this.rule, this);
        }

        @Override
        public String get() {
            return "Unbound rule " + String.valueOf(this.symbol);
        }

        @Override
        public /* synthetic */ Object get() {
            return this.get();
        }
    }

    record RuleTerm<S, T>(RuleEntryImpl<S, T> ruleToParse, Symbol<T> nameToStore) implements Term<S>
    {
        @Override
        public boolean matches(ParsingState<S> state, ParseResults results, Cut cut) {
            T object = state.parse(this.ruleToParse);
            if (object == null) {
                return false;
            }
            results.put(this.nameToStore, object);
            return true;
        }
    }
}

