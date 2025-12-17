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
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnchantedItemCriterion
extends AbstractCriterion<Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, ItemStack stack, int levels) {
        this.trigger(player, conditions -> conditions.matches(stack, levels));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<ItemPredicate> item, NumberRange.IntRange levels) implements AbstractCriterion.Conditions
    {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Conditions::item), (App)NumberRange.IntRange.CODEC.optionalFieldOf("levels", (Object)NumberRange.IntRange.ANY).forGetter(Conditions::levels)).apply((Applicative)instance, Conditions::new));

        public static AdvancementCriterion<Conditions> any() {
            return Criteria.ENCHANTED_ITEM.create(new Conditions(Optional.empty(), Optional.empty(), NumberRange.IntRange.ANY));
        }

        public boolean matches(ItemStack stack, int levels) {
            if (this.item.isPresent() && !this.item.get().test(stack)) {
                return false;
            }
            return this.levels.test(levels);
        }
    }
}

