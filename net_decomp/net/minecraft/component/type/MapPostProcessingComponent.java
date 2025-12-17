/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.component.type;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public final class MapPostProcessingComponent
extends Enum<MapPostProcessingComponent> {
    public static final /* enum */ MapPostProcessingComponent LOCK = new MapPostProcessingComponent(0);
    public static final /* enum */ MapPostProcessingComponent SCALE = new MapPostProcessingComponent(1);
    public static final IntFunction<MapPostProcessingComponent> ID_TO_VALUE;
    public static final PacketCodec<ByteBuf, MapPostProcessingComponent> PACKET_CODEC;
    private final int id;
    private static final /* synthetic */ MapPostProcessingComponent[] field_49358;

    public static MapPostProcessingComponent[] values() {
        return (MapPostProcessingComponent[])field_49358.clone();
    }

    public static MapPostProcessingComponent valueOf(String string) {
        return Enum.valueOf(MapPostProcessingComponent.class, string);
    }

    private MapPostProcessingComponent(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    private static /* synthetic */ MapPostProcessingComponent[] method_57506() {
        return new MapPostProcessingComponent[]{LOCK, SCALE};
    }

    static {
        field_49358 = MapPostProcessingComponent.method_57506();
        ID_TO_VALUE = ValueLists.createIndexToValueFunction(MapPostProcessingComponent::getId, MapPostProcessingComponent.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, MapPostProcessingComponent::getId);
    }
}

