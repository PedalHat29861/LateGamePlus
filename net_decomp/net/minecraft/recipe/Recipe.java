/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.recipe;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public interface Recipe<T extends RecipeInput> {
    public static final Codec<Recipe<?>> CODEC = Registries.RECIPE_SERIALIZER.getCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
    public static final Codec<RegistryKey<Recipe<?>>> KEY_CODEC = RegistryKey.createCodec(RegistryKeys.RECIPE);
    public static final PacketCodec<RegistryByteBuf, Recipe<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.RECIPE_SERIALIZER).dispatch(Recipe::getSerializer, RecipeSerializer::packetCodec);

    public boolean matches(T var1, World var2);

    public ItemStack craft(T var1, RegistryWrapper.WrapperLookup var2);

    default public boolean isIgnoredInRecipeBook() {
        return false;
    }

    default public boolean showNotification() {
        return true;
    }

    default public String getGroup() {
        return "";
    }

    public RecipeSerializer<? extends Recipe<T>> getSerializer();

    public RecipeType<? extends Recipe<T>> getType();

    public IngredientPlacement getIngredientPlacement();

    default public List<RecipeDisplay> getDisplays() {
        return List.of();
    }

    public RecipeBookCategory getRecipeBookCategory();
}

