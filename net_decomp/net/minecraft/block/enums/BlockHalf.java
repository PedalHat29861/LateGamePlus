/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class BlockHalf
extends Enum<BlockHalf>
implements StringIdentifiable {
    public static final /* enum */ BlockHalf TOP = new BlockHalf("top");
    public static final /* enum */ BlockHalf BOTTOM = new BlockHalf("bottom");
    private final String name;
    private static final /* synthetic */ BlockHalf[] field_12618;

    public static BlockHalf[] values() {
        return (BlockHalf[])field_12618.clone();
    }

    public static BlockHalf valueOf(String string) {
        return Enum.valueOf(BlockHalf.class, string);
    }

    private BlockHalf(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ BlockHalf[] method_36729() {
        return new BlockHalf[]{TOP, BOTTOM};
    }

    static {
        field_12618 = BlockHalf.method_36729();
    }
}

