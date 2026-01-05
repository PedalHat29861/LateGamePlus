package com.pedalhat.lategameplus.block.entity;

import com.pedalhat.lategameplus.block.FusionForgeBlock;
import com.pedalhat.lategameplus.block.FusionForgeState;
import com.pedalhat.lategameplus.registry.ModBlockEntities;
import com.pedalhat.lategameplus.registry.ModItems;
import com.pedalhat.lategameplus.screen.FusionForgeScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FusionForgeBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, SidedInventory {
    public static final int INVENTORY_SIZE = 5;
    private static final int COOK_TIME_TOTAL = 200;
    private static final int FUEL_PER_CRAFT = 1600;
    private static final int FUEL_PER_TICK = FUEL_PER_CRAFT / COOK_TIME_TOTAL;
    private static final int IDLE_DELAY_TICKS = 20;
    private static final int[] TOP_SLOTS = {FusionForgeScreenHandler.INPUT_A_SLOT};
    private static final int[] BOTTOM_SLOTS = {FusionForgeScreenHandler.OUTPUT_SLOT};
    private static final int[] BACK_SLOTS = {FusionForgeScreenHandler.INPUT_B_SLOT};
    private static final int[] FUEL_SLOTS = {FusionForgeScreenHandler.FUEL_SLOT};
    private static final int[] EMPTY_SLOTS = {};

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(4) {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> COOK_TIME_TOTAL;
                case 2 -> fuelTicks;
                case 3 -> fuelMaxTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookTime = value;
                case 1 -> { }
                case 2 -> fuelTicks = value;
                case 3 -> fuelMaxTicks = value;
                default -> { }
            }
        }
    };
    private int cookTime;
    private int fuelTicks;
    private int fuelMaxTicks;
    private boolean hadCatalyst;
    private int idleDelayTicks;

    public FusionForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_FORGE, pos, state);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
        cookTime = view.getInt("cook_time", 0);
        fuelTicks = view.getInt("fuel_ticks", 0);
        fuelMaxTicks = view.getInt("fuel_max", 0);
        idleDelayTicks = view.getInt("idle_delay", 0);
        hadCatalyst = hasCatalyst();
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
        view.putInt("cook_time", cookTime);
        view.putInt("fuel_ticks", fuelTicks);
        view.putInt("fuel_max", fuelMaxTicks);
        view.putInt("idle_delay", idleDelayTicks);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.lategameplus.fusion_forge");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FusionForgeScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(inventory, slot);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case FusionForgeScreenHandler.FUEL_SLOT -> {
                World world = getWorld();
                if (world == null) yield false;
                yield world.getFuelRegistry().isFuel(stack);
            }
            case FusionForgeScreenHandler.CATALYST_SLOT -> stack.isOf(Items.NETHER_STAR);
            case FusionForgeScreenHandler.OUTPUT_SLOT -> false;
            default -> true;
        };
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.UP) {
            return TOP_SLOTS;
        }
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        Direction facing = getFacing();
        Direction back = facing.getOpposite();
        if (side == back) {
            return BACK_SLOTS;
        }
        Direction left = facing.rotateYCounterclockwise();
        Direction right = facing.rotateYClockwise();
        if (side == left || side == right) {
            return FUEL_SLOTS;
        }
        return EMPTY_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        if (direction == null) {
            return isValid(slot, stack);
        }
        if (direction == Direction.UP) {
            return slot == FusionForgeScreenHandler.INPUT_A_SLOT && isValid(slot, stack);
        }
        if (direction == Direction.DOWN) {
            return false;
        }
        Direction facing = getFacing();
        if (direction == facing.getOpposite()) {
            return slot == FusionForgeScreenHandler.INPUT_B_SLOT && isValid(slot, stack);
        }
        Direction left = facing.rotateYCounterclockwise();
        Direction right = facing.rotateYClockwise();
        if (direction == left || direction == right) {
            return slot == FusionForgeScreenHandler.FUEL_SLOT && isValid(slot, stack);
        }
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        if (direction == Direction.DOWN) {
            return slot == FusionForgeScreenHandler.OUTPUT_SLOT;
        }
        return false;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FusionForgeBlockEntity blockEntity) {
        if (world.isClient()) {
            return;
        }

        boolean dirty = false;
        boolean hasCatalyst = blockEntity.hasCatalyst();
        boolean canCraft = hasCatalyst && blockEntity.canCraft();
        boolean workingThisTick = false;

        if (hasCatalyst && !blockEntity.hadCatalyst) {
            world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.6f, 1.0f);
            blockEntity.hadCatalyst = true;
        }
        if (!hasCatalyst && blockEntity.hadCatalyst) {
            blockEntity.hadCatalyst = false;
            if (blockEntity.idleDelayTicks != 0) {
                blockEntity.idleDelayTicks = 0;
                dirty = true;
            }
        }

        if (!hasCatalyst) {
            if (blockEntity.cookTime != 0) {
                blockEntity.cookTime = 0;
                dirty = true;
            }
        } else if (canCraft) {
            if (blockEntity.fuelTicks < FUEL_PER_TICK) {
                if (blockEntity.consumeFuel(world.getFuelRegistry())) {
                    dirty = true;
                }
            }
            if (blockEntity.fuelTicks >= FUEL_PER_TICK) {
                blockEntity.fuelTicks -= FUEL_PER_TICK;
                blockEntity.cookTime++;
                workingThisTick = true;
                dirty = true;
                if (blockEntity.cookTime >= COOK_TIME_TOTAL) {
                    blockEntity.cookTime = 0;
                    blockEntity.craftOnce();
                    blockEntity.idleDelayTicks = IDLE_DELAY_TICKS;
                    dirty = true;
                }
            } else if (blockEntity.cookTime > 0) {
                blockEntity.cookTime = Math.max(0, blockEntity.cookTime - 1);
                dirty = true;
            }
        } else if (blockEntity.cookTime != 0) {
            blockEntity.cookTime = 0;
            dirty = true;
        }

        if (workingThisTick) {
            if (blockEntity.idleDelayTicks != IDLE_DELAY_TICKS) {
                blockEntity.idleDelayTicks = IDLE_DELAY_TICKS;
                dirty = true;
            }
        } else if (blockEntity.idleDelayTicks > 0) {
            blockEntity.idleDelayTicks--;
            dirty = true;
        }

        boolean showWorking = hasCatalyst && canCraft && (workingThisTick || blockEntity.cookTime > 0 || blockEntity.idleDelayTicks > 0);
        FusionForgeState targetState = !hasCatalyst
            ? FusionForgeState.DISABLED
            : (showWorking
                ? FusionForgeState.NETHER_WORKING
                : FusionForgeState.NETHER_DISABLED);

        FusionForgeState previousState = state.get(FusionForgeBlock.STATE);
        if (previousState != targetState) {
            if (targetState == FusionForgeState.DISABLED) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 0.6f, 1.0f);
            }
            world.setBlockState(pos, state.with(FusionForgeBlock.STATE, targetState), net.minecraft.block.Block.NOTIFY_LISTENERS);
        }

        if (blockEntity.fuelTicks <= 0 && blockEntity.fuelMaxTicks != 0) {
            blockEntity.fuelMaxTicks = 0;
            dirty = true;
        }

        if (dirty) {
            blockEntity.markDirty();
        }
    }

    private boolean hasCatalyst() {
        ItemStack stack = inventory.get(FusionForgeScreenHandler.CATALYST_SLOT);
        return !stack.isEmpty() && stack.isOf(Items.NETHER_STAR);
    }

    private boolean consumeFuel(FuelRegistry fuelRegistry) {
        ItemStack fuelStack = inventory.get(FusionForgeScreenHandler.FUEL_SLOT);
        if (fuelStack.isEmpty() || !fuelRegistry.isFuel(fuelStack)) {
            return false;
        }
        int fuelTime = fuelRegistry.getFuelTicks(fuelStack);
        if (fuelTime <= 0) {
            return false;
        }
        fuelTicks += fuelTime;
        fuelMaxTicks = fuelTime;
        fuelStack.decrement(1);
        return true;
    }

    private boolean canCraft() {
        if (!hasInputs()) {
            return false;
        }
        ItemStack output = inventory.get(FusionForgeScreenHandler.OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        if (!output.isOf(ModItems.NETHERITE_NUGGET)) {
            return false;
        }
        return output.getCount() + 3 <= output.getMaxCount();
    }

    private boolean hasInputs() {
        ItemStack a = inventory.get(FusionForgeScreenHandler.INPUT_A_SLOT);
        ItemStack b = inventory.get(FusionForgeScreenHandler.INPUT_B_SLOT);
        boolean first = a.isOf(Items.NETHERITE_SCRAP) && b.isOf(Items.GOLD_INGOT);
        boolean second = a.isOf(Items.GOLD_INGOT) && b.isOf(Items.NETHERITE_SCRAP);
        return first || second;
    }

    private void craftOnce() {
        ItemStack output = inventory.get(FusionForgeScreenHandler.OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.set(FusionForgeScreenHandler.OUTPUT_SLOT, new ItemStack(ModItems.NETHERITE_NUGGET, 3));
        } else {
            output.increment(3);
        }

        consumeInputs();
    }

    private void consumeInputs() {
        ItemStack a = inventory.get(FusionForgeScreenHandler.INPUT_A_SLOT);
        ItemStack b = inventory.get(FusionForgeScreenHandler.INPUT_B_SLOT);
        if (a.isOf(Items.NETHERITE_SCRAP)) {
            a.decrement(1);
        } else if (b.isOf(Items.NETHERITE_SCRAP)) {
            b.decrement(1);
        }
        if (a.isOf(Items.GOLD_INGOT)) {
            a.decrement(1);
        } else if (b.isOf(Items.GOLD_INGOT)) {
            b.decrement(1);
        }
    }

    private Direction getFacing() {
        BlockState state = getCachedState();
        if (state != null && state.contains(FusionForgeBlock.FACING)) {
            return state.get(FusionForgeBlock.FACING);
        }
        return Direction.NORTH;
    }
}
