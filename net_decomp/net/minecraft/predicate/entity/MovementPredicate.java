/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.math.MathHelper;

public record MovementPredicate(NumberRange.DoubleRange x, NumberRange.DoubleRange y, NumberRange.DoubleRange z, NumberRange.DoubleRange speed, NumberRange.DoubleRange horizontalSpeed, NumberRange.DoubleRange verticalSpeed, NumberRange.DoubleRange fallDistance) {
    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NumberRange.DoubleRange.CODEC.optionalFieldOf("x", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::x), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("y", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::y), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("z", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::z), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("speed", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::speed), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("horizontal_speed", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::horizontalSpeed), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("vertical_speed", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::verticalSpeed), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("fall_distance", (Object)NumberRange.DoubleRange.ANY).forGetter(MovementPredicate::fallDistance)).apply((Applicative)instance, MovementPredicate::new));

    public static MovementPredicate speed(NumberRange.DoubleRange speed) {
        return new MovementPredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, speed, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY);
    }

    public static MovementPredicate horizontalSpeed(NumberRange.DoubleRange horizontalSpeed) {
        return new MovementPredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, horizontalSpeed, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY);
    }

    public static MovementPredicate verticalSpeed(NumberRange.DoubleRange verticalSpeed) {
        return new MovementPredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, verticalSpeed, NumberRange.DoubleRange.ANY);
    }

    public static MovementPredicate fallDistance(NumberRange.DoubleRange fallDistance) {
        return new MovementPredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, fallDistance);
    }

    public boolean test(double x, double y, double z, double fallDistance) {
        if (!(this.x.test(x) && this.y.test(y) && this.z.test(z))) {
            return false;
        }
        double d = MathHelper.squaredMagnitude(x, y, z);
        if (!this.speed.testSqrt(d)) {
            return false;
        }
        double e = MathHelper.squaredHypot(x, z);
        if (!this.horizontalSpeed.testSqrt(e)) {
            return false;
        }
        double f = Math.abs(y);
        if (!this.verticalSpeed.test(f)) {
            return false;
        }
        return this.fallDistance.test(fallDistance);
    }
}

