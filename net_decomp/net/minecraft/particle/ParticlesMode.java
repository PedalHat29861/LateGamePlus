/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.particle;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.text.Text;
import net.minecraft.util.function.ValueLists;

public final class ParticlesMode
extends Enum<ParticlesMode> {
    public static final /* enum */ ParticlesMode ALL = new ParticlesMode(0, "options.particles.all");
    public static final /* enum */ ParticlesMode DECREASED = new ParticlesMode(1, "options.particles.decreased");
    public static final /* enum */ ParticlesMode MINIMAL = new ParticlesMode(2, "options.particles.minimal");
    private static final IntFunction<ParticlesMode> BY_ID;
    public static final Codec<ParticlesMode> CODEC;
    private final int id;
    private final Text text;
    private static final /* synthetic */ ParticlesMode[] field_18203;

    public static ParticlesMode[] values() {
        return (ParticlesMode[])field_18203.clone();
    }

    public static ParticlesMode valueOf(String string) {
        return Enum.valueOf(ParticlesMode.class, string);
    }

    private ParticlesMode(int id, String translationKey) {
        this.id = id;
        this.text = Text.translatable(translationKey);
    }

    public Text getText() {
        return this.text;
    }

    private static /* synthetic */ ParticlesMode[] method_36865() {
        return new ParticlesMode[]{ALL, DECREASED, MINIMAL};
    }

    static {
        field_18203 = ParticlesMode.method_36865();
        BY_ID = ValueLists.createIndexToValueFunction(particlesMode -> particlesMode.id, ParticlesMode.values(), ValueLists.OutOfBoundsHandling.WRAP);
        CODEC = Codec.INT.xmap(BY_ID::apply, mode -> mode.id);
    }
}

