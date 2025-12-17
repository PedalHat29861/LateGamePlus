/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.text;

import net.minecraft.text.Style;

@FunctionalInterface
public interface CharacterVisitor {
    public boolean accept(int var1, Style var2, int var3);
}

