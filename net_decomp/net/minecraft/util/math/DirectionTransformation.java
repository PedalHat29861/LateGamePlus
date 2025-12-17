/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Vector3i
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.math;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.block.enums.Orientation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisTransformation;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;

public final class DirectionTransformation
extends Enum<DirectionTransformation>
implements StringIdentifiable {
    public static final /* enum */ DirectionTransformation IDENTITY = new DirectionTransformation("identity", AxisTransformation.P123, false, false, false);
    public static final /* enum */ DirectionTransformation ROT_180_FACE_XY = new DirectionTransformation("rot_180_face_xy", AxisTransformation.P123, true, true, false);
    public static final /* enum */ DirectionTransformation ROT_180_FACE_XZ = new DirectionTransformation("rot_180_face_xz", AxisTransformation.P123, true, false, true);
    public static final /* enum */ DirectionTransformation ROT_180_FACE_YZ = new DirectionTransformation("rot_180_face_yz", AxisTransformation.P123, false, true, true);
    public static final /* enum */ DirectionTransformation ROT_120_NNN = new DirectionTransformation("rot_120_nnn", AxisTransformation.P231, false, false, false);
    public static final /* enum */ DirectionTransformation ROT_120_NNP = new DirectionTransformation("rot_120_nnp", AxisTransformation.P312, true, false, true);
    public static final /* enum */ DirectionTransformation ROT_120_NPN = new DirectionTransformation("rot_120_npn", AxisTransformation.P312, false, true, true);
    public static final /* enum */ DirectionTransformation ROT_120_NPP = new DirectionTransformation("rot_120_npp", AxisTransformation.P231, true, false, true);
    public static final /* enum */ DirectionTransformation ROT_120_PNN = new DirectionTransformation("rot_120_pnn", AxisTransformation.P312, true, true, false);
    public static final /* enum */ DirectionTransformation ROT_120_PNP = new DirectionTransformation("rot_120_pnp", AxisTransformation.P231, true, true, false);
    public static final /* enum */ DirectionTransformation ROT_120_PPN = new DirectionTransformation("rot_120_ppn", AxisTransformation.P231, false, true, true);
    public static final /* enum */ DirectionTransformation ROT_120_PPP = new DirectionTransformation("rot_120_ppp", AxisTransformation.P312, false, false, false);
    public static final /* enum */ DirectionTransformation ROT_180_EDGE_XY_NEG = new DirectionTransformation("rot_180_edge_xy_neg", AxisTransformation.P213, true, true, true);
    public static final /* enum */ DirectionTransformation ROT_180_EDGE_XY_POS = new DirectionTransformation("rot_180_edge_xy_pos", AxisTransformation.P213, false, false, true);
    public static final /* enum */ DirectionTransformation ROT_180_EDGE_XZ_NEG = new DirectionTransformation("rot_180_edge_xz_neg", AxisTransformation.P321, true, true, true);
    public static final /* enum */ DirectionTransformation ROT_180_EDGE_XZ_POS = new DirectionTransformation("rot_180_edge_xz_pos", AxisTransformation.P321, false, true, false);
    public static final /* enum */ DirectionTransformation ROT_180_EDGE_YZ_NEG = new DirectionTransformation("rot_180_edge_yz_neg", AxisTransformation.P132, true, true, true);
    public static final /* enum */ DirectionTransformation ROT_180_EDGE_YZ_POS = new DirectionTransformation("rot_180_edge_yz_pos", AxisTransformation.P132, true, false, false);
    public static final /* enum */ DirectionTransformation ROT_90_X_NEG = new DirectionTransformation("rot_90_x_neg", AxisTransformation.P132, false, false, true);
    public static final /* enum */ DirectionTransformation ROT_90_X_POS = new DirectionTransformation("rot_90_x_pos", AxisTransformation.P132, false, true, false);
    public static final /* enum */ DirectionTransformation ROT_90_Y_NEG = new DirectionTransformation("rot_90_y_neg", AxisTransformation.P321, true, false, false);
    public static final /* enum */ DirectionTransformation ROT_90_Y_POS = new DirectionTransformation("rot_90_y_pos", AxisTransformation.P321, false, false, true);
    public static final /* enum */ DirectionTransformation ROT_90_Z_NEG = new DirectionTransformation("rot_90_z_neg", AxisTransformation.P213, false, true, false);
    public static final /* enum */ DirectionTransformation ROT_90_Z_POS = new DirectionTransformation("rot_90_z_pos", AxisTransformation.P213, true, false, false);
    public static final /* enum */ DirectionTransformation INVERSION = new DirectionTransformation("inversion", AxisTransformation.P123, true, true, true);
    public static final /* enum */ DirectionTransformation INVERT_X = new DirectionTransformation("invert_x", AxisTransformation.P123, true, false, false);
    public static final /* enum */ DirectionTransformation INVERT_Y = new DirectionTransformation("invert_y", AxisTransformation.P123, false, true, false);
    public static final /* enum */ DirectionTransformation INVERT_Z = new DirectionTransformation("invert_z", AxisTransformation.P123, false, false, true);
    public static final /* enum */ DirectionTransformation ROT_60_REF_NNN = new DirectionTransformation("rot_60_ref_nnn", AxisTransformation.P312, true, true, true);
    public static final /* enum */ DirectionTransformation ROT_60_REF_NNP = new DirectionTransformation("rot_60_ref_nnp", AxisTransformation.P231, true, false, false);
    public static final /* enum */ DirectionTransformation ROT_60_REF_NPN = new DirectionTransformation("rot_60_ref_npn", AxisTransformation.P231, false, false, true);
    public static final /* enum */ DirectionTransformation ROT_60_REF_NPP = new DirectionTransformation("rot_60_ref_npp", AxisTransformation.P312, false, false, true);
    public static final /* enum */ DirectionTransformation ROT_60_REF_PNN = new DirectionTransformation("rot_60_ref_pnn", AxisTransformation.P231, false, true, false);
    public static final /* enum */ DirectionTransformation ROT_60_REF_PNP = new DirectionTransformation("rot_60_ref_pnp", AxisTransformation.P312, true, false, false);
    public static final /* enum */ DirectionTransformation ROT_60_REF_PPN = new DirectionTransformation("rot_60_ref_ppn", AxisTransformation.P312, false, true, false);
    public static final /* enum */ DirectionTransformation ROT_60_REF_PPP = new DirectionTransformation("rot_60_ref_ppp", AxisTransformation.P231, true, true, true);
    public static final /* enum */ DirectionTransformation SWAP_XY = new DirectionTransformation("swap_xy", AxisTransformation.P213, false, false, false);
    public static final /* enum */ DirectionTransformation SWAP_YZ = new DirectionTransformation("swap_yz", AxisTransformation.P132, false, false, false);
    public static final /* enum */ DirectionTransformation SWAP_XZ = new DirectionTransformation("swap_xz", AxisTransformation.P321, false, false, false);
    public static final /* enum */ DirectionTransformation SWAP_NEG_XY = new DirectionTransformation("swap_neg_xy", AxisTransformation.P213, true, true, false);
    public static final /* enum */ DirectionTransformation SWAP_NEG_YZ = new DirectionTransformation("swap_neg_yz", AxisTransformation.P132, false, true, true);
    public static final /* enum */ DirectionTransformation SWAP_NEG_XZ = new DirectionTransformation("swap_neg_xz", AxisTransformation.P321, true, false, true);
    public static final /* enum */ DirectionTransformation ROT_90_REF_X_NEG = new DirectionTransformation("rot_90_ref_x_neg", AxisTransformation.P132, true, false, true);
    public static final /* enum */ DirectionTransformation ROT_90_REF_X_POS = new DirectionTransformation("rot_90_ref_x_pos", AxisTransformation.P132, true, true, false);
    public static final /* enum */ DirectionTransformation ROT_90_REF_Y_NEG = new DirectionTransformation("rot_90_ref_y_neg", AxisTransformation.P321, true, true, false);
    public static final /* enum */ DirectionTransformation ROT_90_REF_Y_POS = new DirectionTransformation("rot_90_ref_y_pos", AxisTransformation.P321, false, true, true);
    public static final /* enum */ DirectionTransformation ROT_90_REF_Z_NEG = new DirectionTransformation("rot_90_ref_z_neg", AxisTransformation.P213, false, true, true);
    public static final /* enum */ DirectionTransformation ROT_90_REF_Z_POS = new DirectionTransformation("rot_90_ref_z_pos", AxisTransformation.P213, true, false, true);
    public static final DirectionTransformation field_64506;
    public static final DirectionTransformation field_64507;
    public static final DirectionTransformation field_64508;
    public static final DirectionTransformation field_64509;
    public static final DirectionTransformation field_64510;
    public static final DirectionTransformation field_64511;
    public static final DirectionTransformation field_64512;
    public static final DirectionTransformation field_64513;
    public static final DirectionTransformation field_64514;
    private final Matrix3fc matrix;
    private final String name;
    private @Nullable Map<Direction, Direction> mappings;
    private final boolean flipX;
    private final boolean flipY;
    private final boolean flipZ;
    private final AxisTransformation axisTransformation;
    private static final DirectionTransformation[][] COMBINATIONS;
    private static final DirectionTransformation[] INVERSES;
    private static final /* synthetic */ DirectionTransformation[] field_23298;

    public static DirectionTransformation[] values() {
        return (DirectionTransformation[])field_23298.clone();
    }

    public static DirectionTransformation valueOf(String string) {
        return Enum.valueOf(DirectionTransformation.class, string);
    }

    private DirectionTransformation(String name, AxisTransformation axisTransformation, boolean flipX, boolean flipY, boolean flipZ) {
        this.name = name;
        this.flipX = flipX;
        this.flipY = flipY;
        this.flipZ = flipZ;
        this.axisTransformation = axisTransformation;
        this.matrix = new Matrix3f().scaling(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f, flipZ ? -1.0f : 1.0f).mul(axisTransformation.getMatrix());
    }

    private static int toIndex(boolean flipX, boolean flipY, boolean flipZ, AxisTransformation axisTransformation) {
        int i = (flipZ ? 4 : 0) + (flipY ? 2 : 0) + (flipX ? 1 : 0);
        return axisTransformation.ordinal() << 3 | i;
    }

    private int getIndex() {
        return DirectionTransformation.toIndex(this.flipX, this.flipY, this.flipZ, this.axisTransformation);
    }

    public DirectionTransformation prepend(DirectionTransformation transformation) {
        return COMBINATIONS[this.ordinal()][transformation.ordinal()];
    }

    public DirectionTransformation inverse() {
        return INVERSES[this.ordinal()];
    }

    public Matrix3fc getMatrix() {
        return this.matrix;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public Direction map(Direction direction) {
        if (this.mappings == null) {
            this.mappings = Util.mapEnum(Direction.class, d -> {
                Direction.Axis axis = d.getAxis();
                Direction.AxisDirection axisDirection = d.getDirection();
                Direction.Axis axis2 = this.axisTransformation.getInverse().map(axis);
                Direction.AxisDirection axisDirection2 = this.shouldFlipDirection(axis2) ? axisDirection.getOpposite() : axisDirection;
                return Direction.from(axis2, axisDirection2);
            });
        }
        return this.mappings.get(direction);
    }

    public Vector3i map(Vector3i vec) {
        this.axisTransformation.map(vec);
        vec.x = vec.x * (this.flipX ? -1 : 1);
        vec.y = vec.y * (this.flipY ? -1 : 1);
        vec.z = vec.z * (this.flipZ ? -1 : 1);
        return vec;
    }

    public boolean shouldFlipDirection(Direction.Axis axis) {
        return switch (axis) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> this.flipX;
            case Direction.Axis.Y -> this.flipY;
            case Direction.Axis.Z -> this.flipZ;
        };
    }

    public AxisTransformation getAxisTransformation() {
        return this.axisTransformation;
    }

    public Orientation mapJigsawOrientation(Orientation orientation) {
        return Orientation.byDirections(this.map(orientation.getFacing()), this.map(orientation.getRotation()));
    }

    private static /* synthetic */ DirectionTransformation[] method_36928() {
        return new DirectionTransformation[]{IDENTITY, ROT_180_FACE_XY, ROT_180_FACE_XZ, ROT_180_FACE_YZ, ROT_120_NNN, ROT_120_NNP, ROT_120_NPN, ROT_120_NPP, ROT_120_PNN, ROT_120_PNP, ROT_120_PPN, ROT_120_PPP, ROT_180_EDGE_XY_NEG, ROT_180_EDGE_XY_POS, ROT_180_EDGE_XZ_NEG, ROT_180_EDGE_XZ_POS, ROT_180_EDGE_YZ_NEG, ROT_180_EDGE_YZ_POS, ROT_90_X_NEG, ROT_90_X_POS, ROT_90_Y_NEG, ROT_90_Y_POS, ROT_90_Z_NEG, ROT_90_Z_POS, INVERSION, INVERT_X, INVERT_Y, INVERT_Z, ROT_60_REF_NNN, ROT_60_REF_NNP, ROT_60_REF_NPN, ROT_60_REF_NPP, ROT_60_REF_PNN, ROT_60_REF_PNP, ROT_60_REF_PPN, ROT_60_REF_PPP, SWAP_XY, SWAP_YZ, SWAP_XZ, SWAP_NEG_XY, SWAP_NEG_YZ, SWAP_NEG_XZ, ROT_90_REF_X_NEG, ROT_90_REF_X_POS, ROT_90_REF_Y_NEG, ROT_90_REF_Y_POS, ROT_90_REF_Z_NEG, ROT_90_REF_Z_POS};
    }

    static {
        field_23298 = DirectionTransformation.method_36928();
        field_64506 = ROT_90_X_POS;
        field_64507 = ROT_180_FACE_YZ;
        field_64508 = ROT_90_X_NEG;
        field_64509 = ROT_90_Y_POS;
        field_64510 = ROT_180_FACE_XZ;
        field_64511 = ROT_90_Y_NEG;
        field_64512 = ROT_90_Z_POS;
        field_64513 = ROT_180_FACE_XY;
        field_64514 = ROT_90_Z_NEG;
        COMBINATIONS = Util.make(() -> {
            DirectionTransformation[] directionTransformations = DirectionTransformation.values();
            DirectionTransformation[][] directionTransformations2 = new DirectionTransformation[directionTransformations.length][directionTransformations.length];
            Map<Integer, DirectionTransformation> map = Arrays.stream(directionTransformations).collect(Collectors.toMap(DirectionTransformation::getIndex, transformation -> transformation));
            for (DirectionTransformation directionTransformation : directionTransformations) {
                for (DirectionTransformation directionTransformation2 : directionTransformations) {
                    AxisTransformation axisTransformation = directionTransformation2.axisTransformation.prepend(directionTransformation.axisTransformation);
                    boolean bl = directionTransformation.shouldFlipDirection(Direction.Axis.X) ^ directionTransformation2.shouldFlipDirection(directionTransformation.axisTransformation.map(Direction.Axis.X));
                    boolean bl2 = directionTransformation.shouldFlipDirection(Direction.Axis.Y) ^ directionTransformation2.shouldFlipDirection(directionTransformation.axisTransformation.map(Direction.Axis.Y));
                    boolean bl3 = directionTransformation.shouldFlipDirection(Direction.Axis.Z) ^ directionTransformation2.shouldFlipDirection(directionTransformation.axisTransformation.map(Direction.Axis.Z));
                    directionTransformations2[directionTransformation.ordinal()][directionTransformation2.ordinal()] = map.get(DirectionTransformation.toIndex(bl, bl2, bl3, axisTransformation));
                }
            }
            return directionTransformations2;
        });
        INVERSES = (DirectionTransformation[])Arrays.stream(DirectionTransformation.values()).map((? super T a) -> Arrays.stream(DirectionTransformation.values()).filter(b -> a.prepend((DirectionTransformation)b) == IDENTITY).findAny().get()).toArray(DirectionTransformation[]::new);
    }
}

