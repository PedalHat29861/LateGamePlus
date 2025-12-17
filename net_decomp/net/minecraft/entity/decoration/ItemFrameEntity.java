/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.decoration;

import java.util.Objects;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class ItemFrameEntity
extends AbstractDecorationEntity {
    private static final TrackedData<ItemStack> ITEM_STACK = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Integer> ROTATION = DataTracker.registerData(ItemFrameEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int field_30454 = 8;
    private static final float field_51592 = 0.0625f;
    private static final float field_51593 = 0.75f;
    private static final float field_51594 = 0.75f;
    private static final byte DEFAULT_ITEM_ROTATION = 0;
    private static final float DEFAULT_ITEM_DROP_CHANCE = 1.0f;
    private static final boolean DEFAULT_INVISIBLE = false;
    private static final boolean DEFAULT_FIXED = false;
    private float itemDropChance = 1.0f;
    private boolean fixed = false;

    public ItemFrameEntity(EntityType<? extends ItemFrameEntity> entityType, World world) {
        super((EntityType<? extends AbstractDecorationEntity>)entityType, world);
        this.setInvisible(false);
    }

    public ItemFrameEntity(World world, BlockPos pos, Direction facing) {
        this(EntityType.ITEM_FRAME, world, pos, facing);
    }

    public ItemFrameEntity(EntityType<? extends ItemFrameEntity> type, World world, BlockPos pos, Direction facing) {
        super((EntityType<? extends AbstractDecorationEntity>)type, world, pos);
        this.setFacing(facing);
        this.setInvisible(false);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ITEM_STACK, ItemStack.EMPTY);
        builder.add(ROTATION, 0);
    }

    @Override
    protected void setFacing(Direction facing) {
        Objects.requireNonNull(facing);
        super.setFacingInternal(facing);
        if (facing.getAxis().isHorizontal()) {
            this.setPitch(0.0f);
            this.setYaw(facing.getHorizontalQuarterTurns() * 90);
        } else {
            this.setPitch(-90 * facing.getDirection().offset());
            this.setYaw(0.0f);
        }
        this.lastPitch = this.getPitch();
        this.lastYaw = this.getYaw();
        this.updateAttachmentPosition();
    }

    @Override
    protected final void updateAttachmentPosition() {
        super.updateAttachmentPosition();
        this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        return this.calculateBoundingBox(pos, side, this.containsMap());
    }

    @Override
    protected Box calculatePopOffCheckBoundingBox() {
        return this.calculateBoundingBox(this.attachedBlockPos, this.getHorizontalFacing(), false);
    }

    private Box calculateBoundingBox(BlockPos pos, Direction side, boolean map) {
        float f = 0.46875f;
        Vec3d vec3d = Vec3d.ofCenter(pos).offset(side, -0.46875);
        float g = map ? 1.0f : 0.75f;
        float h = map ? 1.0f : 0.75f;
        Direction.Axis axis = side.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : (double)g;
        double e = axis == Direction.Axis.Y ? 0.0625 : (double)h;
        double i = axis == Direction.Axis.Z ? 0.0625 : (double)g;
        return Box.of(vec3d, d, e, i);
    }

    @Override
    public boolean canStayAttached() {
        if (this.fixed) {
            return true;
        }
        if (this.shouldPopOff(this.calculatePopOffCheckBoundingBox())) {
            return false;
        }
        BlockState blockState = this.getEntityWorld().getBlockState(this.attachedBlockPos.offset(this.getHorizontalFacing().getOpposite()));
        if (!(blockState.isSolid() || this.getHorizontalFacing().getAxis().isHorizontal() && AbstractRedstoneGateBlock.isRedstoneGate(blockState))) {
            return false;
        }
        return this.hasNoIntersectingDecoration(true);
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        if (!this.fixed) {
            super.move(type, movement);
        }
    }

    @Override
    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        if (!this.fixed) {
            super.addVelocity(deltaX, deltaY, deltaZ);
        }
    }

    @Override
    public void kill(ServerWorld world) {
        this.removeFromFrame(this.getHeldItemStack());
        super.kill(world);
    }

    private boolean shouldDropHeldStackWhenDamaged(DamageSource damageSource) {
        return !damageSource.isIn(DamageTypeTags.IS_EXPLOSION) && !this.getHeldItemStack().isEmpty();
    }

    private static boolean canDamageWhenFixed(DamageSource damageSource) {
        return damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || damageSource.isSourceCreativePlayer();
    }

    @Override
    public boolean clientDamage(DamageSource source) {
        if (this.fixed && !ItemFrameEntity.canDamageWhenFixed(source)) {
            return false;
        }
        return !this.isAlwaysInvulnerableTo(source);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.fixed) {
            return ItemFrameEntity.canDamageWhenFixed(source) && super.damage(world, source, amount);
        }
        if (this.isAlwaysInvulnerableTo(source)) {
            return false;
        }
        if (this.shouldDropHeldStackWhenDamaged(source)) {
            this.dropHeldStack(world, source.getAttacker(), false);
            this.emitGameEvent(GameEvent.BLOCK_CHANGE, source.getAttacker());
            this.playSound(this.getRemoveItemSound(), 1.0f, 1.0f);
            return true;
        }
        return super.damage(world, source, amount);
    }

    public SoundEvent getRemoveItemSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 16.0;
        return distance < (d *= 64.0 * ItemFrameEntity.getRenderDistanceMultiplier()) * d;
    }

    @Override
    public void onBreak(ServerWorld world, @Nullable Entity breaker) {
        this.playSound(this.getBreakSound(), 1.0f, 1.0f);
        this.dropHeldStack(world, breaker, true);
        this.emitGameEvent(GameEvent.BLOCK_CHANGE, breaker);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_BREAK;
    }

    @Override
    public void onPlace() {
        this.playSound(this.getPlaceSound(), 1.0f, 1.0f);
    }

    public SoundEvent getPlaceSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_PLACE;
    }

    private void dropHeldStack(ServerWorld world, @Nullable Entity entity, boolean dropSelf) {
        PlayerEntity playerEntity;
        if (this.fixed) {
            return;
        }
        ItemStack itemStack = this.getHeldItemStack();
        this.setHeldItemStack(ItemStack.EMPTY);
        if (!world.getGameRules().getValue(GameRules.ENTITY_DROPS).booleanValue()) {
            if (entity == null) {
                this.removeFromFrame(itemStack);
            }
            return;
        }
        if (entity instanceof PlayerEntity && (playerEntity = (PlayerEntity)entity).isInCreativeMode()) {
            this.removeFromFrame(itemStack);
            return;
        }
        if (dropSelf) {
            this.dropStack(world, this.getAsItemStack());
        }
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            this.removeFromFrame(itemStack);
            if (this.random.nextFloat() < this.itemDropChance) {
                this.dropStack(world, itemStack);
            }
        }
    }

    private void removeFromFrame(ItemStack stack) {
        MapState mapState;
        MapIdComponent mapIdComponent = this.getMapId(stack);
        if (mapIdComponent != null && (mapState = FilledMapItem.getMapState(mapIdComponent, this.getEntityWorld())) != null) {
            mapState.removeFrame(this.attachedBlockPos, this.getId());
        }
        stack.setHolder(null);
    }

    public ItemStack getHeldItemStack() {
        return this.getDataTracker().get(ITEM_STACK);
    }

    public @Nullable MapIdComponent getMapId(ItemStack stack) {
        return stack.get(DataComponentTypes.MAP_ID);
    }

    public boolean containsMap() {
        return this.getHeldItemStack().contains(DataComponentTypes.MAP_ID);
    }

    public void setHeldItemStack(ItemStack stack) {
        this.setHeldItemStack(stack, true);
    }

    public void setHeldItemStack(ItemStack value, boolean update) {
        if (!value.isEmpty()) {
            value = value.copyWithCount(1);
        }
        this.setAsStackHolder(value);
        this.getDataTracker().set(ITEM_STACK, value);
        if (!value.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0f, 1.0f);
        }
        if (update && this.attachedBlockPos != null) {
            this.getEntityWorld().updateComparators(this.attachedBlockPos, Blocks.AIR);
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public @Nullable StackReference getStackReference(int slot) {
        if (slot == 0) {
            return StackReference.of(this::getHeldItemStack, this::setHeldItemStack);
        }
        return super.getStackReference(slot);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (data.equals(ITEM_STACK)) {
            this.setAsStackHolder(this.getHeldItemStack());
        }
    }

    private void setAsStackHolder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getFrame() != this) {
            stack.setHolder(this);
        }
        this.updateAttachmentPosition();
    }

    public int getRotation() {
        return this.getDataTracker().get(ROTATION);
    }

    public void setRotation(int value) {
        this.setRotation(value, true);
    }

    private void setRotation(int value, boolean updateComparators) {
        this.getDataTracker().set(ROTATION, value % 8);
        if (updateComparators && this.attachedBlockPos != null) {
            this.getEntityWorld().updateComparators(this.attachedBlockPos, Blocks.AIR);
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        ItemStack itemStack = this.getHeldItemStack();
        if (!itemStack.isEmpty()) {
            view.put("Item", ItemStack.CODEC, itemStack);
        }
        view.putByte("ItemRotation", (byte)this.getRotation());
        view.putFloat("ItemDropChance", this.itemDropChance);
        view.put("Facing", Direction.INDEX_CODEC, this.getHorizontalFacing());
        view.putBoolean("Invisible", this.isInvisible());
        view.putBoolean("Fixed", this.fixed);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        ItemStack itemStack = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        ItemStack itemStack2 = this.getHeldItemStack();
        if (!itemStack2.isEmpty() && !ItemStack.areEqual(itemStack, itemStack2)) {
            this.removeFromFrame(itemStack2);
        }
        this.setHeldItemStack(itemStack, false);
        this.setRotation(view.getByte("ItemRotation", (byte)0), false);
        this.itemDropChance = view.getFloat("ItemDropChance", 1.0f);
        this.setFacing(view.read("Facing", Direction.INDEX_CODEC).orElse(Direction.DOWN));
        this.setInvisible(view.getBoolean("Invisible", false));
        this.fixed = view.getBoolean("Fixed", false);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        boolean bl2;
        ItemStack itemStack = player.getStackInHand(hand);
        boolean bl = !this.getHeldItemStack().isEmpty();
        boolean bl3 = bl2 = !itemStack.isEmpty();
        if (this.fixed) {
            return ActionResult.PASS;
        }
        if (player.getEntityWorld().isClient()) {
            return bl || bl2 ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        if (!bl) {
            if (bl2 && !this.isRemoved()) {
                MapState mapState = FilledMapItem.getMapState(itemStack, this.getEntityWorld());
                if (mapState != null && mapState.decorationCountNotLessThan(256)) {
                    return ActionResult.FAIL;
                }
                this.setHeldItemStack(itemStack);
                this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
                itemStack.decrementUnlessCreative(1, player);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        this.playSound(this.getRotateItemSound(), 1.0f, 1.0f);
        this.setRotation(this.getRotation() + 1);
        this.emitGameEvent(GameEvent.BLOCK_CHANGE, player);
        return ActionResult.SUCCESS;
    }

    public SoundEvent getRotateItemSound() {
        return SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM;
    }

    public int getComparatorPower() {
        if (this.getHeldItemStack().isEmpty()) {
            return 0;
        }
        return this.getRotation() % 8 + 1;
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
        ItemStack itemStack = this.getHeldItemStack();
        if (itemStack.isEmpty()) {
            return this.getAsItemStack();
        }
        return itemStack.copy();
    }

    protected ItemStack getAsItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getBodyYaw() {
        Direction direction = this.getHorizontalFacing();
        int i = direction.getAxis().isVertical() ? 90 * direction.getDirection().offset() : 0;
        return MathHelper.wrapDegrees(180 + direction.getHorizontalQuarterTurns() * 90 + this.getRotation() * 45 + i);
    }
}

