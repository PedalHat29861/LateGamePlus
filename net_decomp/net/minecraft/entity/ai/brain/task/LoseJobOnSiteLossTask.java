/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

public class LoseJobOnSiteLossTask {
    public static Task<VillagerEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.JOB_SITE)).apply((Applicative)context, jobSite -> (world, entity, time) -> {
            boolean bl;
            VillagerData villagerData = entity.getVillagerData();
            boolean bl2 = bl = !villagerData.profession().matchesKey(VillagerProfession.NONE) && !villagerData.profession().matchesKey(VillagerProfession.NITWIT);
            if (bl && entity.getExperience() == 0 && villagerData.level() <= 1) {
                entity.setVillagerData(entity.getVillagerData().withProfession(world.getRegistryManager(), VillagerProfession.NONE));
                entity.reinitializeBrain(world);
                return true;
            }
            return false;
        }));
    }
}

