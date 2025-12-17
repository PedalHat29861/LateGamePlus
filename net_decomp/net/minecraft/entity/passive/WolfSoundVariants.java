/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.passive.WolfSoundVariant;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class WolfSoundVariants {
    public static final RegistryKey<WolfSoundVariant> CLASSIC = WolfSoundVariants.of(Type.CLASSIC);
    public static final RegistryKey<WolfSoundVariant> PUGLIN = WolfSoundVariants.of(Type.PUGLIN);
    public static final RegistryKey<WolfSoundVariant> SAD = WolfSoundVariants.of(Type.SAD);
    public static final RegistryKey<WolfSoundVariant> ANGRY = WolfSoundVariants.of(Type.ANGRY);
    public static final RegistryKey<WolfSoundVariant> GRUMPY = WolfSoundVariants.of(Type.GRUMPY);
    public static final RegistryKey<WolfSoundVariant> BIG = WolfSoundVariants.of(Type.BIG);
    public static final RegistryKey<WolfSoundVariant> CUTE = WolfSoundVariants.of(Type.CUTE);

    private static RegistryKey<WolfSoundVariant> of(Type type) {
        return RegistryKey.of(RegistryKeys.WOLF_SOUND_VARIANT, Identifier.ofVanilla(type.getId()));
    }

    public static void bootstrap(Registerable<WolfSoundVariant> registry) {
        WolfSoundVariants.register(registry, CLASSIC, Type.CLASSIC);
        WolfSoundVariants.register(registry, PUGLIN, Type.PUGLIN);
        WolfSoundVariants.register(registry, SAD, Type.SAD);
        WolfSoundVariants.register(registry, ANGRY, Type.ANGRY);
        WolfSoundVariants.register(registry, GRUMPY, Type.GRUMPY);
        WolfSoundVariants.register(registry, BIG, Type.BIG);
        WolfSoundVariants.register(registry, CUTE, Type.CUTE);
    }

    private static void register(Registerable<WolfSoundVariant> registry, RegistryKey<WolfSoundVariant> key, Type type) {
        registry.register(key, SoundEvents.WOLF_SOUNDS.get((Object)type));
    }

    public static RegistryEntry<WolfSoundVariant> select(DynamicRegistryManager registries, Random random) {
        return registries.getOrThrow(RegistryKeys.WOLF_SOUND_VARIANT).getRandom(random).orElseThrow();
    }

    public static final class Type
    extends Enum<Type> {
        public static final /* enum */ Type CLASSIC = new Type("classic", "");
        public static final /* enum */ Type PUGLIN = new Type("puglin", "_puglin");
        public static final /* enum */ Type SAD = new Type("sad", "_sad");
        public static final /* enum */ Type ANGRY = new Type("angry", "_angry");
        public static final /* enum */ Type GRUMPY = new Type("grumpy", "_grumpy");
        public static final /* enum */ Type BIG = new Type("big", "_big");
        public static final /* enum */ Type CUTE = new Type("cute", "_cute");
        private final String id;
        private final String suffix;
        private static final /* synthetic */ Type[] field_57096;

        public static Type[] values() {
            return (Type[])field_57096.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private Type(String id, String suffix) {
            this.id = id;
            this.suffix = suffix;
        }

        public String getId() {
            return this.id;
        }

        public String getSoundEventSuffix() {
            return this.suffix;
        }

        private static /* synthetic */ Type[] method_68141() {
            return new Type[]{CLASSIC, PUGLIN, SAD, ANGRY, GRUMPY, BIG, CUTE};
        }

        static {
            field_57096 = Type.method_68141();
        }
    }
}

