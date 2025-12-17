/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jspecify.annotations.Nullable;

public final class Difficulty
extends Enum<Difficulty>
implements StringIdentifiable {
    public static final /* enum */ Difficulty PEACEFUL = new Difficulty(0, "peaceful");
    public static final /* enum */ Difficulty EASY = new Difficulty(1, "easy");
    public static final /* enum */ Difficulty NORMAL = new Difficulty(2, "normal");
    public static final /* enum */ Difficulty HARD = new Difficulty(3, "hard");
    public static final StringIdentifiable.EnumCodec<Difficulty> CODEC;
    private static final IntFunction<Difficulty> BY_ID;
    public static final PacketCodec<ByteBuf, Difficulty> PACKET_CODEC;
    private final int id;
    private final String name;
    private static final /* synthetic */ Difficulty[] field_5804;

    public static Difficulty[] values() {
        return (Difficulty[])field_5804.clone();
    }

    public static Difficulty valueOf(String string) {
        return Enum.valueOf(Difficulty.class, string);
    }

    private Difficulty(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public Text getTranslatableName() {
        return Text.translatable("options.difficulty." + this.name);
    }

    public Text getInfo() {
        return Text.translatable("options.difficulty." + this.name + ".info");
    }

    @Deprecated
    public static Difficulty byId(int id) {
        return BY_ID.apply(id);
    }

    public static @Nullable Difficulty byName(String name) {
        return CODEC.byId(name);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ Difficulty[] method_36597() {
        return new Difficulty[]{PEACEFUL, EASY, NORMAL, HARD};
    }

    static {
        field_5804 = Difficulty.method_36597();
        CODEC = StringIdentifiable.createCodec(Difficulty::values);
        BY_ID = ValueLists.createIndexToValueFunction(Difficulty::getId, Difficulty.values(), ValueLists.OutOfBoundsHandling.WRAP);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, Difficulty::getId);
    }
}

