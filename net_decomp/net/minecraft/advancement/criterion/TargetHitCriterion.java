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
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class TargetHitCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Entity projectile, Vec3d hitPos, int signalStrength) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, projectile);
        this.trigger(player, conditions -> conditions.test(lootContext, hitPos, signalStrength));
    }

    public record Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange signalStrength, Optional<LootContextPredicate> projectile) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), (App)NumberRange.IntRange.CODEC.optionalFieldOf("signal_strength", (Object)NumberRange.IntRange.ANY).forGetter(Conditions::signalStrength), (App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("projectile").forGetter(Conditions::projectile)).apply((Applicative)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(NumberRange.IntRange signalStrength, Optional<LootContextPredicate> projectile) {
            return Criteria.TARGET_HIT.create(new Conditions(Optional.empty(), signalStrength, projectile));
        }

        public boolean test(LootContext projectile, Vec3d hitPos, int signalStrength) {
            if (!this.signalStrength.test(signalStrength)) {
                return false;
            }
            return !this.projectile.isPresent() || this.projectile.get().test(projectile);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.projectile, "projectile");
        }
    }
}

