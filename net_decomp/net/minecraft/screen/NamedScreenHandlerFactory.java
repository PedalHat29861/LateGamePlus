/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen;

import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.text.Text;

public interface NamedScreenHandlerFactory
extends ScreenHandlerFactory {
    public Text getDisplayName();
}

