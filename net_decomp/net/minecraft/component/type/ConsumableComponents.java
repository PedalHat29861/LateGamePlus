/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.component.type;

import java.util.List;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import net.minecraft.item.consume.RemoveEffectsConsumeEffect;
import net.minecraft.item.consume.TeleportRandomlyConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundEvents;

public class ConsumableComponents {
    public static final ConsumableComponent FOOD = ConsumableComponents.food().build();
    public static final ConsumableComponent DRINK = ConsumableComponents.drink().build();
    public static final ConsumableComponent HONEY_BOTTLE = ConsumableComponents.drink().consumeSeconds(2.0f).sound(SoundEvents.ITEM_HONEY_BOTTLE_DRINK).consumeEffect(new RemoveEffectsConsumeEffect(StatusEffects.POISON)).build();
    public static final ConsumableComponent OMINOUS_BOTTLE = ConsumableComponents.drink().finishSound(SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE).build();
    public static final ConsumableComponent DRIED_KELP = ConsumableComponents.food().consumeSeconds(0.8f).build();
    public static final ConsumableComponent RAW_CHICKEN = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.3f)).build();
    public static final ConsumableComponent ENCHANTED_GOLDEN_APPLE = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1), new StatusEffectInstance(StatusEffects.RESISTANCE, 6000, 0), new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0), new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 3)))).build();
    public static final ConsumableComponent GOLDEN_APPLE = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1), new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 0)))).build();
    public static final ConsumableComponent POISONOUS_POTATO = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), 0.6f)).build();
    public static final ConsumableComponent PUFFERFISH = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.POISON, 1200, 1), new StatusEffectInstance(StatusEffects.HUNGER, 300, 2), new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0)))).build();
    public static final ConsumableComponent ROTTEN_FLESH = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.8f)).build();
    public static final ConsumableComponent SPIDER_EYE = ConsumableComponents.food().consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0))).build();
    public static final ConsumableComponent MILK_BUCKET = ConsumableComponents.drink().consumeEffect(ClearAllEffectsConsumeEffect.INSTANCE).build();
    public static final ConsumableComponent CHORUS_FRUIT = ConsumableComponents.food().consumeEffect(new TeleportRandomlyConsumeEffect()).build();

    public static ConsumableComponent.Builder food() {
        return ConsumableComponent.builder().consumeSeconds(1.6f).useAction(UseAction.EAT).sound(SoundEvents.ENTITY_GENERIC_EAT).consumeParticles(true);
    }

    public static ConsumableComponent.Builder drink() {
        return ConsumableComponent.builder().consumeSeconds(1.6f).useAction(UseAction.DRINK).sound(SoundEvents.ENTITY_GENERIC_DRINK).consumeParticles(false);
    }
}

