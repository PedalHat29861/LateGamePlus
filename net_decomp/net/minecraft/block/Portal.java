/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import org.jspecify.annotations.Nullable;

public interface Portal {
    default public int getPortalDelay(ServerWorld world, Entity entity) {
        return 0;
    }

    public @Nullable TeleportTarget createTeleportTarget(ServerWorld var1, Entity var2, BlockPos var3);

    default public Effect getPortalEffect() {
        return Effect.NONE;
    }

    public static final class Effect
    extends Enum<Effect> {
        public static final /* enum */ Effect CONFUSION = new Effect();
        public static final /* enum */ Effect NONE = new Effect();
        private static final /* synthetic */ Effect[] field_52063;

        public static Effect[] values() {
            return (Effect[])field_52063.clone();
        }

        public static Effect valueOf(String string) {
            return Enum.valueOf(Effect.class, string);
        }

        private static /* synthetic */ Effect[] method_60779() {
            return new Effect[]{CONFUSION, NONE};
        }

        static {
            field_52063 = Effect.method_60779();
        }
    }
}

