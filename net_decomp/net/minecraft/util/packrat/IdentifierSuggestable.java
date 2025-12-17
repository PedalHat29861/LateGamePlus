/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Suggestable;

public interface IdentifierSuggestable
extends Suggestable<StringReader> {
    public Stream<Identifier> possibleIds();

    @Override
    default public Stream<String> possibleValues(ParsingState<StringReader> parsingState) {
        return this.possibleIds().map(Identifier::toString);
    }
}

