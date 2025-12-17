/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public class MoveItemsTask
extends MultiTickTask<PathAwareEntity> {
    public static final int INTERACTION_TICKS = 60;
    private static final int VISITED_POSITION_EXPIRY = 6000;
    private static final int MAX_STACK_SIZE_AT_ONCE = 16;
    private static final int VISITS_UNTIL_COOLDOWN = 10;
    private static final int field_62427 = 50;
    private static final int field_63014 = 1;
    private static final int COOLDOWN_EXPIRY = 140;
    private static final double QUEUING_RANGE = 3.0;
    private static final double INTERACTION_RANGE = 0.5;
    private static final double field_62428 = 1.0;
    private static final double field_62911 = 2.0;
    private final float speed;
    private final int horizontalRange;
    private final int verticalRange;
    private final Predicate<BlockState> inputContainerPredicate;
    private final Predicate<BlockState> outputContainerPredicate;
    private final Predicate<Storage> storagePredicate;
    private final Consumer<PathAwareEntity> travellingCallback;
    private final Map<InteractionState, InteractionCallback> interactionCallbacks;
    private @Nullable Storage targetStorage = null;
    private NavigationState navigationState;
    private @Nullable InteractionState interactionState;
    private int interactionTicks;

    public MoveItemsTask(float speed, Predicate<BlockState> inputContainerPredicate, Predicate<BlockState> outputChestPredicate, int horizontalRange, int verticalRange, Map<InteractionState, InteractionCallback> interactionCallbacks, Consumer<PathAwareEntity> travellingCallback, Predicate<Storage> storagePredicate) {
        super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.VISITED_BLOCK_POSITIONS, (Object)((Object)MemoryModuleState.REGISTERED), MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, (Object)((Object)MemoryModuleState.REGISTERED), MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryModuleState.VALUE_ABSENT)));
        this.speed = speed;
        this.inputContainerPredicate = inputContainerPredicate;
        this.outputContainerPredicate = outputChestPredicate;
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.travellingCallback = travellingCallback;
        this.storagePredicate = storagePredicate;
        this.interactionCallbacks = interactionCallbacks;
        this.navigationState = NavigationState.TRAVELLING;
    }

    @Override
    protected void run(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        EntityNavigation entityNavigation = pathAwareEntity.getNavigation();
        if (entityNavigation instanceof MobNavigation) {
            MobNavigation mobNavigation = (MobNavigation)entityNavigation;
            mobNavigation.setSkipRetarget(true);
        }
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, PathAwareEntity pathAwareEntity) {
        return !pathAwareEntity.isLeashed();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        return pathAwareEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS).isEmpty() && !pathAwareEntity.isPanicking() && !pathAwareEntity.isLeashed();
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        boolean bl = this.tick(serverWorld, pathAwareEntity);
        if (this.targetStorage == null) {
            this.finishRunning(serverWorld, pathAwareEntity, l);
            return;
        }
        if (bl) {
            return;
        }
        if (this.navigationState.equals((Object)NavigationState.QUEUING)) {
            this.tickQueuing(this.targetStorage, serverWorld, pathAwareEntity);
        }
        if (this.navigationState.equals((Object)NavigationState.TRAVELLING)) {
            this.tickTravelling(this.targetStorage, serverWorld, pathAwareEntity);
        }
        if (this.navigationState.equals((Object)NavigationState.INTERACTING)) {
            this.tickInteracting(this.targetStorage, serverWorld, pathAwareEntity);
        }
    }

    private boolean tick(ServerWorld world, PathAwareEntity entity) {
        if (!this.hasValidTargetStorage(world, entity)) {
            this.invalidateTargetStorage(entity);
            Optional<Storage> optional = this.findStorage(world, entity);
            if (optional.isPresent()) {
                this.targetStorage = optional.get();
                this.transitionToTravelling(entity);
                this.markVisited(entity, world, this.targetStorage.pos);
                return true;
            }
            this.cooldown(entity);
            return true;
        }
        return false;
    }

    private void tickQueuing(Storage storage, World world, PathAwareEntity entity) {
        if (!this.matchesStoragePredicate(storage, world)) {
            this.onCannotUseStorage(entity);
        }
    }

    protected void tickTravelling(Storage storage, World world, PathAwareEntity entity) {
        if (this.isWithinRange(3.0, storage, world, entity, this.atCenterY(entity)) && this.matchesStoragePredicate(storage, world)) {
            this.transitionToQueuing(entity);
        } else if (this.isWithinRange(MoveItemsTask.getSightRange(entity), storage, world, entity, this.atCenterY(entity))) {
            this.transitionToInteracting(storage, entity);
        } else {
            this.walkTowardsTargetStorage(entity);
        }
    }

    private Vec3d atCenterY(PathAwareEntity entity) {
        return this.atCenterY(entity, entity.getEntityPos());
    }

    protected void tickInteracting(Storage storage, World world, PathAwareEntity entity) {
        if (!this.isWithinRange(2.0, storage, world, entity, this.atCenterY(entity))) {
            this.transitionToTravelling(entity);
        } else {
            ++this.interactionTicks;
            this.setLookTarget(storage, entity);
            if (this.interactionTicks >= 60) {
                this.selectInteractionState(entity, storage.inventory, this::takeStack, (entityx, inventory) -> this.invalidateTargetStorage(entity), this::placeStack, (entityx, inventory) -> this.invalidateTargetStorage(entity));
                this.transitionToTravelling(entity);
            }
        }
    }

    private void transitionToQueuing(PathAwareEntity entity) {
        this.resetNavigation(entity);
        this.setNavigationState(NavigationState.QUEUING);
    }

    private void onCannotUseStorage(PathAwareEntity entity) {
        this.setNavigationState(NavigationState.TRAVELLING);
        this.walkTowardsTargetStorage(entity);
    }

    private void walkTowardsTargetStorage(PathAwareEntity entity) {
        if (this.targetStorage != null) {
            TargetUtil.walkTowards((LivingEntity)entity, this.targetStorage.pos, this.speed, 0);
        }
    }

    private void transitionToInteracting(Storage storage, PathAwareEntity entity) {
        this.selectInteractionState(entity, storage.inventory, this.createSetInteractionStateCallback(InteractionState.PICKUP_ITEM), this.createSetInteractionStateCallback(InteractionState.PICKUP_NO_ITEM), this.createSetInteractionStateCallback(InteractionState.PLACE_ITEM), this.createSetInteractionStateCallback(InteractionState.PLACE_NO_ITEM));
        this.setNavigationState(NavigationState.INTERACTING);
    }

    private void transitionToTravelling(PathAwareEntity entity) {
        this.travellingCallback.accept(entity);
        this.setNavigationState(NavigationState.TRAVELLING);
        this.interactionState = null;
        this.interactionTicks = 0;
    }

    private BiConsumer<PathAwareEntity, Inventory> createSetInteractionStateCallback(InteractionState state) {
        return (entity, inventory) -> this.setInteractionState(state);
    }

    private void setNavigationState(NavigationState state) {
        this.navigationState = state;
    }

    private void setInteractionState(InteractionState state) {
        this.interactionState = state;
    }

    private void setLookTarget(Storage storage, PathAwareEntity entity) {
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(storage.pos));
        this.resetNavigation(entity);
        if (this.interactionState != null) {
            Optional.ofNullable(this.interactionCallbacks.get((Object)this.interactionState)).ifPresent(consumer -> consumer.accept(entity, storage, this.interactionTicks));
        }
    }

    private void selectInteractionState(PathAwareEntity entity, Inventory inventory, BiConsumer<PathAwareEntity, Inventory> pickupItemCallback, BiConsumer<PathAwareEntity, Inventory> pickupNoItemCallback, BiConsumer<PathAwareEntity, Inventory> placeItemCallback, BiConsumer<PathAwareEntity, Inventory> placeNoItemCallback) {
        if (MoveItemsTask.canPickUpItem(entity)) {
            if (MoveItemsTask.hasItem(inventory)) {
                pickupItemCallback.accept(entity, inventory);
            } else {
                pickupNoItemCallback.accept(entity, inventory);
            }
        } else if (MoveItemsTask.canInsert(entity, inventory)) {
            placeItemCallback.accept(entity, inventory);
        } else {
            placeNoItemCallback.accept(entity, inventory);
        }
    }

    private Optional<Storage> findStorage(ServerWorld world, PathAwareEntity entity) {
        Box box = this.getSearchBoundingBox(entity);
        Set<GlobalPos> set = MoveItemsTask.getVisitedPositions(entity);
        Set<GlobalPos> set2 = MoveItemsTask.getUnreachablePositions(entity);
        List<ChunkPos> list = ChunkPos.stream(new ChunkPos(entity.getBlockPos()), Math.floorDiv(this.getHorizontalRange(entity), 16) + 1).toList();
        Storage storage = null;
        double d = 3.4028234663852886E38;
        for (ChunkPos chunkPos : list) {
            WorldChunk worldChunk = world.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);
            if (worldChunk == null) continue;
            for (BlockEntity blockEntity : worldChunk.getBlockEntities().values()) {
                Storage storage2;
                ChestBlockEntity chestBlockEntity;
                double e;
                if (!(blockEntity instanceof ChestBlockEntity) || !((e = (chestBlockEntity = (ChestBlockEntity)blockEntity).getPos().getSquaredDistance(entity.getEntityPos())) < d) || (storage2 = this.getStorageFor(entity, world, chestBlockEntity, set, set2, box)) == null) continue;
                storage = storage2;
                d = e;
            }
        }
        return storage == null ? Optional.empty() : Optional.of(storage);
    }

    private @Nullable Storage getStorageFor(PathAwareEntity entity, World world, BlockEntity blockEntity, Set<GlobalPos> visitedPositions, Set<GlobalPos> unreachablePositions, Box box) {
        BlockPos blockPos = blockEntity.getPos();
        boolean bl = box.contains(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (!bl) {
            return null;
        }
        Storage storage = Storage.forContainer(blockEntity, world);
        if (storage == null) {
            return null;
        }
        boolean bl2 = this.testContainer(entity, storage.state) && !this.hasVisited(visitedPositions, unreachablePositions, storage, world) && !this.isLocked(storage);
        return bl2 ? storage : null;
    }

    private boolean isLocked(Storage storage) {
        LockableContainerBlockEntity lockableContainerBlockEntity;
        BlockEntity blockEntity = storage.blockEntity;
        return blockEntity instanceof LockableContainerBlockEntity && (lockableContainerBlockEntity = (LockableContainerBlockEntity)blockEntity).isLocked();
    }

    private boolean hasValidTargetStorage(World world, PathAwareEntity entity) {
        boolean bl;
        boolean bl2 = bl = this.targetStorage != null && this.testContainer(entity, this.targetStorage.state) && this.isUnchanged(world, this.targetStorage);
        if (bl && !this.isChestBlocked(world, this.targetStorage)) {
            if (!this.navigationState.equals((Object)NavigationState.TRAVELLING)) {
                return true;
            }
            if (this.canNavigateTo(world, this.targetStorage, entity)) {
                return true;
            }
            this.markUnreachable(entity, world, this.targetStorage.pos);
        }
        return false;
    }

    private boolean canNavigateTo(World world, Storage storage, PathAwareEntity entity) {
        Path path = entity.getNavigation().getCurrentPath() == null ? entity.getNavigation().findPathTo(storage.pos, 0) : entity.getNavigation().getCurrentPath();
        Vec3d vec3d = this.getTargetPos(path, entity);
        boolean bl = this.isWithinRange(MoveItemsTask.getSightRange(entity), storage, world, entity, vec3d);
        boolean bl2 = path == null && !bl;
        return bl2 || this.isVisible(world, bl, vec3d, storage, entity);
    }

    private Vec3d getTargetPos(@Nullable Path path, PathAwareEntity entity) {
        boolean bl = path == null || path.getEnd() == null;
        Vec3d vec3d = bl ? entity.getEntityPos() : path.getEnd().getBlockPos().toBottomCenterPos();
        return this.atCenterY(entity, vec3d);
    }

    private Vec3d atCenterY(PathAwareEntity entity, Vec3d pos) {
        return pos.add(0.0, entity.getBoundingBox().getLengthY() / 2.0, 0.0);
    }

    private boolean isChestBlocked(World world, Storage storage) {
        return ChestBlock.isChestBlocked(world, storage.pos);
    }

    private boolean isUnchanged(World world, Storage storage) {
        return storage.blockEntity.equals(world.getBlockEntity(storage.pos));
    }

    private Stream<Storage> getContainerStorages(Storage storage, World world) {
        if (storage.state.get(ChestBlock.CHEST_TYPE, ChestType.SINGLE) != ChestType.SINGLE) {
            Storage storage2 = Storage.forContainer(ChestBlock.getPosInFrontOf(storage.pos, storage.state), world);
            return storage2 != null ? Stream.of(storage, storage2) : Stream.of(storage);
        }
        return Stream.of(storage);
    }

    private Box getSearchBoundingBox(PathAwareEntity entity) {
        int i = this.getHorizontalRange(entity);
        return new Box(entity.getBlockPos()).expand(i, this.getVerticalRange(entity), i);
    }

    private int getHorizontalRange(PathAwareEntity entity) {
        return entity.hasVehicle() ? 1 : this.horizontalRange;
    }

    private int getVerticalRange(PathAwareEntity entity) {
        return entity.hasVehicle() ? 1 : this.verticalRange;
    }

    private static Set<GlobalPos> getVisitedPositions(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
    }

    private boolean hasVisited(Set<GlobalPos> visitedPositions, Set<GlobalPos> checkedPositions, Storage storage, World visited) {
        return this.getContainerStorages(storage, visited).map(checkedStorage -> new GlobalPos(visited.getRegistryKey(), checkedStorage.pos)).anyMatch(pos -> visitedPositions.contains(pos) || checkedPositions.contains(pos));
    }

    private static boolean hasFinishedNavigation(PathAwareEntity entity) {
        return entity.getNavigation().getCurrentPath() != null && entity.getNavigation().getCurrentPath().isFinished();
    }

    protected void markVisited(PathAwareEntity entity, World world, BlockPos pos) {
        HashSet<GlobalPos> set = new HashSet<GlobalPos>(MoveItemsTask.getVisitedPositions(entity));
        set.add(new GlobalPos(world.getRegistryKey(), pos));
        if (set.size() > 10) {
            this.cooldown(entity);
        } else {
            entity.getBrain().remember(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
        }
    }

    protected void markUnreachable(PathAwareEntity entity, World world, BlockPos blockPos) {
        HashSet<GlobalPos> set = new HashSet<GlobalPos>(MoveItemsTask.getVisitedPositions(entity));
        set.remove(new GlobalPos(world.getRegistryKey(), blockPos));
        HashSet<GlobalPos> set2 = new HashSet<GlobalPos>(MoveItemsTask.getUnreachablePositions(entity));
        set2.add(new GlobalPos(world.getRegistryKey(), blockPos));
        if (set2.size() > 50) {
            this.cooldown(entity);
        } else {
            entity.getBrain().remember(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
            entity.getBrain().remember(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, set2, 6000L);
        }
    }

    private boolean testContainer(PathAwareEntity entity, BlockState state) {
        return MoveItemsTask.canPickUpItem(entity) ? this.inputContainerPredicate.test(state) : this.outputContainerPredicate.test(state);
    }

    private static double getSightRange(PathAwareEntity entity) {
        return MoveItemsTask.hasFinishedNavigation(entity) ? 1.0 : 0.5;
    }

    private boolean isWithinRange(double range, Storage storage, World world, PathAwareEntity entity, Vec3d pos) {
        Box box = entity.getBoundingBox();
        Box box2 = Box.of(pos, box.getLengthX(), box.getLengthY(), box.getLengthZ());
        return storage.state.getCollisionShape(world, storage.pos).getBoundingBox().expand(range, 0.5, range).offset(storage.pos).intersects(box2);
    }

    private boolean isVisible(World world, boolean nextToStorage, Vec3d pos, Storage storage, PathAwareEntity entity) {
        return nextToStorage && this.isVisible(storage, world, entity, pos);
    }

    private boolean isVisible(Storage storage, World world, PathAwareEntity entity, Vec3d pos) {
        Vec3d vec3d = storage.pos.toCenterPos();
        return Direction.stream().map(direction -> vec3d.add(0.5 * (double)direction.getOffsetX(), 0.5 * (double)direction.getOffsetY(), 0.5 * (double)direction.getOffsetZ())).map(storagePos -> world.raycast(new RaycastContext(pos, (Vec3d)storagePos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity))).anyMatch(hitResult -> hitResult.getType() == HitResult.Type.BLOCK && hitResult.getBlockPos().equals(storage.pos));
    }

    private boolean matchesStoragePredicate(Storage storage, World world) {
        return this.getContainerStorages(storage, world).anyMatch(this.storagePredicate);
    }

    private static boolean canPickUpItem(PathAwareEntity entity) {
        return entity.getMainHandStack().isEmpty();
    }

    private static boolean hasItem(Inventory inventory) {
        return !inventory.isEmpty();
    }

    private static boolean canInsert(PathAwareEntity entity, Inventory inventory) {
        return inventory.isEmpty() || MoveItemsTask.hasExistingStack(entity, inventory);
    }

    private static boolean hasExistingStack(PathAwareEntity entity, Inventory inventory) {
        ItemStack itemStack = entity.getMainHandStack();
        for (ItemStack itemStack2 : inventory) {
            if (!ItemStack.areItemsEqual(itemStack2, itemStack)) continue;
            return true;
        }
        return false;
    }

    private void takeStack(PathAwareEntity entity, Inventory inventory) {
        entity.equipStack(EquipmentSlot.MAINHAND, MoveItemsTask.extractStack(inventory));
        entity.setDropGuaranteed(EquipmentSlot.MAINHAND);
        inventory.markDirty();
        this.resetVisitedPositions(entity);
    }

    private void placeStack(PathAwareEntity entity, Inventory inventory) {
        ItemStack itemStack = MoveItemsTask.insertStack(entity, inventory);
        inventory.markDirty();
        entity.equipStack(EquipmentSlot.MAINHAND, itemStack);
        if (itemStack.isEmpty()) {
            this.resetVisitedPositions(entity);
        } else {
            this.invalidateTargetStorage(entity);
        }
    }

    private static ItemStack extractStack(Inventory inventory) {
        int i = 0;
        for (ItemStack itemStack : inventory) {
            if (!itemStack.isEmpty()) {
                int j = Math.min(itemStack.getCount(), 16);
                return inventory.removeStack(i, j);
            }
            ++i;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack insertStack(PathAwareEntity entity, Inventory inventory) {
        int i = 0;
        ItemStack itemStack = entity.getMainHandStack();
        for (ItemStack itemStack2 : inventory) {
            if (itemStack2.isEmpty()) {
                inventory.setStack(i, itemStack);
                return ItemStack.EMPTY;
            }
            if (ItemStack.areItemsAndComponentsEqual(itemStack2, itemStack) && itemStack2.getCount() < itemStack2.getMaxCount()) {
                int j = itemStack2.getMaxCount() - itemStack2.getCount();
                int k = Math.min(j, itemStack.getCount());
                itemStack2.setCount(itemStack2.getCount() + k);
                itemStack.setCount(itemStack.getCount() - j);
                inventory.setStack(i, itemStack2);
                if (itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            ++i;
        }
        return itemStack;
    }

    protected void invalidateTargetStorage(PathAwareEntity entity) {
        this.interactionTicks = 0;
        this.targetStorage = null;
        entity.getNavigation().stop();
        entity.getBrain().forget(MemoryModuleType.WALK_TARGET);
    }

    protected void resetVisitedPositions(PathAwareEntity entity) {
        this.invalidateTargetStorage(entity);
        entity.getBrain().forget(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        entity.getBrain().forget(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    private void cooldown(PathAwareEntity entity) {
        this.invalidateTargetStorage(entity);
        entity.getBrain().remember(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
        entity.getBrain().forget(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        entity.getBrain().forget(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        this.transitionToTravelling(pathAwareEntity);
        EntityNavigation entityNavigation = pathAwareEntity.getNavigation();
        if (entityNavigation instanceof MobNavigation) {
            MobNavigation mobNavigation = (MobNavigation)entityNavigation;
            mobNavigation.setSkipRetarget(false);
        }
    }

    private void resetNavigation(PathAwareEntity entity) {
        entity.getNavigation().stop();
        entity.setSidewaysSpeed(0.0f);
        entity.setUpwardSpeed(0.0f);
        entity.setMovementSpeed(0.0f);
        entity.setVelocity(0.0, entity.getVelocity().y, 0.0);
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (PathAwareEntity)entity, time);
    }

    public static final class Storage
    extends Record {
        final BlockPos pos;
        final Inventory inventory;
        final BlockEntity blockEntity;
        final BlockState state;

        public Storage(BlockPos pos, Inventory inventory, BlockEntity blockEntity, BlockState state) {
            this.pos = pos;
            this.inventory = inventory;
            this.blockEntity = blockEntity;
            this.state = state;
        }

        public static @Nullable Storage forContainer(BlockEntity blockEntity, World world) {
            BlockPos blockPos = blockEntity.getPos();
            BlockState blockState = blockEntity.getCachedState();
            Inventory inventory = Storage.getInventory(blockEntity, blockState, world, blockPos);
            if (inventory != null) {
                return new Storage(blockPos, inventory, blockEntity, blockState);
            }
            return null;
        }

        public static @Nullable Storage forContainer(BlockPos pos, World world) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            return blockEntity == null ? null : Storage.forContainer(blockEntity, world);
        }

        private static @Nullable Inventory getInventory(BlockEntity blockEntity, BlockState state, World world, BlockPos pos) {
            Block block = state.getBlock();
            if (block instanceof ChestBlock) {
                ChestBlock chestBlock = (ChestBlock)block;
                return ChestBlock.getInventory(chestBlock, state, world, pos, false);
            }
            if (blockEntity instanceof Inventory) {
                Inventory inventory = (Inventory)((Object)blockEntity);
                return inventory;
            }
            return null;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Storage.class, "pos;container;blockEntity;state", "pos", "inventory", "blockEntity", "state"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Storage.class, "pos;container;blockEntity;state", "pos", "inventory", "blockEntity", "state"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Storage.class, "pos;container;blockEntity;state", "pos", "inventory", "blockEntity", "state"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Inventory inventory() {
            return this.inventory;
        }

        public BlockEntity blockEntity() {
            return this.blockEntity;
        }

        public BlockState state() {
            return this.state;
        }
    }

    public static final class NavigationState
    extends Enum<NavigationState> {
        public static final /* enum */ NavigationState TRAVELLING = new NavigationState();
        public static final /* enum */ NavigationState QUEUING = new NavigationState();
        public static final /* enum */ NavigationState INTERACTING = new NavigationState();
        private static final /* synthetic */ NavigationState[] field_61253;

        public static NavigationState[] values() {
            return (NavigationState[])field_61253.clone();
        }

        public static NavigationState valueOf(String string) {
            return Enum.valueOf(NavigationState.class, string);
        }

        private static /* synthetic */ NavigationState[] method_72452() {
            return new NavigationState[]{TRAVELLING, QUEUING, INTERACTING};
        }

        static {
            field_61253 = NavigationState.method_72452();
        }
    }

    public static final class InteractionState
    extends Enum<InteractionState> {
        public static final /* enum */ InteractionState PICKUP_ITEM = new InteractionState();
        public static final /* enum */ InteractionState PICKUP_NO_ITEM = new InteractionState();
        public static final /* enum */ InteractionState PLACE_ITEM = new InteractionState();
        public static final /* enum */ InteractionState PLACE_NO_ITEM = new InteractionState();
        private static final /* synthetic */ InteractionState[] field_61249;

        public static InteractionState[] values() {
            return (InteractionState[])field_61249.clone();
        }

        public static InteractionState valueOf(String string) {
            return Enum.valueOf(InteractionState.class, string);
        }

        private static /* synthetic */ InteractionState[] method_72451() {
            return new InteractionState[]{PICKUP_ITEM, PICKUP_NO_ITEM, PLACE_ITEM, PLACE_NO_ITEM};
        }

        static {
            field_61249 = InteractionState.method_72451();
        }
    }

    @FunctionalInterface
    public static interface InteractionCallback
    extends TriConsumer<PathAwareEntity, Storage, Integer> {
    }
}

