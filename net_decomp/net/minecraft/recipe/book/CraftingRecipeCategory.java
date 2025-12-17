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

public final class CraftingRecipeCategory
extends Enum<CraftingRecipeCategory>
implements StringIdentifiable {
    public static final /* enum */ CraftingRecipeCategory BUILDING = new CraftingRecipeCategory("building", 0);
    public static final /* enum */ CraftingRecipeCategory REDSTONE = new CraftingRecipeCategory("redstone", 1);
    public static final /* enum */ CraftingRecipeCategory EQUIPMENT = new CraftingRecipeCategory("equipment", 2);
    public static final /* enum */ CraftingRecipeCategory MISC = new CraftingRecipeCategory("misc", 3);
    public static final Codec<CraftingRecipeCategory> CODEC;
    public static final IntFunction<CraftingRecipeCategory> INDEX_TO_VALUE;
    public static final PacketCodec<ByteBuf, CraftingRecipeCategory> PACKET_CODEC;
    private final String id;
    private final int index;
    private static final /* synthetic */ CraftingRecipeCategory[] field_40254;

    public static CraftingRecipeCategory[] values() {
        return (CraftingRecipeCategory[])field_40254.clone();
    }

    public static CraftingRecipeCategory valueOf(String string) {
        return Enum.valueOf(CraftingRecipeCategory.class, string);
    }

    private CraftingRecipeCategory(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    private static /* synthetic */ CraftingRecipeCategory[] method_45440() {
        return new CraftingRecipeCategory[]{BUILDING, REDSTONE, EQUIPMENT, MISC};
    }

    static {
        field_40254 = CraftingRecipeCategory.method_45440();
        CODEC = StringIdentifiable.createCodec(CraftingRecipeCategory::values);
        INDEX_TO_VALUE = ValueLists.createIndexToValueFunction(CraftingRecipeCategory::getIndex, CraftingRecipeCategory.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, CraftingRecipeCategory::getIndex);
    }
}

