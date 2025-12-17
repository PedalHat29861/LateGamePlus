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
import java.util.Objects;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import org.jspecify.annotations.Nullable;

public class SmithingTrimRecipe
implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final RegistryEntry<ArmorTrimPattern> pattern;
    private @Nullable IngredientPlacement ingredientPlacement;

    public SmithingTrimRecipe(Ingredient template, Ingredient base, Ingredient addition, RegistryEntry<ArmorTrimPattern> pattern) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.pattern = pattern;
    }

    @Override
    public ItemStack craft(SmithingRecipeInput smithingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup) {
        return SmithingTrimRecipe.craft(wrapperLookup, smithingRecipeInput.base(), smithingRecipeInput.addition(), this.pattern);
    }

    public static ItemStack craft(RegistryWrapper.WrapperLookup registries, ItemStack base, ItemStack addition, RegistryEntry<ArmorTrimPattern> pattern) {
        Optional<RegistryEntry<ArmorTrimMaterial>> optional = ArmorTrimMaterials.get(registries, addition);
        if (optional.isPresent()) {
            ArmorTrim armorTrim2;
            ArmorTrim armorTrim = base.get(DataComponentTypes.TRIM);
            if (Objects.equals(armorTrim, armorTrim2 = new ArmorTrim(optional.get(), pattern))) {
                return ItemStack.EMPTY;
            }
            ItemStack itemStack = base.copyWithCount(1);
            itemStack.set(DataComponentTypes.TRIM, armorTrim2);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<Ingredient> template() {
        return Optional.of(this.template);
    }

    @Override
    public Ingredient base() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> addition() {
        return Optional.of(this.addition);
    }

    @Override
    public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(List.of(this.template, this.base, this.addition));
        }
        return this.ingredientPlacement;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        SlotDisplay slotDisplay = this.base.toDisplay();
        SlotDisplay slotDisplay2 = this.addition.toDisplay();
        SlotDisplay slotDisplay3 = this.template.toDisplay();
        return List.of(new SmithingRecipeDisplay(slotDisplay3, slotDisplay, slotDisplay2, new SlotDisplay.SmithingTrimSlotDisplay(slotDisplay, slotDisplay2, this.pattern), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTrimRecipe> {
        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template), (App)Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base), (App)Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition), (App)ArmorTrimPattern.ENTRY_CODEC.fieldOf("pattern").forGetter(recipe -> recipe.pattern)).apply((Applicative)instance, SmithingTrimRecipe::new));
        public static final PacketCodec<RegistryByteBuf, SmithingTrimRecipe> PACKET_CODEC = PacketCodec.tuple(Ingredient.PACKET_CODEC, recipe -> recipe.template, Ingredient.PACKET_CODEC, recipe -> recipe.base, Ingredient.PACKET_CODEC, recipe -> recipe.addition, ArmorTrimPattern.ENTRY_PACKET_CODEC, recipe -> recipe.pattern, SmithingTrimRecipe::new);

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, SmithingTrimRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}

