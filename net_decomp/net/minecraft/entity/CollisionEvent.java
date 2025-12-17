/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import java.util.function.Consumer;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;

public final class CollisionEvent
extends Enum<CollisionEvent> {
    public static final /* enum */ CollisionEvent FREEZE = new CollisionEvent(entity -> {
        entity.setInPowderSnow(true);
        if (entity.canFreeze()) {
            entity.setFrozenTicks(Math.min(entity.getMinFreezeDamageTicks(), entity.getFrozenTicks() + 1));
        }
    });
    public static final /* enum */ CollisionEvent CLEAR_FREEZE = new CollisionEvent(Entity::defrost);
    public static final /* enum */ CollisionEvent FIRE_IGNITE = new CollisionEvent(AbstractFireBlock::igniteEntity);
    public static final /* enum */ CollisionEvent LAVA_IGNITE = new CollisionEvent(Entity::igniteByLava);
    public static final /* enum */ CollisionEvent EXTINGUISH = new CollisionEvent(Entity::extinguish);
    private final Consumer<Entity> action;
    private static final /* synthetic */ CollisionEvent[] field_56647;

    public static CollisionEvent[] values() {
        return (CollisionEvent[])field_56647.clone();
    }

    public static CollisionEvent valueOf(String string) {
        return Enum.valueOf(CollisionEvent.class, string);
    }

    private CollisionEvent(Consumer<Entity> action) {
        this.action = action;
    }

    public Consumer<Entity> getAction() {
        return this.action;
    }

    private static /* synthetic */ CollisionEvent[] method_67648() {
        return new CollisionEvent[]{FREEZE, CLEAR_FREEZE, FIRE_IGNITE, LAVA_IGNITE, EXTINGUISH};
    }

    static {
        field_56647 = CollisionEvent.method_67648();
    }
}

