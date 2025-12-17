/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Hash$Strategy
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.tick;

import it.unimi.dsi.fastutil.Hash;
import java.util.Comparator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.Tick;
import net.minecraft.world.tick.TickPriority;
import org.jspecify.annotations.Nullable;

public record OrderedTick<T>(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    public static final Comparator<OrderedTick<?>> TRIGGER_TICK_COMPARATOR = (first, second) -> {
        int i = Long.compare(first.triggerTick, second.triggerTick);
        if (i != 0) {
            return i;
        }
        i = first.priority.compareTo(second.priority);
        if (i != 0) {
            return i;
        }
        return Long.compare(first.subTickOrder, second.subTickOrder);
    };
    public static final Comparator<OrderedTick<?>> BASIC_COMPARATOR = (first, second) -> {
        int i = first.priority.compareTo(second.priority);
        if (i != 0) {
            return i;
        }
        return Long.compare(first.subTickOrder, second.subTickOrder);
    };
    public static final Hash.Strategy<OrderedTick<?>> HASH_STRATEGY = new Hash.Strategy<OrderedTick<?>>(){

        public int hashCode(OrderedTick<?> orderedTick) {
            return 31 * orderedTick.pos().hashCode() + orderedTick.type().hashCode();
        }

        public boolean equals(@Nullable OrderedTick<?> orderedTick, @Nullable OrderedTick<?> orderedTick2) {
            if (orderedTick == orderedTick2) {
                return true;
            }
            if (orderedTick == null || orderedTick2 == null) {
                return false;
            }
            return orderedTick.type() == orderedTick2.type() && orderedTick.pos().equals(orderedTick2.pos());
        }

        public /* synthetic */ boolean equals(@Nullable Object first, @Nullable Object second) {
            return this.equals((OrderedTick)first, (OrderedTick)second);
        }

        public /* synthetic */ int hashCode(Object orderedTick) {
            return this.hashCode((OrderedTick)orderedTick);
        }
    };

    public OrderedTick(T type, BlockPos pos, long triggerTick, long subTickOrder) {
        this(type, pos, triggerTick, TickPriority.NORMAL, subTickOrder);
    }

    public OrderedTick {
        pos = pos.toImmutable();
    }

    public static <T> OrderedTick<T> create(T type, BlockPos pos) {
        return new OrderedTick<T>(type, pos, 0L, TickPriority.NORMAL, 0L);
    }

    public Tick<T> toTick(long time) {
        return new Tick<T>(this.type, this.pos, (int)(this.triggerTick - time), this.priority);
    }
}

