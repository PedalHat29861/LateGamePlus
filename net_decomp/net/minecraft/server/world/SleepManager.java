/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.world;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class SleepManager {
    private int total;
    private int sleeping;

    public boolean canSkipNight(int percentage) {
        return this.sleeping >= this.getNightSkippingRequirement(percentage);
    }

    public boolean canResetTime(int percentage, List<ServerPlayerEntity> players) {
        int i = (int)players.stream().filter(PlayerEntity::canResetTimeBySleeping).count();
        return i >= this.getNightSkippingRequirement(percentage);
    }

    public int getNightSkippingRequirement(int percentage) {
        return Math.max(1, MathHelper.ceil((float)(this.total * percentage) / 100.0f));
    }

    public void clearSleeping() {
        this.sleeping = 0;
    }

    public int getSleeping() {
        return this.sleeping;
    }

    public boolean update(List<ServerPlayerEntity> players) {
        int i = this.total;
        int j = this.sleeping;
        this.total = 0;
        this.sleeping = 0;
        for (ServerPlayerEntity serverPlayerEntity : players) {
            if (serverPlayerEntity.isSpectator()) continue;
            ++this.total;
            if (!serverPlayerEntity.isSleeping()) continue;
            ++this.sleeping;
        }
        return !(j <= 0 && this.sleeping <= 0 || i == this.total && j == this.sleeping);
    }
}

