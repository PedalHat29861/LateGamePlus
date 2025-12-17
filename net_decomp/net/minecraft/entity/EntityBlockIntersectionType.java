/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.entity;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public final class EntityBlockIntersectionType
extends Enum<EntityBlockIntersectionType> {
    public static final /* enum */ EntityBlockIntersectionType IN_BLOCK = new EntityBlockIntersectionType(0, 0x6000FF00);
    public static final /* enum */ EntityBlockIntersectionType IN_FLUID = new EntityBlockIntersectionType(1, 0x600000FF);
    public static final /* enum */ EntityBlockIntersectionType IN_AIR = new EntityBlockIntersectionType(2, 0x60333333);
    private static final IntFunction<EntityBlockIntersectionType> BY_ID;
    public static final PacketCodec<ByteBuf, EntityBlockIntersectionType> PACKET_CODEC;
    private final int id;
    private final int color;
    private static final /* synthetic */ EntityBlockIntersectionType[] field_62854;

    public static EntityBlockIntersectionType[] values() {
        return (EntityBlockIntersectionType[])field_62854.clone();
    }

    public static EntityBlockIntersectionType valueOf(String string) {
        return Enum.valueOf(EntityBlockIntersectionType.class, string);
    }

    private EntityBlockIntersectionType(int id, int color) {
        this.id = id;
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    private static /* synthetic */ EntityBlockIntersectionType[] method_74566() {
        return new EntityBlockIntersectionType[]{IN_BLOCK, IN_FLUID, IN_AIR};
    }

    static {
        field_62854 = EntityBlockIntersectionType.method_74566();
        BY_ID = ValueLists.createIndexToValueFunction(type -> type.id, EntityBlockIntersectionType.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, type -> type.id);
    }
}

