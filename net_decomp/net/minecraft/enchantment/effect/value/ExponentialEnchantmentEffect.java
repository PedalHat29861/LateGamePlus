/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.enchantment.effect.value;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.util.math.random.Random;

public record ExponentialEnchantmentEffect(EnchantmentLevelBasedValue base, EnchantmentLevelBasedValue exponent) implements EnchantmentValueEffect
{
    public static final MapCodec<ExponentialEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EnchantmentLevelBasedValue.CODEC.fieldOf("base").forGetter(ExponentialEnchantmentEffect::base), (App)EnchantmentLevelBasedValue.CODEC.fieldOf("exponent").forGetter(ExponentialEnchantmentEffect::exponent)).apply((Applicative)instance, ExponentialEnchantmentEffect::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        return (float)((double)inputValue * Math.pow(this.base.getValue(level), this.exponent.getValue(level)));
    }

    public MapCodec<ExponentialEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

