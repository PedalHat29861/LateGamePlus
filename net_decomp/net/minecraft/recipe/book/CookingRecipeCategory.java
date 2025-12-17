/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.recipe.book;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class CookingRecipeCategory
extends Enum<CookingRecipeCategory>
implements StringIdentifiable {
    public static final /* enum */ CookingRecipeCategory FOOD = new CookingRecipeCategory(0, "food");
    public static final /* enum */ CookingRecipeCategory BLOCKS = new CookingRecipeCategory(1, "blocks");
    public static final /* enum */ CookingRecipeCategory MISC = new CookingRecipeCategory(2, "misc");
    private static final IntFunction<CookingRecipeCategory> BY_ID;
    public static final Codec<CookingRecipeCategory> CODEC;
    public static final PacketCodec<ByteBuf, CookingRecipeCategory> PACKET_CODEC;
    private final int id;
    private final String name;
    private static final /* synthetic */ CookingRecipeCategory[] field_40247;

    public static CookingRecipeCategory[] values() {
        return (CookingRecipeCategory[])field_40247.clone();
    }

    public static CookingRecipeCategory valueOf(String string) {
        return Enum.valueOf(CookingRecipeCategory.class, string);
    }

    private CookingRecipeCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ CookingRecipeCategory[] method_45439() {
        return new CookingRecipeCategory[]{FOOD, BLOCKS, MISC};
    }

    static {
        field_40247 = CookingRecipeCategory.method_45439();
        BY_ID = ValueLists.createIndexToValueFunction(category -> category.id, CookingRecipeCategory.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(CookingRecipeCategory::values);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, category -> category.id);
    }
}

