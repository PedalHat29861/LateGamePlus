/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class ShieldItem
extends Item {
    public ShieldItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        DyeColor dyeColor = stack.get(DataComponentTypes.BASE_COLOR);
        if (dyeColor != null) {
            return Text.translatable(this.translationKey + "." + dyeColor.getId());
        }
        return super.getName(stack);
    }
}

