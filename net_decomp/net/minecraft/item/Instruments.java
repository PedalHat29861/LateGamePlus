/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.item.Instrument;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface Instruments {
    public static final int GOAT_HORN_RANGE = 256;
    public static final float GOAT_HORN_USE_DURATION = 7.0f;
    public static final RegistryKey<Instrument> PONDER_GOAT_HORN = Instruments.of("ponder_goat_horn");
    public static final RegistryKey<Instrument> SING_GOAT_HORN = Instruments.of("sing_goat_horn");
    public static final RegistryKey<Instrument> SEEK_GOAT_HORN = Instruments.of("seek_goat_horn");
    public static final RegistryKey<Instrument> FEEL_GOAT_HORN = Instruments.of("feel_goat_horn");
    public static final RegistryKey<Instrument> ADMIRE_GOAT_HORN = Instruments.of("admire_goat_horn");
    public static final RegistryKey<Instrument> CALL_GOAT_HORN = Instruments.of("call_goat_horn");
    public static final RegistryKey<Instrument> YEARN_GOAT_HORN = Instruments.of("yearn_goat_horn");
    public static final RegistryKey<Instrument> DREAM_GOAT_HORN = Instruments.of("dream_goat_horn");

    private static RegistryKey<Instrument> of(String id) {
        return RegistryKey.of(RegistryKeys.INSTRUMENT, Identifier.ofVanilla(id));
    }

    public static void bootstrap(Registerable<Instrument> registry) {
        Instruments.register(registry, PONDER_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(0), 7.0f, 256.0f);
        Instruments.register(registry, SING_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(1), 7.0f, 256.0f);
        Instruments.register(registry, SEEK_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(2), 7.0f, 256.0f);
        Instruments.register(registry, FEEL_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(3), 7.0f, 256.0f);
        Instruments.register(registry, ADMIRE_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(4), 7.0f, 256.0f);
        Instruments.register(registry, CALL_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(5), 7.0f, 256.0f);
        Instruments.register(registry, YEARN_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(6), 7.0f, 256.0f);
        Instruments.register(registry, DREAM_GOAT_HORN, (RegistryEntry)SoundEvents.GOAT_HORN_SOUNDS.get(7), 7.0f, 256.0f);
    }

    public static void register(Registerable<Instrument> registry, RegistryKey<Instrument> key, RegistryEntry<SoundEvent> sound, float useDuration, float range) {
        MutableText mutableText = Text.translatable(Util.createTranslationKey("instrument", key.getValue()));
        registry.register(key, new Instrument(sound, useDuration, range, mutableText));
    }
}

