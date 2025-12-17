/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.world.gen.heightprovider;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import org.slf4j.Logger;

public class UniformHeightProvider
extends HeightProvider {
    public static final MapCodec<UniformHeightProvider> UNIFORM_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)YOffset.OFFSET_CODEC.fieldOf("min_inclusive").forGetter(provider -> provider.minOffset), (App)YOffset.OFFSET_CODEC.fieldOf("max_inclusive").forGetter(provider -> provider.maxOffset)).apply((Applicative)instance, UniformHeightProvider::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final YOffset minOffset;
    private final YOffset maxOffset;
    private final LongSet warnedEmptyHeightRanges = new LongOpenHashSet();

    private UniformHeightProvider(YOffset minOffset, YOffset maxOffset) {
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
    }

    public static UniformHeightProvider create(YOffset minOffset, YOffset maxOffset) {
        return new UniformHeightProvider(minOffset, maxOffset);
    }

    @Override
    public int get(Random random, HeightContext context) {
        int j;
        int i = this.minOffset.getY(context);
        if (i > (j = this.maxOffset.getY(context))) {
            if (this.warnedEmptyHeightRanges.add((long)i << 32 | (long)j)) {
                LOGGER.warn("Empty height range: {}", (Object)this);
            }
            return i;
        }
        return MathHelper.nextBetween(random, i, j);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + String.valueOf(this.minOffset) + "-" + String.valueOf(this.maxOffset) + "]";
    }
}

