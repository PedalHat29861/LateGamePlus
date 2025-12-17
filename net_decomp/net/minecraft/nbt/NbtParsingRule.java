/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;
import org.jspecify.annotations.Nullable;

public class NbtParsingRule<T>
implements ParsingRule<StringReader, Dynamic<?>> {
    private final StringNbtReader<T> nbtReader;

    public NbtParsingRule(DynamicOps<T> ops) {
        this.nbtReader = StringNbtReader.fromOps(ops);
    }

    @Override
    public @Nullable Dynamic<T> parse(ParsingState<StringReader> parsingState) {
        parsingState.getReader().skipWhitespace();
        int i = parsingState.getCursor();
        try {
            return new Dynamic(this.nbtReader.getOps(), this.nbtReader.readAsArgument(parsingState.getReader()));
        }
        catch (Exception exception) {
            parsingState.getErrors().add(i, exception);
            return null;
        }
    }

    @Override
    public /* synthetic */ @Nullable Object parse(ParsingState state) {
        return this.parse((ParsingState<StringReader>)state);
    }
}

