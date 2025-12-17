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
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SingleStackRecipe;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.display.FurnaceRecipeDisplay;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;

public abstract class AbstractCookingRecipe
extends SingleStackRecipe {
    private final CookingRecipeCategory category;
    private final float experience;
    private final int cookingTime;

    public AbstractCookingRecipe(String group, CookingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(group, ingredient, result);
        this.category = category;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public abstract RecipeSerializer<? extends AbstractCookingRecipe> getSerializer();

    @Override
    public abstract RecipeType<? extends AbstractCookingRecipe> getType();

    public float getExperience() {
        return this.experience;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    public CookingRecipeCategory getCategory() {
        return this.category;
    }

    protected abstract Item getCookerItem();

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new FurnaceRecipeDisplay(this.ingredient().toDisplay(), SlotDisplay.AnyFuelSlotDisplay.INSTANCE, new SlotDisplay.StackSlotDisplay(this.result()), new SlotDisplay.ItemSlotDisplay(this.getCookerItem()), this.cookingTime, this.experience));
    }

    @FunctionalInterface
    public static interface RecipeFactory<T extends AbstractCookingRecipe> {
        public T create(String var1, CookingRecipeCategory var2, Ingredient var3, ItemStack var4, float var5, int var6);
    }

    public static class Serializer<T extends AbstractCookingRecipe>
    implements RecipeSerializer<T> {
        private final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.optionalFieldOf("group", (Object)"").forGetter(SingleStackRecipe::getGroup), (App)CookingRecipeCategory.CODEC.fieldOf("category").orElse((Object)CookingRecipeCategory.MISC).forGetter(AbstractCookingRecipe::getCategory), (App)Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleStackRecipe::ingredient), (App)ItemStack.VALIDATED_UNCOUNTED_CODEC.fieldOf("result").forGetter(SingleStackRecipe::result), (App)Codec.FLOAT.fieldOf("experience").orElse((Object)Float.valueOf(0.0f)).forGetter(AbstractCookingRecipe::getExperience), (App)Codec.INT.fieldOf("cookingtime").orElse((Object)defaultCookingTime).forGetter(AbstractCookingRecipe::getCookingTime)).apply((Applicative)instance, factory::create));
        private final PacketCodec<RegistryByteBuf, T> packetCodec = PacketCodec.tuple(PacketCodecs.STRING, SingleStackRecipe::getGroup, CookingRecipeCategory.PACKET_CODEC, AbstractCookingRecipe::getCategory, Ingredient.PACKET_CODEC, SingleStackRecipe::ingredient, ItemStack.PACKET_CODEC, SingleStackRecipe::result, PacketCodecs.FLOAT, AbstractCookingRecipe::getExperience, PacketCodecs.INTEGER, AbstractCookingRecipe::getCookingTime, factory::create);

        public Serializer(RecipeFactory<T> factory, int defaultCookingTime) {
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, T> packetCodec() {
            return this.packetCodec;
        }
    }
}

