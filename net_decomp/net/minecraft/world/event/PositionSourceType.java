/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.PositionSource;

public interface PositionSourceType<T extends PositionSource> {
    public static final PositionSourceType<BlockPositionSource> BLOCK = PositionSourceType.register("block", new BlockPositionSource.Type());
    public static final PositionSourceType<EntityPositionSource> ENTITY = PositionSourceType.register("entity", new EntityPositionSource.Type());

    public MapCodec<T> getCodec();

    public PacketCodec<? super RegistryByteBuf, T> getPacketCodec();

    public static <S extends PositionSourceType<T>, T extends PositionSource> S register(String id, S positionSourceType) {
        return (S)Registry.register(Registries.POSITION_SOURCE_TYPE, id, positionSourceType);
    }
}

