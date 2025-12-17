/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.AbstractIterator
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  org.apache.commons.lang3.Validate
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jetbrains.annotations.Contract
 *  org.jetbrains.annotations.Unmodifiable
 */
package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class BlockPos
extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap(stream -> Util.decodeFixedLengthArray(stream, 3).map(values -> new BlockPos(values[0], values[1], values[2])), pos -> IntStream.of(pos.getX(), pos.getY(), pos.getZ())).stable();
    public static final PacketCodec<ByteBuf, BlockPos> PACKET_CODEC = new PacketCodec<ByteBuf, BlockPos>(){

        @Override
        public BlockPos decode(ByteBuf byteBuf) {
            return PacketByteBuf.readBlockPos(byteBuf);
        }

        @Override
        public void encode(ByteBuf byteBuf, BlockPos blockPos) {
            PacketByteBuf.writeBlockPos(byteBuf, blockPos);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (BlockPos)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };
    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
    public static final int SIZE_BITS_XZ = 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    public static final int SIZE_BITS_Y = 64 - 2 * SIZE_BITS_XZ;
    private static final long BITS_X = (1L << SIZE_BITS_XZ) - 1L;
    private static final long BITS_Y = (1L << SIZE_BITS_Y) - 1L;
    private static final long BITS_Z = (1L << SIZE_BITS_XZ) - 1L;
    private static final int field_33083 = 0;
    private static final int BIT_SHIFT_Z = SIZE_BITS_Y;
    private static final int BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_XZ;
    public static final int MAX_XZ = (1 << SIZE_BITS_XZ) / 2 - 1;

    public BlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPos(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public static long offset(long value, Direction direction) {
        return BlockPos.add(value, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    public static long add(long value, int x, int y, int z) {
        return BlockPos.asLong(BlockPos.unpackLongX(value) + x, BlockPos.unpackLongY(value) + y, BlockPos.unpackLongZ(value) + z);
    }

    public static int unpackLongX(long packedPos) {
        return (int)(packedPos << 64 - BIT_SHIFT_X - SIZE_BITS_XZ >> 64 - SIZE_BITS_XZ);
    }

    public static int unpackLongY(long packedPos) {
        return (int)(packedPos << 64 - SIZE_BITS_Y >> 64 - SIZE_BITS_Y);
    }

    public static int unpackLongZ(long packedPos) {
        return (int)(packedPos << 64 - BIT_SHIFT_Z - SIZE_BITS_XZ >> 64 - SIZE_BITS_XZ);
    }

    public static BlockPos fromLong(long packedPos) {
        return new BlockPos(BlockPos.unpackLongX(packedPos), BlockPos.unpackLongY(packedPos), BlockPos.unpackLongZ(packedPos));
    }

    public static BlockPos ofFloored(double x, double y, double z) {
        return new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public static BlockPos ofFloored(Position pos) {
        return BlockPos.ofFloored(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos min(BlockPos a, BlockPos b) {
        return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    public static BlockPos max(BlockPos a, BlockPos b) {
        return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long)x & BITS_X) << BIT_SHIFT_X;
        l |= ((long)y & BITS_Y) << 0;
        return l |= ((long)z & BITS_Z) << BIT_SHIFT_Z;
    }

    public static long removeChunkSectionLocalY(long y) {
        return y & 0xFFFFFFFFFFFFFFF0L;
    }

    @Override
    public BlockPos add(int i, int j, int k) {
        if (i == 0 && j == 0 && k == 0) {
            return this;
        }
        return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
    }

    public Vec3d toCenterPos() {
        return Vec3d.ofCenter(this);
    }

    public Vec3d toBottomCenterPos() {
        return Vec3d.ofBottomCenter(this);
    }

    @Override
    @Contract(pure=true)
    public BlockPos add(Vec3i vec3i) {
        return this.add(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    @Override
    public BlockPos subtract(Vec3i vec3i) {
        return this.add(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
    }

    @Override
    public BlockPos multiply(int i) {
        if (i == 1) {
            return this;
        }
        if (i == 0) {
            return ORIGIN;
        }
        return new BlockPos(this.getX() * i, this.getY() * i, this.getZ() * i);
    }

    @Override
    public BlockPos up() {
        return this.offset(Direction.UP);
    }

    @Override
    public BlockPos up(int distance) {
        return this.offset(Direction.UP, distance);
    }

    @Override
    public BlockPos down() {
        return this.offset(Direction.DOWN);
    }

    @Override
    public BlockPos down(int i) {
        return this.offset(Direction.DOWN, i);
    }

    @Override
    public BlockPos north() {
        return this.offset(Direction.NORTH);
    }

    @Override
    public BlockPos north(int distance) {
        return this.offset(Direction.NORTH, distance);
    }

    @Override
    public BlockPos south() {
        return this.offset(Direction.SOUTH);
    }

    @Override
    public BlockPos south(int distance) {
        return this.offset(Direction.SOUTH, distance);
    }

    @Override
    public BlockPos west() {
        return this.offset(Direction.WEST);
    }

    @Override
    public BlockPos west(int distance) {
        return this.offset(Direction.WEST, distance);
    }

    @Override
    public BlockPos east() {
        return this.offset(Direction.EAST);
    }

    @Override
    public BlockPos east(int distance) {
        return this.offset(Direction.EAST, distance);
    }

    @Override
    public BlockPos offset(Direction direction) {
        return new BlockPos(this.getX() + direction.getOffsetX(), this.getY() + direction.getOffsetY(), this.getZ() + direction.getOffsetZ());
    }

    @Override
    public BlockPos offset(Direction direction, int i) {
        if (i == 0) {
            return this;
        }
        return new BlockPos(this.getX() + direction.getOffsetX() * i, this.getY() + direction.getOffsetY() * i, this.getZ() + direction.getOffsetZ() * i);
    }

    @Override
    public BlockPos offset(Direction.Axis axis, int i) {
        if (i == 0) {
            return this;
        }
        int j = axis == Direction.Axis.X ? i : 0;
        int k = axis == Direction.Axis.Y ? i : 0;
        int l = axis == Direction.Axis.Z ? i : 0;
        return new BlockPos(this.getX() + j, this.getY() + k, this.getZ() + l);
    }

    public BlockPos rotate(BlockRotation rotation) {
        return switch (rotation) {
            default -> throw new MatchException(null, null);
            case BlockRotation.CLOCKWISE_90 -> new BlockPos(-this.getZ(), this.getY(), this.getX());
            case BlockRotation.CLOCKWISE_180 -> new BlockPos(-this.getX(), this.getY(), -this.getZ());
            case BlockRotation.COUNTERCLOCKWISE_90 -> new BlockPos(this.getZ(), this.getY(), -this.getX());
            case BlockRotation.NONE -> this;
        };
    }

    @Override
    public BlockPos crossProduct(Vec3i pos) {
        return new BlockPos(this.getY() * pos.getZ() - this.getZ() * pos.getY(), this.getZ() * pos.getX() - this.getX() * pos.getZ(), this.getX() * pos.getY() - this.getY() * pos.getX());
    }

    public BlockPos withY(int y) {
        return new BlockPos(this.getX(), y, this.getZ());
    }

    public BlockPos toImmutable() {
        return this;
    }

    public Mutable mutableCopy() {
        return new Mutable(this.getX(), this.getY(), this.getZ());
    }

    public Vec3d clampToWithin(Vec3d pos) {
        return new Vec3d(MathHelper.clamp(pos.x, (double)((float)this.getX() + 1.0E-5f), (double)this.getX() + 1.0 - (double)1.0E-5f), MathHelper.clamp(pos.y, (double)((float)this.getY() + 1.0E-5f), (double)this.getY() + 1.0 - (double)1.0E-5f), MathHelper.clamp(pos.z, (double)((float)this.getZ() + 1.0E-5f), (double)this.getZ() + 1.0 - (double)1.0E-5f));
    }

    public static Iterable<BlockPos> iterateRandomly(Random random, int count, BlockPos around, int range) {
        return BlockPos.iterateRandomly(random, count, around.getX() - range, around.getY() - range, around.getZ() - range, around.getX() + range, around.getY() + range, around.getZ() + range);
    }

    @Deprecated
    public static Stream<BlockPos> streamSouthEastSquare(BlockPos pos) {
        return Stream.of(pos, pos.south(), pos.east(), pos.south().east());
    }

    public static Iterable<BlockPos> iterateRandomly(final Random random, final int count, final int minX, final int minY, final int minZ, int maxX, int maxY, int maxZ) {
        final int i = maxX - minX + 1;
        final int j = maxY - minY + 1;
        final int k = maxZ - minZ + 1;
        return () -> new AbstractIterator<BlockPos>(){
            final Mutable pos = new Mutable();
            int remaining = count;

            protected BlockPos computeNext() {
                if (this.remaining <= 0) {
                    return (BlockPos)this.endOfData();
                }
                Mutable blockPos = this.pos.set(minX + random.nextInt(i), minY + random.nextInt(j), minZ + random.nextInt(k));
                --this.remaining;
                return blockPos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<BlockPos> iterateOutwards(BlockPos center, final int rangeX, final int rangeY, final int rangeZ) {
        final int i = rangeX + rangeY + rangeZ;
        final int j = center.getX();
        final int k = center.getY();
        final int l = center.getZ();
        return () -> new AbstractIterator<BlockPos>(){
            private final Mutable pos = new Mutable();
            private int manhattanDistance;
            private int limitX;
            private int limitY;
            private int dx;
            private int dy;
            private boolean swapZ;

            protected BlockPos computeNext() {
                if (this.swapZ) {
                    this.swapZ = false;
                    this.pos.setZ(l - (this.pos.getZ() - l));
                    return this.pos;
                }
                Mutable blockPos = null;
                while (blockPos == null) {
                    if (this.dy > this.limitY) {
                        ++this.dx;
                        if (this.dx > this.limitX) {
                            ++this.manhattanDistance;
                            if (this.manhattanDistance > i) {
                                return (BlockPos)this.endOfData();
                            }
                            this.limitX = Math.min(rangeX, this.manhattanDistance);
                            this.dx = -this.limitX;
                        }
                        this.limitY = Math.min(rangeY, this.manhattanDistance - Math.abs(this.dx));
                        this.dy = -this.limitY;
                    }
                    int i2 = this.dx;
                    int j2 = this.dy;
                    int k2 = this.manhattanDistance - Math.abs(i2) - Math.abs(j2);
                    if (k2 <= rangeZ) {
                        this.swapZ = k2 != 0;
                        blockPos = this.pos.set(j + i2, k + j2, l + k2);
                    }
                    ++this.dy;
                }
                return blockPos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Optional<BlockPos> findClosest(BlockPos pos, int horizontalRange, int verticalRange, Predicate<BlockPos> condition) {
        for (BlockPos blockPos : BlockPos.iterateOutwards(pos, horizontalRange, verticalRange, horizontalRange)) {
            if (!condition.test(blockPos)) continue;
            return Optional.of(blockPos);
        }
        return Optional.empty();
    }

    public static Stream<BlockPos> streamOutwards(BlockPos center, int maxX, int maxY, int maxZ) {
        return StreamSupport.stream(BlockPos.iterateOutwards(center, maxX, maxY, maxZ).spliterator(), false);
    }

    public static Iterable<BlockPos> iterate(Box box) {
        BlockPos blockPos = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
        BlockPos blockPos2 = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);
        return BlockPos.iterate(blockPos, blockPos2);
    }

    public static Iterable<BlockPos> iterate(BlockPos start, BlockPos end) {
        return BlockPos.iterate(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()), Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
    }

    public static Stream<BlockPos> stream(BlockPos start, BlockPos end) {
        return StreamSupport.stream(BlockPos.iterate(start, end).spliterator(), false);
    }

    public static Stream<BlockPos> stream(BlockBox box) {
        return BlockPos.stream(Math.min(box.getMinX(), box.getMaxX()), Math.min(box.getMinY(), box.getMaxY()), Math.min(box.getMinZ(), box.getMaxZ()), Math.max(box.getMinX(), box.getMaxX()), Math.max(box.getMinY(), box.getMaxY()), Math.max(box.getMinZ(), box.getMaxZ()));
    }

    public static Stream<BlockPos> stream(Box box) {
        return BlockPos.stream(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ));
    }

    public static Stream<BlockPos> stream(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return StreamSupport.stream(BlockPos.iterate(startX, startY, startZ, endX, endY, endZ).spliterator(), false);
    }

    public static Iterable<BlockPos> iterate(final int startX, final int startY, final int startZ, int endX, int endY, int endZ) {
        final int i = endX - startX + 1;
        final int j = endY - startY + 1;
        int k = endZ - startZ + 1;
        final int l = i * j * k;
        return () -> new AbstractIterator<BlockPos>(){
            private final Mutable pos = new Mutable();
            private int index;

            protected BlockPos computeNext() {
                if (this.index == l) {
                    return (BlockPos)this.endOfData();
                }
                int i2 = this.index % i;
                int j2 = this.index / i;
                int k = j2 % j;
                int l2 = j2 / j;
                ++this.index;
                return this.pos.set(startX + i2, startY + k, startZ + l2);
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<Mutable> iterateInSquare(final BlockPos center, final int radius, final Direction firstDirection, final Direction secondDirection) {
        Validate.validState((firstDirection.getAxis() != secondDirection.getAxis() ? 1 : 0) != 0, (String)"The two directions cannot be on the same axis", (Object[])new Object[0]);
        return () -> new AbstractIterator<Mutable>(){
            private final Direction[] directions;
            private final Mutable pos;
            private final int maxDirectionChanges;
            private int directionChangeCount;
            private int maxSteps;
            private int steps;
            private int currentX;
            private int currentY;
            private int currentZ;
            {
                this.directions = new Direction[]{firstDirection, secondDirection, firstDirection.getOpposite(), secondDirection.getOpposite()};
                this.pos = center.mutableCopy().move(secondDirection);
                this.maxDirectionChanges = 4 * radius;
                this.directionChangeCount = -1;
                this.currentX = this.pos.getX();
                this.currentY = this.pos.getY();
                this.currentZ = this.pos.getZ();
            }

            protected Mutable computeNext() {
                this.pos.set(this.currentX, this.currentY, this.currentZ).move(this.directions[(this.directionChangeCount + 4) % 4]);
                this.currentX = this.pos.getX();
                this.currentY = this.pos.getY();
                this.currentZ = this.pos.getZ();
                if (this.steps >= this.maxSteps) {
                    if (this.directionChangeCount >= this.maxDirectionChanges) {
                        return (Mutable)this.endOfData();
                    }
                    ++this.directionChangeCount;
                    this.steps = 0;
                    this.maxSteps = this.directionChangeCount / 2 + 1;
                }
                ++this.steps;
                return this.pos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static int iterateRecursively(BlockPos pos, int maxDepth, int maxIterations, BiConsumer<BlockPos, Consumer<BlockPos>> nextQueuer, Function<BlockPos, IterationState> callback) {
        ArrayDeque<Pair> queue = new ArrayDeque<Pair>();
        LongOpenHashSet longSet = new LongOpenHashSet();
        queue.add(Pair.of((Object)pos, (Object)0));
        int i = 0;
        while (!queue.isEmpty()) {
            IterationState iterationState;
            Pair pair = (Pair)queue.poll();
            BlockPos blockPos = (BlockPos)pair.getLeft();
            int j = (Integer)pair.getRight();
            long l = blockPos.asLong();
            if (!longSet.add(l) || (iterationState = callback.apply(blockPos)) == IterationState.SKIP) continue;
            if (iterationState == IterationState.STOP) break;
            if (++i >= maxIterations) {
                return i;
            }
            if (j >= maxDepth) continue;
            nextQueuer.accept(blockPos, queuedPos -> queue.add(Pair.of((Object)queuedPos, (Object)(j + 1))));
        }
        return i;
    }

    public static Iterable<BlockPos> iterateCollisionOrder(Box bounds, Vec3d velocity) {
        Vec3d vec3d = bounds.getMinPos();
        int i = MathHelper.floor(vec3d.getX());
        int j = MathHelper.floor(vec3d.getY());
        int k = MathHelper.floor(vec3d.getZ());
        Vec3d vec3d2 = bounds.getMaxPos();
        int l = MathHelper.floor(vec3d2.getX());
        int m = MathHelper.floor(vec3d2.getY());
        int n = MathHelper.floor(vec3d2.getZ());
        return BlockPos.iterateCollisionOrder(i, j, k, l, m, n, velocity);
    }

    public static Iterable<BlockPos> iterateCollisionOrder(BlockPos start, BlockPos end, Vec3d velocity) {
        return BlockPos.iterateCollisionOrder(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), velocity);
    }

    public static Iterable<BlockPos> iterateCollisionOrder(int x1, int y1, int z1, int x2, int y2, int z2, Vec3d velocity) {
        int i = Math.min(x1, x2);
        int j = Math.min(y1, y2);
        int k = Math.min(z1, z2);
        int l = Math.max(x1, x2);
        int m = Math.max(y1, y2);
        int n = Math.max(z1, z2);
        int o = l - i;
        int p = m - j;
        int q = n - k;
        final int r = velocity.x >= 0.0 ? i : l;
        final int s = velocity.y >= 0.0 ? j : m;
        final int t = velocity.z >= 0.0 ? k : n;
        ImmutableList<Direction.Axis> list = Direction.getCollisionOrder(velocity);
        Direction.Axis axis = (Direction.Axis)list.get(0);
        Direction.Axis axis2 = (Direction.Axis)list.get(1);
        Direction.Axis axis3 = (Direction.Axis)list.get(2);
        final Direction direction = velocity.getComponentAlongAxis(axis) >= 0.0 ? axis.getPositiveDirection() : axis.getNegativeDirection();
        final Direction direction2 = velocity.getComponentAlongAxis(axis2) >= 0.0 ? axis2.getPositiveDirection() : axis2.getNegativeDirection();
        final Direction direction3 = velocity.getComponentAlongAxis(axis3) >= 0.0 ? axis3.getPositiveDirection() : axis3.getNegativeDirection();
        final int u = axis.choose(o, p, q);
        final int v = axis2.choose(o, p, q);
        final int w = axis3.choose(o, p, q);
        return () -> new AbstractIterator<BlockPos>(){
            private final Mutable pos = new Mutable();
            private int deltaAxis1;
            private int deltaAxis2;
            private int deltaAxis3;
            private boolean done;
            private final int axis1x = direction.getOffsetX();
            private final int axis1y = direction.getOffsetY();
            private final int axis1z = direction.getOffsetZ();
            private final int axis2x = direction2.getOffsetX();
            private final int axis2y = direction2.getOffsetY();
            private final int axis2z = direction2.getOffsetZ();
            private final int axis3x = direction3.getOffsetX();
            private final int axis3y = direction3.getOffsetY();
            private final int axis3z = direction3.getOffsetZ();

            protected BlockPos computeNext() {
                if (this.done) {
                    return (BlockPos)this.endOfData();
                }
                this.pos.set(r + this.axis1x * this.deltaAxis1 + this.axis2x * this.deltaAxis2 + this.axis3x * this.deltaAxis3, s + this.axis1y * this.deltaAxis1 + this.axis2y * this.deltaAxis2 + this.axis3y * this.deltaAxis3, t + this.axis1z * this.deltaAxis1 + this.axis2z * this.deltaAxis2 + this.axis3z * this.deltaAxis3);
                if (this.deltaAxis3 < w) {
                    ++this.deltaAxis3;
                } else if (this.deltaAxis2 < v) {
                    ++this.deltaAxis2;
                    this.deltaAxis3 = 0;
                } else if (this.deltaAxis1 < u) {
                    ++this.deltaAxis1;
                    this.deltaAxis3 = 0;
                    this.deltaAxis2 = 0;
                } else {
                    this.done = true;
                }
                return this.pos;
            }

            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    @Override
    public /* synthetic */ Vec3i crossProduct(Vec3i vec) {
        return this.crossProduct(vec);
    }

    @Override
    public /* synthetic */ Vec3i offset(Direction.Axis axis, int distance) {
        return this.offset(axis, distance);
    }

    @Override
    public /* synthetic */ Vec3i offset(Direction direction, int distance) {
        return this.offset(direction, distance);
    }

    @Override
    public /* synthetic */ Vec3i offset(Direction direction) {
        return this.offset(direction);
    }

    @Override
    public /* synthetic */ Vec3i east(int distance) {
        return this.east(distance);
    }

    @Override
    public /* synthetic */ Vec3i east() {
        return this.east();
    }

    @Override
    public /* synthetic */ Vec3i west(int distance) {
        return this.west(distance);
    }

    @Override
    public /* synthetic */ Vec3i west() {
        return this.west();
    }

    @Override
    public /* synthetic */ Vec3i south(int distance) {
        return this.south(distance);
    }

    @Override
    public /* synthetic */ Vec3i south() {
        return this.south();
    }

    @Override
    public /* synthetic */ Vec3i north(int distance) {
        return this.north(distance);
    }

    @Override
    public /* synthetic */ Vec3i north() {
        return this.north();
    }

    @Override
    public /* synthetic */ Vec3i down(int distance) {
        return this.down(distance);
    }

    @Override
    public /* synthetic */ Vec3i down() {
        return this.down();
    }

    @Override
    public /* synthetic */ Vec3i up(int distance) {
        return this.up(distance);
    }

    @Override
    public /* synthetic */ Vec3i up() {
        return this.up();
    }

    @Override
    public /* synthetic */ Vec3i multiply(int scale) {
        return this.multiply(scale);
    }

    @Override
    public /* synthetic */ Vec3i subtract(Vec3i vec) {
        return this.subtract(vec);
    }

    @Override
    public /* synthetic */ Vec3i add(Vec3i vec) {
        return this.add(vec);
    }

    @Override
    public /* synthetic */ Vec3i add(int x, int y, int z) {
        return this.add(x, y, z);
    }

    public static class Mutable
    extends BlockPos {
        public Mutable() {
            this(0, 0, 0);
        }

        public Mutable(int i, int j, int k) {
            super(i, j, k);
        }

        public Mutable(double x, double y, double z) {
            this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        }

        @Override
        public BlockPos add(int i, int j, int k) {
            return super.add(i, j, k).toImmutable();
        }

        @Override
        public BlockPos multiply(int i) {
            return super.multiply(i).toImmutable();
        }

        @Override
        public BlockPos offset(Direction direction, int i) {
            return super.offset(direction, i).toImmutable();
        }

        @Override
        public BlockPos offset(Direction.Axis axis, int i) {
            return super.offset(axis, i).toImmutable();
        }

        @Override
        public BlockPos rotate(BlockRotation rotation) {
            return super.rotate(rotation).toImmutable();
        }

        public Mutable set(int x, int y, int z) {
            this.setX(x);
            this.setY(y);
            this.setZ(z);
            return this;
        }

        public Mutable set(double x, double y, double z) {
            return this.set(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        }

        public Mutable set(Vec3i pos) {
            return this.set(pos.getX(), pos.getY(), pos.getZ());
        }

        public Mutable set(long pos) {
            return this.set(Mutable.unpackLongX(pos), Mutable.unpackLongY(pos), Mutable.unpackLongZ(pos));
        }

        public Mutable set(AxisCycleDirection axis, int x, int y, int z) {
            return this.set(axis.choose(x, y, z, Direction.Axis.X), axis.choose(x, y, z, Direction.Axis.Y), axis.choose(x, y, z, Direction.Axis.Z));
        }

        public Mutable set(Vec3i pos, Direction direction) {
            return this.set(pos.getX() + direction.getOffsetX(), pos.getY() + direction.getOffsetY(), pos.getZ() + direction.getOffsetZ());
        }

        public Mutable set(Vec3i pos, int x, int y, int z) {
            return this.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
        }

        public Mutable set(Vec3i vec1, Vec3i vec2) {
            return this.set(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY(), vec1.getZ() + vec2.getZ());
        }

        public Mutable move(Direction direction) {
            return this.move(direction, 1);
        }

        public Mutable move(Direction direction, int distance) {
            return this.set(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance, this.getZ() + direction.getOffsetZ() * distance);
        }

        public Mutable move(int dx, int dy, int dz) {
            return this.set(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
        }

        public Mutable move(Vec3i vec) {
            return this.set(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ());
        }

        public Mutable clamp(Direction.Axis axis, int min, int max) {
            return switch (axis) {
                default -> throw new MatchException(null, null);
                case Direction.Axis.X -> this.set(MathHelper.clamp(this.getX(), min, max), this.getY(), this.getZ());
                case Direction.Axis.Y -> this.set(this.getX(), MathHelper.clamp(this.getY(), min, max), this.getZ());
                case Direction.Axis.Z -> this.set(this.getX(), this.getY(), MathHelper.clamp(this.getZ(), min, max));
            };
        }

        @Override
        public Mutable setX(int i) {
            super.setX(i);
            return this;
        }

        @Override
        public Mutable setY(int i) {
            super.setY(i);
            return this;
        }

        @Override
        public Mutable setZ(int i) {
            super.setZ(i);
            return this;
        }

        @Override
        public BlockPos toImmutable() {
            return new BlockPos(this);
        }

        @Override
        public /* synthetic */ Vec3i crossProduct(Vec3i vec) {
            return super.crossProduct(vec);
        }

        @Override
        public /* synthetic */ Vec3i offset(Direction.Axis axis, int distance) {
            return this.offset(axis, distance);
        }

        @Override
        public /* synthetic */ Vec3i offset(Direction direction, int distance) {
            return this.offset(direction, distance);
        }

        @Override
        public /* synthetic */ Vec3i offset(Direction direction) {
            return super.offset(direction);
        }

        @Override
        public /* synthetic */ Vec3i east(int distance) {
            return super.east(distance);
        }

        @Override
        public /* synthetic */ Vec3i east() {
            return super.east();
        }

        @Override
        public /* synthetic */ Vec3i west(int distance) {
            return super.west(distance);
        }

        @Override
        public /* synthetic */ Vec3i west() {
            return super.west();
        }

        @Override
        public /* synthetic */ Vec3i south(int distance) {
            return super.south(distance);
        }

        @Override
        public /* synthetic */ Vec3i south() {
            return super.south();
        }

        @Override
        public /* synthetic */ Vec3i north(int distance) {
            return super.north(distance);
        }

        @Override
        public /* synthetic */ Vec3i north() {
            return super.north();
        }

        @Override
        public /* synthetic */ Vec3i down(int distance) {
            return super.down(distance);
        }

        @Override
        public /* synthetic */ Vec3i down() {
            return super.down();
        }

        @Override
        public /* synthetic */ Vec3i up(int distance) {
            return super.up(distance);
        }

        @Override
        public /* synthetic */ Vec3i up() {
            return super.up();
        }

        @Override
        public /* synthetic */ Vec3i multiply(int scale) {
            return this.multiply(scale);
        }

        @Override
        public /* synthetic */ Vec3i subtract(Vec3i vec) {
            return super.subtract(vec);
        }

        @Override
        public /* synthetic */ Vec3i add(Vec3i vec) {
            return super.add(vec);
        }

        @Override
        public /* synthetic */ Vec3i add(int x, int y, int z) {
            return this.add(x, y, z);
        }

        @Override
        public /* synthetic */ Vec3i setZ(int z) {
            return this.setZ(z);
        }

        @Override
        public /* synthetic */ Vec3i setY(int y) {
            return this.setY(y);
        }

        @Override
        public /* synthetic */ Vec3i setX(int x) {
            return this.setX(x);
        }
    }

    public static final class IterationState
    extends Enum<IterationState> {
        public static final /* enum */ IterationState ACCEPT = new IterationState();
        public static final /* enum */ IterationState SKIP = new IterationState();
        public static final /* enum */ IterationState STOP = new IterationState();
        private static final /* synthetic */ IterationState[] field_55168;

        public static IterationState[] values() {
            return (IterationState[])field_55168.clone();
        }

        public static IterationState valueOf(String string) {
            return Enum.valueOf(IterationState.class, string);
        }

        private static /* synthetic */ IterationState[] method_65259() {
            return new IterationState[]{ACCEPT, SKIP, STOP};
        }

        static {
            field_55168 = IterationState.method_65259();
        }
    }
}

