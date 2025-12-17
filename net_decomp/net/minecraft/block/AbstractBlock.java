/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeyedValue;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBlock
implements ToggleableFeature {
    protected static final Direction[] DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    protected final boolean collidable;
    protected final float resistance;
    protected final boolean randomTicks;
    protected final BlockSoundGroup soundGroup;
    protected final float slipperiness;
    protected final float velocityMultiplier;
    protected final float jumpVelocityMultiplier;
    protected final boolean dynamicBounds;
    protected final FeatureSet requiredFeatures;
    protected final Settings settings;
    protected final Optional<RegistryKey<LootTable>> lootTableKey;
    protected final String translationKey;

    public AbstractBlock(Settings settings) {
        this.collidable = settings.collidable;
        this.lootTableKey = settings.getLootTableKey();
        this.translationKey = settings.getTranslationKey();
        this.resistance = settings.resistance;
        this.randomTicks = settings.randomTicks;
        this.soundGroup = settings.soundGroup;
        this.slipperiness = settings.slipperiness;
        this.velocityMultiplier = settings.velocityMultiplier;
        this.jumpVelocityMultiplier = settings.jumpVelocityMultiplier;
        this.dynamicBounds = settings.dynamicBounds;
        this.requiredFeatures = settings.requiredFeatures;
        this.settings = settings;
    }

    public Settings getSettings() {
        return this.settings;
    }

    protected abstract MapCodec<? extends Block> getCodec();

    protected static <B extends Block> RecordCodecBuilder<B, Settings> createSettingsCodec() {
        return Settings.CODEC.fieldOf("properties").forGetter(AbstractBlock::getSettings);
    }

    public static <B extends Block> MapCodec<B> createCodec(Function<Settings, B> blockFromSettings) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(AbstractBlock.createSettingsCodec()).apply((Applicative)instance, blockFromSettings));
    }

    protected void prepare(BlockState state, WorldAccess world, BlockPos pos, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
    }

    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        switch (type) {
            case LAND: {
                return !state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            }
            case WATER: {
                return state.getFluidState().isIn(FluidTags.WATER);
            }
            case AIR: {
                return !state.isFullCube(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            }
        }
        return false;
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return state;
    }

    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return false;
    }

    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
    }

    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
    }

    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
    }

    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (state.isAir() || explosion.getDestructionType() == Explosion.DestructionType.TRIGGER_BLOCK) {
            return;
        }
        Block block = state.getBlock();
        boolean bl = explosion.getCausingEntity() instanceof PlayerEntity;
        if (block.shouldDropItemsOnExplosion(explosion)) {
            BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            LootWorldContext.Builder builder = new LootWorldContext.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).add(LootContextParameters.TOOL, ItemStack.EMPTY).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity).addOptional(LootContextParameters.THIS_ENTITY, explosion.getEntity());
            if (explosion.getDestructionType() == Explosion.DestructionType.DESTROY_WITH_DECAY) {
                builder.add(LootContextParameters.EXPLOSION_RADIUS, Float.valueOf(explosion.getPower()));
            }
            state.onStacksDropped(world, pos, ItemStack.EMPTY, bl);
            state.getDroppedStacks(builder).forEach(stack -> stackMerger.accept((ItemStack)stack, pos));
        }
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        block.onDestroyedByExplosion(world, pos, explosion);
    }

    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        return false;
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected boolean hasSidedTransparency(BlockState state) {
        return false;
    }

    protected boolean emitsRedstonePower(BlockState state) {
        return false;
    }

    protected FluidState getFluidState(BlockState state) {
        return Fluids.EMPTY.getDefaultState();
    }

    protected boolean hasComparatorOutput(BlockState state) {
        return false;
    }

    protected float getMaxHorizontalModelOffset() {
        return 0.25f;
    }

    protected float getVerticalModelOffsetMultiplier() {
        return 0.2f;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.requiredFeatures;
    }

    protected boolean keepBlockEntityWhenReplacedWith(BlockState state) {
        return false;
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state;
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state;
    }

    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return state.isReplaceable() && (context.getStack().isEmpty() || !context.getStack().isOf(this.asItem()));
    }

    protected boolean canBucketPlace(BlockState state, Fluid fluid) {
        return state.isReplaceable() || !state.isSolid();
    }

    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        if (this.lootTableKey.isEmpty()) {
            return Collections.emptyList();
        }
        LootWorldContext lootWorldContext = builder.add(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
        ServerWorld serverWorld = lootWorldContext.getWorld();
        LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(this.lootTableKey.get());
        return lootTable.generateLoot(lootWorldContext);
    }

    protected long getRenderingSeed(BlockState state, BlockPos pos) {
        return MathHelper.hashCode(pos);
    }

    protected VoxelShape getCullingShape(BlockState state) {
        return state.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
    }

    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return this.getCollisionShape(state, world, pos, ShapeContext.absent());
    }

    protected VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    protected int getOpacity(BlockState state) {
        if (state.isOpaqueFullCube()) {
            return 15;
        }
        return state.isTransparent() ? 0 : 1;
    }

    protected @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return null;
    }

    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return state.isFullCube(world, pos) ? 0.2f : 1.0f;
    }

    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return 0;
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collidable ? state.getOutlineShape(world, pos) : VoxelShapes.empty();
    }

    protected VoxelShape getInsideCollisionShape(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return VoxelShapes.fullCube();
    }

    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return Block.isShapeFullCube(state.getCollisionShape(world, pos));
    }

    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getCollisionShape(state, world, pos, context);
    }

    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

    protected float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float f = state.getHardness(world, pos);
        if (f == -1.0f) {
            return 0.0f;
        }
        int i = player.canHarvest(state) ? 30 : 100;
        return player.getBlockBreakingSpeed(state) / f / (float)i;
    }

    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
    }

    protected void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
    }

    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
    }

    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public final Optional<RegistryKey<LootTable>> getLootTableKey() {
        return this.lootTableKey;
    }

    public final String getTranslationKey() {
        return this.translationKey;
    }

    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
    }

    protected boolean isTransparent(BlockState state) {
        return !Block.isShapeFullCube(state.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)) && state.getFluidState().isEmpty();
    }

    protected boolean hasRandomTicks(BlockState state) {
        return this.randomTicks;
    }

    protected BlockSoundGroup getSoundGroup(BlockState state) {
        return this.soundGroup;
    }

    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.asItem());
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MapColor getDefaultMapColor() {
        return this.settings.mapColorProvider.apply(this.asBlock().getDefaultState());
    }

    public float getHardness() {
        return this.settings.hardness;
    }

    public static class Settings {
        public static final Codec<Settings> CODEC = MapCodec.unitCodec(() -> Settings.create());
        Function<BlockState, MapColor> mapColorProvider = state -> MapColor.CLEAR;
        boolean collidable = true;
        BlockSoundGroup soundGroup = BlockSoundGroup.STONE;
        ToIntFunction<BlockState> luminance = state -> 0;
        float resistance;
        float hardness;
        boolean toolRequired;
        boolean randomTicks;
        float slipperiness = 0.6f;
        float velocityMultiplier = 1.0f;
        float jumpVelocityMultiplier = 1.0f;
        private @Nullable RegistryKey<Block> registryKey;
        private RegistryKeyedValue<Block, Optional<RegistryKey<LootTable>>> lootTable = registryKey -> Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE, registryKey.getValue().withPrefixedPath("blocks/")));
        private RegistryKeyedValue<Block, String> translationKey = registryKey -> Util.createTranslationKey("block", registryKey.getValue());
        boolean opaque = true;
        boolean isAir;
        boolean burnable;
        @Deprecated
        boolean liquid;
        @Deprecated
        boolean forceNotSolid;
        boolean forceSolid;
        PistonBehavior pistonBehavior = PistonBehavior.NORMAL;
        boolean blockBreakParticles = true;
        NoteBlockInstrument instrument = NoteBlockInstrument.HARP;
        boolean replaceable;
        TypedContextPredicate<EntityType<?>> allowsSpawningPredicate = (state, world, pos, type) -> state.isSideSolidFullSquare(world, pos, Direction.UP) && state.getLuminance() < 14;
        ContextPredicate solidBlockPredicate = (state, world, pos) -> state.isFullCube(world, pos);
        ContextPredicate suffocationPredicate;
        ContextPredicate blockVisionPredicate = this.suffocationPredicate = (state, world, pos) -> state.blocksMovement() && state.isFullCube(world, pos);
        ContextPredicate postProcessPredicate = (state, world, pos) -> false;
        ContextPredicate emissiveLightingPredicate = (state, world, pos) -> false;
        boolean dynamicBounds;
        FeatureSet requiredFeatures = FeatureFlags.VANILLA_FEATURES;
        @Nullable Offsetter offsetter;

        private Settings() {
        }

        public static Settings create() {
            return new Settings();
        }

        public static Settings copy(AbstractBlock block) {
            Settings settings = Settings.copyShallow(block);
            Settings settings2 = block.settings;
            settings.jumpVelocityMultiplier = settings2.jumpVelocityMultiplier;
            settings.solidBlockPredicate = settings2.solidBlockPredicate;
            settings.allowsSpawningPredicate = settings2.allowsSpawningPredicate;
            settings.postProcessPredicate = settings2.postProcessPredicate;
            settings.suffocationPredicate = settings2.suffocationPredicate;
            settings.blockVisionPredicate = settings2.blockVisionPredicate;
            settings.lootTable = settings2.lootTable;
            settings.translationKey = settings2.translationKey;
            return settings;
        }

        @Deprecated
        public static Settings copyShallow(AbstractBlock block) {
            Settings settings = new Settings();
            Settings settings2 = block.settings;
            settings.hardness = settings2.hardness;
            settings.resistance = settings2.resistance;
            settings.collidable = settings2.collidable;
            settings.randomTicks = settings2.randomTicks;
            settings.luminance = settings2.luminance;
            settings.mapColorProvider = settings2.mapColorProvider;
            settings.soundGroup = settings2.soundGroup;
            settings.slipperiness = settings2.slipperiness;
            settings.velocityMultiplier = settings2.velocityMultiplier;
            settings.dynamicBounds = settings2.dynamicBounds;
            settings.opaque = settings2.opaque;
            settings.isAir = settings2.isAir;
            settings.burnable = settings2.burnable;
            settings.liquid = settings2.liquid;
            settings.forceNotSolid = settings2.forceNotSolid;
            settings.forceSolid = settings2.forceSolid;
            settings.pistonBehavior = settings2.pistonBehavior;
            settings.toolRequired = settings2.toolRequired;
            settings.offsetter = settings2.offsetter;
            settings.blockBreakParticles = settings2.blockBreakParticles;
            settings.requiredFeatures = settings2.requiredFeatures;
            settings.emissiveLightingPredicate = settings2.emissiveLightingPredicate;
            settings.instrument = settings2.instrument;
            settings.replaceable = settings2.replaceable;
            return settings;
        }

        public Settings mapColor(DyeColor color) {
            this.mapColorProvider = state -> color.getMapColor();
            return this;
        }

        public Settings mapColor(MapColor color) {
            this.mapColorProvider = state -> color;
            return this;
        }

        public Settings mapColor(Function<BlockState, MapColor> mapColorProvider) {
            this.mapColorProvider = mapColorProvider;
            return this;
        }

        public Settings noCollision() {
            this.collidable = false;
            this.opaque = false;
            return this;
        }

        public Settings nonOpaque() {
            this.opaque = false;
            return this;
        }

        public Settings slipperiness(float slipperiness) {
            this.slipperiness = slipperiness;
            return this;
        }

        public Settings velocityMultiplier(float velocityMultiplier) {
            this.velocityMultiplier = velocityMultiplier;
            return this;
        }

        public Settings jumpVelocityMultiplier(float jumpVelocityMultiplier) {
            this.jumpVelocityMultiplier = jumpVelocityMultiplier;
            return this;
        }

        public Settings sounds(BlockSoundGroup soundGroup) {
            this.soundGroup = soundGroup;
            return this;
        }

        public Settings luminance(ToIntFunction<BlockState> luminance) {
            this.luminance = luminance;
            return this;
        }

        public Settings strength(float hardness, float resistance) {
            return this.hardness(hardness).resistance(resistance);
        }

        public Settings breakInstantly() {
            return this.strength(0.0f);
        }

        public Settings strength(float strength) {
            this.strength(strength, strength);
            return this;
        }

        public Settings ticksRandomly() {
            this.randomTicks = true;
            return this;
        }

        public Settings dynamicBounds() {
            this.dynamicBounds = true;
            return this;
        }

        public Settings dropsNothing() {
            this.lootTable = RegistryKeyedValue.fixed(Optional.empty());
            return this;
        }

        public Settings lootTable(Optional<RegistryKey<LootTable>> lootTableKey) {
            this.lootTable = RegistryKeyedValue.fixed(lootTableKey);
            return this;
        }

        protected Optional<RegistryKey<LootTable>> getLootTableKey() {
            return this.lootTable.get(Objects.requireNonNull(this.registryKey, "Block id not set"));
        }

        public Settings burnable() {
            this.burnable = true;
            return this;
        }

        public Settings liquid() {
            this.liquid = true;
            return this;
        }

        public Settings solid() {
            this.forceSolid = true;
            return this;
        }

        @Deprecated
        public Settings notSolid() {
            this.forceNotSolid = true;
            return this;
        }

        public Settings pistonBehavior(PistonBehavior pistonBehavior) {
            this.pistonBehavior = pistonBehavior;
            return this;
        }

        public Settings air() {
            this.isAir = true;
            return this;
        }

        public Settings allowsSpawning(TypedContextPredicate<EntityType<?>> predicate) {
            this.allowsSpawningPredicate = predicate;
            return this;
        }

        public Settings solidBlock(ContextPredicate predicate) {
            this.solidBlockPredicate = predicate;
            return this;
        }

        public Settings suffocates(ContextPredicate predicate) {
            this.suffocationPredicate = predicate;
            return this;
        }

        public Settings blockVision(ContextPredicate predicate) {
            this.blockVisionPredicate = predicate;
            return this;
        }

        public Settings postProcess(ContextPredicate predicate) {
            this.postProcessPredicate = predicate;
            return this;
        }

        public Settings emissiveLighting(ContextPredicate predicate) {
            this.emissiveLightingPredicate = predicate;
            return this;
        }

        public Settings requiresTool() {
            this.toolRequired = true;
            return this;
        }

        public Settings hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Settings resistance(float resistance) {
            this.resistance = Math.max(0.0f, resistance);
            return this;
        }

        public Settings offset(OffsetType offsetType) {
            this.offsetter = switch (offsetType.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> null;
                case 2 -> (state, pos) -> {
                    Block block = state.getBlock();
                    long l = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
                    double d = ((double)((float)(l >> 4 & 0xFL) / 15.0f) - 1.0) * (double)block.getVerticalModelOffsetMultiplier();
                    float f = block.getMaxHorizontalModelOffset();
                    double e = MathHelper.clamp(((double)((float)(l & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    double g = MathHelper.clamp(((double)((float)(l >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    return new Vec3d(e, d, g);
                };
                case 1 -> (state, pos) -> {
                    Block block = state.getBlock();
                    long l = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
                    float f = block.getMaxHorizontalModelOffset();
                    double d = MathHelper.clamp(((double)((float)(l & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    double e = MathHelper.clamp(((double)((float)(l >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, (double)(-f), (double)f);
                    return new Vec3d(d, 0.0, e);
                };
            };
            return this;
        }

        public Settings noBlockBreakParticles() {
            this.blockBreakParticles = false;
            return this;
        }

        public Settings requires(FeatureFlag ... features) {
            this.requiredFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(features);
            return this;
        }

        public Settings instrument(NoteBlockInstrument instrument) {
            this.instrument = instrument;
            return this;
        }

        public Settings replaceable() {
            this.replaceable = true;
            return this;
        }

        public Settings registryKey(RegistryKey<Block> registryKey) {
            this.registryKey = registryKey;
            return this;
        }

        public Settings overrideTranslationKey(String translationKey) {
            this.translationKey = RegistryKeyedValue.fixed(translationKey);
            return this;
        }

        protected String getTranslationKey() {
            return this.translationKey.get(Objects.requireNonNull(this.registryKey, "Block id not set"));
        }
    }

    @FunctionalInterface
    public static interface TypedContextPredicate<A> {
        public boolean test(BlockState var1, BlockView var2, BlockPos var3, A var4);
    }

    @FunctionalInterface
    public static interface Offsetter {
        public Vec3d evaluate(BlockState var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface ContextPredicate {
        public boolean test(BlockState var1, BlockView var2, BlockPos var3);
    }

    public static abstract class AbstractBlockState
    extends State<Block, BlockState> {
        private static final Direction[] DIRECTIONS = Direction.values();
        private static final VoxelShape[] EMPTY_CULLING_FACES = Util.make(new VoxelShape[DIRECTIONS.length], direction -> Arrays.fill(direction, VoxelShapes.empty()));
        private static final VoxelShape[] FULL_CULLING_FACES = Util.make(new VoxelShape[DIRECTIONS.length], direction -> Arrays.fill(direction, VoxelShapes.fullCube()));
        private final int luminance;
        private final boolean hasSidedTransparency;
        private final boolean isAir;
        private final boolean burnable;
        @Deprecated
        private final boolean liquid;
        @Deprecated
        private boolean solid;
        private final PistonBehavior pistonBehavior;
        private final MapColor mapColor;
        private final float hardness;
        private final boolean toolRequired;
        private final boolean opaque;
        private final ContextPredicate solidBlockPredicate;
        private final ContextPredicate suffocationPredicate;
        private final ContextPredicate blockVisionPredicate;
        private final ContextPredicate postProcessPredicate;
        private final ContextPredicate emissiveLightingPredicate;
        private final @Nullable Offsetter offsetter;
        private final boolean blockBreakParticles;
        private final NoteBlockInstrument instrument;
        private final boolean replaceable;
        private @Nullable ShapeCache shapeCache;
        private FluidState fluidState = Fluids.EMPTY.getDefaultState();
        private boolean ticksRandomly;
        private boolean opaqueFullCube;
        private VoxelShape cullingShape;
        private VoxelShape[] cullingFaces;
        private boolean transparent;
        private int opacity;

        protected AbstractBlockState(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
            super(block, propertyMap, codec);
            Settings settings = block.settings;
            this.luminance = settings.luminance.applyAsInt(this.asBlockState());
            this.hasSidedTransparency = block.hasSidedTransparency(this.asBlockState());
            this.isAir = settings.isAir;
            this.burnable = settings.burnable;
            this.liquid = settings.liquid;
            this.pistonBehavior = settings.pistonBehavior;
            this.mapColor = settings.mapColorProvider.apply(this.asBlockState());
            this.hardness = settings.hardness;
            this.toolRequired = settings.toolRequired;
            this.opaque = settings.opaque;
            this.solidBlockPredicate = settings.solidBlockPredicate;
            this.suffocationPredicate = settings.suffocationPredicate;
            this.blockVisionPredicate = settings.blockVisionPredicate;
            this.postProcessPredicate = settings.postProcessPredicate;
            this.emissiveLightingPredicate = settings.emissiveLightingPredicate;
            this.offsetter = settings.offsetter;
            this.blockBreakParticles = settings.blockBreakParticles;
            this.instrument = settings.instrument;
            this.replaceable = settings.replaceable;
        }

        private boolean shouldBeSolid() {
            if (((Block)this.owner).settings.forceSolid) {
                return true;
            }
            if (((Block)this.owner).settings.forceNotSolid) {
                return false;
            }
            if (this.shapeCache == null) {
                return false;
            }
            VoxelShape voxelShape = this.shapeCache.collisionShape;
            if (voxelShape.isEmpty()) {
                return false;
            }
            Box box = voxelShape.getBoundingBox();
            if (box.getAverageSideLength() >= 0.7291666666666666) {
                return true;
            }
            return box.getLengthY() >= 1.0;
        }

        public void initShapeCache() {
            this.fluidState = ((Block)this.owner).getFluidState(this.asBlockState());
            this.ticksRandomly = ((Block)this.owner).hasRandomTicks(this.asBlockState());
            if (!this.getBlock().hasDynamicBounds()) {
                this.shapeCache = new ShapeCache(this.asBlockState());
            }
            this.solid = this.shouldBeSolid();
            this.cullingShape = this.opaque ? ((Block)this.owner).getCullingShape(this.asBlockState()) : VoxelShapes.empty();
            this.opaqueFullCube = Block.isShapeFullCube(this.cullingShape);
            if (this.cullingShape.isEmpty()) {
                this.cullingFaces = EMPTY_CULLING_FACES;
            } else if (this.opaqueFullCube) {
                this.cullingFaces = FULL_CULLING_FACES;
            } else {
                this.cullingFaces = new VoxelShape[DIRECTIONS.length];
                for (Direction direction : DIRECTIONS) {
                    this.cullingFaces[direction.ordinal()] = this.cullingShape.getFace(direction);
                }
            }
            this.transparent = ((Block)this.owner).isTransparent(this.asBlockState());
            this.opacity = ((Block)this.owner).getOpacity(this.asBlockState());
        }

        public Block getBlock() {
            return (Block)this.owner;
        }

        public RegistryEntry<Block> getRegistryEntry() {
            return ((Block)this.owner).getRegistryEntry();
        }

        @Deprecated
        public boolean blocksMovement() {
            Block block = this.getBlock();
            return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && this.isSolid();
        }

        @Deprecated
        public boolean isSolid() {
            return this.solid;
        }

        public boolean allowsSpawning(BlockView world, BlockPos pos, EntityType<?> type) {
            return this.getBlock().settings.allowsSpawningPredicate.test(this.asBlockState(), world, pos, type);
        }

        public boolean isTransparent() {
            return this.transparent;
        }

        public int getOpacity() {
            return this.opacity;
        }

        public VoxelShape getCullingFace(Direction direction) {
            return this.cullingFaces[direction.ordinal()];
        }

        public VoxelShape getCullingShape() {
            return this.cullingShape;
        }

        public boolean exceedsCube() {
            return this.shapeCache == null || this.shapeCache.exceedsCube;
        }

        public boolean hasSidedTransparency() {
            return this.hasSidedTransparency;
        }

        public int getLuminance() {
            return this.luminance;
        }

        public boolean isAir() {
            return this.isAir;
        }

        public boolean isBurnable() {
            return this.burnable;
        }

        @Deprecated
        public boolean isLiquid() {
            return this.liquid;
        }

        public MapColor getMapColor(BlockView world, BlockPos pos) {
            return this.mapColor;
        }

        public BlockState rotate(BlockRotation rotation) {
            return this.getBlock().rotate(this.asBlockState(), rotation);
        }

        public BlockState mirror(BlockMirror mirror) {
            return this.getBlock().mirror(this.asBlockState(), mirror);
        }

        public BlockRenderType getRenderType() {
            return this.getBlock().getRenderType(this.asBlockState());
        }

        public boolean hasEmissiveLighting(BlockView world, BlockPos pos) {
            return this.emissiveLightingPredicate.test(this.asBlockState(), world, pos);
        }

        public float getAmbientOcclusionLightLevel(BlockView world, BlockPos pos) {
            return this.getBlock().getAmbientOcclusionLightLevel(this.asBlockState(), world, pos);
        }

        public boolean isSolidBlock(BlockView world, BlockPos pos) {
            return this.solidBlockPredicate.test(this.asBlockState(), world, pos);
        }

        public boolean emitsRedstonePower() {
            return this.getBlock().emitsRedstonePower(this.asBlockState());
        }

        public int getWeakRedstonePower(BlockView world, BlockPos pos, Direction direction) {
            return this.getBlock().getWeakRedstonePower(this.asBlockState(), world, pos, direction);
        }

        public boolean hasComparatorOutput() {
            return this.getBlock().hasComparatorOutput(this.asBlockState());
        }

        public int getComparatorOutput(World world, BlockPos pos, Direction direction) {
            return this.getBlock().getComparatorOutput(this.asBlockState(), world, pos, direction);
        }

        public float getHardness(BlockView world, BlockPos pos) {
            return this.hardness;
        }

        public float calcBlockBreakingDelta(PlayerEntity player, BlockView world, BlockPos pos) {
            return this.getBlock().calcBlockBreakingDelta(this.asBlockState(), player, world, pos);
        }

        public int getStrongRedstonePower(BlockView world, BlockPos pos, Direction direction) {
            return this.getBlock().getStrongRedstonePower(this.asBlockState(), world, pos, direction);
        }

        public PistonBehavior getPistonBehavior() {
            return this.pistonBehavior;
        }

        public boolean isOpaqueFullCube() {
            return this.opaqueFullCube;
        }

        public boolean isOpaque() {
            return this.opaque;
        }

        public boolean isSideInvisible(BlockState state, Direction direction) {
            return this.getBlock().isSideInvisible(this.asBlockState(), state, direction);
        }

        public VoxelShape getOutlineShape(BlockView world, BlockPos pos) {
            return this.getOutlineShape(world, pos, ShapeContext.absent());
        }

        public VoxelShape getOutlineShape(BlockView world, BlockPos pos, ShapeContext context) {
            return this.getBlock().getOutlineShape(this.asBlockState(), world, pos, context);
        }

        public VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.collisionShape;
            }
            return this.getCollisionShape(world, pos, ShapeContext.absent());
        }

        public VoxelShape getCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
            return this.getBlock().getCollisionShape(this.asBlockState(), world, pos, context);
        }

        public VoxelShape getInsideCollisionShape(BlockView blockView, BlockPos pos, Entity entity) {
            return this.getBlock().getInsideCollisionShape(this.asBlockState(), blockView, pos, entity);
        }

        public VoxelShape getSidesShape(BlockView world, BlockPos pos) {
            return this.getBlock().getSidesShape(this.asBlockState(), world, pos);
        }

        public VoxelShape getCameraCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
            return this.getBlock().getCameraCollisionShape(this.asBlockState(), world, pos, context);
        }

        public VoxelShape getRaycastShape(BlockView world, BlockPos pos) {
            return this.getBlock().getRaycastShape(this.asBlockState(), world, pos);
        }

        public final boolean hasSolidTopSurface(BlockView world, BlockPos pos, Entity entity) {
            return this.isSolidSurface(world, pos, entity, Direction.UP);
        }

        public final boolean isSolidSurface(BlockView world, BlockPos pos, Entity entity, Direction direction) {
            return Block.isFaceFullSquare(this.getCollisionShape(world, pos, ShapeContext.of(entity)), direction);
        }

        public Vec3d getModelOffset(BlockPos pos) {
            Offsetter offsetter = this.offsetter;
            if (offsetter != null) {
                return offsetter.evaluate(this.asBlockState(), pos);
            }
            return Vec3d.ZERO;
        }

        public boolean hasModelOffset() {
            return this.offsetter != null;
        }

        public boolean onSyncedBlockEvent(World world, BlockPos pos, int type, int data) {
            return this.getBlock().onSyncedBlockEvent(this.asBlockState(), world, pos, type, data);
        }

        public void neighborUpdate(World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
            this.getBlock().neighborUpdate(this.asBlockState(), world, pos, sourceBlock, wireOrientation, notify);
        }

        public final void updateNeighbors(WorldAccess world, BlockPos pos, @Block.SetBlockStateFlag int flags) {
            this.updateNeighbors(world, pos, flags, 512);
        }

        public final void updateNeighbors(WorldAccess world, BlockPos pos, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (Direction direction : DIRECTIONS) {
                mutable.set((Vec3i)pos, direction);
                world.replaceWithStateForNeighborUpdate(direction.getOpposite(), mutable, pos, this.asBlockState(), flags, maxUpdateDepth);
            }
        }

        public final void prepare(WorldAccess world, BlockPos pos, @Block.SetBlockStateFlag int flags) {
            this.prepare(world, pos, flags, 512);
        }

        public void prepare(WorldAccess world, BlockPos pos, @Block.SetBlockStateFlag int flags, int maxUpdateDepth) {
            this.getBlock().prepare(this.asBlockState(), world, pos, flags, maxUpdateDepth);
        }

        public void onBlockAdded(World world, BlockPos pos, BlockState state, boolean notify) {
            this.getBlock().onBlockAdded(this.asBlockState(), world, pos, state, notify);
        }

        public void onStateReplaced(ServerWorld world, BlockPos pos, boolean moved) {
            this.getBlock().onStateReplaced(this.asBlockState(), world, pos, moved);
        }

        public void onExploded(ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
            this.getBlock().onExploded(this.asBlockState(), world, pos, explosion, stackMerger);
        }

        public void scheduledTick(ServerWorld world, BlockPos pos, Random random) {
            this.getBlock().scheduledTick(this.asBlockState(), world, pos, random);
        }

        public void randomTick(ServerWorld world, BlockPos pos, Random random) {
            this.getBlock().randomTick(this.asBlockState(), world, pos, random);
        }

        public void onEntityCollision(World world, BlockPos pos, Entity entity, EntityCollisionHandler entityCollisionHandler, boolean bl) {
            this.getBlock().onEntityCollision(this.asBlockState(), world, pos, entity, entityCollisionHandler, bl);
        }

        public void onStacksDropped(ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
            this.getBlock().onStacksDropped(this.asBlockState(), world, pos, tool, dropExperience);
        }

        public List<ItemStack> getDroppedStacks(LootWorldContext.Builder builder) {
            return this.getBlock().getDroppedStacks(this.asBlockState(), builder);
        }

        public ActionResult onUseWithItem(ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
            return this.getBlock().onUseWithItem(stack, this.asBlockState(), world, hit.getBlockPos(), player, hand, hit);
        }

        public ActionResult onUse(World world, PlayerEntity player, BlockHitResult hit) {
            return this.getBlock().onUse(this.asBlockState(), world, hit.getBlockPos(), player, hit);
        }

        public void onBlockBreakStart(World world, BlockPos pos, PlayerEntity player) {
            this.getBlock().onBlockBreakStart(this.asBlockState(), world, pos, player);
        }

        public boolean shouldSuffocate(BlockView world, BlockPos pos) {
            return this.suffocationPredicate.test(this.asBlockState(), world, pos);
        }

        public boolean shouldBlockVision(BlockView world, BlockPos pos) {
            return this.blockVisionPredicate.test(this.asBlockState(), world, pos);
        }

        public BlockState getStateForNeighborUpdate(WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
            return this.getBlock().getStateForNeighborUpdate(this.asBlockState(), world, tickView, pos, direction, neighborPos, neighborState, random);
        }

        public boolean canPathfindThrough(NavigationType type) {
            return this.getBlock().canPathfindThrough(this.asBlockState(), type);
        }

        public boolean canReplace(ItemPlacementContext context) {
            return this.getBlock().canReplace(this.asBlockState(), context);
        }

        public boolean canBucketPlace(Fluid fluid) {
            return this.getBlock().canBucketPlace(this.asBlockState(), fluid);
        }

        public boolean isReplaceable() {
            return this.replaceable;
        }

        public boolean canPlaceAt(WorldView world, BlockPos pos) {
            return this.getBlock().canPlaceAt(this.asBlockState(), world, pos);
        }

        public boolean shouldPostProcess(BlockView world, BlockPos pos) {
            return this.postProcessPredicate.test(this.asBlockState(), world, pos);
        }

        public @Nullable NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
            return this.getBlock().createScreenHandlerFactory(this.asBlockState(), world, pos);
        }

        public boolean isIn(TagKey<Block> tag) {
            return this.getBlock().getRegistryEntry().isIn(tag);
        }

        public boolean isIn(TagKey<Block> tag, Predicate<AbstractBlockState> predicate) {
            return this.isIn(tag) && predicate.test(this);
        }

        public boolean isIn(RegistryEntryList<Block> blocks) {
            return blocks.contains(this.getBlock().getRegistryEntry());
        }

        public boolean isOf(RegistryEntry<Block> blockEntry) {
            return this.isOf(blockEntry.value());
        }

        public Stream<TagKey<Block>> streamTags() {
            return this.getBlock().getRegistryEntry().streamTags();
        }

        public boolean hasBlockEntity() {
            return this.getBlock() instanceof BlockEntityProvider;
        }

        public boolean keepBlockEntityWhenReplacedWith(BlockState state) {
            return this.getBlock().keepBlockEntityWhenReplacedWith(state);
        }

        public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getBlockEntityTicker(World world, BlockEntityType<T> blockEntityType) {
            if (this.getBlock() instanceof BlockEntityProvider) {
                return ((BlockEntityProvider)((Object)this.getBlock())).getTicker(world, this.asBlockState(), blockEntityType);
            }
            return null;
        }

        public boolean isOf(Block block) {
            return this.getBlock() == block;
        }

        public boolean matchesKey(RegistryKey<Block> key) {
            return this.getBlock().getRegistryEntry().matchesKey(key);
        }

        public FluidState getFluidState() {
            return this.fluidState;
        }

        public boolean hasRandomTicks() {
            return this.ticksRandomly;
        }

        public long getRenderingSeed(BlockPos pos) {
            return this.getBlock().getRenderingSeed(this.asBlockState(), pos);
        }

        public BlockSoundGroup getSoundGroup() {
            return this.getBlock().getSoundGroup(this.asBlockState());
        }

        public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
            this.getBlock().onProjectileHit(world, state, hit, projectile);
        }

        public boolean isSideSolidFullSquare(BlockView world, BlockPos pos, Direction direction) {
            return this.isSideSolid(world, pos, direction, SideShapeType.FULL);
        }

        public boolean isSideSolid(BlockView world, BlockPos pos, Direction direction, SideShapeType shapeType) {
            if (this.shapeCache != null) {
                return this.shapeCache.isSideSolid(direction, shapeType);
            }
            return shapeType.matches(this.asBlockState(), world, pos, direction);
        }

        public boolean isFullCube(BlockView world, BlockPos pos) {
            if (this.shapeCache != null) {
                return this.shapeCache.isFullCube;
            }
            return this.getBlock().isShapeFullCube(this.asBlockState(), world, pos);
        }

        public ItemStack getPickStack(WorldView world, BlockPos pos, boolean includeData) {
            return this.getBlock().getPickStack(world, pos, this.asBlockState(), includeData);
        }

        protected abstract BlockState asBlockState();

        public boolean isToolRequired() {
            return this.toolRequired;
        }

        public boolean hasBlockBreakParticles() {
            return this.blockBreakParticles;
        }

        public NoteBlockInstrument getInstrument() {
            return this.instrument;
        }

        static final class ShapeCache {
            private static final Direction[] DIRECTIONS = Direction.values();
            private static final int SHAPE_TYPE_LENGTH = SideShapeType.values().length;
            protected final VoxelShape collisionShape;
            protected final boolean exceedsCube;
            private final boolean[] solidSides;
            protected final boolean isFullCube;

            ShapeCache(BlockState state) {
                Block block = state.getBlock();
                this.collisionShape = block.getCollisionShape(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, ShapeContext.absent());
                if (!this.collisionShape.isEmpty() && state.hasModelOffset()) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", Registries.BLOCK.getId(block)));
                }
                this.exceedsCube = Arrays.stream(Direction.Axis.values()).anyMatch(axis -> this.collisionShape.getMin((Direction.Axis)axis) < 0.0 || this.collisionShape.getMax((Direction.Axis)axis) > 1.0);
                this.solidSides = new boolean[DIRECTIONS.length * SHAPE_TYPE_LENGTH];
                for (Direction direction : DIRECTIONS) {
                    for (SideShapeType sideShapeType : SideShapeType.values()) {
                        this.solidSides[ShapeCache.indexSolidSide((Direction)direction, (SideShapeType)sideShapeType)] = sideShapeType.matches(state, EmptyBlockView.INSTANCE, BlockPos.ORIGIN, direction);
                    }
                }
                this.isFullCube = Block.isShapeFullCube(state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN));
            }

            public boolean isSideSolid(Direction direction, SideShapeType shapeType) {
                return this.solidSides[ShapeCache.indexSolidSide(direction, shapeType)];
            }

            private static int indexSolidSide(Direction direction, SideShapeType shapeType) {
                return direction.ordinal() * SHAPE_TYPE_LENGTH + shapeType.ordinal();
            }
        }
    }

    public static final class OffsetType
    extends Enum<OffsetType> {
        public static final /* enum */ OffsetType NONE = new OffsetType();
        public static final /* enum */ OffsetType XZ = new OffsetType();
        public static final /* enum */ OffsetType XYZ = new OffsetType();
        private static final /* synthetic */ OffsetType[] field_10658;

        public static OffsetType[] values() {
            return (OffsetType[])field_10658.clone();
        }

        public static OffsetType valueOf(String string) {
            return Enum.valueOf(OffsetType.class, string);
        }

        private static /* synthetic */ OffsetType[] method_36719() {
            return new OffsetType[]{NONE, XZ, XYZ};
        }

        static {
            field_10658 = OffsetType.method_36719();
        }
    }
}

