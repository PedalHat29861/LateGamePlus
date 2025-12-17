/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.village;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.village.TradeOffer;
import org.jspecify.annotations.Nullable;

public class TradeOfferList
extends ArrayList<TradeOffer> {
    public static final Codec<TradeOfferList> CODEC = TradeOffer.CODEC.listOf().optionalFieldOf("Recipes", List.of()).xmap(TradeOfferList::new, Function.identity()).codec();
    public static final PacketCodec<RegistryByteBuf, TradeOfferList> PACKET_CODEC = TradeOffer.PACKET_CODEC.collect(PacketCodecs.toCollection(TradeOfferList::new));

    public TradeOfferList() {
    }

    private TradeOfferList(int size) {
        super(size);
    }

    private TradeOfferList(Collection<TradeOffer> tradeOffers) {
        super(tradeOffers);
    }

    public @Nullable TradeOffer getValidOffer(ItemStack firstBuyItem, ItemStack secondBuyItem, int index) {
        if (index > 0 && index < this.size()) {
            TradeOffer tradeOffer = (TradeOffer)this.get(index);
            if (tradeOffer.matchesBuyItems(firstBuyItem, secondBuyItem)) {
                return tradeOffer;
            }
            return null;
        }
        for (int i = 0; i < this.size(); ++i) {
            TradeOffer tradeOffer2 = (TradeOffer)this.get(i);
            if (!tradeOffer2.matchesBuyItems(firstBuyItem, secondBuyItem)) continue;
            return tradeOffer2;
        }
        return null;
    }

    public TradeOfferList copy() {
        TradeOfferList tradeOfferList = new TradeOfferList(this.size());
        for (TradeOffer tradeOffer : this) {
            tradeOfferList.add(tradeOffer.copy());
        }
        return tradeOfferList;
    }
}

