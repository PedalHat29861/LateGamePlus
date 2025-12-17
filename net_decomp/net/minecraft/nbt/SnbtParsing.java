/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.primitives.UnsignedBytes
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.chars.CharList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.nbt.SnbtOperation;
import net.minecraft.text.Text;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.Literals;
import net.minecraft.util.packrat.NumeralParsingRule;
import net.minecraft.util.packrat.PackratParser;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.PatternParsingRule;
import net.minecraft.util.packrat.Symbol;
import net.minecraft.util.packrat.Term;
import net.minecraft.util.packrat.TokenParsingRule;
import net.minecraft.util.packrat.UnquotedStringParsingRule;
import org.jspecify.annotations.Nullable;

public class SnbtParsing {
    private static final DynamicCommandExceptionType NUMBER_PARSE_FAILURE_EXCEPTION = new DynamicCommandExceptionType(value -> Text.stringifiedTranslatable("snbt.parser.number_parse_failure", value));
    static final DynamicCommandExceptionType EXPECTED_HEX_ESCAPE_EXCEPTION = new DynamicCommandExceptionType(length -> Text.stringifiedTranslatable("snbt.parser.expected_hex_escape", length));
    private static final DynamicCommandExceptionType INVALID_CODEPOINT_EXCEPTION = new DynamicCommandExceptionType(value -> Text.stringifiedTranslatable("snbt.parser.invalid_codepoint", value));
    private static final DynamicCommandExceptionType NO_SUCH_OPERATION_EXCEPTION = new DynamicCommandExceptionType(operation -> Text.stringifiedTranslatable("snbt.parser.no_such_operation", operation));
    static final CursorExceptionType<CommandSyntaxException> EXPECTED_INTEGER_TYPE_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_integer_type")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_FLOAT_TYPE_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_float_type")));
    static final CursorExceptionType<CommandSyntaxException> EXPECTED_NON_NEGATIVE_NUMBER_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_non_negative_number")));
    private static final CursorExceptionType<CommandSyntaxException> INVALID_CHARACTER_NAME_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.invalid_character_name")));
    static final CursorExceptionType<CommandSyntaxException> INVALID_ARRAY_ELEMENT_TYPE_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.invalid_array_element_type")));
    private static final CursorExceptionType<CommandSyntaxException> INVALID_UNQUOTED_START_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.invalid_unquoted_start")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_UNQUOTED_STRING_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_unquoted_string")));
    private static final CursorExceptionType<CommandSyntaxException> INVALID_STRING_CONTENTS_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.invalid_string_contents")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_BINARY_NUMERAL_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_binary_numeral")));
    private static final CursorExceptionType<CommandSyntaxException> UNDERSCORE_NOT_ALLOWED_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.underscore_not_allowed")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_DECIMAL_NUMERAL_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_decimal_numeral")));
    private static final CursorExceptionType<CommandSyntaxException> EXPECTED_HEX_NUMERAL_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.expected_hex_numeral")));
    private static final CursorExceptionType<CommandSyntaxException> EMPTY_KEY_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.empty_key")));
    private static final CursorExceptionType<CommandSyntaxException> LEADING_ZERO_NOT_ALLOWED_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.leading_zero_not_allowed")));
    private static final CursorExceptionType<CommandSyntaxException> INFINITY_NOT_ALLOWED_EXCEPTION = CursorExceptionType.create(new SimpleCommandExceptionType((Message)Text.translatable("snbt.parser.infinity_not_allowed")));
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final NumeralParsingRule BINARY_RULE = new NumeralParsingRule((CursorExceptionType)EXPECTED_BINARY_NUMERAL_EXCEPTION, (CursorExceptionType)UNDERSCORE_NOT_ALLOWED_EXCEPTION){

        @Override
        protected boolean accepts(char c) {
            return switch (c) {
                case '0', '1', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumeralParsingRule DECIMAL_RULE = new NumeralParsingRule((CursorExceptionType)EXPECTED_DECIMAL_NUMERAL_EXCEPTION, (CursorExceptionType)UNDERSCORE_NOT_ALLOWED_EXCEPTION){

        @Override
        protected boolean accepts(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumeralParsingRule HEX_RULE = new NumeralParsingRule((CursorExceptionType)EXPECTED_HEX_NUMERAL_EXCEPTION, (CursorExceptionType)UNDERSCORE_NOT_ALLOWED_EXCEPTION){

        @Override
        protected boolean accepts(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', '_', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    };
    private static final TokenParsingRule UNQUOTED_STRING_RULE = new TokenParsingRule(1, (CursorExceptionType)INVALID_STRING_CONTENTS_EXCEPTION){

        @Override
        protected boolean isValidChar(char c) {
            return switch (c) {
                case '\"', '\'', '\\' -> false;
                default -> true;
            };
        }
    };
    private static final Literals.CharacterLiteral DECIMAL_CHAR = new Literals.CharacterLiteral(CharList.of()){

        @Override
        protected boolean accepts(char c) {
            return SnbtParsing.isPartOfDecimal(c);
        }
    };
    private static final Pattern UNICODE_NAME_PATTERN = Pattern.compile("[-a-zA-Z0-9 ]+");

    static CursorExceptionType<CommandSyntaxException> toNumberParseFailure(NumberFormatException exception) {
        return CursorExceptionType.create(NUMBER_PARSE_FAILURE_EXCEPTION, exception.getMessage());
    }

    public static @Nullable String escapeSpecialChar(char c) {
        return switch (c) {
            case '\b' -> "b";
            case '\t' -> "t";
            case '\n' -> "n";
            case '\f' -> "f";
            case '\r' -> "r";
            default -> c < ' ' ? "x" + HEX_FORMAT.toHexDigits((byte)c) : null;
        };
    }

    private static boolean canUnquotedStringStartWith(char c) {
        return !SnbtParsing.isPartOfDecimal(c);
    }

    static boolean isPartOfDecimal(char c) {
        return switch (c) {
            case '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }

    static boolean containsUnderscore(String string) {
        return string.indexOf(95) != -1;
    }

    private static void skipUnderscoreAndAppend(StringBuilder builder, String value) {
        SnbtParsing.append(builder, value, SnbtParsing.containsUnderscore(value));
    }

    static void append(StringBuilder builder, String value, boolean skipUnderscore) {
        if (skipUnderscore) {
            for (char c : value.toCharArray()) {
                if (c == '_') continue;
                builder.append(c);
            }
        } else {
            builder.append(value);
        }
    }

    static short parseUnsignedShort(String value, int radix) {
        int i = Integer.parseInt(value, radix);
        if (i >> 16 == 0) {
            return (short)i;
        }
        throw new NumberFormatException("out of range: " + i);
    }

    private static <T> @Nullable T decodeFloat(DynamicOps<T> ops, Sign sign, @Nullable String intPart, @Nullable String fractionalPart, @Nullable SignedValue<String> exponent, @Nullable NumericType type, ParsingState<?> state) {
        StringBuilder stringBuilder = new StringBuilder();
        sign.append(stringBuilder);
        if (intPart != null) {
            SnbtParsing.skipUnderscoreAndAppend(stringBuilder, intPart);
        }
        if (fractionalPart != null) {
            stringBuilder.append('.');
            SnbtParsing.skipUnderscoreAndAppend(stringBuilder, fractionalPart);
        }
        if (exponent != null) {
            stringBuilder.append('e');
            exponent.sign().append(stringBuilder);
            SnbtParsing.skipUnderscoreAndAppend(stringBuilder, (String)exponent.value);
        }
        try {
            String string = stringBuilder.toString();
            NumericType numericType = type;
            int n = 0;
            return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"FLOAT", "DOUBLE"}, (NumericType)numericType, n)) {
                case 0 -> SnbtParsing.parseFiniteFloat(ops, state, string);
                case 1 -> SnbtParsing.parseFiniteDouble(ops, state, string);
                case -1 -> SnbtParsing.parseFiniteDouble(ops, state, string);
                default -> {
                    state.getErrors().add(state.getCursor(), EXPECTED_FLOAT_TYPE_EXCEPTION);
                    yield null;
                }
            };
        }
        catch (NumberFormatException numberFormatException) {
            state.getErrors().add(state.getCursor(), SnbtParsing.toNumberParseFailure(numberFormatException));
            return null;
        }
    }

    private static <T> @Nullable T parseFiniteFloat(DynamicOps<T> ops, ParsingState<?> state, String value) {
        float f = Float.parseFloat(value);
        if (!Float.isFinite(f)) {
            state.getErrors().add(state.getCursor(), INFINITY_NOT_ALLOWED_EXCEPTION);
            return null;
        }
        return (T)ops.createFloat(f);
    }

    private static <T> @Nullable T parseFiniteDouble(DynamicOps<T> ops, ParsingState<?> state, String value) {
        double d = Double.parseDouble(value);
        if (!Double.isFinite(d)) {
            state.getErrors().add(state.getCursor(), INFINITY_NOT_ALLOWED_EXCEPTION);
            return null;
        }
        return (T)ops.createDouble(d);
    }

    private static String join(List<String> values) {
        return switch (values.size()) {
            case 0 -> "";
            case 1 -> values.getFirst();
            default -> String.join((CharSequence)"", values);
        };
    }

    public static <T> PackratParser<T> createParser(DynamicOps<T> ops) {
        Object object = ops.createBoolean(true);
        Object object2 = ops.createBoolean(false);
        Object object3 = ops.emptyMap();
        Object object4 = ops.emptyList();
        ParsingRules<StringReader> parsingRules = new ParsingRules<StringReader>();
        Symbol symbol = Symbol.of("sign");
        parsingRules.set(symbol, Term.anyOf(Term.sequence(Literals.character('+'), Term.always(symbol, Sign.PLUS)), Term.sequence(Literals.character('-'), Term.always(symbol, Sign.MINUS))), results -> (Sign)((Object)((Object)results.getOrThrow(symbol))));
        Symbol symbol2 = Symbol.of("integer_suffix");
        parsingRules.set(symbol2, Term.anyOf(Term.sequence(Literals.character('u', 'U'), Term.anyOf(Term.sequence(Literals.character('b', 'B'), Term.always(symbol2, new NumberSuffix(Signedness.UNSIGNED, NumericType.BYTE))), Term.sequence(Literals.character('s', 'S'), Term.always(symbol2, new NumberSuffix(Signedness.UNSIGNED, NumericType.SHORT))), Term.sequence(Literals.character('i', 'I'), Term.always(symbol2, new NumberSuffix(Signedness.UNSIGNED, NumericType.INT))), Term.sequence(Literals.character('l', 'L'), Term.always(symbol2, new NumberSuffix(Signedness.UNSIGNED, NumericType.LONG))))), Term.sequence(Literals.character('s', 'S'), Term.anyOf(Term.sequence(Literals.character('b', 'B'), Term.always(symbol2, new NumberSuffix(Signedness.SIGNED, NumericType.BYTE))), Term.sequence(Literals.character('s', 'S'), Term.always(symbol2, new NumberSuffix(Signedness.SIGNED, NumericType.SHORT))), Term.sequence(Literals.character('i', 'I'), Term.always(symbol2, new NumberSuffix(Signedness.SIGNED, NumericType.INT))), Term.sequence(Literals.character('l', 'L'), Term.always(symbol2, new NumberSuffix(Signedness.SIGNED, NumericType.LONG))))), Term.sequence(Literals.character('b', 'B'), Term.always(symbol2, new NumberSuffix(null, NumericType.BYTE))), Term.sequence(Literals.character('s', 'S'), Term.always(symbol2, new NumberSuffix(null, NumericType.SHORT))), Term.sequence(Literals.character('i', 'I'), Term.always(symbol2, new NumberSuffix(null, NumericType.INT))), Term.sequence(Literals.character('l', 'L'), Term.always(symbol2, new NumberSuffix(null, NumericType.LONG)))), results -> (NumberSuffix)results.getOrThrow(symbol2));
        Symbol symbol3 = Symbol.of("binary_numeral");
        parsingRules.set(symbol3, BINARY_RULE);
        Symbol symbol4 = Symbol.of("decimal_numeral");
        parsingRules.set(symbol4, DECIMAL_RULE);
        Symbol symbol5 = Symbol.of("hex_numeral");
        parsingRules.set(symbol5, HEX_RULE);
        Symbol symbol6 = Symbol.of("integer_literal");
        ParsingRuleEntry parsingRuleEntry = parsingRules.set(symbol6, Term.sequence(Term.optional(parsingRules.term(symbol)), Term.anyOf(Term.sequence(Literals.character('0'), Term.cutting(), Term.anyOf(Term.sequence(Literals.character('x', 'X'), Term.cutting(), parsingRules.term(symbol5)), Term.sequence(Literals.character('b', 'B'), parsingRules.term(symbol3)), Term.sequence(parsingRules.term(symbol4), Term.cutting(), Term.fail(LEADING_ZERO_NOT_ALLOWED_EXCEPTION)), Term.always(symbol4, "0"))), parsingRules.term(symbol4)), Term.optional(parsingRules.term(symbol2))), results -> {
            NumberSuffix numberSuffix = results.getOrDefault(symbol2, NumberSuffix.DEFAULT);
            Sign sign = results.getOrDefault(symbol, Sign.PLUS);
            String string = (String)results.get(symbol4);
            if (string != null) {
                return new IntValue(sign, Radix.DECIMAL, string, numberSuffix);
            }
            String string2 = (String)results.get(symbol5);
            if (string2 != null) {
                return new IntValue(sign, Radix.HEX, string2, numberSuffix);
            }
            String string3 = (String)results.getOrThrow(symbol3);
            return new IntValue(sign, Radix.BINARY, string3, numberSuffix);
        });
        Symbol symbol7 = Symbol.of("float_type_suffix");
        parsingRules.set(symbol7, Term.anyOf(Term.sequence(Literals.character('f', 'F'), Term.always(symbol7, NumericType.FLOAT)), Term.sequence(Literals.character('d', 'D'), Term.always(symbol7, NumericType.DOUBLE))), results -> (NumericType)((Object)((Object)results.getOrThrow(symbol7))));
        Symbol symbol8 = Symbol.of("float_exponent_part");
        parsingRules.set(symbol8, Term.sequence(Literals.character('e', 'E'), Term.optional(parsingRules.term(symbol)), parsingRules.term(symbol4)), results -> new SignedValue<String>(results.getOrDefault(symbol, Sign.PLUS), (String)results.getOrThrow(symbol4)));
        Symbol symbol9 = Symbol.of("float_whole_part");
        Symbol symbol10 = Symbol.of("float_fraction_part");
        Symbol symbol11 = Symbol.of("float_literal");
        parsingRules.set(symbol11, Term.sequence(Term.optional(parsingRules.term(symbol)), Term.anyOf(Term.sequence(parsingRules.term(symbol4, symbol9), Literals.character('.'), Term.cutting(), Term.optional(parsingRules.term(symbol4, symbol10)), Term.optional(parsingRules.term(symbol8)), Term.optional(parsingRules.term(symbol7))), Term.sequence(Literals.character('.'), Term.cutting(), parsingRules.term(symbol4, symbol10), Term.optional(parsingRules.term(symbol8)), Term.optional(parsingRules.term(symbol7))), Term.sequence(parsingRules.term(symbol4, symbol9), parsingRules.term(symbol8), Term.cutting(), Term.optional(parsingRules.term(symbol7))), Term.sequence(parsingRules.term(symbol4, symbol9), Term.optional(parsingRules.term(symbol8)), parsingRules.term(symbol7)))), state -> {
            ParseResults parseResults = state.getResults();
            Sign sign = parseResults.getOrDefault(symbol, Sign.PLUS);
            String string = (String)parseResults.get(symbol9);
            String string2 = (String)parseResults.get(symbol10);
            SignedValue signedValue = (SignedValue)parseResults.get(symbol8);
            NumericType numericType = (NumericType)((Object)((Object)parseResults.get(symbol7)));
            return SnbtParsing.decodeFloat(ops, sign, string, string2, signedValue, numericType, state);
        });
        Symbol symbol12 = Symbol.of("string_hex_2");
        parsingRules.set(symbol12, new HexParsingRule(2));
        Symbol symbol13 = Symbol.of("string_hex_4");
        parsingRules.set(symbol13, new HexParsingRule(4));
        Symbol symbol14 = Symbol.of("string_hex_8");
        parsingRules.set(symbol14, new HexParsingRule(8));
        Symbol symbol15 = Symbol.of("string_unicode_name");
        parsingRules.set(symbol15, new PatternParsingRule(UNICODE_NAME_PATTERN, INVALID_CHARACTER_NAME_EXCEPTION));
        Symbol symbol16 = Symbol.of("string_escape_sequence");
        parsingRules.set(symbol16, Term.anyOf(Term.sequence(Literals.character('b'), Term.always(symbol16, "\b")), Term.sequence(Literals.character('s'), Term.always(symbol16, " ")), Term.sequence(Literals.character('t'), Term.always(symbol16, "\t")), Term.sequence(Literals.character('n'), Term.always(symbol16, "\n")), Term.sequence(Literals.character('f'), Term.always(symbol16, "\f")), Term.sequence(Literals.character('r'), Term.always(symbol16, "\r")), Term.sequence(Literals.character('\\'), Term.always(symbol16, "\\")), Term.sequence(Literals.character('\''), Term.always(symbol16, "'")), Term.sequence(Literals.character('\"'), Term.always(symbol16, "\"")), Term.sequence(Literals.character('x'), parsingRules.term(symbol12)), Term.sequence(Literals.character('u'), parsingRules.term(symbol13)), Term.sequence(Literals.character('U'), parsingRules.term(symbol14)), Term.sequence(Literals.character('N'), Literals.character('{'), parsingRules.term(symbol15), Literals.character('}'))), state -> {
            int j;
            ParseResults parseResults = state.getResults();
            String string = (String)parseResults.getAny(symbol16);
            if (string != null) {
                return string;
            }
            String string2 = (String)parseResults.getAny(symbol12, symbol13, symbol14);
            if (string2 != null) {
                int i = HexFormat.fromHexDigits(string2);
                if (!Character.isValidCodePoint(i)) {
                    state.getErrors().add(state.getCursor(), CursorExceptionType.create(INVALID_CODEPOINT_EXCEPTION, String.format(Locale.ROOT, "U+%08X", i)));
                    return null;
                }
                return Character.toString(i);
            }
            String string3 = (String)parseResults.getOrThrow(symbol15);
            try {
                j = Character.codePointOf(string3);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                state.getErrors().add(state.getCursor(), INVALID_CHARACTER_NAME_EXCEPTION);
                return null;
            }
            return Character.toString(j);
        });
        Symbol symbol17 = Symbol.of("string_plain_contents");
        parsingRules.set(symbol17, UNQUOTED_STRING_RULE);
        Symbol symbol18 = Symbol.of("string_chunks");
        Symbol symbol19 = Symbol.of("string_contents");
        Symbol symbol20 = Symbol.of("single_quoted_string_chunk");
        ParsingRuleEntry parsingRuleEntry2 = parsingRules.set(symbol20, Term.anyOf(parsingRules.term(symbol17, symbol19), Term.sequence(Literals.character('\\'), parsingRules.term(symbol16, symbol19)), Term.sequence(Literals.character('\"'), Term.always(symbol19, "\""))), results -> (String)results.getOrThrow(symbol19));
        Symbol symbol21 = Symbol.of("single_quoted_string_contents");
        parsingRules.set(symbol21, Term.repeated(parsingRuleEntry2, symbol18), results -> SnbtParsing.join((List)results.getOrThrow(symbol18)));
        Symbol symbol22 = Symbol.of("double_quoted_string_chunk");
        ParsingRuleEntry parsingRuleEntry3 = parsingRules.set(symbol22, Term.anyOf(parsingRules.term(symbol17, symbol19), Term.sequence(Literals.character('\\'), parsingRules.term(symbol16, symbol19)), Term.sequence(Literals.character('\''), Term.always(symbol19, "'"))), results -> (String)results.getOrThrow(symbol19));
        Symbol symbol23 = Symbol.of("double_quoted_string_contents");
        parsingRules.set(symbol23, Term.repeated(parsingRuleEntry3, symbol18), results -> SnbtParsing.join((List)results.getOrThrow(symbol18)));
        Symbol symbol24 = Symbol.of("quoted_string_literal");
        parsingRules.set(symbol24, Term.anyOf(Term.sequence(Literals.character('\"'), Term.cutting(), Term.optional(parsingRules.term(symbol23, symbol19)), Literals.character('\"')), Term.sequence(Literals.character('\''), Term.optional(parsingRules.term(symbol21, symbol19)), Literals.character('\''))), results -> (String)results.getOrThrow(symbol19));
        Symbol symbol25 = Symbol.of("unquoted_string");
        parsingRules.set(symbol25, new UnquotedStringParsingRule(1, EXPECTED_UNQUOTED_STRING_EXCEPTION));
        Symbol symbol26 = Symbol.of("literal");
        Symbol symbol27 = Symbol.of("arguments");
        parsingRules.set(symbol27, Term.repeatWithPossiblyTrailingSeparator(parsingRules.getOrCreate(symbol26), symbol27, Literals.character(',')), parseResults -> (List)parseResults.getOrThrow(symbol27));
        Symbol symbol28 = Symbol.of("unquoted_string_or_builtin");
        parsingRules.set(symbol28, Term.sequence(parsingRules.term(symbol25), Term.optional(Term.sequence(Literals.character('('), parsingRules.term(symbol27), Literals.character(')')))), state -> {
            ParseResults parseResults = state.getResults();
            String string = (String)parseResults.getOrThrow(symbol25);
            if (string.isEmpty() || !SnbtParsing.canUnquotedStringStartWith(string.charAt(0))) {
                state.getErrors().add(state.getCursor(), SnbtOperation.SUGGESTIONS, INVALID_UNQUOTED_START_EXCEPTION);
                return null;
            }
            List list = (List)parseResults.get(symbol27);
            if (list != null) {
                SnbtOperation.Type type = new SnbtOperation.Type(string, list.size());
                SnbtOperation.Operator operator = SnbtOperation.OPERATIONS.get(type);
                if (operator != null) {
                    return operator.apply(ops, list, state);
                }
                state.getErrors().add(state.getCursor(), CursorExceptionType.create(NO_SUCH_OPERATION_EXCEPTION, type.toString()));
                return null;
            }
            if (string.equalsIgnoreCase("true")) {
                return object;
            }
            if (string.equalsIgnoreCase("false")) {
                return object2;
            }
            return ops.createString(string);
        });
        Symbol symbol29 = Symbol.of("map_key");
        parsingRules.set(symbol29, Term.anyOf(parsingRules.term(symbol24), parsingRules.term(symbol25)), results -> (String)results.getAnyOrThrow(symbol24, symbol25));
        Symbol symbol30 = Symbol.of("map_entry");
        ParsingRuleEntry parsingRuleEntry4 = parsingRules.set(symbol30, Term.sequence(parsingRules.term(symbol29), Literals.character(':'), parsingRules.term(symbol26)), state -> {
            ParseResults parseResults = state.getResults();
            String string = (String)parseResults.getOrThrow(symbol29);
            if (string.isEmpty()) {
                state.getErrors().add(state.getCursor(), EMPTY_KEY_EXCEPTION);
                return null;
            }
            Object object = parseResults.getOrThrow(symbol26);
            return Map.entry(string, object);
        });
        Symbol symbol31 = Symbol.of("map_entries");
        parsingRules.set(symbol31, Term.repeatWithPossiblyTrailingSeparator(parsingRuleEntry4, symbol31, Literals.character(',')), results -> (List)results.getOrThrow(symbol31));
        Symbol symbol32 = Symbol.of("map_literal");
        parsingRules.set(symbol32, Term.sequence(Literals.character('{'), parsingRules.term(symbol31), Literals.character('}')), results -> {
            List list = (List)results.getOrThrow(symbol31);
            if (list.isEmpty()) {
                return object3;
            }
            ImmutableMap.Builder builder = ImmutableMap.builderWithExpectedSize((int)list.size());
            for (Map.Entry entry : list) {
                builder.put(ops.createString((String)entry.getKey()), entry.getValue());
            }
            return ops.createMap((Map)builder.buildKeepingLast());
        });
        Symbol symbol33 = Symbol.of("list_entries");
        parsingRules.set(symbol33, Term.repeatWithPossiblyTrailingSeparator(parsingRules.getOrCreate(symbol26), symbol33, Literals.character(',')), results -> (List)results.getOrThrow(symbol33));
        Symbol symbol34 = Symbol.of("array_prefix");
        parsingRules.set(symbol34, Term.anyOf(Term.sequence(Literals.character('B'), Term.always(symbol34, ArrayType.BYTE)), Term.sequence(Literals.character('L'), Term.always(symbol34, ArrayType.LONG)), Term.sequence(Literals.character('I'), Term.always(symbol34, ArrayType.INT))), results -> (ArrayType)((Object)((Object)results.getOrThrow(symbol34))));
        Symbol symbol35 = Symbol.of("int_array_entries");
        parsingRules.set(symbol35, Term.repeatWithPossiblyTrailingSeparator(parsingRuleEntry, symbol35, Literals.character(',')), results -> (List)results.getOrThrow(symbol35));
        Symbol symbol36 = Symbol.of("list_literal");
        parsingRules.set(symbol36, Term.sequence(Literals.character('['), Term.anyOf(Term.sequence(parsingRules.term(symbol34), Literals.character(';'), parsingRules.term(symbol35)), parsingRules.term(symbol33)), Literals.character(']')), state -> {
            ParseResults parseResults = state.getResults();
            ArrayType arrayType = (ArrayType)((Object)((Object)parseResults.get(symbol34)));
            if (arrayType != null) {
                List list = (List)parseResults.getOrThrow(symbol35);
                return list.isEmpty() ? arrayType.createEmpty(ops) : arrayType.decode(ops, list, state);
            }
            List list = (List)parseResults.getOrThrow(symbol33);
            return list.isEmpty() ? object4 : ops.createList(list.stream());
        });
        ParsingRuleEntry parsingRuleEntry5 = parsingRules.set(symbol26, Term.anyOf(Term.sequence(Term.positiveLookahead(DECIMAL_CHAR), Term.anyOf(parsingRules.term(symbol11, symbol26), parsingRules.term(symbol6))), Term.sequence(Term.positiveLookahead(Literals.character('\"', '\'')), Term.cutting(), parsingRules.term(symbol24)), Term.sequence(Term.positiveLookahead(Literals.character('{')), Term.cutting(), parsingRules.term(symbol32, symbol26)), Term.sequence(Term.positiveLookahead(Literals.character('[')), Term.cutting(), parsingRules.term(symbol36, symbol26)), parsingRules.term(symbol28, symbol26)), state -> {
            ParseResults parseResults = state.getResults();
            String string = (String)parseResults.get(symbol24);
            if (string != null) {
                return ops.createString(string);
            }
            IntValue intValue = (IntValue)parseResults.get(symbol6);
            if (intValue != null) {
                return intValue.decode(ops, state);
            }
            return parseResults.getOrThrow(symbol26);
        });
        return new PackratParser<Object>(parsingRules, parsingRuleEntry5);
    }

    static final class Sign
    extends Enum<Sign> {
        public static final /* enum */ Sign PLUS = new Sign();
        public static final /* enum */ Sign MINUS = new Sign();
        private static final /* synthetic */ Sign[] field_58016;

        public static Sign[] values() {
            return (Sign[])field_58016.clone();
        }

        public static Sign valueOf(String string) {
            return Enum.valueOf(Sign.class, string);
        }

        public void append(StringBuilder builder) {
            if (this == MINUS) {
                builder.append("-");
            }
        }

        private static /* synthetic */ Sign[] method_68653() {
            return new Sign[]{PLUS, MINUS};
        }

        static {
            field_58016 = Sign.method_68653();
        }
    }

    static final class SignedValue<T>
    extends Record {
        private final Sign sign;
        final T value;

        SignedValue(Sign sign, T value) {
            this.sign = sign;
            this.value = value;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SignedValue.class, "sign;value", "sign", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SignedValue.class, "sign;value", "sign", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SignedValue.class, "sign;value", "sign", "value"}, this, object);
        }

        public Sign sign() {
            return this.sign;
        }

        public T value() {
            return this.value;
        }
    }

    static final class NumericType
    extends Enum<NumericType> {
        public static final /* enum */ NumericType FLOAT = new NumericType();
        public static final /* enum */ NumericType DOUBLE = new NumericType();
        public static final /* enum */ NumericType BYTE = new NumericType();
        public static final /* enum */ NumericType SHORT = new NumericType();
        public static final /* enum */ NumericType INT = new NumericType();
        public static final /* enum */ NumericType LONG = new NumericType();
        private static final /* synthetic */ NumericType[] field_58026;

        public static NumericType[] values() {
            return (NumericType[])field_58026.clone();
        }

        public static NumericType valueOf(String string) {
            return Enum.valueOf(NumericType.class, string);
        }

        private static /* synthetic */ NumericType[] method_68656() {
            return new NumericType[]{FLOAT, DOUBLE, BYTE, SHORT, INT, LONG};
        }

        static {
            field_58026 = NumericType.method_68656();
        }
    }

    static final class NumberSuffix
    extends Record {
        final @Nullable Signedness signed;
        final @Nullable NumericType type;
        public static final NumberSuffix DEFAULT = new NumberSuffix(null, null);

        NumberSuffix(@Nullable Signedness signed, @Nullable NumericType type) {
            this.signed = signed;
            this.type = type;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NumberSuffix.class, "signed;type", "signed", "type"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NumberSuffix.class, "signed;type", "signed", "type"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NumberSuffix.class, "signed;type", "signed", "type"}, this, object);
        }

        public @Nullable Signedness signed() {
            return this.signed;
        }

        public @Nullable NumericType type() {
            return this.type;
        }
    }

    static final class Signedness
    extends Enum<Signedness> {
        public static final /* enum */ Signedness SIGNED = new Signedness();
        public static final /* enum */ Signedness UNSIGNED = new Signedness();
        private static final /* synthetic */ Signedness[] field_58019;

        public static Signedness[] values() {
            return (Signedness[])field_58019.clone();
        }

        public static Signedness valueOf(String string) {
            return Enum.valueOf(Signedness.class, string);
        }

        private static /* synthetic */ Signedness[] method_68655() {
            return new Signedness[]{SIGNED, UNSIGNED};
        }

        static {
            field_58019 = Signedness.method_68655();
        }
    }

    static class HexParsingRule
    extends TokenParsingRule {
        public HexParsingRule(int length) {
            super(length, length, CursorExceptionType.create(EXPECTED_HEX_ESCAPE_EXCEPTION, String.valueOf(length)));
        }

        @Override
        protected boolean isValidChar(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    }

    static abstract sealed class ArrayType
    extends Enum<ArrayType> {
        public static final /* enum */ ArrayType BYTE = new ArrayType(NumericType.BYTE, new NumericType[0]){
            private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

            @Override
            public <T> T createEmpty(DynamicOps<T> ops) {
                return (T)ops.createByteList(EMPTY_BUFFER);
            }

            @Override
            public <T> @Nullable T decode(DynamicOps<T> ops, List<IntValue> values, ParsingState<?> state) {
                ByteArrayList byteList = new ByteArrayList();
                for (IntValue intValue : values) {
                    Number number = this.decode(intValue, state);
                    if (number == null) {
                        return null;
                    }
                    byteList.add(number.byteValue());
                }
                return (T)ops.createByteList(ByteBuffer.wrap(byteList.toByteArray()));
            }
        };
        public static final /* enum */ ArrayType INT = new ArrayType(NumericType.INT, new NumericType[]{NumericType.BYTE, NumericType.SHORT}){

            @Override
            public <T> T createEmpty(DynamicOps<T> ops) {
                return (T)ops.createIntList(IntStream.empty());
            }

            @Override
            public <T> @Nullable T decode(DynamicOps<T> ops, List<IntValue> values, ParsingState<?> state) {
                IntStream.Builder builder = IntStream.builder();
                for (IntValue intValue : values) {
                    Number number = this.decode(intValue, state);
                    if (number == null) {
                        return null;
                    }
                    builder.add(number.intValue());
                }
                return (T)ops.createIntList(builder.build());
            }
        };
        public static final /* enum */ ArrayType LONG = new ArrayType(NumericType.LONG, new NumericType[]{NumericType.BYTE, NumericType.SHORT, NumericType.INT}){

            @Override
            public <T> T createEmpty(DynamicOps<T> ops) {
                return (T)ops.createLongList(LongStream.empty());
            }

            @Override
            public <T> @Nullable T decode(DynamicOps<T> ops, List<IntValue> values, ParsingState<?> state) {
                LongStream.Builder builder = LongStream.builder();
                for (IntValue intValue : values) {
                    Number number = this.decode(intValue, state);
                    if (number == null) {
                        return null;
                    }
                    builder.add(number.longValue());
                }
                return (T)ops.createLongList(builder.build());
            }
        };
        private final NumericType elementType;
        private final Set<NumericType> castableTypes;
        private static final /* synthetic */ ArrayType[] field_58007;

        public static ArrayType[] values() {
            return (ArrayType[])field_58007.clone();
        }

        public static ArrayType valueOf(String string) {
            return Enum.valueOf(ArrayType.class, string);
        }

        ArrayType(NumericType elementType, NumericType ... castableTypes) {
            this.castableTypes = Set.of(castableTypes);
            this.elementType = elementType;
        }

        public boolean isTypeAllowed(NumericType type) {
            return type == this.elementType || this.castableTypes.contains((Object)type);
        }

        public abstract <T> T createEmpty(DynamicOps<T> var1);

        public abstract <T> @Nullable T decode(DynamicOps<T> var1, List<IntValue> var2, ParsingState<?> var3);

        protected @Nullable Number decode(IntValue value, ParsingState<?> state) {
            NumericType numericType = this.getType(value.suffix);
            if (numericType == null) {
                state.getErrors().add(state.getCursor(), INVALID_ARRAY_ELEMENT_TYPE_EXCEPTION);
                return null;
            }
            return (Number)value.decode(JavaOps.INSTANCE, numericType, state);
        }

        private @Nullable NumericType getType(NumberSuffix suffix) {
            NumericType numericType = suffix.type();
            if (numericType == null) {
                return this.elementType;
            }
            if (!this.isTypeAllowed(numericType)) {
                return null;
            }
            return numericType;
        }

        private static /* synthetic */ ArrayType[] method_68642() {
            return new ArrayType[]{BYTE, INT, LONG};
        }

        static {
            field_58007 = ArrayType.method_68642();
        }
    }

    static final class IntValue
    extends Record {
        private final Sign sign;
        private final Radix base;
        private final String digits;
        final NumberSuffix suffix;

        IntValue(Sign sign, Radix base, String digits, NumberSuffix suffix) {
            this.sign = sign;
            this.base = base;
            this.digits = digits;
            this.suffix = suffix;
        }

        private Signedness getSignedness() {
            if (this.suffix.signed != null) {
                return this.suffix.signed;
            }
            return switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> Signedness.UNSIGNED;
                case 1 -> Signedness.SIGNED;
            };
        }

        private String toString(Sign sign) {
            boolean bl = SnbtParsing.containsUnderscore(this.digits);
            if (sign == Sign.MINUS || bl) {
                StringBuilder stringBuilder = new StringBuilder();
                sign.append(stringBuilder);
                SnbtParsing.append(stringBuilder, this.digits, bl);
                return stringBuilder.toString();
            }
            return this.digits;
        }

        public <T> @Nullable T decode(DynamicOps<T> ops, ParsingState<?> state) {
            return this.decode(ops, Objects.requireNonNullElse(this.suffix.type, NumericType.INT), state);
        }

        public <T> @Nullable T decode(DynamicOps<T> ops, NumericType type, ParsingState<?> state) {
            boolean bl;
            boolean bl2 = bl = this.getSignedness() == Signedness.SIGNED;
            if (!bl && this.sign == Sign.MINUS) {
                state.getErrors().add(state.getCursor(), EXPECTED_NON_NEGATIVE_NUMBER_EXCEPTION);
                return null;
            }
            String string = this.toString(this.sign);
            int i = switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 2;
                case 1 -> 10;
                case 2 -> 16;
            };
            try {
                if (bl) {
                    return (T)(switch (type.ordinal()) {
                        case 2 -> ops.createByte(Byte.parseByte(string, i));
                        case 3 -> ops.createShort(Short.parseShort(string, i));
                        case 4 -> ops.createInt(Integer.parseInt(string, i));
                        case 5 -> ops.createLong(Long.parseLong(string, i));
                        default -> {
                            state.getErrors().add(state.getCursor(), EXPECTED_INTEGER_TYPE_EXCEPTION);
                            yield null;
                        }
                    });
                }
                return (T)(switch (type.ordinal()) {
                    case 2 -> ops.createByte(UnsignedBytes.parseUnsignedByte((String)string, (int)i));
                    case 3 -> ops.createShort(SnbtParsing.parseUnsignedShort(string, i));
                    case 4 -> ops.createInt(Integer.parseUnsignedInt(string, i));
                    case 5 -> ops.createLong(Long.parseUnsignedLong(string, i));
                    default -> {
                        state.getErrors().add(state.getCursor(), EXPECTED_INTEGER_TYPE_EXCEPTION);
                        yield null;
                    }
                });
            }
            catch (NumberFormatException numberFormatException) {
                state.getErrors().add(state.getCursor(), SnbtParsing.toNumberParseFailure(numberFormatException));
                return null;
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntValue.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntValue.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntValue.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this, object);
        }

        public Sign sign() {
            return this.sign;
        }

        public Radix base() {
            return this.base;
        }

        public String digits() {
            return this.digits;
        }

        public NumberSuffix suffix() {
            return this.suffix;
        }
    }

    static final class Radix
    extends Enum<Radix> {
        public static final /* enum */ Radix BINARY = new Radix();
        public static final /* enum */ Radix DECIMAL = new Radix();
        public static final /* enum */ Radix HEX = new Radix();
        private static final /* synthetic */ Radix[] field_58012;

        public static Radix[] values() {
            return (Radix[])field_58012.clone();
        }

        public static Radix valueOf(String string) {
            return Enum.valueOf(Radix.class, string);
        }

        private static /* synthetic */ Radix[] method_68648() {
            return new Radix[]{BINARY, DECIMAL, HEX};
        }

        static {
            field_58012 = Radix.method_68648();
        }
    }
}

