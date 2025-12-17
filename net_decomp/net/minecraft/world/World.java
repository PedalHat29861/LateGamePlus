/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.attribute.EnvironmentAttributeAccess;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.WorldEnvironmentAttributeAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.TickManager;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;

public abstract class World
implements WorldAccess,
AutoCloseable {
    public static final Codec<RegistryKey<World>> CODEC = RegistryKey.createCodec(RegistryKeys.WORLD);
    public static final RegistryKey<World> OVERWORLD = RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("overworld"));
    public static final RegistryKey<World> NETHER = RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("the_nether"));
    public static final RegistryKey<World> END = RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("the_end"));
    public static final int HORIZONTAL_LIMIT = 30000000;
    public static final int MAX_UPDATE_DEPTH = 512;
    public static final int field_30967 = 32;
    public static final int field_30968 = 15;
    public static final int MAX_Y = 20000000;
    public static final int MIN_Y = -20000000;
    private static final Pool<BlockParticleEffect> EXPLOSION_BLOCK_PARTICLES = Pool.builder().add(new BlockParticleEffect(ParticleTypes.POOF, 0.5f, 1.0f)).add(new BlockParticleEffect(ParticleTypes.SMOKE, 1.0f, 1.0f)).build();
    protected final List<BlockEntityTickInvoker> blockEntityTickers = Lists.newArrayList();
    protected final ChainRestrictedNeighborUpdater neighborUpdater;
    private final List<BlockEntityTickInvoker> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean iteratingTickingBlockEntities;
    private final Thread thread;
    private final boolean debugWorld;
    private int ambientDarkness;
    protected int lcgBlockSeed = Random.create().nextInt();
    protected final int lcgBlockSeedIncrement = 1013904223;
    protected float lastRainGradient;
    protected float rainGradient;
    protected float lastThunderGradient;
    protected float thunderGradient;
    public final Random random = Random.create();
    @Deprecated
    private final Random threadSafeRandom = Random.createThreadSafe();
    private final RegistryEntry<DimensionType> dimensionEntry;
    protected final MutableWorldProperties properties;
    private final boolean isClient;
    private final BiomeAccess biomeAccess;
    private final RegistryKey<World> registryKey;
    private final DynamicRegistryManager registryManager;
    private final DamageSources damageSources;
    private final PalettesFactory palettesFactory;
    private long tickOrder;

    protected World(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        this.properties = properties;
        this.dimensionEntry = dimensionEntry;
        this.registryKey = registryRef;
        this.isClient = isClient;
        this.thread = Thread.currentThread();
        this.biomeAccess = new BiomeAccess(this, seed);
        this.debugWorld = debugWorld;
        this.neighborUpdater = new ChainRestrictedNeighborUpdater(this, maxChainedNeighborUpdates);
        this.registryManager = registryManager;
        this.palettesFactory = PalettesFactory.fromRegistryManager(registryManager);
        this.damageSources = new DamageSources(registryManager);
    }

    @Override
    public boolean isClient() {
        return this.isClient;
    }

    @Override
    public @Nullable MinecraftServer getServer() {
        return null;
    }

    public boolean isInBuildLimit(BlockPos pos) {
        return !this.isOutOfHeightLimit(pos) && World.isValidHorizontally(pos);
    }

    public boolean isInLoadLimit(BlockPos pos) {
        return !this.isOutOfHeightLimit(pos) && World.isChunkLoadable(pos);
    }

    public static boolean isValid(BlockPos pos) {
        return !World.isInvalidVertically(pos.getY()) && World.isValidHorizontally(pos);
    }

    private static boolean isValidHorizontally(BlockPos pos) {
        return pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
    }

    private static boolean isChunkLoadable(BlockPos pos) {
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        int j = ChunkSectionPos.getSectionCoord(pos.getZ());
        return ChunkPos.isLoadable(i, j);
    }

    private static boolean isInvalidVertically(int y) {
        return y < -20000000 || y >= 20000000;
    }

    public WorldChunk getWorldChunk(BlockPos pos) {
        return this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    @Override
    public WorldChunk getChunk(int i, int j) {
        return (WorldChunk)this.getChunk(i, j, ChunkStatus.FULL);
    }

    @Override
    public @Nullable Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        Chunk chunk = this.getChunkManager().getChunk(chunkX, chunkZ, leastStatus, create);
        if (chunk == null && create) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        return chunk;
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, @Block.SetBlockStateFlag int flags) {
        return this.setBlockState(pos, state, flags, 512);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
        if (!this.isInLoadLimit(pos)) {
            return false;
        }
        if (!this.isClient() && this.isDebugWorld()) {
            return false;
        }
        WorldChunk worldChunk = this.getWorldChunk(pos);
        Block block = state.getBlock();
        BlockState blockState = worldChunk.setBlockState(pos, state, flags);
        if (blockState != null) {
            BlockState blockState2 = this.getBlockState(pos);
            if (blockState2 == state) {
                if (blockState != blockState2) {
                    this.scheduleBlockRerenderIfNeeded(pos, blockState, blockState2);
                }
                if ((flags & 2) != 0 && (!this.isClient() || (flags & 4) == 0) && (this.isClient() || worldChunk.getLevelType() != null && worldChunk.getLevelType().isAfter(ChunkLevelType.BLOCK_TICKING))) {
                    this.updateListeners(pos, blockState, state, flags);
                }
                if ((flags & 1) != 0) {
                    this.updateNeighbors(pos, blockState.getBlock());
                    if (!this.isClient() && state.hasComparatorOutput()) {
                        this.updateComparators(pos, block);
                    }
                }
                if ((flags & 0x10) == 0 && maxUpdateDepth > 0) {
                    int i = flags & 0xFFFFFFDE;
                    blockState.prepare(this, pos, i, maxUpdateDepth - 1);
                    state.updateNeighbors(this, pos, i, maxUpdateDepth - 1);
                    state.prepare(this, pos, i, maxUpdateDepth - 1);
                }
                this.onBlockStateChanged(pos, blockState, blockState2);
            }
            return true;
        }
        return false;
    }

    public void onBlockStateChanged(BlockPos pos, BlockState oldState, BlockState newState) {
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean move) {
        FluidState fluidState = this.getFluidState(pos);
        return this.setBlockState(pos, fluidState.getBlockState(), 3 | (move ? 64 : 0));
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth) {
        boolean bl;
        BlockState blockState = this.getBlockState(pos);
        if (blockState.isAir()) {
            return false;
        }
        FluidState fluidState = this.getFluidState(pos);
        if (!(blockState.getBlock() instanceof AbstractFireBlock)) {
            this.syncWorldEvent(2001, pos, Block.getRawIdFromState(blockState));
        }
        if (drop) {
            BlockEntity blockEntity = blockState.hasBlockEntity() ? this.getBlockEntity(pos) : null;
            Block.dropStacks(blockState, this, pos, blockEntity, breakingEntity, ItemStack.EMPTY);
        }
        if (bl = this.setBlockState(pos, fluidState.getBlockState(), 3, maxUpdateDepth)) {
            this.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(breakingEntity, blockState));
        }
        return bl;
    }

    public void addBlockBreakParticles(BlockPos pos, BlockState state) {
    }

    public boolean setBlockState(BlockPos pos, BlockState state) {
        return this.setBlockState(pos, state, 3);
    }

    public abstract void updateListeners(BlockPos var1, BlockState var2, BlockState var3, @Block.SetBlockStateFlag int var4);

    public void scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated) {
    }

    public void updateNeighborsAlways(BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation) {
    }

    public void updateNeighborsExcept(BlockPos pos, Block sourceBlock, Direction direction, @Nullable WireOrientation orientation) {
    }

    public void updateNeighbor(BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation) {
    }

    public void updateNeighbor(BlockState state, BlockPos pos, Block sourceBlock, @Nullable WireOrientation orientation, boolean notify) {
    }

    @Override
    public void replaceWithStateForNeighborUpdate(Direction direction, BlockPos pos, BlockPos neighborPos, BlockState neighborState, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
        this.neighborUpdater.replaceWithStateForNeighborUpdate(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth);
    }

    @Override
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        int i = x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 ? this.getSeaLevel() + 1 : (this.isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)) ? this.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z)).sampleHeightmap(heightmap, x & 0xF, z & 0xF) + 1 : this.getBottomY());
        return i;
    }

    @Override
    public LightingProvider getLightingProvider() {
        return this.getChunkManager().getLightingProvider();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!this.isInLoadLimit(pos)) {
            return Blocks.VOID_AIR.getDefaultState();
        }
        WorldChunk worldChunk = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
        return worldChunk.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (!this.isInLoadLimit(pos)) {
            return Fluids.EMPTY.getDefaultState();
        }
        WorldChunk worldChunk = this.getWorldChunk(pos);
        return worldChunk.getFluidState(pos);
    }

    public boolean isDay() {
        return !this.getDimension().hasFixedTime() && this.ambientDarkness < 4;
    }

    public boolean isNight() {
        return !this.getDimension().hasFixedTime() && !this.isDay();
    }

    @Override
    public void playSound(@Nullable Entity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.playSound(source, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch);
    }

    public abstract void playSound(@Nullable Entity var1, double var2, double var4, double var6, RegistryEntry<SoundEvent> var8, SoundCategory var9, float var10, float var11, long var12);

    public void playSound(@Nullable Entity source, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {
        this.playSound(source, x, y, z, Registries.SOUND_EVENT.getEntry(sound), category, volume, pitch, seed);
    }

    public abstract void playSoundFromEntity(@Nullable Entity var1, Entity var2, RegistryEntry<SoundEvent> var3, SoundCategory var4, float var5, float var6, long var7);

    public void playSound(@Nullable Entity source, double x, double y, double z, SoundEvent sound, SoundCategory category) {
        this.playSound(source, x, y, z, sound, category, 1.0f, 1.0f);
    }

    public void playSound(@Nullable Entity source, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.playSound(source, x, y, z, sound, category, volume, pitch, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable Entity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch) {
        this.playSound(source, x, y, z, sound, category, volume, pitch, this.threadSafeRandom.nextLong());
    }

    public void playSoundFromEntity(@Nullable Entity source, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.playSoundFromEntity(source, entity, Registries.SOUND_EVENT.getEntry(sound), category, volume, pitch, this.threadSafeRandom.nextLong());
    }

    public void playSoundAtBlockCenterClient(BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
        this.playSoundClient((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, sound, category, volume, pitch, useDistance);
    }

    public void playSoundFromEntityClient(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    public void playSoundClient(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
    }

    public void playSoundClient(SoundEvent sound, SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void addParticleClient(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addParticleClient(ParticleEffect parameters, boolean force, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addImportantParticleClient(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addImportantParticleClient(ParticleEffect parameters, boolean force, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
    }

    public void addBlockEntityTicker(BlockEntityTickInvoker ticker) {
        (this.iteratingTickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(ticker);
    }

    public void tickBlockEntities() {
        this.iteratingTickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }
        Iterator<BlockEntityTickInvoker> iterator = this.blockEntityTickers.iterator();
        boolean bl = this.getTickManager().shouldTick();
        while (iterator.hasNext()) {
            BlockEntityTickInvoker blockEntityTickInvoker = iterator.next();
            if (blockEntityTickInvoker.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!bl || !this.shouldTickBlockPos(blockEntityTickInvoker.getPos())) continue;
            blockEntityTickInvoker.tick();
        }
        this.iteratingTickingBlockEntities = false;
    }

    public <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity) {
        try {
            tickConsumer.accept(entity);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Ticking entity");
            CrashReportSection crashReportSection = crashReport.addElement("Entity being ticked");
            entity.populateCrashReport(crashReportSection);
            throw new CrashException(crashReport);
        }
    }

    public boolean shouldUpdatePostDeath(Entity entity) {
        return true;
    }

    public boolean shouldTickBlocksInChunk(long chunkPos) {
        return true;
    }

    public boolean shouldTickBlockPos(BlockPos pos) {
        return this.shouldTickBlocksInChunk(ChunkPos.toLong(pos));
    }

    public void createExplosion(@Nullable Entity entity, double x, double y, double z, float power, ExplosionSourceType explosionSourceType) {
        this.createExplosion(entity, Explosion.createDamageSource(this, entity), null, x, y, z, power, false, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, EXPLOSION_BLOCK_PARTICLES, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public void createExplosion(@Nullable Entity entity, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
        this.createExplosion(entity, Explosion.createDamageSource(this, entity), null, x, y, z, power, createFire, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, EXPLOSION_BLOCK_PARTICLES, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public void createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, Vec3d pos, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
        this.createExplosion(entity, damageSource, behavior, pos.getX(), pos.getY(), pos.getZ(), power, createFire, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, EXPLOSION_BLOCK_PARTICLES, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public void createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType) {
        this.createExplosion(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, EXPLOSION_BLOCK_PARTICLES, SoundEvents.ENTITY_GENERIC_EXPLODE);
    }

    public abstract void createExplosion(@Nullable Entity var1, @Nullable DamageSource var2, @Nullable ExplosionBehavior var3, double var4, double var6, double var8, float var10, boolean var11, ExplosionSourceType var12, ParticleEffect var13, ParticleEffect var14, Pool<BlockParticleEffect> var15, RegistryEntry<SoundEvent> var16);

    public abstract String asString();

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        if (!this.isInLoadLimit(pos)) {
            return null;
        }
        if (!this.isClient() && Thread.currentThread() != this.thread) {
            return null;
        }
        return this.getWorldChunk(pos).getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getPos();
        if (!this.isInLoadLimit(blockPos)) {
            return;
        }
        this.getWorldChunk(blockPos).addBlockEntity(blockEntity);
    }

    public void removeBlockEntity(BlockPos pos) {
        if (!this.isInLoadLimit(pos)) {
            return;
        }
        this.getWorldChunk(pos).removeBlockEntity(pos);
    }

    public boolean isPosLoaded(BlockPos pos) {
        if (!this.isInLoadLimit(pos)) {
            return false;
        }
        return this.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
    }

    public boolean isDirectionSolid(BlockPos pos, Entity entity, Direction direction) {
        if (!this.isInLoadLimit(pos)) {
            return false;
        }
        Chunk chunk = this.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        if (chunk == null) {
            return false;
        }
        return chunk.getBlockState(pos).isSolidSurface(this, pos, entity, direction);
    }

    public boolean isTopSolid(BlockPos pos, Entity entity) {
        return this.isDirectionSolid(pos, entity, Direction.UP);
    }

    public void calculateAmbientDarkness() {
        this.ambientDarkness = (int)(15.0f - this.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.SKY_LIGHT_LEVEL_GAMEPLAY).floatValue());
    }

    public void setMobSpawnOptions(boolean spawnMonsters) {
        this.getChunkManager().setMobSpawnOptions(spawnMonsters);
    }

    public abstract void setSpawnPoint(WorldProperties.SpawnPoint var1);

    public abstract WorldProperties.SpawnPoint getSpawnPoint();

    public WorldProperties.SpawnPoint ensureWithinBorder(WorldProperties.SpawnPoint spawnPoint) {
        WorldBorder worldBorder = this.getWorldBorder();
        if (!worldBorder.contains(spawnPoint.getPos())) {
            BlockPos blockPos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(worldBorder.getCenterX(), 0.0, worldBorder.getCenterZ()));
            return WorldProperties.SpawnPoint.create(spawnPoint.getDimension(), blockPos, spawnPoint.yaw(), spawnPoint.pitch());
        }
        return spawnPoint;
    }

    protected void initWeatherGradients() {
        if (this.properties.isRaining()) {
            this.rainGradient = 1.0f;
            if (this.properties.isThundering()) {
                this.thunderGradient = 1.0f;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.getChunkManager().close();
    }

    @Override
    public @Nullable BlockView getChunkAsView(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
        Profilers.get().visit("getEntities");
        ArrayList list = Lists.newArrayList();
        this.getEntityLookup().forEachIntersects(box, entity -> {
            if (entity != except && predicate.test((Entity)entity)) {
                list.add(entity);
            }
        });
        for (EnderDragonPart enderDragonPart : this.getEnderDragonParts()) {
            if (enderDragonPart == except || enderDragonPart.owner == except || !predicate.test(enderDragonPart) || !box.intersects(enderDragonPart.getBoundingBox())) continue;
            list.add(enderDragonPart);
        }
        return list;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        ArrayList list = Lists.newArrayList();
        this.collectEntitiesByType(filter, box, predicate, list);
        return list;
    }

    public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, List<? super T> result) {
        this.collectEntitiesByType(filter, box, predicate, result, Integer.MAX_VALUE);
    }

    public <T extends Entity> void collectEntitiesByType(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate, List<? super T> result, int limit) {
        Profilers.get().visit("getEntities");
        this.getEntityLookup().forEachIntersects(filter, box, entity -> {
            if (predicate.test(entity)) {
                result.add((Object)entity);
                if (result.size() >= limit) {
                    return LazyIterationConsumer.NextIteration.ABORT;
                }
            }
            if (entity instanceof EnderDragonEntity) {
                EnderDragonEntity enderDragonEntity = (EnderDragonEntity)entity;
                for (EnderDragonPart enderDragonPart : enderDragonEntity.getBodyParts()) {
                    Entity entity2 = (Entity)filter.downcast(enderDragonPart);
                    if (entity2 == null || !predicate.test(entity2)) continue;
                    result.add((Object)entity2);
                    if (result.size() < limit) continue;
                    return LazyIterationConsumer.NextIteration.ABORT;
                }
            }
            return LazyIterationConsumer.NextIteration.CONTINUE;
        });
    }

    public <T extends Entity> boolean hasEntities(TypeFilter<Entity, T> filter, Box box, Predicate<? super T> predicate) {
        Profilers.get().visit("hasEntities");
        MutableBoolean mutableBoolean = new MutableBoolean();
        this.getEntityLookup().forEachIntersects(filter, box, entity -> {
            if (predicate.test(entity)) {
                mutableBoolean.setTrue();
                return LazyIterationConsumer.NextIteration.ABORT;
            }
            if (entity instanceof EnderDragonEntity) {
                EnderDragonEntity enderDragonEntity = (EnderDragonEntity)entity;
                for (EnderDragonPart enderDragonPart : enderDragonEntity.getBodyParts()) {
                    Entity entity2 = (Entity)filter.downcast(enderDragonPart);
                    if (entity2 == null || !predicate.test(entity2)) continue;
                    mutableBoolean.setTrue();
                    return LazyIterationConsumer.NextIteration.ABORT;
                }
            }
            return LazyIterationConsumer.NextIteration.CONTINUE;
        });
        return mutableBoolean.isTrue();
    }

    public List<Entity> getCrammedEntities(Entity entity, Box box) {
        return this.getOtherEntities(entity, box, EntityPredicates.canBePushedBy(entity));
    }

    public abstract @Nullable Entity getEntityById(int var1);

    public @Nullable Entity getEntity(UUID uuid) {
        return this.getEntityLookup().get(uuid);
    }

    public @Nullable Entity getEntityAnyDimension(UUID uuid) {
        return this.getEntity(uuid);
    }

    public @Nullable PlayerEntity getPlayerAnyDimension(UUID uuid) {
        return this.getPlayerByUuid(uuid);
    }

    public abstract Collection<EnderDragonPart> getEnderDragonParts();

    public void markDirty(BlockPos pos) {
        if (this.isChunkLoaded(pos)) {
            this.getWorldChunk(pos).markNeedsSaving();
        }
    }

    public void loadBlockEntity(BlockEntity blockEntity) {
    }

    public long getTimeOfDay() {
        return this.properties.getTimeOfDay();
    }

    public boolean canEntityModifyAt(Entity entity, BlockPos pos) {
        return true;
    }

    public void sendEntityStatus(Entity entity, byte status) {
    }

    public void sendEntityDamage(Entity entity, DamageSource damageSource) {
    }

    public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
        this.getBlockState(pos).onSyncedBlockEvent(this, pos, type, data);
    }

    @Override
    public WorldProperties getLevelProperties() {
        return this.properties;
    }

    public abstract TickManager getTickManager();

    public float getThunderGradient(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastThunderGradient, this.thunderGradient) * this.getRainGradient(tickProgress);
    }

    public void setThunderGradient(float thunderGradient) {
        float f;
        this.lastThunderGradient = f = MathHelper.clamp(thunderGradient, 0.0f, 1.0f);
        this.thunderGradient = f;
    }

    public float getRainGradient(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastRainGradient, this.rainGradient);
    }

    public void setRainGradient(float rainGradient) {
        float f;
        this.lastRainGradient = f = MathHelper.clamp(rainGradient, 0.0f, 1.0f);
        this.rainGradient = f;
    }

    public boolean canHaveWeather() {
        return this.getDimension().hasSkyLight() && !this.getDimension().hasCeiling() && this.getRegistryKey() != END;
    }

    public boolean isThundering() {
        return this.canHaveWeather() && (double)this.getThunderGradient(1.0f) > 0.9;
    }

    public boolean isRaining() {
        return this.canHaveWeather() && (double)this.getRainGradient(1.0f) > 0.2;
    }

    public boolean hasRain(BlockPos pos) {
        return this.getPrecipitation(pos) == Biome.Precipitation.RAIN;
    }

    public Biome.Precipitation getPrecipitation(BlockPos pos) {
        if (!this.isRaining()) {
            return Biome.Precipitation.NONE;
        }
        if (!this.isSkyVisible(pos)) {
            return Biome.Precipitation.NONE;
        }
        if (this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() > pos.getY()) {
            return Biome.Precipitation.NONE;
        }
        Biome biome = this.getBiome(pos).value();
        return biome.getPrecipitation(pos, this.getSeaLevel());
    }

    public abstract @Nullable MapState getMapState(MapIdComponent var1);

    public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
    }

    public CrashReportSection addDetailsToCrashReport(CrashReport report) {
        CrashReportSection crashReportSection = report.addElement("Affected level", 1);
        crashReportSection.add("All players", () -> {
            List<? extends PlayerEntity> list = this.getPlayers();
            return list.size() + " total; " + list.stream().map(PlayerEntity::asString).collect(Collectors.joining(", "));
        });
        crashReportSection.add("Chunk stats", this.getChunkManager()::getDebugString);
        crashReportSection.add("Level dimension", () -> this.getRegistryKey().getValue().toString());
        try {
            this.properties.populateCrashReport(crashReportSection, this);
        }
        catch (Throwable throwable) {
            crashReportSection.add("Level Data Unobtainable", throwable);
        }
        return crashReportSection;
    }

    public abstract void setBlockBreakingInfo(int var1, BlockPos var2, int var3);

    public void addFireworkParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, List<FireworkExplosionComponent> explosions) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateComparators(BlockPos pos, Block block) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            if (!this.isChunkLoaded(blockPos)) continue;
            BlockState blockState = this.getBlockState(blockPos);
            if (blockState.isOf(Blocks.COMPARATOR)) {
                this.updateNeighbor(blockState, blockPos, block, null, false);
                continue;
            }
            if (!blockState.isSolidBlock(this, blockPos) || !(blockState = this.getBlockState(blockPos = blockPos.offset(direction))).isOf(Blocks.COMPARATOR)) continue;
            this.updateNeighbor(blockState, blockPos, block, null, false);
        }
    }

    @Override
    public int getAmbientDarkness() {
        return this.ambientDarkness;
    }

    public void setLightningTicksLeft(int lightningTicksLeft) {
    }

    public void sendPacket(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override
    public DimensionType getDimension() {
        return this.dimensionEntry.value();
    }

    public RegistryEntry<DimensionType> getDimensionEntry() {
        return this.dimensionEntry;
    }

    public RegistryKey<World> getRegistryKey() {
        return this.registryKey;
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> state) {
        return state.test(this.getBlockState(pos));
    }

    @Override
    public boolean testFluidState(BlockPos pos, Predicate<FluidState> state) {
        return state.test(this.getFluidState(pos));
    }

    public abstract RecipeManager getRecipeManager();

    public BlockPos getRandomPosInChunk(int x, int y, int z, int yMask) {
        this.lcgBlockSeed = this.lcgBlockSeed * 3 + 1013904223;
        int i = this.lcgBlockSeed >> 2;
        return new BlockPos(x + (i & 0xF), y + (i >> 16 & yMask), z + (i >> 8 & 0xF));
    }

    public boolean isSavingDisabled() {
        return false;
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return this.biomeAccess;
    }

    public final boolean isDebugWorld() {
        return this.debugWorld;
    }

    protected abstract EntityLookup<Entity> getEntityLookup();

    @Override
    public long getTickOrder() {
        return this.tickOrder++;
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.registryManager;
    }

    public DamageSources getDamageSources() {
        return this.damageSources;
    }

    @Override
    public abstract WorldEnvironmentAttributeAccess getEnvironmentAttributes();

    public abstract BrewingRecipeRegistry getBrewingRecipeRegistry();

    public abstract FuelRegistry getFuelRegistry();

    public int getBlockColor(BlockPos pos) {
        return 0;
    }

    public PalettesFactory getPalettesFactory() {
        return this.palettesFactory;
    }

    @Override
    public /* synthetic */ EnvironmentAttributeAccess getEnvironmentAttributes() {
        return this.getEnvironmentAttributes();
    }

    @Override
    public /* synthetic */ Chunk getChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ);
    }

    public static final class ExplosionSourceType
    extends Enum<ExplosionSourceType>
    implements StringIdentifiable {
        public static final /* enum */ ExplosionSourceType NONE = new ExplosionSourceType("none");
        public static final /* enum */ ExplosionSourceType BLOCK = new ExplosionSourceType("block");
        public static final /* enum */ ExplosionSourceType MOB = new ExplosionSourceType("mob");
        public static final /* enum */ ExplosionSourceType TNT = new ExplosionSourceType("tnt");
        public static final /* enum */ ExplosionSourceType TRIGGER = new ExplosionSourceType("trigger");
        public static final Codec<ExplosionSourceType> CODEC;
        private final String id;
        private static final /* synthetic */ ExplosionSourceType[] field_40892;

        public static ExplosionSourceType[] values() {
            return (ExplosionSourceType[])field_40892.clone();
        }

        public static ExplosionSourceType valueOf(String string) {
            return Enum.valueOf(ExplosionSourceType.class, string);
        }

        private ExplosionSourceType(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ ExplosionSourceType[] method_46670() {
            return new ExplosionSourceType[]{NONE, BLOCK, MOB, TNT, TRIGGER};
        }

        static {
            field_40892 = ExplosionSourceType.method_46670();
            CODEC = StringIdentifiable.createCodec(ExplosionSourceType::values);
        }
    }
}

