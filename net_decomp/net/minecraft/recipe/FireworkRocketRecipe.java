/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import java.util.ArrayList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class FireworkRocketRecipe
extends SpecialCraftingRecipe {
    private static final Ingredient PAPER = Ingredient.ofItem(Items.PAPER);
    private static final Ingredient DURATION_MODIFIER = Ingredient.ofItem(Items.GUNPOWDER);
    private static final Ingredient FIREWORK_STAR = Ingredient.ofItem(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(CraftingRecipeCategory craftingRecipeCategory) {
        super(craftingRecipeCategory);
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        if (craftingRecipeInput.getStackCount() < 2) {
            return false;
        }
        boolean bl = false;
        int i = 0;
        for (int j = 0; j < craftingRecipeInput.size(); ++j) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(j);
            if (itemStack.isEmpty()) continue;
            if (PAPER.test(itemStack)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!(DURATION_MODIFIER.test(itemStack) ? ++i > 3 : !FIREWORK_STAR.test(itemStack))) continue;
            return false;
        }
        return bl && i >= 1;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        ArrayList<FireworkExplosionComponent> list = new ArrayList<FireworkExplosionComponent>();
        int i = 0;
        for (int j = 0; j < craftingRecipeInput.size(); ++j) {
            FireworkExplosionComponent fireworkExplosionComponent;
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(j);
            if (itemStack.isEmpty()) continue;
            if (DURATION_MODIFIER.test(itemStack)) {
                ++i;
                continue;
            }
            if (!FIREWORK_STAR.test(itemStack) || (fireworkExplosionComponent = itemStack.get(DataComponentTypes.FIREWORK_EXPLOSION)) == null) continue;
            list.add(fireworkExplosionComponent);
        }
        ItemStack itemStack2 = new ItemStack(Items.FIREWORK_ROCKET, 3);
        itemStack2.set(DataComponentTypes.FIREWORKS, new FireworksComponent(i, list));
        return itemStack2;
    }

    @Override
    public RecipeSerializer<FireworkRocketRecipe> getSerializer() {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}

