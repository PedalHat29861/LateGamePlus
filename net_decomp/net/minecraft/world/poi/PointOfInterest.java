/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.poi;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestType;

public class PointOfInterest {
    private final BlockPos pos;
    private final RegistryEntry<PointOfInterestType> type;
    private int freeTickets;
    private final Runnable updateListener;

    PointOfInterest(BlockPos pos, RegistryEntry<PointOfInterestType> type, int freeTickets, Runnable updateListener) {
        this.pos = pos.toImmutable();
        this.type = type;
        this.freeTickets = freeTickets;
        this.updateListener = updateListener;
    }

    public PointOfInterest(BlockPos pos, RegistryEntry<PointOfInterestType> type, Runnable updateListener) {
        this(pos, type, type.value().ticketCount(), updateListener);
    }

    public Serialized toSerialized() {
        return new Serialized(this.pos, this.type, this.freeTickets);
    }

    @Deprecated
    @Debug
    public int getFreeTickets() {
        return this.freeTickets;
    }

    protected boolean reserveTicket() {
        if (this.freeTickets <= 0) {
            return false;
        }
        --this.freeTickets;
        this.updateListener.run();
        return true;
    }

    protected boolean releaseTicket() {
        if (this.freeTickets >= this.type.value().ticketCount()) {
            return false;
        }
        ++this.freeTickets;
        this.updateListener.run();
        return true;
    }

    public boolean hasSpace() {
        return this.freeTickets > 0;
    }

    public boolean isOccupied() {
        return this.freeTickets != this.type.value().ticketCount();
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public RegistryEntry<PointOfInterestType> getType() {
        return this.type;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.pos, ((PointOfInterest)o).pos);
    }

    public int hashCode() {
        return this.pos.hashCode();
    }

    public record Serialized(BlockPos pos, RegistryEntry<PointOfInterestType> poiType, int freeTickets) {
        public static final Codec<Serialized> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(Serialized::pos), (App)RegistryFixedCodec.of(RegistryKeys.POINT_OF_INTEREST_TYPE).fieldOf("type").forGetter(Serialized::poiType), (App)Codec.INT.fieldOf("free_tickets").orElse((Object)0).forGetter(Serialized::freeTickets)).apply((Applicative)instance, Serialized::new));

        public PointOfInterest toPointOfInterest(Runnable updateListener) {
            return new PointOfInterest(this.pos, this.poiType, this.freeTickets, updateListener);
        }
    }
}

