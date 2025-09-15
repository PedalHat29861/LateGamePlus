package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilNuggetRepairMixin {

    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Unique
    private boolean ne$inUpdate;

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void ne$addNuggetRepair(CallbackInfo ci) {
        if (ne$inUpdate) return;
        ne$inUpdate = true;
        try {
            AnvilScreenHandler self = (AnvilScreenHandler)(Object) this;

            ItemStack left  = self.getSlot(0).getStack();
            ItemStack right = self.getSlot(1).getStack();
            Slot outSlot    = self.getSlot(2);
            ItemStack out   = outSlot.getStack();

            if (left.isEmpty() || right.isEmpty()) return;
            if (!left.isDamageable()) return;

            if (!right.isOf(ModItems.NETHERITE_NUGGET)) return;

            boolean vanillaGaveResult = !out.isEmpty();

            boolean isVanillaNetherite =
                    left.isOf(Items.NETHERITE_SWORD)   ||
                    left.isOf(Items.NETHERITE_SHOVEL)  ||
                    left.isOf(Items.NETHERITE_PICKAXE) ||
                    left.isOf(Items.NETHERITE_AXE)     ||
                    left.isOf(Items.NETHERITE_HOE)     ||
                    left.isOf(Items.NETHERITE_HELMET)      ||
                    left.isOf(Items.NETHERITE_CHESTPLATE)  ||
                    left.isOf(Items.NETHERITE_LEGGINGS)    ||
                    left.isOf(Items.NETHERITE_BOOTS);

            if (!vanillaGaveResult && !isVanillaNetherite) return;

            int max    = left.getMaxDamage();
            int damage = left.getDamage();
            if (max <= 0 || damage <= 0) return;

            float pct = ConfigManager.get().nuggetRepairPercent;
            if (pct < 0f) pct = 0f;
            if (pct > 1f) pct = 1f;
            int perNugget = Math.max(1, Math.round(max * pct));

            int nuggetsAvailable = right.getCount();
            int nuggetsNeeded    = (int)Math.ceil(damage / (double)perNugget);
            int use              = Math.min(nuggetsAvailable, nuggetsNeeded);

            ItemStack base  = vanillaGaveResult ? out : left;
            ItemStack fixed = base.copy();
            fixed.setDamage(Math.max(0, damage - perNugget * use));

            outSlot.setStack(fixed);
            this.repairItemUsage = use;
            this.levelCost.set(Math.max(1, use));

        } finally {
            ne$inUpdate = false;
        }
    }
}
