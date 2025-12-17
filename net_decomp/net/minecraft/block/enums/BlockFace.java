/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class BlockFace
extends Enum<BlockFace>
implements StringIdentifiable {
    public static final /* enum */ BlockFace FLOOR = new BlockFace("floor");
    public static final /* enum */ BlockFace WALL = new BlockFace("wall");
    public static final /* enum */ BlockFace CEILING = new BlockFace("ceiling");
    private final String name;
    private static final /* synthetic */ BlockFace[] field_12474;

    public static BlockFace[] values() {
        return (BlockFace[])field_12474.clone();
    }

    public static BlockFace valueOf(String string) {
        return Enum.valueOf(BlockFace.class, string);
    }

    private BlockFace(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ BlockFace[] method_36720() {
        return new BlockFace[]{FLOOR, WALL, CEILING};
    }

    static {
        field_12474 = BlockFace.method_36720();
    }
}

