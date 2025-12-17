/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P4
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.AbstractNoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;

public class NoiseBlockStateProvider
extends AbstractNoiseBlockStateProvider {
    public static final MapCodec<NoiseBlockStateProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> NoiseBlockStateProvider.fillNoiseCodecFields(instance).apply((Applicative)instance, NoiseBlockStateProvider::new));
    protected final List<BlockState> states;

    protected static <P extends NoiseBlockStateProvider> Products.P4<RecordCodecBuilder.Mu<P>, Long, DoublePerlinNoiseSampler.NoiseParameters, Float, List<BlockState>> fillNoiseCodecFields(RecordCodecBuilder.Instance<P> instance) {
        return NoiseBlockStateProvider.fillCodecFields(instance).and((App)Codecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("states").forGetter(noiseBlockStateProvider -> noiseBlockStateProvider.states));
    }

    public NoiseBlockStateProvider(long seed, DoublePerlinNoiseSampler.NoiseParameters noiseParameters, float scale, List<BlockState> states) {
        super(seed, noiseParameters, scale);
        this.states = states;
    }

    @Override
    protected BlockStateProviderType<?> getType() {
        return BlockStateProviderType.NOISE_PROVIDER;
    }

    @Override
    public BlockState get(Random random, BlockPos pos) {
        return this.getStateFromList(this.states, pos, this.scale);
    }

    protected BlockState getStateFromList(List<BlockState> states, BlockPos pos, double scale) {
        double d = this.getNoiseValue(pos, scale);
        return this.getStateAtValue(states, d);
    }

    protected BlockState getStateAtValue(List<BlockState> states, double value) {
        double d = MathHelper.clamp((1.0 + value) / 2.0, 0.0, 0.9999);
        return states.get((int)(d * (double)states.size()));
    }
}

