/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.event.listener;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;

public interface GameEventListener {
    public PositionSource getPositionSource();

    public int getRange();

    public boolean listen(ServerWorld var1, RegistryEntry<GameEvent> var2, GameEvent.Emitter var3, Vec3d var4);

    default public TriggerOrder getTriggerOrder() {
        return TriggerOrder.UNSPECIFIED;
    }

    public static final class TriggerOrder
    extends Enum<TriggerOrder> {
        public static final /* enum */ TriggerOrder UNSPECIFIED = new TriggerOrder();
        public static final /* enum */ TriggerOrder BY_DISTANCE = new TriggerOrder();
        private static final /* synthetic */ TriggerOrder[] field_40355;

        public static TriggerOrder[] values() {
            return (TriggerOrder[])field_40355.clone();
        }

        public static TriggerOrder valueOf(String string) {
            return Enum.valueOf(TriggerOrder.class, string);
        }

        private static /* synthetic */ TriggerOrder[] method_45493() {
            return new TriggerOrder[]{UNSPECIFIED, BY_DISTANCE};
        }

        static {
            field_40355 = TriggerOrder.method_45493();
        }
    }

    public static interface Holder<T extends GameEventListener> {
        public T getEventListener();
    }
}

