package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.item.DebrisResonatorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class DebrisResonatorAnvilRechargeMixin {

    @Shadow @Final private Property levelCost;
    @Shadow private int repairItemUsage;

    @Unique
    private boolean lategameplus$processingRecharge;

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void lategameplus$applyDebrisRecharge(CallbackInfo ci) {
        if (lategameplus$processingRecharge) {
            return;
        }
        lategameplus$processingRecharge = true;
        try {
            AnvilScreenHandler self = (AnvilScreenHandler)(Object) this;

            Slot baseSlot = self.getSlot(0);
            Slot additionSlot = self.getSlot(1);
            Slot outputSlot = self.getSlot(2);

            ItemStack baseStack = baseSlot.getStack();
            ItemStack additionStack = additionSlot.getStack();

            if (baseStack.isEmpty() || additionStack.isEmpty()) {
                return;
            }
            if (!(baseStack.getItem() instanceof DebrisResonatorItem)) {
                return;
            }

            boolean usesEchoShard = additionStack.isOf(Items.ECHO_SHARD);
            boolean usesAmethystShard = additionStack.isOf(Items.AMETHYST_SHARD);

            if (!usesEchoShard && !usesAmethystShard) {
                return;
            }

            int currentBattery = DebrisResonatorItem.getBatterySeconds(baseStack);
            int maxBattery = DebrisResonatorItem.getMaxBatterySeconds();

            if (usesEchoShard) {
                if (currentBattery >= maxBattery) {
                    return;
                }

                ItemStack result = baseStack.copy();
                DebrisResonatorItem.setBatterySeconds(result, maxBattery);
                outputSlot.setStack(result);
                this.repairItemUsage = 1;
                this.levelCost.set(5);
                self.sendContentUpdates();
                return;
            }

            if (usesAmethystShard) {
                if (currentBattery >= maxBattery) {
                    return;
                }

                int chunk = Math.max(1, Math.ceilDiv(maxBattery, 60));
                int missing = Math.max(0, maxBattery - currentBattery);
                if (chunk <= 0 || missing <= 0) {
                    return;
                }

                int piecesNeeded = Math.ceilDiv(missing, chunk);
                int use = MathHelper.clamp(additionStack.getCount(), 1, piecesNeeded);
                int restored = chunk * use;
                if (restored <= 0) {
                    return;
                }

                ItemStack result = baseStack.copy();
                DebrisResonatorItem.addBatterySeconds(result, restored);
                outputSlot.setStack(result);
                this.repairItemUsage = use;
                int xpCost = MathHelper.clamp(use, 1, 60);
                this.levelCost.set(xpCost);
                self.sendContentUpdates();
            }
        } finally {
            lategameplus$processingRecharge = false;
        }
    }
}
