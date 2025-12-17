/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.Nullable;

public final class TextColor {
    private static final String RGB_PREFIX = "#";
    public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(TextColor::parse, TextColor::getName);
    private static final Map<Formatting, TextColor> FORMATTING_TO_COLOR = (Map)Stream.of(Formatting.values()).filter(Formatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), formatting -> new TextColor(formatting.getColorValue(), formatting.getName())));
    private static final Map<String, TextColor> BY_NAME = (Map)FORMATTING_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap(textColor -> textColor.name, Function.identity()));
    private final int rgb;
    private final @Nullable String name;

    private TextColor(int rgb, String name) {
        this.rgb = rgb & 0xFFFFFF;
        this.name = name;
    }

    private TextColor(int rgb) {
        this.rgb = rgb & 0xFFFFFF;
        this.name = null;
    }

    public int getRgb() {
        return this.rgb;
    }

    public String getName() {
        return this.name != null ? this.name : this.getHexCode();
    }

    private String getHexCode() {
        return String.format(Locale.ROOT, "#%06X", this.rgb);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TextColor textColor = (TextColor)o;
        return this.rgb == textColor.rgb;
    }

    public int hashCode() {
        return Objects.hash(this.rgb, this.name);
    }

    public String toString() {
        return this.getName();
    }

    public static @Nullable TextColor fromFormatting(Formatting formatting) {
        return FORMATTING_TO_COLOR.get(formatting);
    }

    public static TextColor fromRgb(int rgb) {
        return new TextColor(rgb);
    }

    public static DataResult<TextColor> parse(String name) {
        if (name.startsWith(RGB_PREFIX)) {
            try {
                int i = Integer.parseInt(name.substring(1), 16);
                if (i < 0 || i > 0xFFFFFF) {
                    return DataResult.error(() -> "Color value out of range: " + name);
                }
                return DataResult.success((Object)TextColor.fromRgb(i), (Lifecycle)Lifecycle.stable());
            }
            catch (NumberFormatException numberFormatException) {
                return DataResult.error(() -> "Invalid color value: " + name);
            }
        }
        TextColor textColor = BY_NAME.get(name);
        if (textColor == null) {
            return DataResult.error(() -> "Invalid color name: " + name);
        }
        return DataResult.success((Object)textColor, (Lifecycle)Lifecycle.stable());
    }
}

