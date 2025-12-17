/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record LightPredicate(NumberRange.IntRange range) {
    public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NumberRange.IntRange.CODEC.optionalFieldOf("light", (Object)NumberRange.IntRange.ANY).forGetter(LightPredicate::range)).apply((Applicative)instance, LightPredicate::new));

    public boolean test(ServerWorld world, BlockPos pos) {
        if (!world.isPosLoaded(pos)) {
            return false;
        }
        return this.range.test(world.getLightLevel(pos));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{LightPredicate.class, "composite", "range"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LightPredicate.class, "composite", "range"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LightPredicate.class, "composite", "range"}, this, object);
    }

    public static class Builder {
        private NumberRange.IntRange light = NumberRange.IntRange.ANY;

        public static Builder create() {
            return new Builder();
        }

        public Builder light(NumberRange.IntRange light) {
            this.light = light;
            return this;
        }

        public LightPredicate build() {
            return new LightPredicate(this.light);
        }
    }
}

