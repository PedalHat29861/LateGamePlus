/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.item;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class ItemDisplayContext
extends Enum<ItemDisplayContext>
implements StringIdentifiable {
    public static final /* enum */ ItemDisplayContext NONE = new ItemDisplayContext(0, "none");
    public static final /* enum */ ItemDisplayContext THIRD_PERSON_LEFT_HAND = new ItemDisplayContext(1, "thirdperson_lefthand");
    public static final /* enum */ ItemDisplayContext THIRD_PERSON_RIGHT_HAND = new ItemDisplayContext(2, "thirdperson_righthand");
    public static final /* enum */ ItemDisplayContext FIRST_PERSON_LEFT_HAND = new ItemDisplayContext(3, "firstperson_lefthand");
    public static final /* enum */ ItemDisplayContext FIRST_PERSON_RIGHT_HAND = new ItemDisplayContext(4, "firstperson_righthand");
    public static final /* enum */ ItemDisplayContext HEAD = new ItemDisplayContext(5, "head");
    public static final /* enum */ ItemDisplayContext GUI = new ItemDisplayContext(6, "gui");
    public static final /* enum */ ItemDisplayContext GROUND = new ItemDisplayContext(7, "ground");
    public static final /* enum */ ItemDisplayContext FIXED = new ItemDisplayContext(8, "fixed");
    public static final /* enum */ ItemDisplayContext ON_SHELF = new ItemDisplayContext(9, "on_shelf");
    public static final Codec<ItemDisplayContext> CODEC;
    public static final IntFunction<ItemDisplayContext> FROM_INDEX;
    private final byte index;
    private final String name;
    private static final /* synthetic */ ItemDisplayContext[] field_4314;

    public static ItemDisplayContext[] values() {
        return (ItemDisplayContext[])field_4314.clone();
    }

    public static ItemDisplayContext valueOf(String string) {
        return Enum.valueOf(ItemDisplayContext.class, string);
    }

    private ItemDisplayContext(int index, String name) {
        this.name = name;
        this.index = (byte)index;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public byte getIndex() {
        return this.index;
    }

    public boolean isFirstPerson() {
        return this == FIRST_PERSON_LEFT_HAND || this == FIRST_PERSON_RIGHT_HAND;
    }

    public boolean isLeftHand() {
        return this == FIRST_PERSON_LEFT_HAND || this == THIRD_PERSON_LEFT_HAND;
    }

    private static /* synthetic */ ItemDisplayContext[] method_36922() {
        return new ItemDisplayContext[]{NONE, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, HEAD, GUI, GROUND, FIXED, ON_SHELF};
    }

    static {
        field_4314 = ItemDisplayContext.method_36922();
        CODEC = StringIdentifiable.createCodec(ItemDisplayContext::values);
        FROM_INDEX = ValueLists.createIndexToValueFunction(ItemDisplayContext::getIndex, ItemDisplayContext.values(), ValueLists.OutOfBoundsHandling.ZERO);
    }
}

