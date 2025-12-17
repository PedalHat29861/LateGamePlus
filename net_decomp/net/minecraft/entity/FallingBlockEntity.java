/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.Falling;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FallingBlockEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.SAND.getDefaultState();
    private static final int DEFAULT_TIME = 0;
    private static final float DEFAULT_FALL_HURT_AMOUNT = 0.0f;
    private static final int DEFAULT_FALL_HURT_MAX = 40;
    private static final boolean DEFAULT_DROP_ITEM = true;
    private static final boolean DEFAULT_DESTROYED_ON_LANDING = false;
    private BlockState blockState = DEFAULT_BLOCK_STATE;
    public int timeFalling = 0;
    public boolean dropItem = true;
    private boolean destroyedOnLanding = false;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 0.0f;
    public @Nullable NbtCompound blockEntityData;
    public boolean shouldDupe;
    protected static final TrackedData<BlockPos> BLOCK_POS = DataTracker.registerData(FallingBlockEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, World world) {
        super(entityType, world);
    }

    private FallingBlockEntity(World world, double x, double y, double z, BlockState blockState) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, world);
        this.blockState = blockState;
        this.intersectionChecked = true;
        this.setPosition(x, y, z);
        this.setVelocity(Vec3d.ZERO);
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.setFallingBlockPos(this.getBlockPos());
    }

    public static FallingBlockEntity spawnFromBlock(World world, BlockPos pos, BlockState state) {
        FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, state.contains(Properties.WATERLOGGED) ? (BlockState)state.with(Properties.WATERLOGGED, false) : state);
        world.setBlockState(pos, state.getFluidState().getBlockState(), 3);
        world.spawnEntity(fallingBlockEntity);
        return fallingBlockEntity;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (!this.isAlwaysInvulnerableTo(source)) {
            this.scheduleVelocityUpdate();
        }
        return false;
    }

    public void setFallingBlockPos(BlockPos pos) {
        this.dataTracker.set(BLOCK_POS, pos);
    }

    public BlockPos getFallingBlockPos() {
        return this.dataTracker.get(BLOCK_POS);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(BLOCK_POS, BlockPos.ORIGIN);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
            return;
        }
        Block block = this.blockState.getBlock();
        ++this.timeFalling;
        this.applyGravity();
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
        this.tickPortalTeleportation();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (this.isAlive() || this.shouldDupe) {
                BlockHitResult blockHitResult;
                BlockPos blockPos = this.getBlockPos();
                boolean bl = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean bl2 = bl && this.getEntityWorld().getFluidState(blockPos).isIn(FluidTags.WATER);
                double d = this.getVelocity().lengthSquared();
                if (bl && d > 1.0 && (blockHitResult = this.getEntityWorld().raycast(new RaycastContext(new Vec3d(this.lastX, this.lastY, this.lastZ), this.getEntityPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, this))).getType() != HitResult.Type.MISS && this.getEntityWorld().getFluidState(blockHitResult.getBlockPos()).isIn(FluidTags.WATER)) {
                    blockPos = blockHitResult.getBlockPos();
                    bl2 = true;
                }
                if (this.isOnGround() || bl2) {
                    BlockState blockState = this.getEntityWorld().getBlockState(blockPos);
                    this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
                    if (!blockState.isOf(Blocks.MOVING_PISTON)) {
                        if (!this.destroyedOnLanding) {
                            boolean bl5;
                            boolean bl3 = blockState.canReplace(new AutomaticItemPlacementContext(this.getEntityWorld(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean bl4 = FallingBlock.canFallThrough(this.getEntityWorld().getBlockState(blockPos.down())) && (!bl || !bl2);
                            boolean bl6 = bl5 = this.blockState.canPlaceAt(this.getEntityWorld(), blockPos) && !bl4;
                            if (bl3 && bl5) {
                                if (this.blockState.contains(Properties.WATERLOGGED) && this.getEntityWorld().getFluidState(blockPos).getFluid() == Fluids.WATER) {
                                    this.blockState = (BlockState)this.blockState.with(Properties.WATERLOGGED, true);
                                }
                                if (this.getEntityWorld().setBlockState(blockPos, this.blockState, 3)) {
                                    BlockEntity blockEntity;
                                    serverWorld.getChunkManager().chunkLoadingManager.sendToOtherNearbyPlayers(this, new BlockUpdateS2CPacket(blockPos, this.getEntityWorld().getBlockState(blockPos)));
                                    this.discard();
                                    if (block instanceof Falling) {
                                        Falling falling = (Falling)((Object)block);
                                        falling.onLanding(this.getEntityWorld(), blockPos, this.blockState, blockState, this);
                                    }
                                    if (this.blockEntityData != null && this.blockState.hasBlockEntity() && (blockEntity = this.getEntityWorld().getBlockEntity(blockPos)) != null) {
                                        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER);){
                                            DynamicRegistryManager dynamicRegistryManager = this.getEntityWorld().getRegistryManager();
                                            NbtWriteView nbtWriteView = NbtWriteView.create(logging, dynamicRegistryManager);
                                            blockEntity.writeDataWithoutId(nbtWriteView);
                                            NbtCompound nbtCompound = nbtWriteView.getNbt();
                                            this.blockEntityData.forEach((string, nbtElement) -> nbtCompound.put((String)string, nbtElement.copy()));
                                            blockEntity.read(NbtReadView.create(logging, dynamicRegistryManager, nbtCompound));
                                        }
                                        catch (Exception exception) {
                                            LOGGER.error("Failed to load block entity from falling block", (Throwable)exception);
                                        }
                                        blockEntity.markDirty();
                                    }
                                } else if (this.dropItem && serverWorld.getGameRules().getValue(GameRules.ENTITY_DROPS).booleanValue()) {
                                    this.discard();
                                    this.onDestroyedOnLanding(block, blockPos);
                                    this.dropItem(serverWorld, block);
                                }
                            } else {
                                this.discard();
                                if (this.dropItem && serverWorld.getGameRules().getValue(GameRules.ENTITY_DROPS).booleanValue()) {
                                    this.onDestroyedOnLanding(block, blockPos);
                                    this.dropItem(serverWorld, block);
                                }
                            }
                        } else {
                            this.discard();
                            this.onDestroyedOnLanding(block, blockPos);
                        }
                    }
                } else if (this.timeFalling > 100 && (blockPos.getY() <= this.getEntityWorld().getBottomY() || blockPos.getY() > this.getEntityWorld().getTopYInclusive()) || this.timeFalling > 600) {
                    if (this.dropItem && serverWorld.getGameRules().getValue(GameRules.ENTITY_DROPS).booleanValue()) {
                        this.dropItem(serverWorld, block);
                    }
                    this.discard();
                }
            }
        }
        this.setVelocity(this.getVelocity().multiply(0.98));
    }

    public void onDestroyedOnLanding(Block block, BlockPos pos) {
        if (block instanceof Falling) {
            ((Falling)((Object)block)).onDestroyedOnLanding(this.getEntityWorld(), pos, this);
        }
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        DamageSource damageSource2;
        if (!this.hurtEntities) {
            return false;
        }
        int i = MathHelper.ceil(fallDistance - 1.0);
        if (i < 0) {
            return false;
        }
        Predicate<Entity> predicate = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY);
        Block block = this.blockState.getBlock();
        if (block instanceof Falling) {
            Falling falling = (Falling)((Object)block);
            damageSource2 = falling.getDamageSource(this);
        } else {
            damageSource2 = this.getDamageSources().fallingBlock(this);
        }
        DamageSource damageSource22 = damageSource2;
        float f = Math.min(MathHelper.floor((float)i * this.fallHurtAmount), this.fallHurtMax);
        this.getEntityWorld().getOtherEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.serverDamage(damageSource22, f));
        boolean bl = this.blockState.isIn(BlockTags.ANVIL);
        if (bl && f > 0.0f && this.random.nextFloat() < 0.05f + (float)i * 0.05f) {
            BlockState blockState = AnvilBlock.getLandingState(this.blockState);
            if (blockState == null) {
                this.destroyedOnLanding = true;
            } else {
                this.blockState = blockState;
            }
        }
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("BlockState", BlockState.CODEC, this.blockState);
        view.putInt("Time", this.timeFalling);
        view.putBoolean("DropItem", this.dropItem);
        view.putBoolean("HurtEntities", this.hurtEntities);
        view.putFloat("FallHurtAmount", this.fallHurtAmount);
        view.putInt("FallHurtMax", this.fallHurtMax);
        if (this.blockEntityData != null) {
            view.put("TileEntityData", NbtCompound.CODEC, this.blockEntityData);
        }
        view.putBoolean("CancelDrop", this.destroyedOnLanding);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.blockState = view.read("BlockState", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE);
        this.timeFalling = view.getInt("Time", 0);
        boolean bl = this.blockState.isIn(BlockTags.ANVIL);
        this.hurtEntities = view.getBoolean("HurtEntities", bl);
        this.fallHurtAmount = view.getFloat("FallHurtAmount", 0.0f);
        this.fallHurtMax = view.getInt("FallHurtMax", 40);
        this.dropItem = view.getBoolean("DropItem", true);
        this.blockEntityData = view.read("TileEntityData", NbtCompound.CODEC).orElse(null);
        this.destroyedOnLanding = view.getBoolean("CancelDrop", false);
    }

    public void setHurtEntities(float fallHurtAmount, int fallHurtMax) {
        this.hurtEntities = true;
        this.fallHurtAmount = fallHurtAmount;
        this.fallHurtMax = fallHurtMax;
    }

    public void setDestroyedOnLanding() {
        this.destroyedOnLanding = true;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public void populateCrashReport(CrashReportSection section) {
        super.populateCrashReport(section);
        section.add("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    protected Text getDefaultName() {
        return Text.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, Block.getRawIdFromState(this.getBlockState()));
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.blockState = Block.getStateFromRawId(packet.getEntityData());
        this.intersectionChecked = true;
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.setPosition(d, e, f);
        this.setFallingBlockPos(this.getBlockPos());
    }

    @Override
    public @Nullable Entity teleportTo(TeleportTarget teleportTarget) {
        RegistryKey<World> registryKey = teleportTarget.world().getRegistryKey();
        RegistryKey<World> registryKey2 = this.getEntityWorld().getRegistryKey();
        boolean bl = (registryKey2 == World.END || registryKey == World.END) && registryKey2 != registryKey;
        Entity entity = super.teleportTo(teleportTarget);
        this.shouldDupe = entity != null && bl;
        return entity;
    }
}

