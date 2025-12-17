/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.packrat;

import java.util.stream.Stream;
import net.minecraft.util.packrat.ParsingState;

public interface Suggestable<S> {
    public Stream<String> possibleValues(ParsingState<S> var1);

    public static <S> Suggestable<S> empty() {
        return state -> Stream.empty();
    }
}

