/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.world;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public record ChunkTicketType(long expiryTicks, @Flags int flags) {
    public static final long NO_EXPIRATION = 0L;
    public static final int SERIALIZE = 1;
    public static final int FOR_LOADING = 2;
    public static final int FOR_SIMULATION = 4;
    public static final int RESETS_IDLE_TIMEOUT = 8;
    public static final int CAN_EXPIRE_BEFORE_LOAD = 16;
    public static final ChunkTicketType PLAYER_SPAWN = ChunkTicketType.register("player_spawn", 20L, 2);
    public static final ChunkTicketType SPAWN_SEARCH = ChunkTicketType.register("spawn_search", 1L, 2);
    public static final ChunkTicketType DRAGON = ChunkTicketType.register("dragon", 0L, 6);
    public static final ChunkTicketType PLAYER_LOADING = ChunkTicketType.register("player_loading", 0L, 2);
    public static final ChunkTicketType PLAYER_SIMULATION = ChunkTicketType.register("player_simulation", 0L, 12);
    public static final ChunkTicketType FORCED = ChunkTicketType.register("forced", 0L, 15);
    public static final ChunkTicketType PORTAL = ChunkTicketType.register("portal", 300L, 15);
    public static final ChunkTicketType ENDER_PEARL = ChunkTicketType.register("ender_pearl", 40L, 14);
    public static final ChunkTicketType UNKNOWN = ChunkTicketType.register("unknown", 1L, 18);

    private static ChunkTicketType register(String id, long expiryTicks, @Flags int flags) {
        return Registry.register(Registries.TICKET_TYPE, id, new ChunkTicketType(expiryTicks, flags));
    }

    public boolean shouldSerialize() {
        return (this.flags & 1) != 0;
    }

    public boolean isForLoading() {
        return (this.flags & 2) != 0;
    }

    public boolean isForSimulation() {
        return (this.flags & 4) != 0;
    }

    public boolean resetsIdleTimeout() {
        return (this.flags & 8) != 0;
    }

    public boolean canExpireBeforeLoad() {
        return (this.flags & 0x10) != 0;
    }

    public boolean canExpire() {
        return this.expiryTicks != 0L;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChunkTicketType.class, "timeout;flags", "expiryTicks", "flags"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChunkTicketType.class, "timeout;flags", "expiryTicks", "flags"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChunkTicketType.class, "timeout;flags", "expiryTicks", "flags"}, this, object);
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
    public static @interface Flags {
    }
}

