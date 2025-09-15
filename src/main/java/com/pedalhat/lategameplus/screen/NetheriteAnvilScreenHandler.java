package com.pedalhat.lategameplus.screen;

import com.pedalhat.lategameplus.LateGamePlus;
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
    private static final int MAX_COST = 35;

    public NetheriteAnvilScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx) {
        super(syncId, inv, ctx);
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

        int vanillaCost = levelCost.get();
        int finalCost = vanillaCost;
        boolean capped = vanillaCost > MAX_COST;
        if (capped) {
            finalCost = MAX_COST;
            levelCost.set(finalCost);
            LateGamePlus.LOGGER.info(
                "[NetheriteAnvil] cost capped from {} to {}",
                vanillaCost,
                finalCost
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