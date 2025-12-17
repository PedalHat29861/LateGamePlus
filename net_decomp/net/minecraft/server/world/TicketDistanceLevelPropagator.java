/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.world;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkPosDistanceLevelPropagator;
import net.minecraft.server.world.ChunkTicketManager;

class TicketDistanceLevelPropagator
extends ChunkPosDistanceLevelPropagator {
    private static final int UNLOADED = ChunkLevels.INACCESSIBLE + 1;
    private final ChunkLevelManager levelManager;
    private final ChunkTicketManager ticketManager;

    public TicketDistanceLevelPropagator(ChunkLevelManager levelManager, ChunkTicketManager ticketManager) {
        super(UNLOADED + 1, 16, 256);
        this.levelManager = levelManager;
        this.ticketManager = ticketManager;
        ticketManager.setLoadingLevelUpdater(this::updateLevel);
    }

    @Override
    protected int getInitialLevel(long id) {
        return this.ticketManager.getLevel(id, false);
    }

    @Override
    protected int getLevel(long id) {
        ChunkHolder chunkHolder;
        if (!this.levelManager.isUnloaded(id) && (chunkHolder = this.levelManager.getChunkHolder(id)) != null) {
            return chunkHolder.getLevel();
        }
        return UNLOADED;
    }

    @Override
    protected void setLevel(long id, int level) {
        int i;
        ChunkHolder chunkHolder = this.levelManager.getChunkHolder(id);
        int n = i = chunkHolder == null ? UNLOADED : chunkHolder.getLevel();
        if (i == level) {
            return;
        }
        if ((chunkHolder = this.levelManager.setLevel(id, level, chunkHolder, i)) != null) {
            this.levelManager.chunkHoldersWithPendingUpdates.add(chunkHolder);
        }
    }

    public int update(int distance) {
        return this.applyPendingUpdates(distance);
    }
}

