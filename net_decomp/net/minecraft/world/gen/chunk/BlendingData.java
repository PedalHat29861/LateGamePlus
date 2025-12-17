/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Doubles
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.doubles.DoubleArrays
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen.chunk;

import com.google.common.primitives.Doubles;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EightWayDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jspecify.annotations.Nullable;

public class BlendingData {
    private static final double field_35514 = 0.1;
    protected static final int field_36280 = 4;
    protected static final int field_35511 = 8;
    protected static final int field_36281 = 2;
    private static final double field_37704 = 1.0;
    private static final double field_37705 = -1.0;
    private static final int field_35516 = 2;
    private static final int BIOMES_PER_CHUNK = BiomeCoords.fromBlock(16);
    private static final int LAST_CHUNK_BIOME_INDEX = BIOMES_PER_CHUNK - 1;
    private static final int CHUNK_BIOME_END_INDEX = BIOMES_PER_CHUNK;
    private static final int NORTH_WEST_END_INDEX = 2 * LAST_CHUNK_BIOME_INDEX + 1;
    private static final int SOUTH_EAST_END_INDEX_PART = 2 * CHUNK_BIOME_END_INDEX + 1;
    static final int HORIZONTAL_BIOME_COUNT = NORTH_WEST_END_INDEX + SOUTH_EAST_END_INDEX_PART;
    private final HeightLimitView oldHeightLimit;
    private static final List<Block> SURFACE_BLOCKS = List.of(Blocks.PODZOL, Blocks.GRAVEL, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COARSE_DIRT, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.DIRT);
    protected static final double field_35513 = Double.MAX_VALUE;
    private boolean initializedBlendingData;
    private final double[] surfaceHeights;
    private final List<@Nullable List<@Nullable RegistryEntry<Biome>>> biomes;
    private final transient double[][] collidableBlockDensities;

    private BlendingData(int oldBottomSectionY, int oldTopSectionY, Optional<double[]> heights) {
        this.surfaceHeights = heights.orElseGet(() -> Util.make(new double[HORIZONTAL_BIOME_COUNT], ds -> Arrays.fill(ds, Double.MAX_VALUE)));
        this.collidableBlockDensities = new double[HORIZONTAL_BIOME_COUNT][];
        ObjectArrayList objectArrayList = new ObjectArrayList(HORIZONTAL_BIOME_COUNT);
        objectArrayList.size(HORIZONTAL_BIOME_COUNT);
        this.biomes = objectArrayList;
        int i = ChunkSectionPos.getBlockCoord(oldBottomSectionY);
        int j = ChunkSectionPos.getBlockCoord(oldTopSectionY) - i;
        this.oldHeightLimit = HeightLimitView.create(i, j);
    }

    public static @Nullable BlendingData fromSerialized(@Nullable Serialized serialized) {
        if (serialized == null) {
            return null;
        }
        return new BlendingData(serialized.minSection(), serialized.maxSection(), serialized.heights());
    }

    public Serialized toSerialized() {
        boolean bl = false;
        for (double d : this.surfaceHeights) {
            if (d == Double.MAX_VALUE) continue;
            bl = true;
            break;
        }
        return new Serialized(this.oldHeightLimit.getBottomSectionCoord(), this.oldHeightLimit.getTopSectionCoord() + 1, bl ? Optional.of(DoubleArrays.copy((double[])this.surfaceHeights)) : Optional.empty());
    }

    public static @Nullable BlendingData getBlendingData(ChunkRegion chunkRegion, int chunkX, int chunkZ) {
        Chunk chunk = chunkRegion.getChunk(chunkX, chunkZ);
        BlendingData blendingData = chunk.getBlendingData();
        if (blendingData == null || chunk.getMaxStatus().isEarlierThan(ChunkStatus.BIOMES)) {
            return null;
        }
        blendingData.initChunkBlendingData(chunk, BlendingData.getAdjacentChunksWithNoise(chunkRegion, chunkX, chunkZ, false));
        return blendingData;
    }

    public static Set<EightWayDirection> getAdjacentChunksWithNoise(StructureWorldAccess access, int chunkX, int chunkZ, boolean oldNoise) {
        EnumSet<EightWayDirection> set = EnumSet.noneOf(EightWayDirection.class);
        for (EightWayDirection eightWayDirection : EightWayDirection.values()) {
            int j;
            int i = chunkX + eightWayDirection.getOffsetX();
            if (access.getChunk(i, j = chunkZ + eightWayDirection.getOffsetZ()).usesOldNoise() != oldNoise) continue;
            set.add(eightWayDirection);
        }
        return set;
    }

    private void initChunkBlendingData(Chunk chunk, Set<EightWayDirection> newNoiseChunkDirections) {
        int i;
        if (this.initializedBlendingData) {
            return;
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.NORTH) || newNoiseChunkDirections.contains((Object)EightWayDirection.WEST) || newNoiseChunkDirections.contains((Object)EightWayDirection.NORTH_WEST)) {
            this.initBlockColumn(BlendingData.getNorthWestIndex(0, 0), chunk, 0, 0);
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.NORTH)) {
            for (i = 1; i < BIOMES_PER_CHUNK; ++i) {
                this.initBlockColumn(BlendingData.getNorthWestIndex(i, 0), chunk, 4 * i, 0);
            }
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.WEST)) {
            for (i = 1; i < BIOMES_PER_CHUNK; ++i) {
                this.initBlockColumn(BlendingData.getNorthWestIndex(0, i), chunk, 0, 4 * i);
            }
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.EAST)) {
            for (i = 1; i < BIOMES_PER_CHUNK; ++i) {
                this.initBlockColumn(BlendingData.getSouthEastIndex(CHUNK_BIOME_END_INDEX, i), chunk, 15, 4 * i);
            }
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.SOUTH)) {
            for (i = 0; i < BIOMES_PER_CHUNK; ++i) {
                this.initBlockColumn(BlendingData.getSouthEastIndex(i, CHUNK_BIOME_END_INDEX), chunk, 4 * i, 15);
            }
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.EAST) && newNoiseChunkDirections.contains((Object)EightWayDirection.NORTH_EAST)) {
            this.initBlockColumn(BlendingData.getSouthEastIndex(CHUNK_BIOME_END_INDEX, 0), chunk, 15, 0);
        }
        if (newNoiseChunkDirections.contains((Object)EightWayDirection.EAST) && newNoiseChunkDirections.contains((Object)EightWayDirection.SOUTH) && newNoiseChunkDirections.contains((Object)EightWayDirection.SOUTH_EAST)) {
            this.initBlockColumn(BlendingData.getSouthEastIndex(CHUNK_BIOME_END_INDEX, CHUNK_BIOME_END_INDEX), chunk, 15, 15);
        }
        this.initializedBlendingData = true;
    }

    private void initBlockColumn(int index, Chunk chunk, int chunkBlockX, int chunkBlockZ) {
        if (this.surfaceHeights[index] == Double.MAX_VALUE) {
            this.surfaceHeights[index] = this.getSurfaceBlockY(chunk, chunkBlockX, chunkBlockZ);
        }
        this.collidableBlockDensities[index] = this.calculateCollidableBlockDensityColumn(chunk, chunkBlockX, chunkBlockZ, MathHelper.floor(this.surfaceHeights[index]));
        this.biomes.set(index, this.getVerticalBiomeSections(chunk, chunkBlockX, chunkBlockZ));
    }

    private int getSurfaceBlockY(Chunk chunk, int blockX, int blockZ) {
        int i = chunk.hasHeightmap(Heightmap.Type.WORLD_SURFACE_WG) ? Math.min(chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, blockX, blockZ), this.oldHeightLimit.getTopYInclusive()) : this.oldHeightLimit.getTopYInclusive();
        int j = this.oldHeightLimit.getBottomY();
        BlockPos.Mutable mutable = new BlockPos.Mutable(blockX, i, blockZ);
        while (mutable.getY() > j) {
            if (SURFACE_BLOCKS.contains(chunk.getBlockState(mutable).getBlock())) {
                return mutable.getY();
            }
            mutable.move(Direction.DOWN);
        }
        return j;
    }

    private static double getAboveCollidableBlockValue(Chunk chunk, BlockPos.Mutable mutablePos) {
        return BlendingData.isCollidableAndNotTreeAt(chunk, mutablePos.move(Direction.DOWN)) ? 1.0 : -1.0;
    }

    private static double getCollidableBlockDensityBelow(Chunk chunk, BlockPos.Mutable mutablePos) {
        double d = 0.0;
        for (int i = 0; i < 7; ++i) {
            d += BlendingData.getAboveCollidableBlockValue(chunk, mutablePos);
        }
        return d;
    }

    private double[] calculateCollidableBlockDensityColumn(Chunk chunk, int chunkBlockX, int chunkBlockZ, int surfaceHeight) {
        double f;
        double e;
        int i;
        double[] ds = new double[this.getVerticalHalfSectionCount()];
        Arrays.fill(ds, -1.0);
        BlockPos.Mutable mutable = new BlockPos.Mutable(chunkBlockX, this.oldHeightLimit.getTopYInclusive() + 1, chunkBlockZ);
        double d = BlendingData.getCollidableBlockDensityBelow(chunk, mutable);
        for (i = ds.length - 2; i >= 0; --i) {
            e = BlendingData.getAboveCollidableBlockValue(chunk, mutable);
            f = BlendingData.getCollidableBlockDensityBelow(chunk, mutable);
            ds[i] = (d + e + f) / 15.0;
            d = f;
        }
        i = this.getHalfSectionHeight(MathHelper.floorDiv(surfaceHeight, 8));
        if (i >= 0 && i < ds.length - 1) {
            e = ((double)surfaceHeight + 0.5) % 8.0 / 8.0;
            f = (1.0 - e) / e;
            double g = Math.max(f, 1.0) * 0.25;
            ds[i + 1] = -f / g;
            ds[i] = 1.0 / g;
        }
        return ds;
    }

    private List<RegistryEntry<Biome>> getVerticalBiomeSections(Chunk chunk, int chunkBlockX, int chunkBlockZ) {
        ObjectArrayList objectArrayList = new ObjectArrayList(this.getVerticalBiomeCount());
        objectArrayList.size(this.getVerticalBiomeCount());
        for (int i = 0; i < objectArrayList.size(); ++i) {
            int j = i + BiomeCoords.fromBlock(this.oldHeightLimit.getBottomY());
            objectArrayList.set(i, chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(chunkBlockX), j, BiomeCoords.fromBlock(chunkBlockZ)));
        }
        return objectArrayList;
    }

    private static boolean isCollidableAndNotTreeAt(Chunk chunk, BlockPos pos) {
        BlockState blockState = chunk.getBlockState(pos);
        if (blockState.isAir()) {
            return false;
        }
        if (blockState.isIn(BlockTags.LEAVES)) {
            return false;
        }
        if (blockState.isIn(BlockTags.LOGS)) {
            return false;
        }
        if (blockState.isOf(Blocks.BROWN_MUSHROOM_BLOCK) || blockState.isOf(Blocks.RED_MUSHROOM_BLOCK)) {
            return false;
        }
        return !blockState.getCollisionShape(chunk, pos).isEmpty();
    }

    protected double getHeight(int biomeX, int biomeY, int biomeZ) {
        if (biomeX == CHUNK_BIOME_END_INDEX || biomeZ == CHUNK_BIOME_END_INDEX) {
            return this.surfaceHeights[BlendingData.getSouthEastIndex(biomeX, biomeZ)];
        }
        if (biomeX == 0 || biomeZ == 0) {
            return this.surfaceHeights[BlendingData.getNorthWestIndex(biomeX, biomeZ)];
        }
        return Double.MAX_VALUE;
    }

    private double getCollidableBlockDensity(double @Nullable [] collidableBlockDensityColumn, int halfSectionY) {
        if (collidableBlockDensityColumn == null) {
            return Double.MAX_VALUE;
        }
        int i = this.getHalfSectionHeight(halfSectionY);
        if (i < 0 || i >= collidableBlockDensityColumn.length) {
            return Double.MAX_VALUE;
        }
        return collidableBlockDensityColumn[i] * 0.1;
    }

    protected double getCollidableBlockDensity(int chunkBiomeX, int halfSectionY, int chunkBiomeZ) {
        if (halfSectionY == this.getBottomHalfSectionY()) {
            return 0.1;
        }
        if (chunkBiomeX == CHUNK_BIOME_END_INDEX || chunkBiomeZ == CHUNK_BIOME_END_INDEX) {
            return this.getCollidableBlockDensity(this.collidableBlockDensities[BlendingData.getSouthEastIndex(chunkBiomeX, chunkBiomeZ)], halfSectionY);
        }
        if (chunkBiomeX == 0 || chunkBiomeZ == 0) {
            return this.getCollidableBlockDensity(this.collidableBlockDensities[BlendingData.getNorthWestIndex(chunkBiomeX, chunkBiomeZ)], halfSectionY);
        }
        return Double.MAX_VALUE;
    }

    protected void acceptBiomes(int biomeX, int biomeY, int biomeZ, BiomeConsumer consumer) {
        if (biomeY < BiomeCoords.fromBlock(this.oldHeightLimit.getBottomY()) || biomeY > BiomeCoords.fromBlock(this.oldHeightLimit.getTopYInclusive())) {
            return;
        }
        int i = biomeY - BiomeCoords.fromBlock(this.oldHeightLimit.getBottomY());
        for (int j = 0; j < this.biomes.size(); ++j) {
            RegistryEntry<Biome> registryEntry;
            List<@Nullable RegistryEntry<Biome>> list = this.biomes.get(j);
            if (list == null || (registryEntry = list.get(i)) == null) continue;
            consumer.consume(biomeX + BlendingData.getX(j), biomeZ + BlendingData.getZ(j), registryEntry);
        }
    }

    protected void acceptHeights(int biomeX, int biomeZ, HeightConsumer consumer) {
        for (int i = 0; i < this.surfaceHeights.length; ++i) {
            double d = this.surfaceHeights[i];
            if (d == Double.MAX_VALUE) continue;
            consumer.consume(biomeX + BlendingData.getX(i), biomeZ + BlendingData.getZ(i), d);
        }
    }

    protected void acceptCollidableBlockDensities(int biomeX, int biomeZ, int minHalfSectionY, int maxHalfSectionY, CollidableBlockDensityConsumer consumer) {
        int i = this.getOneAboveBottomHalfSectionY();
        int j = Math.max(0, minHalfSectionY - i);
        int k = Math.min(this.getVerticalHalfSectionCount(), maxHalfSectionY - i);
        for (int l = 0; l < this.collidableBlockDensities.length; ++l) {
            double[] ds = this.collidableBlockDensities[l];
            if (ds == null) continue;
            int m = biomeX + BlendingData.getX(l);
            int n = biomeZ + BlendingData.getZ(l);
            for (int o = j; o < k; ++o) {
                consumer.consume(m, o + i, n, ds[o] * 0.1);
            }
        }
    }

    private int getVerticalHalfSectionCount() {
        return this.oldHeightLimit.countVerticalSections() * 2;
    }

    private int getVerticalBiomeCount() {
        return BiomeCoords.fromChunk(this.oldHeightLimit.countVerticalSections());
    }

    private int getOneAboveBottomHalfSectionY() {
        return this.getBottomHalfSectionY() + 1;
    }

    private int getBottomHalfSectionY() {
        return this.oldHeightLimit.getBottomSectionCoord() * 2;
    }

    private int getHalfSectionHeight(int halfSectionY) {
        return halfSectionY - this.getOneAboveBottomHalfSectionY();
    }

    private static int getNorthWestIndex(int chunkBiomeX, int chunkBiomeZ) {
        return LAST_CHUNK_BIOME_INDEX - chunkBiomeX + chunkBiomeZ;
    }

    private static int getSouthEastIndex(int chunkBiomeX, int chunkBiomeZ) {
        return NORTH_WEST_END_INDEX + chunkBiomeX + CHUNK_BIOME_END_INDEX - chunkBiomeZ;
    }

    private static int getX(int index) {
        if (index < NORTH_WEST_END_INDEX) {
            return BlendingData.clampPositive(LAST_CHUNK_BIOME_INDEX - index);
        }
        int i = index - NORTH_WEST_END_INDEX;
        return CHUNK_BIOME_END_INDEX - BlendingData.clampPositive(CHUNK_BIOME_END_INDEX - i);
    }

    private static int getZ(int index) {
        if (index < NORTH_WEST_END_INDEX) {
            return BlendingData.clampPositive(index - LAST_CHUNK_BIOME_INDEX);
        }
        int i = index - NORTH_WEST_END_INDEX;
        return CHUNK_BIOME_END_INDEX - BlendingData.clampPositive(i - CHUNK_BIOME_END_INDEX);
    }

    private static int clampPositive(int value) {
        return value & ~(value >> 31);
    }

    public HeightLimitView getOldHeightLimit() {
        return this.oldHeightLimit;
    }

    public record Serialized(int minSection, int maxSection, Optional<double[]> heights) {
        private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
        public static final Codec<Serialized> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("min_section").forGetter(Serialized::minSection), (App)Codec.INT.fieldOf("max_section").forGetter(Serialized::maxSection), (App)DOUBLE_ARRAY_CODEC.lenientOptionalFieldOf("heights").forGetter(Serialized::heights)).apply((Applicative)instance, Serialized::new)).validate(Serialized::validate);

        private static DataResult<Serialized> validate(Serialized serialized) {
            if (serialized.heights.isPresent() && serialized.heights.get().length != HORIZONTAL_BIOME_COUNT) {
                return DataResult.error(() -> "heights has to be of length " + HORIZONTAL_BIOME_COUNT);
            }
            return DataResult.success((Object)serialized);
        }
    }

    protected static interface BiomeConsumer {
        public void consume(int var1, int var2, RegistryEntry<Biome> var3);
    }

    protected static interface HeightConsumer {
        public void consume(int var1, int var2, double var3);
    }

    protected static interface CollidableBlockDensityConsumer {
        public void consume(int var1, int var2, int var3, double var4);
    }
}

