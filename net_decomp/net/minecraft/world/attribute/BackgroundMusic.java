/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvent;

public record BackgroundMusic(Optional<MusicSound> defaultMusic, Optional<MusicSound> creativeMusic, Optional<MusicSound> underwaterMusic) {
    public static final BackgroundMusic EMPTY = new BackgroundMusic(Optional.empty(), Optional.empty(), Optional.empty());
    public static final BackgroundMusic DEFAULT = new BackgroundMusic(Optional.of(MusicType.GAME), Optional.of(MusicType.CREATIVE), Optional.empty());
    public static final Codec<BackgroundMusic> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MusicSound.CODEC.optionalFieldOf("default").forGetter(BackgroundMusic::defaultMusic), (App)MusicSound.CODEC.optionalFieldOf("creative").forGetter(BackgroundMusic::creativeMusic), (App)MusicSound.CODEC.optionalFieldOf("underwater").forGetter(BackgroundMusic::underwaterMusic)).apply((Applicative)instance, BackgroundMusic::new));

    public BackgroundMusic(MusicSound defaultMusic) {
        this(Optional.of(defaultMusic), Optional.empty(), Optional.empty());
    }

    public BackgroundMusic(RegistryEntry<SoundEvent> defaultMusic) {
        this(MusicType.createIngameMusic(defaultMusic));
    }

    public BackgroundMusic withUnderwater(MusicSound underwater) {
        return new BackgroundMusic(this.defaultMusic, this.creativeMusic, Optional.of(underwater));
    }

    public Optional<MusicSound> getCurrent(boolean creative, boolean underwater) {
        if (underwater && this.underwaterMusic.isPresent()) {
            return this.underwaterMusic;
        }
        if (creative && this.creativeMusic.isPresent()) {
            return this.creativeMusic;
        }
        return this.defaultMusic;
    }
}

