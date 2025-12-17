/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.sound;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record SoundEvent(Identifier id, Optional<Float> fixedRange) {
    public static final Codec<SoundEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Identifier.CODEC.fieldOf("sound_id").forGetter(SoundEvent::id), (App)Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEvent::fixedRange)).apply((Applicative)instance, SoundEvent::of));
    public static final Codec<RegistryEntry<SoundEvent>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.SOUND_EVENT, CODEC);
    public static final PacketCodec<ByteBuf, SoundEvent> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, SoundEvent::id, PacketCodecs.FLOAT.collect(PacketCodecs::optional), SoundEvent::fixedRange, SoundEvent::of);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<SoundEvent>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.SOUND_EVENT, PACKET_CODEC);

    private static SoundEvent of(Identifier id, Optional<Float> fixedRange) {
        return fixedRange.map(fixedRangex -> SoundEvent.of(id, fixedRangex.floatValue())).orElseGet(() -> SoundEvent.of(id));
    }

    public static SoundEvent of(Identifier id) {
        return new SoundEvent(id, Optional.empty());
    }

    public static SoundEvent of(Identifier id, float fixedRange) {
        return new SoundEvent(id, Optional.of(Float.valueOf(fixedRange)));
    }

    public float getDistanceToTravel(float volume) {
        return this.fixedRange.orElse(Float.valueOf(volume > 1.0f ? 16.0f * volume : 16.0f)).floatValue();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{SoundEvent.class, "location;fixedRange", "id", "fixedRange"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SoundEvent.class, "location;fixedRange", "id", "fixedRange"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SoundEvent.class, "location;fixedRange", "id", "fixedRange"}, this, object);
    }
}

