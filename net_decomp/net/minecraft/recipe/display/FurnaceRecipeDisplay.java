/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.recipe.display;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.resource.featuretoggle.FeatureSet;

public record FurnaceRecipeDisplay(SlotDisplay ingredient, SlotDisplay fuel, SlotDisplay result, SlotDisplay craftingStation, int duration, float experience) implements RecipeDisplay
{
    public static final MapCodec<FurnaceRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SlotDisplay.CODEC.fieldOf("ingredient").forGetter(FurnaceRecipeDisplay::ingredient), (App)SlotDisplay.CODEC.fieldOf("fuel").forGetter(FurnaceRecipeDisplay::fuel), (App)SlotDisplay.CODEC.fieldOf("result").forGetter(FurnaceRecipeDisplay::result), (App)SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(FurnaceRecipeDisplay::craftingStation), (App)Codec.INT.fieldOf("duration").forGetter(FurnaceRecipeDisplay::duration), (App)Codec.FLOAT.fieldOf("experience").forGetter(FurnaceRecipeDisplay::experience)).apply((Applicative)instance, FurnaceRecipeDisplay::new));
    public static final PacketCodec<RegistryByteBuf, FurnaceRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(SlotDisplay.PACKET_CODEC, FurnaceRecipeDisplay::ingredient, SlotDisplay.PACKET_CODEC, FurnaceRecipeDisplay::fuel, SlotDisplay.PACKET_CODEC, FurnaceRecipeDisplay::result, SlotDisplay.PACKET_CODEC, FurnaceRecipeDisplay::craftingStation, PacketCodecs.VAR_INT, FurnaceRecipeDisplay::duration, PacketCodecs.FLOAT, FurnaceRecipeDisplay::experience, FurnaceRecipeDisplay::new);
    public static final RecipeDisplay.Serializer<FurnaceRecipeDisplay> SERIALIZER = new RecipeDisplay.Serializer<FurnaceRecipeDisplay>(CODEC, PACKET_CODEC);

    public RecipeDisplay.Serializer<FurnaceRecipeDisplay> serializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isEnabled(FeatureSet features) {
        return this.ingredient.isEnabled(features) && this.fuel().isEnabled(features) && RecipeDisplay.super.isEnabled(features);
    }
}

