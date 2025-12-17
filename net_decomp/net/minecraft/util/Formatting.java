/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class Formatting
extends Enum<Formatting>
implements StringIdentifiable {
    public static final /* enum */ Formatting BLACK = new Formatting("BLACK", '0', 0, 0);
    public static final /* enum */ Formatting DARK_BLUE = new Formatting("DARK_BLUE", '1', 1, 170);
    public static final /* enum */ Formatting DARK_GREEN = new Formatting("DARK_GREEN", '2', 2, 43520);
    public static final /* enum */ Formatting DARK_AQUA = new Formatting("DARK_AQUA", '3', 3, 43690);
    public static final /* enum */ Formatting DARK_RED = new Formatting("DARK_RED", '4', 4, 0xAA0000);
    public static final /* enum */ Formatting DARK_PURPLE = new Formatting("DARK_PURPLE", '5', 5, 0xAA00AA);
    public static final /* enum */ Formatting GOLD = new Formatting("GOLD", '6', 6, 0xFFAA00);
    public static final /* enum */ Formatting GRAY = new Formatting("GRAY", '7', 7, 0xAAAAAA);
    public static final /* enum */ Formatting DARK_GRAY = new Formatting("DARK_GRAY", '8', 8, 0x555555);
    public static final /* enum */ Formatting BLUE = new Formatting("BLUE", '9', 9, 0x5555FF);
    public static final /* enum */ Formatting GREEN = new Formatting("GREEN", 'a', 10, 0x55FF55);
    public static final /* enum */ Formatting AQUA = new Formatting("AQUA", 'b', 11, 0x55FFFF);
    public static final /* enum */ Formatting RED = new Formatting("RED", 'c', 12, 0xFF5555);
    public static final /* enum */ Formatting LIGHT_PURPLE = new Formatting("LIGHT_PURPLE", 'd', 13, 0xFF55FF);
    public static final /* enum */ Formatting YELLOW = new Formatting("YELLOW", 'e', 14, 0xFFFF55);
    public static final /* enum */ Formatting WHITE = new Formatting("WHITE", 'f', 15, 0xFFFFFF);
    public static final /* enum */ Formatting OBFUSCATED = new Formatting("OBFUSCATED", 'k', true);
    public static final /* enum */ Formatting BOLD = new Formatting("BOLD", 'l', true);
    public static final /* enum */ Formatting STRIKETHROUGH = new Formatting("STRIKETHROUGH", 'm', true);
    public static final /* enum */ Formatting UNDERLINE = new Formatting("UNDERLINE", 'n', true);
    public static final /* enum */ Formatting ITALIC = new Formatting("ITALIC", 'o', true);
    public static final /* enum */ Formatting RESET = new Formatting("RESET", 'r', -1, null);
    public static final Codec<Formatting> CODEC;
    public static final Codec<Formatting> COLOR_CODEC;
    public static final char FORMATTING_CODE_PREFIX = '\u00a7';
    private static final Map<String, Formatting> BY_NAME;
    private static final Pattern FORMATTING_CODE_PATTERN;
    private final String name;
    private final char code;
    private final boolean modifier;
    private final String stringValue;
    private final int colorIndex;
    private final @Nullable Integer colorValue;
    private static final /* synthetic */ Formatting[] field_1072;

    public static Formatting[] values() {
        return (Formatting[])field_1072.clone();
    }

    public static Formatting valueOf(String string) {
        return Enum.valueOf(Formatting.class, string);
    }

    private static String sanitize(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private Formatting(String name, @Nullable char code, int colorIndex, Integer colorValue) {
        this(name, code, false, colorIndex, colorValue);
    }

    private Formatting(String name, char code, boolean modifier) {
        this(name, code, modifier, -1, null);
    }

    private Formatting(String name, char code, @Nullable boolean modifier, int colorIndex, Integer colorValue) {
        this.name = name;
        this.code = code;
        this.modifier = modifier;
        this.colorIndex = colorIndex;
        this.colorValue = colorValue;
        this.stringValue = "\u00a7" + String.valueOf(code);
    }

    public char getCode() {
        return this.code;
    }

    public int getColorIndex() {
        return this.colorIndex;
    }

    public boolean isModifier() {
        return this.modifier;
    }

    public boolean isColor() {
        return !this.modifier && this != RESET;
    }

    public @Nullable Integer getColorValue() {
        return this.colorValue;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String toString() {
        return this.stringValue;
    }

    @Contract(value="!null->!null;_->_")
    public static @Nullable String strip(@Nullable String string) {
        return string == null ? null : FORMATTING_CODE_PATTERN.matcher(string).replaceAll("");
    }

    public static @Nullable Formatting byName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return BY_NAME.get(Formatting.sanitize(name));
    }

    public static @Nullable Formatting byColorIndex(int colorIndex) {
        if (colorIndex < 0) {
            return RESET;
        }
        for (Formatting formatting : Formatting.values()) {
            if (formatting.getColorIndex() != colorIndex) continue;
            return formatting;
        }
        return null;
    }

    public static @Nullable Formatting byCode(char code) {
        char c = Character.toLowerCase(code);
        for (Formatting formatting : Formatting.values()) {
            if (formatting.code != c) continue;
            return formatting;
        }
        return null;
    }

    public static Collection<String> getNames(boolean colors, boolean modifiers) {
        ArrayList list = Lists.newArrayList();
        for (Formatting formatting : Formatting.values()) {
            if (formatting.isColor() && !colors || formatting.isModifier() && !modifiers) continue;
            list.add(formatting.getName());
        }
        return list;
    }

    @Override
    public String asString() {
        return this.getName();
    }

    private static /* synthetic */ Formatting[] method_36946() {
        return new Formatting[]{BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE, OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET};
    }

    static {
        field_1072 = Formatting.method_36946();
        CODEC = StringIdentifiable.createCodec(Formatting::values);
        COLOR_CODEC = CODEC.validate(formatting -> formatting.isModifier() ? DataResult.error(() -> "Formatting was not a valid color: " + String.valueOf(formatting)) : DataResult.success((Object)formatting));
        BY_NAME = Arrays.stream(Formatting.values()).collect(Collectors.toMap(f -> Formatting.sanitize(f.name), f -> f));
        FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
    }
}

