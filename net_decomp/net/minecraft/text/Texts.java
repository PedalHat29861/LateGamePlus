/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixUtils
 *  javax.annotation.CheckReturnValue
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.CheckReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import org.jspecify.annotations.Nullable;

public class Texts {
    public static final String DEFAULT_SEPARATOR = ", ";
    public static final Text GRAY_DEFAULT_SEPARATOR_TEXT = Text.literal(", ").formatted(Formatting.GRAY);
    public static final Text DEFAULT_SEPARATOR_TEXT = Text.literal(", ");

    @CheckReturnValue
    public static MutableText setStyleIfAbsent(MutableText text, Style style) {
        if (style.isEmpty()) {
            return text;
        }
        Style style2 = text.getStyle();
        if (style2.isEmpty()) {
            return text.setStyle(style);
        }
        if (style2.equals(style)) {
            return text;
        }
        return text.setStyle(style2.withParent(style));
    }

    @CheckReturnValue
    public static Text withStyle(Text text, Style style) {
        if (style.isEmpty()) {
            return text;
        }
        Style style2 = text.getStyle();
        if (style2.isEmpty()) {
            return text.copy().setStyle(style);
        }
        if (style2.equals(style)) {
            return text;
        }
        return text.copy().setStyle(style2.withParent(style));
    }

    public static Optional<MutableText> parse(@Nullable ServerCommandSource source, Optional<Text> text, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        return text.isPresent() ? Optional.of(Texts.parse(source, text.get(), sender, depth)) : Optional.empty();
    }

    public static MutableText parse(@Nullable ServerCommandSource source, Text text, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        if (depth > 100) {
            return text.copy();
        }
        MutableText mutableText = text.getContent().parse(source, sender, depth + 1);
        for (Text text2 : text.getSiblings()) {
            mutableText.append(Texts.parse(source, text2, sender, depth + 1));
        }
        return mutableText.fillStyle(Texts.parseStyle(source, text.getStyle(), sender, depth));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static Style parseStyle(@Nullable ServerCommandSource source, Style style, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (!(hoverEvent instanceof HoverEvent.ShowText)) return style;
        HoverEvent.ShowText showText = (HoverEvent.ShowText)hoverEvent;
        try {
            Text text;
            Text text2 = text = showText.value();
            HoverEvent.ShowText hoverEvent2 = new HoverEvent.ShowText(Texts.parse(source, text2, sender, depth + 1));
            return style.withHoverEvent(hoverEvent2);
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public static Text joinOrdered(Collection<String> strings) {
        return Texts.joinOrdered(strings, string -> Text.literal(string).formatted(Formatting.GREEN));
    }

    public static <T extends Comparable<T>> Text joinOrdered(Collection<T> elements, Function<T, Text> transformer) {
        if (elements.isEmpty()) {
            return ScreenTexts.EMPTY;
        }
        if (elements.size() == 1) {
            return transformer.apply((Comparable)elements.iterator().next());
        }
        ArrayList list = Lists.newArrayList(elements);
        list.sort(Comparable::compareTo);
        return Texts.join(list, transformer);
    }

    public static <T> Text join(Collection<? extends T> elements, Function<T, Text> transformer) {
        return Texts.join(elements, GRAY_DEFAULT_SEPARATOR_TEXT, transformer);
    }

    public static <T> MutableText join(Collection<? extends T> elements, Optional<? extends Text> separator, Function<T, Text> transformer) {
        return Texts.join(elements, (Text)DataFixUtils.orElse(separator, (Object)GRAY_DEFAULT_SEPARATOR_TEXT), transformer);
    }

    public static Text join(Collection<? extends Text> texts, Text separator) {
        return Texts.join(texts, separator, Function.identity());
    }

    public static <T> MutableText join(Collection<? extends T> elements, Text separator, Function<T, Text> transformer) {
        if (elements.isEmpty()) {
            return Text.empty();
        }
        if (elements.size() == 1) {
            return transformer.apply(elements.iterator().next()).copy();
        }
        MutableText mutableText = Text.empty();
        boolean bl = true;
        for (T object : elements) {
            if (!bl) {
                mutableText.append(separator);
            }
            mutableText.append(transformer.apply(object));
            bl = false;
        }
        return mutableText;
    }

    public static MutableText bracketed(Text text) {
        return Text.translatable("chat.square_brackets", text);
    }

    public static Text toText(Message message) {
        if (message instanceof Text) {
            Text text = (Text)message;
            return text;
        }
        return Text.literal(message.getString());
    }

    public static boolean hasTranslation(@Nullable Text text) {
        TextContent textContent;
        if (text != null && (textContent = text.getContent()) instanceof TranslatableTextContent) {
            TranslatableTextContent translatableTextContent = (TranslatableTextContent)textContent;
            String string = translatableTextContent.getKey();
            String string2 = translatableTextContent.getFallback();
            return string2 != null || Language.getInstance().hasTranslation(string);
        }
        return true;
    }

    public static MutableText bracketedCopyable(String string) {
        return Texts.bracketed(Text.literal(string).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent.CopyToClipboard(string)).withHoverEvent(new HoverEvent.ShowText(Text.translatable("chat.copy.click"))).withInsertion(string)));
    }
}

