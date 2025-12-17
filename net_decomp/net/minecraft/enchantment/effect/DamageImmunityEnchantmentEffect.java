/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.enchantment.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record DamageImmunityEnchantmentEffect() {
    public static final DamageImmunityEnchantmentEffect INSTANCE = new DamageImmunityEnchantmentEffect();
    public static final Codec<DamageImmunityEnchantmentEffect> CODEC = MapCodec.unitCodec((Object)INSTANCE);
}

