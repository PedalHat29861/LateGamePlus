/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.village;

import java.util.OptionalInt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jspecify.annotations.Nullable;

public interface Merchant {
    public void setCustomer(@Nullable PlayerEntity var1);

    public @Nullable PlayerEntity getCustomer();

    public TradeOfferList getOffers();

    public void setOffersFromServer(TradeOfferList var1);

    public void trade(TradeOffer var1);

    public void onSellingItem(ItemStack var1);

    public int getExperience();

    public void setExperienceFromServer(int var1);

    public boolean isLeveledMerchant();

    public SoundEvent getYesSound();

    default public boolean canRefreshTrades() {
        return false;
    }

    default public void sendOffers(PlayerEntity player, Text name, int levelProgress) {
        TradeOfferList tradeOfferList;
        OptionalInt optionalInt = player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerx) -> new MerchantScreenHandler(syncId, playerInventory, this), name));
        if (optionalInt.isPresent() && !(tradeOfferList = this.getOffers()).isEmpty()) {
            player.sendTradeOffers(optionalInt.getAsInt(), tradeOfferList, levelProgress, this.getExperience(), this.isLeveledMerchant(), this.canRefreshTrades());
        }
    }

    public boolean isClient();

    public boolean canInteract(PlayerEntity var1);
}

