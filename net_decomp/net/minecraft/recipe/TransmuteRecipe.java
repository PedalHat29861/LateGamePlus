/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.TransmuteRecipeResult;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class TransmuteRecipe
implements CraftingRecipe {
    final String group;
    final CraftingRecipeCategory category;
    final Ingredient input;
    final Ingredient material;
    final TransmuteRecipeResult result;
    private @Nullable IngredientPlacement ingredientPlacement;

    public TransmuteRecipe(String group, CraftingRecipeCategory category, Ingredient input, Ingredient material, TransmuteRecipeResult result) {
        this.group = group;
        this.category = category;
        this.input = input;
        this.material = material;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        if (craftingRecipeInput.getStackCount() != 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingRecipeInput.size(); ++i) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            if (!bl && this.input.test(itemStack)) {
                if (this.result.isEqualToResult(itemStack)) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (!bl2 && this.material.test(itemStack)) {
                bl2 = true;
                continue;
            }
            return false;
        }
        return bl && bl2;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        for (int i = 0; i < craftingRecipeInput.size(); ++i) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.isEmpty() || !this.input.test(itemStack)) continue;
            return this.result.apply(itemStack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new ShapelessCraftingRecipeDisplay(List.of(this.input.toDisplay(), this.material.toDisplay()), this.result.createSlotDisplay(), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    public RecipeSerializer<TransmuteRecipe> getSerializer() {
        return RecipeSerializer.CRAFTING_TRANSMUTE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(List.of(this.input, this.material));
        }
        return this.ingredientPlacement;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    public static class Serializer
    implements RecipeSerializer<TransmuteRecipe> {
        private static final MapCodec<TransmuteRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(recipe -> recipe.group), (App)CraftingRecipeCategory.CODEC.fieldOf("category").orElse((Object)CraftingRecipeCategory.MISC).forGetter(recipe -> recipe.category), (App)Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input), (App)Ingredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.material), (App)TransmuteRecipeResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)).apply((Applicative)instance, TransmuteRecipe::new));
        public static final PacketCodec<RegistryByteBuf, TransmuteRecipe> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, recipe -> recipe.group, CraftingRecipeCategory.PACKET_CODEC, recipe -> recipe.category, Ingredient.PACKET_CODEC, recipe -> recipe.input, Ingredient.PACKET_CODEC, recipe -> recipe.material, TransmuteRecipeResult.PACKET_CODEC, recipe -> recipe.result, TransmuteRecipe::new);

        @Override
        public MapCodec<TransmuteRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, TransmuteRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}

