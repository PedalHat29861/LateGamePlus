/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;

public class TippedArrowItem
extends ArrowItem {
    public TippedArrowItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack itemStack = super.getDefaultStack();
        itemStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.POISON));
        return itemStack;
    }

    @Override
    public Text getName(ItemStack stack) {
        PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
        return potionContentsComponent != null ? potionContentsComponent.getName(this.translationKey + ".effect.") : super.getName(stack);
    }
}

