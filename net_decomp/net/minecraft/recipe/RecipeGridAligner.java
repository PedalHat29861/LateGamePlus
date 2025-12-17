/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import java.util.Iterator;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.math.MathHelper;

public interface RecipeGridAligner {
    public static <T> void alignRecipeToGrid(int width, int height, Recipe<?> recipe, Iterable<T> slots, Filler<T> filler) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            RecipeGridAligner.alignRecipeToGrid(width, height, shapedRecipe.getWidth(), shapedRecipe.getHeight(), slots, filler);
        } else {
            RecipeGridAligner.alignRecipeToGrid(width, height, width, height, slots, filler);
        }
    }

    public static <T> void alignRecipeToGrid(int width, int height, int recipeWidth, int recipeHeight, Iterable<T> slots, Filler<T> filler) {
        Iterator<T> iterator = slots.iterator();
        int i = 0;
        block0: for (int j = 0; j < height; ++j) {
            boolean bl = (float)recipeHeight < (float)height / 2.0f;
            int k = MathHelper.floor((float)height / 2.0f - (float)recipeHeight / 2.0f);
            if (bl && k > j) {
                i += width;
                ++j;
            }
            for (int l = 0; l < width; ++l) {
                boolean bl2;
                if (!iterator.hasNext()) {
                    return;
                }
                bl = (float)recipeWidth < (float)width / 2.0f;
                k = MathHelper.floor((float)width / 2.0f - (float)recipeWidth / 2.0f);
                int m = recipeWidth;
                boolean bl3 = bl2 = l < recipeWidth;
                if (bl) {
                    m = k + recipeWidth;
                    boolean bl4 = bl2 = k <= l && l < k + recipeWidth;
                }
                if (bl2) {
                    filler.addItemToSlot(iterator.next(), i, l, j);
                } else if (m == l) {
                    i += width - l;
                    continue block0;
                }
                ++i;
            }
        }
    }

    @FunctionalInterface
    public static interface Filler<T> {
        public void addItemToSlot(T var1, int var2, int var3, int var4);
    }
}

