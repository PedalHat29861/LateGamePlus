/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.decoration.painting;

import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Variants;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class PaintingEntity
extends AbstractDecorationEntity {
    private static final TrackedData<RegistryEntry<PaintingVariant>> VARIANT = DataTracker.registerData(PaintingEntity.class, TrackedDataHandlerRegistry.PAINTING_VARIANT);
    public static final float field_51595 = 0.0625f;

    public PaintingEntity(EntityType<? extends PaintingEntity> entityType, World world) {
        super((EntityType<? extends AbstractDecorationEntity>)entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variants.getDefaultOrThrow(this.getRegistryManager(), RegistryKeys.PAINTING_VARIANT));
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (VARIANT.equals(data)) {
            this.updateAttachmentPosition();
        }
    }

    private void setVariant(RegistryEntry<PaintingVariant> variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public RegistryEntry<PaintingVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.PAINTING_VARIANT) {
            return PaintingEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.PAINTING_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.PAINTING_VARIANT) {
            this.setVariant(PaintingEntity.castComponentValue(DataComponentTypes.PAINTING_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    public static Optional<PaintingEntity> placePainting(World world, BlockPos pos, Direction facing) {
        PaintingEntity paintingEntity = new PaintingEntity(world, pos);
        ArrayList<RegistryEntry> list = new ArrayList<RegistryEntry>();
        world.getRegistryManager().getOrThrow(RegistryKeys.PAINTING_VARIANT).iterateEntries(PaintingVariantTags.PLACEABLE).forEach(list::add);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        paintingEntity.setFacing(facing);
        list.removeIf(variant -> {
            paintingEntity.setVariant((RegistryEntry<PaintingVariant>)variant);
            return !paintingEntity.canStayAttached();
        });
        if (list.isEmpty()) {
            return Optional.empty();
        }
        int i = list.stream().mapToInt(PaintingEntity::getSize).max().orElse(0);
        list.removeIf(variant -> PaintingEntity.getSize(variant) < i);
        Optional optional = Util.getRandomOrEmpty(list, paintingEntity.random);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        paintingEntity.setVariant((RegistryEntry)optional.get());
        paintingEntity.setFacing(facing);
        return Optional.of(paintingEntity);
    }

    private static int getSize(RegistryEntry<PaintingVariant> variant) {
        return variant.value().getArea();
    }

    private PaintingEntity(World world, BlockPos pos) {
        super((EntityType<? extends AbstractDecorationEntity>)EntityType.PAINTING, world, pos);
    }

    public PaintingEntity(World world, BlockPos pos, Direction direction, RegistryEntry<PaintingVariant> variant) {
        this(world, pos);
        this.setVariant(variant);
        this.setFacing(direction);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.put("facing", Direction.HORIZONTAL_QUARTER_TURNS_CODEC, this.getHorizontalFacing());
        super.writeCustomData(view);
        Variants.writeData(view, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        Direction direction = view.read("facing", Direction.HORIZONTAL_QUARTER_TURNS_CODEC).orElse(Direction.SOUTH);
        super.readCustomData(view);
        this.setFacing(direction);
        Variants.fromData(view, RegistryKeys.PAINTING_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        float f = 0.46875f;
        Vec3d vec3d = Vec3d.ofCenter(pos).offset(side, -0.46875);
        PaintingVariant paintingVariant = this.getVariant().value();
        double d = this.getOffset(paintingVariant.width());
        double e = this.getOffset(paintingVariant.height());
        Direction direction = side.rotateYCounterclockwise();
        Vec3d vec3d2 = vec3d.offset(direction, d).offset(Direction.UP, e);
        Direction.Axis axis = side.getAxis();
        double g = axis == Direction.Axis.X ? 0.0625 : (double)paintingVariant.width();
        double h = paintingVariant.height();
        double i = axis == Direction.Axis.Z ? 0.0625 : (double)paintingVariant.width();
        return Box.of(vec3d2, g, h, i);
    }

    private double getOffset(int length) {
        return length % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void onBreak(ServerWorld world, @Nullable Entity breaker) {
        PlayerEntity playerEntity;
        if (!world.getGameRules().getValue(GameRules.ENTITY_DROPS).booleanValue()) {
            return;
        }
        this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0f, 1.0f);
        if (breaker instanceof PlayerEntity && (playerEntity = (PlayerEntity)breaker).isInCreativeMode()) {
            return;
        }
        this.dropItem(world, Items.PAINTING);
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0f, 1.0f);
    }

    @Override
    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        this.setPosition(x, y, z);
    }

    @Override
    public Vec3d getSyncedPos() {
        return Vec3d.of(this.attachedBlockPos);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, this.getHorizontalFacing().getIndex(), this.getAttachedBlockPos());
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.setFacing(Direction.byIndex(packet.getEntityData()));
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(Items.PAINTING);
    }
}

