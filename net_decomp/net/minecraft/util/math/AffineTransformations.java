/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.util.math;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.Util;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class AffineTransformations {
    private static final Map<Direction, AffineTransformation> DIRECTION_ROTATIONS = Maps.newEnumMap(Map.of(Direction.SOUTH, AffineTransformation.identity(), Direction.EAST, new AffineTransformation(null, (Quaternionfc)new Quaternionf().rotateY(1.5707964f), null, null), Direction.WEST, new AffineTransformation(null, (Quaternionfc)new Quaternionf().rotateY(-1.5707964f), null, null), Direction.NORTH, new AffineTransformation(null, (Quaternionfc)new Quaternionf().rotateY((float)Math.PI), null, null), Direction.UP, new AffineTransformation(null, (Quaternionfc)new Quaternionf().rotateX(-1.5707964f), null, null), Direction.DOWN, new AffineTransformation(null, (Quaternionfc)new Quaternionf().rotateX(1.5707964f), null, null)));
    private static final Map<Direction, AffineTransformation> INVERTED_DIRECTION_ROTATIONS = Maps.newEnumMap(Util.transformMapValues(DIRECTION_ROTATIONS, AffineTransformation::invert));

    public static AffineTransformation setupUvLock(AffineTransformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(-0.5f, -0.5f, -0.5f);
        return new AffineTransformation((Matrix4fc)matrix4f);
    }

    public static AffineTransformation method_35829(AffineTransformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(0.5f, 0.5f, 0.5f);
        return new AffineTransformation((Matrix4fc)matrix4f);
    }

    public static AffineTransformation getTransformed(AffineTransformation affineTransformation, Direction direction) {
        if (MatrixUtil.isIdentity(affineTransformation.getMatrix())) {
            return affineTransformation;
        }
        AffineTransformation affineTransformation2 = DIRECTION_ROTATIONS.get(direction);
        affineTransformation2 = affineTransformation.multiply(affineTransformation2);
        Vector3f vector3f = affineTransformation2.getMatrix().transformDirection(new Vector3f(0.0f, 0.0f, 1.0f));
        Direction direction2 = Direction.getFacing(vector3f.x, vector3f.y, vector3f.z);
        return INVERTED_DIRECTION_ROTATIONS.get(direction2).multiply(affineTransformation2);
    }
}

