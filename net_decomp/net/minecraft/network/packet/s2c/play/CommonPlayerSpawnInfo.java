/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

public record CommonPlayerSpawnInfo(RegistryEntry<DimensionType> dimensionType, RegistryKey<World> dimension, long seed, GameMode gameMode, @Nullable GameMode lastGameMode, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown, int seaLevel) {
    public CommonPlayerSpawnInfo(RegistryByteBuf buf) {
        this((RegistryEntry)DimensionType.PACKET_CODEC.decode(buf), buf.readRegistryKey(RegistryKeys.WORLD), buf.readLong(), GameMode.byIndex(buf.readByte()), GameMode.getOrNull(buf.readByte()), buf.readBoolean(), buf.readBoolean(), buf.readOptional(PacketByteBuf::readGlobalPos), buf.readVarInt(), buf.readVarInt());
    }

    public void write(RegistryByteBuf buf) {
        DimensionType.PACKET_CODEC.encode(buf, this.dimensionType);
        buf.writeRegistryKey(this.dimension);
        buf.writeLong(this.seed);
        buf.writeByte(this.gameMode.getIndex());
        buf.writeByte(GameMode.getId(this.lastGameMode));
        buf.writeBoolean(this.isDebug);
        buf.writeBoolean(this.isFlat);
        buf.writeOptional(this.lastDeathLocation, PacketByteBuf::writeGlobalPos);
        buf.writeVarInt(this.portalCooldown);
        buf.writeVarInt(this.seaLevel);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CommonPlayerSpawnInfo.class, "dimensionType;dimension;seed;gameType;previousGameType;isDebug;isFlat;lastDeathLocation;portalCooldown;seaLevel", "dimensionType", "dimension", "seed", "gameMode", "lastGameMode", "isDebug", "isFlat", "lastDeathLocation", "portalCooldown", "seaLevel"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CommonPlayerSpawnInfo.class, "dimensionType;dimension;seed;gameType;previousGameType;isDebug;isFlat;lastDeathLocation;portalCooldown;seaLevel", "dimensionType", "dimension", "seed", "gameMode", "lastGameMode", "isDebug", "isFlat", "lastDeathLocation", "portalCooldown", "seaLevel"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CommonPlayerSpawnInfo.class, "dimensionType;dimension;seed;gameType;previousGameType;isDebug;isFlat;lastDeathLocation;portalCooldown;seaLevel", "dimensionType", "dimension", "seed", "gameMode", "lastGameMode", "isDebug", "isFlat", "lastDeathLocation", "portalCooldown", "seaLevel"}, this, object);
    }
}

