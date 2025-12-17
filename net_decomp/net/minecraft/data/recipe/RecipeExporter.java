/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipe;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import org.jspecify.annotations.Nullable;

public interface RecipeExporter {
    public void accept(RegistryKey<Recipe<?>> var1, Recipe<?> var2, @Nullable AdvancementEntry var3);

    public Advancement.Builder getAdvancementBuilder();

    public void addRootAdvancement();
}

