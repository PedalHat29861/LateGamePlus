/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.scoreboard.number;

import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.text.MutableText;

public interface NumberFormat {
    public MutableText format(int var1);

    public NumberFormatType<? extends NumberFormat> getType();
}

