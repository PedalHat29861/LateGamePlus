/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.entity.passive;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.world.World;
import org.slf4j.Logger;

public abstract class TameableShoulderEntity
extends TameableEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int READY_TO_SIT_COOLDOWN = 100;
    private int ticks;

    protected TameableShoulderEntity(EntityType<? extends TameableShoulderEntity> entityType, World world) {
        super((EntityType<? extends TameableEntity>)entityType, world);
    }

    public boolean mountOnto(ServerPlayerEntity player) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.getErrorReporterContext(), LOGGER);){
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, this.getRegistryManager());
            this.writeData(nbtWriteView);
            nbtWriteView.putString("id", this.getSavedEntityId());
            if (player.mountOntoShoulder(nbtWriteView.getNbt())) {
                this.discard();
                boolean bl = true;
                return bl;
            }
        }
        return false;
    }

    @Override
    public void tick() {
        ++this.ticks;
        super.tick();
    }

    public boolean isReadyToSitOnPlayer() {
        return this.ticks > 100;
    }
}

