/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.timeline;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Easing;

public interface EasingType {
    public static final Codecs.IdMapper<String, EasingType> EASING_TYPES_BY_NAME = new Codecs.IdMapper();
    public static final Codec<EasingType> CODEC = Codec.either(EASING_TYPES_BY_NAME.getCodec((Codec<String>)Codec.STRING), CubicBezier.CODEC).xmap(Either::unwrap, easing -> {
        Either either;
        if (easing instanceof CubicBezier) {
            CubicBezier cubicBezier = (CubicBezier)easing;
            either = Either.right((Object)cubicBezier);
        } else {
            either = Either.left((Object)easing);
        }
        return either;
    });
    public static final EasingType CONSTANT = EasingType.register("constant", x -> 0.0f);
    public static final EasingType LINEAR = EasingType.register("linear", x -> x);
    public static final EasingType IN_BACK = EasingType.register("in_back", Easing::inBack);
    public static final EasingType IN_BOUNCE = EasingType.register("in_bounce", Easing::inBounce);
    public static final EasingType IN_CIRC = EasingType.register("in_circ", Easing::inCirc);
    public static final EasingType IN_CUBIC = EasingType.register("in_cubic", Easing::inCubic);
    public static final EasingType IN_ELASTIC = EasingType.register("in_elastic", Easing::inElastic);
    public static final EasingType IN_EXPO = EasingType.register("in_expo", Easing::inExpo);
    public static final EasingType IN_QUAD = EasingType.register("in_quad", Easing::inQuad);
    public static final EasingType IN_QUART = EasingType.register("in_quart", Easing::inQuart);
    public static final EasingType IN_QUINT = EasingType.register("in_quint", Easing::inQuint);
    public static final EasingType IN_SINE = EasingType.register("in_sine", Easing::inSine);
    public static final EasingType IN_OUT_BACK = EasingType.register("in_out_back", Easing::inOutBack);
    public static final EasingType IN_OUT_BOUNCE = EasingType.register("in_out_bounce", Easing::inOutBounce);
    public static final EasingType IN_OUT_CIRC = EasingType.register("in_out_circ", Easing::inOutCirc);
    public static final EasingType IN_OUT_CUBIC = EasingType.register("in_out_cubic", Easing::inOutCubic);
    public static final EasingType IN_OUT_ELASTIC = EasingType.register("in_out_elastic", Easing::inOutElastic);
    public static final EasingType IN_OUT_EXPO = EasingType.register("in_out_expo", Easing::inOutExpo);
    public static final EasingType IN_OUT_QUAD = EasingType.register("in_out_quad", Easing::inOutQuad);
    public static final EasingType IN_OUT_QUART = EasingType.register("in_out_quart", Easing::inOutQuart);
    public static final EasingType IN_OUT_QUINT = EasingType.register("in_out_quint", Easing::inOutQuint);
    public static final EasingType IN_OUT_SINE = EasingType.register("in_out_sine", Easing::inOutSine);
    public static final EasingType OUT_BACK = EasingType.register("out_back", Easing::outBack);
    public static final EasingType OUT_BOUNCE = EasingType.register("out_bounce", Easing::outBounce);
    public static final EasingType OUT_CIRC = EasingType.register("out_circ", Easing::outCirc);
    public static final EasingType OUT_CUBIC = EasingType.register("out_cubic", Easing::outCubic);
    public static final EasingType OUT_ELASTIC = EasingType.register("out_elastic", Easing::outElastic);
    public static final EasingType OUT_EXPO = EasingType.register("out_expo", Easing::outExpo);
    public static final EasingType OUT_QUAD = EasingType.register("out_quad", Easing::outQuad);
    public static final EasingType OUT_QUART = EasingType.register("out_quart", Easing::outQuart);
    public static final EasingType OUT_QUINT = EasingType.register("out_quint", Easing::outQuint);
    public static final EasingType OUT_SINE = EasingType.register("out_sine", Easing::outSine);

    public static EasingType register(String name, EasingType easingType) {
        EASING_TYPES_BY_NAME.put(name, easingType);
        return easingType;
    }

    public static EasingType cubicBezier(float x1, float y1, float x2, float y2) {
        return new CubicBezier(new CubicBezierControlPoints(x1, y1, x2, y2));
    }

    public static EasingType cubicBezierSymmetric(float x1, float y1) {
        return EasingType.cubicBezier(x1, y1, 1.0f - x1, 1.0f - y1);
    }

    public float apply(float var1);

    public static final class CubicBezier
    implements EasingType {
        public static final Codec<CubicBezier> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CubicBezierControlPoints.CODEC.fieldOf("cubic_bezier").forGetter(easing -> easing.controlPoints)).apply((Applicative)instance, CubicBezier::new));
        private static final int MAX_NEWTON_ITERATIONS = 4;
        private final CubicBezierControlPoints controlPoints;
        private final Parameters xParams;
        private final Parameters yParams;

        public CubicBezier(CubicBezierControlPoints controlPoints) {
            this.controlPoints = controlPoints;
            this.xParams = CubicBezier.computeParameters(controlPoints.x1, controlPoints.x2);
            this.yParams = CubicBezier.computeParameters(controlPoints.y1, controlPoints.y2);
        }

        private static Parameters computeParameters(float z1, float z2) {
            return new Parameters(3.0f * z1 - 3.0f * z2 + 1.0f, -6.0f * z1 + 3.0f * z2, 3.0f * z1);
        }

        @Override
        public float apply(float f) {
            float h;
            float g = f;
            for (int i = 0; i < 4 && !((h = this.xParams.derivative(g)) < 1.0E-5f); ++i) {
                float j = this.xParams.apply(g) - f;
                g -= j / h;
            }
            return this.yParams.apply(g);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object other) {
            if (!(other instanceof CubicBezier)) return false;
            CubicBezier cubicBezier = (CubicBezier)other;
            if (!this.controlPoints.equals(cubicBezier.controlPoints)) return false;
            return true;
        }

        public int hashCode() {
            return this.controlPoints.hashCode();
        }

        public String toString() {
            return "CubicBezier(" + this.controlPoints.x1 + ", " + this.controlPoints.y1 + ", " + this.controlPoints.x2 + ", " + this.controlPoints.y2 + ")";
        }

        record Parameters(float a, float b, float c) {
            public float apply(float t) {
                return ((this.a * t + this.b) * t + this.c) * t;
            }

            public float derivative(float t) {
                return (3.0f * this.a * t + 2.0f * this.b) * t + this.c;
            }
        }
    }

    public static final class CubicBezierControlPoints
    extends Record {
        final float x1;
        final float y1;
        final float x2;
        final float y2;
        public static final Codec<CubicBezierControlPoints> CODEC = Codec.FLOAT.listOf(4, 4).xmap(points -> new CubicBezierControlPoints(((Float)points.get(0)).floatValue(), ((Float)points.get(1)).floatValue(), ((Float)points.get(2)).floatValue(), ((Float)points.get(3)).floatValue()), points -> List.of(Float.valueOf(points.x1), Float.valueOf(points.y1), Float.valueOf(points.x2), Float.valueOf(points.y2))).validate(CubicBezierControlPoints::validate);

        public CubicBezierControlPoints(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        private DataResult<CubicBezierControlPoints> validate() {
            if (this.x1 < 0.0f || this.x1 > 1.0f) {
                return DataResult.error(() -> "x1 must be in range [0; 1]");
            }
            if (this.x2 < 0.0f || this.x2 > 1.0f) {
                return DataResult.error(() -> "x2 must be in range [0; 1]");
            }
            return DataResult.success((Object)this);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CubicBezierControlPoints.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CubicBezierControlPoints.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CubicBezierControlPoints.class, "x1;y1;x2;y2", "x1", "y1", "x2", "y2"}, this, object);
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float x2() {
            return this.x2;
        }

        public float y2() {
            return this.y2;
        }
    }
}

