/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.equipment.trim;

import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ArmorTrimPatterns {
    public static final RegistryKey<ArmorTrimPattern> SENTRY = ArmorTrimPatterns.of("sentry");
    public static final RegistryKey<ArmorTrimPattern> DUNE = ArmorTrimPatterns.of("dune");
    public static final RegistryKey<ArmorTrimPattern> COAST = ArmorTrimPatterns.of("coast");
    public static final RegistryKey<ArmorTrimPattern> WILD = ArmorTrimPatterns.of("wild");
    public static final RegistryKey<ArmorTrimPattern> WARD = ArmorTrimPatterns.of("ward");
    public static final RegistryKey<ArmorTrimPattern> EYE = ArmorTrimPatterns.of("eye");
    public static final RegistryKey<ArmorTrimPattern> VEX = ArmorTrimPatterns.of("vex");
    public static final RegistryKey<ArmorTrimPattern> TIDE = ArmorTrimPatterns.of("tide");
    public static final RegistryKey<ArmorTrimPattern> SNOUT = ArmorTrimPatterns.of("snout");
    public static final RegistryKey<ArmorTrimPattern> RIB = ArmorTrimPatterns.of("rib");
    public static final RegistryKey<ArmorTrimPattern> SPIRE = ArmorTrimPatterns.of("spire");
    public static final RegistryKey<ArmorTrimPattern> WAYFINDER = ArmorTrimPatterns.of("wayfinder");
    public static final RegistryKey<ArmorTrimPattern> SHAPER = ArmorTrimPatterns.of("shaper");
    public static final RegistryKey<ArmorTrimPattern> SILENCE = ArmorTrimPatterns.of("silence");
    public static final RegistryKey<ArmorTrimPattern> RAISER = ArmorTrimPatterns.of("raiser");
    public static final RegistryKey<ArmorTrimPattern> HOST = ArmorTrimPatterns.of("host");
    public static final RegistryKey<ArmorTrimPattern> FLOW = ArmorTrimPatterns.of("flow");
    public static final RegistryKey<ArmorTrimPattern> BOLT = ArmorTrimPatterns.of("bolt");

    public static void bootstrap(Registerable<ArmorTrimPattern> registry) {
        ArmorTrimPatterns.register(registry, SENTRY);
        ArmorTrimPatterns.register(registry, DUNE);
        ArmorTrimPatterns.register(registry, COAST);
        ArmorTrimPatterns.register(registry, WILD);
        ArmorTrimPatterns.register(registry, WARD);
        ArmorTrimPatterns.register(registry, EYE);
        ArmorTrimPatterns.register(registry, VEX);
        ArmorTrimPatterns.register(registry, TIDE);
        ArmorTrimPatterns.register(registry, SNOUT);
        ArmorTrimPatterns.register(registry, RIB);
        ArmorTrimPatterns.register(registry, SPIRE);
        ArmorTrimPatterns.register(registry, WAYFINDER);
        ArmorTrimPatterns.register(registry, SHAPER);
        ArmorTrimPatterns.register(registry, SILENCE);
        ArmorTrimPatterns.register(registry, RAISER);
        ArmorTrimPatterns.register(registry, HOST);
        ArmorTrimPatterns.register(registry, FLOW);
        ArmorTrimPatterns.register(registry, BOLT);
    }

    public static void register(Registerable<ArmorTrimPattern> registry, RegistryKey<ArmorTrimPattern> key) {
        ArmorTrimPattern armorTrimPattern = new ArmorTrimPattern(ArmorTrimPatterns.getId(key), Text.translatable(Util.createTranslationKey("trim_pattern", key.getValue())), false);
        registry.register(key, armorTrimPattern);
    }

    private static RegistryKey<ArmorTrimPattern> of(String id) {
        return RegistryKey.of(RegistryKeys.TRIM_PATTERN, Identifier.ofVanilla(id));
    }

    public static Identifier getId(RegistryKey<ArmorTrimPattern> key) {
        return key.getValue();
    }
}

