/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.ArmorDyeRecipe;
import net.minecraft.recipe.BannerDuplicateRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.BookCloningRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.CraftingDecoratedPotRecipe;
import net.minecraft.recipe.FireworkRocketRecipe;
import net.minecraft.recipe.FireworkStarFadeRecipe;
import net.minecraft.recipe.FireworkStarRecipe;
import net.minecraft.recipe.MapCloningRecipe;
import net.minecraft.recipe.MapExtendingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RepairItemRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.ShieldDecorationRecipe;
import net.minecraft.recipe.SingleStackRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.recipe.SmithingTrimRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.recipe.TransmuteRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface RecipeSerializer<T extends Recipe<?>> {
    public static final RecipeSerializer<ShapedRecipe> SHAPED = RecipeSerializer.register("crafting_shaped", new ShapedRecipe.Serializer());
    public static final RecipeSerializer<ShapelessRecipe> SHAPELESS = RecipeSerializer.register("crafting_shapeless", new ShapelessRecipe.Serializer());
    public static final RecipeSerializer<ArmorDyeRecipe> ARMOR_DYE = RecipeSerializer.register("crafting_special_armordye", new SpecialCraftingRecipe.SpecialRecipeSerializer<ArmorDyeRecipe>(ArmorDyeRecipe::new));
    public static final RecipeSerializer<BookCloningRecipe> BOOK_CLONING = RecipeSerializer.register("crafting_special_bookcloning", new SpecialCraftingRecipe.SpecialRecipeSerializer<BookCloningRecipe>(BookCloningRecipe::new));
    public static final RecipeSerializer<MapCloningRecipe> MAP_CLONING = RecipeSerializer.register("crafting_special_mapcloning", new SpecialCraftingRecipe.SpecialRecipeSerializer<MapCloningRecipe>(MapCloningRecipe::new));
    public static final RecipeSerializer<MapExtendingRecipe> MAP_EXTENDING = RecipeSerializer.register("crafting_special_mapextending", new SpecialCraftingRecipe.SpecialRecipeSerializer<MapExtendingRecipe>(MapExtendingRecipe::new));
    public static final RecipeSerializer<FireworkRocketRecipe> FIREWORK_ROCKET = RecipeSerializer.register("crafting_special_firework_rocket", new SpecialCraftingRecipe.SpecialRecipeSerializer<FireworkRocketRecipe>(FireworkRocketRecipe::new));
    public static final RecipeSerializer<FireworkStarRecipe> FIREWORK_STAR = RecipeSerializer.register("crafting_special_firework_star", new SpecialCraftingRecipe.SpecialRecipeSerializer<FireworkStarRecipe>(FireworkStarRecipe::new));
    public static final RecipeSerializer<FireworkStarFadeRecipe> FIREWORK_STAR_FADE = RecipeSerializer.register("crafting_special_firework_star_fade", new SpecialCraftingRecipe.SpecialRecipeSerializer<FireworkStarFadeRecipe>(FireworkStarFadeRecipe::new));
    public static final RecipeSerializer<TippedArrowRecipe> TIPPED_ARROW = RecipeSerializer.register("crafting_special_tippedarrow", new SpecialCraftingRecipe.SpecialRecipeSerializer<TippedArrowRecipe>(TippedArrowRecipe::new));
    public static final RecipeSerializer<BannerDuplicateRecipe> BANNER_DUPLICATE = RecipeSerializer.register("crafting_special_bannerduplicate", new SpecialCraftingRecipe.SpecialRecipeSerializer<BannerDuplicateRecipe>(BannerDuplicateRecipe::new));
    public static final RecipeSerializer<ShieldDecorationRecipe> SHIELD_DECORATION = RecipeSerializer.register("crafting_special_shielddecoration", new SpecialCraftingRecipe.SpecialRecipeSerializer<ShieldDecorationRecipe>(ShieldDecorationRecipe::new));
    public static final RecipeSerializer<TransmuteRecipe> CRAFTING_TRANSMUTE = RecipeSerializer.register("crafting_transmute", new TransmuteRecipe.Serializer());
    public static final RecipeSerializer<RepairItemRecipe> REPAIR_ITEM = RecipeSerializer.register("crafting_special_repairitem", new SpecialCraftingRecipe.SpecialRecipeSerializer<RepairItemRecipe>(RepairItemRecipe::new));
    public static final RecipeSerializer<SmeltingRecipe> SMELTING = RecipeSerializer.register("smelting", new AbstractCookingRecipe.Serializer<SmeltingRecipe>(SmeltingRecipe::new, 200));
    public static final RecipeSerializer<BlastingRecipe> BLASTING = RecipeSerializer.register("blasting", new AbstractCookingRecipe.Serializer<BlastingRecipe>(BlastingRecipe::new, 100));
    public static final RecipeSerializer<SmokingRecipe> SMOKING = RecipeSerializer.register("smoking", new AbstractCookingRecipe.Serializer<SmokingRecipe>(SmokingRecipe::new, 100));
    public static final RecipeSerializer<CampfireCookingRecipe> CAMPFIRE_COOKING = RecipeSerializer.register("campfire_cooking", new AbstractCookingRecipe.Serializer<CampfireCookingRecipe>(CampfireCookingRecipe::new, 100));
    public static final RecipeSerializer<StonecuttingRecipe> STONECUTTING = RecipeSerializer.register("stonecutting", new SingleStackRecipe.Serializer<StonecuttingRecipe>(StonecuttingRecipe::new));
    public static final RecipeSerializer<SmithingTransformRecipe> SMITHING_TRANSFORM = RecipeSerializer.register("smithing_transform", new SmithingTransformRecipe.Serializer());
    public static final RecipeSerializer<SmithingTrimRecipe> SMITHING_TRIM = RecipeSerializer.register("smithing_trim", new SmithingTrimRecipe.Serializer());
    public static final RecipeSerializer<CraftingDecoratedPotRecipe> CRAFTING_DECORATED_POT = RecipeSerializer.register("crafting_decorated_pot", new SpecialCraftingRecipe.SpecialRecipeSerializer<CraftingDecoratedPotRecipe>(CraftingDecoratedPotRecipe::new));

    public MapCodec<T> codec();

    @Deprecated
    public PacketCodec<RegistryByteBuf, T> packetCodec();

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return (S)Registry.register(Registries.RECIPE_SERIALIZER, id, serializer);
    }
}

