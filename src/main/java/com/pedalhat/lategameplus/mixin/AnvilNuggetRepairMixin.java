package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.registry.ModItems;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilNuggetRepairMixin {

    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Unique
    private boolean ne$inUpdate;

    @Inject(method = "createResult", at = @At("RETURN"))
    private void ne$addNuggetRepair(CallbackInfo ci) {
        if (ne$inUpdate) return;
        ne$inUpdate = true;
        try {
            AnvilMenu self = (AnvilMenu)(Object) this;

            ItemStack left  = self.getSlot(0).getItem();
            ItemStack right = self.getSlot(1).getItem();
            Slot outSlot    = self.getSlot(2);
            ItemStack out   = outSlot.getItem();

            if (left.isEmpty() || right.isEmpty()) return;
            if (!left.isDamageableItem()) return;

            if (!right.is(ModItems.NETHERITE_NUGGET)) return;

            boolean vanillaGaveResult = !out.isEmpty();

            boolean isVanillaNetherite =
                    left.is(Items.NETHERITE_SWORD)   ||
                    left.is(Items.NETHERITE_SHOVEL)  ||
                    left.is(Items.NETHERITE_PICKAXE) ||
                    left.is(Items.NETHERITE_AXE)     ||
                    left.is(Items.NETHERITE_HOE)     ||
                    left.is(Items.NETHERITE_HELMET)      ||
                    left.is(Items.NETHERITE_CHESTPLATE)  ||
                    left.is(Items.NETHERITE_LEGGINGS)    ||
                    left.is(Items.NETHERITE_BOOTS);

            if (!vanillaGaveResult && !isVanillaNetherite) return;

            int max    = left.getMaxDamage();
            int damage = left.getDamageValue();
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
            fixed.setDamageValue(Math.max(0, damage - perNugget * use));

            outSlot.setByPlayer(fixed);
            this.repairItemCountCost = use;
            this.cost.set(Math.max(1, use));

        } finally {
            ne$inUpdate = false;
        }
    }
}
