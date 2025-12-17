/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.provider.number;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.random.Random;

public record BinomialLootNumberProvider(LootNumberProvider n, LootNumberProvider p) implements LootNumberProvider
{
    public static final MapCodec<BinomialLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootNumberProviderTypes.CODEC.fieldOf("n").forGetter(BinomialLootNumberProvider::n), (App)LootNumberProviderTypes.CODEC.fieldOf("p").forGetter(BinomialLootNumberProvider::p)).apply((Applicative)instance, BinomialLootNumberProvider::new));

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.BINOMIAL;
    }

    @Override
    public int nextInt(LootContext context) {
        int i = this.n.nextInt(context);
        float f = this.p.nextFloat(context);
        Random random = context.getRandom();
        int j = 0;
        for (int k = 0; k < i; ++k) {
            if (!(random.nextFloat() < f)) continue;
            ++j;
        }
        return j;
    }

    @Override
    public float nextFloat(LootContext context) {
        return this.nextInt(context);
    }

    public static BinomialLootNumberProvider create(int n, float p) {
        return new BinomialLootNumberProvider(ConstantLootNumberProvider.create(n), ConstantLootNumberProvider.create(p));
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Sets.union(this.n.getAllowedParameters(), this.p.getAllowedParameters());
    }
}

