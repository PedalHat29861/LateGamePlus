package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public abstract class AnvilHandlerCapMixin {
    private static boolean lategameplus$loggedConstantReplacement;

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int lategameplus$raiseOrRemoveTooExpensiveCap(int original) {
        if (!lategameplus$loggedConstantReplacement) {
            LateGamePlus.LOGGER.info(
                "[NetheriteAnvil] replacing vanilla too-expensive cap {} -> {}",
                original,
                Integer.MAX_VALUE
            );
            lategameplus$loggedConstantReplacement = true;
        }
        return Integer.MAX_VALUE;
    }
}
