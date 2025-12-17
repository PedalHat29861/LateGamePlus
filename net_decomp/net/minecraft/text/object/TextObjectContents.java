/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.text.object;

import com.mojang.serialization.MapCodec;
import net.minecraft.text.StyleSpriteSource;

public interface TextObjectContents {
    public StyleSpriteSource spriteSource();

    public String asText();

    public MapCodec<? extends TextObjectContents> getCodec();
}

