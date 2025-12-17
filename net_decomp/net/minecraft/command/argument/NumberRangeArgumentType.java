/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;

public interface NumberRangeArgumentType<T extends NumberRange<?>>
extends ArgumentType<T> {
    public static IntRangeArgumentType intRange() {
        return new IntRangeArgumentType();
    }

    public static FloatRangeArgumentType floatRange() {
        return new FloatRangeArgumentType();
    }

    public static class IntRangeArgumentType
    implements NumberRangeArgumentType<NumberRange.IntRange> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5", "0", "-5", "-100..", "..100");

        public static NumberRange.IntRange getRangeArgument(CommandContext<ServerCommandSource> context, String name) {
            return (NumberRange.IntRange)context.getArgument(name, NumberRange.IntRange.class);
        }

        public NumberRange.IntRange parse(StringReader stringReader) throws CommandSyntaxException {
            return NumberRange.IntRange.parse(stringReader);
        }

        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
            return this.parse(reader);
        }
    }

    public static class FloatRangeArgumentType
    implements NumberRangeArgumentType<NumberRange.DoubleRange> {
        private static final Collection<String> EXAMPLES = Arrays.asList("0..5.2", "0", "-5.4", "-100.76..", "..100");

        public static NumberRange.DoubleRange getRangeArgument(CommandContext<ServerCommandSource> context, String name) {
            return (NumberRange.DoubleRange)context.getArgument(name, NumberRange.DoubleRange.class);
        }

        public NumberRange.DoubleRange parse(StringReader stringReader) throws CommandSyntaxException {
            return NumberRange.DoubleRange.parse(stringReader);
        }

        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
            return this.parse(reader);
        }
    }
}

