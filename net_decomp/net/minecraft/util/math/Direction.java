/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  org.jetbrains.annotations.Contract
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.math;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class Direction
extends Enum<Direction>
implements StringIdentifiable {
    public static final /* enum */ Direction DOWN = new Direction(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0));
    public static final /* enum */ Direction UP = new Direction(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0));
    public static final /* enum */ Direction NORTH = new Direction(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1));
    public static final /* enum */ Direction SOUTH = new Direction(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1));
    public static final /* enum */ Direction WEST = new Direction(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0));
    public static final /* enum */ Direction EAST = new Direction(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));
    public static final StringIdentifiable.EnumCodec<Direction> CODEC;
    public static final Codec<Direction> VERTICAL_CODEC;
    public static final IntFunction<Direction> INDEX_TO_VALUE_FUNCTION;
    public static final PacketCodec<ByteBuf, Direction> PACKET_CODEC;
    @Deprecated
    public static final Codec<Direction> INDEX_CODEC;
    @Deprecated
    public static final Codec<Direction> HORIZONTAL_QUARTER_TURNS_CODEC;
    private static final ImmutableList<Axis> YXZ;
    private static final ImmutableList<Axis> YZX;
    private final int index;
    private final int oppositeIndex;
    private final int horizontalQuarterTurns;
    private final String id;
    private final Axis axis;
    private final AxisDirection direction;
    private final Vec3i vec3i;
    private final Vec3d doubleVector;
    private final Vector3fc floatVector;
    private static final Direction[] ALL;
    private static final Direction[] VALUES;
    private static final Direction[] HORIZONTAL;
    private static final /* synthetic */ Direction[] field_11037;

    public static Direction[] values() {
        return (Direction[])field_11037.clone();
    }

    public static Direction valueOf(String string) {
        return Enum.valueOf(Direction.class, string);
    }

    private Direction(int index, int oppositeIndex, int horizontalQuarterTurns, String id, AxisDirection direction, Axis axis, Vec3i vector) {
        this.index = index;
        this.horizontalQuarterTurns = horizontalQuarterTurns;
        this.oppositeIndex = oppositeIndex;
        this.id = id;
        this.axis = axis;
        this.direction = direction;
        this.vec3i = vector;
        this.doubleVector = Vec3d.of(vector);
        this.floatVector = new Vector3f((float)vector.getX(), (float)vector.getY(), (float)vector.getZ());
    }

    public static Direction[] getEntityFacingOrder(Entity entity) {
        Direction direction3;
        float f = entity.getPitch(1.0f) * ((float)Math.PI / 180);
        float g = -entity.getYaw(1.0f) * ((float)Math.PI / 180);
        float h = MathHelper.sin(f);
        float i = MathHelper.cos(f);
        float j = MathHelper.sin(g);
        float k = MathHelper.cos(g);
        boolean bl = j > 0.0f;
        boolean bl2 = h < 0.0f;
        boolean bl3 = k > 0.0f;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? EAST : WEST;
        Direction direction2 = bl2 ? UP : DOWN;
        Direction direction4 = direction3 = bl3 ? SOUTH : NORTH;
        if (l > n) {
            if (m > o) {
                return Direction.listClosest(direction2, direction, direction3);
            }
            if (p > m) {
                return Direction.listClosest(direction, direction3, direction2);
            }
            return Direction.listClosest(direction, direction2, direction3);
        }
        if (m > p) {
            return Direction.listClosest(direction2, direction3, direction);
        }
        if (o > m) {
            return Direction.listClosest(direction3, direction, direction2);
        }
        return Direction.listClosest(direction3, direction2, direction);
    }

    private static Direction[] listClosest(Direction first, Direction second, Direction third) {
        return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
    }

    public static Direction transform(Matrix4fc matrix, Direction direction) {
        Vector3f vector3f = matrix.transformDirection(direction.floatVector, new Vector3f());
        return Direction.getFacing(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public static Collection<Direction> shuffle(Random random) {
        return Util.copyShuffled(Direction.values(), random);
    }

    public static Stream<Direction> stream() {
        return Stream.of(ALL);
    }

    public static float getHorizontalDegreesOrThrow(Direction direction) {
        return switch (direction.ordinal()) {
            case 2 -> 180.0f;
            case 3 -> 0.0f;
            case 4 -> 90.0f;
            case 5 -> -90.0f;
            default -> throw new IllegalStateException("No y-Rot for vertical axis: " + String.valueOf(direction));
        };
    }

    public Quaternionf getRotationQuaternion() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> new Quaternionf().rotationX((float)Math.PI);
            case 1 -> new Quaternionf();
            case 2 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, (float)Math.PI);
            case 3 -> new Quaternionf().rotationX(1.5707964f);
            case 4 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, 1.5707964f);
            case 5 -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, -1.5707964f);
        };
    }

    public int getIndex() {
        return this.index;
    }

    public int getHorizontalQuarterTurns() {
        return this.horizontalQuarterTurns;
    }

    public AxisDirection getDirection() {
        return this.direction;
    }

    public static Direction getLookDirectionForAxis(Entity entity, Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (EAST.pointsTo(entity.getYaw(1.0f))) {
                    yield EAST;
                }
                yield WEST;
            }
            case 2 -> {
                if (SOUTH.pointsTo(entity.getYaw(1.0f))) {
                    yield SOUTH;
                }
                yield NORTH;
            }
            case 1 -> entity.getPitch(1.0f) < 0.0f ? UP : DOWN;
        };
    }

    public Direction getOpposite() {
        return Direction.byIndex(this.oppositeIndex);
    }

    public Direction rotateClockwise(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.rotateXClockwise();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.rotateYClockwise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.rotateZClockwise();
        };
    }

    public Direction rotateCounterclockwise(Axis axis) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.rotateXCounterclockwise();
            }
            case 1 -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.rotateYCounterclockwise();
            }
            case 2 -> this == NORTH || this == SOUTH ? this : this.rotateZCounterclockwise();
        };
    }

    public Direction rotateYClockwise() {
        return switch (this.ordinal()) {
            case 2 -> EAST;
            case 5 -> SOUTH;
            case 3 -> WEST;
            case 4 -> NORTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction rotateXClockwise() {
        return switch (this.ordinal()) {
            case 1 -> NORTH;
            case 2 -> DOWN;
            case 0 -> SOUTH;
            case 3 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction rotateXCounterclockwise() {
        return switch (this.ordinal()) {
            case 1 -> SOUTH;
            case 3 -> DOWN;
            case 0 -> NORTH;
            case 2 -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction rotateZClockwise() {
        return switch (this.ordinal()) {
            case 1 -> EAST;
            case 5 -> DOWN;
            case 0 -> WEST;
            case 4 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    private Direction rotateZCounterclockwise() {
        return switch (this.ordinal()) {
            case 1 -> WEST;
            case 4 -> DOWN;
            case 0 -> EAST;
            case 5 -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + String.valueOf(this));
        };
    }

    public Direction rotateYCounterclockwise() {
        return switch (this.ordinal()) {
            case 2 -> WEST;
            case 5 -> NORTH;
            case 3 -> EAST;
            case 4 -> SOUTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + String.valueOf(this));
        };
    }

    public int getOffsetX() {
        return this.vec3i.getX();
    }

    public int getOffsetY() {
        return this.vec3i.getY();
    }

    public int getOffsetZ() {
        return this.vec3i.getZ();
    }

    public Vector3f getUnitVector() {
        return new Vector3f(this.floatVector);
    }

    public String getId() {
        return this.id;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public static @Nullable Direction byId(String id) {
        return CODEC.byId(id);
    }

    public static Direction byIndex(int index) {
        return VALUES[MathHelper.abs(index % VALUES.length)];
    }

    public static Direction fromHorizontalQuarterTurns(int quarterTurns) {
        return HORIZONTAL[MathHelper.abs(quarterTurns % HORIZONTAL.length)];
    }

    public static Direction fromHorizontalDegrees(double angle) {
        return Direction.fromHorizontalQuarterTurns(MathHelper.floor(angle / 90.0 + 0.5) & 3);
    }

    public static Direction from(Axis axis, AxisDirection direction) {
        return switch (axis.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (direction == AxisDirection.POSITIVE) {
                    yield EAST;
                }
                yield WEST;
            }
            case 1 -> {
                if (direction == AxisDirection.POSITIVE) {
                    yield UP;
                }
                yield DOWN;
            }
            case 2 -> direction == AxisDirection.POSITIVE ? SOUTH : NORTH;
        };
    }

    public float getPositiveHorizontalDegrees() {
        return (this.horizontalQuarterTurns & 3) * 90;
    }

    public static Direction random(Random random) {
        return Util.getRandom(ALL, random);
    }

    public static Direction getFacing(double x, double y, double z) {
        return Direction.getFacing((float)x, (float)y, (float)z);
    }

    public static Direction getFacing(float x, float y, float z) {
        Direction direction = NORTH;
        float f = Float.MIN_VALUE;
        for (Direction direction2 : ALL) {
            float g = x * (float)direction2.vec3i.getX() + y * (float)direction2.vec3i.getY() + z * (float)direction2.vec3i.getZ();
            if (!(g > f)) continue;
            f = g;
            direction = direction2;
        }
        return direction;
    }

    public static Direction getFacing(Vec3d vec) {
        return Direction.getFacing(vec.x, vec.y, vec.z);
    }

    @Contract(value="_,_,_,!null->!null;_,_,_,_->_")
    public static @Nullable Direction fromVector(int x, int y, int z, @Nullable Direction fallback) {
        int i = Math.abs(x);
        int j = Math.abs(y);
        int k = Math.abs(z);
        if (i > k && i > j) {
            return x < 0 ? WEST : EAST;
        }
        if (k > i && k > j) {
            return z < 0 ? NORTH : SOUTH;
        }
        if (j > i && j > k) {
            return y < 0 ? DOWN : UP;
        }
        return fallback;
    }

    @Contract(value="_,!null->!null;_,_->_")
    public static @Nullable Direction fromVector(Vec3i vec, @Nullable Direction fallback) {
        return Direction.fromVector(vec.getX(), vec.getY(), vec.getZ(), fallback);
    }

    public String toString() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private static DataResult<Direction> validateVertical(Direction direction) {
        return direction.getAxis().isVertical() ? DataResult.success((Object)direction) : DataResult.error(() -> "Expected a vertical direction");
    }

    public static Direction get(AxisDirection direction, Axis axis) {
        for (Direction direction2 : ALL) {
            if (direction2.getDirection() != direction || direction2.getAxis() != axis) continue;
            return direction2;
        }
        throw new IllegalArgumentException("No such direction: " + String.valueOf((Object)direction) + " " + String.valueOf(axis));
    }

    public static ImmutableList<Axis> getCollisionOrder(Vec3d vec3d) {
        if (Math.abs(vec3d.x) < Math.abs(vec3d.z)) {
            return YZX;
        }
        return YXZ;
    }

    public Vec3i getVector() {
        return this.vec3i;
    }

    public Vec3d getDoubleVector() {
        return this.doubleVector;
    }

    public Vector3fc getFloatVector() {
        return this.floatVector;
    }

    public boolean pointsTo(float yaw) {
        float f = yaw * ((float)Math.PI / 180);
        float g = -MathHelper.sin(f);
        float h = MathHelper.cos(f);
        return (float)this.vec3i.getX() * g + (float)this.vec3i.getZ() * h > 0.0f;
    }

    private static /* synthetic */ Direction[] method_36931() {
        return new Direction[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
    }

    static {
        field_11037 = Direction.method_36931();
        CODEC = StringIdentifiable.createCodec(Direction::values);
        VERTICAL_CODEC = CODEC.validate(Direction::validateVertical);
        INDEX_TO_VALUE_FUNCTION = ValueLists.createIndexToValueFunction(Direction::getIndex, Direction.values(), ValueLists.OutOfBoundsHandling.WRAP);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE_FUNCTION, Direction::getIndex);
        INDEX_CODEC = Codec.BYTE.xmap(Direction::byIndex, direction -> (byte)direction.getIndex());
        HORIZONTAL_QUARTER_TURNS_CODEC = Codec.BYTE.xmap(Direction::fromHorizontalQuarterTurns, direction -> (byte)direction.getHorizontalQuarterTurns());
        YXZ = ImmutableList.of((Object)Axis.Y, (Object)Axis.X, (Object)Axis.Z);
        YZX = ImmutableList.of((Object)Axis.Y, (Object)Axis.Z, (Object)Axis.X);
        ALL = Direction.values();
        VALUES = (Direction[])Arrays.stream(ALL).sorted(Comparator.comparingInt(direction -> direction.index)).toArray(Direction[]::new);
        HORIZONTAL = (Direction[])Arrays.stream(ALL).filter(direction -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt(direction -> direction.horizontalQuarterTurns)).toArray(Direction[]::new);
    }

    public static abstract sealed class Axis
    extends Enum<Axis>
    implements StringIdentifiable,
    Predicate<Direction> {
        public static final /* enum */ Axis X = new Axis("x"){

            @Override
            public int choose(int x, int y, int z) {
                return x;
            }

            @Override
            public boolean choose(boolean x, boolean y, boolean z) {
                return x;
            }

            @Override
            public double choose(double x, double y, double z) {
                return x;
            }

            @Override
            public Direction getPositiveDirection() {
                return EAST;
            }

            @Override
            public Direction getNegativeDirection() {
                return WEST;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };
        public static final /* enum */ Axis Y = new Axis("y"){

            @Override
            public int choose(int x, int y, int z) {
                return y;
            }

            @Override
            public double choose(double x, double y, double z) {
                return y;
            }

            @Override
            public boolean choose(boolean x, boolean y, boolean z) {
                return y;
            }

            @Override
            public Direction getPositiveDirection() {
                return UP;
            }

            @Override
            public Direction getNegativeDirection() {
                return DOWN;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };
        public static final /* enum */ Axis Z = new Axis("z"){

            @Override
            public int choose(int x, int y, int z) {
                return z;
            }

            @Override
            public double choose(double x, double y, double z) {
                return z;
            }

            @Override
            public boolean choose(boolean x, boolean y, boolean z) {
                return z;
            }

            @Override
            public Direction getPositiveDirection() {
                return SOUTH;
            }

            @Override
            public Direction getNegativeDirection() {
                return NORTH;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };
        public static final Axis[] VALUES;
        public static final StringIdentifiable.EnumCodec<Axis> CODEC;
        private final String id;
        private static final /* synthetic */ Axis[] field_11049;

        public static Axis[] values() {
            return (Axis[])field_11049.clone();
        }

        public static Axis valueOf(String string) {
            return Enum.valueOf(Axis.class, string);
        }

        Axis(String id) {
            this.id = id;
        }

        public static @Nullable Axis fromId(String id) {
            return CODEC.byId(id);
        }

        public String getId() {
            return this.id;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public abstract Direction getPositiveDirection();

        public abstract Direction getNegativeDirection();

        public Direction[] getDirections() {
            return new Direction[]{this.getPositiveDirection(), this.getNegativeDirection()};
        }

        public String toString() {
            return this.id;
        }

        public static Axis pickRandomAxis(Random random) {
            return Util.getRandom(VALUES, random);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis() == this;
        }

        public Type getType() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> Type.HORIZONTAL;
                case 1 -> Type.VERTICAL;
            };
        }

        @Override
        public String asString() {
            return this.id;
        }

        public abstract int choose(int var1, int var2, int var3);

        public abstract double choose(double var1, double var3, double var5);

        public abstract boolean choose(boolean var1, boolean var2, boolean var3);

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }

        private static /* synthetic */ Axis[] method_36932() {
            return new Axis[]{X, Y, Z};
        }

        static {
            field_11049 = Axis.method_36932();
            VALUES = Axis.values();
            CODEC = StringIdentifiable.createCodec(Axis::values);
        }
    }

    public static final class AxisDirection
    extends Enum<AxisDirection> {
        public static final /* enum */ AxisDirection POSITIVE = new AxisDirection(1, "Towards positive");
        public static final /* enum */ AxisDirection NEGATIVE = new AxisDirection(-1, "Towards negative");
        private final int offset;
        private final String description;
        private static final /* synthetic */ AxisDirection[] field_11058;

        public static AxisDirection[] values() {
            return (AxisDirection[])field_11058.clone();
        }

        public static AxisDirection valueOf(String string) {
            return Enum.valueOf(AxisDirection.class, string);
        }

        private AxisDirection(int offset, String description) {
            this.offset = offset;
            this.description = description;
        }

        public int offset() {
            return this.offset;
        }

        public String getDescription() {
            return this.description;
        }

        public String toString() {
            return this.description;
        }

        public AxisDirection getOpposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }

        private static /* synthetic */ AxisDirection[] method_36933() {
            return new AxisDirection[]{POSITIVE, NEGATIVE};
        }

        static {
            field_11058 = AxisDirection.method_36933();
        }
    }

    public static final class Type
    extends Enum<Type>
    implements Iterable<Direction>,
    Predicate<Direction> {
        public static final /* enum */ Type HORIZONTAL = new Type(new Direction[]{NORTH, EAST, SOUTH, WEST}, new Axis[]{Axis.X, Axis.Z});
        public static final /* enum */ Type VERTICAL = new Type(new Direction[]{UP, DOWN}, new Axis[]{Axis.Y});
        private final Direction[] facingArray;
        private final Axis[] axisArray;
        private static final /* synthetic */ Type[] field_11063;

        public static Type[] values() {
            return (Type[])field_11063.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(Direction[] facingArray, Axis[] axisArray) {
            this.facingArray = facingArray;
            this.axisArray = axisArray;
        }

        public Direction random(Random random) {
            return Util.getRandom(this.facingArray, random);
        }

        public Axis randomAxis(Random random) {
            return Util.getRandom(this.axisArray, random);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis().getType() == this;
        }

        @Override
        public Iterator<Direction> iterator() {
            return Iterators.forArray((Object[])this.facingArray);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.facingArray);
        }

        public List<Direction> getShuffled(Random random) {
            return Util.copyShuffled(this.facingArray, random);
        }

        public int getFacingCount() {
            return this.facingArray.length;
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object direction) {
            return this.test((Direction)direction);
        }

        private static /* synthetic */ Type[] method_36934() {
            return new Type[]{HORIZONTAL, VERTICAL};
        }

        static {
            field_11063 = Type.method_36934();
        }
    }
}

