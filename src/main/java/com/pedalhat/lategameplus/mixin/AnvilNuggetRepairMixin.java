package com.pedalhat.lategameplus.mixin;

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

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.registry.ModItems;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilNuggetRepairMixin {

    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Unique
    private boolean ne$inUpdate;

    // IMPORTANTE: @At("RETURN") -> corre antes de CADA return del método
    @Inject(method = "updateResult", at = @At("RETURN"))
    private void ne$addNuggetRepair(CallbackInfo ci) {
        if (ne$inUpdate) return;
        ne$inUpdate = true;
        try {
            AnvilScreenHandler self = (AnvilScreenHandler)(Object) this;

            ItemStack left  = self.getSlot(0).getStack(); // item a reparar
            ItemStack right = self.getSlot(1).getStack(); // material
            Slot outSlot    = self.getSlot(2);
            ItemStack out   = outSlot.getStack();

            if (left.isEmpty() || right.isEmpty()) return;
            if (!right.isOf(ModItems.NETHERITE_NUGGET)) return; // no tocamos lingotes
            if (!left.isDamageable()) return;

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

            // Si vanilla no dio salida y tampoco es pieza vanilla de netherita, no hacemos nada
            if (!vanillaGaveResult && !isVanillaNetherite) return;

            int max    = left.getMaxDamage();
            int damage = left.getDamage();
            if (max <= 0 || damage <= 0) return;

            // ~1/18 de la durabilidad por pepita (ajustable)
            int perNugget = Math.max(1, max / 18);

            int nuggetsAvailable = right.getCount();
            int nuggetsNeeded    = (int)Math.ceil(damage / (double)perNugget);
            int use              = Math.min(nuggetsAvailable, nuggetsNeeded);

            ItemStack base  = vanillaGaveResult ? out : left;
            ItemStack fixed = base.copy();
            fixed.setDamage(Math.max(0, damage - perNugget * use));

            outSlot.setStack(fixed);
            this.repairItemUsage = use;
            this.levelCost.set(Math.max(1, use));

            LateGamePlus.LOGGER.debug(
                "[NE DEBUG] nugget repair -> {} dmg {} -> {} usando {} pepitas",
                left.getItem(), damage, fixed.getDamage(), use
            );
        } finally {
            ne$inUpdate = false;
        }
    }
}
