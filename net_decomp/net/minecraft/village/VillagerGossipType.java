/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.village;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class VillagerGossipType
extends Enum<VillagerGossipType>
implements StringIdentifiable {
    public static final /* enum */ VillagerGossipType MAJOR_NEGATIVE = new VillagerGossipType("major_negative", -5, 100, 10, 10);
    public static final /* enum */ VillagerGossipType MINOR_NEGATIVE = new VillagerGossipType("minor_negative", -1, 200, 20, 20);
    public static final /* enum */ VillagerGossipType MINOR_POSITIVE = new VillagerGossipType("minor_positive", 1, 25, 1, 5);
    public static final /* enum */ VillagerGossipType MAJOR_POSITIVE = new VillagerGossipType("major_positive", 5, 20, 0, 20);
    public static final /* enum */ VillagerGossipType TRADING = new VillagerGossipType("trading", 1, 25, 2, 20);
    public static final int MAX_TRADING_REPUTATION = 25;
    public static final int TRADING_GOSSIP_SHARE_DECREMENT = 20;
    public static final int TRADING_GOSSIP_DECAY = 2;
    public final String id;
    public final int multiplier;
    public final int maxValue;
    public final int decay;
    public final int shareDecrement;
    public static final Codec<VillagerGossipType> CODEC;
    private static final /* synthetic */ VillagerGossipType[] field_18436;

    public static VillagerGossipType[] values() {
        return (VillagerGossipType[])field_18436.clone();
    }

    public static VillagerGossipType valueOf(String string) {
        return Enum.valueOf(VillagerGossipType.class, string);
    }

    private VillagerGossipType(String id, int multiplier, int maxReputation, int decay, int shareDecrement) {
        this.id = id;
        this.multiplier = multiplier;
        this.maxValue = maxReputation;
        this.decay = decay;
        this.shareDecrement = shareDecrement;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private static /* synthetic */ VillagerGossipType[] method_36623() {
        return new VillagerGossipType[]{MAJOR_NEGATIVE, MINOR_NEGATIVE, MINOR_POSITIVE, MAJOR_POSITIVE, TRADING};
    }

    static {
        field_18436 = VillagerGossipType.method_36623();
        CODEC = StringIdentifiable.createCodec(VillagerGossipType::values);
    }
}

