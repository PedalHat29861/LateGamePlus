/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RegistryWorldView;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.world.tick.TickPriority;
import org.jspecify.annotations.Nullable;

public interface WorldAccess
extends RegistryWorldView,
WorldView,
ScheduledTickView {
    public long getTickOrder();

    @Override
    default public <T> OrderedTick<T> createOrderedTick(BlockPos pos, T type, int delay, TickPriority priority) {
        return new OrderedTick<T>(type, pos, this.getTime() + (long)delay, priority, this.getTickOrder());
    }

    @Override
    default public <T> OrderedTick<T> createOrderedTick(BlockPos pos, T type, int delay) {
        return new OrderedTick<T>(type, pos, this.getTime() + (long)delay, this.getTickOrder());
    }

    public WorldProperties getLevelProperties();

    default public long getTime() {
        return this.getLevelProperties().getTime();
    }

    public @Nullable MinecraftServer getServer();

    default public Difficulty getDifficulty() {
        return this.getLevelProperties().getDifficulty();
    }

    public ChunkManager getChunkManager();

    @Override
    default public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return this.getChunkManager().isChunkLoaded(chunkX, chunkZ);
    }

    public Random getRandom();

    default public void updateNeighbors(BlockPos pos, Block block) {
    }

    default public void replaceWithStateForNeighborUpdate(Direction direction, BlockPos pos, BlockPos neighborPos, BlockState neighborState, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
        NeighborUpdater.replaceWithStateForNeighborUpdate(this, direction, pos, neighborPos, neighborState, flags, maxUpdateDepth - 1);
    }

    default public void playSound(@Nullable Entity source, BlockPos pos, SoundEvent sound, SoundCategory category) {
        this.playSound(source, pos, sound, category, 1.0f, 1.0f);
    }

    public void playSound(@Nullable Entity var1, BlockPos var2, SoundEvent var3, SoundCategory var4, float var5, float var6);

    public void addParticleClient(ParticleEffect var1, double var2, double var4, double var6, double var8, double var10, double var12);

    public void syncWorldEvent(@Nullable Entity var1, int var2, BlockPos var3, int var4);

    default public void syncWorldEvent(int eventId, BlockPos pos, int data) {
        this.syncWorldEvent(null, eventId, pos, data);
    }

    public void emitGameEvent(RegistryEntry<GameEvent> var1, Vec3d var2, GameEvent.Emitter var3);

    default public void emitGameEvent(@Nullable Entity entity, RegistryEntry<GameEvent> event, Vec3d pos) {
        this.emitGameEvent(event, pos, new GameEvent.Emitter(entity, null));
    }

    default public void emitGameEvent(@Nullable Entity entity, RegistryEntry<GameEvent> event, BlockPos pos) {
        this.emitGameEvent(event, pos, new GameEvent.Emitter(entity, null));
    }

    default public void emitGameEvent(RegistryEntry<GameEvent> event, BlockPos pos, GameEvent.Emitter emitter) {
        this.emitGameEvent(event, Vec3d.ofCenter(pos), emitter);
    }

    default public void emitGameEvent(RegistryKey<GameEvent> event, BlockPos pos, GameEvent.Emitter emitter) {
        this.emitGameEvent((RegistryEntry<GameEvent>)this.getRegistryManager().getOrThrow(RegistryKeys.GAME_EVENT).getOrThrow(event), pos, emitter);
    }
}

