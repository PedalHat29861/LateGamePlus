/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public interface Leashable {
    public static final String LEASH_NBT_KEY = "leash";
    public static final double DEFAULT_SNAPPING_DISTANCE = 12.0;
    public static final double DEFAULT_ELASTIC_DISTANCE = 6.0;
    public static final double field_60003 = 16.0;
    public static final Vec3d ELASTICITY_MULTIPLIER = new Vec3d(0.8, 0.2, 0.8);
    public static final float field_59997 = 0.7f;
    public static final double field_59998 = 10.0;
    public static final double field_59999 = 0.11;
    public static final List<Vec3d> HELD_ENTITY_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3d(0.0, 0.5, 0.5));
    public static final List<Vec3d> LEASH_HOLDER_ATTACHMENT_POINT = ImmutableList.of((Object)new Vec3d(0.0, 0.5, 0.0));
    public static final List<Vec3d> QUAD_LEASH_ATTACHMENT_POINTS = ImmutableList.of((Object)new Vec3d(-0.5, 0.5, 0.5), (Object)new Vec3d(-0.5, 0.5, -0.5), (Object)new Vec3d(0.5, 0.5, -0.5), (Object)new Vec3d(0.5, 0.5, 0.5));

    public @Nullable LeashData getLeashData();

    public void setLeashData(@Nullable LeashData var1);

    default public boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default public boolean mightBeLeashed() {
        return this.getLeashData() != null;
    }

    default public boolean canBeLeashedTo(Entity entity) {
        if (this == entity) {
            return false;
        }
        if (this.getDistanceToCenter(entity) > this.getLeashSnappingDistance()) {
            return false;
        }
        return this.canBeLeashed();
    }

    default public double getDistanceToCenter(Entity entity) {
        return entity.getBoundingBox().getCenter().distanceTo(((Entity)((Object)this)).getBoundingBox().getCenter());
    }

    default public boolean canBeLeashed() {
        return true;
    }

    default public void setUnresolvedLeashHolderId(int unresolvedLeashHolderId) {
        this.setLeashData(new LeashData(unresolvedLeashHolderId));
        Leashable.detachLeash((Entity)((Object)this), false, false);
    }

    default public void readLeashData(ReadView view) {
        LeashData leashData = view.read(LEASH_NBT_KEY, LeashData.CODEC).orElse(null);
        if (this.getLeashData() != null && leashData == null) {
            this.detachLeashWithoutDrop();
        }
        this.setLeashData(leashData);
    }

    default public void writeLeashData(WriteView view, @Nullable LeashData leashData) {
        view.putNullable(LEASH_NBT_KEY, LeashData.CODEC, leashData);
    }

    private static <E extends Entity> void resolveLeashData(E entity, LeashData leashData) {
        World world;
        if (leashData.unresolvedLeashData != null && (world = entity.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            Optional optional = leashData.unresolvedLeashData.left();
            Optional optional2 = leashData.unresolvedLeashData.right();
            if (optional.isPresent()) {
                Entity entity2 = serverWorld.getEntity((UUID)optional.get());
                if (entity2 != null) {
                    Leashable.attachLeash(entity, entity2, true);
                    return;
                }
            } else if (optional2.isPresent()) {
                Leashable.attachLeash(entity, LeashKnotEntity.getOrCreate(serverWorld, (BlockPos)optional2.get()), true);
                return;
            }
            if (entity.age > 100) {
                entity.dropItem(serverWorld, Items.LEAD);
                ((Leashable)((Object)entity)).setLeashData(null);
            }
        }
    }

    default public void detachLeash() {
        Leashable.detachLeash((Entity)((Object)this), true, true);
    }

    default public void detachLeashWithoutDrop() {
        Leashable.detachLeash((Entity)((Object)this), true, false);
    }

    default public void onLeashRemoved() {
    }

    private static <E extends Entity> void detachLeash(E entity, boolean sendPacket, boolean dropItem) {
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData != null && leashData.leashHolder != null) {
            ((Leashable)((Object)entity)).setLeashData(null);
            ((Leashable)((Object)entity)).onLeashRemoved();
            World world = entity.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                if (dropItem) {
                    entity.dropItem(serverWorld, Items.LEAD);
                }
                if (sendPacket) {
                    serverWorld.getChunkManager().sendToOtherNearbyPlayers(entity, new EntityAttachS2CPacket(entity, null));
                }
                leashData.leashHolder.onHeldLeashUpdate((Leashable)((Object)entity));
            }
        }
    }

    public static <E extends Entity> void tickLeash(ServerWorld world, E entity) {
        Entity entity2;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData != null && leashData.unresolvedLeashData != null) {
            Leashable.resolveLeashData(entity, leashData);
        }
        if (leashData == null || leashData.leashHolder == null) {
            return;
        }
        if (!entity.isInteractable() || !leashData.leashHolder.isInteractable()) {
            if (world.getGameRules().getValue(GameRules.ENTITY_DROPS).booleanValue()) {
                ((Leashable)((Object)entity)).detachLeash();
            } else {
                ((Leashable)((Object)entity)).detachLeashWithoutDrop();
            }
        }
        if ((entity2 = ((Leashable)((Object)entity)).getLeashHolder()) != null && entity2.getEntityWorld() == entity.getEntityWorld()) {
            double d = ((Leashable)((Object)entity)).getDistanceToCenter(entity2);
            ((Leashable)((Object)entity)).beforeLeashTick(entity2);
            if (d > ((Leashable)((Object)entity)).getLeashSnappingDistance()) {
                world.playSound(null, entity2.getX(), entity2.getY(), entity2.getZ(), SoundEvents.ITEM_LEAD_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                ((Leashable)((Object)entity)).snapLongLeash();
            } else if (d > ((Leashable)((Object)entity)).getElasticLeashDistance() - (double)entity2.getWidth() - (double)entity.getWidth() && ((Leashable)((Object)entity)).applyElasticity(entity2, leashData)) {
                ((Leashable)((Object)entity)).onLongLeashTick();
            } else {
                ((Leashable)((Object)entity)).onShortLeashTick(entity2);
            }
            entity.setYaw((float)((double)entity.getYaw() - leashData.momentum));
            leashData.momentum *= (double)Leashable.getSlipperiness(entity);
        }
    }

    default public void onLongLeashTick() {
        Entity entity = (Entity)((Object)this);
        entity.limitFallDistance();
    }

    default public double getLeashSnappingDistance() {
        return 12.0;
    }

    default public double getElasticLeashDistance() {
        return 6.0;
    }

    public static <E extends Entity> float getSlipperiness(E entity) {
        if (entity.isOnGround()) {
            return entity.getEntityWorld().getBlockState(entity.getVelocityAffectingPos()).getBlock().getSlipperiness() * 0.91f;
        }
        if (entity.isInFluid()) {
            return 0.8f;
        }
        return 0.91f;
    }

    default public void beforeLeashTick(Entity leashHolder) {
        leashHolder.tickHeldLeash(this);
    }

    default public void snapLongLeash() {
        this.detachLeash();
    }

    default public void onShortLeashTick(Entity entity) {
    }

    default public boolean applyElasticity(Entity leashHolder, LeashData leashData) {
        boolean bl = leashHolder.hasQuadLeashAttachmentPoints() && this.canUseQuadLeashAttachmentPoint();
        List<Elasticity> list = Leashable.calculateLeashElasticities((Entity)((Object)this), leashHolder, bl ? QUAD_LEASH_ATTACHMENT_POINTS : HELD_ENTITY_ATTACHMENT_POINT, bl ? QUAD_LEASH_ATTACHMENT_POINTS : LEASH_HOLDER_ATTACHMENT_POINT);
        if (list.isEmpty()) {
            return false;
        }
        Elasticity elasticity = Elasticity.sumOf(list).multiply(bl ? 0.25 : 1.0);
        leashData.momentum += 10.0 * elasticity.torque();
        Vec3d vec3d = Leashable.getLeashHolderMovement(leashHolder).subtract(((Entity)((Object)this)).getMovement());
        ((Entity)((Object)this)).addVelocityInternal(elasticity.force().multiply(ELASTICITY_MULTIPLIER).add(vec3d.multiply(0.11)));
        return true;
    }

    private static Vec3d getLeashHolderMovement(Entity leashHolder) {
        MobEntity mobEntity;
        if (leashHolder instanceof MobEntity && (mobEntity = (MobEntity)leashHolder).isAiDisabled()) {
            return Vec3d.ZERO;
        }
        return leashHolder.getMovement();
    }

    private static <E extends Entity> List<Elasticity> calculateLeashElasticities(E heldEntity, Entity leashHolder, List<Vec3d> heldEntityAttachmentPoints, List<Vec3d> leashHolderAttachmentPoints) {
        double d = ((Leashable)((Object)heldEntity)).getElasticLeashDistance();
        Vec3d vec3d = Leashable.getLeashHolderMovement(heldEntity);
        float f = heldEntity.getYaw() * ((float)Math.PI / 180);
        Vec3d vec3d2 = new Vec3d(heldEntity.getWidth(), heldEntity.getHeight(), heldEntity.getWidth());
        float g = leashHolder.getYaw() * ((float)Math.PI / 180);
        Vec3d vec3d3 = new Vec3d(leashHolder.getWidth(), leashHolder.getHeight(), leashHolder.getWidth());
        ArrayList<Elasticity> list = new ArrayList<Elasticity>();
        for (int i = 0; i < heldEntityAttachmentPoints.size(); ++i) {
            Vec3d vec3d4 = heldEntityAttachmentPoints.get(i).multiply(vec3d2).rotateY(-f);
            Vec3d vec3d5 = heldEntity.getEntityPos().add(vec3d4);
            Vec3d vec3d6 = leashHolderAttachmentPoints.get(i).multiply(vec3d3).rotateY(-g);
            Vec3d vec3d7 = leashHolder.getEntityPos().add(vec3d6);
            Leashable.calculateLeashElasticity(vec3d7, vec3d5, d, vec3d, vec3d4).ifPresent(list::add);
        }
        return list;
    }

    private static Optional<Elasticity> calculateLeashElasticity(Vec3d leashHolderAttachmentPos, Vec3d heldEntityAttachmentPos, double elasticDistance, Vec3d heldEntityMovement, Vec3d heldEntityAttachmentPoint) {
        boolean bl;
        double d = heldEntityAttachmentPos.distanceTo(leashHolderAttachmentPos);
        if (d < elasticDistance) {
            return Optional.empty();
        }
        Vec3d vec3d = leashHolderAttachmentPos.subtract(heldEntityAttachmentPos).normalize().multiply(d - elasticDistance);
        double e = Elasticity.calculateTorque(heldEntityAttachmentPoint, vec3d);
        boolean bl2 = bl = heldEntityMovement.dotProduct(vec3d) >= 0.0;
        if (bl) {
            vec3d = vec3d.multiply(0.3f);
        }
        return Optional.of(new Elasticity(vec3d, e));
    }

    default public boolean canUseQuadLeashAttachmentPoint() {
        return false;
    }

    default public Vec3d[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets((Entity)((Object)this), 0.0, 0.5, 0.5, 0.5);
    }

    public static Vec3d[] createQuadLeashOffsets(Entity leashedEntity, double addedZOffset, double zOffset, double xOffset, double yOffset) {
        float f = leashedEntity.getWidth();
        double d = addedZOffset * (double)f;
        double e = zOffset * (double)f;
        double g = xOffset * (double)f;
        double h = yOffset * (double)leashedEntity.getHeight();
        return new Vec3d[]{new Vec3d(-g, h, e + d), new Vec3d(-g, h, -e + d), new Vec3d(g, h, -e + d), new Vec3d(g, h, e + d)};
    }

    default public Vec3d getLeashOffset(float tickProgress) {
        return this.getLeashOffset();
    }

    default public Vec3d getLeashOffset() {
        Entity entity = (Entity)((Object)this);
        return new Vec3d(0.0, entity.getStandingEyeHeight(), entity.getWidth() * 0.4f);
    }

    default public void attachLeash(Entity leashHolder, boolean sendPacket) {
        if (this == leashHolder) {
            return;
        }
        Leashable.attachLeash((Entity)((Object)this), leashHolder, sendPacket);
    }

    private static <E extends Entity> void attachLeash(E entity, Entity leashHolder, boolean sendPacket) {
        World world;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData == null) {
            leashData = new LeashData(leashHolder);
            ((Leashable)((Object)entity)).setLeashData(leashData);
        } else {
            Entity entity2 = leashData.leashHolder;
            leashData.setLeashHolder(leashHolder);
            if (entity2 != null && entity2 != leashHolder) {
                entity2.onHeldLeashUpdate((Leashable)((Object)entity));
            }
        }
        if (sendPacket && (world = entity.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            serverWorld.getChunkManager().sendToOtherNearbyPlayers(entity, new EntityAttachS2CPacket(entity, leashHolder));
        }
        if (entity.hasVehicle()) {
            entity.stopRiding();
        }
    }

    default public @Nullable Entity getLeashHolder() {
        return Leashable.getLeashHolder((Entity)((Object)this));
    }

    private static <E extends Entity> @Nullable Entity getLeashHolder(E entity) {
        Entity entity2;
        LeashData leashData = ((Leashable)((Object)entity)).getLeashData();
        if (leashData == null) {
            return null;
        }
        if (leashData.unresolvedLeashHolderId != 0 && entity.getEntityWorld().isClient() && (entity2 = entity.getEntityWorld().getEntityById(leashData.unresolvedLeashHolderId)) instanceof Entity) {
            Entity entity22 = entity2;
            leashData.setLeashHolder(entity22);
        }
        return leashData.leashHolder;
    }

    public static List<Leashable> collectLeashablesHeldBy(Entity leashHolder) {
        return Leashable.collectLeashablesAround(leashHolder, leashable -> leashable.getLeashHolder() == leashHolder);
    }

    public static List<Leashable> collectLeashablesAround(Entity entity, Predicate<Leashable> leashablePredicate) {
        return Leashable.collectLeashablesAround(entity.getEntityWorld(), entity.getBoundingBox().getCenter(), leashablePredicate);
    }

    public static List<Leashable> collectLeashablesAround(World world, Vec3d pos, Predicate<Leashable> leashablePredicate) {
        double d = 32.0;
        Box box = Box.of(pos, 32.0, 32.0, 32.0);
        return world.getEntitiesByClass(Entity.class, box, entity -> {
            Leashable leashable;
            return entity instanceof Leashable && leashablePredicate.test(leashable = (Leashable)((Object)entity));
        }).stream().map(Leashable.class::cast).toList();
    }

    public static final class LeashData {
        public static final Codec<LeashData> CODEC = Codec.xor((Codec)Uuids.INT_STREAM_CODEC.fieldOf("UUID").codec(), BlockPos.CODEC).xmap(LeashData::new, data -> {
            Entity entity = data.leashHolder;
            if (entity instanceof LeashKnotEntity) {
                LeashKnotEntity leashKnotEntity = (LeashKnotEntity)entity;
                return Either.right((Object)leashKnotEntity.getAttachedBlockPos());
            }
            if (data.leashHolder != null) {
                return Either.left((Object)data.leashHolder.getUuid());
            }
            return Objects.requireNonNull(data.unresolvedLeashData, "Invalid LeashData had no attachment");
        });
        int unresolvedLeashHolderId;
        public @Nullable Entity leashHolder;
        public @Nullable Either<UUID, BlockPos> unresolvedLeashData;
        public double momentum;

        private LeashData(Either<UUID, BlockPos> unresolvedLeashData) {
            this.unresolvedLeashData = unresolvedLeashData;
        }

        LeashData(Entity leashHolder) {
            this.leashHolder = leashHolder;
        }

        LeashData(int unresolvedLeashHolderId) {
            this.unresolvedLeashHolderId = unresolvedLeashHolderId;
        }

        public void setLeashHolder(Entity leashHolder) {
            this.leashHolder = leashHolder;
            this.unresolvedLeashData = null;
            this.unresolvedLeashHolderId = 0;
        }
    }

    public record Elasticity(Vec3d force, double torque) {
        static Elasticity ZERO = new Elasticity(Vec3d.ZERO, 0.0);

        static double calculateTorque(Vec3d force, Vec3d force2) {
            return force.z * force2.x - force.x * force2.z;
        }

        static Elasticity sumOf(List<Elasticity> elasticities) {
            if (elasticities.isEmpty()) {
                return ZERO;
            }
            double d = 0.0;
            double e = 0.0;
            double f = 0.0;
            double g = 0.0;
            for (Elasticity elasticity : elasticities) {
                Vec3d vec3d = elasticity.force;
                d += vec3d.x;
                e += vec3d.y;
                f += vec3d.z;
                g += elasticity.torque;
            }
            return new Elasticity(new Vec3d(d, e, f), g);
        }

        public Elasticity multiply(double value) {
            return new Elasticity(this.force.multiply(value), this.torque * value);
        }
    }
}

