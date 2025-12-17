/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.ai.goal.SitOnOwnerShoulderGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TameableShoulderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;

public class ParrotEntity
extends TameableShoulderEntity
implements Flutterer {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(ParrotEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final Predicate<MobEntity> CAN_IMITATE = new Predicate<MobEntity>(){

        @Override
        public boolean test(@Nullable MobEntity mobEntity) {
            return mobEntity != null && MOB_SOUNDS.containsKey(mobEntity.getType());
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object entity) {
            return this.test((MobEntity)entity);
        }
    };
    static final Map<EntityType<?>, SoundEvent> MOB_SOUNDS = Util.make(Maps.newHashMap(), map -> {
        map.put(EntityType.BLAZE, SoundEvents.ENTITY_PARROT_IMITATE_BLAZE);
        map.put(EntityType.BOGGED, SoundEvents.ENTITY_PARROT_IMITATE_BOGGED);
        map.put(EntityType.BREEZE, SoundEvents.ENTITY_PARROT_IMITATE_BREEZE);
        map.put(EntityType.CAMEL_HUSK, SoundEvents.ENTITY_PARROT_IMITATE_CAMEL_HUSK);
        map.put(EntityType.CAVE_SPIDER, SoundEvents.ENTITY_PARROT_IMITATE_SPIDER);
        map.put(EntityType.CREAKING, SoundEvents.ENTITY_PARROT_IMITATE_CREAKING);
        map.put(EntityType.CREEPER, SoundEvents.ENTITY_PARROT_IMITATE_CREEPER);
        map.put(EntityType.DROWNED, SoundEvents.ENTITY_PARROT_IMITATE_DROWNED);
        map.put(EntityType.ELDER_GUARDIAN, SoundEvents.ENTITY_PARROT_IMITATE_ELDER_GUARDIAN);
        map.put(EntityType.ENDER_DRAGON, SoundEvents.ENTITY_PARROT_IMITATE_ENDER_DRAGON);
        map.put(EntityType.ENDERMITE, SoundEvents.ENTITY_PARROT_IMITATE_ENDERMITE);
        map.put(EntityType.EVOKER, SoundEvents.ENTITY_PARROT_IMITATE_EVOKER);
        map.put(EntityType.GHAST, SoundEvents.ENTITY_PARROT_IMITATE_GHAST);
        map.put(EntityType.HAPPY_GHAST, SoundEvents.INTENTIONALLY_EMPTY);
        map.put(EntityType.GUARDIAN, SoundEvents.ENTITY_PARROT_IMITATE_GUARDIAN);
        map.put(EntityType.HOGLIN, SoundEvents.ENTITY_PARROT_IMITATE_HOGLIN);
        map.put(EntityType.HUSK, SoundEvents.ENTITY_PARROT_IMITATE_HUSK);
        map.put(EntityType.ILLUSIONER, SoundEvents.ENTITY_PARROT_IMITATE_ILLUSIONER);
        map.put(EntityType.MAGMA_CUBE, SoundEvents.ENTITY_PARROT_IMITATE_MAGMA_CUBE);
        map.put(EntityType.PARCHED, SoundEvents.ENTITY_PARROT_IMITATE_PARCHED);
        map.put(EntityType.PHANTOM, SoundEvents.ENTITY_PARROT_IMITATE_PHANTOM);
        map.put(EntityType.PIGLIN, SoundEvents.ENTITY_PARROT_IMITATE_PIGLIN);
        map.put(EntityType.PIGLIN_BRUTE, SoundEvents.ENTITY_PARROT_IMITATE_PIGLIN_BRUTE);
        map.put(EntityType.PILLAGER, SoundEvents.ENTITY_PARROT_IMITATE_PILLAGER);
        map.put(EntityType.RAVAGER, SoundEvents.ENTITY_PARROT_IMITATE_RAVAGER);
        map.put(EntityType.SHULKER, SoundEvents.ENTITY_PARROT_IMITATE_SHULKER);
        map.put(EntityType.SILVERFISH, SoundEvents.ENTITY_PARROT_IMITATE_SILVERFISH);
        map.put(EntityType.SKELETON, SoundEvents.ENTITY_PARROT_IMITATE_SKELETON);
        map.put(EntityType.SLIME, SoundEvents.ENTITY_PARROT_IMITATE_SLIME);
        map.put(EntityType.SPIDER, SoundEvents.ENTITY_PARROT_IMITATE_SPIDER);
        map.put(EntityType.STRAY, SoundEvents.ENTITY_PARROT_IMITATE_STRAY);
        map.put(EntityType.VEX, SoundEvents.ENTITY_PARROT_IMITATE_VEX);
        map.put(EntityType.VINDICATOR, SoundEvents.ENTITY_PARROT_IMITATE_VINDICATOR);
        map.put(EntityType.WARDEN, SoundEvents.ENTITY_PARROT_IMITATE_WARDEN);
        map.put(EntityType.WITCH, SoundEvents.ENTITY_PARROT_IMITATE_WITCH);
        map.put(EntityType.WITHER, SoundEvents.ENTITY_PARROT_IMITATE_WITHER);
        map.put(EntityType.WITHER_SKELETON, SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON);
        map.put(EntityType.ZOGLIN, SoundEvents.ENTITY_PARROT_IMITATE_ZOGLIN);
        map.put(EntityType.ZOMBIE, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE);
        map.put(EntityType.ZOMBIE_HORSE, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE_HORSE);
        map.put(EntityType.ZOMBIE_NAUTILUS, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE_NAUTILUS);
        map.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flapProgress;
    public float maxWingDeviation;
    public float lastMaxWingDeviation;
    public float lastFlapProgress;
    private float flapSpeed = 1.0f;
    private float field_28640 = 1.0f;
    private boolean songPlaying;
    private @Nullable BlockPos songSource;

    public ParrotEntity(EntityType<? extends ParrotEntity> entityType, World world) {
        super((EntityType<? extends TameableShoulderEntity>)entityType, world);
        this.moveControl = new FlightMoveControl(this, 10, false);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
        this.setPathfindingPenalty(PathNodeType.COCOA, -1.0f);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setVariant(Util.getRandom(Variant.values(), world.getRandom()));
        if (entityData == null) {
            entityData = new PassiveEntity.PassiveData(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new TameableEntity.TameableEscapeDangerGoal(1.25));
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(2, new SitGoal(this));
        this.goalSelector.add(2, new FollowOwnerGoal(this, 1.0, 5.0f, 1.0f));
        this.goalSelector.add(2, new FlyOntoTreeGoal(this, 1.0));
        this.goalSelector.add(3, new SitOnOwnerShoulderGoal(this));
        this.goalSelector.add(3, new FollowMobGoal(this, 1.0, 3.0f, 7.0f));
    }

    public static DefaultAttributeContainer.Builder createParrotAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 6.0).add(EntityAttributes.FLYING_SPEED, 0.4f).add(EntityAttributes.MOVEMENT_SPEED, 0.2f).add(EntityAttributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanOpenDoors(false);
        birdNavigation.setCanSwim(true);
        return birdNavigation;
    }

    @Override
    public void tickMovement() {
        if (this.songSource == null || !this.songSource.isWithinDistance(this.getEntityPos(), 3.46) || !this.getEntityWorld().getBlockState(this.songSource).isOf(Blocks.JUKEBOX)) {
            this.songPlaying = false;
            this.songSource = null;
        }
        if (this.getEntityWorld().random.nextInt(400) == 0) {
            ParrotEntity.imitateNearbyMob(this.getEntityWorld(), this);
        }
        super.tickMovement();
        this.flapWings();
    }

    @Override
    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
        this.songSource = songPosition;
        this.songPlaying = playing;
    }

    public boolean isSongPlaying() {
        return this.songPlaying;
    }

    private void flapWings() {
        this.lastFlapProgress = this.flapProgress;
        this.lastMaxWingDeviation = this.maxWingDeviation;
        this.maxWingDeviation += (float)(this.isOnGround() || this.hasVehicle() ? -1 : 4) * 0.3f;
        this.maxWingDeviation = MathHelper.clamp(this.maxWingDeviation, 0.0f, 1.0f);
        if (!this.isOnGround() && this.flapSpeed < 1.0f) {
            this.flapSpeed = 1.0f;
        }
        this.flapSpeed *= 0.9f;
        Vec3d vec3d = this.getVelocity();
        if (!this.isOnGround() && vec3d.y < 0.0) {
            this.setVelocity(vec3d.multiply(1.0, 0.6, 1.0));
        }
        this.flapProgress += this.flapSpeed * 2.0f;
    }

    public static boolean imitateNearbyMob(World world, Entity parrot) {
        MobEntity mobEntity;
        if (!parrot.isAlive() || parrot.isSilent() || world.random.nextInt(2) != 0) {
            return false;
        }
        List<MobEntity> list = world.getEntitiesByClass(MobEntity.class, parrot.getBoundingBox().expand(20.0), CAN_IMITATE);
        if (!list.isEmpty() && !(mobEntity = list.get(world.random.nextInt(list.size()))).isSilent()) {
            SoundEvent soundEvent = ParrotEntity.getSound(mobEntity.getType());
            world.playSound(null, parrot.getX(), parrot.getY(), parrot.getZ(), soundEvent, parrot.getSoundCategory(), 0.7f, ParrotEntity.getSoundPitch(world.random));
            return true;
        }
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!this.isTamed() && itemStack.isIn(ItemTags.PARROT_FOOD)) {
            this.eat(player, hand, itemStack);
            if (!this.isSilent()) {
                this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PARROT_EAT, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            if (!this.getEntityWorld().isClient()) {
                if (this.random.nextInt(10) == 0) {
                    this.setTamedBy(player);
                    this.getEntityWorld().sendEntityStatus(this, (byte)7);
                } else {
                    this.getEntityWorld().sendEntityStatus(this, (byte)6);
                }
            }
            return ActionResult.SUCCESS;
        }
        if (itemStack.isIn(ItemTags.PARROT_POISONOUS_FOOD)) {
            this.eat(player, hand, itemStack);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 900));
            if (player.isCreative() || !this.isInvulnerable()) {
                this.serverDamage(this.getDamageSources().playerAttack(player), Float.MAX_VALUE);
            }
            return ActionResult.SUCCESS;
        }
        if (!this.isInAir() && this.isTamed() && this.isOwner(player)) {
            if (!this.getEntityWorld().isClient()) {
                this.setSitting(!this.isSitting());
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    public static boolean canSpawn(EntityType<ParrotEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.PARROTS_SPAWNABLE_ON) && ParrotEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public @Nullable SoundEvent getAmbientSound() {
        return ParrotEntity.getRandomSound(this.getEntityWorld(), this.getEntityWorld().random);
    }

    public static SoundEvent getRandomSound(World world, Random random) {
        if (world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(1000) == 0) {
            ArrayList list = Lists.newArrayList(MOB_SOUNDS.keySet());
            return ParrotEntity.getSound((EntityType)list.get(random.nextInt(list.size())));
        }
        return SoundEvents.ENTITY_PARROT_AMBIENT;
    }

    private static SoundEvent getSound(EntityType<?> imitate) {
        return MOB_SOUNDS.getOrDefault(imitate, SoundEvents.ENTITY_PARROT_AMBIENT);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PARROT_STEP, 0.15f, 1.0f);
    }

    @Override
    protected boolean isFlappingWings() {
        return this.speed > this.field_28640;
    }

    @Override
    protected void addFlapEffects() {
        this.playSound(SoundEvents.ENTITY_PARROT_FLY, 0.15f, 1.0f);
        this.field_28640 = this.speed + this.maxWingDeviation / 2.0f;
    }

    @Override
    public float getSoundPitch() {
        return ParrotEntity.getSoundPitch(this.random);
    }

    public static float getSoundPitch(Random random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void pushAway(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return;
        }
        super.pushAway(entity);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        this.setSitting(false);
        return super.damage(world, source, amount);
    }

    public Variant getVariant() {
        return Variant.byIndex(this.dataTracker.get(VARIANT));
    }

    private void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.index);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.PARROT_VARIANT) {
            return ParrotEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.PARROT_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.PARROT_VARIANT) {
            this.setVariant(ParrotEntity.castComponentValue(DataComponentTypes.PARROT_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variant.DEFAULT.index);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("Variant", Variant.INDEX_CODEC, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.read("Variant", Variant.INDEX_CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    public boolean isInAir() {
        return !this.isOnGround();
    }

    @Override
    protected boolean canTeleportOntoLeaves() {
        return true;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.5f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    public static final class Variant
    extends Enum<Variant>
    implements StringIdentifiable {
        public static final /* enum */ Variant RED_BLUE = new Variant(0, "red_blue");
        public static final /* enum */ Variant BLUE = new Variant(1, "blue");
        public static final /* enum */ Variant GREEN = new Variant(2, "green");
        public static final /* enum */ Variant YELLOW_BLUE = new Variant(3, "yellow_blue");
        public static final /* enum */ Variant GRAY = new Variant(4, "gray");
        public static final Variant DEFAULT;
        private static final IntFunction<Variant> INDEX_MAPPER;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> INDEX_CODEC;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        final int index;
        private final String id;
        private static final /* synthetic */ Variant[] field_41559;

        public static Variant[] values() {
            return (Variant[])field_41559.clone();
        }

        public static Variant valueOf(String string) {
            return Enum.valueOf(Variant.class, string);
        }

        private Variant(int index, String id) {
            this.index = index;
            this.id = id;
        }

        public int getIndex() {
            return this.index;
        }

        public static Variant byIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Variant[] method_47851() {
            return new Variant[]{RED_BLUE, BLUE, GREEN, YELLOW_BLUE, GRAY};
        }

        static {
            field_41559 = Variant.method_47851();
            DEFAULT = RED_BLUE;
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.CLAMP);
            CODEC = StringIdentifiable.createCodec(Variant::values);
            INDEX_CODEC = Codec.INT.xmap(INDEX_MAPPER::apply, Variant::getIndex);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Variant::getIndex);
        }
    }

    static class FlyOntoTreeGoal
    extends FlyGoal {
        public FlyOntoTreeGoal(PathAwareEntity pathAwareEntity, double d) {
            super(pathAwareEntity, d);
        }

        @Override
        protected @Nullable Vec3d getWanderTarget() {
            Vec3d vec3d = null;
            if (this.mob.isTouchingWater()) {
                vec3d = FuzzyTargeting.find(this.mob, 15, 15);
            }
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                vec3d = this.locateTree();
            }
            return vec3d == null ? super.getWanderTarget() : vec3d;
        }

        private @Nullable Vec3d locateTree() {
            BlockPos blockPos = this.mob.getBlockPos();
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            BlockPos.Mutable mutable2 = new BlockPos.Mutable();
            Iterable<BlockPos> iterable = BlockPos.iterate(MathHelper.floor(this.mob.getX() - 3.0), MathHelper.floor(this.mob.getY() - 6.0), MathHelper.floor(this.mob.getZ() - 3.0), MathHelper.floor(this.mob.getX() + 3.0), MathHelper.floor(this.mob.getY() + 6.0), MathHelper.floor(this.mob.getZ() + 3.0));
            for (BlockPos blockPos2 : iterable) {
                BlockState blockState;
                boolean bl;
                if (blockPos.equals(blockPos2) || !(bl = (blockState = this.mob.getEntityWorld().getBlockState(mutable2.set((Vec3i)blockPos2, Direction.DOWN))).getBlock() instanceof LeavesBlock || blockState.isIn(BlockTags.LOGS)) || !this.mob.getEntityWorld().isAir(blockPos2) || !this.mob.getEntityWorld().isAir(mutable.set((Vec3i)blockPos2, Direction.UP))) continue;
                return Vec3d.ofBottomCenter(blockPos2);
            }
            return null;
        }
    }
}

