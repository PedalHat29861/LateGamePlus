/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.block;

import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;

public class WireOrientation {
    public static final PacketCodec<ByteBuf, WireOrientation> PACKET_CODEC = PacketCodecs.indexed(WireOrientation::fromOrdinal, WireOrientation::ordinal);
    private static final WireOrientation[] VALUES = Util.make(() -> {
        WireOrientation[] wireOrientations = new WireOrientation[48];
        WireOrientation.initializeValuesArray(new WireOrientation(Direction.UP, Direction.NORTH, SideBias.LEFT), wireOrientations);
        return wireOrientations;
    });
    private final Direction up;
    private final Direction front;
    private final Direction right;
    private final SideBias sideBias;
    private final int ordinal;
    private final List<Direction> directionsByPriority;
    private final List<Direction> horizontalDirections;
    private final List<Direction> verticalDirections;
    private final Map<Direction, WireOrientation> siblingsByFront = new EnumMap<Direction, WireOrientation>(Direction.class);
    private final Map<Direction, WireOrientation> siblingsByUp = new EnumMap<Direction, WireOrientation>(Direction.class);
    private final Map<SideBias, WireOrientation> siblingsBySideBias = new EnumMap<SideBias, WireOrientation>(SideBias.class);

    private WireOrientation(Direction up, Direction front, SideBias sideBias) {
        this.up = up;
        this.front = front;
        this.sideBias = sideBias;
        this.ordinal = WireOrientation.ordinalFromComponents(up, front, sideBias);
        Vec3i vec3i = front.getVector().crossProduct(up.getVector());
        Direction direction2 = Direction.fromVector(vec3i, null);
        Objects.requireNonNull(direction2);
        this.right = this.sideBias == SideBias.RIGHT ? direction2 : direction2.getOpposite();
        this.directionsByPriority = List.of(this.front.getOpposite(), this.front, this.right, this.right.getOpposite(), this.up.getOpposite(), this.up);
        this.horizontalDirections = this.directionsByPriority.stream().filter(direction -> direction.getAxis() != this.up.getAxis()).toList();
        this.verticalDirections = this.directionsByPriority.stream().filter(direction -> direction.getAxis() == this.up.getAxis()).toList();
    }

    public static WireOrientation of(Direction up, Direction front, SideBias sideBias) {
        return VALUES[WireOrientation.ordinalFromComponents(up, front, sideBias)];
    }

    public WireOrientation withUp(Direction direction) {
        return this.siblingsByUp.get(direction);
    }

    public WireOrientation withFront(Direction direction) {
        return this.siblingsByFront.get(direction);
    }

    public WireOrientation withFrontIfNotUp(Direction direction) {
        if (direction.getAxis() == this.up.getAxis()) {
            return this;
        }
        return this.siblingsByFront.get(direction);
    }

    public WireOrientation withFrontAndSideBias(Direction direction) {
        WireOrientation wireOrientation = this.withFront(direction);
        if (this.front == wireOrientation.right) {
            return wireOrientation.withOppositeSideBias();
        }
        return wireOrientation;
    }

    public WireOrientation withSideBias(SideBias sideBias) {
        return this.siblingsBySideBias.get((Object)sideBias);
    }

    public WireOrientation withOppositeSideBias() {
        return this.withSideBias(this.sideBias.opposite());
    }

    public Direction getFront() {
        return this.front;
    }

    public Direction getUp() {
        return this.up;
    }

    public Direction getRight() {
        return this.right;
    }

    public SideBias getSideBias() {
        return this.sideBias;
    }

    public List<Direction> getDirectionsByPriority() {
        return this.directionsByPriority;
    }

    public List<Direction> getHorizontalDirections() {
        return this.horizontalDirections;
    }

    public List<Direction> getVerticalDirections() {
        return this.verticalDirections;
    }

    public String toString() {
        return "[up=" + String.valueOf(this.up) + ",front=" + String.valueOf(this.front) + ",sideBias=" + String.valueOf((Object)this.sideBias) + "]";
    }

    public int ordinal() {
        return this.ordinal;
    }

    public static WireOrientation fromOrdinal(int ordinal) {
        return VALUES[ordinal];
    }

    public static WireOrientation random(Random random) {
        return Util.getRandom(VALUES, random);
    }

    private static WireOrientation initializeValuesArray(WireOrientation prime, WireOrientation[] valuesOut) {
        Direction direction2;
        if (valuesOut[prime.ordinal()] != null) {
            return valuesOut[prime.ordinal()];
        }
        valuesOut[prime.ordinal()] = prime;
        for (SideBias sideBias : SideBias.values()) {
            prime.siblingsBySideBias.put(sideBias, WireOrientation.initializeValuesArray(new WireOrientation(prime.up, prime.front, sideBias), valuesOut));
        }
        for (Enum enum_ : Direction.values()) {
            direction2 = prime.up;
            if (enum_ == prime.up) {
                direction2 = prime.front.getOpposite();
            }
            if (enum_ == prime.up.getOpposite()) {
                direction2 = prime.front;
            }
            prime.siblingsByFront.put((Direction)enum_, WireOrientation.initializeValuesArray(new WireOrientation(direction2, (Direction)enum_, prime.sideBias), valuesOut));
        }
        for (Enum enum_ : Direction.values()) {
            direction2 = prime.front;
            if (enum_ == prime.front) {
                direction2 = prime.up.getOpposite();
            }
            if (enum_ == prime.front.getOpposite()) {
                direction2 = prime.up;
            }
            prime.siblingsByUp.put((Direction)enum_, WireOrientation.initializeValuesArray(new WireOrientation((Direction)enum_, direction2, prime.sideBias), valuesOut));
        }
        return prime;
    }

    @VisibleForTesting
    protected static int ordinalFromComponents(Direction up, Direction front, SideBias sideBias) {
        if (up.getAxis() == front.getAxis()) {
            throw new IllegalStateException("Up-vector and front-vector can not be on the same axis");
        }
        int i = up.getAxis() == Direction.Axis.Y ? (front.getAxis() == Direction.Axis.X ? 1 : 0) : (front.getAxis() == Direction.Axis.Y ? 1 : 0);
        int j = i << 1 | front.getDirection().ordinal();
        return ((up.ordinal() << 2) + j << 1) + sideBias.ordinal();
    }

    public static final class SideBias
    extends Enum<SideBias> {
        public static final /* enum */ SideBias LEFT = new SideBias("left");
        public static final /* enum */ SideBias RIGHT = new SideBias("right");
        private final String name;
        private static final /* synthetic */ SideBias[] field_52684;

        public static SideBias[] values() {
            return (SideBias[])field_52684.clone();
        }

        public static SideBias valueOf(String string) {
            return Enum.valueOf(SideBias.class, string);
        }

        private SideBias(String name) {
            this.name = name;
        }

        public SideBias opposite() {
            return this == LEFT ? RIGHT : LEFT;
        }

        public String toString() {
            return this.name;
        }

        private static /* synthetic */ SideBias[] method_61864() {
            return new SideBias[]{LEFT, RIGHT};
        }

        static {
            field_52684 = SideBias.method_61864();
        }
    }
}

