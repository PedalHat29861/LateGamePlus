/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.tooltip;

import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public interface TooltipAppender {
    public void appendTooltip(Item.TooltipContext var1, Consumer<Text> var2, TooltipType var3, ComponentsAccess var4);
}

