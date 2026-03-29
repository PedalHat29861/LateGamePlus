package com.pedalhat.lategameplus.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class FusionForgeRecipeSerializer {
    public static final MapCodec<FusionForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC.fieldOf("input_a").forGetter(FusionForgeRecipe::inputA),
        Ingredient.CODEC.fieldOf("input_b").forGetter(FusionForgeRecipe::inputB),
        ItemStack.CODEC.fieldOf("result").forGetter(FusionForgeRecipe::output),
        Codec.INT.optionalFieldOf("cook_time", 200).forGetter(FusionForgeRecipe::cookTime),
        Codec.INT.optionalFieldOf("fuel_cost", 8).forGetter(FusionForgeRecipe::fuelCost),
        Codec.FLOAT.optionalFieldOf("experience", 0.1f).forGetter(FusionForgeRecipe::experience)
    ).apply(instance, FusionForgeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FusionForgeRecipe> PACKET_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, FusionForgeRecipe::inputA,
        Ingredient.CONTENTS_STREAM_CODEC, FusionForgeRecipe::inputB,
        ItemStack.STREAM_CODEC, FusionForgeRecipe::output,
        ByteBufCodecs.VAR_INT, FusionForgeRecipe::cookTime,
        ByteBufCodecs.VAR_INT, FusionForgeRecipe::fuelCost,
        ByteBufCodecs.FLOAT, FusionForgeRecipe::experience,
        FusionForgeRecipe::new
    );

    public static final RecipeSerializer<FusionForgeRecipe> INSTANCE = new RecipeSerializer<>(CODEC, PACKET_CODEC);

    private FusionForgeRecipeSerializer() {}
}
