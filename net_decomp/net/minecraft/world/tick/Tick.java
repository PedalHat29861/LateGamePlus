/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.tick;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.TickPriority;
import org.jspecify.annotations.Nullable;

public record Tick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
    public static final Hash.Strategy<Tick<?>> HASH_STRATEGY = new Hash.Strategy<Tick<?>>(){

        public int hashCode(Tick<?> tick) {
            return 31 * tick.pos().hashCode() + tick.type().hashCode();
        }

        public boolean equals(@Nullable Tick<?> tick, @Nullable Tick<?> tick2) {
            if (tick == tick2) {
                return true;
            }
            if (tick == null || tick2 == null) {
                return false;
            }
            return tick.type() == tick2.type() && tick.pos().equals(tick2.pos());
        }

        public /* synthetic */ boolean equals(@Nullable Object first, @Nullable Object second) {
            return this.equals((Tick)first, (Tick)second);
        }

        public /* synthetic */ int hashCode(Object tick) {
            return this.hashCode((Tick)tick);
        }
    };

    public static <T> Codec<Tick<T>> createCodec(Codec<T> typeCodec) {
        MapCodec mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("x").forGetter(Vec3i::getX), (App)Codec.INT.fieldOf("y").forGetter(Vec3i::getY), (App)Codec.INT.fieldOf("z").forGetter(Vec3i::getZ)).apply((Applicative)instance, BlockPos::new));
        return RecordCodecBuilder.create(instance -> instance.group((App)typeCodec.fieldOf("i").forGetter(Tick::type), (App)mapCodec.forGetter(Tick::pos), (App)Codec.INT.fieldOf("t").forGetter(Tick::delay), (App)TickPriority.CODEC.fieldOf("p").forGetter(Tick::priority)).apply((Applicative)instance, Tick::new));
    }

    public static <T> List<Tick<T>> filter(List<Tick<T>> ticks, ChunkPos chunkPos) {
        long l = chunkPos.toLong();
        return ticks.stream().filter(tick -> ChunkPos.toLong(tick.pos()) == l).toList();
    }

    public OrderedTick<T> createOrderedTick(long time, long subTickOrder) {
        return new OrderedTick<T>(this.type, this.pos, time + (long)this.delay, this.priority, subTickOrder);
    }

    public static <T> Tick<T> create(T type, BlockPos pos) {
        return new Tick<T>(type, pos, 0, TickPriority.NORMAL);
    }
}

