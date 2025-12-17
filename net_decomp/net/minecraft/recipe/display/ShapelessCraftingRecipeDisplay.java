/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.recipe.display;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.resource.featuretoggle.FeatureSet;

public record ShapelessCraftingRecipeDisplay(List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay
{
    public static final MapCodec<ShapelessCraftingRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(ShapelessCraftingRecipeDisplay::ingredients), (App)SlotDisplay.CODEC.fieldOf("result").forGetter(ShapelessCraftingRecipeDisplay::result), (App)SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ShapelessCraftingRecipeDisplay::craftingStation)).apply((Applicative)instance, ShapelessCraftingRecipeDisplay::new));
    public static final PacketCodec<RegistryByteBuf, ShapelessCraftingRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(SlotDisplay.PACKET_CODEC.collect(PacketCodecs.toList()), ShapelessCraftingRecipeDisplay::ingredients, SlotDisplay.PACKET_CODEC, ShapelessCraftingRecipeDisplay::result, SlotDisplay.PACKET_CODEC, ShapelessCraftingRecipeDisplay::craftingStation, ShapelessCraftingRecipeDisplay::new);
    public static final RecipeDisplay.Serializer<ShapelessCraftingRecipeDisplay> SERIALIZER = new RecipeDisplay.Serializer<ShapelessCraftingRecipeDisplay>(CODEC, PACKET_CODEC);

    public RecipeDisplay.Serializer<ShapelessCraftingRecipeDisplay> serializer() {
        return SERIALIZER;
    }

    @Override
    public boolean isEnabled(FeatureSet features) {
        return this.ingredients.stream().allMatch(ingredient -> ingredient.isEnabled(features)) && RecipeDisplay.super.isEnabled(features);
    }
}

