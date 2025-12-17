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
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;

public record SmithingRecipeDisplay(SlotDisplay template, SlotDisplay base, SlotDisplay addition, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay
{
    public static final MapCodec<SmithingRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SlotDisplay.CODEC.fieldOf("template").forGetter(SmithingRecipeDisplay::template), (App)SlotDisplay.CODEC.fieldOf("base").forGetter(SmithingRecipeDisplay::base), (App)SlotDisplay.CODEC.fieldOf("addition").forGetter(SmithingRecipeDisplay::addition), (App)SlotDisplay.CODEC.fieldOf("result").forGetter(SmithingRecipeDisplay::result), (App)SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(SmithingRecipeDisplay::craftingStation)).apply((Applicative)instance, SmithingRecipeDisplay::new));
    public static final PacketCodec<RegistryByteBuf, SmithingRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(SlotDisplay.PACKET_CODEC, SmithingRecipeDisplay::template, SlotDisplay.PACKET_CODEC, SmithingRecipeDisplay::base, SlotDisplay.PACKET_CODEC, SmithingRecipeDisplay::addition, SlotDisplay.PACKET_CODEC, SmithingRecipeDisplay::result, SlotDisplay.PACKET_CODEC, SmithingRecipeDisplay::craftingStation, SmithingRecipeDisplay::new);
    public static final RecipeDisplay.Serializer<SmithingRecipeDisplay> SERIALIZER = new RecipeDisplay.Serializer<SmithingRecipeDisplay>(CODEC, PACKET_CODEC);

    public RecipeDisplay.Serializer<SmithingRecipeDisplay> serializer() {
        return SERIALIZER;
    }
}

