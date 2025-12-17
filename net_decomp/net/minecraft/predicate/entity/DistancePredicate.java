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

public record DistancePredicate(NumberRange.DoubleRange x, NumberRange.DoubleRange y, NumberRange.DoubleRange z, NumberRange.DoubleRange horizontal, NumberRange.DoubleRange absolute) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NumberRange.DoubleRange.CODEC.optionalFieldOf("x", (Object)NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::x), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("y", (Object)NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::y), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("z", (Object)NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::z), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("horizontal", (Object)NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::horizontal), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("absolute", (Object)NumberRange.DoubleRange.ANY).forGetter(DistancePredicate::absolute)).apply((Applicative)instance, DistancePredicate::new));

    public static DistancePredicate horizontal(NumberRange.DoubleRange horizontal) {
        return new DistancePredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, horizontal, NumberRange.DoubleRange.ANY);
    }

    public static DistancePredicate y(NumberRange.DoubleRange y) {
        return new DistancePredicate(NumberRange.DoubleRange.ANY, y, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY);
    }

    public static DistancePredicate absolute(NumberRange.DoubleRange absolute) {
        return new DistancePredicate(NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, NumberRange.DoubleRange.ANY, absolute);
    }

    public boolean test(double x0, double y0, double z0, double x1, double y1, double z1) {
        float f = (float)(x0 - x1);
        float g = (float)(y0 - y1);
        float h = (float)(z0 - z1);
        if (!(this.x.test(MathHelper.abs(f)) && this.y.test(MathHelper.abs(g)) && this.z.test(MathHelper.abs(h)))) {
            return false;
        }
        if (!this.horizontal.testSqrt(f * f + h * h)) {
            return false;
        }
        return this.absolute.testSqrt(f * f + g * g + h * h);
    }
}

