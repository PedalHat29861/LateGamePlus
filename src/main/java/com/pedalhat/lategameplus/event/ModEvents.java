package com.pedalhat.lategameplus.event;

import com.pedalhat.lategameplus.config.ModConfig;
import com.pedalhat.lategameplus.registry.ModItems;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class ModEvents {

    public static void register(ModConfig cfg) {
        LootTableEvents.MODIFY.register((ResourceKey<LootTable> key,
                                         LootTable.Builder table,
                                         LootTableSource source,
                                         HolderLookup.Provider registries) -> {
            if (!source.isBuiltin()) return;

            var bruteKey = EntityType.PIGLIN_BRUTE.getDefaultLootTable().orElse(null);
            if (bruteKey != null && key.equals(bruteKey)) {
                float chance = Math.max(0f, Math.min(1f, cfg.piglinBruteDropChance));
                int min = Math.max(0, cfg.piglinBruteNuggetMin);
                int max = Math.max(min, cfg.piglinBruteNuggetMax);

                table.withPool(
                    LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(chance))
                        .add(LootItem.lootTableItem(ModItems.NETHERITE_NUGGET))
                        .apply(SetItemCountFunction.setCount(
                            UniformGenerator.between((float) min, (float) max)
                        ))
                );
            }
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((LivingEntity living, DamageSource source, float amount) -> {
            if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;

            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = living.getItemInHand(hand);
                if (stack.is(ModItems.TOTEM_OF_NETHERDYING)) {
                    if (living instanceof ServerPlayer player) {
                        player.awardStat(Stats.ITEM_USED.get(ModItems.TOTEM_OF_NETHERDYING));
                        CriteriaTriggers.USED_TOTEM.trigger(player, stack);
                    }

                    stack.setDamageValue(stack.getDamageValue() + 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1);

                    living.setHealth(1.0F);
                    living.removeAllEffects();
                    living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 2));
                    living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 150, 1));
                    living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                    living.level().broadcastEntityEvent(living, (byte) 35);

                    return false;
                }
            }
            return true;
        });
    }
}
