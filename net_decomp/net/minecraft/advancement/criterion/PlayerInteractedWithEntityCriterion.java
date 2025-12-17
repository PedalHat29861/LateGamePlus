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
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerInteractedWithEntityCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, ItemStack stack, Entity entity) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, entity);
        this.trigger(player, conditions -> conditions.test(stack, lootContext));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item, Optional<LootContextPredicate> entity) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item), (App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity)).apply((Applicative)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> create(Optional<LootContextPredicate> playerPredicate, ItemPredicate.Builder item, Optional<LootContextPredicate> entity) {
            return Criteria.PLAYER_INTERACTED_WITH_ENTITY.create(new Conditions(playerPredicate, Optional.of(item.build()), entity));
        }

        public static AdvancementCriterion<Conditions> createPlayerShearedEquipment(Optional<LootContextPredicate> playerPredicate, ItemPredicate.Builder item, Optional<LootContextPredicate> entity) {
            return Criteria.PLAYER_SHEARED_EQUIPMENT.create(new Conditions(playerPredicate, Optional.of(item.build()), entity));
        }

        public static AdvancementCriterion<Conditions> createPlayerShearedEquipment(ItemPredicate.Builder item, Optional<LootContextPredicate> entity) {
            return Criteria.PLAYER_SHEARED_EQUIPMENT.create(new Conditions(Optional.empty(), Optional.of(item.build()), entity));
        }

        public static AdvancementCriterion<Conditions> create(ItemPredicate.Builder item, Optional<LootContextPredicate> entity) {
            return Conditions.create(Optional.empty(), item, entity);
        }

        public boolean test(ItemStack stack, LootContext entity) {
            if (this.item.isPresent() && !this.item.get().test(stack)) {
                return false;
            }
            return this.entity.isEmpty() || this.entity.get().test(entity);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.entity, "entity");
        }
    }
}

