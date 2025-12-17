/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.entry.RegistryEntry;
import org.jspecify.annotations.Nullable;

public record EntityEffectPredicate(Map<RegistryEntry<StatusEffect>, EffectData> effects) {
    public static final Codec<EntityEffectPredicate> CODEC = Codec.unboundedMap(StatusEffect.ENTRY_CODEC, EffectData.CODEC).xmap(EntityEffectPredicate::new, EntityEffectPredicate::effects);

    public boolean test(Entity entity) {
        LivingEntity livingEntity;
        return entity instanceof LivingEntity && this.test((livingEntity = (LivingEntity)entity).getActiveStatusEffects());
    }

    public boolean test(LivingEntity livingEntity) {
        return this.test(livingEntity.getActiveStatusEffects());
    }

    public boolean test(Map<RegistryEntry<StatusEffect>, StatusEffectInstance> effects) {
        for (Map.Entry<RegistryEntry<StatusEffect>, EffectData> entry : this.effects.entrySet()) {
            StatusEffectInstance statusEffectInstance = effects.get(entry.getKey());
            if (entry.getValue().test(statusEffectInstance)) continue;
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityEffectPredicate.class, "effectMap", "effects"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityEffectPredicate.class, "effectMap", "effects"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityEffectPredicate.class, "effectMap", "effects"}, this, object);
    }

    public record EffectData(NumberRange.IntRange amplifier, NumberRange.IntRange duration, Optional<Boolean> ambient, Optional<Boolean> visible) {
        public static final Codec<EffectData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NumberRange.IntRange.CODEC.optionalFieldOf("amplifier", (Object)NumberRange.IntRange.ANY).forGetter(EffectData::amplifier), (App)NumberRange.IntRange.CODEC.optionalFieldOf("duration", (Object)NumberRange.IntRange.ANY).forGetter(EffectData::duration), (App)Codec.BOOL.optionalFieldOf("ambient").forGetter(EffectData::ambient), (App)Codec.BOOL.optionalFieldOf("visible").forGetter(EffectData::visible)).apply((Applicative)instance, EffectData::new));

        public EffectData() {
            this(NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, Optional.empty(), Optional.empty());
        }

        public boolean test(@Nullable StatusEffectInstance statusEffectInstance) {
            if (statusEffectInstance == null) {
                return false;
            }
            if (!this.amplifier.test(statusEffectInstance.getAmplifier())) {
                return false;
            }
            if (!this.duration.test(statusEffectInstance.getDuration())) {
                return false;
            }
            if (this.ambient.isPresent() && this.ambient.get().booleanValue() != statusEffectInstance.isAmbient()) {
                return false;
            }
            return !this.visible.isPresent() || this.visible.get().booleanValue() == statusEffectInstance.shouldShowParticles();
        }
    }

    public static class Builder {
        private final ImmutableMap.Builder<RegistryEntry<StatusEffect>, EffectData> effects = ImmutableMap.builder();

        public static Builder create() {
            return new Builder();
        }

        public Builder addEffect(RegistryEntry<StatusEffect> effect) {
            this.effects.put(effect, (Object)new EffectData());
            return this;
        }

        public Builder addEffect(RegistryEntry<StatusEffect> effect, EffectData effectData) {
            this.effects.put(effect, (Object)effectData);
            return this;
        }

        public Optional<EntityEffectPredicate> build() {
            return Optional.of(new EntityEffectPredicate((Map<RegistryEntry<StatusEffect>, EffectData>)this.effects.build()));
        }
    }
}

