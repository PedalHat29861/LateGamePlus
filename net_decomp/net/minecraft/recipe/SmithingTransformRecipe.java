/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.TransmuteRecipeResult;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.jspecify.annotations.Nullable;

public class SmithingTransformRecipe
implements SmithingRecipe {
    final Optional<Ingredient> template;
    final Ingredient base;
    final Optional<Ingredient> addition;
    final TransmuteRecipeResult result;
    private @Nullable IngredientPlacement ingredientPlacement;

    public SmithingTransformRecipe(Optional<Ingredient> template, Ingredient base, Optional<Ingredient> addition, TransmuteRecipeResult result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public ItemStack craft(SmithingRecipeInput smithingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        return this.result.apply(smithingRecipeInput.base());
    }

    @Override
    public Optional<Ingredient> template() {
        return this.template;
    }

    @Override
    public Ingredient base() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> addition() {
        return this.addition;
    }

    @Override
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forMultipleSlots(List.of(this.template, Optional.of(this.base), this.addition));
        }
        return this.ingredientPlacement;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new SmithingRecipeDisplay(Ingredient.toDisplay(this.template), this.base.toDisplay(), Ingredient.toDisplay(this.addition), this.result.createSlotDisplay(), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Ingredient.CODEC.optionalFieldOf("template").forGetter(recipe -> recipe.template), (App)Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base), (App)Ingredient.CODEC.optionalFieldOf("addition").forGetter(recipe -> recipe.addition), (App)TransmuteRecipeResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)).apply((Applicative)instance, SmithingTransformRecipe::new));
        public static final PacketCodec<RegistryByteBuf, SmithingTransformRecipe> PACKET_CODEC = PacketCodec.tuple(Ingredient.OPTIONAL_PACKET_CODEC, recipe -> recipe.template, Ingredient.PACKET_CODEC, recipe -> recipe.base, Ingredient.OPTIONAL_PACKET_CODEC, recipe -> recipe.addition, TransmuteRecipeResult.PACKET_CODEC, recipe -> recipe.result, SmithingTransformRecipe::new);

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SmithingTransformRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}

