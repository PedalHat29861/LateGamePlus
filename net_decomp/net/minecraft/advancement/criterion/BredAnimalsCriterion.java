/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
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
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jspecify.annotations.Nullable;

public class BredAnimalsCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, AnimalEntity parent, AnimalEntity partner, @Nullable PassiveEntity child) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, parent);
        LootContext lootContext2 = EntityPredicate.createAdvancementEntityLootContext(player, partner);
        LootContext lootContext3 = child != null ? EntityPredicate.createAdvancementEntityLootContext(player, child) : null;
        this.trigger(player, conditions -> conditions.matches(lootContext, lootContext2, lootContext3));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<LootContextPredicate> parent, Optional<LootContextPredicate> partner, Optional<LootContextPredicate> child) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((T instance) -> instance.group((App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), (App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("parent").forGetter(Conditions::parent), (App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("partner").forGetter(Conditions::partner), (App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("child").forGetter(Conditions::child)).apply((Applicative)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> any() {
            return Criteria.BRED_ANIMALS.create(new Conditions(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static AdvancementCriterion<Conditions> create(EntityPredicate.Builder child) {
            return Criteria.BRED_ANIMALS.create(new Conditions(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.contextPredicateFromEntityPredicate(child))));
        }

        public static AdvancementCriterion<Conditions> create(Optional<EntityPredicate> parent, Optional<EntityPredicate> partner, Optional<EntityPredicate> child) {
            return Criteria.BRED_ANIMALS.create(new Conditions(Optional.empty(), EntityPredicate.contextPredicateFromEntityPredicate(parent), EntityPredicate.contextPredicateFromEntityPredicate(partner), EntityPredicate.contextPredicateFromEntityPredicate(child)));
        }

        public boolean matches(LootContext parentContext, LootContext partnerContext, @Nullable LootContext childContext) {
            if (this.child.isPresent() && (childContext == null || !this.child.get().test(childContext))) {
                return false;
            }
            return Conditions.parentMatches(this.parent, parentContext) && Conditions.parentMatches(this.partner, partnerContext) || Conditions.parentMatches(this.parent, partnerContext) && Conditions.parentMatches(this.partner, parentContext);
        }

        private static boolean parentMatches(Optional<LootContextPredicate> parent, LootContext parentContext) {
            return parent.isEmpty() || parent.get().test(parentContext);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.parent, "parent");
            validator.validateEntityPredicate(this.partner, "partner");
            validator.validateEntityPredicate(this.child, "child");
        }
    }
}

