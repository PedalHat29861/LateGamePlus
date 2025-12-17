/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.consume.PlaySoundConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public record ConsumableComponent(float consumeSeconds, UseAction useAction, RegistryEntry<SoundEvent> sound, boolean hasConsumeParticles, List<ConsumeEffect> onConsumeEffects) {
    public static final float DEFAULT_CONSUME_SECONDS = 1.6f;
    private static final int PARTICLES_AND_SOUND_TICK_INTERVAL = 4;
    private static final float PARTICLES_AND_SOUND_TICK_THRESHOLD = 0.21875f;
    public static final Codec<ConsumableComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("consume_seconds", (Object)Float.valueOf(1.6f)).forGetter(ConsumableComponent::consumeSeconds), (App)UseAction.CODEC.optionalFieldOf("animation", (Object)UseAction.EAT).forGetter(ConsumableComponent::useAction), (App)SoundEvent.ENTRY_CODEC.optionalFieldOf("sound", SoundEvents.ENTITY_GENERIC_EAT).forGetter(ConsumableComponent::sound), (App)Codec.BOOL.optionalFieldOf("has_consume_particles", (Object)true).forGetter(ConsumableComponent::hasConsumeParticles), (App)ConsumeEffect.CODEC.listOf().optionalFieldOf("on_consume_effects", List.of()).forGetter(ConsumableComponent::onConsumeEffects)).apply((Applicative)instance, ConsumableComponent::new));
    public static final PacketCodec<RegistryByteBuf, ConsumableComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, ConsumableComponent::consumeSeconds, UseAction.PACKET_CODEC, ConsumableComponent::useAction, SoundEvent.ENTRY_PACKET_CODEC, ConsumableComponent::sound, PacketCodecs.BOOLEAN, ConsumableComponent::hasConsumeParticles, ConsumeEffect.PACKET_CODEC.collect(PacketCodecs.toList()), ConsumableComponent::onConsumeEffects, ConsumableComponent::new);

    public ActionResult consume(LivingEntity user, ItemStack stack, Hand hand) {
        boolean bl;
        if (!this.canConsume(user, stack)) {
            return ActionResult.FAIL;
        }
        boolean bl2 = bl = this.getConsumeTicks() > 0;
        if (bl) {
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }
        ItemStack itemStack = this.finishConsumption(user.getEntityWorld(), user, stack);
        return ActionResult.CONSUME.withNewHandStack(itemStack);
    }

    public ItemStack finishConsumption(World world, LivingEntity user, ItemStack stack) {
        Random random = user.getRandom();
        this.spawnParticlesAndPlaySound(random, user, stack, 16);
        if (user instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)user;
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }
        stack.streamAll(Consumable.class).forEach(consumable -> consumable.onConsume(world, user, stack, this));
        if (!world.isClient()) {
            this.onConsumeEffects.forEach(effect -> effect.onConsume(world, stack, user));
        }
        user.emitGameEvent(this.useAction == UseAction.DRINK ? GameEvent.DRINK : GameEvent.EAT);
        stack.decrementUnlessCreative(1, user);
        return stack;
    }

    public boolean canConsume(LivingEntity user, ItemStack stack) {
        FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
        if (foodComponent != null && user instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)user;
            return playerEntity.canConsume(foodComponent.canAlwaysEat());
        }
        return true;
    }

    public int getConsumeTicks() {
        return (int)(this.consumeSeconds * 20.0f);
    }

    public void spawnParticlesAndPlaySound(Random random, LivingEntity user, ItemStack stack, int particleCount) {
        SoundEvent soundEvent;
        float k;
        float f = random.nextBoolean() ? 0.5f : 1.0f;
        float g = random.nextTriangular(1.0f, 0.2f);
        float h = 0.5f;
        float i = MathHelper.nextBetween(random, 0.9f, 1.0f);
        float j = this.useAction == UseAction.DRINK ? 0.5f : f;
        float f2 = k = this.useAction == UseAction.DRINK ? i : g;
        if (this.hasConsumeParticles) {
            user.spawnItemParticles(stack, particleCount);
        }
        if (user instanceof ConsumableSoundProvider) {
            ConsumableSoundProvider consumableSoundProvider = (ConsumableSoundProvider)((Object)user);
            soundEvent = consumableSoundProvider.getConsumeSound(stack);
        } else {
            soundEvent = this.sound.value();
        }
        SoundEvent soundEvent2 = soundEvent;
        user.playSound(soundEvent2, j, k);
    }

    public boolean shouldSpawnParticlesAndPlaySounds(int remainingUseTicks) {
        int j;
        int i = this.getConsumeTicks() - remainingUseTicks;
        boolean bl = i > (j = (int)((float)this.getConsumeTicks() * 0.21875f));
        return bl && remainingUseTicks % 4 == 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ConsumableComponent.class, "consumeSeconds;animation;sound;hasConsumeParticles;onConsumeEffects", "consumeSeconds", "useAction", "sound", "hasConsumeParticles", "onConsumeEffects"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ConsumableComponent.class, "consumeSeconds;animation;sound;hasConsumeParticles;onConsumeEffects", "consumeSeconds", "useAction", "sound", "hasConsumeParticles", "onConsumeEffects"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ConsumableComponent.class, "consumeSeconds;animation;sound;hasConsumeParticles;onConsumeEffects", "consumeSeconds", "useAction", "sound", "hasConsumeParticles", "onConsumeEffects"}, this, object);
    }

    public static interface ConsumableSoundProvider {
        public SoundEvent getConsumeSound(ItemStack var1);
    }

    public static class Builder {
        private float consumeSeconds = 1.6f;
        private UseAction useAction = UseAction.EAT;
        private RegistryEntry<SoundEvent> sound = SoundEvents.ENTITY_GENERIC_EAT;
        private boolean consumeParticles = true;
        private final List<ConsumeEffect> consumeEffects = new ArrayList<ConsumeEffect>();

        Builder() {
        }

        public Builder consumeSeconds(float consumeSeconds) {
            this.consumeSeconds = consumeSeconds;
            return this;
        }

        public Builder useAction(UseAction useAction) {
            this.useAction = useAction;
            return this;
        }

        public Builder sound(RegistryEntry<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        public Builder finishSound(RegistryEntry<SoundEvent> finishSound) {
            return this.consumeEffect(new PlaySoundConsumeEffect(finishSound));
        }

        public Builder consumeParticles(boolean consumeParticles) {
            this.consumeParticles = consumeParticles;
            return this;
        }

        public Builder consumeEffect(ConsumeEffect consumeEffect) {
            this.consumeEffects.add(consumeEffect);
            return this;
        }

        public ConsumableComponent build() {
            return new ConsumableComponent(this.consumeSeconds, this.useAction, this.sound, this.consumeParticles, this.consumeEffects);
        }
    }
}

