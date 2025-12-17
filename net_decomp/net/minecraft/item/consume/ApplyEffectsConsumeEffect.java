/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.item.consume;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.world.World;

public record ApplyEffectsConsumeEffect(List<StatusEffectInstance> effects, float probability) implements ConsumeEffect
{
    public static final MapCodec<ApplyEffectsConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)StatusEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(ApplyEffectsConsumeEffect::effects), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("probability", (Object)Float.valueOf(1.0f)).forGetter(ApplyEffectsConsumeEffect::probability)).apply((Applicative)instance, ApplyEffectsConsumeEffect::new));
    public static final PacketCodec<RegistryByteBuf, ApplyEffectsConsumeEffect> PACKET_CODEC = PacketCodec.tuple(StatusEffectInstance.PACKET_CODEC.collect(PacketCodecs.toList()), ApplyEffectsConsumeEffect::effects, PacketCodecs.FLOAT, ApplyEffectsConsumeEffect::probability, ApplyEffectsConsumeEffect::new);

    public ApplyEffectsConsumeEffect(StatusEffectInstance effect, float probability) {
        this(List.of(effect), probability);
    }

    public ApplyEffectsConsumeEffect(List<StatusEffectInstance> effects) {
        this(effects, 1.0f);
    }

    public ApplyEffectsConsumeEffect(StatusEffectInstance effect) {
        this(effect, 1.0f);
    }

    public ConsumeEffect.Type<ApplyEffectsConsumeEffect> getType() {
        return ConsumeEffect.Type.APPLY_EFFECTS;
    }

    @Override
    public boolean onConsume(World world, ItemStack stack, LivingEntity user) {
        if (user.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        boolean bl = false;
        for (StatusEffectInstance statusEffectInstance : this.effects) {
            if (!user.addStatusEffect(new StatusEffectInstance(statusEffectInstance))) continue;
            bl = true;
        }
        return bl;
    }
}

