/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ComparisonChain
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StatusEffectInstance
implements Comparable<StatusEffectInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INFINITE = -1;
    public static final int MIN_AMPLIFIER = 0;
    public static final int MAX_AMPLIFIER = 255;
    public static final Codec<StatusEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)StatusEffect.ENTRY_CODEC.fieldOf("id").forGetter(StatusEffectInstance::getEffectType), (App)Parameters.CODEC.forGetter(StatusEffectInstance::asParameters)).apply((Applicative)instance, StatusEffectInstance::new));
    public static final PacketCodec<RegistryByteBuf, StatusEffectInstance> PACKET_CODEC = PacketCodec.tuple(StatusEffect.ENTRY_PACKET_CODEC, StatusEffectInstance::getEffectType, Parameters.PACKET_CODEC, StatusEffectInstance::asParameters, StatusEffectInstance::new);
    private final RegistryEntry<StatusEffect> type;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean showParticles;
    private boolean showIcon;
    private @Nullable StatusEffectInstance hiddenEffect;
    private final Fading fading = new Fading();

    public StatusEffectInstance(RegistryEntry<StatusEffect> effect) {
        this(effect, 0, 0);
    }

    public StatusEffectInstance(RegistryEntry<StatusEffect> effect, int duration) {
        this(effect, duration, 0);
    }

    public StatusEffectInstance(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        this(effect, duration, amplifier, false, true);
    }

    public StatusEffectInstance(RegistryEntry<StatusEffect> effect, int duration, int amplifier, boolean ambient, boolean visible) {
        this(effect, duration, amplifier, ambient, visible, visible);
    }

    public StatusEffectInstance(RegistryEntry<StatusEffect> effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        this(effect, duration, amplifier, ambient, showParticles, showIcon, null);
    }

    public StatusEffectInstance(RegistryEntry<StatusEffect> effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, @Nullable StatusEffectInstance hiddenEffect) {
        this.type = effect;
        this.duration = duration;
        this.amplifier = MathHelper.clamp(amplifier, 0, 255);
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
        this.hiddenEffect = hiddenEffect;
    }

    public StatusEffectInstance(StatusEffectInstance instance) {
        this.type = instance.type;
        this.copyFrom(instance);
    }

    private StatusEffectInstance(RegistryEntry<StatusEffect> effect, Parameters parameters) {
        this(effect, parameters.duration(), parameters.amplifier(), parameters.ambient(), parameters.showParticles(), parameters.showIcon(), parameters.hiddenEffect().map(parametersx -> new StatusEffectInstance(effect, (Parameters)parametersx)).orElse(null));
    }

    private Parameters asParameters() {
        return new Parameters(this.getAmplifier(), this.getDuration(), this.isAmbient(), this.shouldShowParticles(), this.shouldShowIcon(), Optional.ofNullable(this.hiddenEffect).map(StatusEffectInstance::asParameters));
    }

    public float getFadeFactor(LivingEntity entity, float tickProgress) {
        return this.fading.calculate(entity, tickProgress);
    }

    public ParticleEffect createParticle() {
        return this.type.value().createParticle(this);
    }

    void copyFrom(StatusEffectInstance that) {
        this.duration = that.duration;
        this.amplifier = that.amplifier;
        this.ambient = that.ambient;
        this.showParticles = that.showParticles;
        this.showIcon = that.showIcon;
    }

    public boolean upgrade(StatusEffectInstance that) {
        if (!this.type.equals(that.type)) {
            LOGGER.warn("This method should only be called for matching effects!");
        }
        boolean bl = false;
        if (that.amplifier > this.amplifier) {
            if (that.lastsShorterThan(this)) {
                StatusEffectInstance statusEffectInstance = this.hiddenEffect;
                this.hiddenEffect = new StatusEffectInstance(this);
                this.hiddenEffect.hiddenEffect = statusEffectInstance;
            }
            this.amplifier = that.amplifier;
            this.duration = that.duration;
            bl = true;
        } else if (this.lastsShorterThan(that)) {
            if (that.amplifier == this.amplifier) {
                this.duration = that.duration;
                bl = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new StatusEffectInstance(that);
            } else {
                this.hiddenEffect.upgrade(that);
            }
        }
        if (!that.ambient && this.ambient || bl) {
            this.ambient = that.ambient;
            bl = true;
        }
        if (that.showParticles != this.showParticles) {
            this.showParticles = that.showParticles;
            bl = true;
        }
        if (that.showIcon != this.showIcon) {
            this.showIcon = that.showIcon;
            bl = true;
        }
        return bl;
    }

    private boolean lastsShorterThan(StatusEffectInstance effect) {
        return !this.isInfinite() && (this.duration < effect.duration || effect.isInfinite());
    }

    public boolean isInfinite() {
        return this.duration == -1;
    }

    public boolean isDurationBelow(int duration) {
        return !this.isInfinite() && this.duration <= duration;
    }

    public StatusEffectInstance withScaledDuration(float durationMultiplier) {
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(this);
        statusEffectInstance.duration = statusEffectInstance.mapDuration(duration -> Math.max(MathHelper.floor((float)duration * durationMultiplier), 1));
        return statusEffectInstance;
    }

    public int mapDuration(Int2IntFunction mapper) {
        if (this.isInfinite() || this.duration == 0) {
            return this.duration;
        }
        return mapper.applyAsInt(this.duration);
    }

    public RegistryEntry<StatusEffect> getEffectType() {
        return this.type;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean shouldShowParticles() {
        return this.showParticles;
    }

    public boolean shouldShowIcon() {
        return this.showIcon;
    }

    public boolean update(ServerWorld world, LivingEntity entity, Runnable hiddenEffectCallback) {
        int i;
        if (!this.isActive()) {
            return false;
        }
        int n = i = this.isInfinite() ? entity.age : this.duration;
        if (this.type.value().canApplyUpdateEffect(i, this.amplifier) && !this.type.value().applyUpdateEffect(world, entity, this.amplifier)) {
            return false;
        }
        this.updateDuration();
        if (this.tickHiddenEffect()) {
            hiddenEffectCallback.run();
        }
        return this.isActive();
    }

    public void tickClient() {
        if (this.isActive()) {
            this.updateDuration();
            this.tickHiddenEffect();
        }
        this.fading.update(this);
    }

    private boolean isActive() {
        return this.isInfinite() || this.duration > 0;
    }

    private void updateDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.updateDuration();
        }
        this.duration = this.mapDuration(duration -> duration - 1);
    }

    private boolean tickHiddenEffect() {
        if (this.duration == 0 && this.hiddenEffect != null) {
            this.copyFrom(this.hiddenEffect);
            this.hiddenEffect = this.hiddenEffect.hiddenEffect;
            return true;
        }
        return false;
    }

    public void onApplied(LivingEntity entity) {
        this.type.value().onApplied(entity, this.amplifier);
    }

    public void onEntityRemoval(ServerWorld world, LivingEntity entity, Entity.RemovalReason reason) {
        this.type.value().onEntityRemoval(world, entity, this.amplifier, reason);
    }

    public void onEntityDamage(ServerWorld world, LivingEntity entity, DamageSource source, float amount) {
        this.type.value().onEntityDamage(world, entity, this.amplifier, source, amount);
    }

    public String getTranslationKey() {
        return this.type.value().getTranslationKey();
    }

    public String toString() {
        String string = this.amplifier > 0 ? this.getTranslationKey() + " x " + (this.amplifier + 1) + ", Duration: " + this.getDurationString() : this.getTranslationKey() + ", Duration: " + this.getDurationString();
        if (!this.showParticles) {
            string = string + ", Particles: false";
        }
        if (!this.showIcon) {
            string = string + ", Show Icon: false";
        }
        return string;
    }

    private String getDurationString() {
        if (this.isInfinite()) {
            return "infinite";
        }
        return Integer.toString(this.duration);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof StatusEffectInstance) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)o;
            return this.duration == statusEffectInstance.duration && this.amplifier == statusEffectInstance.amplifier && this.ambient == statusEffectInstance.ambient && this.showParticles == statusEffectInstance.showParticles && this.showIcon == statusEffectInstance.showIcon && this.type.equals(statusEffectInstance.type);
        }
        return false;
    }

    public int hashCode() {
        int i = this.type.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        i = 31 * i + (this.ambient ? 1 : 0);
        i = 31 * i + (this.showParticles ? 1 : 0);
        i = 31 * i + (this.showIcon ? 1 : 0);
        return i;
    }

    @Override
    public int compareTo(StatusEffectInstance statusEffectInstance) {
        int i = 32147;
        if (this.getDuration() > 32147 && statusEffectInstance.getDuration() > 32147 || this.isAmbient() && statusEffectInstance.isAmbient()) {
            return ComparisonChain.start().compare(Boolean.valueOf(this.isAmbient()), Boolean.valueOf(statusEffectInstance.isAmbient())).compare(this.getEffectType().value().getColor(), statusEffectInstance.getEffectType().value().getColor()).result();
        }
        return ComparisonChain.start().compareFalseFirst(this.isAmbient(), statusEffectInstance.isAmbient()).compareFalseFirst(this.isInfinite(), statusEffectInstance.isInfinite()).compare(this.getDuration(), statusEffectInstance.getDuration()).compare(this.getEffectType().value().getColor(), statusEffectInstance.getEffectType().value().getColor()).result();
    }

    public void playApplySound(LivingEntity entity) {
        this.type.value().playApplySound(entity, this.amplifier);
    }

    public boolean equals(RegistryEntry<StatusEffect> effect) {
        return this.type.equals(effect);
    }

    public void copyFadingFrom(StatusEffectInstance effect) {
        this.fading.copyFrom(effect.fading);
    }

    public void skipFading() {
        this.fading.skipFading(this);
    }

    @Override
    public /* synthetic */ int compareTo(Object that) {
        return this.compareTo((StatusEffectInstance)that);
    }

    static class Fading {
        private float factor;
        private float lastFactor;

        Fading() {
        }

        public void skipFading(StatusEffectInstance effect) {
            this.lastFactor = this.factor = Fading.shouldFadeIn(effect) ? 1.0f : 0.0f;
        }

        public void copyFrom(Fading fading) {
            this.factor = fading.factor;
            this.lastFactor = fading.lastFactor;
        }

        public void update(StatusEffectInstance effect) {
            int i;
            float f;
            this.lastFactor = this.factor;
            boolean bl = Fading.shouldFadeIn(effect);
            float f2 = f = bl ? 1.0f : 0.0f;
            if (this.factor == f) {
                return;
            }
            StatusEffect statusEffect = effect.getEffectType().value();
            int n = i = bl ? statusEffect.getFadeInTicks() : statusEffect.getFadeOutTicks();
            if (i == 0) {
                this.factor = f;
            } else {
                float g = 1.0f / (float)i;
                this.factor += MathHelper.clamp(f - this.factor, -g, g);
            }
        }

        private static boolean shouldFadeIn(StatusEffectInstance effect) {
            return !effect.isDurationBelow(effect.getEffectType().value().getFadeOutThresholdTicks());
        }

        public float calculate(LivingEntity entity, float tickProgress) {
            if (entity.isRemoved()) {
                this.lastFactor = this.factor;
            }
            return MathHelper.lerp(tickProgress, this.lastFactor, this.factor);
        }
    }

    record Parameters(int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, Optional<Parameters> hiddenEffect) {
        public static final MapCodec<Parameters> CODEC = MapCodec.recursive((String)"MobEffectInstance.Details", codec -> RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", (Object)0).forGetter(Parameters::amplifier), (App)Codec.INT.optionalFieldOf("duration", (Object)0).forGetter(Parameters::duration), (App)Codec.BOOL.optionalFieldOf("ambient", (Object)false).forGetter(Parameters::ambient), (App)Codec.BOOL.optionalFieldOf("show_particles", (Object)true).forGetter(Parameters::showParticles), (App)Codec.BOOL.optionalFieldOf("show_icon").forGetter(parameters -> Optional.of(parameters.showIcon())), (App)codec.optionalFieldOf("hidden_effect").forGetter(Parameters::hiddenEffect)).apply((Applicative)instance, Parameters::create)));
        public static final PacketCodec<ByteBuf, Parameters> PACKET_CODEC = PacketCodec.recursive(packetCodec -> PacketCodec.tuple(PacketCodecs.VAR_INT, Parameters::amplifier, PacketCodecs.VAR_INT, Parameters::duration, PacketCodecs.BOOLEAN, Parameters::ambient, PacketCodecs.BOOLEAN, Parameters::showParticles, PacketCodecs.BOOLEAN, Parameters::showIcon, packetCodec.collect(PacketCodecs::optional), Parameters::hiddenEffect, Parameters::new));

        private static Parameters create(int amplifier, int duration, boolean ambient, boolean showParticles, Optional<Boolean> showIcon, Optional<Parameters> hiddenEffect) {
            return new Parameters(amplifier, duration, ambient, showParticles, showIcon.orElse(showParticles), hiddenEffect);
        }
    }
}

