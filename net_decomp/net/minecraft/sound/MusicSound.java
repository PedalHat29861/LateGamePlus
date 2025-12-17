/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.sound;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;

public record MusicSound(RegistryEntry<SoundEvent> sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
    public static final Codec<MusicSound> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.ENTRY_CODEC.fieldOf("sound").forGetter(MusicSound::sound), (App)Codecs.NON_NEGATIVE_INT.fieldOf("min_delay").forGetter(MusicSound::minDelay), (App)Codecs.NON_NEGATIVE_INT.fieldOf("max_delay").forGetter(MusicSound::maxDelay), (App)Codec.BOOL.optionalFieldOf("replace_current_music", (Object)false).forGetter(MusicSound::replaceCurrentMusic)).apply((Applicative)instance, MusicSound::new));
}

