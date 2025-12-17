/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.item.consume;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class UseAction
extends Enum<UseAction>
implements StringIdentifiable {
    public static final /* enum */ UseAction NONE = new UseAction(0, "none");
    public static final /* enum */ UseAction EAT = new UseAction(1, "eat", true);
    public static final /* enum */ UseAction DRINK = new UseAction(2, "drink", true);
    public static final /* enum */ UseAction BLOCK = new UseAction(3, "block");
    public static final /* enum */ UseAction BOW = new UseAction(4, "bow");
    public static final /* enum */ UseAction TRIDENT = new UseAction(5, "trident");
    public static final /* enum */ UseAction CROSSBOW = new UseAction(6, "crossbow");
    public static final /* enum */ UseAction SPYGLASS = new UseAction(7, "spyglass");
    public static final /* enum */ UseAction TOOT_HORN = new UseAction(8, "toot_horn");
    public static final /* enum */ UseAction BRUSH = new UseAction(9, "brush");
    public static final /* enum */ UseAction BUNDLE = new UseAction(10, "bundle");
    public static final /* enum */ UseAction SPEAR = new UseAction(11, "spear", true);
    private static final IntFunction<UseAction> BY_ID;
    public static final Codec<UseAction> CODEC;
    public static final PacketCodec<ByteBuf, UseAction> PACKET_CODEC;
    private final int id;
    private final String name;
    private final boolean noOffset;
    private static final /* synthetic */ UseAction[] field_8948;

    public static UseAction[] values() {
        return (UseAction[])field_8948.clone();
    }

    public static UseAction valueOf(String string) {
        return Enum.valueOf(UseAction.class, string);
    }

    private UseAction(int id, String name) {
        this(id, name, false);
    }

    private UseAction(int id, String name, boolean noOffset) {
        this.id = id;
        this.name = name;
        this.noOffset = noOffset;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public boolean hasNoOffset() {
        return this.noOffset;
    }

    private static /* synthetic */ UseAction[] method_36686() {
        return new UseAction[]{NONE, EAT, DRINK, BLOCK, BOW, TRIDENT, CROSSBOW, SPYGLASS, TOOT_HORN, BRUSH, BUNDLE, SPEAR};
    }

    static {
        field_8948 = UseAction.method_36686();
        BY_ID = ValueLists.createIndexToValueFunction(UseAction::getId, UseAction.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(UseAction::values);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, UseAction::getId);
    }
}

