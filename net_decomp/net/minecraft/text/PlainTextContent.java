/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;

public interface PlainTextContent
extends TextContent {
    public static final MapCodec<PlainTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("text").forGetter(PlainTextContent::string)).apply((Applicative)instance, PlainTextContent::of));
    public static final PlainTextContent EMPTY = new PlainTextContent(){

        public String toString() {
            return "empty";
        }

        @Override
        public String string() {
            return "";
        }
    };

    public static PlainTextContent of(String string) {
        return string.isEmpty() ? EMPTY : new Literal(string);
    }

    public String string();

    default public MapCodec<PlainTextContent> getCodec() {
        return CODEC;
    }

    public record Literal(String string) implements PlainTextContent
    {
        @Override
        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
            return visitor.accept(this.string);
        }

        @Override
        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
            return visitor.accept(style, this.string);
        }

        @Override
        public String toString() {
            return "literal{" + this.string + "}";
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Literal.class, "text", "string"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Literal.class, "text", "string"}, this, object);
        }
    }
}

