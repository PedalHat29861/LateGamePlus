/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

public record EquipmentDropChances(Map<EquipmentSlot, Float> byEquipment) {
    public static final float DEFAULT_CHANCE = 0.085f;
    public static final float UNHARMED_DROP_THRESHOLD = 1.0f;
    public static final int GUARANTEED_DROP_CHANCE = 2;
    public static final EquipmentDropChances DEFAULT = new EquipmentDropChances(Util.mapEnum(EquipmentSlot.class, slot -> Float.valueOf(0.085f)));
    public static final Codec<EquipmentDropChances> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, Codecs.NON_NEGATIVE_FLOAT).xmap(EquipmentDropChances::getWithDefaultChances, EquipmentDropChances::getWithoutDefaultChances).xmap(EquipmentDropChances::new, EquipmentDropChances::byEquipment);

    private static Map<EquipmentSlot, Float> getWithoutDefaultChances(Map<EquipmentSlot, Float> byEquipment) {
        HashMap<EquipmentSlot, Float> map = new HashMap<EquipmentSlot, Float>(byEquipment);
        map.values().removeIf(chance -> chance.floatValue() == 0.085f);
        return map;
    }

    private static Map<EquipmentSlot, Float> getWithDefaultChances(Map<EquipmentSlot, Float> byEquipment) {
        return Util.mapEnum(EquipmentSlot.class, slot -> byEquipment.getOrDefault(slot, Float.valueOf(0.085f)));
    }

    public EquipmentDropChances withGuaranteed(EquipmentSlot slot) {
        return this.withChance(slot, 2.0f);
    }

    public EquipmentDropChances withChance(EquipmentSlot slot, float chance) {
        if (chance < 0.0f) {
            throw new IllegalArgumentException("Tried to set invalid equipment chance " + chance + " for " + String.valueOf(slot));
        }
        if (this.get(slot) == chance) {
            return this;
        }
        return new EquipmentDropChances(Util.mapEnum(EquipmentSlot.class, slotx -> Float.valueOf(slotx == slot ? chance : this.get((EquipmentSlot)slotx))));
    }

    public float get(EquipmentSlot slot) {
        return this.byEquipment.getOrDefault(slot, Float.valueOf(0.085f)).floatValue();
    }

    public boolean dropsExactly(EquipmentSlot slot) {
        return this.get(slot) > 1.0f;
    }
}

