/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;

public class FurnaceSmeltLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<FurnaceSmeltLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FurnaceSmeltLootFunction.addConditionsField(instance).apply((Applicative)instance, FurnaceSmeltLootFunction::new));

    private FurnaceSmeltLootFunction(List<LootCondition> conditions) {
        super(conditions);
    }

    public LootFunctionType<FurnaceSmeltLootFunction> getType() {
        return LootFunctionTypes.FURNACE_SMELT;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        ItemStack itemStack;
        if (stack.isEmpty()) {
            return stack;
        }
        SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(stack);
        Optional<RecipeEntry<SmeltingRecipe>> optional = context.getWorld().getRecipeManager().getFirstMatch(RecipeType.SMELTING, singleStackRecipeInput, context.getWorld());
        if (optional.isPresent() && !(itemStack = optional.get().value().craft(singleStackRecipeInput, (RegistryWrapper.WrapperLookup)context.getWorld().getRegistryManager())).isEmpty()) {
            return itemStack.copyWithCount(stack.getCount());
        }
        LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)stack);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder() {
        return FurnaceSmeltLootFunction.builder(FurnaceSmeltLootFunction::new);
    }
}

