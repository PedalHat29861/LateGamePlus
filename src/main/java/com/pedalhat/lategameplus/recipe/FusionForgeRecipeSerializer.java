package com.pedalhat.lategameplus.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;

public class FusionForgeRecipeSerializer implements RecipeSerializer<FusionForgeRecipe> {
    public static final MapCodec<FusionForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Ingredient.CODEC.fieldOf("input_a").forGetter(FusionForgeRecipe::inputA),
        Ingredient.CODEC.fieldOf("input_b").forGetter(FusionForgeRecipe::inputB),
        Ingredient.CODEC.fieldOf("catalyst").forGetter(FusionForgeRecipe::catalyst),
        ItemStack.CODEC.fieldOf("result").forGetter(FusionForgeRecipe::output),
        Codec.INT.optionalFieldOf("cook_time", 200).forGetter(FusionForgeRecipe::cookTime),
        Codec.INT.optionalFieldOf("fuel_cost", 1600).forGetter(FusionForgeRecipe::fuelCost),
        Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(FusionForgeRecipe::experience)
    ).apply(instance, FusionForgeRecipe::new));

    public static final PacketCodec<RegistryByteBuf, FusionForgeRecipe> PACKET_CODEC = PacketCodec.tuple(
        Ingredient.PACKET_CODEC, FusionForgeRecipe::inputA,
        Ingredient.PACKET_CODEC, FusionForgeRecipe::inputB,
        Ingredient.PACKET_CODEC, FusionForgeRecipe::catalyst,
        ItemStack.PACKET_CODEC, FusionForgeRecipe::output,
        PacketCodecs.VAR_INT, FusionForgeRecipe::cookTime,
        PacketCodecs.VAR_INT, FusionForgeRecipe::fuelCost,
        PacketCodecs.FLOAT, FusionForgeRecipe::experience,
        FusionForgeRecipe::new
    );

    @Override
    public MapCodec<FusionForgeRecipe> codec() {
        return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, FusionForgeRecipe> packetCodec() {
        return PACKET_CODEC;
    }
}
