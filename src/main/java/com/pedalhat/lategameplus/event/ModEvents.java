package com.pedalhat.lategameplus.event;

import com.pedalhat.lategameplus.config.ModConfig;
import com.pedalhat.lategameplus.registry.ModItems;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public final class ModEvents {

    public static void register(ModConfig cfg) {
        LootTableEvents.MODIFY.register((RegistryKey<LootTable> key,
                                         LootTable.Builder table,
                                         LootTableSource source,
                                         RegistryWrapper.WrapperLookup registries) -> {
            if (!source.isBuiltin()) return;

            var bruteKey = EntityType.PIGLIN_BRUTE.getLootTableKey().orElse(null);
            if (bruteKey != null && key.equals(bruteKey)) {
                float chance = Math.max(0f, Math.min(1f, cfg.piglinBruteDropChance));
                int min = Math.max(0, cfg.piglinBruteNuggetMin);
                int max = Math.max(min, cfg.piglinBruteNuggetMax);

                table.pool(
                    LootPool.builder()
                        .conditionally(RandomChanceLootCondition.builder(chance))
                        .with(ItemEntry.builder(ModItems.NETHERITE_NUGGET))
                        .apply(SetCountLootFunction.builder(
                            UniformLootNumberProvider.create((float) min, (float) max)
                        ))
                );
            }
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((LivingEntity living, DamageSource source, float amount) -> {
            if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;

            for (Hand hand : Hand.values()) {
                ItemStack stack = living.getStackInHand(hand);
                if (stack.isOf(ModItems.TOTEM_OF_NETHERDYING)) {
                    if (living instanceof ServerPlayerEntity player) {
                        player.incrementStat(Stats.USED.getOrCreateStat(ModItems.TOTEM_OF_NETHERDYING));
                        Criteria.USED_TOTEM.trigger(player, stack);
                    }

                    stack.setDamage(stack.getDamage() + 1);
                    if (stack.getDamage() >= stack.getMaxDamage()) stack.decrement(1);

                    living.setHealth(1.0F);
                    living.clearStatusEffects();
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 2));
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 150, 1));
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                    living.getEntityWorld().sendEntityStatus(living, (byte) 35);

                    return false;
                }
            }
            return true;
        });
    }
}
