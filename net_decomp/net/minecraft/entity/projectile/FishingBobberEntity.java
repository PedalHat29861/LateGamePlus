/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.projectile;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FishingBobberEntity
extends ProjectileEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Random velocityRandom = Random.create();
    private boolean caughtFish;
    private int outOfOpenWaterTicks;
    private static final int field_30665 = 10;
    private static final TrackedData<Integer> HOOK_ENTITY_ID = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CAUGHT_FISH = DataTracker.registerData(FishingBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int removalTimer;
    private int hookCountdown;
    private int waitCountdown;
    private int fishTravelCountdown;
    private float fishAngle;
    private boolean inOpenWater = true;
    private @Nullable Entity hookedEntity;
    private State state = State.FLYING;
    private final int luckBonus;
    private final int waitTimeReductionTicks;
    private final PositionInterpolator positionInterpolator = new PositionInterpolator(this);

    private FishingBobberEntity(EntityType<? extends FishingBobberEntity> type, World world, int luckBonus, int waitTimeReductionTicks) {
        super((EntityType<? extends ProjectileEntity>)type, world);
        this.luckBonus = Math.max(0, luckBonus);
        this.waitTimeReductionTicks = Math.max(0, waitTimeReductionTicks);
    }

    public FishingBobberEntity(EntityType<? extends FishingBobberEntity> entityType, World world) {
        this(entityType, world, 0, 0);
    }

    public FishingBobberEntity(PlayerEntity thrower, World world, int luckBonus, int waitTimeReductionTicks) {
        this(EntityType.FISHING_BOBBER, world, luckBonus, waitTimeReductionTicks);
        this.setOwner(thrower);
        float f = thrower.getPitch();
        float g = thrower.getYaw();
        float h = MathHelper.cos(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float i = MathHelper.sin(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float j = -MathHelper.cos(-f * ((float)Math.PI / 180));
        float k = MathHelper.sin(-f * ((float)Math.PI / 180));
        double d = thrower.getX() - (double)i * 0.3;
        double e = thrower.getEyeY();
        double l = thrower.getZ() - (double)h * 0.3;
        this.refreshPositionAndAngles(d, e, l, g, f);
        Vec3d vec3d = new Vec3d(-i, MathHelper.clamp(-(k / j), -5.0f, 5.0f), -h);
        double m = vec3d.length();
        vec3d = vec3d.multiply(0.6 / m + this.random.nextTriangular(0.5, 0.0103365), 0.6 / m + this.random.nextTriangular(0.5, 0.0103365), 0.6 / m + this.random.nextTriangular(0.5, 0.0103365));
        this.setVelocity(vec3d);
        this.setYaw((float)(MathHelper.atan2(vec3d.x, vec3d.z) * 57.2957763671875));
        this.setPitch((float)(MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 57.2957763671875));
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.positionInterpolator;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HOOK_ENTITY_ID, 0);
        builder.add(CAUGHT_FISH, false);
    }

    @Override
    protected boolean deflectsAgainstWorldBorder() {
        return true;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (HOOK_ENTITY_ID.equals(data)) {
            int i = this.getDataTracker().get(HOOK_ENTITY_ID);
            Entity entity = this.hookedEntity = i > 0 ? this.getEntityWorld().getEntityById(i - 1) : null;
        }
        if (CAUGHT_FISH.equals(data)) {
            this.caughtFish = this.getDataTracker().get(CAUGHT_FISH);
            if (this.caughtFish) {
                this.setVelocity(this.getVelocity().x, -0.4f * MathHelper.nextFloat(this.velocityRandom, 0.6f, 1.0f), this.getVelocity().z);
            }
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 64.0;
        return distance < 4096.0;
    }

    @Override
    public void tick() {
        boolean bl;
        this.velocityRandom.setSeed(this.getUuid().getLeastSignificantBits() ^ this.getEntityWorld().getTime());
        this.getInterpolator().tick();
        super.tick();
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity == null) {
            this.discard();
            return;
        }
        if (!this.getEntityWorld().isClient() && this.removeIfInvalid(playerEntity)) {
            return;
        }
        if (this.isOnGround()) {
            ++this.removalTimer;
            if (this.removalTimer >= 1200) {
                this.discard();
                return;
            }
        } else {
            this.removalTimer = 0;
        }
        float f = 0.0f;
        BlockPos blockPos = this.getBlockPos();
        FluidState fluidState = this.getEntityWorld().getFluidState(blockPos);
        if (fluidState.isIn(FluidTags.WATER)) {
            f = fluidState.getHeight(this.getEntityWorld(), blockPos);
        }
        boolean bl2 = bl = f > 0.0f;
        if (this.state == State.FLYING) {
            if (this.hookedEntity != null) {
                this.setVelocity(Vec3d.ZERO);
                this.state = State.HOOKED_IN_ENTITY;
                return;
            }
            if (bl) {
                this.setVelocity(this.getVelocity().multiply(0.3, 0.2, 0.3));
                this.state = State.BOBBING;
                return;
            }
            this.checkForCollision();
        } else {
            if (this.state == State.HOOKED_IN_ENTITY) {
                if (this.hookedEntity != null) {
                    if (this.hookedEntity.isRemoved() || !this.hookedEntity.isInteractable() || this.hookedEntity.getEntityWorld().getRegistryKey() != this.getEntityWorld().getRegistryKey()) {
                        this.updateHookedEntityId(null);
                        this.state = State.FLYING;
                    } else {
                        this.setPosition(this.hookedEntity.getX(), this.hookedEntity.getBodyY(0.8), this.hookedEntity.getZ());
                    }
                }
                return;
            }
            if (this.state == State.BOBBING) {
                Vec3d vec3d = this.getVelocity();
                double d = this.getY() + vec3d.y - (double)blockPos.getY() - (double)f;
                if (Math.abs(d) < 0.01) {
                    d += Math.signum(d) * 0.1;
                }
                this.setVelocity(vec3d.x * 0.9, vec3d.y - d * (double)this.random.nextFloat() * 0.2, vec3d.z * 0.9);
                this.inOpenWater = this.hookCountdown > 0 || this.fishTravelCountdown > 0 ? this.inOpenWater && this.outOfOpenWaterTicks < 10 && this.isOpenOrWaterAround(blockPos) : true;
                if (bl) {
                    this.outOfOpenWaterTicks = Math.max(0, this.outOfOpenWaterTicks - 1);
                    if (this.caughtFish) {
                        this.setVelocity(this.getVelocity().add(0.0, -0.1 * (double)this.velocityRandom.nextFloat() * (double)this.velocityRandom.nextFloat(), 0.0));
                    }
                    if (!this.getEntityWorld().isClient()) {
                        this.tickFishingLogic(blockPos);
                    }
                } else {
                    this.outOfOpenWaterTicks = Math.min(10, this.outOfOpenWaterTicks + 1);
                }
            }
        }
        if (!fluidState.isIn(FluidTags.WATER) && !this.isOnGround() && this.hookedEntity == null) {
            this.setVelocity(this.getVelocity().add(0.0, -0.03, 0.0));
        }
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
        this.updateRotation();
        if (this.state == State.FLYING && (this.isOnGround() || this.horizontalCollision)) {
            this.setVelocity(Vec3d.ZERO);
        }
        double e = 0.92;
        this.setVelocity(this.getVelocity().multiply(0.92));
        this.refreshPosition();
    }

    private boolean removeIfInvalid(PlayerEntity player) {
        if (player.isInteractable()) {
            ItemStack itemStack = player.getMainHandStack();
            ItemStack itemStack2 = player.getOffHandStack();
            boolean bl = itemStack.isOf(Items.FISHING_ROD);
            boolean bl2 = itemStack2.isOf(Items.FISHING_ROD);
            if ((bl || bl2) && this.squaredDistanceTo(player) <= 1024.0) {
                return false;
            }
        }
        this.discard();
        return true;
    }

    private void checkForCollision() {
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.hitOrDeflect(hitResult);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getEntityWorld().isClient()) {
            this.updateHookedEntityId(entityHitResult.getEntity());
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
    }

    private void updateHookedEntityId(@Nullable Entity entity) {
        this.hookedEntity = entity;
        this.getDataTracker().set(HOOK_ENTITY_ID, entity == null ? 0 : entity.getId() + 1);
    }

    private void tickFishingLogic(BlockPos pos) {
        ServerWorld serverWorld = (ServerWorld)this.getEntityWorld();
        int i = 1;
        BlockPos blockPos = pos.up();
        if (this.random.nextFloat() < 0.25f && this.getEntityWorld().hasRain(blockPos)) {
            ++i;
        }
        if (this.random.nextFloat() < 0.5f && !this.getEntityWorld().isSkyVisible(blockPos)) {
            --i;
        }
        if (this.hookCountdown > 0) {
            --this.hookCountdown;
            if (this.hookCountdown <= 0) {
                this.waitCountdown = 0;
                this.fishTravelCountdown = 0;
                this.getDataTracker().set(CAUGHT_FISH, false);
            }
        } else if (this.fishTravelCountdown > 0) {
            this.fishTravelCountdown -= i;
            if (this.fishTravelCountdown > 0) {
                double j;
                double e;
                this.fishAngle += (float)this.random.nextTriangular(0.0, 9.188);
                float f = this.fishAngle * ((float)Math.PI / 180);
                float g = MathHelper.sin(f);
                float h = MathHelper.cos(f);
                double d = this.getX() + (double)(g * (float)this.fishTravelCountdown * 0.1f);
                BlockState blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, (e = (double)((float)MathHelper.floor(this.getY()) + 1.0f)) - 1.0, j = this.getZ() + (double)(h * (float)this.fishTravelCountdown * 0.1f)));
                if (blockState.isOf(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15f) {
                        serverWorld.spawnParticles(ParticleTypes.BUBBLE, d, e - (double)0.1f, j, 1, g, 0.1, h, 0.0);
                    }
                    float k = g * 0.04f;
                    float l = h * 0.04f;
                    serverWorld.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, l, 0.01, -k, 1.0);
                    serverWorld.spawnParticles(ParticleTypes.FISHING, d, e, j, 0, -l, 0.01, k, 1.0);
                }
            } else {
                this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                double m = this.getY() + 0.5;
                serverWorld.spawnParticles(ParticleTypes.BUBBLE, this.getX(), m, this.getZ(), (int)(1.0f + this.getWidth() * 20.0f), this.getWidth(), 0.0, this.getWidth(), 0.2f);
                serverWorld.spawnParticles(ParticleTypes.FISHING, this.getX(), m, this.getZ(), (int)(1.0f + this.getWidth() * 20.0f), this.getWidth(), 0.0, this.getWidth(), 0.2f);
                this.hookCountdown = MathHelper.nextInt(this.random, 20, 40);
                this.getDataTracker().set(CAUGHT_FISH, true);
            }
        } else if (this.waitCountdown > 0) {
            this.waitCountdown -= i;
            float f = 0.15f;
            if (this.waitCountdown < 20) {
                f += (float)(20 - this.waitCountdown) * 0.05f;
            } else if (this.waitCountdown < 40) {
                f += (float)(40 - this.waitCountdown) * 0.02f;
            } else if (this.waitCountdown < 60) {
                f += (float)(60 - this.waitCountdown) * 0.01f;
            }
            if (this.random.nextFloat() < f) {
                double j;
                double e;
                float g = MathHelper.nextFloat(this.random, 0.0f, 360.0f) * ((float)Math.PI / 180);
                float h = MathHelper.nextFloat(this.random, 25.0f, 60.0f);
                double d = this.getX() + (double)(MathHelper.sin(g) * h) * 0.1;
                BlockState blockState = serverWorld.getBlockState(BlockPos.ofFloored(d, (e = (double)((float)MathHelper.floor(this.getY()) + 1.0f)) - 1.0, j = this.getZ() + (double)(MathHelper.cos(g) * h) * 0.1));
                if (blockState.isOf(Blocks.WATER)) {
                    serverWorld.spawnParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.1f, 0.0, 0.1f, 0.0);
                }
            }
            if (this.waitCountdown <= 0) {
                this.fishAngle = MathHelper.nextFloat(this.random, 0.0f, 360.0f);
                this.fishTravelCountdown = MathHelper.nextInt(this.random, 20, 80);
            }
        } else {
            this.waitCountdown = MathHelper.nextInt(this.random, 100, 600);
            this.waitCountdown -= this.waitTimeReductionTicks;
        }
    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        PositionType positionType = PositionType.INVALID;
        for (int i = -1; i <= 2; ++i) {
            PositionType positionType2 = this.getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2));
            switch (positionType2.ordinal()) {
                case 2: {
                    return false;
                }
                case 0: {
                    if (positionType != PositionType.INVALID) break;
                    return false;
                }
                case 1: {
                    if (positionType != PositionType.ABOVE_WATER) break;
                    return false;
                }
            }
            positionType = positionType2;
        }
        return true;
    }

    private PositionType getPositionType(BlockPos start, BlockPos end) {
        return BlockPos.stream(start, end).map(this::getPositionType).reduce((positionType, positionType2) -> positionType == positionType2 ? positionType : PositionType.INVALID).orElse(PositionType.INVALID);
    }

    private PositionType getPositionType(BlockPos pos) {
        BlockState blockState = this.getEntityWorld().getBlockState(pos);
        if (blockState.isAir() || blockState.isOf(Blocks.LILY_PAD)) {
            return PositionType.ABOVE_WATER;
        }
        FluidState fluidState = blockState.getFluidState();
        if (fluidState.isIn(FluidTags.WATER) && fluidState.isStill() && blockState.getCollisionShape(this.getEntityWorld(), pos).isEmpty()) {
            return PositionType.INSIDE_WATER;
        }
        return PositionType.INVALID;
    }

    public boolean isInOpenWater() {
        return this.inOpenWater;
    }

    @Override
    protected void writeCustomData(WriteView view) {
    }

    @Override
    protected void readCustomData(ReadView view) {
    }

    public int use(ItemStack usedItem) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (this.getEntityWorld().isClient() || playerEntity == null || this.removeIfInvalid(playerEntity)) {
            return 0;
        }
        int i = 0;
        if (this.hookedEntity != null) {
            this.pullHookedEntity(this.hookedEntity);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this, Collections.emptyList());
            this.getEntityWorld().sendEntityStatus(this, (byte)31);
            i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
        } else if (this.hookCountdown > 0) {
            LootWorldContext lootWorldContext = new LootWorldContext.Builder((ServerWorld)this.getEntityWorld()).add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.TOOL, usedItem).add(LootContextParameters.THIS_ENTITY, this).luck((float)this.luckBonus + playerEntity.getLuck()).build(LootContextTypes.FISHING);
            LootTable lootTable = this.getEntityWorld().getServer().getReloadableRegistries().getLootTable(LootTables.FISHING_GAMEPLAY);
            ObjectArrayList<ItemStack> list = lootTable.generateLoot(lootWorldContext);
            Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity)playerEntity, usedItem, this, (Collection<ItemStack>)list);
            for (ItemStack itemStack : list) {
                ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), itemStack);
                double d = playerEntity.getX() - this.getX();
                double e = playerEntity.getY() - this.getY();
                double f = playerEntity.getZ() - this.getZ();
                double g = 0.1;
                itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
                this.getEntityWorld().spawnEntity(itemEntity);
                playerEntity.getEntityWorld().spawnEntity(new ExperienceOrbEntity(playerEntity.getEntityWorld(), playerEntity.getX(), playerEntity.getY() + 0.5, playerEntity.getZ() + 0.5, this.random.nextInt(6) + 1));
                if (!itemStack.isIn(ItemTags.FISHES)) continue;
                playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
            }
            i = 1;
        }
        if (this.isOnGround()) {
            i = 2;
        }
        this.discard();
        return i;
    }

    @Override
    public void handleStatus(byte status) {
        PlayerEntity playerEntity;
        Entity entity;
        if (status == 31 && this.getEntityWorld().isClient() && (entity = this.hookedEntity) instanceof PlayerEntity && (playerEntity = (PlayerEntity)entity).isMainPlayer()) {
            this.pullHookedEntity(this.hookedEntity);
        }
        super.handleStatus(status);
    }

    protected void pullHookedEntity(Entity entity) {
        Entity entity2 = this.getOwner();
        if (entity2 == null) {
            return;
        }
        Vec3d vec3d = new Vec3d(entity2.getX() - this.getX(), entity2.getY() - this.getY(), entity2.getZ() - this.getZ()).multiply(0.1);
        entity.setVelocity(entity.getVelocity().add(vec3d));
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.setPlayerFishHook(null);
        super.remove(reason);
    }

    @Override
    public void onRemoved() {
        this.setPlayerFishHook(null);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        this.setPlayerFishHook(this);
    }

    private void setPlayerFishHook(@Nullable FishingBobberEntity fishingBobber) {
        PlayerEntity playerEntity = this.getPlayerOwner();
        if (playerEntity != null) {
            playerEntity.fishHook = fishingBobber;
        }
    }

    public @Nullable PlayerEntity getPlayerOwner() {
        PlayerEntity playerEntity;
        Entity entity = this.getOwner();
        return entity instanceof PlayerEntity ? (playerEntity = (PlayerEntity)entity) : null;
    }

    public @Nullable Entity getHookedEntity() {
        return this.hookedEntity;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        Entity entity = this.getOwner();
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry, entity == null ? this.getId() : entity.getId());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        if (this.getPlayerOwner() == null) {
            int i = packet.getEntityData();
            LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", (Object)this.getEntityWorld().getEntityById(i), (Object)i);
            this.discard();
        }
    }

    static final class State
    extends Enum<State> {
        public static final /* enum */ State FLYING = new State();
        public static final /* enum */ State HOOKED_IN_ENTITY = new State();
        public static final /* enum */ State BOBBING = new State();
        private static final /* synthetic */ State[] field_7181;

        public static State[] values() {
            return (State[])field_7181.clone();
        }

        public static State valueOf(String string) {
            return Enum.valueOf(State.class, string);
        }

        private static /* synthetic */ State[] method_36664() {
            return new State[]{FLYING, HOOKED_IN_ENTITY, BOBBING};
        }

        static {
            field_7181 = State.method_36664();
        }
    }

    static final class PositionType
    extends Enum<PositionType> {
        public static final /* enum */ PositionType ABOVE_WATER = new PositionType();
        public static final /* enum */ PositionType INSIDE_WATER = new PositionType();
        public static final /* enum */ PositionType INVALID = new PositionType();
        private static final /* synthetic */ PositionType[] field_23239;

        public static PositionType[] values() {
            return (PositionType[])field_23239.clone();
        }

        public static PositionType valueOf(String string) {
            return Enum.valueOf(PositionType.class, string);
        }

        private static /* synthetic */ PositionType[] method_36665() {
            return new PositionType[]{ABOVE_WATER, INSIDE_WATER, INVALID};
        }

        static {
            field_23239 = PositionType.method_36665();
        }
    }
}

