/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.boss.dragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonFrameTracker;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.feature.EndPortalFeature;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class EnderDragonEntity
extends MobEntity
implements Monster {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TrackedData<Integer> PHASE_TYPE = DataTracker.registerData(EnderDragonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TargetPredicate CLOSE_PLAYER_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(64.0);
    private static final int MAX_HEALTH = 200;
    private static final int field_30429 = 400;
    private static final float TAKEOFF_THRESHOLD = 0.25f;
    private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
    private static final String DRAGON_PHASE_KEY = "DragonPhase";
    private static final int DEFAULT_TICKS_SINCE_DEATH = 0;
    public final EnderDragonFrameTracker frameTracker = new EnderDragonFrameTracker();
    private final EnderDragonPart[] parts;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart rightWing;
    private final EnderDragonPart leftWing;
    public float lastWingPosition;
    public float wingPosition;
    public boolean slowedDownByBlock;
    public int ticksSinceDeath = 0;
    public float yawAcceleration;
    public @Nullable EndCrystalEntity connectedCrystal;
    private @Nullable EnderDragonFight fight;
    private BlockPos fightOrigin = BlockPos.ORIGIN;
    private final PhaseManager phaseManager;
    private int ticksUntilNextGrowl = 100;
    private float damageDuringSitting;
    private final PathNode[] pathNodes = new PathNode[24];
    private final int[] pathNodeConnections = new int[24];
    private final PathMinHeap pathHeap = new PathMinHeap();

    public EnderDragonEntity(EntityType<? extends EnderDragonEntity> entityType, World world) {
        super((EntityType<? extends MobEntity>)EntityType.ENDER_DRAGON, world);
        this.head = new EnderDragonPart(this, "head", 1.0f, 1.0f);
        this.neck = new EnderDragonPart(this, "neck", 3.0f, 3.0f);
        this.body = new EnderDragonPart(this, "body", 5.0f, 3.0f);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.rightWing = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.leftWing = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.parts = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.rightWing, this.leftWing};
        this.setHealth(this.getMaxHealth());
        this.noClip = true;
        this.phaseManager = new PhaseManager(this);
    }

    public void setFight(EnderDragonFight fight) {
        this.fight = fight;
    }

    public void setFightOrigin(BlockPos fightOrigin) {
        this.fightOrigin = fightOrigin;
    }

    public BlockPos getFightOrigin() {
        return this.fightOrigin;
    }

    public static DefaultAttributeContainer.Builder createEnderDragonAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 200.0).add(EntityAttributes.CAMERA_DISTANCE, 16.0);
    }

    @Override
    public boolean isFlappingWings() {
        float f = MathHelper.cos(this.wingPosition * ((float)Math.PI * 2));
        float g = MathHelper.cos(this.lastWingPosition * ((float)Math.PI * 2));
        return g <= -0.3f && f >= -0.3f;
    }

    @Override
    public void addFlapEffects() {
        if (this.getEntityWorld().isClient() && !this.isSilent()) {
            this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, this.getSoundCategory(), 5.0f, 0.8f + this.random.nextFloat() * 0.3f, false);
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(PHASE_TYPE, PhaseType.HOVER.getTypeId());
    }

    @Override
    public void tickMovement() {
        float o;
        float n;
        float m;
        ServerWorld serverWorld;
        EnderDragonFight enderDragonFight;
        World world;
        this.addAirTravelEffects();
        if (this.getEntityWorld().isClient()) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && !this.phaseManager.getCurrent().isSittingOrHovering() && --this.ticksUntilNextGrowl < 0) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.5f, 0.8f + this.random.nextFloat() * 0.3f, false);
                this.ticksUntilNextGrowl = 200 + this.random.nextInt(200);
            }
        }
        if (this.fight == null && (world = this.getEntityWorld()) instanceof ServerWorld && (enderDragonFight = (serverWorld = (ServerWorld)world).getEnderDragonFight()) != null && this.getUuid().equals(enderDragonFight.getDragonUuid())) {
            this.fight = enderDragonFight;
        }
        this.lastWingPosition = this.wingPosition;
        if (this.isDead()) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float g = (this.random.nextFloat() - 0.5f) * 4.0f;
            float h = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.getEntityWorld().addParticleClient(ParticleTypes.EXPLOSION, this.getX() + (double)f, this.getY() + 2.0 + (double)g, this.getZ() + (double)h, 0.0, 0.0, 0.0);
            return;
        }
        this.tickWithEndCrystals();
        Vec3d vec3d = this.getVelocity();
        float g = 0.2f / ((float)vec3d.horizontalLength() * 10.0f + 1.0f);
        this.wingPosition = this.phaseManager.getCurrent().isSittingOrHovering() ? (this.wingPosition += 0.1f) : (this.slowedDownByBlock ? (this.wingPosition += g * 0.5f) : (this.wingPosition += (g *= (float)Math.pow(2.0, vec3d.y))));
        this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
        if (this.isAiDisabled()) {
            this.wingPosition = 0.5f;
            return;
        }
        this.frameTracker.tick(this.getY(), this.getYaw());
        World world2 = this.getEntityWorld();
        if (!(world2 instanceof ServerWorld)) {
            this.interpolator.tick();
            this.phaseManager.getCurrent().clientTick();
        } else {
            Vec3d vec3d2;
            ServerWorld serverWorld2 = (ServerWorld)world2;
            Phase phase = this.phaseManager.getCurrent();
            phase.serverTick(serverWorld2);
            if (this.phaseManager.getCurrent() != phase) {
                phase = this.phaseManager.getCurrent();
                phase.serverTick(serverWorld2);
            }
            if ((vec3d2 = phase.getPathTarget()) != null) {
                double d = vec3d2.x - this.getX();
                double e = vec3d2.y - this.getY();
                double i = vec3d2.z - this.getZ();
                double j = d * d + e * e + i * i;
                float k = phase.getMaxYAcceleration();
                double l = Math.sqrt(d * d + i * i);
                if (l > 0.0) {
                    e = MathHelper.clamp(e / l, (double)(-k), (double)k);
                }
                this.setVelocity(this.getVelocity().add(0.0, e * 0.01, 0.0));
                this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
                Vec3d vec3d3 = vec3d2.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                Vec3d vec3d4 = new Vec3d(MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), this.getVelocity().y, -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180))).normalize();
                m = Math.max(((float)vec3d4.dotProduct(vec3d3) + 0.5f) / 1.5f, 0.0f);
                if (Math.abs(d) > (double)1.0E-5f || Math.abs(i) > (double)1.0E-5f) {
                    n = MathHelper.clamp(MathHelper.wrapDegrees(180.0f - (float)MathHelper.atan2(d, i) * 57.295776f - this.getYaw()), -50.0f, 50.0f);
                    this.yawAcceleration *= 0.8f;
                    this.yawAcceleration += n * phase.getYawAcceleration();
                    this.setYaw(this.getYaw() + this.yawAcceleration * 0.1f);
                }
                n = (float)(2.0 / (j + 1.0));
                o = 0.06f;
                this.updateVelocity(0.06f * (m * n + (1.0f - n)), new Vec3d(0.0, 0.0, -1.0));
                if (this.slowedDownByBlock) {
                    this.move(MovementType.SELF, this.getVelocity().multiply(0.8f));
                } else {
                    this.move(MovementType.SELF, this.getVelocity());
                }
                Vec3d vec3d5 = this.getVelocity().normalize();
                double p = 0.8 + 0.15 * (vec3d5.dotProduct(vec3d4) + 1.0) / 2.0;
                this.setVelocity(this.getVelocity().multiply(p, 0.91f, p));
            }
        }
        if (!this.getEntityWorld().isClient()) {
            this.tickBlockCollision();
        }
        this.bodyYaw = this.getYaw();
        Vec3d[] vec3ds = new Vec3d[this.parts.length];
        for (int q = 0; q < this.parts.length; ++q) {
            vec3ds[q] = new Vec3d(this.parts[q].getX(), this.parts[q].getY(), this.parts[q].getZ());
        }
        float r = (float)(this.frameTracker.getFrame(5).y() - this.frameTracker.getFrame(10).y()) * 10.0f * ((float)Math.PI / 180);
        float s = MathHelper.cos(r);
        float t = MathHelper.sin(r);
        float u = this.getYaw() * ((float)Math.PI / 180);
        float v = MathHelper.sin(u);
        float w = MathHelper.cos(u);
        this.movePart(this.body, v * 0.5f, 0.0, -w * 0.5f);
        this.movePart(this.rightWing, w * 4.5f, 2.0, v * 4.5f);
        this.movePart(this.leftWing, w * -4.5f, 2.0, v * -4.5f);
        World world3 = this.getEntityWorld();
        if (world3 instanceof ServerWorld) {
            ServerWorld serverWorld3 = (ServerWorld)world3;
            if (this.hurtTime == 0) {
                this.launchLivingEntities(serverWorld3, serverWorld3.getOtherEntities(this, this.rightWing.getBoundingBox().expand(4.0, 2.0, 4.0).offset(0.0, -2.0, 0.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                this.launchLivingEntities(serverWorld3, serverWorld3.getOtherEntities(this, this.leftWing.getBoundingBox().expand(4.0, 2.0, 4.0).offset(0.0, -2.0, 0.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                this.damageLivingEntities(serverWorld3, serverWorld3.getOtherEntities(this, this.head.getBoundingBox().expand(1.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                this.damageLivingEntities(serverWorld3, serverWorld3.getOtherEntities(this, this.neck.getBoundingBox().expand(1.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            }
        }
        float x = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180) - this.yawAcceleration * 0.01f);
        float y = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180) - this.yawAcceleration * 0.01f);
        float z = this.getHeadVerticalMovement();
        this.movePart(this.head, x * 6.5f * s, z + t * 6.5f, -y * 6.5f * s);
        this.movePart(this.neck, x * 5.5f * s, z + t * 5.5f, -y * 5.5f * s);
        EnderDragonFrameTracker.Frame frame = this.frameTracker.getFrame(5);
        for (int aa = 0; aa < 3; ++aa) {
            EnderDragonPart enderDragonPart = null;
            if (aa == 0) {
                enderDragonPart = this.tail1;
            }
            if (aa == 1) {
                enderDragonPart = this.tail2;
            }
            if (aa == 2) {
                enderDragonPart = this.tail3;
            }
            EnderDragonFrameTracker.Frame frame2 = this.frameTracker.getFrame(12 + aa * 2);
            float ab = this.getYaw() * ((float)Math.PI / 180) + this.wrapYawChange(frame2.yRot() - frame.yRot()) * ((float)Math.PI / 180);
            float ac = MathHelper.sin(ab);
            m = MathHelper.cos(ab);
            n = 1.5f;
            o = (float)(aa + 1) * 2.0f;
            this.movePart(enderDragonPart, -(v * 1.5f + ac * o) * s, frame2.y() - frame.y() - (double)((o + 1.5f) * t) + 1.5, (w * 1.5f + m * o) * s);
        }
        World world4 = this.getEntityWorld();
        if (world4 instanceof ServerWorld) {
            ServerWorld serverWorld4 = (ServerWorld)world4;
            this.slowedDownByBlock = this.destroyBlocks(serverWorld4, this.head.getBoundingBox()) | this.destroyBlocks(serverWorld4, this.neck.getBoundingBox()) | this.destroyBlocks(serverWorld4, this.body.getBoundingBox());
            if (this.fight != null) {
                this.fight.updateFight(this);
            }
        }
        for (int aa = 0; aa < this.parts.length; ++aa) {
            this.parts[aa].lastX = vec3ds[aa].x;
            this.parts[aa].lastY = vec3ds[aa].y;
            this.parts[aa].lastZ = vec3ds[aa].z;
            this.parts[aa].lastRenderX = vec3ds[aa].x;
            this.parts[aa].lastRenderY = vec3ds[aa].y;
            this.parts[aa].lastRenderZ = vec3ds[aa].z;
        }
    }

    private void movePart(EnderDragonPart enderDragonPart, double dx, double dy, double dz) {
        enderDragonPart.setPosition(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
    }

    private float getHeadVerticalMovement() {
        if (this.phaseManager.getCurrent().isSittingOrHovering()) {
            return -1.0f;
        }
        EnderDragonFrameTracker.Frame frame = this.frameTracker.getFrame(5);
        EnderDragonFrameTracker.Frame frame2 = this.frameTracker.getFrame(0);
        return (float)(frame.y() - frame2.y());
    }

    private void tickWithEndCrystals() {
        if (this.connectedCrystal != null) {
            if (this.connectedCrystal.isRemoved()) {
                this.connectedCrystal = null;
            } else if (this.age % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0f);
            }
        }
        if (this.random.nextInt(10) == 0) {
            List<EndCrystalEntity> list = this.getEntityWorld().getNonSpectatingEntities(EndCrystalEntity.class, this.getBoundingBox().expand(32.0));
            EndCrystalEntity endCrystalEntity = null;
            double d = Double.MAX_VALUE;
            for (EndCrystalEntity endCrystalEntity2 : list) {
                double e = endCrystalEntity2.squaredDistanceTo(this);
                if (!(e < d)) continue;
                d = e;
                endCrystalEntity = endCrystalEntity2;
            }
            this.connectedCrystal = endCrystalEntity;
        }
    }

    private void launchLivingEntities(ServerWorld world, List<Entity> entities) {
        double d = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
        double e = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity livingEntity = (LivingEntity)entity;
            double f = entity.getX() - d;
            double g = entity.getZ() - e;
            double h = Math.max(f * f + g * g, 0.1);
            entity.addVelocity(f / h * 4.0, 0.2f, g / h * 4.0);
            if (this.phaseManager.getCurrent().isSittingOrHovering() || livingEntity.getLastAttackedTime() >= entity.age - 2) continue;
            DamageSource damageSource = this.getDamageSources().mobAttack(this);
            entity.damage(world, damageSource, 5.0f);
            EnchantmentHelper.onTargetDamaged(world, entity, damageSource);
        }
    }

    private void damageLivingEntities(ServerWorld world, List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            DamageSource damageSource = this.getDamageSources().mobAttack(this);
            entity.damage(world, damageSource, 10.0f);
            EnchantmentHelper.onTargetDamaged(world, entity, damageSource);
        }
    }

    private float wrapYawChange(double yawDegrees) {
        return (float)MathHelper.wrapDegrees(yawDegrees);
    }

    private boolean destroyBlocks(ServerWorld world, Box box) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.minY);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.floor(box.maxX);
        int m = MathHelper.floor(box.maxY);
        int n = MathHelper.floor(box.maxZ);
        boolean bl = false;
        boolean bl2 = false;
        for (int o = i; o <= l; ++o) {
            for (int p = j; p <= m; ++p) {
                for (int q = k; q <= n; ++q) {
                    BlockPos blockPos = new BlockPos(o, p, q);
                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.isAir() || blockState.isIn(BlockTags.DRAGON_TRANSPARENT)) continue;
                    if (!world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue() || blockState.isIn(BlockTags.DRAGON_IMMUNE)) {
                        bl = true;
                        continue;
                    }
                    bl2 = world.removeBlock(blockPos, false) || bl2;
                }
            }
        }
        if (bl2) {
            BlockPos blockPos2 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(m - j + 1), k + this.random.nextInt(n - k + 1));
            world.syncWorldEvent(2008, blockPos2, 0);
        }
        return bl;
    }

    public boolean damagePart(ServerWorld world, EnderDragonPart part, DamageSource source, float amount) {
        if (this.phaseManager.getCurrent().getType() == PhaseType.DYING) {
            return false;
        }
        amount = this.phaseManager.getCurrent().modifyDamageTaken(source, amount);
        if (part != this.head) {
            amount = amount / 4.0f + Math.min(amount, 1.0f);
        }
        if (amount < 0.01f) {
            return false;
        }
        if (source.getAttacker() instanceof PlayerEntity || source.isIn(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
            float f = this.getHealth();
            this.parentDamage(world, source, amount);
            if (this.isDead() && !this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.setHealth(1.0f);
                this.phaseManager.setPhase(PhaseType.DYING);
            }
            if (this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.damageDuringSitting = this.damageDuringSitting + f - this.getHealth();
                if (this.damageDuringSitting > 0.25f * this.getMaxHealth()) {
                    this.damageDuringSitting = 0.0f;
                    this.phaseManager.setPhase(PhaseType.TAKEOFF);
                }
            }
        }
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return this.damagePart(world, this.body, source, amount);
    }

    protected void parentDamage(ServerWorld world, DamageSource source, float amount) {
        super.damage(world, source, amount);
    }

    @Override
    public void kill(ServerWorld world) {
        this.remove(Entity.RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
        if (this.fight != null) {
            this.fight.updateFight(this);
            this.fight.dragonKilled(this);
        }
    }

    @Override
    protected void updatePostDeath() {
        World world;
        EnderDragonPart[] h2;
        if (this.fight != null) {
            this.fight.updateFight(this);
        }
        ++this.ticksSinceDeath;
        if (this.ticksSinceDeath >= 180 && this.ticksSinceDeath <= 200) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float g = (this.random.nextFloat() - 0.5f) * 4.0f;
            float h2 = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.getEntityWorld().addParticleClient(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0 + (double)g, this.getZ() + (double)h2, 0.0, 0.0, 0.0);
        }
        int i = 500;
        if (this.fight != null && !this.fight.hasPreviouslyKilled()) {
            i = 12000;
        }
        if ((h2 = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)h2;
            if (this.ticksSinceDeath > 150 && this.ticksSinceDeath % 5 == 0 && serverWorld.getGameRules().getValue(GameRules.DO_MOB_LOOT).booleanValue()) {
                ExperienceOrbEntity.spawn(serverWorld, this.getEntityPos(), MathHelper.floor((float)i * 0.08f));
            }
            if (this.ticksSinceDeath == 1 && !this.isSilent()) {
                serverWorld.syncGlobalEvent(1028, this.getBlockPos(), 0);
            }
        }
        Vec3d vec3d = new Vec3d(0.0, 0.1f, 0.0);
        this.move(MovementType.SELF, vec3d);
        for (EnderDragonPart enderDragonPart : this.parts) {
            enderDragonPart.resetPosition();
            enderDragonPart.setPosition(enderDragonPart.getEntityPos().add(vec3d));
        }
        if (this.ticksSinceDeath == 200 && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld2 = (ServerWorld)world;
            if (serverWorld2.getGameRules().getValue(GameRules.DO_MOB_LOOT).booleanValue()) {
                ExperienceOrbEntity.spawn(serverWorld2, this.getEntityPos(), MathHelper.floor((float)i * 0.2f));
            }
            if (this.fight != null) {
                this.fight.dragonKilled(this);
            }
            this.remove(Entity.RemovalReason.KILLED);
            this.emitGameEvent(GameEvent.ENTITY_DIE);
        }
    }

    public int getNearestPathNodeIndex() {
        if (this.pathNodes[0] == null) {
            for (int i = 0; i < 24; ++i) {
                int m;
                int l;
                int j = 5;
                int k = i;
                if (i < 12) {
                    l = MathHelper.floor(60.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)k)));
                    m = MathHelper.floor(60.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)k)));
                } else if (i < 20) {
                    l = MathHelper.floor(40.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)(k -= 12))));
                    m = MathHelper.floor(40.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)k)));
                    j += 10;
                } else {
                    l = MathHelper.floor(20.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)(k -= 20))));
                    m = MathHelper.floor(20.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)k)));
                }
                int n = Math.max(73, this.getEntityWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, m)).getY() + j);
                this.pathNodes[i] = new PathNode(l, n, m);
            }
            this.pathNodeConnections[0] = 6146;
            this.pathNodeConnections[1] = 8197;
            this.pathNodeConnections[2] = 8202;
            this.pathNodeConnections[3] = 16404;
            this.pathNodeConnections[4] = 32808;
            this.pathNodeConnections[5] = 32848;
            this.pathNodeConnections[6] = 65696;
            this.pathNodeConnections[7] = 131392;
            this.pathNodeConnections[8] = 131712;
            this.pathNodeConnections[9] = 263424;
            this.pathNodeConnections[10] = 526848;
            this.pathNodeConnections[11] = 525313;
            this.pathNodeConnections[12] = 1581057;
            this.pathNodeConnections[13] = 3166214;
            this.pathNodeConnections[14] = 2138120;
            this.pathNodeConnections[15] = 6373424;
            this.pathNodeConnections[16] = 4358208;
            this.pathNodeConnections[17] = 12910976;
            this.pathNodeConnections[18] = 9044480;
            this.pathNodeConnections[19] = 9706496;
            this.pathNodeConnections[20] = 15216640;
            this.pathNodeConnections[21] = 0xD0E000;
            this.pathNodeConnections[22] = 11763712;
            this.pathNodeConnections[23] = 0x7E0000;
        }
        return this.getNearestPathNodeIndex(this.getX(), this.getY(), this.getZ());
    }

    public int getNearestPathNodeIndex(double x, double y, double z) {
        float f = 10000.0f;
        int i = 0;
        PathNode pathNode = new PathNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        int j = 0;
        if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
            j = 12;
        }
        for (int k = j; k < 24; ++k) {
            float g;
            if (this.pathNodes[k] == null || !((g = this.pathNodes[k].getSquaredDistance(pathNode)) < f)) continue;
            f = g;
            i = k;
        }
        return i;
    }

    public @Nullable Path findPath(int from, int to, @Nullable PathNode pathNode) {
        PathNode pathNode2;
        for (int i = 0; i < 24; ++i) {
            pathNode2 = this.pathNodes[i];
            pathNode2.visited = false;
            pathNode2.heapWeight = 0.0f;
            pathNode2.penalizedPathLength = 0.0f;
            pathNode2.distanceToNearestTarget = 0.0f;
            pathNode2.previous = null;
            pathNode2.heapIndex = -1;
        }
        PathNode pathNode3 = this.pathNodes[from];
        pathNode2 = this.pathNodes[to];
        pathNode3.penalizedPathLength = 0.0f;
        pathNode3.heapWeight = pathNode3.distanceToNearestTarget = pathNode3.getDistance(pathNode2);
        this.pathHeap.clear();
        this.pathHeap.push(pathNode3);
        PathNode pathNode4 = pathNode3;
        int j = 0;
        if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
            j = 12;
        }
        while (!this.pathHeap.isEmpty()) {
            int l;
            PathNode pathNode5 = this.pathHeap.pop();
            if (pathNode5.equals(pathNode2)) {
                if (pathNode != null) {
                    pathNode.previous = pathNode2;
                    pathNode2 = pathNode;
                }
                return this.getPathOfAllPredecessors(pathNode3, pathNode2);
            }
            if (pathNode5.getDistance(pathNode2) < pathNode4.getDistance(pathNode2)) {
                pathNode4 = pathNode5;
            }
            pathNode5.visited = true;
            int k = 0;
            for (l = 0; l < 24; ++l) {
                if (this.pathNodes[l] != pathNode5) continue;
                k = l;
                break;
            }
            for (l = j; l < 24; ++l) {
                if ((this.pathNodeConnections[k] & 1 << l) <= 0) continue;
                PathNode pathNode6 = this.pathNodes[l];
                if (pathNode6.visited) continue;
                float f = pathNode5.penalizedPathLength + pathNode5.getDistance(pathNode6);
                if (pathNode6.isInHeap() && !(f < pathNode6.penalizedPathLength)) continue;
                pathNode6.previous = pathNode5;
                pathNode6.penalizedPathLength = f;
                pathNode6.distanceToNearestTarget = pathNode6.getDistance(pathNode2);
                if (pathNode6.isInHeap()) {
                    this.pathHeap.setNodeWeight(pathNode6, pathNode6.penalizedPathLength + pathNode6.distanceToNearestTarget);
                    continue;
                }
                pathNode6.heapWeight = pathNode6.penalizedPathLength + pathNode6.distanceToNearestTarget;
                this.pathHeap.push(pathNode6);
            }
        }
        if (pathNode4 == pathNode3) {
            return null;
        }
        LOGGER.debug("Failed to find path from {} to {}", (Object)from, (Object)to);
        if (pathNode != null) {
            pathNode.previous = pathNode4;
            pathNode4 = pathNode;
        }
        return this.getPathOfAllPredecessors(pathNode3, pathNode4);
    }

    private Path getPathOfAllPredecessors(PathNode unused, PathNode node) {
        ArrayList list = Lists.newArrayList();
        PathNode pathNode = node;
        list.add(0, pathNode);
        while (pathNode.previous != null) {
            pathNode = pathNode.previous;
            list.add(0, pathNode);
        }
        return new Path(list, new BlockPos(node.x, node.y, node.z), true);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt(DRAGON_PHASE_KEY, this.phaseManager.getCurrent().getType().getTypeId());
        view.putInt(DRAGON_DEATH_TIME_KEY, this.ticksSinceDeath);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        view.getOptionalInt(DRAGON_PHASE_KEY).ifPresent(phase -> this.phaseManager.setPhase(PhaseType.getFromId(phase)));
        this.ticksSinceDeath = view.getInt(DRAGON_DEATH_TIME_KEY, 0);
    }

    @Override
    public void checkDespawn() {
    }

    public EnderDragonPart[] getBodyParts() {
        return this.parts;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public Vec3d getRotationVectorFromPhase(float tickProgress) {
        Vec3d vec3d;
        Phase phase = this.phaseManager.getCurrent();
        PhaseType<? extends Phase> phaseType = phase.getType();
        if (phaseType == PhaseType.LANDING || phaseType == PhaseType.TAKEOFF) {
            BlockPos blockPos = this.getEntityWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.offsetOrigin(this.fightOrigin));
            float f = Math.max((float)Math.sqrt(blockPos.getSquaredDistance(this.getEntityPos())) / 4.0f, 1.0f);
            float g = 6.0f / f;
            float h = this.getPitch();
            float i = 1.5f;
            this.setPitch(-g * 1.5f * 5.0f);
            vec3d = this.getRotationVec(tickProgress);
            this.setPitch(h);
        } else if (phase.isSittingOrHovering()) {
            float j = this.getPitch();
            float f = 1.5f;
            this.setPitch(-45.0f);
            vec3d = this.getRotationVec(tickProgress);
            this.setPitch(j);
        } else {
            vec3d = this.getRotationVec(tickProgress);
        }
        return vec3d;
    }

    public void crystalDestroyed(ServerWorld world, EndCrystalEntity crystal, BlockPos pos, DamageSource source) {
        PlayerEntity playerEntity;
        Entity entity = source.getAttacker();
        PlayerEntity playerEntity2 = entity instanceof PlayerEntity ? (playerEntity = (PlayerEntity)entity) : world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, pos.getX(), pos.getY(), pos.getZ());
        if (crystal == this.connectedCrystal) {
            this.damagePart(world, this.head, this.getDamageSources().explosion(crystal, playerEntity2), 10.0f);
        }
        this.phaseManager.getCurrent().crystalDestroyed(crystal, pos, source, playerEntity2);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (PHASE_TYPE.equals(data) && this.getEntityWorld().isClient()) {
            this.phaseManager.setPhase(PhaseType.getFromId(this.getDataTracker().get(PHASE_TYPE)));
        }
        super.onTrackedDataSet(data);
    }

    public PhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    public @Nullable EnderDragonFight getFight() {
        return this.fight;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return false;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        EnderDragonPart[] enderDragonParts = this.getBodyParts();
        for (int i = 0; i < enderDragonParts.length; ++i) {
            enderDragonParts[i].setId(i + packet.getEntityId() + 1);
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return target.canTakeDamage();
    }

    @Override
    protected float clampScale(float scale) {
        return 1.0f;
    }
}

