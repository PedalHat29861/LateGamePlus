/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.command.argument;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtParsingRule;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.AnyIdParsingRule;
import net.minecraft.util.packrat.IdentifiableParsingRule;
import net.minecraft.util.packrat.Literals;
import net.minecraft.util.packrat.PackratParser;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;

public class ItemPredicateParsing {
    public static <T, C, P> PackratParser<List<T>> createParser(Callbacks<T, C, P> callbacks) {
        Symbol symbol = Symbol.of("top");
        Symbol symbol2 = Symbol.of("type");
        Symbol symbol3 = Symbol.of("any_type");
        Symbol symbol4 = Symbol.of("element_type");
        Symbol symbol5 = Symbol.of("tag_type");
        Symbol symbol6 = Symbol.of("conditions");
        Symbol symbol7 = Symbol.of("alternatives");
        Symbol symbol8 = Symbol.of("term");
        Symbol symbol9 = Symbol.of("negation");
        Symbol symbol10 = Symbol.of("test");
        Symbol symbol11 = Symbol.of("component_type");
        Symbol symbol12 = Symbol.of("predicate_type");
        Symbol symbol13 = Symbol.of("id");
        Symbol symbol14 = Symbol.of("tag");
        ParsingRules<StringReader> parsingRules = new ParsingRules<StringReader>();
        ParsingRuleEntry<StringReader, Identifier> parsingRuleEntry = parsingRules.set(symbol13, AnyIdParsingRule.INSTANCE);
        ParsingRuleEntry parsingRuleEntry2 = parsingRules.set(symbol, Term.anyOf(Term.sequence(parsingRules.term(symbol2), Literals.character('['), Term.cutting(), Term.optional(parsingRules.term(symbol6)), Literals.character(']')), parsingRules.term(symbol2)), results -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            ((Optional)results.getOrThrow(symbol2)).ifPresent(arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
            List list = (List)results.get(symbol6);
            if (list != null) {
                builder.addAll((Iterable)list);
            }
            return builder.build();
        });
        parsingRules.set(symbol2, Term.anyOf(parsingRules.term(symbol4), Term.sequence(Literals.character('#'), Term.cutting(), parsingRules.term(symbol5)), parsingRules.term(symbol3)), results -> Optional.ofNullable(results.getAny(symbol4, symbol5)));
        parsingRules.set(symbol3, Literals.character('*'), results -> Unit.INSTANCE);
        parsingRules.set(symbol4, new ItemParsingRule<T, C, P>(parsingRuleEntry, callbacks));
        parsingRules.set(symbol5, new TagParsingRule<T, C, P>(parsingRuleEntry, callbacks));
        parsingRules.set(symbol6, Term.sequence(parsingRules.term(symbol7), Term.optional(Term.sequence(Literals.character(','), parsingRules.term(symbol6)))), results -> {
            Object object = callbacks.anyOf((List)results.getOrThrow(symbol7));
            return Optional.ofNullable((List)results.get(symbol6)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        parsingRules.set(symbol7, Term.sequence(parsingRules.term(symbol8), Term.optional(Term.sequence(Literals.character('|'), parsingRules.term(symbol7)))), results -> {
            Object object = results.getOrThrow(symbol8);
            return Optional.ofNullable((List)results.get(symbol7)).map(predicates -> Util.withPrepended(object, predicates)).orElse(List.of(object));
        });
        parsingRules.set(symbol8, Term.anyOf(parsingRules.term(symbol10), Term.sequence(Literals.character('!'), parsingRules.term(symbol9))), results -> results.getAnyOrThrow(symbol10, symbol9));
        parsingRules.set(symbol9, parsingRules.term(symbol10), results -> callbacks.negate(results.getOrThrow(symbol10)));
        parsingRules.set(symbol10, Term.anyOf(Term.sequence(parsingRules.term(symbol11), Literals.character('='), Term.cutting(), parsingRules.term(symbol14)), Term.sequence(parsingRules.term(symbol12), Literals.character('~'), Term.cutting(), parsingRules.term(symbol14)), parsingRules.term(symbol11)), state -> {
            ParseResults parseResults = state.getResults();
            Object object = parseResults.get(symbol12);
            try {
                if (object != null) {
                    Dynamic dynamic = (Dynamic)parseResults.getOrThrow(symbol14);
                    return callbacks.subPredicatePredicate((ImmutableStringReader)state.getReader(), object, dynamic);
                }
                Object object2 = parseResults.getOrThrow(symbol11);
                Dynamic dynamic2 = (Dynamic)parseResults.get(symbol14);
                return dynamic2 != null ? callbacks.componentMatchPredicate((ImmutableStringReader)state.getReader(), object2, dynamic2) : callbacks.componentPresencePredicate((ImmutableStringReader)state.getReader(), object2);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                state.getErrors().add(state.getCursor(), (Object)commandSyntaxException);
                return null;
            }
        });
        parsingRules.set(symbol11, new ComponentParsingRule<T, C, P>(parsingRuleEntry, callbacks));
        parsingRules.set(symbol12, new SubPredicateParsingRule<T, C, P>(parsingRuleEntry, callbacks));
        parsingRules.set(symbol14, new NbtParsingRule<NbtElement>(NbtOps.INSTANCE));
        return new PackratParser<List<T>>(parsingRules, parsingRuleEntry2);
    }

    static class ItemParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        ItemParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).itemMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamItemIds();
        }
    }

    public static interface Callbacks<T, C, P> {
        public T itemMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamItemIds();

        public T tagMatchPredicate(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamTags();

        public C componentCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamComponentIds();

        public T componentMatchPredicate(ImmutableStringReader var1, C var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T componentPresencePredicate(ImmutableStringReader var1, C var2);

        public P subPredicateCheck(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> streamSubPredicateIds();

        public T subPredicatePredicate(ImmutableStringReader var1, P var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, T> {
        TagParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected T parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).tagMatchPredicate(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamTags();
        }
    }

    static class ComponentParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, C> {
        ComponentParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected C parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).componentCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamComponentIds();
        }
    }

    static class SubPredicateParsingRule<T, C, P>
    extends IdentifiableParsingRule<Callbacks<T, C, P>, P> {
        SubPredicateParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, Callbacks<T, C, P> callbacks) {
            super(idParsingRule, callbacks);
        }

        @Override
        protected P parse(ImmutableStringReader reader, Identifier id) throws Exception {
            return ((Callbacks)this.callbacks).subPredicateCheck(reader, id);
        }

        @Override
        public Stream<Identifier> possibleIds() {
            return ((Callbacks)this.callbacks).streamSubPredicateIds();
        }
    }
}

