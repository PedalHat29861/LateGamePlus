/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;
import net.minecraft.text.object.TextObjectContentTypes;
import net.minecraft.text.object.TextObjectContents;

public record ObjectTextContent(TextObjectContents contents) implements TextContent
{
    private static final String REPLACEMENT = Character.toString('\ufffc');
    public static final MapCodec<ObjectTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextObjectContentTypes.CODEC.forGetter(ObjectTextContent::contents)).apply((Applicative)instance, ObjectTextContent::new));

    public MapCodec<ObjectTextContent> getCodec() {
        return CODEC;
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return visitor.accept(this.contents.asText());
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return visitor.accept(style.withFont(this.contents.spriteSource()), REPLACEMENT);
    }
}

