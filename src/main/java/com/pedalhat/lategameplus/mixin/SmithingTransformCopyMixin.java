package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.registry.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingTransformCopyMixin {
    @Inject(method = "craft", at = @At("RETURN"), cancellable = true)
    private void lategameplus$copyAllComponents(SmithingRecipeInput input, RegistryWrapper.WrapperLookup registries, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result != null && result.getItem() == ModBlocks.NETHERITE_SHULKER_BOX_ITEM) {
            ItemStack base = input.base();
            if (!base.isEmpty()) {
                result.applyComponentsFrom(base.getComponents());
                cir.setReturnValue(result);
            }
        }
    }
}

