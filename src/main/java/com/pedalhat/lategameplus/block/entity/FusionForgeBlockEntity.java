package com.pedalhat.lategameplus.block.entity;

import com.pedalhat.lategameplus.block.FusionForgeBlock;
import com.pedalhat.lategameplus.block.FusionForgeState;
import com.pedalhat.lategameplus.registry.ModBlockEntities;
import com.pedalhat.lategameplus.recipe.FusionForgeRecipe;
import com.pedalhat.lategameplus.recipe.FusionForgeRecipeInput;
import com.pedalhat.lategameplus.recipe.ModRecipes;
import com.pedalhat.lategameplus.screen.FusionForgeScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class FusionForgeBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
    public static final int INVENTORY_SIZE = 5;
    private static final int PROPERTY_COUNT = 6;
    private static final int DEFAULT_COOK_TIME = 200;
    private static final int DEFAULT_FUEL_COST = 1600;
    private static final int FALLBACK_LAVA_FUEL_TICKS = 20_000;
    private static final int MAX_LAVA_BUCKETS_STORED = 10;
    private static final int IDLE_DELAY_TICKS = 20;
    private static final int[] TOP_SLOTS = {FusionForgeScreenHandler.INPUT_A_SLOT};
    private static final int[] BOTTOM_SLOTS = {FusionForgeScreenHandler.OUTPUT_SLOT, FusionForgeScreenHandler.FUEL_SLOT};
    private static final int[] BACK_SLOTS = {FusionForgeScreenHandler.INPUT_B_SLOT};
    private static final int[] FUEL_SLOTS = {FusionForgeScreenHandler.FUEL_SLOT};
    private static final int[] EMPTY_SLOTS = {};

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private final ContainerData propertyDelegate = new SimpleContainerData(PROPERTY_COUNT) {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> cookTimeTotal;
                case 2 -> low16(fuelStoredTicks);
                case 3 -> high16(fuelStoredTicks);
                case 4 -> low16(fuelCapacityTicks);
                case 5 -> high16(fuelCapacityTicks);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookTime = value;
                case 1 -> cookTimeTotal = value;
                case 2 -> fuelStoredTicks = mergeLow16(fuelStoredTicks, value);
                case 3 -> fuelStoredTicks = mergeHigh16(fuelStoredTicks, value);
                case 4 -> fuelCapacityTicks = mergeLow16(fuelCapacityTicks, value);
                case 5 -> fuelCapacityTicks = mergeHigh16(fuelCapacityTicks, value);
                default -> { }
            }
        }
    };
    private int cookTime;
    private int cookTimeTotal = DEFAULT_COOK_TIME;
    private int fuelCost = DEFAULT_FUEL_COST;
    private int fuelStoredTicks;
    private int fuelCapacityTicks;
    private float storedExperience;
    private boolean hadCatalyst;
    private int idleDelayTicks;

    public FusionForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_FORGE, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ContainerHelper.loadAllItems(view, inventory);
        cookTime = view.getIntOr("cook_time", 0);
        fuelStoredTicks = Math.max(0, view.getIntOr("fuel_ticks", 0));
        fuelCapacityTicks = Math.max(0, view.getIntOr("fuel_capacity", view.getIntOr("fuel_max", 0)));
        if (fuelStoredTicks > fuelCapacityTicks && fuelCapacityTicks > 0) {
            fuelStoredTicks = fuelCapacityTicks;
        }
        storedExperience = view.getFloatOr("stored_exp", 0.0f);
        idleDelayTicks = view.getIntOr("idle_delay", 0);
        hadCatalyst = hasCatalyst();
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, inventory);
        view.putInt("cook_time", cookTime);
        view.putInt("fuel_ticks", fuelStoredTicks);
        view.putInt("fuel_capacity", fuelCapacityTicks);
        view.putInt("fuel_max", fuelCapacityTicks);
        view.putFloat("stored_exp", storedExperience);
        view.putInt("idle_delay", idleDelayTicks);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lategameplus.fusion_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FusionForgeScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public int getContainerSize() {
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
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(inventory, slot);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) {
            stack.setCount(stack.getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case FusionForgeScreenHandler.FUEL_SLOT -> {
                Level world = getLevel();
                if (world == null) yield false;
                yield world.fuelValues().isFuel(stack);
            }
            case FusionForgeScreenHandler.CATALYST_SLOT -> stack.is(Items.NETHER_STAR);
            case FusionForgeScreenHandler.OUTPUT_SLOT -> false;
            default -> true;
        };
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
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
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        if (side == left || side == right) {
            return FUEL_SLOTS;
        }
        return EMPTY_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @SuppressWarnings("null") Direction direction) {
        if (direction == null) {
            return canPlaceItem(slot, stack);
        }
        if (direction == Direction.UP) {
            return slot == FusionForgeScreenHandler.INPUT_A_SLOT && canPlaceItem(slot, stack);
        }
        if (direction == Direction.DOWN) {
            return false;
        }
        Direction facing = getFacing();
        if (direction == facing.getOpposite()) {
            return slot == FusionForgeScreenHandler.INPUT_B_SLOT && canPlaceItem(slot, stack);
        }
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        if (direction == left || direction == right) {
            return slot == FusionForgeScreenHandler.FUEL_SLOT && canPlaceItem(slot, stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if (direction == Direction.DOWN) {
            if (slot == FusionForgeScreenHandler.OUTPUT_SLOT) {
                return true;
            }
            return slot == FusionForgeScreenHandler.FUEL_SLOT && stack.is(Items.BUCKET);
        }
        return false;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, FusionForgeBlockEntity blockEntity) {
        if (world.isClientSide()) {
            return;
        }

        boolean dirty = false;
        FusionForgeRecipe recipe = blockEntity.getRecipe(world);
        if (blockEntity.updateRecipeValues(recipe)) {
            dirty = true;
        }
        if (blockEntity.syncFuelCapacity(world.fuelValues())) {
            dirty = true;
        }
        int fuelPerTick = blockEntity.getFuelPerTick();
        boolean hasCatalyst = blockEntity.hasCatalyst();
        boolean canCraft = recipe != null && blockEntity.canCraft(recipe);
        boolean workingThisTick = false;

        if (hasCatalyst && !blockEntity.hadCatalyst) {
            world.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.6f, 1.0f);
            if (world instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(
                    ParticleTypes.ENCHANT,
                    pos.getX() + 0.5,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5,
                    24,
                    0.6,
                    0.6,
                    0.6,
                    0.1
                );
            }
            blockEntity.hadCatalyst = true;
        }
        if (!hasCatalyst && blockEntity.hadCatalyst) {
            blockEntity.hadCatalyst = false;
            if (blockEntity.idleDelayTicks != 0) {
                blockEntity.idleDelayTicks = 0;
                dirty = true;
            }
        }

        if (blockEntity.fuelStoredTicks < blockEntity.fuelCapacityTicks) {
            int consumeGuard = 0;
            while (blockEntity.fuelStoredTicks < blockEntity.fuelCapacityTicks && consumeGuard++ < 64) {
                if (!blockEntity.consumeFuel(world.fuelValues())) {
                    break;
                }
                dirty = true;
            }
        }

        if (canCraft) {
            if (blockEntity.fuelStoredTicks >= fuelPerTick) {
                blockEntity.fuelStoredTicks -= fuelPerTick;
                blockEntity.cookTime++;
                workingThisTick = true;
                dirty = true;
                if (blockEntity.cookTime >= blockEntity.cookTimeTotal) {
                    blockEntity.cookTime = 0;
                    blockEntity.craftOnce(recipe);
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

        boolean showWorking = canCraft && (workingThisTick || blockEntity.cookTime > 0 || blockEntity.idleDelayTicks > 0);
        FusionForgeState targetState = hasCatalyst
            ? (showWorking ? FusionForgeState.NETHER_WORKING : FusionForgeState.NETHER_DISABLED)
            : (showWorking ? FusionForgeState.WORKING : FusionForgeState.DISABLED);

        FusionForgeState previousState = state.getValue(FusionForgeBlock.STATE);
        if (previousState != targetState) {
            if (targetState == FusionForgeState.DISABLED
                && (previousState == FusionForgeState.NETHER_DISABLED
                    || previousState == FusionForgeState.NETHER_WORKING)) {
                world.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6f, 1.0f);
            }
            world.setBlock(pos, state.setValue(FusionForgeBlock.STATE, targetState), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }

        if (dirty) {
            blockEntity.setChanged();
        }
    }

    private boolean hasCatalyst() {
        ItemStack stack = inventory.get(FusionForgeScreenHandler.CATALYST_SLOT);
        return !stack.isEmpty() && stack.is(Items.NETHER_STAR);
    }

    private boolean consumeFuel(FuelValues fuelRegistry) {
        ItemStack fuelStack = inventory.get(FusionForgeScreenHandler.FUEL_SLOT);
        if (fuelStack.isEmpty() || !fuelRegistry.isFuel(fuelStack)) {
            return false;
        }
        int fuelTime = fuelRegistry.burnDuration(fuelStack);
        if (fuelTime <= 0) {
            return false;
        }
        int capacity = getFuelCapacity(fuelRegistry);
        if (fuelStoredTicks + fuelTime > capacity) {
            return false;
        }

        ItemStackTemplate remainderTemplate = fuelStack.getItem().getCraftingRemainder();
        ItemStack remainder = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
        if (remainder.isEmpty()) {
            fuelStack.shrink(1);
        } else if (fuelStack.getCount() == 1) {
            inventory.set(FusionForgeScreenHandler.FUEL_SLOT, remainder.copy());
        } else {
            fuelStack.shrink(1);
        }
        fuelCapacityTicks = capacity;
        fuelStoredTicks = Math.min(fuelCapacityTicks, fuelStoredTicks + fuelTime);
        return true;
    }

    private boolean syncFuelCapacity(FuelValues fuelRegistry) {
        int capacity = getFuelCapacity(fuelRegistry);
        boolean changed = false;
        if (fuelCapacityTicks != capacity) {
            fuelCapacityTicks = capacity;
            changed = true;
        }
        if (fuelStoredTicks > fuelCapacityTicks) {
            fuelStoredTicks = fuelCapacityTicks;
            changed = true;
        }
        return changed;
    }

    private static int getFuelCapacity(FuelValues fuelRegistry) {
        int lavaFuel = fuelRegistry.burnDuration(new ItemStack(Items.LAVA_BUCKET));
        if (lavaFuel <= 0) {
            lavaFuel = FALLBACK_LAVA_FUEL_TICKS;
        }
        long capacity = (long) lavaFuel * MAX_LAVA_BUCKETS_STORED;
        return (int) Math.min(Integer.MAX_VALUE, capacity);
    }

    private FusionForgeRecipe getRecipe(Level world) {
        if (!(world.recipeAccess() instanceof RecipeManager recipeManager)) {
            return null;
        }
        FusionForgeRecipeInput input = createRecipeInput();
        FusionForgeRecipe match = recipeManager.getRecipeFor(ModRecipes.FUSION_FORGE, input, world)
            .map(RecipeHolder::value)
            .orElse(null);
        if (match != null) {
            return match;
        }
        FusionForgeRecipe fallback = findRecipeFallback(recipeManager, input, world);
        if (fallback != null) {
            return fallback;
        }
        return null;
    }

    private FusionForgeRecipeInput createRecipeInput() {
        return new FusionForgeRecipeInput(
            inventory.get(FusionForgeScreenHandler.INPUT_A_SLOT),
            inventory.get(FusionForgeScreenHandler.INPUT_B_SLOT)
        );
    }

    private boolean updateRecipeValues(FusionForgeRecipe recipe) {
        int newCookTime = recipe != null ? Math.max(1, recipe.getCookTime()) : DEFAULT_COOK_TIME;
        int newFuelCost = recipe != null ? Math.max(1, recipe.getFuelCost()) : DEFAULT_FUEL_COST;
        boolean changed = false;
        if (cookTimeTotal != newCookTime) {
            cookTimeTotal = newCookTime;
            changed = true;
        }
        if (fuelCost != newFuelCost) {
            fuelCost = newFuelCost;
            changed = true;
        }
        if (cookTime > cookTimeTotal) {
            cookTime = cookTimeTotal;
            changed = true;
        }
        return changed;
    }

    private int getFuelPerTick() {
        int total = Math.max(1, cookTimeTotal);
        int cost = Math.max(1, fuelCost);
        return Math.max(1, cost / total);
    }

    private boolean canCraft(FusionForgeRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        ItemStack output = inventory.get(FusionForgeScreenHandler.OUTPUT_SLOT);
        ItemStack result = recipe.getOutput();
        int multiplier = hasCatalyst() ? 2 : 1;
        int totalCount = result.getCount() * multiplier;
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, result)) {
            return false;
        }
        return output.getCount() + totalCount <= output.getMaxStackSize();
    }

    private void craftOnce(FusionForgeRecipe recipe) {
        ItemStack result = recipe.getOutput();
        int multiplier = hasCatalyst() ? 2 : 1;
        int totalCount = result.getCount() * multiplier;
        ItemStack output = inventory.get(FusionForgeScreenHandler.OUTPUT_SLOT);
        if (output.isEmpty()) {
            ItemStack toInsert = result.copy();
            toInsert.setCount(totalCount);
            inventory.set(FusionForgeScreenHandler.OUTPUT_SLOT, toInsert);
        } else {
            output.grow(totalCount);
        }
        addExperience(recipe);

        consumeInputs(recipe);
    }

    private void addExperience(FusionForgeRecipe recipe) {
        float perCraft = recipe.getExperience();
        if (perCraft <= 0.0f) {
            return;
        }
        storedExperience += perCraft * (hasCatalyst() ? 2 : 1);
    }

    private void consumeInputs(FusionForgeRecipe recipe) {
        ItemStack a = inventory.get(FusionForgeScreenHandler.INPUT_A_SLOT);
        ItemStack b = inventory.get(FusionForgeScreenHandler.INPUT_B_SLOT);
        boolean direct = recipe.getInputA().test(a) && recipe.getInputB().test(b);
        boolean swapped = recipe.getInputA().test(b) && recipe.getInputB().test(a);
        if (direct || swapped) {
            a.shrink(1);
            b.shrink(1);
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state != null && state.hasProperty(FusionForgeBlock.FACING)) {
            return state.getValue(FusionForgeBlock.FACING);
        }
        return Direction.NORTH;
    }

    private FusionForgeRecipe findRecipeFallback(RecipeManager recipeManager, FusionForgeRecipeInput input, Level world) {
        for (RecipeHolder<?> entry : recipeManager.getRecipes()) {
            if (entry.value() instanceof FusionForgeRecipe recipe && recipe.matches(input, world)) {
                return recipe;
            }
        }
        return null;
    }

    public void onOutputTaken(Player player) {
        if (storedExperience <= 0.0f) {
            return;
        }
        Level world = getLevel();
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }
        int xp = popStoredExperience(serverWorld);
        if (xp > 0) {
            ExperienceOrb.award(serverWorld, player.position(), xp);
        }
    }

    public void dropStoredExperience(ServerLevel world) {
        if (storedExperience <= 0.0f) {
            return;
        }
        int xp = popStoredExperience(world);
        if (xp > 0) {
            ExperienceOrb.award(world, Vec3.atCenterOf(worldPosition), xp);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Level world = getLevel();
        if (world instanceof ServerLevel serverWorld) {
            dropStoredExperience(serverWorld);
        }
    }

    private int popStoredExperience(ServerLevel world) {
        int whole = Mth.floor(storedExperience);
        float fractional = storedExperience - whole;
        if (fractional > 0.0f && world.getRandom().nextFloat() < fractional) {
            whole += 1;
        }
        storedExperience = 0.0f;
        setChanged();
        return whole;
    }

    private static int low16(int value) {
        return value & 0xFFFF;
    }

    private static int high16(int value) {
        return (value >>> 16) & 0xFFFF;
    }

    private static int mergeLow16(int original, int low) {
        return (original & 0xFFFF0000) | (low & 0xFFFF);
    }

    private static int mergeHigh16(int original, int high) {
        return (original & 0x0000FFFF) | ((high & 0xFFFF) << 16);
    }
}
