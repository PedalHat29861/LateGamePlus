/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.mob;

import java.util.Arrays;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

public final class Angriness
extends Enum<Angriness> {
    public static final /* enum */ Angriness CALM = new Angriness(0, SoundEvents.ENTITY_WARDEN_AMBIENT, SoundEvents.ENTITY_WARDEN_LISTENING);
    public static final /* enum */ Angriness AGITATED = new Angriness(40, SoundEvents.ENTITY_WARDEN_AGITATED, SoundEvents.ENTITY_WARDEN_LISTENING_ANGRY);
    public static final /* enum */ Angriness ANGRY = new Angriness(80, SoundEvents.ENTITY_WARDEN_ANGRY, SoundEvents.ENTITY_WARDEN_LISTENING_ANGRY);
    private static final Angriness[] VALUES;
    private final int threshold;
    private final SoundEvent sound;
    private final SoundEvent listeningSound;
    private static final /* synthetic */ Angriness[] field_38126;

    public static Angriness[] values() {
        return (Angriness[])field_38126.clone();
    }

    public static Angriness valueOf(String string) {
        return Enum.valueOf(Angriness.class, string);
    }

    private Angriness(int threshold, SoundEvent sound, SoundEvent listeningSound) {
        this.threshold = threshold;
        this.sound = sound;
        this.listeningSound = listeningSound;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public SoundEvent getSound() {
        return this.sound;
    }

    public SoundEvent getListeningSound() {
        return this.listeningSound;
    }

    public static Angriness getForAnger(int anger) {
        for (Angriness angriness : VALUES) {
            if (anger < angriness.threshold) continue;
            return angriness;
        }
        return CALM;
    }

    public boolean isAngry() {
        return this == ANGRY;
    }

    private static /* synthetic */ Angriness[] method_42175() {
        return new Angriness[]{CALM, AGITATED, ANGRY};
    }

    static {
        field_38126 = Angriness.method_42175();
        VALUES = Util.make(Angriness.values(), values -> Arrays.sort(values, (a, b) -> Integer.compare(b.threshold, a.threshold)));
    }
}

