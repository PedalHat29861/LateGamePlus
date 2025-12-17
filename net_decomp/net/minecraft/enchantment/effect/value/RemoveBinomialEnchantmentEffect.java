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

public record RemoveBinomialEnchantmentEffect(EnchantmentLevelBasedValue chance) implements EnchantmentValueEffect
{
    public static final MapCodec<RemoveBinomialEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EnchantmentLevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomialEnchantmentEffect::chance)).apply((Applicative)instance, RemoveBinomialEnchantmentEffect::new));

    @Override
    public float apply(int level, Random random, float inputValue) {
        float f = this.chance.getValue(level);
        int i = 0;
        if (inputValue <= 128.0f || inputValue * f < 20.0f || inputValue * (1.0f - f) < 20.0f) {
            int j = 0;
            while ((float)j < inputValue) {
                if (random.nextFloat() < f) {
                    ++i;
                }
                ++j;
            }
        } else {
            double d = Math.floor(inputValue * f);
            double e = Math.sqrt(inputValue * f * (1.0f - f));
            i = (int)Math.round(d + random.nextGaussian() * e);
            i = Math.clamp((long)i, 0, (int)inputValue);
        }
        return inputValue - (float)i;
    }

    public MapCodec<RemoveBinomialEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

