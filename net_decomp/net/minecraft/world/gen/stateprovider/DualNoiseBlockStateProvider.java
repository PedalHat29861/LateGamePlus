/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.stateprovider;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.stateprovider.NoiseBlockStateProvider;

public class DualNoiseBlockStateProvider
extends NoiseBlockStateProvider {
    public static final MapCodec<DualNoiseBlockStateProvider> DUAL_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Range.createRangedCodec(Codec.INT, 1, 64).fieldOf("variety").forGetter(dualNoiseBlockStateProvider -> dualNoiseBlockStateProvider.variety), (App)DoublePerlinNoiseSampler.NoiseParameters.CODEC.fieldOf("slow_noise").forGetter(dualNoiseBlockStateProvider -> dualNoiseBlockStateProvider.slowNoiseParameters), (App)Codecs.POSITIVE_FLOAT.fieldOf("slow_scale").forGetter(dualNoiseBlockStateProvider -> Float.valueOf(dualNoiseBlockStateProvider.slowScale))).and(DualNoiseBlockStateProvider.fillNoiseCodecFields(instance)).apply((Applicative)instance, DualNoiseBlockStateProvider::new));
    private final Range<Integer> variety;
    private final DoublePerlinNoiseSampler.NoiseParameters slowNoiseParameters;
    private final float slowScale;
    private final DoublePerlinNoiseSampler slowNoiseSampler;

    public DualNoiseBlockStateProvider(Range<Integer> variety, DoublePerlinNoiseSampler.NoiseParameters slowNoiseParameters, float slowScale, long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, List<BlockState> states) {
        super(seed, noiseParameters, scale, states);
        this.variety = variety;
        this.slowNoiseParameters = slowNoiseParameters;
        this.slowScale = slowScale;
        this.slowNoiseSampler = DoublePerlinNoiseSampler.create(new ChunkRandom(new CheckedRandom(seed)), slowNoiseParameters);
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.DUAL_NOISE_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        double d = this.getSlowNoiseValue(pos);
        int i = (int)MathHelper.clampedMap(d, -1.0, 1.0, (double)this.variety.minInclusive().intValue(), (double)(this.variety.maxInclusive() + 1));
        ArrayList list = Lists.newArrayListWithCapacity((int)i);
        for (int j = 0; j < i; ++j) {
            list.add(this.getStateAtValue(this.states, this.getSlowNoiseValue(pos.add(j * 54545, 0, j * 34234))));
        }
        return this.getStateFromList(list, pos, this.scale);
    }

    protected double getSlowNoiseValue(BlockPos pos) {
        return this.slowNoiseSampler.sample((float)pos.getX() * this.slowScale, (float)pos.getY() * this.slowScale, (float)pos.getZ() * this.slowScale);
    }
}

