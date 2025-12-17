/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 */
package net.minecraft.server.world;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.server.world.ChunkPosDistanceLevelPropagator;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.math.ChunkPos;

public class SimulationDistanceLevelPropagator
extends ChunkPosDistanceLevelPropagator {
    public static final int field_44858 = 33;
    protected final Long2ByteMap levels = new Long2ByteOpenHashMap();
    private final ChunkTicketManager ticketManager;

    public SimulationDistanceLevelPropagator(ChunkTicketManager ticketManager) {
        super(34, 16, 256);
        this.ticketManager = ticketManager;
        ticketManager.setSimulationLevelUpdater(this::updateLevel);
        this.levels.defaultReturnValue((byte)33);
    }

    @Override
    protected int getInitialLevel(long id) {
        return this.ticketManager.getLevel(id, true);
    }

    public int getLevel(ChunkPos pos) {
        return this.getLevel(pos.toLong());
    }

    @Override
    protected int getLevel(long id) {
        return this.levels.get(id);
    }

    @Override
    protected void setLevel(long id, int level) {
        if (level >= 33) {
            this.levels.remove(id);
        } else {
            this.levels.put(id, (byte)level);
        }
    }

    public void updateLevels() {
        this.applyPendingUpdates(Integer.MAX_VALUE);
    }
}

