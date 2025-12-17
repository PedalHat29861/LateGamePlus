/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.Nullable;

public final class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null, null);
    public static final int field_63933 = 0;
    final @Nullable TextColor color;
    final @Nullable Integer shadowColor;
    final @Nullable Boolean bold;
    final @Nullable Boolean italic;
    final @Nullable Boolean underlined;
    final @Nullable Boolean strikethrough;
    final @Nullable Boolean obfuscated;
    final @Nullable ClickEvent clickEvent;
    final @Nullable HoverEvent hoverEvent;
    final @Nullable String insertion;
    final @Nullable StyleSpriteSource font;

    private static Style of(Optional<TextColor> color, Optional<Integer> shadowColor, Optional<Boolean> bold, Optional<Boolean> italic, Optional<Boolean> underlined, Optional<Boolean> strikethrough, Optional<Boolean> obfuscated, Optional<ClickEvent> clickEvent, Optional<HoverEvent> hoverEvent, Optional<String> insertion, Optional<StyleSpriteSource> font) {
        Style style = new Style(color.orElse(null), shadowColor.orElse(null), bold.orElse(null), italic.orElse(null), underlined.orElse(null), strikethrough.orElse(null), obfuscated.orElse(null), clickEvent.orElse(null), hoverEvent.orElse(null), insertion.orElse(null), font.orElse(null));
        if (style.equals(EMPTY)) {
            return EMPTY;
        }
        return style;
    }

    private Style(@Nullable TextColor color, @Nullable Integer shadowColor, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough, @Nullable Boolean obfuscated, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String insertion, @Nullable StyleSpriteSource font) {
        this.color = color;
        this.shadowColor = shadowColor;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.insertion = insertion;
        this.font = font;
    }

    public @Nullable TextColor getColor() {
        return this.color;
    }

    public @Nullable Integer getShadowColor() {
        return this.shadowColor;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public @Nullable ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public @Nullable HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    public @Nullable String getInsertion() {
        return this.insertion;
    }

    public StyleSpriteSource getFont() {
        return this.font != null ? this.font : StyleSpriteSource.DEFAULT;
    }

    private static <T> Style with(Style newStyle, @Nullable T oldAttribute, @Nullable T newAttribute) {
        if (oldAttribute != null && newAttribute == null && newStyle.equals(EMPTY)) {
            return EMPTY;
        }
        return newStyle;
    }

    public Style withColor(@Nullable TextColor color) {
        if (Objects.equals(this.color, color)) {
            return this;
        }
        return Style.with(new Style(color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.color, color);
    }

    public Style withColor(@Nullable Formatting color) {
        return this.withColor(color != null ? TextColor.fromFormatting(color) : null);
    }

    public Style withColor(int rgbColor) {
        return this.withColor(TextColor.fromRgb(rgbColor));
    }

    public Style withShadowColor(int shadowColor) {
        if (Objects.equals(this.shadowColor, shadowColor)) {
            return this;
        }
        return Style.with(new Style(this.color, shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.shadowColor, shadowColor);
    }

    public Style withoutShadow() {
        return this.withShadowColor(0);
    }

    public Style withBold(@Nullable Boolean bold) {
        if (Objects.equals(this.bold, bold)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.bold, bold);
    }

    public Style withItalic(@Nullable Boolean italic) {
        if (Objects.equals(this.italic, italic)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.italic, italic);
    }

    public Style withUnderline(@Nullable Boolean underline) {
        if (Objects.equals(this.underlined, underline)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, underline, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.underlined, underline);
    }

    public Style withStrikethrough(@Nullable Boolean strikethrough) {
        if (Objects.equals(this.strikethrough, strikethrough)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.strikethrough, strikethrough);
    }

    public Style withObfuscated(@Nullable Boolean obfuscated) {
        if (Objects.equals(this.obfuscated, obfuscated)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.obfuscated, obfuscated);
    }

    public Style withClickEvent(@Nullable ClickEvent clickEvent) {
        if (Objects.equals(this.clickEvent, clickEvent)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font), this.clickEvent, clickEvent);
    }

    public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
        if (Objects.equals(this.hoverEvent, hoverEvent)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font), this.hoverEvent, hoverEvent);
    }

    public Style withInsertion(@Nullable String insertion) {
        if (Objects.equals(this.insertion, insertion)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, insertion, this.font), this.insertion, insertion);
    }

    public Style withFont(@Nullable StyleSpriteSource font) {
        if (Objects.equals(this.font, font)) {
            return this;
        }
        return Style.with(new Style(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, font), this.font, font);
    }

    public Style withFormatting(Formatting formatting) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                textColor = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(textColor, this.shadowColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withExclusiveFormatting(Formatting formatting) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (formatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                boolean5 = false;
                boolean_ = false;
                boolean3 = false;
                boolean4 = false;
                boolean2 = false;
                textColor = TextColor.fromFormatting(formatting);
            }
        }
        return new Style(textColor, this.shadowColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withFormatting(Formatting ... formattings) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        block8: for (Formatting formatting : formattings) {
            switch (formatting) {
                case OBFUSCATED: {
                    boolean5 = true;
                    continue block8;
                }
                case BOLD: {
                    boolean_ = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    boolean3 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    boolean4 = true;
                    continue block8;
                }
                case ITALIC: {
                    boolean2 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    textColor = TextColor.fromFormatting(formatting);
                }
            }
        }
        return new Style(textColor, this.shadowColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withParent(Style parent) {
        if (this == EMPTY) {
            return parent;
        }
        if (parent == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : parent.color, this.shadowColor != null ? this.shadowColor : parent.shadowColor, this.bold != null ? this.bold : parent.bold, this.italic != null ? this.italic : parent.italic, this.underlined != null ? this.underlined : parent.underlined, this.strikethrough != null ? this.strikethrough : parent.strikethrough, this.obfuscated != null ? this.obfuscated : parent.obfuscated, this.clickEvent != null ? this.clickEvent : parent.clickEvent, this.hoverEvent != null ? this.hoverEvent : parent.hoverEvent, this.insertion != null ? this.insertion : parent.insertion, this.font != null ? this.font : parent.font);
    }

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("{");
        class Writer {
            private boolean shouldAppendComma;

            Writer() {
            }

            private void appendComma() {
                if (this.shouldAppendComma) {
                    stringBuilder.append(',');
                }
                this.shouldAppendComma = true;
            }

            void append(String key, @Nullable Boolean value) {
                if (value != null) {
                    this.appendComma();
                    if (!value.booleanValue()) {
                        stringBuilder.append('!');
                    }
                    stringBuilder.append(key);
                }
            }

            void append(String key, @Nullable Object value) {
                if (value != null) {
                    this.appendComma();
                    stringBuilder.append(key);
                    stringBuilder.append('=');
                    stringBuilder.append(value);
                }
            }
        }
        Writer writer = new Writer();
        writer.append("color", this.color);
        writer.append("shadowColor", this.shadowColor);
        writer.append("bold", this.bold);
        writer.append("italic", this.italic);
        writer.append("underlined", this.underlined);
        writer.append("strikethrough", this.strikethrough);
        writer.append("obfuscated", this.obfuscated);
        writer.append("clickEvent", this.clickEvent);
        writer.append("hoverEvent", this.hoverEvent);
        writer.append("insertion", this.insertion);
        writer.append("font", this.font);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Style) {
            Style style = (Style)o;
            return this.bold == style.bold && Objects.equals(this.getColor(), style.getColor()) && Objects.equals(this.getShadowColor(), style.getShadowColor()) && this.italic == style.italic && this.obfuscated == style.obfuscated && this.strikethrough == style.strikethrough && this.underlined == style.underlined && Objects.equals(this.clickEvent, style.clickEvent) && Objects.equals(this.hoverEvent, style.hoverEvent) && Objects.equals(this.insertion, style.insertion) && Objects.equals(this.font, style.font);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.shadowColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Codecs {
        public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextColor.CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.color)), (App)net.minecraft.util.dynamic.Codecs.ARGB.optionalFieldOf("shadow_color").forGetter(style -> Optional.ofNullable(style.shadowColor)), (App)Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)), (App)Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic)), (App)Codec.BOOL.optionalFieldOf("underlined").forGetter(style -> Optional.ofNullable(style.underlined)), (App)Codec.BOOL.optionalFieldOf("strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)), (App)Codec.BOOL.optionalFieldOf("obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)), (App)ClickEvent.CODEC.optionalFieldOf("click_event").forGetter(style -> Optional.ofNullable(style.clickEvent)), (App)HoverEvent.CODEC.optionalFieldOf("hover_event").forGetter(style -> Optional.ofNullable(style.hoverEvent)), (App)Codec.STRING.optionalFieldOf("insertion").forGetter(style -> Optional.ofNullable(style.insertion)), (App)StyleSpriteSource.FONT_CODEC.optionalFieldOf("font").forGetter(style -> Optional.ofNullable(style.font))).apply((Applicative)instance, Style::of));
        public static final Codec<Style> CODEC = MAP_CODEC.codec();
        public static final PacketCodec<RegistryByteBuf, Style> PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    }
}

