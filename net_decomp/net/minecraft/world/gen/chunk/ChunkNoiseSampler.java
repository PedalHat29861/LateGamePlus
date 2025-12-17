/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2IntMap
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.OreVeinSampler;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.jspecify.annotations.Nullable;

public class ChunkNoiseSampler
implements DensityFunction.EachApplier,
DensityFunction.NoisePos {
    final int horizontalCellCount;
    final int verticalCellCount;
    final int minimumCellY;
    private final int startCellX;
    private final int startCellZ;
    final int startBiomeX;
    final int startBiomeZ;
    final List<DensityInterpolator> interpolators;
    final List<CellCache> caches;
    private final Map<DensityFunction, DensityFunction> actualDensityFunctionCache = new HashMap<DensityFunction, DensityFunction>();
    private final Long2IntMap surfaceHeightEstimateCache = new Long2IntOpenHashMap();
    private final AquiferSampler aquiferSampler;
    private final DensityFunction preliminarySurfaceLevel;
    private final BlockStateSampler blockStateSampler;
    private final Blender blender;
    private final FlatCache cachedBlendAlphaDensityFunction;
    private final FlatCache cachedBlendOffsetDensityFunction;
    private final DensityFunctionTypes.Beardifying beardifying;
    private long lastBlendingColumnPos = ChunkPos.MARKER;
    private Blender.BlendResult lastBlendingResult = new Blender.BlendResult(1.0, 0.0);
    final int horizontalBiomeEnd;
    final int horizontalCellBlockCount;
    final int verticalCellBlockCount;
    boolean isInInterpolationLoop;
    boolean isSamplingForCaches;
    private int startBlockX;
    int startBlockY;
    private int startBlockZ;
    int cellBlockX;
    int cellBlockY;
    int cellBlockZ;
    long sampleUniqueIndex;
    long cacheOnceUniqueIndex;
    int index;
    private final DensityFunction.EachApplier interpolationEachApplier = new DensityFunction.EachApplier(){

        @Override
        public DensityFunction.NoisePos at(int index) {
            ChunkNoiseSampler.this.startBlockY = (index + ChunkNoiseSampler.this.minimumCellY) * ChunkNoiseSampler.this.verticalCellBlockCount;
            ++ChunkNoiseSampler.this.sampleUniqueIndex;
            ChunkNoiseSampler.this.cellBlockY = 0;
            ChunkNoiseSampler.this.index = index;
            return ChunkNoiseSampler.this;
        }

        @Override
        public void fill(double[] densities, DensityFunction densityFunction) {
            for (int i = 0; i < ChunkNoiseSampler.this.verticalCellCount + 1; ++i) {
                ChunkNoiseSampler.this.startBlockY = (i + ChunkNoiseSampler.this.minimumCellY) * ChunkNoiseSampler.this.verticalCellBlockCount;
                ++ChunkNoiseSampler.this.sampleUniqueIndex;
                ChunkNoiseSampler.this.cellBlockY = 0;
                ChunkNoiseSampler.this.index = i;
                densities[i] = densityFunction.sample(ChunkNoiseSampler.this);
            }
        }
    };

    public static ChunkNoiseSampler create(Chunk chunk, NoiseConfig noiseConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender) {
        GenerationShapeConfig generationShapeConfig = chunkGeneratorSettings.generationShapeConfig().trimHeight(chunk);
        ChunkPos chunkPos = chunk.getPos();
        int i = 16 / generationShapeConfig.horizontalCellBlockCount();
        return new ChunkNoiseSampler(i, noiseConfig, chunkPos.getStartX(), chunkPos.getStartZ(), generationShapeConfig, beardifying, chunkGeneratorSettings, fluidLevelSampler, blender);
    }

    public ChunkNoiseSampler(int horizontalCellCount, NoiseConfig noiseConfig, int startBlockX, int startBlockZ, GenerationShapeConfig generationShapeConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender) {
        int l;
        int k;
        this.horizontalCellBlockCount = generationShapeConfig.horizontalCellBlockCount();
        this.verticalCellBlockCount = generationShapeConfig.verticalCellBlockCount();
        this.horizontalCellCount = horizontalCellCount;
        this.verticalCellCount = MathHelper.floorDiv(generationShapeConfig.height(), this.verticalCellBlockCount);
        this.minimumCellY = MathHelper.floorDiv(generationShapeConfig.minimumY(), this.verticalCellBlockCount);
        this.startCellX = Math.floorDiv(startBlockX, this.horizontalCellBlockCount);
        this.startCellZ = Math.floorDiv(startBlockZ, this.horizontalCellBlockCount);
        this.interpolators = Lists.newArrayList();
        this.caches = Lists.newArrayList();
        this.startBiomeX = BiomeCoords.fromBlock(startBlockX);
        this.startBiomeZ = BiomeCoords.fromBlock(startBlockZ);
        this.horizontalBiomeEnd = BiomeCoords.fromBlock(horizontalCellCount * this.horizontalCellBlockCount);
        this.blender = blender;
        this.beardifying = beardifying;
        this.cachedBlendAlphaDensityFunction = new FlatCache(new BlendAlphaDensityFunction(), false);
        this.cachedBlendOffsetDensityFunction = new FlatCache(new BlendOffsetDensityFunction(), false);
        if (!blender.isEmpty()) {
            for (int i = 0; i <= this.horizontalBiomeEnd; ++i) {
                int j = this.startBiomeX + i;
                k = BiomeCoords.toBlock(j);
                for (l = 0; l <= this.horizontalBiomeEnd; ++l) {
                    int m = this.startBiomeZ + l;
                    int n = BiomeCoords.toBlock(m);
                    Blender.BlendResult blendResult = blender.calculate(k, n);
                    this.cachedBlendAlphaDensityFunction.cache[i + l * this.cachedBlendAlphaDensityFunction.horizontalCacheSize] = blendResult.alpha();
                    this.cachedBlendOffsetDensityFunction.cache[i + l * this.cachedBlendOffsetDensityFunction.horizontalCacheSize] = blendResult.blendingOffset();
                }
            }
        } else {
            Arrays.fill(this.cachedBlendAlphaDensityFunction.cache, 1.0);
            Arrays.fill(this.cachedBlendOffsetDensityFunction.cache, 0.0);
        }
        NoiseRouter noiseRouter = noiseConfig.getNoiseRouter();
        NoiseRouter noiseRouter2 = noiseRouter.apply(this::getActualDensityFunction);
        this.preliminarySurfaceLevel = noiseRouter2.preliminarySurfaceLevel();
        if (!chunkGeneratorSettings.hasAquifers()) {
            this.aquiferSampler = AquiferSampler.seaLevel(fluidLevelSampler);
        } else {
            k = ChunkSectionPos.getSectionCoord(startBlockX);
            l = ChunkSectionPos.getSectionCoord(startBlockZ);
            this.aquiferSampler = AquiferSampler.aquifer(this, new ChunkPos(k, l), noiseRouter2, noiseConfig.getAquiferRandomDeriver(), generationShapeConfig.minimumY(), generationShapeConfig.height(), fluidLevelSampler);
        }
        ArrayList<BlockStateSampler> list = new ArrayList<BlockStateSampler>();
        DensityFunction densityFunction = DensityFunctionTypes.cacheAllInCell(DensityFunctionTypes.add(noiseRouter2.finalDensity(), DensityFunctionTypes.Beardifier.INSTANCE)).apply(this::getActualDensityFunction);
        list.add(pos -> this.aquiferSampler.apply(pos, densityFunction.sample(pos)));
        if (chunkGeneratorSettings.oreVeins()) {
            list.add(OreVeinSampler.create(noiseRouter2.veinToggle(), noiseRouter2.veinRidged(), noiseRouter2.veinGap(), noiseConfig.getOreRandomDeriver()));
        }
        this.blockStateSampler = new ChainedBlockSource(list.toArray(new BlockStateSampler[0]));
    }

    protected MultiNoiseUtil.MultiNoiseSampler createMultiNoiseSampler(NoiseRouter noiseRouter, List<MultiNoiseUtil.NoiseHypercube> spawnTarget) {
        return new MultiNoiseUtil.MultiNoiseSampler(noiseRouter.temperature().apply(this::getActualDensityFunction), noiseRouter.vegetation().apply(this::getActualDensityFunction), noiseRouter.continents().apply(this::getActualDensityFunction), noiseRouter.erosion().apply(this::getActualDensityFunction), noiseRouter.depth().apply(this::getActualDensityFunction), noiseRouter.ridges().apply(this::getActualDensityFunction), spawnTarget);
    }

    protected @Nullable BlockState sampleBlockState() {
        return this.blockStateSampler.sample(this);
    }

    @Override
    public int blockX() {
        return this.startBlockX + this.cellBlockX;
    }

    @Override
    public int blockY() {
        return this.startBlockY + this.cellBlockY;
    }

    @Override
    public int blockZ() {
        return this.startBlockZ + this.cellBlockZ;
    }

    public int estimateHighestSurfaceLevel(int minX, int minZ, int maxX, int maxZ) {
        int i = Integer.MIN_VALUE;
        for (int j = minZ; j <= maxZ; j += 4) {
            for (int k = minX; k <= maxX; k += 4) {
                int l = this.estimateSurfaceHeight(k, j);
                if (l <= i) continue;
                i = l;
            }
        }
        return i;
    }

    public int estimateSurfaceHeight(int blockX, int blockZ) {
        int i = BiomeCoords.toBlock(BiomeCoords.fromBlock(blockX));
        int j = BiomeCoords.toBlock(BiomeCoords.fromBlock(blockZ));
        return this.surfaceHeightEstimateCache.computeIfAbsent(ColumnPos.pack(i, j), this::calculateSurfaceHeightEstimate);
    }

    private int calculateSurfaceHeightEstimate(long columnPos) {
        int i = ColumnPos.getX(columnPos);
        int j = ColumnPos.getZ(columnPos);
        return MathHelper.floor(this.preliminarySurfaceLevel.sample(new DensityFunction.UnblendedNoisePos(i, 0, j)));
    }

    @Override
    public Blender getBlender() {
        return this.blender;
    }

    private void sampleDensity(boolean start, int cellX) {
        this.startBlockX = cellX * this.horizontalCellBlockCount;
        this.cellBlockX = 0;
        for (int i = 0; i < this.horizontalCellCount + 1; ++i) {
            int j = this.startCellZ + i;
            this.startBlockZ = j * this.horizontalCellBlockCount;
            this.cellBlockZ = 0;
            ++this.cacheOnceUniqueIndex;
            for (DensityInterpolator densityInterpolator : this.interpolators) {
                double[] ds = (start ? densityInterpolator.startDensityBuffer : densityInterpolator.endDensityBuffer)[i];
                densityInterpolator.fill(ds, this.interpolationEachApplier);
            }
        }
        ++this.cacheOnceUniqueIndex;
    }

    public void sampleStartDensity() {
        if (this.isInInterpolationLoop) {
            throw new IllegalStateException("Staring interpolation twice");
        }
        this.isInInterpolationLoop = true;
        this.sampleUniqueIndex = 0L;
        this.sampleDensity(true, this.startCellX);
    }

    public void sampleEndDensity(int cellX) {
        this.sampleDensity(false, this.startCellX + cellX + 1);
        this.startBlockX = (this.startCellX + cellX) * this.horizontalCellBlockCount;
    }

    @Override
    public ChunkNoiseSampler at(int i) {
        int j = Math.floorMod(i, this.horizontalCellBlockCount);
        int k = Math.floorDiv(i, this.horizontalCellBlockCount);
        int l = Math.floorMod(k, this.horizontalCellBlockCount);
        int m = this.verticalCellBlockCount - 1 - Math.floorDiv(k, this.horizontalCellBlockCount);
        this.cellBlockX = l;
        this.cellBlockY = m;
        this.cellBlockZ = j;
        this.index = i;
        return this;
    }

    @Override
    public void fill(double[] densities, DensityFunction densityFunction) {
        this.index = 0;
        for (int i = this.verticalCellBlockCount - 1; i >= 0; --i) {
            this.cellBlockY = i;
            for (int j = 0; j < this.horizontalCellBlockCount; ++j) {
                this.cellBlockX = j;
                int k = 0;
                while (k < this.horizontalCellBlockCount) {
                    this.cellBlockZ = k++;
                    densities[this.index++] = densityFunction.sample(this);
                }
            }
        }
    }

    public void onSampledCellCorners(int cellY, int cellZ) {
        for (DensityInterpolator densityInterpolator : this.interpolators) {
            densityInterpolator.onSampledCellCorners(cellY, cellZ);
        }
        this.isSamplingForCaches = true;
        this.startBlockY = (cellY + this.minimumCellY) * this.verticalCellBlockCount;
        this.startBlockZ = (this.startCellZ + cellZ) * this.horizontalCellBlockCount;
        ++this.cacheOnceUniqueIndex;
        for (CellCache cellCache : this.caches) {
            cellCache.delegate.fill(cellCache.cache, this);
        }
        ++this.cacheOnceUniqueIndex;
        this.isSamplingForCaches = false;
    }

    public void interpolateY(int blockY, double deltaY) {
        this.cellBlockY = blockY - this.startBlockY;
        for (DensityInterpolator densityInterpolator : this.interpolators) {
            densityInterpolator.interpolateY(deltaY);
        }
    }

    public void interpolateX(int blockX, double deltaX) {
        this.cellBlockX = blockX - this.startBlockX;
        for (DensityInterpolator densityInterpolator : this.interpolators) {
            densityInterpolator.interpolateX(deltaX);
        }
    }

    public void interpolateZ(int blockZ, double deltaZ) {
        this.cellBlockZ = blockZ - this.startBlockZ;
        ++this.sampleUniqueIndex;
        for (DensityInterpolator densityInterpolator : this.interpolators) {
            densityInterpolator.interpolateZ(deltaZ);
        }
    }

    public void stopInterpolation() {
        if (!this.isInInterpolationLoop) {
            throw new IllegalStateException("Staring interpolation twice");
        }
        this.isInInterpolationLoop = false;
    }

    public void swapBuffers() {
        this.interpolators.forEach(DensityInterpolator::swapBuffers);
    }

    public AquiferSampler getAquiferSampler() {
        return this.aquiferSampler;
    }

    protected int getHorizontalCellBlockCount() {
        return this.horizontalCellBlockCount;
    }

    protected int getVerticalCellBlockCount() {
        return this.verticalCellBlockCount;
    }

    Blender.BlendResult calculateBlendResult(int blockX, int blockZ) {
        Blender.BlendResult blendResult;
        long l = ChunkPos.toLong(blockX, blockZ);
        if (this.lastBlendingColumnPos == l) {
            return this.lastBlendingResult;
        }
        this.lastBlendingColumnPos = l;
        this.lastBlendingResult = blendResult = this.blender.calculate(blockX, blockZ);
        return blendResult;
    }

    protected DensityFunction getActualDensityFunction(DensityFunction function) {
        return this.actualDensityFunctionCache.computeIfAbsent(function, this::getActualDensityFunctionImpl);
    }

    private DensityFunction getActualDensityFunctionImpl(DensityFunction function) {
        if (function instanceof DensityFunctionTypes.Wrapping) {
            DensityFunctionTypes.Wrapping wrapping = (DensityFunctionTypes.Wrapping)function;
            return switch (wrapping.type()) {
                default -> throw new MatchException(null, null);
                case DensityFunctionTypes.Wrapping.Type.INTERPOLATED -> new DensityInterpolator(wrapping.wrapped());
                case DensityFunctionTypes.Wrapping.Type.FLAT_CACHE -> new FlatCache(wrapping.wrapped(), true);
                case DensityFunctionTypes.Wrapping.Type.CACHE2D -> new Cache2D(wrapping.wrapped());
                case DensityFunctionTypes.Wrapping.Type.CACHE_ONCE -> new CacheOnce(wrapping.wrapped());
                case DensityFunctionTypes.Wrapping.Type.CACHE_ALL_IN_CELL -> new CellCache(wrapping.wrapped());
            };
        }
        if (this.blender != Blender.getNoBlending()) {
            if (function == DensityFunctionTypes.BlendAlpha.INSTANCE) {
                return this.cachedBlendAlphaDensityFunction;
            }
            if (function == DensityFunctionTypes.BlendOffset.INSTANCE) {
                return this.cachedBlendOffsetDensityFunction;
            }
        }
        if (function == DensityFunctionTypes.Beardifier.INSTANCE) {
            return this.beardifying;
        }
        if (function instanceof DensityFunctionTypes.RegistryEntryHolder) {
            DensityFunctionTypes.RegistryEntryHolder registryEntryHolder = (DensityFunctionTypes.RegistryEntryHolder)function;
            return registryEntryHolder.function().value();
        }
        return function;
    }

    @Override
    public /* synthetic */ DensityFunction.NoisePos at(int index) {
        return this.at(index);
    }

    class FlatCache
    implements DensityFunctionTypes.Wrapper,
    ParentedNoiseType {
        private final DensityFunction delegate;
        final double[] cache;
        final int horizontalCacheSize;

        FlatCache(DensityFunction delegate, boolean sample) {
            this.delegate = delegate;
            this.horizontalCacheSize = ChunkNoiseSampler.this.horizontalBiomeEnd + 1;
            this.cache = new double[this.horizontalCacheSize * this.horizontalCacheSize];
            if (sample) {
                for (int i = 0; i <= ChunkNoiseSampler.this.horizontalBiomeEnd; ++i) {
                    int j = ChunkNoiseSampler.this.startBiomeX + i;
                    int k = BiomeCoords.toBlock(j);
                    for (int l = 0; l <= ChunkNoiseSampler.this.horizontalBiomeEnd; ++l) {
                        int m = ChunkNoiseSampler.this.startBiomeZ + l;
                        int n = BiomeCoords.toBlock(m);
                        this.cache[i + l * this.horizontalCacheSize] = delegate.sample(new DensityFunction.UnblendedNoisePos(k, 0, n));
                    }
                }
            }
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            int i = BiomeCoords.fromBlock(pos.blockX());
            int j = BiomeCoords.fromBlock(pos.blockZ());
            int k = i - ChunkNoiseSampler.this.startBiomeX;
            int l = j - ChunkNoiseSampler.this.startBiomeZ;
            if (k >= 0 && l >= 0 && k < this.horizontalCacheSize && l < this.horizontalCacheSize) {
                return this.cache[k + l * this.horizontalCacheSize];
            }
            return this.delegate.sample(pos);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public DensityFunction wrapped() {
            return this.delegate;
        }

        @Override
        public DensityFunctionTypes.Wrapping.Type type() {
            return DensityFunctionTypes.Wrapping.Type.FLAT_CACHE;
        }
    }

    class BlendAlphaDensityFunction
    implements ParentedNoiseType {
        BlendAlphaDensityFunction() {
        }

        @Override
        public DensityFunction wrapped() {
            return DensityFunctionTypes.BlendAlpha.INSTANCE;
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return this.wrapped().apply(visitor);
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return ChunkNoiseSampler.this.calculateBlendResult(pos.blockX(), pos.blockZ()).alpha();
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return DensityFunctionTypes.BlendAlpha.CODEC;
        }
    }

    class BlendOffsetDensityFunction
    implements ParentedNoiseType {
        BlendOffsetDensityFunction() {
        }

        @Override
        public DensityFunction wrapped() {
            return DensityFunctionTypes.BlendOffset.INSTANCE;
        }

        @Override
        public DensityFunction apply(DensityFunction.DensityFunctionVisitor visitor) {
            return this.wrapped().apply(visitor);
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            return ChunkNoiseSampler.this.calculateBlendResult(pos.blockX(), pos.blockZ()).blendingOffset();
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return DensityFunctionTypes.BlendOffset.CODEC;
        }
    }

    @FunctionalInterface
    public static interface BlockStateSampler {
        public @Nullable BlockState sample(DensityFunction.NoisePos var1);
    }

    public class DensityInterpolator
    implements DensityFunctionTypes.Wrapper,
    ParentedNoiseType {
        double[][] startDensityBuffer;
        double[][] endDensityBuffer;
        private final DensityFunction delegate;
        private double x0y0z0;
        private double x0y0z1;
        private double x1y0z0;
        private double x1y0z1;
        private double x0y1z0;
        private double x0y1z1;
        private double x1y1z0;
        private double x1y1z1;
        private double x0z0;
        private double x1z0;
        private double x0z1;
        private double x1z1;
        private double z0;
        private double z1;
        private double result;

        DensityInterpolator(DensityFunction delegate) {
            this.delegate = delegate;
            this.startDensityBuffer = this.createBuffer(ChunkNoiseSampler.this.verticalCellCount, ChunkNoiseSampler.this.horizontalCellCount);
            this.endDensityBuffer = this.createBuffer(ChunkNoiseSampler.this.verticalCellCount, ChunkNoiseSampler.this.horizontalCellCount);
            ChunkNoiseSampler.this.interpolators.add(this);
        }

        private double[][] createBuffer(int sizeZ, int sizeX) {
            int i = sizeX + 1;
            int j = sizeZ + 1;
            double[][] ds = new double[i][j];
            for (int k = 0; k < i; ++k) {
                ds[k] = new double[j];
            }
            return ds;
        }

        void onSampledCellCorners(int cellY, int cellZ) {
            this.x0y0z0 = this.startDensityBuffer[cellZ][cellY];
            this.x0y0z1 = this.startDensityBuffer[cellZ + 1][cellY];
            this.x1y0z0 = this.endDensityBuffer[cellZ][cellY];
            this.x1y0z1 = this.endDensityBuffer[cellZ + 1][cellY];
            this.x0y1z0 = this.startDensityBuffer[cellZ][cellY + 1];
            this.x0y1z1 = this.startDensityBuffer[cellZ + 1][cellY + 1];
            this.x1y1z0 = this.endDensityBuffer[cellZ][cellY + 1];
            this.x1y1z1 = this.endDensityBuffer[cellZ + 1][cellY + 1];
        }

        void interpolateY(double deltaY) {
            this.x0z0 = MathHelper.lerp(deltaY, this.x0y0z0, this.x0y1z0);
            this.x1z0 = MathHelper.lerp(deltaY, this.x1y0z0, this.x1y1z0);
            this.x0z1 = MathHelper.lerp(deltaY, this.x0y0z1, this.x0y1z1);
            this.x1z1 = MathHelper.lerp(deltaY, this.x1y0z1, this.x1y1z1);
        }

        void interpolateX(double deltaX) {
            this.z0 = MathHelper.lerp(deltaX, this.x0z0, this.x1z0);
            this.z1 = MathHelper.lerp(deltaX, this.x0z1, this.x1z1);
        }

        void interpolateZ(double deltaZ) {
            this.result = MathHelper.lerp(deltaZ, this.z0, this.z1);
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            if (pos != ChunkNoiseSampler.this) {
                return this.delegate.sample(pos);
            }
            if (!ChunkNoiseSampler.this.isInInterpolationLoop) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            }
            if (ChunkNoiseSampler.this.isSamplingForCaches) {
                return MathHelper.lerp3((double)ChunkNoiseSampler.this.cellBlockX / (double)ChunkNoiseSampler.this.horizontalCellBlockCount, (double)ChunkNoiseSampler.this.cellBlockY / (double)ChunkNoiseSampler.this.verticalCellBlockCount, (double)ChunkNoiseSampler.this.cellBlockZ / (double)ChunkNoiseSampler.this.horizontalCellBlockCount, this.x0y0z0, this.x1y0z0, this.x0y1z0, this.x1y1z0, this.x0y0z1, this.x1y0z1, this.x0y1z1, this.x1y1z1);
            }
            return this.result;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            if (ChunkNoiseSampler.this.isSamplingForCaches) {
                applier.fill(densities, this);
                return;
            }
            this.wrapped().fill(densities, applier);
        }

        @Override
        public DensityFunction wrapped() {
            return this.delegate;
        }

        private void swapBuffers() {
            double[][] ds = this.startDensityBuffer;
            this.startDensityBuffer = this.endDensityBuffer;
            this.endDensityBuffer = ds;
        }

        @Override
        public DensityFunctionTypes.Wrapping.Type type() {
            return DensityFunctionTypes.Wrapping.Type.INTERPOLATED;
        }
    }

    class CellCache
    implements DensityFunctionTypes.Wrapper,
    ParentedNoiseType {
        final DensityFunction delegate;
        final double[] cache;

        CellCache(DensityFunction delegate) {
            this.delegate = delegate;
            this.cache = new double[ChunkNoiseSampler.this.horizontalCellBlockCount * ChunkNoiseSampler.this.horizontalCellBlockCount * ChunkNoiseSampler.this.verticalCellBlockCount];
            ChunkNoiseSampler.this.caches.add(this);
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            if (pos != ChunkNoiseSampler.this) {
                return this.delegate.sample(pos);
            }
            if (!ChunkNoiseSampler.this.isInInterpolationLoop) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            }
            int i = ChunkNoiseSampler.this.cellBlockX;
            int j = ChunkNoiseSampler.this.cellBlockY;
            int k = ChunkNoiseSampler.this.cellBlockZ;
            if (i >= 0 && j >= 0 && k >= 0 && i < ChunkNoiseSampler.this.horizontalCellBlockCount && j < ChunkNoiseSampler.this.verticalCellBlockCount && k < ChunkNoiseSampler.this.horizontalCellBlockCount) {
                return this.cache[((ChunkNoiseSampler.this.verticalCellBlockCount - 1 - j) * ChunkNoiseSampler.this.horizontalCellBlockCount + i) * ChunkNoiseSampler.this.horizontalCellBlockCount + k];
            }
            return this.delegate.sample(pos);
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            applier.fill(densities, this);
        }

        @Override
        public DensityFunction wrapped() {
            return this.delegate;
        }

        @Override
        public DensityFunctionTypes.Wrapping.Type type() {
            return DensityFunctionTypes.Wrapping.Type.CACHE_ALL_IN_CELL;
        }
    }

    static class Cache2D
    implements DensityFunctionTypes.Wrapper,
    ParentedNoiseType {
        private final DensityFunction delegate;
        private long lastSamplingColumnPos = ChunkPos.MARKER;
        private double lastSamplingResult;

        Cache2D(DensityFunction delegate) {
            this.delegate = delegate;
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            double d;
            int j;
            int i = pos.blockX();
            long l = ChunkPos.toLong(i, j = pos.blockZ());
            if (this.lastSamplingColumnPos == l) {
                return this.lastSamplingResult;
            }
            this.lastSamplingColumnPos = l;
            this.lastSamplingResult = d = this.delegate.sample(pos);
            return d;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            this.delegate.fill(densities, applier);
        }

        @Override
        public DensityFunction wrapped() {
            return this.delegate;
        }

        @Override
        public DensityFunctionTypes.Wrapping.Type type() {
            return DensityFunctionTypes.Wrapping.Type.CACHE2D;
        }
    }

    class CacheOnce
    implements DensityFunctionTypes.Wrapper,
    ParentedNoiseType {
        private final DensityFunction delegate;
        private long sampleUniqueIndex;
        private long cacheOnceUniqueIndex;
        private double lastSamplingResult;
        private double @Nullable [] cache;

        CacheOnce(DensityFunction delegate) {
            this.delegate = delegate;
        }

        @Override
        public double sample(DensityFunction.NoisePos pos) {
            double d;
            if (pos != ChunkNoiseSampler.this) {
                return this.delegate.sample(pos);
            }
            if (this.cache != null && this.cacheOnceUniqueIndex == ChunkNoiseSampler.this.cacheOnceUniqueIndex) {
                return this.cache[ChunkNoiseSampler.this.index];
            }
            if (this.sampleUniqueIndex == ChunkNoiseSampler.this.sampleUniqueIndex) {
                return this.lastSamplingResult;
            }
            this.sampleUniqueIndex = ChunkNoiseSampler.this.sampleUniqueIndex;
            this.lastSamplingResult = d = this.delegate.sample(pos);
            return d;
        }

        @Override
        public void fill(double[] densities, DensityFunction.EachApplier applier) {
            if (this.cache != null && this.cacheOnceUniqueIndex == ChunkNoiseSampler.this.cacheOnceUniqueIndex) {
                System.arraycopy(this.cache, 0, densities, 0, densities.length);
                return;
            }
            this.wrapped().fill(densities, applier);
            if (this.cache != null && this.cache.length == densities.length) {
                System.arraycopy(densities, 0, this.cache, 0, densities.length);
            } else {
                this.cache = (double[])densities.clone();
            }
            this.cacheOnceUniqueIndex = ChunkNoiseSampler.this.cacheOnceUniqueIndex;
        }

        @Override
        public DensityFunction wrapped() {
            return this.delegate;
        }

        @Override
        public DensityFunctionTypes.Wrapping.Type type() {
            return DensityFunctionTypes.Wrapping.Type.CACHE_ONCE;
        }
    }

    static interface ParentedNoiseType
    extends DensityFunction {
        public DensityFunction wrapped();

        @Override
        default public double minValue() {
            return this.wrapped().minValue();
        }

        @Override
        default public double maxValue() {
            return this.wrapped().maxValue();
        }
    }
}

