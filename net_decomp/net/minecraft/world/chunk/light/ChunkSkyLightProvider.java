/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk.light;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.ChunkSkyLight;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.jspecify.annotations.Nullable;

public final class ChunkSkyLightProvider
extends ChunkLightProvider<SkyLightStorage.Data, SkyLightStorage> {
    private static final long field_44743 = ChunkLightProvider.PackedInfo.packWithAllDirectionsSet(15);
    private static final long field_44744 = ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(15, Direction.UP);
    private static final long field_44745 = ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(15, false, Direction.UP);
    private final BlockPos.Mutable field_44746 = new BlockPos.Mutable();
    private final ChunkSkyLight defaultSkyLight;

    public ChunkSkyLightProvider(ChunkProvider chunkProvider) {
        this(chunkProvider, new SkyLightStorage(chunkProvider));
    }

    @VisibleForTesting
    protected ChunkSkyLightProvider(ChunkProvider chunkProvider, SkyLightStorage lightStorage) {
        super(chunkProvider, lightStorage);
        this.defaultSkyLight = new ChunkSkyLight(chunkProvider.getWorld());
    }

    private static boolean isMaxLightLevel(int lightLevel) {
        return lightLevel == 15;
    }

    private int getSkyLightOrDefault(int x, int z, int defaultValue) {
        ChunkSkyLight chunkSkyLight = this.getSkyLight(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        if (chunkSkyLight == null) {
            return defaultValue;
        }
        return chunkSkyLight.get(ChunkSectionPos.getLocalCoord(x), ChunkSectionPos.getLocalCoord(z));
    }

    private @Nullable ChunkSkyLight getSkyLight(int chunkX, int chunkZ) {
        LightSourceView lightSourceView = this.chunkProvider.getChunk(chunkX, chunkZ);
        return lightSourceView != null ? lightSourceView.getChunkSkyLight() : null;
    }

    @Override
    protected void checkForLightUpdate(long blockPos) {
        boolean bl;
        int m;
        int i = BlockPos.unpackLongX(blockPos);
        int j = BlockPos.unpackLongY(blockPos);
        int k = BlockPos.unpackLongZ(blockPos);
        long l = ChunkSectionPos.fromBlockPos(blockPos);
        int n = m = ((SkyLightStorage)this.lightStorage).isSectionInEnabledColumn(l) ? this.getSkyLightOrDefault(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (m != Integer.MAX_VALUE) {
            this.method_51590(i, k, m);
        }
        if (!((SkyLightStorage)this.lightStorage).hasSection(l)) {
            return;
        }
        boolean bl2 = bl = j >= m;
        if (bl) {
            this.queueLightDecrease(blockPos, field_44744);
            this.queueLightIncrease(blockPos, field_44745);
        } else {
            int n2 = ((SkyLightStorage)this.lightStorage).get(blockPos);
            if (n2 > 0) {
                ((SkyLightStorage)this.lightStorage).set(blockPos, 0);
                this.queueLightDecrease(blockPos, ChunkLightProvider.PackedInfo.packWithAllDirectionsSet(n2));
            } else {
                this.queueLightDecrease(blockPos, field_44731);
            }
        }
    }

    private void method_51590(int i, int j, int k) {
        int l = ChunkSectionPos.getBlockCoord(((SkyLightStorage)this.lightStorage).getMinSectionY());
        this.method_51586(i, j, k, l);
        this.method_51591(i, j, k, l);
    }

    private void method_51586(int x, int z, int i, int j) {
        if (i <= j) {
            return;
        }
        int k = ChunkSectionPos.getSectionCoord(x);
        int l = ChunkSectionPos.getSectionCoord(z);
        int m = i - 1;
        int n = ChunkSectionPos.getSectionCoord(m);
        while (((SkyLightStorage)this.lightStorage).isAboveMinHeight(n)) {
            if (((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(k, n, l))) {
                int o = ChunkSectionPos.getBlockCoord(n);
                int p = o + 15;
                for (int q = Math.min(p, m); q >= o; --q) {
                    long r = BlockPos.asLong(x, q, z);
                    if (!ChunkSkyLightProvider.isMaxLightLevel(((SkyLightStorage)this.lightStorage).get(r))) {
                        return;
                    }
                    ((SkyLightStorage)this.lightStorage).set(r, 0);
                    this.queueLightDecrease(r, q == i - 1 ? field_44743 : field_44744);
                }
            }
            --n;
        }
    }

    private void method_51591(int x, int z, int i, int j) {
        int k = ChunkSectionPos.getSectionCoord(x);
        int l = ChunkSectionPos.getSectionCoord(z);
        int m = Math.max(Math.max(this.getSkyLightOrDefault(x - 1, z, Integer.MIN_VALUE), this.getSkyLightOrDefault(x + 1, z, Integer.MIN_VALUE)), Math.max(this.getSkyLightOrDefault(x, z - 1, Integer.MIN_VALUE), this.getSkyLightOrDefault(x, z + 1, Integer.MIN_VALUE)));
        int n = Math.max(i, j);
        long o = ChunkSectionPos.asLong(k, ChunkSectionPos.getSectionCoord(n), l);
        while (!((SkyLightStorage)this.lightStorage).isAtOrAboveTopmostSection(o)) {
            if (((SkyLightStorage)this.lightStorage).hasSection(o)) {
                int p = ChunkSectionPos.getBlockCoord(ChunkSectionPos.unpackY(o));
                int q = p + 15;
                for (int r = Math.max(p, n); r <= q; ++r) {
                    long s = BlockPos.asLong(x, r, z);
                    if (ChunkSkyLightProvider.isMaxLightLevel(((SkyLightStorage)this.lightStorage).get(s))) {
                        return;
                    }
                    ((SkyLightStorage)this.lightStorage).set(s, 15);
                    if (r >= m && r != i) continue;
                    this.queueLightIncrease(s, field_44745);
                }
            }
            o = ChunkSectionPos.offset(o, Direction.UP);
        }
    }

    @Override
    protected void propagateLightIncrease(long blockPos, long packed, int lightLevel) {
        BlockState blockState = null;
        int i = this.getNumberOfSectionsBelowPos(blockPos);
        for (Direction direction : DIRECTIONS) {
            int j;
            int k;
            long l;
            if (!ChunkLightProvider.PackedInfo.isDirectionBitSet(packed, direction) || !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(l = BlockPos.offset(blockPos, direction))) || (k = lightLevel - 1) <= (j = ((SkyLightStorage)this.lightStorage).get(l))) continue;
            this.field_44746.set(l);
            BlockState blockState2 = this.getStateForLighting(this.field_44746);
            int m = lightLevel - this.getOpacity(blockState2);
            if (m <= j) continue;
            if (blockState == null) {
                BlockState blockState3 = blockState = ChunkLightProvider.PackedInfo.isTrivial(packed) ? Blocks.AIR.getDefaultState() : this.getStateForLighting(this.field_44746.set(blockPos));
            }
            if (this.shapesCoverFullCube(blockState, blockState2, direction)) continue;
            ((SkyLightStorage)this.lightStorage).set(l, m);
            if (m > 1) {
                this.queueLightIncrease(l, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(m, ChunkSkyLightProvider.isTrivialForLighting(blockState2), direction.getOpposite()));
            }
            this.method_51587(l, direction, m, true, i);
        }
    }

    @Override
    protected void propagateLightDecrease(long blockPos, long packed) {
        int i = this.getNumberOfSectionsBelowPos(blockPos);
        int j = ChunkLightProvider.PackedInfo.getLightLevel(packed);
        for (Direction direction : DIRECTIONS) {
            int k;
            long l;
            if (!ChunkLightProvider.PackedInfo.isDirectionBitSet(packed, direction) || !((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.fromBlockPos(l = BlockPos.offset(blockPos, direction))) || (k = ((SkyLightStorage)this.lightStorage).get(l)) == 0) continue;
            if (k <= j - 1) {
                ((SkyLightStorage)this.lightStorage).set(l, 0);
                this.queueLightDecrease(l, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(k, direction.getOpposite()));
                this.method_51587(l, direction, k, false, i);
                continue;
            }
            this.queueLightIncrease(l, ChunkLightProvider.PackedInfo.packWithRepropagate(k, false, direction.getOpposite()));
        }
    }

    private int getNumberOfSectionsBelowPos(long blockPos) {
        int i = BlockPos.unpackLongY(blockPos);
        int j = ChunkSectionPos.getLocalCoord(i);
        if (j != 0) {
            return 0;
        }
        int k = BlockPos.unpackLongX(blockPos);
        int l = BlockPos.unpackLongZ(blockPos);
        int m = ChunkSectionPos.getLocalCoord(k);
        int n = ChunkSectionPos.getLocalCoord(l);
        if (m == 0 || m == 15 || n == 0 || n == 15) {
            int o = ChunkSectionPos.getSectionCoord(k);
            int p = ChunkSectionPos.getSectionCoord(i);
            int q = ChunkSectionPos.getSectionCoord(l);
            int r = 0;
            while (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(o, p - r - 1, q)) && ((SkyLightStorage)this.lightStorage).isAboveMinHeight(p - r - 1)) {
                ++r;
            }
            return r;
        }
        return 0;
    }

    private void method_51587(long blockPos, Direction direction, int lightLevel, boolean bl, int i) {
        if (i == 0) {
            return;
        }
        int j = BlockPos.unpackLongX(blockPos);
        int k = BlockPos.unpackLongZ(blockPos);
        if (!ChunkSkyLightProvider.exitsChunkXZ(direction, ChunkSectionPos.getLocalCoord(j), ChunkSectionPos.getLocalCoord(k))) {
            return;
        }
        int l = BlockPos.unpackLongY(blockPos);
        int m = ChunkSectionPos.getSectionCoord(j);
        int n = ChunkSectionPos.getSectionCoord(k);
        int o = ChunkSectionPos.getSectionCoord(l) - 1;
        int p = o - i + 1;
        while (o >= p) {
            if (!((SkyLightStorage)this.lightStorage).hasSection(ChunkSectionPos.asLong(m, o, n))) {
                --o;
                continue;
            }
            int q = ChunkSectionPos.getBlockCoord(o);
            for (int r = 15; r >= 0; --r) {
                long s = BlockPos.asLong(j, q + r, k);
                if (bl) {
                    ((SkyLightStorage)this.lightStorage).set(s, lightLevel);
                    if (lightLevel <= 1) continue;
                    this.queueLightIncrease(s, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(lightLevel, true, direction.getOpposite()));
                    continue;
                }
                ((SkyLightStorage)this.lightStorage).set(s, 0);
                this.queueLightDecrease(s, ChunkLightProvider.PackedInfo.packWithOneDirectionCleared(lightLevel, direction.getOpposite()));
            }
            --o;
        }
    }

    private static boolean exitsChunkXZ(Direction direction, int localX, int localZ) {
        return switch (direction) {
            case Direction.NORTH -> {
                if (localZ == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (localZ == 0) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (localX == 15) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> {
                if (localX == 0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        super.setColumnEnabled(pos, retainData);
        if (retainData) {
            ChunkSkyLight chunkSkyLight = Objects.requireNonNullElse(this.getSkyLight(pos.x, pos.z), this.defaultSkyLight);
            int i = chunkSkyLight.getMaxSurfaceY() - 1;
            int j = ChunkSectionPos.getSectionCoord(i) + 1;
            long l = ChunkSectionPos.withZeroY(pos.x, pos.z);
            int k = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
            int m = Math.max(((SkyLightStorage)this.lightStorage).getMinSectionY(), j);
            for (int n = k - 1; n >= m; --n) {
                ChunkNibbleArray chunkNibbleArray = ((SkyLightStorage)this.lightStorage).method_51547(ChunkSectionPos.asLong(pos.x, n, pos.z));
                if (chunkNibbleArray == null || !chunkNibbleArray.isUninitialized()) continue;
                chunkNibbleArray.clear(15);
            }
        }
    }

    @Override
    public void propagateLight(ChunkPos chunkPos) {
        long l = ChunkSectionPos.withZeroY(chunkPos.x, chunkPos.z);
        ((SkyLightStorage)this.lightStorage).setColumnEnabled(l, true);
        ChunkSkyLight chunkSkyLight = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x, chunkPos.z), this.defaultSkyLight);
        ChunkSkyLight chunkSkyLight2 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x, chunkPos.z - 1), this.defaultSkyLight);
        ChunkSkyLight chunkSkyLight3 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x, chunkPos.z + 1), this.defaultSkyLight);
        ChunkSkyLight chunkSkyLight4 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x - 1, chunkPos.z), this.defaultSkyLight);
        ChunkSkyLight chunkSkyLight5 = Objects.requireNonNullElse(this.getSkyLight(chunkPos.x + 1, chunkPos.z), this.defaultSkyLight);
        int i = ((SkyLightStorage)this.lightStorage).getTopSectionForColumn(l);
        int j = ((SkyLightStorage)this.lightStorage).getMinSectionY();
        int k = ChunkSectionPos.getBlockCoord(chunkPos.x);
        int m = ChunkSectionPos.getBlockCoord(chunkPos.z);
        for (int n = i - 1; n >= j; --n) {
            long o = ChunkSectionPos.asLong(chunkPos.x, n, chunkPos.z);
            ChunkNibbleArray chunkNibbleArray = ((SkyLightStorage)this.lightStorage).method_51547(o);
            if (chunkNibbleArray == null) continue;
            int p = ChunkSectionPos.getBlockCoord(n);
            int q = p + 15;
            boolean bl = false;
            for (int r = 0; r < 16; ++r) {
                for (int s = 0; s < 16; ++s) {
                    int t = chunkSkyLight.get(s, r);
                    if (t > q) continue;
                    int u = r == 0 ? chunkSkyLight2.get(s, 15) : chunkSkyLight.get(s, r - 1);
                    int v = r == 15 ? chunkSkyLight3.get(s, 0) : chunkSkyLight.get(s, r + 1);
                    int w = s == 0 ? chunkSkyLight4.get(15, r) : chunkSkyLight.get(s - 1, r);
                    int x = s == 15 ? chunkSkyLight5.get(0, r) : chunkSkyLight.get(s + 1, r);
                    int y = Math.max(Math.max(u, v), Math.max(w, x));
                    for (int z = q; z >= Math.max(p, t); --z) {
                        chunkNibbleArray.set(s, ChunkSectionPos.getLocalCoord(z), r, 15);
                        if (z != t && z >= y) continue;
                        long aa = BlockPos.asLong(k + s, z, m + r);
                        this.queueLightIncrease(aa, ChunkLightProvider.PackedInfo.packSkyLightPropagation(z == t, z < u, z < v, z < w, z < x));
                    }
                    if (t >= p) continue;
                    bl = true;
                }
            }
            if (!bl) break;
        }
    }
}

