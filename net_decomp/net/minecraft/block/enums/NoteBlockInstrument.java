/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;

public final class NoteBlockInstrument
extends Enum<NoteBlockInstrument>
implements StringIdentifiable {
    public static final /* enum */ NoteBlockInstrument HARP = new NoteBlockInstrument("harp", SoundEvents.BLOCK_NOTE_BLOCK_HARP, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument BASEDRUM = new NoteBlockInstrument("basedrum", SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument SNARE = new NoteBlockInstrument("snare", SoundEvents.BLOCK_NOTE_BLOCK_SNARE, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument HAT = new NoteBlockInstrument("hat", SoundEvents.BLOCK_NOTE_BLOCK_HAT, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument BASS = new NoteBlockInstrument("bass", SoundEvents.BLOCK_NOTE_BLOCK_BASS, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument FLUTE = new NoteBlockInstrument("flute", SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument BELL = new NoteBlockInstrument("bell", SoundEvents.BLOCK_NOTE_BLOCK_BELL, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument GUITAR = new NoteBlockInstrument("guitar", SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument CHIME = new NoteBlockInstrument("chime", SoundEvents.BLOCK_NOTE_BLOCK_CHIME, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument XYLOPHONE = new NoteBlockInstrument("xylophone", SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument IRON_XYLOPHONE = new NoteBlockInstrument("iron_xylophone", SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument COW_BELL = new NoteBlockInstrument("cow_bell", SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument DIDGERIDOO = new NoteBlockInstrument("didgeridoo", SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument BIT = new NoteBlockInstrument("bit", SoundEvents.BLOCK_NOTE_BLOCK_BIT, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument BANJO = new NoteBlockInstrument("banjo", SoundEvents.BLOCK_NOTE_BLOCK_BANJO, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument PLING = new NoteBlockInstrument("pling", SoundEvents.BLOCK_NOTE_BLOCK_PLING, Type.BASE_BLOCK);
    public static final /* enum */ NoteBlockInstrument ZOMBIE = new NoteBlockInstrument("zombie", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_ZOMBIE, Type.MOB_HEAD);
    public static final /* enum */ NoteBlockInstrument SKELETON = new NoteBlockInstrument("skeleton", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_SKELETON, Type.MOB_HEAD);
    public static final /* enum */ NoteBlockInstrument CREEPER = new NoteBlockInstrument("creeper", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_CREEPER, Type.MOB_HEAD);
    public static final /* enum */ NoteBlockInstrument DRAGON = new NoteBlockInstrument("dragon", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, Type.MOB_HEAD);
    public static final /* enum */ NoteBlockInstrument WITHER_SKELETON = new NoteBlockInstrument("wither_skeleton", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_WITHER_SKELETON, Type.MOB_HEAD);
    public static final /* enum */ NoteBlockInstrument PIGLIN = new NoteBlockInstrument("piglin", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_PIGLIN, Type.MOB_HEAD);
    public static final /* enum */ NoteBlockInstrument CUSTOM_HEAD = new NoteBlockInstrument("custom_head", SoundEvents.UI_BUTTON_CLICK, Type.CUSTOM);
    private final String name;
    private final RegistryEntry<SoundEvent> sound;
    private final Type type;
    private static final /* synthetic */ NoteBlockInstrument[] field_12652;

    public static NoteBlockInstrument[] values() {
        return (NoteBlockInstrument[])field_12652.clone();
    }

    public static NoteBlockInstrument valueOf(String string) {
        return Enum.valueOf(NoteBlockInstrument.class, string);
    }

    private NoteBlockInstrument(String name, RegistryEntry<SoundEvent> sound, Type type) {
        this.name = name;
        this.sound = sound;
        this.type = type;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public RegistryEntry<SoundEvent> getSound() {
        return this.sound;
    }

    public boolean canBePitched() {
        return this.type == Type.BASE_BLOCK;
    }

    public boolean hasCustomSound() {
        return this.type == Type.CUSTOM;
    }

    public boolean isNotBaseBlock() {
        return this.type != Type.BASE_BLOCK;
    }

    private static /* synthetic */ NoteBlockInstrument[] method_36730() {
        return new NoteBlockInstrument[]{HARP, BASEDRUM, SNARE, HAT, BASS, FLUTE, BELL, GUITAR, CHIME, XYLOPHONE, IRON_XYLOPHONE, COW_BELL, DIDGERIDOO, BIT, BANJO, PLING, ZOMBIE, SKELETON, CREEPER, DRAGON, WITHER_SKELETON, PIGLIN, CUSTOM_HEAD};
    }

    static {
        field_12652 = NoteBlockInstrument.method_36730();
    }

    static final class Type
    extends Enum<Type> {
        public static final /* enum */ Type BASE_BLOCK = new Type();
        public static final /* enum */ Type MOB_HEAD = new Type();
        public static final /* enum */ Type CUSTOM = new Type();
        private static final /* synthetic */ Type[] field_41609;

        public static Type[] values() {
            return (Type[])field_41609.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private static /* synthetic */ Type[] method_47892() {
            return new Type[]{BASE_BLOCK, MOB_HEAD, CUSTOM};
        }

        static {
            field_41609 = Type.method_47892();
        }
    }
}

