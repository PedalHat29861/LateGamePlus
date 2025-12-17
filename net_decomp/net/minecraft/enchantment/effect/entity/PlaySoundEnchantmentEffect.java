/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.random.Random;

public record PlaySoundEnchantmentEffect(List<RegistryEntry<SoundEvent>> soundEvents, FloatProvider volume, FloatProvider pitch) implements EnchantmentEntityEffect
{
    public static final MapCodec<PlaySoundEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codecs.listOrSingle(SoundEvent.ENTRY_CODEC, SoundEvent.ENTRY_CODEC.sizeLimitedListOf(255)).fieldOf("sound").forGetter(PlaySoundEnchantmentEffect::soundEvents), (App)FloatProvider.createValidatedCodec(1.0E-5f, 10.0f).fieldOf("volume").forGetter(PlaySoundEnchantmentEffect::volume), (App)FloatProvider.createValidatedCodec(1.0E-5f, 2.0f).fieldOf("pitch").forGetter(PlaySoundEnchantmentEffect::pitch)).apply((Applicative)instance, PlaySoundEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (user.isSilent()) {
            return;
        }
        Random random = user.getRandom();
        int i = MathHelper.clamp(level - 1, 0, this.soundEvents.size() - 1);
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), this.soundEvents.get(i), user.getSoundCategory(), this.volume.get(random), this.pitch.get(random));
    }

    public MapCodec<PlaySoundEnchantmentEffect> getCodec() {
        return CODEC;
    }
}

