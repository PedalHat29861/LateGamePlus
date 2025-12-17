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
import net.minecraft.sound.SoundEvents;

public record BiomeMoodSound(RegistryEntry<SoundEvent> sound, int tickDelay, int blockSearchExtent, double offset) {
    public static final Codec<BiomeMoodSound> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.ENTRY_CODEC.fieldOf("sound").forGetter(sound -> sound.sound), (App)Codec.INT.fieldOf("tick_delay").forGetter(sound -> sound.tickDelay), (App)Codec.INT.fieldOf("block_search_extent").forGetter(sound -> sound.blockSearchExtent), (App)Codec.DOUBLE.fieldOf("offset").forGetter(sound -> sound.offset)).apply((Applicative)instance, BiomeMoodSound::new));
    public static final BiomeMoodSound CAVE = new BiomeMoodSound(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BiomeMoodSound.class, "soundEvent;tickDelay;blockSearchExtent;soundPositionOffset", "sound", "tickDelay", "blockSearchExtent", "offset"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BiomeMoodSound.class, "soundEvent;tickDelay;blockSearchExtent;soundPositionOffset", "sound", "tickDelay", "blockSearchExtent", "offset"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BiomeMoodSound.class, "soundEvent;tickDelay;blockSearchExtent;soundPositionOffset", "sound", "tickDelay", "blockSearchExtent", "offset"}, this, object);
    }
}

