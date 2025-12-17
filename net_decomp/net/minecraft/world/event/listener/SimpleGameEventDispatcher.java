/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 */
package net.minecraft.world.event.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.GameEventListenerDebugData;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventDispatcher;
import net.minecraft.world.event.listener.GameEventListener;

public class SimpleGameEventDispatcher
implements GameEventDispatcher {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> toRemove = Sets.newHashSet();
    private final List<GameEventListener> toAdd = Lists.newArrayList();
    private boolean dispatching;
    private final ServerWorld world;
    private final int ySectionCoord;
    private final DisposalCallback disposalCallback;

    public SimpleGameEventDispatcher(ServerWorld world, int ySectionCoord, DisposalCallback disposalCallback) {
        this.world = world;
        this.ySectionCoord = ySectionCoord;
        this.disposalCallback = disposalCallback;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void addListener(GameEventListener listener) {
        if (this.dispatching) {
            this.toAdd.add(listener);
        } else {
            this.listeners.add(listener);
        }
        SimpleGameEventDispatcher.sendDebugData(this.world, listener);
    }

    private static void sendDebugData(ServerWorld world, GameEventListener listener) {
        EntityPositionSource entityPositionSource;
        Entity entity;
        if (!world.getSubscriptionTracker().isSubscribed(DebugSubscriptionTypes.GAME_EVENT_LISTENERS)) {
            return;
        }
        GameEventListenerDebugData gameEventListenerDebugData = new GameEventListenerDebugData(listener.getRange());
        PositionSource positionSource = listener.getPositionSource();
        if (positionSource instanceof BlockPositionSource) {
            BlockPositionSource blockPositionSource = (BlockPositionSource)positionSource;
            world.getSubscriptionTracker().sendBlockDebugData(blockPositionSource.pos(), DebugSubscriptionTypes.GAME_EVENT_LISTENERS, gameEventListenerDebugData);
        } else if (positionSource instanceof EntityPositionSource && (entity = world.getEntity((entityPositionSource = (EntityPositionSource)positionSource).getUuid())) != null) {
            world.getSubscriptionTracker().sendEntityDebugData(entity, DebugSubscriptionTypes.GAME_EVENT_LISTENERS, gameEventListenerDebugData);
        }
    }

    @Override
    public void removeListener(GameEventListener listener) {
        if (this.dispatching) {
            this.toRemove.add(listener);
        } else {
            this.listeners.remove(listener);
        }
        if (this.listeners.isEmpty()) {
            this.disposalCallback.apply(this.ySectionCoord);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean dispatch(RegistryEntry<GameEvent> event, Vec3d pos, GameEvent.Emitter emitter, GameEventDispatcher.DispatchCallback callback) {
        this.dispatching = true;
        boolean bl = false;
        try {
            Iterator<GameEventListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                GameEventListener gameEventListener = iterator.next();
                if (this.toRemove.remove(gameEventListener)) {
                    iterator.remove();
                    continue;
                }
                Optional<Vec3d> optional = SimpleGameEventDispatcher.dispatchTo(this.world, pos, gameEventListener);
                if (!optional.isPresent()) continue;
                callback.visit(gameEventListener, optional.get());
                bl = true;
            }
        }
        finally {
            this.dispatching = false;
        }
        if (!this.toAdd.isEmpty()) {
            this.listeners.addAll(this.toAdd);
            this.toAdd.clear();
        }
        if (!this.toRemove.isEmpty()) {
            this.listeners.removeAll(this.toRemove);
            this.toRemove.clear();
        }
        return bl;
    }

    private static Optional<Vec3d> dispatchTo(ServerWorld world, Vec3d listenerPos, GameEventListener listener) {
        int i;
        Optional<Vec3d> optional = listener.getPositionSource().getPos(world);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        double d = BlockPos.ofFloored(optional.get()).getSquaredDistance(BlockPos.ofFloored(listenerPos));
        if (d > (double)(i = listener.getRange() * listener.getRange())) {
            return Optional.empty();
        }
        return optional;
    }

    @FunctionalInterface
    public static interface DisposalCallback {
        public void apply(int var1);
    }
}

