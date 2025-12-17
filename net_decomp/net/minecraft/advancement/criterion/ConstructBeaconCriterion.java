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
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class ConstructBeaconCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int level) {
        this.trigger(player, conditions -> conditions.matches(level));
    }

    public record Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange level) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), (App)NumberRange.IntRange.CODEC.optionalFieldOf("level", (Object)NumberRange.IntRange.ANY).forGetter(Conditions::level)).apply((Applicative)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create() {
            return Criteria.CONSTRUCT_BEACON.create(new Conditions(Optional.empty(), NumberRange.IntRange.ANY));
        }

        public static AdvancementCriterion<Conditions> level(NumberRange.IntRange level) {
            return Criteria.CONSTRUCT_BEACON.create(new Conditions(Optional.empty(), level));
        }

        public boolean matches(int level) {
            return this.level.test(level);
        }
    }
}

