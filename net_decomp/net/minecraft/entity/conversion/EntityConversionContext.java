/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.conversion;

import net.minecraft.entity.conversion.EntityConversionType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.scoreboard.Team;
import org.jspecify.annotations.Nullable;

public record EntityConversionContext(EntityConversionType type, boolean keepEquipment, boolean preserveCanPickUpLoot, @Nullable Team team) {
    public static EntityConversionContext create(MobEntity entity, boolean keepEquipment, boolean preserveCanPickUpLoot) {
        return new EntityConversionContext(EntityConversionType.SINGLE, keepEquipment, preserveCanPickUpLoot, entity.getScoreboardTeam());
    }

    @FunctionalInterface
    public static interface Finalizer<T extends MobEntity> {
        public void finalizeConversion(T var1);
    }
}

