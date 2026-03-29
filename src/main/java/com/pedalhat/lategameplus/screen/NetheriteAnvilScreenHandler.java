package com.pedalhat.lategameplus.screen;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.mixin.AnvilScreenHandlerAccessor;
import com.pedalhat.lategameplus.registry.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public class NetheriteAnvilScreenHandler extends AnvilMenu {
    private static final int MIN_CAP = 20;
    private static final int MAX_CAP = 39;

    public NetheriteAnvilScreenHandler(int syncId, Inventory inv, ContainerLevelAccess ctx) {
        super(syncId, inv, ctx);
    }

    private static int resolveMaxCost() {
        return Math.max(MIN_CAP, Math.min(MAX_CAP, ConfigManager.get().netheriteAnvilMaxLevelCost));
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.NETHERITE_ANVIL);
    }

    @Override
    public void createResult() {
        AnvilScreenHandlerAccessor accessor = (AnvilScreenHandlerAccessor) (Object) this;
        DataSlot levelCost = accessor.getLevelCost();
        ItemStack baseInput = this.getSlot(INPUT_SLOT).getItem();
        ItemStack additionInput = this.getSlot(ADDITIONAL_SLOT).getItem();
        ItemStack outputBefore = this.getSlot(RESULT_SLOT).getItem();
        int previousCost = levelCost.get();

        super.createResult();

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
            this.broadcastChanges();
        }

        int playerLevels = this.player != null ? this.player.experienceLevel : -1;

        LateGamePlus.LOGGER.info(
            "[NetheriteAnvil] updateResult | in0={} in1={} outBefore={} outAfter={} prevCost={} vanillaCost={} finalCost={} keepSecondSlot={} playerLevels={}",
            baseInput,
            additionInput,
            outputBefore,
            this.getSlot(RESULT_SLOT).getItem(),
            previousCost,
            vanillaCost,
            finalCost,
            accessor.getKeepSecondSlot(),
            playerLevels
        );
    }

    @Override
    public boolean mayPickup(Player player, boolean present) {
        AnvilScreenHandlerAccessor accessor = (AnvilScreenHandlerAccessor) (Object) this;
        DataSlot levelCost = accessor.getLevelCost();
        int cost = levelCost.get();
        boolean allowed = cost > 0 && (player.getAbilities().instabuild || player.experienceLevel >= cost);

        LateGamePlus.LOGGER.info(
            "[NetheriteAnvil] canTakeOutput | playerLevels={} creative={} cost={} present={} allowed={}",
            player.experienceLevel,
            player.getAbilities().instabuild,
            cost,
            present,
            allowed
        );

        return allowed;
    }
}
