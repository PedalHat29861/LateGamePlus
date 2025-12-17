/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.enchantment;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.MathHelper;

public interface EnchantmentLevelBasedValue {
    public static final Codec<EnchantmentLevelBasedValue> BASE_CODEC = Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.getCodec().dispatch(EnchantmentLevelBasedValue::getCodec, codec -> codec);
    public static final Codec<EnchantmentLevelBasedValue> CODEC = Codec.either(Constant.CODEC, BASE_CODEC).xmap(either -> (EnchantmentLevelBasedValue)either.map(type -> type, type -> type), type -> {
        Either either;
        if (type instanceof Constant) {
            Constant constant = (Constant)type;
            either = Either.left((Object)constant);
        } else {
            either = Either.right((Object)type);
        }
        return either;
    });

    public static MapCodec<? extends EnchantmentLevelBasedValue> registerAndGetDefault(Registry<MapCodec<? extends EnchantmentLevelBasedValue>> registry) {
        Registry.register(registry, "clamped", Clamped.CODEC);
        Registry.register(registry, "fraction", Fraction.CODEC);
        Registry.register(registry, "levels_squared", LevelsSquared.CODEC);
        Registry.register(registry, "linear", Linear.CODEC);
        Registry.register(registry, "exponent", Exponent.CODEC);
        return Registry.register(registry, "lookup", Lookup.CODEC);
    }

    public static Constant constant(float value) {
        return new Constant(value);
    }

    public static Linear linear(float base, float perLevelAboveFirst) {
        return new Linear(base, perLevelAboveFirst);
    }

    public static Linear linear(float base) {
        return EnchantmentLevelBasedValue.linear(base, base);
    }

    public static Lookup lookup(List<Float> values, EnchantmentLevelBasedValue fallback) {
        return new Lookup(values, fallback);
    }

    public float getValue(int var1);

    public MapCodec<? extends EnchantmentLevelBasedValue> getCodec();

    public record Clamped(EnchantmentLevelBasedValue value, float min, float max) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Clamped> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("value").forGetter(Clamped::value), (App)Codec.FLOAT.fieldOf("min").forGetter(Clamped::min), (App)Codec.FLOAT.fieldOf("max").forGetter(Clamped::max)).apply((Applicative)instance, Clamped::new)).validate(type -> {
            if (type.max <= type.min) {
                return DataResult.error(() -> "Max must be larger than min, min: " + clamped.min + ", max: " + clamped.max);
            }
            return DataResult.success((Object)type);
        });

        @Override
        public float getValue(int level) {
            return MathHelper.clamp(this.value.getValue(level), this.min, this.max);
        }

        public MapCodec<Clamped> getCodec() {
            return CODEC;
        }
    }

    public record Fraction(EnchantmentLevelBasedValue numerator, EnchantmentLevelBasedValue denominator) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Fraction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("numerator").forGetter(Fraction::numerator), (App)CODEC.fieldOf("denominator").forGetter(Fraction::denominator)).apply((Applicative)instance, Fraction::new));

        @Override
        public float getValue(int level) {
            float f = this.denominator.getValue(level);
            if (f == 0.0f) {
                return 0.0f;
            }
            return this.numerator.getValue(level) / f;
        }

        public MapCodec<Fraction> getCodec() {
            return CODEC;
        }
    }

    public record LevelsSquared(float added) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<LevelsSquared> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("added").forGetter(LevelsSquared::added)).apply((Applicative)instance, LevelsSquared::new));

        @Override
        public float getValue(int level) {
            return (float)MathHelper.square(level) + this.added;
        }

        public MapCodec<LevelsSquared> getCodec() {
            return CODEC;
        }
    }

    public record Linear(float base, float perLevelAboveFirst) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("base").forGetter(Linear::base), (App)Codec.FLOAT.fieldOf("per_level_above_first").forGetter(Linear::perLevelAboveFirst)).apply((Applicative)instance, Linear::new));

        @Override
        public float getValue(int level) {
            return this.base + this.perLevelAboveFirst * (float)(level - 1);
        }

        public MapCodec<Linear> getCodec() {
            return CODEC;
        }
    }

    public record Exponent(EnchantmentLevelBasedValue base, EnchantmentLevelBasedValue power) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Exponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)CODEC.fieldOf("base").forGetter(Exponent::base), (App)CODEC.fieldOf("power").forGetter(Exponent::power)).apply((Applicative)instance, Exponent::new));

        @Override
        public float getValue(int level) {
            return (float)Math.pow(this.base.getValue(level), this.power.getValue(level));
        }

        public MapCodec<Exponent> getCodec() {
            return CODEC;
        }
    }

    public record Lookup(List<Float> values, EnchantmentLevelBasedValue fallback) implements EnchantmentLevelBasedValue
    {
        public static final MapCodec<Lookup> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.listOf().fieldOf("values").forGetter(Lookup::values), (App)CODEC.fieldOf("fallback").forGetter(Lookup::fallback)).apply((Applicative)instance, Lookup::new));

        @Override
        public float getValue(int level) {
            return level <= this.values.size() ? this.values.get(level - 1).floatValue() : this.fallback.getValue(level);
        }

        public MapCodec<Lookup> getCodec() {
            return CODEC;
        }
    }

    public record Constant(float value) implements EnchantmentLevelBasedValue
    {
        public static final Codec<Constant> CODEC = Codec.FLOAT.xmap(Constant::new, Constant::value);
        public static final MapCodec<Constant> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("value").forGetter(Constant::value)).apply((Applicative)instance, Constant::new));

        @Override
        public float getValue(int level) {
            return this.value;
        }

        public MapCodec<Constant> getCodec() {
            return TYPE_CODEC;
        }
    }
}

