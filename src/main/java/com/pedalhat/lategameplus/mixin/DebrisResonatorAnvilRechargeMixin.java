package com.pedalhat.lategameplus.mixin;

import com.pedalhat.lategameplus.item.DebrisResonatorItem;
import net.minecraft.util.Mth;
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
public abstract class DebrisResonatorAnvilRechargeMixin {
    @Unique
    private static final int LGP$FULL_RECHARGE_MAX_XP_COST = 10;

    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Unique
    private boolean lategameplus$processingRecharge;

    @Inject(method = "createResult", at = @At("RETURN"))
    private void lategameplus$applyDebrisRecharge(CallbackInfo ci) {
        if (lategameplus$processingRecharge) {
            return;
        }
        lategameplus$processingRecharge = true;
        try {
            AnvilMenu self = (AnvilMenu)(Object) this;

            Slot baseSlot = self.getSlot(0);
            Slot additionSlot = self.getSlot(1);
            Slot outputSlot = self.getSlot(2);

            ItemStack baseStack = baseSlot.getItem();
            ItemStack additionStack = additionSlot.getItem();

            if (baseStack.isEmpty() || additionStack.isEmpty()) {
                return;
            }
            if (!(baseStack.getItem() instanceof DebrisResonatorItem)) {
                return;
            }

            boolean usesEchoShard = additionStack.is(Items.ECHO_SHARD);
            boolean usesAmethystShard = additionStack.is(Items.AMETHYST_SHARD);

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
                outputSlot.setByPlayer(result);
                this.repairItemCountCost = 1;
                this.cost.set(LGP$FULL_RECHARGE_MAX_XP_COST);
                self.broadcastChanges();
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
                int use = Mth.clamp(additionStack.getCount(), 1, piecesNeeded);
                int restored = chunk * use;
                if (restored <= 0) {
                    return;
                }

                ItemStack result = baseStack.copy();
                DebrisResonatorItem.addBatterySeconds(result, restored);
                outputSlot.setByPlayer(result);
                this.repairItemCountCost = use;
                int fullPieces = Math.max(1, Math.ceilDiv(maxBattery, chunk));
                int xpCost = Math.round((use * (float) LGP$FULL_RECHARGE_MAX_XP_COST) / fullPieces);
                xpCost = Mth.clamp(xpCost, 1, LGP$FULL_RECHARGE_MAX_XP_COST);
                this.cost.set(xpCost);
                self.broadcastChanges();
            }
        } finally {
            lategameplus$processingRecharge = false;
        }
    }
}
