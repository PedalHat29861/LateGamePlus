/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jspecify.annotations.Nullable;

public class StructureWeightSampler
implements DensityFunctionTypes.Beardifying {
    public static final int INDEX_OFFSET = 12;
    private static final int EDGE_LENGTH = 24;
    private static final float[] STRUCTURE_WEIGHT_TABLE = Util.make(new float[13824], array -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    array[i * 24 * 24 + j * 24 + k] = (float)StructureWeightSampler.calculateStructureWeight(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    public static final StructureWeightSampler field_61464 = new StructureWeightSampler(List.of(), List.of(), null);
    private final List<Piece> field_61465;
    private final List<JigsawJunction> field_61466;
    private final @Nullable BlockBox field_61467;

    public static StructureWeightSampler createStructureWeightSampler(StructureAccessor structureAccessor, ChunkPos chunkPos) {
        List<StructureStart> list = structureAccessor.getStructureStarts(chunkPos, structure -> structure.getTerrainAdaptation() != StructureTerrainAdaptation.NONE);
        if (list.isEmpty()) {
            return field_61464;
        }
        int i = chunkPos.getStartX();
        int j = chunkPos.getStartZ();
        ArrayList<Piece> list2 = new ArrayList<Piece>();
        ArrayList<JigsawJunction> list3 = new ArrayList<JigsawJunction>();
        BlockBox blockBox = null;
        for (StructureStart structureStart : list) {
            StructureTerrainAdaptation structureTerrainAdaptation = structureStart.getStructure().getTerrainAdaptation();
            for (StructurePiece structurePiece : structureStart.getChildren()) {
                if (!structurePiece.intersectsChunk(chunkPos, 12)) continue;
                if (structurePiece instanceof PoolStructurePiece) {
                    PoolStructurePiece poolStructurePiece = (PoolStructurePiece)structurePiece;
                    StructurePool.Projection projection = poolStructurePiece.getPoolElement().getProjection();
                    if (projection == StructurePool.Projection.RIGID) {
                        list2.add(new Piece(poolStructurePiece.getBoundingBox(), structureTerrainAdaptation, poolStructurePiece.getGroundLevelDelta()));
                        blockBox = StructureWeightSampler.method_72681(blockBox, structurePiece.getBoundingBox());
                    }
                    for (JigsawJunction jigsawJunction : poolStructurePiece.getJunctions()) {
                        int k = jigsawJunction.getSourceX();
                        int l = jigsawJunction.getSourceZ();
                        if (k <= i - 12 || l <= j - 12 || k >= i + 15 + 12 || l >= j + 15 + 12) continue;
                        list3.add(jigsawJunction);
                        BlockBox blockBox2 = new BlockBox(new BlockPos(k, jigsawJunction.getSourceGroundY(), l));
                        blockBox = StructureWeightSampler.method_72681(blockBox, blockBox2);
                    }
                    continue;
                }
                list2.add(new Piece(structurePiece.getBoundingBox(), structureTerrainAdaptation, 0));
                blockBox = StructureWeightSampler.method_72681(blockBox, structurePiece.getBoundingBox());
            }
        }
        if (blockBox == null) {
            return field_61464;
        }
        BlockBox blockBox3 = blockBox.expand(24);
        return new StructureWeightSampler(List.copyOf(list2), List.copyOf(list3), blockBox3);
    }

    private static BlockBox method_72681(@Nullable BlockBox blockBox, BlockBox blockBox2) {
        if (blockBox == null) {
            return blockBox2;
        }
        return BlockBox.createEncompassing(blockBox, blockBox2);
    }

    @VisibleForTesting
    public StructureWeightSampler(List<Piece> list, List<JigsawJunction> list2, @Nullable BlockBox blockBox) {
        this.field_61465 = list;
        this.field_61466 = list2;
        this.field_61467 = blockBox;
    }

    @Override
    public void fill(double[] densities, DensityFunction.EachApplier applier) {
        if (this.field_61467 == null) {
            Arrays.fill(densities, 0.0);
        } else {
            DensityFunctionTypes.Beardifying.super.fill(densities, applier);
        }
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        int m;
        int l;
        int k;
        int j;
        if (this.field_61467 == null) {
            return 0.0;
        }
        int i = pos.blockX();
        if (!this.field_61467.contains(i, j = pos.blockY(), k = pos.blockZ())) {
            return 0.0;
        }
        double d = 0.0;
        for (Piece piece : this.field_61465) {
            BlockBox blockBox = piece.box();
            l = piece.groundLevelDelta();
            m = Math.max(0, Math.max(blockBox.getMinX() - i, i - blockBox.getMaxX()));
            int n = Math.max(0, Math.max(blockBox.getMinZ() - k, k - blockBox.getMaxZ()));
            int o = blockBox.getMinY() + l;
            int p = j - o;
            int q = switch (piece.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0;
                case StructureTerrainAdaptation.BURY, StructureTerrainAdaptation.BEARD_THIN -> p;
                case StructureTerrainAdaptation.BEARD_BOX -> Math.max(0, Math.max(o - j, j - blockBox.getMaxY()));
                case StructureTerrainAdaptation.ENCAPSULATE -> Math.max(0, Math.max(blockBox.getMinY() - j, j - blockBox.getMaxY()));
            };
            d += (switch (piece.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0.0;
                case StructureTerrainAdaptation.BURY -> StructureWeightSampler.getMagnitudeWeight(m, (double)q / 2.0, n);
                case StructureTerrainAdaptation.BEARD_THIN, StructureTerrainAdaptation.BEARD_BOX -> StructureWeightSampler.getStructureWeight(m, q, n, p) * 0.8;
                case StructureTerrainAdaptation.ENCAPSULATE -> StructureWeightSampler.getMagnitudeWeight((double)m / 2.0, (double)q / 2.0, (double)n / 2.0) * 0.8;
            });
        }
        for (JigsawJunction jigsawJunction : this.field_61466) {
            int r = i - jigsawJunction.getSourceX();
            l = j - jigsawJunction.getSourceGroundY();
            m = k - jigsawJunction.getSourceZ();
            d += StructureWeightSampler.getStructureWeight(r, l, m, l) * 0.4;
        }
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getMagnitudeWeight(double x, double y, double z) {
        double d = MathHelper.magnitude(x, y, z);
        return MathHelper.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getStructureWeight(int x, int y, int z, int yy) {
        int i = x + 12;
        int j = y + 12;
        int k = z + 12;
        if (!(StructureWeightSampler.indexInBounds(i) && StructureWeightSampler.indexInBounds(j) && StructureWeightSampler.indexInBounds(k))) {
            return 0.0;
        }
        double d = (double)yy + 0.5;
        double e = MathHelper.squaredMagnitude(x, d, z);
        double f = -d * MathHelper.fastInverseSqrt(e / 2.0) / 2.0;
        return f * (double)STRUCTURE_WEIGHT_TABLE[k * 24 * 24 + i * 24 + j];
    }

    private static boolean indexInBounds(int i) {
        return i >= 0 && i < 24;
    }

    private static double calculateStructureWeight(int x, int y, int z) {
        return StructureWeightSampler.structureWeight(x, (double)y + 0.5, z);
    }

    private static double structureWeight(int x, double y, int z) {
        double d = MathHelper.squaredMagnitude(x, y, z);
        double e = Math.pow(Math.E, -d / 16.0);
        return e;
    }

    @VisibleForTesting
    public record Piece(BlockBox box, StructureTerrainAdaptation terrainAdjustment, int groundLevelDelta) {
    }
}

