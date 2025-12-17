/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.goal;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.rule.GameRules;

public class UniversalAngerGoal<T extends MobEntity>
extends Goal {
    private static final int BOX_VERTICAL_EXPANSION = 10;
    private final T mob;
    private final boolean triggerOthers;
    private int lastAttackedTime;

    public UniversalAngerGoal(T mob, boolean triggerOthers) {
        this.mob = mob;
        this.triggerOthers = triggerOthers;
    }

    @Override
    public boolean canStart() {
        return UniversalAngerGoal.getServerWorld(this.mob).getGameRules().getValue(GameRules.UNIVERSAL_ANGER) != false && this.canStartUniversalAnger();
    }

    private boolean canStartUniversalAnger() {
        return ((LivingEntity)this.mob).getAttacker() != null && ((LivingEntity)this.mob).getAttacker().getType() == EntityType.PLAYER && ((LivingEntity)this.mob).getLastAttackedTime() > this.lastAttackedTime;
    }

    @Override
    public void start() {
        this.lastAttackedTime = ((LivingEntity)this.mob).getLastAttackedTime();
        ((Angerable)this.mob).universallyAnger();
        if (this.triggerOthers) {
            this.getOthersInRange().stream().filter(entity -> entity != this.mob).map(entity -> (Angerable)((Object)entity)).forEach(Angerable::universallyAnger);
        }
        super.start();
    }

    private List<? extends MobEntity> getOthersInRange() {
        double d = ((LivingEntity)this.mob).getAttributeValue(EntityAttributes.FOLLOW_RANGE);
        Box box = Box.from(((Entity)this.mob).getEntityPos()).expand(d, 10.0, d);
        return ((Entity)this.mob).getEntityWorld().getEntitiesByClass(this.mob.getClass(), box, EntityPredicates.EXCEPT_SPECTATOR);
    }
}

