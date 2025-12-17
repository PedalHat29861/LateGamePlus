/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class ExperienceOrbEntity
extends Entity {
    protected static final TrackedData<Integer> VALUE = DataTracker.registerData(ExperienceOrbEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DESPAWN_AGE = 6000;
    private static final int EXPENSIVE_UPDATE_INTERVAL = 20;
    private static final int field_30057 = 8;
    private static final int MERGING_CHANCE_FRACTION = 40;
    private static final double field_30059 = 0.5;
    private static final short DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_VALUE = 0;
    private static final int DEFAULT_COUNT = 1;
    private int orbAge = 0;
    private int health = 5;
    private int pickingCount = 1;
    private @Nullable PlayerEntity target;
    private final PositionInterpolator interpolator = new PositionInterpolator(this);

    public ExperienceOrbEntity(World world, double x, double y, double z, int amount) {
        this(world, new Vec3d(x, y, z), Vec3d.ZERO, amount);
    }

    public ExperienceOrbEntity(World world, Vec3d pos, Vec3d velocity, int amount) {
        this((EntityType<? extends ExperienceOrbEntity>)EntityType.EXPERIENCE_ORB, world);
        this.setPosition(pos);
        if (!world.isClient()) {
            this.setYaw(this.random.nextFloat() * 360.0f);
            Vec3d vec3d = new Vec3d((this.random.nextDouble() * 0.2 - 0.1) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2 - 0.1) * 2.0);
            if (velocity.lengthSquared() > 0.0 && velocity.dotProduct(vec3d) < 0.0) {
                vec3d = vec3d.multiply(-1.0);
            }
            double d = this.getBoundingBox().getAverageSideLength();
            this.setPosition(pos.add(velocity.normalize().multiply(d * 0.5)));
            this.setVelocity(vec3d);
            if (!world.isSpaceEmpty(this.getBoundingBox())) {
                this.tryMoveToOpenSpace(d);
            }
        }
        this.setValue(amount);
    }

    public ExperienceOrbEntity(EntityType<? extends ExperienceOrbEntity> entityType, World world) {
        super(entityType, world);
    }

    protected void tryMoveToOpenSpace(double boundingBoxLength) {
        Vec3d vec3d = this.getEntityPos().add(0.0, (double)this.getHeight() / 2.0, 0.0);
        VoxelShape voxelShape = VoxelShapes.cuboid(Box.of(vec3d, boundingBoxLength, boundingBoxLength, boundingBoxLength));
        this.getEntityWorld().findClosestCollision(this, voxelShape, vec3d, this.getWidth(), this.getHeight(), this.getWidth()).ifPresent(pos -> this.setPosition(pos.add(0.0, (double)(-this.getHeight()) / 2.0, 0.0)));
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(VALUE, 0);
    }

    @Override
    protected double getGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        boolean bl;
        this.interpolator.tick();
        if (this.firstUpdate && this.getEntityWorld().isClient()) {
            this.firstUpdate = false;
            return;
        }
        super.tick();
        boolean bl2 = bl = !this.getEntityWorld().isSpaceEmpty(this.getBoundingBox());
        if (this.isSubmergedIn(FluidTags.WATER)) {
            this.applyWaterMovement();
        } else if (!bl) {
            this.applyGravity();
        }
        if (this.getEntityWorld().getFluidState(this.getBlockPos()).isIn(FluidTags.LAVA)) {
            this.setVelocity((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        if (this.age % 20 == 1) {
            this.expensiveUpdate();
        }
        this.moveTowardsPlayer();
        if (this.target == null && !this.getEntityWorld().isClient() && bl) {
            boolean bl22;
            boolean bl3 = bl22 = !this.getEntityWorld().isSpaceEmpty(this.getBoundingBox().offset(this.getVelocity()));
            if (bl22) {
                this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
                this.velocityDirty = true;
            }
        }
        double d = this.getVelocity().y;
        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();
        float f = 0.98f;
        if (this.isOnGround()) {
            f = this.getEntityWorld().getBlockState(this.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.98f;
        }
        this.setVelocity(this.getVelocity().multiply(f));
        if (this.groundCollision && d < -this.getFinalGravity()) {
            this.setVelocity(new Vec3d(this.getVelocity().x, -d * 0.4, this.getVelocity().z));
        }
        ++this.orbAge;
        if (this.orbAge >= 6000) {
            this.discard();
        }
    }

    private void moveTowardsPlayer() {
        if (this.target == null || this.target.isSpectator() || this.target.squaredDistanceTo(this) > 64.0) {
            PlayerEntity playerEntity = this.getEntityWorld().getClosestPlayer(this, 8.0);
            this.target = playerEntity != null && !playerEntity.isSpectator() && !playerEntity.isDead() ? playerEntity : null;
        }
        if (this.target != null) {
            Vec3d vec3d = new Vec3d(this.target.getX() - this.getX(), this.target.getY() + (double)this.target.getStandingEyeHeight() / 2.0 - this.getY(), this.target.getZ() - this.getZ());
            double d = vec3d.lengthSquared();
            double e = 1.0 - Math.sqrt(d) / 8.0;
            this.setVelocity(this.getVelocity().add(vec3d.normalize().multiply(e * e * 0.1)));
        }
    }

    @Override
    public BlockPos getVelocityAffectingPos() {
        return this.getPosWithYOffset(0.999999f);
    }

    private void expensiveUpdate() {
        if (this.getEntityWorld() instanceof ServerWorld) {
            List<ExperienceOrbEntity> list = this.getEntityWorld().getEntitiesByType(TypeFilter.instanceOf(ExperienceOrbEntity.class), this.getBoundingBox().expand(0.5), this::isMergeable);
            for (ExperienceOrbEntity experienceOrbEntity : list) {
                this.merge(experienceOrbEntity);
            }
        }
    }

    public static void spawn(ServerWorld world, Vec3d pos, int amount) {
        ExperienceOrbEntity.spawn(world, pos, Vec3d.ZERO, amount);
    }

    public static void spawn(ServerWorld world, Vec3d pos, Vec3d velocity, int amount) {
        while (amount > 0) {
            int i = ExperienceOrbEntity.roundToOrbSize(amount);
            amount -= i;
            if (ExperienceOrbEntity.wasMergedIntoExistingOrb(world, pos, i)) continue;
            world.spawnEntity(new ExperienceOrbEntity(world, pos, velocity, i));
        }
    }

    private static boolean wasMergedIntoExistingOrb(ServerWorld world, Vec3d pos, int amount) {
        Box box = Box.of(pos, 1.0, 1.0, 1.0);
        int i = world.getRandom().nextInt(40);
        List<ExperienceOrbEntity> list = world.getEntitiesByType(TypeFilter.instanceOf(ExperienceOrbEntity.class), box, orb -> ExperienceOrbEntity.isMergeable(orb, i, amount));
        if (!list.isEmpty()) {
            ExperienceOrbEntity experienceOrbEntity = list.get(0);
            ++experienceOrbEntity.pickingCount;
            experienceOrbEntity.orbAge = 0;
            return true;
        }
        return false;
    }

    private boolean isMergeable(ExperienceOrbEntity other) {
        return other != this && ExperienceOrbEntity.isMergeable(other, this.getId(), this.getValue());
    }

    private static boolean isMergeable(ExperienceOrbEntity orb, int seed, int amount) {
        return !orb.isRemoved() && (orb.getId() - seed) % 40 == 0 && orb.getValue() == amount;
    }

    private void merge(ExperienceOrbEntity other) {
        this.pickingCount += other.pickingCount;
        this.orbAge = Math.min(this.orbAge, other.orbAge);
        other.discard();
    }

    private void applyWaterMovement() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x * (double)0.99f, Math.min(vec3d.y + (double)5.0E-4f, (double)0.06f), vec3d.z * (double)0.99f);
    }

    @Override
    protected void onSwimmingStart() {
    }

    @Override
    public final boolean clientDamage(DamageSource source) {
        return !this.isAlwaysInvulnerableTo(source);
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }
        this.scheduleVelocityUpdate();
        this.health = (int)((float)this.health - amount);
        if (this.health <= 0) {
            this.discard();
        }
        return true;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putShort("Health", (short)this.health);
        view.putShort("Age", (short)this.orbAge);
        view.putShort("Value", (short)this.getValue());
        view.putInt("Count", this.pickingCount);
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.health = view.getShort("Health", (short)5);
        this.orbAge = view.getShort("Age", (short)0);
        this.setValue(view.getShort("Value", (short)0));
        this.pickingCount = view.read("Count", Codecs.POSITIVE_INT).orElse(1);
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
        if (player.experiencePickUpDelay == 0) {
            player.experiencePickUpDelay = 2;
            player.sendPickup(this, 1);
            int i = this.repairPlayerGears(serverPlayerEntity, this.getValue());
            if (i > 0) {
                player.addExperience(i);
            }
            --this.pickingCount;
            if (this.pickingCount == 0) {
                this.discard();
            }
        }
    }

    private int repairPlayerGears(ServerPlayerEntity player, int amount) {
        Optional<EnchantmentEffectContext> optional = EnchantmentHelper.chooseEquipmentWith(EnchantmentEffectComponentTypes.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            int k;
            ItemStack itemStack = optional.get().stack();
            int i = EnchantmentHelper.getRepairWithExperience(player.getEntityWorld(), itemStack, amount);
            int j = Math.min(i, itemStack.getDamage());
            itemStack.setDamage(itemStack.getDamage() - j);
            if (j > 0 && (k = amount - j * amount / i) > 0) {
                return this.repairPlayerGears(player, k);
            }
            return 0;
        }
        return amount;
    }

    public int getValue() {
        return this.dataTracker.get(VALUE);
    }

    private void setValue(int value) {
        this.dataTracker.set(VALUE, value);
    }

    public int getOrbSize() {
        int i = this.getValue();
        if (i >= 2477) {
            return 10;
        }
        if (i >= 1237) {
            return 9;
        }
        if (i >= 617) {
            return 8;
        }
        if (i >= 307) {
            return 7;
        }
        if (i >= 149) {
            return 6;
        }
        if (i >= 73) {
            return 5;
        }
        if (i >= 37) {
            return 4;
        }
        if (i >= 17) {
            return 3;
        }
        if (i >= 7) {
            return 2;
        }
        if (i >= 3) {
            return 1;
        }
        return 0;
    }

    public static int roundToOrbSize(int value) {
        if (value >= 2477) {
            return 2477;
        }
        if (value >= 1237) {
            return 1237;
        }
        if (value >= 617) {
            return 617;
        }
        if (value >= 307) {
            return 307;
        }
        if (value >= 149) {
            return 149;
        }
        if (value >= 73) {
            return 73;
        }
        if (value >= 37) {
            return 37;
        }
        if (value >= 17) {
            return 17;
        }
        if (value >= 7) {
            return 7;
        }
        if (value >= 3) {
            return 3;
        }
        return 1;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.AMBIENT;
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }
}
