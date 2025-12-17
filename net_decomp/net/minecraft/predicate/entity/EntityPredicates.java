/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Predicates
 */
package net.minecraft.predicate.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;

public final class EntityPredicates {
    public static final Predicate<Entity> VALID_ENTITY = Entity::isAlive;
    public static final Predicate<Entity> VALID_LIVING_ENTITY = entity -> entity.isAlive() && entity instanceof LivingEntity;
    public static final Predicate<Entity> NOT_MOUNTED = entity -> entity.isAlive() && !entity.hasPassengers() && !entity.hasVehicle();
    public static final Predicate<Entity> VALID_INVENTORIES = entity -> entity instanceof Inventory && entity.isAlive();
    public static final Predicate<Entity> EXCEPT_CREATIVE_OR_SPECTATOR = entity -> {
        if (!(entity instanceof PlayerEntity)) return true;
        PlayerEntity playerEntity = (PlayerEntity)entity;
        if (entity.isSpectator()) return false;
        if (playerEntity.isCreative()) return false;
        return true;
    };
    public static final Predicate<Entity> EXCEPT_SPECTATOR = entity -> !entity.isSpectator();
    public static final Predicate<Entity> CAN_COLLIDE = EXCEPT_SPECTATOR.and(entity -> entity.isCollidable(null));
    public static final Predicate<Entity> CAN_HIT = EXCEPT_SPECTATOR.and(Entity::canHit);

    private EntityPredicates() {
    }

    public static Predicate<Entity> maxDistance(double x, double y, double z, double max) {
        double d = max * max;
        return entity -> entity.squaredDistanceTo(x, y, z) <= d;
    }

    public static Predicate<Entity> canBePushedBy(Entity entity) {
        AbstractTeam.CollisionRule collisionRule;
        Team abstractTeam = entity.getScoreboardTeam();
        AbstractTeam.CollisionRule collisionRule2 = collisionRule = abstractTeam == null ? AbstractTeam.CollisionRule.ALWAYS : ((AbstractTeam)abstractTeam).getCollisionRule();
        if (collisionRule == AbstractTeam.CollisionRule.NEVER) {
            return Predicates.alwaysFalse();
        }
        return EXCEPT_SPECTATOR.and(entityx -> {
            boolean bl;
            AbstractTeam.CollisionRule collisionRule2;
            PlayerEntity playerEntity;
            if (!entityx.isPushable()) {
                return false;
            }
            if (!(!entity.getEntityWorld().isClient() || entityx instanceof PlayerEntity && (playerEntity = (PlayerEntity)entityx).isMainPlayer())) {
                return false;
            }
            Team abstractTeam2 = entityx.getScoreboardTeam();
            AbstractTeam.CollisionRule collisionRule3 = collisionRule2 = abstractTeam2 == null ? AbstractTeam.CollisionRule.ALWAYS : ((AbstractTeam)abstractTeam2).getCollisionRule();
            if (collisionRule2 == AbstractTeam.CollisionRule.NEVER) {
                return false;
            }
            boolean bl2 = bl = abstractTeam != null && abstractTeam.isEqual(abstractTeam2);
            if ((collisionRule == AbstractTeam.CollisionRule.PUSH_OWN_TEAM || collisionRule2 == AbstractTeam.CollisionRule.PUSH_OWN_TEAM) && bl) {
                return false;
            }
            return collisionRule != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS && collisionRule2 != AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS || bl;
        });
    }

    public static Predicate<Entity> rides(Entity entity) {
        return testedEntity -> {
            while (testedEntity.hasVehicle()) {
                if ((testedEntity = testedEntity.getVehicle()) != entity) continue;
                return false;
            }
            return true;
        };
    }
}

