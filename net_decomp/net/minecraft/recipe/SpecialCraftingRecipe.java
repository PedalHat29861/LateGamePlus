/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;

public abstract class SpecialCraftingRecipe
implements CraftingRecipe {
    private final CraftingRecipeCategory category;

    public SpecialCraftingRecipe(CraftingRecipeCategory category) {
        this.category = category;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public abstract RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer();

    public static class SpecialRecipeSerializer<T extends CraftingRecipe>
    implements RecipeSerializer<T> {
        private final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CraftingRecipeCategory.CODEC.fieldOf("category").orElse((Object)CraftingRecipeCategory.MISC).forGetter(CraftingRecipe::getCategory)).apply((Applicative)instance, factory::create));
        private final PacketCodec<RegistryByteBuf, T> packetCodec = PacketCodec.tuple(CraftingRecipeCategory.PACKET_CODEC, CraftingRecipe::getCategory, factory::create);

        public SpecialRecipeSerializer(Factory<T> factory) {
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public PacketCodec<RegistryByteBuf, T> packetCodec() {
            return this.packetCodec;
        }

        @FunctionalInterface
        public static interface Factory<T extends CraftingRecipe> {
            public T create(CraftingRecipeCategory var1);
        }
    }
}

