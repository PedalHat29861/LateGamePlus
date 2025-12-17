/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.boss.dragon.phase;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChargingPlayerPhase
extends AbstractPhase {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DURATION = 10;
    private @Nullable Vec3d pathTarget;
    private int chargingTicks;

    public ChargingPlayerPhase(EnderDragonEntity enderDragonEntity) {
        super(enderDragonEntity);
    }

    @Override
    public void serverTick(ServerWorld world) {
        if (this.pathTarget == null) {
            LOGGER.warn("Aborting charge player as no target was set.");
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        if (this.chargingTicks > 0 && this.chargingTicks++ >= 10) {
            this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
            return;
        }
        double d = this.pathTarget.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            ++this.chargingTicks;
        }
    }

    @Override
    public void beginPhase() {
        this.pathTarget = null;
        this.chargingTicks = 0;
    }

    public void setPathTarget(Vec3d pathTarget) {
        this.pathTarget = pathTarget;
    }

    @Override
    public float getMaxYAcceleration() {
        return 3.0f;
    }

    @Override
    public @Nullable Vec3d getPathTarget() {
        return this.pathTarget;
    }

    public PhaseType<ChargingPlayerPhase> getType() {
        return PhaseType.CHARGING_PLAYER;
    }
}

