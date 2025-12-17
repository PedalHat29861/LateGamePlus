/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.entity.CollisionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;

public interface EntityCollisionHandler {
    public static final EntityCollisionHandler DUMMY = new EntityCollisionHandler(){

        @Override
        public void addEvent(CollisionEvent event) {
        }

        @Override
        public void addPreCallback(CollisionEvent event, Consumer<Entity> callback) {
        }

        @Override
        public void addPostCallback(CollisionEvent event, Consumer<Entity> callback) {
        }
    };

    public void addEvent(CollisionEvent var1);

    public void addPreCallback(CollisionEvent var1, Consumer<Entity> var2);

    public void addPostCallback(CollisionEvent var1, Consumer<Entity> var2);

    public static class Impl
    implements EntityCollisionHandler {
        private static final CollisionEvent[] ALL_EVENTS = CollisionEvent.values();
        private static final int INVALID_VERSION = -1;
        private final Set<CollisionEvent> activeEvents = EnumSet.noneOf(CollisionEvent.class);
        private final Map<CollisionEvent, List<Consumer<Entity>>> preCallbacks = Util.mapEnum(CollisionEvent.class, value -> new ArrayList());
        private final Map<CollisionEvent, List<Consumer<Entity>>> postCallbacks = Util.mapEnum(CollisionEvent.class, value -> new ArrayList());
        private final List<Consumer<Entity>> callbacks = new ArrayList<Consumer<Entity>>();
        private int version = -1;

        public void updateIfNecessary(int version) {
            if (this.version != version) {
                this.version = version;
                this.update();
            }
        }

        public void runCallbacks(Entity entity) {
            this.update();
            for (Consumer<Entity> consumer : this.callbacks) {
                if (!entity.isAlive()) break;
                consumer.accept(entity);
            }
            this.callbacks.clear();
            this.version = -1;
        }

        private void update() {
            for (CollisionEvent collisionEvent : ALL_EVENTS) {
                List<Consumer<Entity>> list = this.preCallbacks.get((Object)collisionEvent);
                this.callbacks.addAll(list);
                list.clear();
                if (this.activeEvents.remove((Object)collisionEvent)) {
                    this.callbacks.add(collisionEvent.getAction());
                }
                List<Consumer<Entity>> list2 = this.postCallbacks.get((Object)collisionEvent);
                this.callbacks.addAll(list2);
                list2.clear();
            }
        }

        @Override
        public void addEvent(CollisionEvent event) {
            this.activeEvents.add(event);
        }

        @Override
        public void addPreCallback(CollisionEvent event, Consumer<Entity> callback) {
            this.preCallbacks.get((Object)event).add(callback);
        }

        @Override
        public void addPostCallback(CollisionEvent event, Consumer<Entity> callback) {
            this.postCallbacks.get((Object)event).add(callback);
        }
    }
}

