/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

public class ChunkTicket {
    public static final MapCodec<ChunkTicket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Registries.TICKET_TYPE.getCodec().fieldOf("type").forGetter(ChunkTicket::getType), (App)Codecs.NON_NEGATIVE_INT.fieldOf("level").forGetter(ChunkTicket::getLevel), (App)Codec.LONG.optionalFieldOf("ticks_left", (Object)0L).forGetter(ticket -> ticket.ticksLeft)).apply((Applicative)instance, ChunkTicket::new));
    private final ChunkTicketType type;
    private final int level;
    private long ticksLeft;

    public ChunkTicket(ChunkTicketType type, int level) {
        this(type, level, type.expiryTicks());
    }

    private ChunkTicket(ChunkTicketType type, int level, long ticksLeft) {
        this.type = type;
        this.level = level;
        this.ticksLeft = ticksLeft;
    }

    public String toString() {
        if (this.type.canExpire()) {
            return "Ticket[" + Util.registryValueToString(Registries.TICKET_TYPE, this.type) + " " + this.level + "] with " + this.ticksLeft + " ticks left ( out of" + this.type.expiryTicks() + ")";
        }
        return "Ticket[" + Util.registryValueToString(Registries.TICKET_TYPE, this.type) + " " + this.level + "] with no timeout";
    }

    public ChunkTicketType getType() {
        return this.type;
    }

    public int getLevel() {
        return this.level;
    }

    public void refreshExpiry() {
        this.ticksLeft = this.type.expiryTicks();
    }

    public void tick() {
        if (this.type.canExpire()) {
            --this.ticksLeft;
        }
    }

    public boolean isExpired() {
        return this.type.canExpire() && this.ticksLeft < 0L;
    }
}

