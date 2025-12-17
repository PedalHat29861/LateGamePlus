/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancement.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class UsedEnderEyeCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockPos strongholdPos) {
        double d = player.getX() - (double)strongholdPos.getX();
        double e = player.getZ() - (double)strongholdPos.getZ();
        double f = d * d + e * e;
        this.trigger(player, (T conditions) -> conditions.matches(f));
    }

    public record Conditions(Optional<LootContextPredicate> player, NumberRange.DoubleRange distance) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), (App)NumberRange.DoubleRange.CODEC.optionalFieldOf("distance", (Object)NumberRange.DoubleRange.ANY).forGetter(Conditions::distance)).apply((Applicative)instance, Conditions::new));

        public boolean matches(double distance) {
            return this.distance.testSqrt(distance);
        }
    }
}

