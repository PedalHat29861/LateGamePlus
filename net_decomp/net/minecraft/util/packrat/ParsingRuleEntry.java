/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.packrat;

import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.Symbol;

public interface ParsingRuleEntry<S, T> {
    public Symbol<T> getSymbol();

    public ParsingRule<S, T> getRule();
}

