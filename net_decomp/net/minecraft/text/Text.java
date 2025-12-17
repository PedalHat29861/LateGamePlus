/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.util.Either;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtDataSource;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.ObjectTextContent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.ParsedSelector;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.text.object.TextObjectContents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.jspecify.annotations.Nullable;

public interface Text
extends Message,
StringVisitable {
    public Style getStyle();

    public TextContent getContent();

    @Override
    default public String getString() {
        return StringVisitable.super.getString();
    }

    default public String asTruncatedString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int j = length - stringBuilder.length();
            if (j <= 0) {
                return TERMINATE_VISIT;
            }
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Text> getSiblings();

    default public @Nullable String getLiteralString() {
        TextContent textContent = this.getContent();
        if (textContent instanceof PlainTextContent) {
            PlainTextContent plainTextContent = (PlainTextContent)textContent;
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return plainTextContent.string();
            }
        }
        return null;
    }

    default public MutableText copyContentOnly() {
        return MutableText.of(this.getContent());
    }

    default public MutableText copy() {
        return new MutableText(this.getContent(), new ArrayList<Text>(this.getSiblings()), this.getStyle());
    }

    public OrderedText asOrderedText();

    @Override
    default public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> styledVisitor, Style style) {
        Style style2 = this.getStyle().withParent(style);
        Optional<T> optional = this.getContent().visit(styledVisitor, style2);
        if (optional.isPresent()) {
            return optional;
        }
        for (Text text : this.getSiblings()) {
            Optional<T> optional2 = text.visit(styledVisitor, style2);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        Optional<T> optional = this.getContent().visit(visitor);
        if (optional.isPresent()) {
            return optional;
        }
        for (Text text : this.getSiblings()) {
            Optional<T> optional2 = text.visit(visitor);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public List<Text> withoutStyle() {
        return this.getWithStyle(Style.EMPTY);
    }

    default public List<Text> getWithStyle(Style style) {
        ArrayList list = Lists.newArrayList();
        this.visit((styleOverride, text) -> {
            if (!text.isEmpty()) {
                list.add(Text.literal(text).fillStyle(styleOverride));
            }
            return Optional.empty();
        }, style);
        return list;
    }

    default public boolean contains(Text text) {
        List<Text> list2;
        if (this.equals(text)) {
            return true;
        }
        List<Text> list = this.withoutStyle();
        return Collections.indexOfSubList(list, list2 = text.getWithStyle(this.getStyle())) != -1;
    }

    public static Text of(@Nullable String string) {
        return string != null ? Text.literal(string) : ScreenTexts.EMPTY;
    }

    public static MutableText literal(String string) {
        return MutableText.of(PlainTextContent.of(string));
    }

    public static MutableText translatable(String key) {
        return MutableText.of(new TranslatableTextContent(key, null, TranslatableTextContent.EMPTY_ARGUMENTS));
    }

    public static MutableText translatable(String key, Object ... args) {
        return MutableText.of(new TranslatableTextContent(key, null, args));
    }

    public static MutableText stringifiedTranslatable(String key, Object ... args) {
        for (int i = 0; i < args.length; ++i) {
            Object object = args[i];
            if (TranslatableTextContent.isPrimitive(object) || object instanceof Text) continue;
            args[i] = String.valueOf(object);
        }
        return Text.translatable(key, args);
    }

    public static MutableText translatableWithFallback(String key, @Nullable String fallback) {
        return MutableText.of(new TranslatableTextContent(key, fallback, TranslatableTextContent.EMPTY_ARGUMENTS));
    }

    public static MutableText translatableWithFallback(String key, @Nullable String fallback, Object ... args) {
        return MutableText.of(new TranslatableTextContent(key, fallback, args));
    }

    public static MutableText empty() {
        return MutableText.of(PlainTextContent.EMPTY);
    }

    public static MutableText keybind(String string) {
        return MutableText.of(new KeybindTextContent(string));
    }

    public static MutableText nbt(String rawPath, boolean interpret, Optional<Text> separator, NbtDataSource dataSource) {
        return MutableText.of(new NbtTextContent(rawPath, interpret, separator, dataSource));
    }

    public static MutableText score(ParsedSelector selector, String objective) {
        return MutableText.of(new ScoreTextContent((Either<ParsedSelector, String>)Either.left((Object)selector), objective));
    }

    public static MutableText score(String name, String objective) {
        return MutableText.of(new ScoreTextContent((Either<ParsedSelector, String>)Either.right((Object)name), objective));
    }

    public static MutableText selector(ParsedSelector selector, Optional<Text> separator) {
        return MutableText.of(new SelectorTextContent(selector, separator));
    }

    public static MutableText object(TextObjectContents object) {
        return MutableText.of(new ObjectTextContent(object));
    }

    public static Text of(Date date) {
        return Text.literal(date.toString());
    }

    public static Text of(Message message) {
        Text text;
        if (message instanceof Text) {
            Text text2 = (Text)message;
            text = text2;
        } else {
            text = Text.literal(message.getString());
        }
        return text;
    }

    public static Text of(UUID uuid) {
        return Text.literal(uuid.toString());
    }

    public static Text of(Identifier id) {
        return Text.literal(id.toString());
    }

    public static Text of(ChunkPos pos) {
        return Text.literal(pos.toString());
    }

    public static Text of(URI uri) {
        return Text.literal(uri.toString());
    }
}

