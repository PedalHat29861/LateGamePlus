/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class AbstractFurnaceBlockEntity
extends LockableContainerBlockEntity
implements SidedInventory,
RecipeUnlocker,
RecipeInputProvider {
    protected static final int INPUT_SLOT_INDEX = 0;
    protected static final int FUEL_SLOT_INDEX = 1;
    protected static final int OUTPUT_SLOT_INDEX = 2;
    public static final int BURN_TIME_PROPERTY_INDEX = 0;
    private static final int[] TOP_SLOTS = new int[]{0};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
    private static final int[] SIDE_SLOTS = new int[]{1};
    public static final int FUEL_TIME_PROPERTY_INDEX = 1;
    public static final int COOK_TIME_PROPERTY_INDEX = 2;
    public static final int COOK_TIME_TOTAL_PROPERTY_INDEX = 3;
    public static final int PROPERTY_COUNT = 4;
    public static final int DEFAULT_COOK_TIME = 200;
    public static final int field_31295 = 2;
    private static final Codec<Map<RegistryKey<Recipe<?>>, Integer>> CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, (Codec)Codec.INT);
    private static final short DEFAULT_LIT_TIME_REMAINING = 0;
    private static final short DEFAULT_LIT_TOTAL_TIME = 0;
    private static final short DEFAULT_COOKING_TIME_SPENT = 0;
    private static final short DEFAULT_COOKING_TOTAL_TIME = 0;
    protected DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    int litTimeRemaining;
    int litTotalTime;
    int cookingTimeSpent;
    int cookingTotalTime;
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate(){

        @Override
        public int get(int index) {
            switch (index) {
                case 0: {
                    return AbstractFurnaceBlockEntity.this.litTimeRemaining;
                }
                case 1: {
                    return AbstractFurnaceBlockEntity.this.litTotalTime;
                }
                case 2: {
                    return AbstractFurnaceBlockEntity.this.cookingTimeSpent;
                }
                case 3: {
                    return AbstractFurnaceBlockEntity.this.cookingTotalTime;
                }
            }
            return 0;
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: {
                    AbstractFurnaceBlockEntity.this.litTimeRemaining = value;
                    break;
                }
                case 1: {
                    AbstractFurnaceBlockEntity.this.litTotalTime = value;
                    break;
                }
                case 2: {
                    AbstractFurnaceBlockEntity.this.cookingTimeSpent = value;
                    break;
                }
                case 3: {
                    AbstractFurnaceBlockEntity.this.cookingTotalTime = value;
                    break;
                }
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };
    private final Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap();
    private final ServerRecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType, pos, state);
        this.matchGetter = ServerRecipeManager.createCachedMatchGetter(recipeType);
    }

    private boolean isBurning() {
        return this.litTimeRemaining > 0;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readData(view, this.inventory);
        this.cookingTimeSpent = view.getShort("cooking_time_spent", (short)0);
        this.cookingTotalTime = view.getShort("cooking_total_time", (short)0);
        this.litTimeRemaining = view.getShort("lit_time_remaining", (short)0);
        this.litTotalTime = view.getShort("lit_total_time", (short)0);
        this.recipesUsed.clear();
        this.recipesUsed.putAll(view.read("RecipesUsed", CODEC).orElse(Map.of()));
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putShort("cooking_time_spent", (short)this.cookingTimeSpent);
        view.putShort("cooking_total_time", (short)this.cookingTotalTime);
        view.putShort("lit_time_remaining", (short)this.litTimeRemaining);
        view.putShort("lit_total_time", (short)this.litTotalTime);
        Inventories.writeData(view, this.inventory);
        view.put("RecipesUsed", CODEC, this.recipesUsed);
    }

    public static void tick(ServerWorld world, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity) {
        boolean bl4;
        boolean bl = blockEntity.isBurning();
        boolean bl2 = false;
        if (blockEntity.isBurning()) {
            --blockEntity.litTimeRemaining;
        }
        ItemStack itemStack = blockEntity.inventory.get(1);
        ItemStack itemStack2 = blockEntity.inventory.get(0);
        boolean bl3 = !itemStack2.isEmpty();
        boolean bl5 = bl4 = !itemStack.isEmpty();
        if (blockEntity.isBurning() || bl4 && bl3) {
            SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(itemStack2);
            RecipeEntry recipeEntry = bl3 ? (RecipeEntry)blockEntity.matchGetter.getFirstMatch(singleStackRecipeInput, world).orElse(null) : null;
            int i = blockEntity.getMaxCountPerStack();
            if (!blockEntity.isBurning() && AbstractFurnaceBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, singleStackRecipeInput, blockEntity.inventory, i)) {
                blockEntity.litTotalTime = blockEntity.litTimeRemaining = blockEntity.getFuelTime(world.getFuelRegistry(), itemStack);
                if (blockEntity.isBurning()) {
                    bl2 = true;
                    if (bl4) {
                        Item item = itemStack.getItem();
                        itemStack.decrement(1);
                        if (itemStack.isEmpty()) {
                            blockEntity.inventory.set(1, item.getRecipeRemainder());
                        }
                    }
                }
            }
            if (blockEntity.isBurning() && AbstractFurnaceBlockEntity.canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, singleStackRecipeInput, blockEntity.inventory, i)) {
                ++blockEntity.cookingTimeSpent;
                if (blockEntity.cookingTimeSpent == blockEntity.cookingTotalTime) {
                    blockEntity.cookingTimeSpent = 0;
                    blockEntity.cookingTotalTime = AbstractFurnaceBlockEntity.getCookTime(world, blockEntity);
                    if (AbstractFurnaceBlockEntity.craftRecipe(world.getRegistryManager(), recipeEntry, singleStackRecipeInput, blockEntity.inventory, i)) {
                        blockEntity.setLastRecipe(recipeEntry);
                    }
                    bl2 = true;
                }
            } else {
                blockEntity.cookingTimeSpent = 0;
            }
        } else if (!blockEntity.isBurning() && blockEntity.cookingTimeSpent > 0) {
            blockEntity.cookingTimeSpent = MathHelper.clamp(blockEntity.cookingTimeSpent - 2, 0, blockEntity.cookingTotalTime);
        }
        if (bl != blockEntity.isBurning()) {
            bl2 = true;
            state = (BlockState)state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
            world.setBlockState(pos, state, 3);
        }
        if (bl2) {
            AbstractFurnaceBlockEntity.markDirty(world, pos, state);
        }
    }

    private static boolean canAcceptRecipeOutput(DynamicRegistryManager dynamicRegistryManager, @Nullable RecipeEntry<? extends AbstractCookingRecipe> recipe, SingleStackRecipeInput input, DefaultedList<ItemStack> inventory, int maxCount) {
        if (inventory.get(0).isEmpty() || recipe == null) {
            return false;
        }
        ItemStack itemStack = recipe.value().craft(input, (RegistryWrapper.WrapperLookup)dynamicRegistryManager);
        if (itemStack.isEmpty()) {
            return false;
        }
        ItemStack itemStack2 = inventory.get(2);
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (!ItemStack.areItemsAndComponentsEqual(itemStack2, itemStack)) {
            return false;
        }
        if (itemStack2.getCount() < maxCount && itemStack2.getCount() < itemStack2.getMaxCount()) {
            return true;
        }
        return itemStack2.getCount() < itemStack.getMaxCount();
    }

    private static boolean craftRecipe(DynamicRegistryManager dynamicRegistryManager, @Nullable RecipeEntry<? extends AbstractCookingRecipe> recipe, SingleStackRecipeInput input, DefaultedList<ItemStack> inventory, int maxCount) {
        if (recipe == null || !AbstractFurnaceBlockEntity.canAcceptRecipeOutput(dynamicRegistryManager, recipe, input, inventory, maxCount)) {
            return false;
        }
        ItemStack itemStack = inventory.get(0);
        ItemStack itemStack2 = recipe.value().craft(input, (RegistryWrapper.WrapperLookup)dynamicRegistryManager);
        ItemStack itemStack3 = inventory.get(2);
        if (itemStack3.isEmpty()) {
            inventory.set(2, itemStack2.copy());
        } else if (ItemStack.areItemsAndComponentsEqual(itemStack3, itemStack2)) {
            itemStack3.increment(1);
        }
        if (itemStack.isOf(Blocks.WET_SPONGE.asItem()) && !inventory.get(1).isEmpty() && inventory.get(1).isOf(Items.BUCKET)) {
            inventory.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        itemStack.decrement(1);
        return true;
    }

    protected int getFuelTime(FuelRegistry fuelRegistry, ItemStack stack) {
        return fuelRegistry.getFuelTicks(stack);
    }

    private static int getCookTime(ServerWorld world, AbstractFurnaceBlockEntity furnace) {
        SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(furnace.getStack(0));
        return furnace.matchGetter.getFirstMatch(singleStackRecipeInput, world).map(recipe -> ((AbstractCookingRecipe)recipe.value()).getCookingTime()).orElse(200);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        }
        if (side == Direction.UP) {
            return TOP_SLOTS;
        }
        return SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == 1) {
            return stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET);
        }
        return true;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        World world;
        ItemStack itemStack = this.inventory.get(slot);
        boolean bl = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(itemStack, stack);
        this.inventory.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        if (slot == 0 && !bl && (world = this.world) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.cookingTotalTime = AbstractFurnaceBlockEntity.getCookTime(serverWorld, this);
            this.cookingTimeSpent = 0;
            this.markDirty();
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 2) {
            return false;
        }
        if (slot == 1) {
            ItemStack itemStack = this.inventory.get(1);
            return this.world.getFuelRegistry().isFuel(stack) || stack.isOf(Items.BUCKET) && !itemStack.isOf(Items.BUCKET);
        }
        return true;
    }

    @Override
    public void setLastRecipe(@Nullable RecipeEntry<?> recipe) {
        if (recipe != null) {
            RegistryKey<Recipe<?>> registryKey = recipe.id();
            this.recipesUsed.addTo(registryKey, 1);
        }
    }

    @Override
    public @Nullable RecipeEntry<?> getLastRecipe() {
        return null;
    }

    @Override
    public void unlockLastRecipe(PlayerEntity player, List<ItemStack> ingredients) {
    }

    public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
        List<RecipeEntry<?>> list = this.getRecipesUsedAndDropExperience(player.getEntityWorld(), player.getEntityPos());
        player.unlockRecipes((Collection<RecipeEntry<?>>)list);
        for (RecipeEntry<?> recipeEntry : list) {
            player.onRecipeCrafted(recipeEntry, this.inventory);
        }
        this.recipesUsed.clear();
    }

    public List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        ArrayList list = Lists.newArrayList();
        for (Reference2IntMap.Entry entry : this.recipesUsed.reference2IntEntrySet()) {
            world.getRecipeManager().get((RegistryKey)entry.getKey()).ifPresent(recipe -> {
                list.add(recipe);
                AbstractFurnaceBlockEntity.dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe.value()).getExperience());
            });
        }
        return list;
    }

    private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float)multiplier * experience);
        float f = MathHelper.fractionalPart((float)multiplier * experience);
        if (f != 0.0f && world.random.nextFloat() < f) {
            ++i;
        }
        ExperienceOrbEntity.spawn(world, pos, i);
    }

    @Override
    public void provideRecipeInputs(RecipeFinder finder) {
        for (ItemStack itemStack : this.inventory) {
            finder.addInput(itemStack);
        }
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        World world = this.world;
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.getRecipesUsedAndDropExperience(serverWorld, Vec3d.ofCenter(pos));
        }
    }
}

