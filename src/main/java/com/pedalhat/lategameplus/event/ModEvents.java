package com.pedalhat.lategameplus.event;

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
    public static void register() {
        LootTableEvents.MODIFY.register((RegistryKey<LootTable> key,
                                         LootTable.Builder table,
                                         LootTableSource source,
                                         RegistryWrapper.WrapperLookup registries) -> {
            if (!source.isBuiltin()) return;

            var bruteKey = EntityType.PIGLIN_BRUTE.getLootTableKey().orElse(null);
            if (bruteKey != null && key.equals(bruteKey)) {
                table.pool(
                    LootPool.builder()
                        .conditionally(RandomChanceLootCondition.builder(0.5f))
                        .with(ItemEntry.builder(ModItems.NETHERITE_NUGGET))
                        .apply(SetCountLootFunction.builder(
                            UniformLootNumberProvider.create(0f, 2.0f) // 0–2 nuggets
                        ))
                );
            }
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((LivingEntity livingEntity, DamageSource damageSource, float damageAmount) -> {
            for (Hand hand : Hand.values()) {
                ItemStack itemStack = livingEntity.getStackInHand(hand);
                if (itemStack.isOf(ModItems.TOTEM_OF_NETHERDYING)) {
                    if (damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;

                    if (livingEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                        serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(ModItems.TOTEM_OF_NETHERDYING));
                        Criteria.USED_TOTEM.trigger(serverPlayerEntity, itemStack);
                    }

                    itemStack.setDamage(itemStack.getDamage() + 1);
                    if (itemStack.getDamage() >= itemStack.getMaxDamage()) itemStack.decrement(1);

                    livingEntity.setHealth(1.0F);
                    livingEntity.clearStatusEffects();
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                    livingEntity.getWorld().sendEntityStatus(livingEntity, (byte) 35);
                    return false;
                }
            }
            return true;
        });
    }
}
