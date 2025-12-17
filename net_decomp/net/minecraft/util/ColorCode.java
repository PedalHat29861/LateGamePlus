/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import java.util.HexFormat;
import net.minecraft.util.dynamic.Codecs;

public record ColorCode(int rgba) {
    public static final Codec<ColorCode> CODEC = Codecs.HEX_ARGB.xmap(ColorCode::new, ColorCode::rgba);

    @Override
    public String toString() {
        return HexFormat.of().toHexDigits(this.rgba, 8);
    }
}

