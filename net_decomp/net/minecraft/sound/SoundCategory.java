/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.sound;

public final class SoundCategory
extends Enum<SoundCategory> {
    public static final /* enum */ SoundCategory MASTER = new SoundCategory("master");
    public static final /* enum */ SoundCategory MUSIC = new SoundCategory("music");
    public static final /* enum */ SoundCategory RECORDS = new SoundCategory("record");
    public static final /* enum */ SoundCategory WEATHER = new SoundCategory("weather");
    public static final /* enum */ SoundCategory BLOCKS = new SoundCategory("block");
    public static final /* enum */ SoundCategory HOSTILE = new SoundCategory("hostile");
    public static final /* enum */ SoundCategory NEUTRAL = new SoundCategory("neutral");
    public static final /* enum */ SoundCategory PLAYERS = new SoundCategory("player");
    public static final /* enum */ SoundCategory AMBIENT = new SoundCategory("ambient");
    public static final /* enum */ SoundCategory VOICE = new SoundCategory("voice");
    public static final /* enum */ SoundCategory UI = new SoundCategory("ui");
    private final String name;
    private static final /* synthetic */ SoundCategory[] field_15255;

    public static SoundCategory[] values() {
        return (SoundCategory[])field_15255.clone();
    }

    public static SoundCategory valueOf(String string) {
        return Enum.valueOf(SoundCategory.class, string);
    }

    private SoundCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    private static /* synthetic */ SoundCategory[] method_36586() {
        return new SoundCategory[]{MASTER, MUSIC, RECORDS, WEATHER, BLOCKS, HOSTILE, NEUTRAL, PLAYERS, AMBIENT, VOICE, UI};
    }

    static {
        field_15255 = SoundCategory.method_36586();
    }
}

