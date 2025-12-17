/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestTypes;

public record LodestoneTrackerComponent(Optional<GlobalPos> target, boolean tracked) {
    public static final Codec<LodestoneTrackerComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GlobalPos.CODEC.optionalFieldOf("target").forGetter(LodestoneTrackerComponent::target), (App)Codec.BOOL.optionalFieldOf("tracked", (Object)true).forGetter(LodestoneTrackerComponent::tracked)).apply((Applicative)instance, LodestoneTrackerComponent::new));
    public static final PacketCodec<ByteBuf, LodestoneTrackerComponent> PACKET_CODEC = PacketCodec.tuple(GlobalPos.PACKET_CODEC.collect(PacketCodecs::optional), LodestoneTrackerComponent::target, PacketCodecs.BOOLEAN, LodestoneTrackerComponent::tracked, LodestoneTrackerComponent::new);

    public LodestoneTrackerComponent forWorld(ServerWorld world) {
        if (!this.tracked || this.target.isEmpty()) {
            return this;
        }
        if (this.target.get().dimension() != world.getRegistryKey()) {
            return this;
        }
        BlockPos blockPos = this.target.get().pos();
        if (!world.isInBuildLimit(blockPos) || !world.getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, blockPos)) {
            return new LodestoneTrackerComponent(Optional.empty(), true);
        }
        return this;
    }
}

