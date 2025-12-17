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
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.text.KeybindTranslations;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jspecify.annotations.Nullable;

public class KeybindTextContent
implements TextContent {
    public static final MapCodec<KeybindTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("keybind").forGetter(content -> content.key)).apply((Applicative)instance, KeybindTextContent::new));
    private final String key;
    private @Nullable Supplier<Text> translated;

    public KeybindTextContent(String key) {
        this.key = key;
    }

    private Text getTranslated() {
        if (this.translated == null) {
            this.translated = KeybindTranslations.factory.apply(this.key);
        }
        return this.translated.get();
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return this.getTranslated().visit(visitor);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return this.getTranslated().visit(visitor, style);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeybindTextContent)) return false;
        KeybindTextContent keybindTextContent = (KeybindTextContent)o;
        if (!this.key.equals(keybindTextContent.key)) return false;
        return true;
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public String toString() {
        return "keybind{" + this.key + "}";
    }

    public String getKey() {
        return this.key;
    }

    public MapCodec<KeybindTextContent> getCodec() {
        return CODEC;
    }
}

