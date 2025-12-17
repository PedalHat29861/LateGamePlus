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
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

public record BiomeAdditionsSound(RegistryEntry<SoundEvent> sound, double tickChance) {
    public static final Codec<BiomeAdditionsSound> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.ENTRY_CODEC.fieldOf("sound").forGetter(sound -> sound.sound), (App)Codec.DOUBLE.fieldOf("tick_chance").forGetter(sound -> sound.tickChance)).apply((Applicative)instance, BiomeAdditionsSound::new));

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeAdditionsSound.class, "soundEvent;tickChance", "sound", "tickChance"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeAdditionsSound.class, "soundEvent;tickChance", "sound", "tickChance"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeAdditionsSound.class, "soundEvent;tickChance", "sound", "tickChance"}, this, object);
    }
}

