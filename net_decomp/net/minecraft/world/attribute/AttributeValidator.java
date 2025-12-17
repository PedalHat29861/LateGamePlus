/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.attribute;

import com.mojang.serialization.DataResult;
import net.minecraft.util.math.MathHelper;

public interface AttributeValidator<Value> {
    public static final AttributeValidator<Float> PROBABILITY = AttributeValidator.ranged(0.0f, 1.0f);
    public static final AttributeValidator<Float> NON_NEGATIVE_FLOAT = AttributeValidator.ranged(0.0f, Float.POSITIVE_INFINITY);

    public static <Value> AttributeValidator<Value> all() {
        return new AttributeValidator<Value>(){

            @Override
            public DataResult<Value> validate(Value value) {
                return DataResult.success(value);
            }

            @Override
            public Value clamp(Value value) {
                return value;
            }
        };
    }

    public static AttributeValidator<Float> ranged(final float min, final float max) {
        return new AttributeValidator<Float>(){

            @Override
            public DataResult<Float> validate(Float float_) {
                if (float_.floatValue() >= min && float_.floatValue() <= max) {
                    return DataResult.success((Object)float_);
                }
                return DataResult.error(() -> float_ + " is not in range [" + min + "; " + max + "]");
            }

            @Override
            public Float clamp(Float float_) {
                if (float_.floatValue() >= min && float_.floatValue() <= max) {
                    return float_;
                }
                return Float.valueOf(MathHelper.clamp(float_.floatValue(), min, max));
            }
        };
    }

    public DataResult<Value> validate(Value var1);

    public Value clamp(Value var1);
}

