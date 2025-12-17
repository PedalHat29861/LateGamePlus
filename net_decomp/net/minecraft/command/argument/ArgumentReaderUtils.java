/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import net.minecraft.util.function.CharPredicate;

public class ArgumentReaderUtils {
    public static String readWhileMatching(StringReader stringReader, CharPredicate predicate) {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && predicate.test(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(i, stringReader.getCursor());
    }
}

