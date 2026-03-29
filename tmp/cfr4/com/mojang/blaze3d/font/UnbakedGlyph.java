/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public interface UnbakedGlyph {
    public GlyphInfo info();

    public BakedGlyph bake(Stitcher var1);

    public static interface Stitcher {
        public BakedGlyph stitch(GlyphInfo var1, GlyphBitmap var2);

        public BakedGlyph getMissing();
    }
}

