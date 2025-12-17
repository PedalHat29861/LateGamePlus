/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestVisibleLivingEntitySensor;
import net.minecraft.server.world.ServerWorld;

public class VillagerHostilesSensor
extends NearestVisibleLivingEntitySensor {
    private static final ImmutableMap<EntityType<?>, Float> SQUARED_DISTANCES_FOR_DANGER = ImmutableMap.builder().put(EntityType.DROWNED, (Object)Float.valueOf(8.0f)).put(EntityType.EVOKER, (Object)Float.valueOf(12.0f)).put(EntityType.HUSK, (Object)Float.valueOf(8.0f)).put(EntityType.ILLUSIONER, (Object)Float.valueOf(12.0f)).put(EntityType.PILLAGER, (Object)Float.valueOf(15.0f)).put(EntityType.RAVAGER, (Object)Float.valueOf(12.0f)).put(EntityType.VEX, (Object)Float.valueOf(8.0f)).put(EntityType.VINDICATOR, (Object)Float.valueOf(10.0f)).put(EntityType.ZOGLIN, (Object)Float.valueOf(10.0f)).put(EntityType.ZOMBIE, (Object)Float.valueOf(8.0f)).put(EntityType.ZOMBIE_VILLAGER, (Object)Float.valueOf(8.0f)).build();

    @Override
    protected boolean matches(ServerWorld world, LivingEntity entity, LivingEntity target) {
        return this.isHostile(target) && this.isCloseEnoughForDanger(entity, target);
    }

    private boolean isCloseEnoughForDanger(LivingEntity villager, LivingEntity target) {
        float f = ((Float)SQUARED_DISTANCES_FOR_DANGER.get(target.getType())).floatValue();
        return target.squaredDistanceTo(villager) <= (double)(f * f);
    }

    @Override
    protected MemoryModuleType<LivingEntity> getOutputMemoryModule() {
        return MemoryModuleType.NEAREST_HOSTILE;
    }

    private boolean isHostile(LivingEntity entity) {
        return SQUARED_DISTANCES_FOR_DANGER.containsKey(entity.getType());
    }
}

