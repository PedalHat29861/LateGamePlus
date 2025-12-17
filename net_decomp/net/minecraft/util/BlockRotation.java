/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.random.Random;

public final class BlockRotation
extends Enum<BlockRotation>
implements StringIdentifiable {
    public static final /* enum */ BlockRotation NONE = new BlockRotation(0, "none", DirectionTransformation.IDENTITY);
    public static final /* enum */ BlockRotation CLOCKWISE_90 = new BlockRotation(1, "clockwise_90", DirectionTransformation.ROT_90_Y_NEG);
    public static final /* enum */ BlockRotation CLOCKWISE_180 = new BlockRotation(2, "180", DirectionTransformation.ROT_180_FACE_XZ);
    public static final /* enum */ BlockRotation COUNTERCLOCKWISE_90 = new BlockRotation(3, "counterclockwise_90", DirectionTransformation.ROT_90_Y_POS);
    public static final IntFunction<BlockRotation> INDEX_MAPPER;
    public static final Codec<BlockRotation> CODEC;
    public static final PacketCodec<ByteBuf, BlockRotation> PACKET_CODEC;
    @Deprecated
    public static final Codec<BlockRotation> ENUM_NAME_CODEC;
    private final int index;
    private final String id;
    private final DirectionTransformation directionTransformation;
    private static final /* synthetic */ BlockRotation[] field_11466;

    public static BlockRotation[] values() {
        return (BlockRotation[])field_11466.clone();
    }

    public static BlockRotation valueOf(String string) {
        return Enum.valueOf(BlockRotation.class, string);
    }

    private BlockRotation(int index, String id, DirectionTransformation directionTransformation) {
        this.index = index;
        this.id = id;
        this.directionTransformation = directionTransformation;
    }

    public BlockRotation rotate(BlockRotation rotation) {
        return switch (rotation.ordinal()) {
            case 2 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield CLOCKWISE_180;
                    }
                    case 1: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 2: {
                        yield NONE;
                    }
                    case 3: 
                }
                yield CLOCKWISE_90;
            }
            case 3 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 1: {
                        yield NONE;
                    }
                    case 2: {
                        yield CLOCKWISE_90;
                    }
                    case 3: 
                }
                yield CLOCKWISE_180;
            }
            case 1 -> {
                switch (this.ordinal()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case 0: {
                        yield CLOCKWISE_90;
                    }
                    case 1: {
                        yield CLOCKWISE_180;
                    }
                    case 2: {
                        yield COUNTERCLOCKWISE_90;
                    }
                    case 3: 
                }
                yield NONE;
            }
            default -> this;
        };
    }

    public DirectionTransformation getDirectionTransformation() {
        return this.directionTransformation;
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return direction;
        }
        return switch (this.ordinal()) {
            case 2 -> direction.getOpposite();
            case 3 -> direction.rotateYCounterclockwise();
            case 1 -> direction.rotateYClockwise();
            default -> direction;
        };
    }

    public int rotate(int rotation, int fullTurn) {
        return switch (this.ordinal()) {
            case 2 -> (rotation + fullTurn / 2) % fullTurn;
            case 3 -> (rotation + fullTurn * 3 / 4) % fullTurn;
            case 1 -> (rotation + fullTurn / 4) % fullTurn;
            default -> rotation;
        };
    }

    public static BlockRotation random(Random random) {
        return Util.getRandom(BlockRotation.values(), random);
    }

    public static List<BlockRotation> randomRotationOrder(Random random) {
        return Util.copyShuffled(BlockRotation.values(), random);
    }

    @Override
    public String asString() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    private static /* synthetic */ BlockRotation[] method_36709() {
        return new BlockRotation[]{NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90};
    }

    static {
        field_11466 = BlockRotation.method_36709();
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(BlockRotation::getIndex, BlockRotation.values(), ValueLists.OutOfBoundsHandling.WRAP);
        CODEC = StringIdentifiable.createCodec(BlockRotation::values);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, BlockRotation::getIndex);
        ENUM_NAME_CODEC = Codecs.enumByName(BlockRotation::valueOf);
    }
}

