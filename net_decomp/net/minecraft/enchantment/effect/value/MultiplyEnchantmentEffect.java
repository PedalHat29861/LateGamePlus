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

public record MultiplyEnchantmentEffect(EnchantmentLevelBasedValue factor) implements EnchantmentValueEffect
{
    public static final MapCodec<MultiplyEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EnchantmentLevelBasedValue.CODEC.fieldOf("factor").forGetter(MultiplyEnchantmentEffect::factor)).apply((Applicative)instance, MultiplyEnchantmentEffect::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        return inputValue * this.factor.getValue(level);
    }

    public MapCodec<MultiplyEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

