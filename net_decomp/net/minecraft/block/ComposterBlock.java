/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Object2FloatMap
 *  it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class ComposterBlock
extends Block
implements InventoryProvider {
    public static final MapCodec<ComposterBlock> CODEC = ComposterBlock.createCodec(ComposterBlock::new);
    public static final int NUM_LEVELS = 8;
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 7;
    public static final IntProperty LEVEL = Properties.LEVEL_8;
    public static final Object2FloatMap<ItemConvertible> ITEM_TO_LEVEL_INCREASE_CHANCE = new Object2FloatOpenHashMap();
    private static final int field_55750 = 12;
    private static final VoxelShape[] COLLISION_SHAPES_BY_LEVEL = Util.make(() -> {
        VoxelShape[] voxelShapes = Block.createShapeArray(8, level -> VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), Block.createColumnShape(12.0, Math.clamp((long)(1 + level * 2), 2, 16), 16.0), BooleanBiFunction.ONLY_FIRST));
        voxelShapes[8] = voxelShapes[7];
        return voxelShapes;
    });

    public MapCodec<ComposterBlock> getCodec() {
        return CODEC;
    }

    public static void registerDefaultCompostableItems() {
        ITEM_TO_LEVEL_INCREASE_CHANCE.defaultReturnValue(-1.0f);
        float f = 0.3f;
        float g = 0.5f;
        float h = 0.65f;
        float i = 0.85f;
        float j = 1.0f;
        ComposterBlock.registerCompostableItem(0.3f, Items.JUNGLE_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.OAK_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.SPRUCE_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.DARK_OAK_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.PALE_OAK_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.ACACIA_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.CHERRY_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.BIRCH_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.AZALEA_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.MANGROVE_LEAVES);
        ComposterBlock.registerCompostableItem(0.3f, Items.OAK_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.SPRUCE_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.BIRCH_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.JUNGLE_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.ACACIA_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.CHERRY_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.DARK_OAK_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.PALE_OAK_SAPLING);
        ComposterBlock.registerCompostableItem(0.3f, Items.MANGROVE_PROPAGULE);
        ComposterBlock.registerCompostableItem(0.3f, Items.BEETROOT_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.DRIED_KELP);
        ComposterBlock.registerCompostableItem(0.3f, Items.SHORT_GRASS);
        ComposterBlock.registerCompostableItem(0.3f, Items.KELP);
        ComposterBlock.registerCompostableItem(0.3f, Items.MELON_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.PUMPKIN_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.SEAGRASS);
        ComposterBlock.registerCompostableItem(0.3f, Items.SWEET_BERRIES);
        ComposterBlock.registerCompostableItem(0.3f, Items.GLOW_BERRIES);
        ComposterBlock.registerCompostableItem(0.3f, Items.WHEAT_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.MOSS_CARPET);
        ComposterBlock.registerCompostableItem(0.3f, Items.PALE_MOSS_CARPET);
        ComposterBlock.registerCompostableItem(0.3f, Items.PALE_HANGING_MOSS);
        ComposterBlock.registerCompostableItem(0.3f, Items.PINK_PETALS);
        ComposterBlock.registerCompostableItem(0.3f, Items.WILDFLOWERS);
        ComposterBlock.registerCompostableItem(0.3f, Items.LEAF_LITTER);
        ComposterBlock.registerCompostableItem(0.3f, Items.SMALL_DRIPLEAF);
        ComposterBlock.registerCompostableItem(0.3f, Items.HANGING_ROOTS);
        ComposterBlock.registerCompostableItem(0.3f, Items.MANGROVE_ROOTS);
        ComposterBlock.registerCompostableItem(0.3f, Items.TORCHFLOWER_SEEDS);
        ComposterBlock.registerCompostableItem(0.3f, Items.PITCHER_POD);
        ComposterBlock.registerCompostableItem(0.3f, Items.FIREFLY_BUSH);
        ComposterBlock.registerCompostableItem(0.3f, Items.BUSH);
        ComposterBlock.registerCompostableItem(0.3f, Items.CACTUS_FLOWER);
        ComposterBlock.registerCompostableItem(0.3f, Items.SHORT_DRY_GRASS);
        ComposterBlock.registerCompostableItem(0.3f, Items.TALL_DRY_GRASS);
        ComposterBlock.registerCompostableItem(0.5f, Items.DRIED_KELP_BLOCK);
        ComposterBlock.registerCompostableItem(0.5f, Items.TALL_GRASS);
        ComposterBlock.registerCompostableItem(0.5f, Items.FLOWERING_AZALEA_LEAVES);
        ComposterBlock.registerCompostableItem(0.5f, Items.CACTUS);
        ComposterBlock.registerCompostableItem(0.5f, Items.SUGAR_CANE);
        ComposterBlock.registerCompostableItem(0.5f, Items.VINE);
        ComposterBlock.registerCompostableItem(0.5f, Items.NETHER_SPROUTS);
        ComposterBlock.registerCompostableItem(0.5f, Items.WEEPING_VINES);
        ComposterBlock.registerCompostableItem(0.5f, Items.TWISTING_VINES);
        ComposterBlock.registerCompostableItem(0.5f, Items.MELON_SLICE);
        ComposterBlock.registerCompostableItem(0.5f, Items.GLOW_LICHEN);
        ComposterBlock.registerCompostableItem(0.65f, Items.SEA_PICKLE);
        ComposterBlock.registerCompostableItem(0.65f, Items.LILY_PAD);
        ComposterBlock.registerCompostableItem(0.65f, Items.PUMPKIN);
        ComposterBlock.registerCompostableItem(0.65f, Items.CARVED_PUMPKIN);
        ComposterBlock.registerCompostableItem(0.65f, Items.MELON);
        ComposterBlock.registerCompostableItem(0.65f, Items.APPLE);
        ComposterBlock.registerCompostableItem(0.65f, Items.BEETROOT);
        ComposterBlock.registerCompostableItem(0.65f, Items.CARROT);
        ComposterBlock.registerCompostableItem(0.65f, Items.COCOA_BEANS);
        ComposterBlock.registerCompostableItem(0.65f, Items.POTATO);
        ComposterBlock.registerCompostableItem(0.65f, Items.WHEAT);
        ComposterBlock.registerCompostableItem(0.65f, Items.BROWN_MUSHROOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.RED_MUSHROOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.MUSHROOM_STEM);
        ComposterBlock.registerCompostableItem(0.65f, Items.CRIMSON_FUNGUS);
        ComposterBlock.registerCompostableItem(0.65f, Items.WARPED_FUNGUS);
        ComposterBlock.registerCompostableItem(0.65f, Items.NETHER_WART);
        ComposterBlock.registerCompostableItem(0.65f, Items.CRIMSON_ROOTS);
        ComposterBlock.registerCompostableItem(0.65f, Items.WARPED_ROOTS);
        ComposterBlock.registerCompostableItem(0.65f, Items.SHROOMLIGHT);
        ComposterBlock.registerCompostableItem(0.65f, Items.DANDELION);
        ComposterBlock.registerCompostableItem(0.65f, Items.POPPY);
        ComposterBlock.registerCompostableItem(0.65f, Items.BLUE_ORCHID);
        ComposterBlock.registerCompostableItem(0.65f, Items.ALLIUM);
        ComposterBlock.registerCompostableItem(0.65f, Items.AZURE_BLUET);
        ComposterBlock.registerCompostableItem(0.65f, Items.RED_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.ORANGE_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.WHITE_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.PINK_TULIP);
        ComposterBlock.registerCompostableItem(0.65f, Items.OXEYE_DAISY);
        ComposterBlock.registerCompostableItem(0.65f, Items.CORNFLOWER);
        ComposterBlock.registerCompostableItem(0.65f, Items.LILY_OF_THE_VALLEY);
        ComposterBlock.registerCompostableItem(0.65f, Items.WITHER_ROSE);
        ComposterBlock.registerCompostableItem(0.65f, Items.OPEN_EYEBLOSSOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.CLOSED_EYEBLOSSOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.FERN);
        ComposterBlock.registerCompostableItem(0.65f, Items.SUNFLOWER);
        ComposterBlock.registerCompostableItem(0.65f, Items.LILAC);
        ComposterBlock.registerCompostableItem(0.65f, Items.ROSE_BUSH);
        ComposterBlock.registerCompostableItem(0.65f, Items.PEONY);
        ComposterBlock.registerCompostableItem(0.65f, Items.LARGE_FERN);
        ComposterBlock.registerCompostableItem(0.65f, Items.SPORE_BLOSSOM);
        ComposterBlock.registerCompostableItem(0.65f, Items.AZALEA);
        ComposterBlock.registerCompostableItem(0.65f, Items.MOSS_BLOCK);
        ComposterBlock.registerCompostableItem(0.65f, Items.PALE_MOSS_BLOCK);
        ComposterBlock.registerCompostableItem(0.65f, Items.BIG_DRIPLEAF);
        ComposterBlock.registerCompostableItem(0.85f, Items.HAY_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.BROWN_MUSHROOM_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.RED_MUSHROOM_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.NETHER_WART_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.WARPED_WART_BLOCK);
        ComposterBlock.registerCompostableItem(0.85f, Items.FLOWERING_AZALEA);
        ComposterBlock.registerCompostableItem(0.85f, Items.BREAD);
        ComposterBlock.registerCompostableItem(0.85f, Items.BAKED_POTATO);
        ComposterBlock.registerCompostableItem(0.85f, Items.COOKIE);
        ComposterBlock.registerCompostableItem(0.85f, Items.TORCHFLOWER);
        ComposterBlock.registerCompostableItem(0.85f, Items.PITCHER_PLANT);
        ComposterBlock.registerCompostableItem(1.0f, Items.CAKE);
        ComposterBlock.registerCompostableItem(1.0f, Items.PUMPKIN_PIE);
    }

    private static void registerCompostableItem(float levelIncreaseChance, ItemConvertible item) {
        ITEM_TO_LEVEL_INCREASE_CHANCE.put((Object)item.asItem(), levelIncreaseChance);
    }

    public ComposterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LEVEL, 0));
    }

    public static void playEffects(World world, BlockPos pos, boolean fill) {
        BlockState blockState = world.getBlockState(pos);
        world.playSoundAtBlockCenterClient(pos, fill ? SoundEvents.BLOCK_COMPOSTER_FILL_SUCCESS : SoundEvents.BLOCK_COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        double d = blockState.getOutlineShape(world, pos).getEndingCoord(Direction.Axis.Y, 0.5, 0.5) + 0.03125;
        double e = 2.0;
        double f = 0.1875;
        double g = 0.625;
        Random random = world.getRandom();
        for (int i = 0; i < 10; ++i) {
            double h = random.nextGaussian() * 0.02;
            double j = random.nextGaussian() * 0.02;
            double k = random.nextGaussian() * 0.02;
            world.addParticleClient(ParticleTypes.COMPOSTER, (double)pos.getX() + 0.1875 + 0.625 * (double)random.nextFloat(), (double)pos.getY() + d + (double)random.nextFloat() * (1.0 - d), (double)pos.getZ() + 0.1875 + 0.625 * (double)random.nextFloat(), h, j, k);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPES_BY_LEVEL[state.get(LEVEL)];
    }

    @Override
    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPES_BY_LEVEL[0];
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(LEVEL) == 7) {
            world.scheduleBlockTick(pos, state.getBlock(), 20);
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int i = state.get(LEVEL);
        if (i < 8 && ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey((Object)stack.getItem())) {
            if (i < 7 && !world.isClient()) {
                BlockState blockState = ComposterBlock.addToComposter(player, state, world, pos, stack);
                world.syncWorldEvent(1500, pos, state != blockState ? 1 : 0);
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                stack.decrementUnlessCreative(1, player);
            }
            return ActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        int i = state.get(LEVEL);
        if (i == 8) {
            ComposterBlock.emptyFullComposter(player, state, world, pos);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public static BlockState compost(Entity user, BlockState state, ServerWorld world, ItemStack stack, BlockPos pos) {
        int i = state.get(LEVEL);
        if (i < 7 && ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey((Object)stack.getItem())) {
            BlockState blockState = ComposterBlock.addToComposter(user, state, world, pos, stack);
            stack.decrement(1);
            return blockState;
        }
        return state;
    }

    public static BlockState emptyFullComposter(Entity user, BlockState state, World world, BlockPos pos) {
        if (!world.isClient()) {
            Vec3d vec3d = Vec3d.add(pos, 0.5, 1.01, 0.5).addHorizontalRandom(world.random, 0.7f);
            ItemEntity itemEntity = new ItemEntity(world, vec3d.getX(), vec3d.getY(), vec3d.getZ(), new ItemStack(Items.BONE_MEAL));
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
        }
        BlockState blockState = ComposterBlock.emptyComposter(user, state, world, pos);
        world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return blockState;
    }

    static BlockState emptyComposter(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos) {
        BlockState blockState = (BlockState)state.with(LEVEL, 0);
        world.setBlockState(pos, blockState, 3);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
        return blockState;
    }

    static BlockState addToComposter(@Nullable Entity user, BlockState state, WorldAccess world, BlockPos pos, ItemStack stack) {
        int i = state.get(LEVEL);
        float f = ITEM_TO_LEVEL_INCREASE_CHANCE.getFloat((Object)stack.getItem());
        if (i == 0 && f > 0.0f || world.getRandom().nextDouble() < (double)f) {
            int j = i + 1;
            BlockState blockState = (BlockState)state.with(LEVEL, j);
            world.setBlockState(pos, blockState, 3);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, blockState));
            if (j == 7) {
                world.scheduleBlockTick(pos, state.getBlock(), 20);
            }
            return blockState;
        }
        return state;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LEVEL) == 7) {
            world.setBlockState(pos, (BlockState)state.cycle(LEVEL), 3);
            world.playSound(null, pos, SoundEvents.BLOCK_COMPOSTER_READY, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return state.get(LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        int i = state.get(LEVEL);
        if (i == 8) {
            return new FullComposterInventory(state, world, pos, new ItemStack(Items.BONE_MEAL));
        }
        if (i < 7) {
            return new ComposterInventory(state, world, pos);
        }
        return new DummyInventory();
    }

    static class FullComposterInventory
    extends SimpleInventory
    implements SidedInventory {
        private final BlockState state;
        private final WorldAccess world;
        private final BlockPos pos;
        private boolean dirty;

        public FullComposterInventory(BlockState state, WorldAccess world, BlockPos pos, ItemStack outputItem) {
            super(outputItem);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            int[] nArray;
            if (side == Direction.DOWN) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return !this.dirty && dir == Direction.DOWN && stack.isOf(Items.BONE_MEAL);
        }

        @Override
        public void markDirty() {
            ComposterBlock.emptyComposter(null, this.state, this.world, this.pos);
            this.dirty = true;
        }
    }

    static class ComposterInventory
    extends SimpleInventory
    implements SidedInventory {
        private final BlockState state;
        private final WorldAccess world;
        private final BlockPos pos;
        private boolean dirty;

        public ComposterInventory(BlockState state, WorldAccess world, BlockPos pos) {
            super(1);
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            int[] nArray;
            if (side == Direction.UP) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return !this.dirty && dir == Direction.UP && ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey((Object)stack.getItem());
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public void markDirty() {
            ItemStack itemStack = this.getStack(0);
            if (!itemStack.isEmpty()) {
                this.dirty = true;
                BlockState blockState = ComposterBlock.addToComposter(null, this.state, this.world, this.pos, itemStack);
                this.world.syncWorldEvent(1500, this.pos, blockState != this.state ? 1 : 0);
                this.removeStack(0);
            }
        }
    }

    static class DummyInventory
    extends SimpleInventory
    implements SidedInventory {
        public DummyInventory() {
            super(0);
        }

        @Override
        public int[] getAvailableSlots(Direction side) {
            return new int[0];
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return false;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }
    }
}

