/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public record JukeboxPlayablePredicate(Optional<RegistryEntryList<JukeboxSong>> song) implements ComponentSubPredicate<JukeboxPlayableComponent>
{
    public static final Codec<JukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RegistryCodecs.entryList(RegistryKeys.JUKEBOX_SONG).optionalFieldOf("song").forGetter(JukeboxPlayablePredicate::song)).apply((Applicative)instance, JukeboxPlayablePredicate::new));

    @Override
    public ComponentType<JukeboxPlayableComponent> getComponentType() {
        return DataComponentTypes.JUKEBOX_PLAYABLE;
    }

    @Override
    public boolean test(JukeboxPlayableComponent jukeboxPlayableComponent) {
        if (this.song.isPresent()) {
            boolean bl = false;
            for (RegistryEntry registryEntry : this.song.get()) {
                Optional optional = registryEntry.getKey();
                if (optional.isEmpty() || !optional.equals(jukeboxPlayableComponent.song().getKey())) continue;
                bl = true;
                break;
            }
            return bl;
        }
        return true;
    }

    public static JukeboxPlayablePredicate empty() {
        return new JukeboxPlayablePredicate(Optional.empty());
    }
}

