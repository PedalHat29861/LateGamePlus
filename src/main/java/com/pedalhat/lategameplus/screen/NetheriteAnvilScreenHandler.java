package com.pedalhat.lategameplus.screen;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.mixin.AnvilScreenHandlerAccessor;
import com.pedalhat.lategameplus.registry.ModBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class NetheriteAnvilScreenHandler extends AnvilScreenHandler {
    private static final int MIN_CAP = 20;
    private static final int MAX_CAP = 39;

    public NetheriteAnvilScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx) {
        super(syncId, inv, ctx);
    }

    private static int resolveMaxCost() {
        return Math.max(MIN_CAP, Math.min(MAX_CAP, ConfigManager.get().netheriteAnvilMaxLevelCost));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return ScreenHandler.canUse(this.context, player, ModBlocks.NETHERITE_ANVIL);
    }

    @Override
    public void updateResult() {
        AnvilScreenHandlerAccessor accessor = (AnvilScreenHandlerAccessor) (Object) this;
        Property levelCost = accessor.getLevelCost();
        ItemStack baseInput = this.getSlot(INPUT_1_ID).getStack();
        ItemStack additionInput = this.getSlot(INPUT_2_ID).getStack();
        ItemStack outputBefore = this.getSlot(OUTPUT_ID).getStack();
        int previousCost = levelCost.get();

        super.updateResult();

        int maxCost = resolveMaxCost();
        int vanillaCost = levelCost.get();
        int finalCost = vanillaCost;
        boolean capped = vanillaCost > maxCost;
        if (capped) {
            finalCost = maxCost;
            levelCost.set(finalCost);
            LateGamePlus.LOGGER.info(
                "[NetheriteAnvil] cost capped from {} to {} (max allowed {})",
                vanillaCost,
                finalCost,
                maxCost
            );
            this.sendContentUpdates();
        }

        int playerLevels = this.player != null ? this.player.experienceLevel : -1;

        LateGamePlus.LOGGER.info(
            "[NetheriteAnvil] updateResult | in0={} in1={} outBefore={} outAfter={} prevCost={} vanillaCost={} finalCost={} keepSecondSlot={} playerLevels={}",
            baseInput,
            additionInput,
            outputBefore,
            this.getSlot(OUTPUT_ID).getStack(),
            previousCost,
            vanillaCost,
            finalCost,
            accessor.getKeepSecondSlot(),
            playerLevels
        );
    }

    @Override
    public boolean canTakeOutput(PlayerEntity player, boolean present) {
        AnvilScreenHandlerAccessor accessor = (AnvilScreenHandlerAccessor) (Object) this;
        Property levelCost = accessor.getLevelCost();
        int cost = levelCost.get();
        boolean allowed = cost > 0 && (player.getAbilities().creativeMode || player.experienceLevel >= cost);

        LateGamePlus.LOGGER.info(
            "[NetheriteAnvil] canTakeOutput | playerLevels={} creative={} cost={} present={} allowed={}",
            player.experienceLevel,
            player.getAbilities().creativeMode,
            cost,
            present,
            allowed
        );

        return allowed;
    }
}
