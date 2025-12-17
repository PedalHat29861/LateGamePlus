/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipe;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.NetworkRecipeId;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.context.ContextParameterMap;

public record RecipeDisplayEntry(NetworkRecipeId id, RecipeDisplay display, OptionalInt group, RecipeBookCategory category, Optional<List<Ingredient>> craftingRequirements) {
    public static final PacketCodec<RegistryByteBuf, RecipeDisplayEntry> PACKET_CODEC = PacketCodec.tuple(NetworkRecipeId.PACKET_CODEC, RecipeDisplayEntry::id, RecipeDisplay.PACKET_CODEC, RecipeDisplayEntry::display, PacketCodecs.OPTIONAL_INT, RecipeDisplayEntry::group, PacketCodecs.registryValue(RegistryKeys.RECIPE_BOOK_CATEGORY), RecipeDisplayEntry::category, Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()).collect(PacketCodecs::optional), RecipeDisplayEntry::craftingRequirements, RecipeDisplayEntry::new);

    public List<ItemStack> getStacks(ContextParameterMap context) {
        return this.display.result().getStacks(context);
    }

    public boolean isCraftable(RecipeFinder finder) {
        if (this.craftingRequirements.isEmpty()) {
            return false;
        }
        return finder.isCraftable(this.craftingRequirements.get(), null);
    }
}

