/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;

public record WeatherCheckLootCondition(Optional<Boolean> raining, Optional<Boolean> thundering) implements LootCondition
{
    public static final MapCodec<WeatherCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherCheckLootCondition::raining), (App)Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherCheckLootCondition::thundering)).apply((Applicative)instance, WeatherCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.WEATHER_CHECK;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerWorld serverWorld = lootContext.getWorld();
        if (this.raining.isPresent() && this.raining.get().booleanValue() != serverWorld.isRaining()) {
            return false;
        }
        return !this.thundering.isPresent() || this.thundering.get().booleanValue() == serverWorld.isThundering();
    }

    public static Builder create() {
        return new Builder();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{WeatherCheckLootCondition.class, "isRaining;isThundering", "raining", "thundering"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{WeatherCheckLootCondition.class, "isRaining;isThundering", "raining", "thundering"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{WeatherCheckLootCondition.class, "isRaining;isThundering", "raining", "thundering"}, this, object);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private Optional<Boolean> raining = Optional.empty();
        private Optional<Boolean> thundering = Optional.empty();

        public Builder raining(boolean raining) {
            this.raining = Optional.of(raining);
            return this;
        }

        public Builder thundering(boolean thundering) {
            this.thundering = Optional.of(thundering);
            return this;
        }

        @Override
        public WeatherCheckLootCondition build() {
            return new WeatherCheckLootCondition(this.raining, this.thundering);
        }

        @Override
        public /* synthetic */ LootCondition build() {
            return this.build();
        }
    }
}

