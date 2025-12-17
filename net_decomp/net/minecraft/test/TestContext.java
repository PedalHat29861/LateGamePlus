/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.util.Either
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.embedded.EmbeddedChannel
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.test;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TimedTaskRunner;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import org.jspecify.annotations.Nullable;

public class TestContext {
    private final GameTestState test;
    private boolean hasFinalClause;

    public TestContext(GameTestState test) {
        this.test = test;
    }

    public GameTestException createError(Text message) {
        return new GameTestException(message, this.test.getTick());
    }

    public GameTestException createError(String translationKey, Object ... args) {
        return this.createError(Text.stringifiedTranslatable(translationKey, args));
    }

    public PositionedException createError(BlockPos pos, Text message) {
        return new PositionedException(message, this.getAbsolutePos(pos), pos, this.test.getTick());
    }

    public PositionedException createError(BlockPos pos, String translationKey, Object ... args) {
        return this.createError(pos, Text.stringifiedTranslatable(translationKey, args));
    }

    public ServerWorld getWorld() {
        return this.test.getWorld();
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getWorld().getBlockState(this.getAbsolutePos(pos));
    }

    public <T extends BlockEntity> T getBlockEntity(BlockPos pos, Class<T> clazz) {
        BlockEntity blockEntity = this.getWorld().getBlockEntity(this.getAbsolutePos(pos));
        if (blockEntity == null) {
            throw this.createError(pos, "test.error.missing_block_entity", new Object[0]);
        }
        if (clazz.isInstance(blockEntity)) {
            return (T)((BlockEntity)clazz.cast(blockEntity));
        }
        throw this.createError(pos, "test.error.wrong_block_entity", blockEntity.getType().getRegistryEntry().getIdAsString());
    }

    public void killAllEntities() {
        this.killAllEntities(Entity.class);
    }

    public void killAllEntities(Class<? extends Entity> entityClass) {
        Box box = this.getTestBox();
        List<Entity> list = this.getWorld().getEntitiesByClass(entityClass, box.expand(1.0), entity -> !(entity instanceof PlayerEntity));
        list.forEach(entity -> entity.kill(this.getWorld()));
    }

    public ItemEntity spawnItem(Item item, Vec3d pos) {
        ServerWorld serverWorld = this.getWorld();
        Vec3d vec3d = this.getAbsolute(pos);
        ItemEntity itemEntity = new ItemEntity(serverWorld, vec3d.x, vec3d.y, vec3d.z, new ItemStack(item, 1));
        itemEntity.setVelocity(0.0, 0.0, 0.0);
        serverWorld.spawnEntity(itemEntity);
        return itemEntity;
    }

    public ItemEntity spawnItem(Item item, float x, float y, float z) {
        return this.spawnItem(item, new Vec3d(x, y, z));
    }

    public ItemEntity spawnItem(Item item, BlockPos pos) {
        return this.spawnItem(item, pos.getX(), pos.getY(), pos.getZ());
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, BlockPos pos) {
        return this.spawnEntity(type, Vec3d.ofBottomCenter(pos));
    }

    public <E extends Entity> List<E> spawnEntities(EntityType<E> type, BlockPos pos, int count) {
        return this.spawnEntities(type, Vec3d.ofBottomCenter(pos), count);
    }

    public <E extends Entity> List<E> spawnEntities(EntityType<E> type, Vec3d pos, int count) {
        ArrayList<E> list = new ArrayList<E>();
        for (int i = 0; i < count; ++i) {
            list.add(this.spawnEntity(type, pos));
        }
        return list;
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, Vec3d pos) {
        return this.spawnEntity(type, pos, null);
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, Vec3d pos, @Nullable SpawnReason reason) {
        ServerWorld serverWorld = this.getWorld();
        E entity = type.create(serverWorld, SpawnReason.STRUCTURE);
        if (entity == null) {
            throw this.createError(BlockPos.ofFloored(pos), "test.error.spawn_failure", type.getRegistryEntry().getIdAsString());
        }
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            mobEntity.setPersistent();
        }
        Vec3d vec3d = this.getAbsolute(pos);
        float f = ((Entity)entity).applyRotation(this.getRotation());
        ((Entity)entity).refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f, ((Entity)entity).getPitch());
        ((Entity)entity).setBodyYaw(f);
        ((Entity)entity).setHeadYaw(f);
        if (reason != null && entity instanceof MobEntity) {
            MobEntity mobEntity2 = (MobEntity)entity;
            mobEntity2.initialize(this.getWorld(), this.getWorld().getLocalDifficulty(mobEntity2.getBlockPos()), reason, null);
        }
        serverWorld.spawnEntityAndPassengers((Entity)entity);
        return entity;
    }

    public <E extends MobEntity> E spawnEntity(EntityType<E> type, int x, int y, int z, SpawnReason reason) {
        return (E)((MobEntity)this.spawnEntity(type, new Vec3d(x, y, z), reason));
    }

    public void damage(Entity entity, DamageSource damageSource, float amount) {
        entity.damage(this.getWorld(), damageSource, amount);
    }

    public void killEntity(Entity entity) {
        entity.kill(this.getWorld());
    }

    public <E extends Entity> E expectEntityInWorld(EntityType<E> type) {
        return this.expectEntity(type, 0, 0, 0, 2.147483647E9);
    }

    public <E extends Entity> E expectEntity(EntityType<E> type, int x, int y, int z, double margin) {
        List<E> list = this.getEntitiesAround(type, x, y, z, margin);
        if (list.isEmpty()) {
            throw this.createError("test.error.expected_entity_around", type.getName(), x, y, z);
        }
        if (list.size() > 1) {
            throw this.createError("test.error.too_many_entities", type.getUntranslatedName(), x, y, z, list.size());
        }
        Vec3d vec3d = this.getAbsolute(new Vec3d(x, y, z));
        list.sort((a, b) -> {
            double d = a.getEntityPos().distanceTo(vec3d);
            double e = b.getEntityPos().distanceTo(vec3d);
            return Double.compare(d, e);
        });
        return (E)((Entity)list.get(0));
    }

    public <E extends Entity> List<E> getEntitiesAround(EntityType<E> type, int x, int y, int z, double margin) {
        return this.getEntitiesAround(type, Vec3d.ofBottomCenter(new BlockPos(x, y, z)), margin);
    }

    public <E extends Entity> List<E> getEntitiesAround(EntityType<E> type, Vec3d pos, double margin) {
        ServerWorld serverWorld = this.getWorld();
        Vec3d vec3d = this.getAbsolute(pos);
        Box box = this.test.getBoundingBox();
        Box box2 = new Box(vec3d.add(-margin, -margin, -margin), vec3d.add(margin, margin, margin));
        return serverWorld.getEntitiesByType(type, box, entity -> entity.getBoundingBox().intersects(box2) && entity.isAlive());
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, int x, int y, int z) {
        return this.spawnEntity(type, new BlockPos(x, y, z));
    }

    public <E extends Entity> E spawnEntity(EntityType<E> type, float x, float y, float z) {
        return this.spawnEntity(type, new Vec3d(x, y, z));
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, BlockPos pos) {
        MobEntity mobEntity = (MobEntity)this.spawnEntity(type, pos);
        mobEntity.clearGoalsAndTasks();
        return (E)mobEntity;
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, int x, int y, int z) {
        return this.spawnMob(type, new BlockPos(x, y, z));
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, Vec3d pos) {
        MobEntity mobEntity = (MobEntity)this.spawnEntity(type, pos);
        mobEntity.clearGoalsAndTasks();
        return (E)mobEntity;
    }

    public <E extends MobEntity> E spawnMob(EntityType<E> type, float x, float y, float z) {
        return this.spawnMob(type, new Vec3d(x, y, z));
    }

    public void setEntityPos(MobEntity entity, float x, float y, float z) {
        Vec3d vec3d = this.getAbsolute(new Vec3d(x, y, z));
        entity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, entity.getYaw(), entity.getPitch());
    }

    public TimedTaskRunner startMovingTowards(MobEntity entity, BlockPos pos, float speed) {
        return this.createTimedTaskRunner().expectMinDurationAndRun(2, () -> {
            Path path = entity.getNavigation().findPathTo(this.getAbsolutePos(pos), 0);
            entity.getNavigation().startMovingAlong(path, speed);
        });
    }

    public void pushButton(int x, int y, int z) {
        this.pushButton(new BlockPos(x, y, z));
    }

    public void pushButton(BlockPos pos) {
        this.expectBlockIn(BlockTags.BUTTONS, pos);
        BlockPos blockPos = this.getAbsolutePos(pos);
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        ButtonBlock buttonBlock = (ButtonBlock)blockState.getBlock();
        buttonBlock.powerOn(blockState, this.getWorld(), blockPos, null);
    }

    public void useBlock(BlockPos pos) {
        this.useBlock(pos, this.createMockPlayer(GameMode.CREATIVE));
    }

    public void useBlock(BlockPos pos, PlayerEntity player) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        this.useBlock(pos, player, new BlockHitResult(Vec3d.ofCenter(blockPos), Direction.NORTH, blockPos, true));
    }

    public void useBlock(BlockPos pos, PlayerEntity player, BlockHitResult result) {
        Hand hand;
        BlockPos blockPos = this.getAbsolutePos(pos);
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        ActionResult actionResult = blockState.onUseWithItem(player.getStackInHand(hand = Hand.MAIN_HAND), this.getWorld(), player, hand, result);
        if (actionResult.isAccepted()) {
            return;
        }
        if (actionResult instanceof ActionResult.PassToDefaultBlockAction && blockState.onUse(this.getWorld(), player, result).isAccepted()) {
            return;
        }
        ItemUsageContext itemUsageContext = new ItemUsageContext(player, hand, result);
        player.getStackInHand(hand).useOnBlock(itemUsageContext);
    }

    public LivingEntity drown(LivingEntity entity) {
        entity.setAir(0);
        entity.setHealth(0.25f);
        return entity;
    }

    public LivingEntity setHealthLow(LivingEntity entity) {
        entity.setHealth(0.25f);
        return entity;
    }

    public PlayerEntity createMockPlayer(final GameMode gameMode) {
        return new PlayerEntity(this, this.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player")){

            @Override
            public GameMode getGameMode() {
                return gameMode;
            }

            @Override
            public boolean isControlledByPlayer() {
                return false;
            }
        };
    }

    @Deprecated(forRemoval=true)
    public ServerPlayerEntity createMockCreativeServerPlayerInWorld() {
        ConnectedClientData connectedClientData = ConnectedClientData.createDefault(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this, this.getWorld().getServer(), this.getWorld(), connectedClientData.gameProfile(), connectedClientData.syncedOptions()){

            @Override
            public GameMode getGameMode() {
                return GameMode.CREATIVE;
            }
        };
        ClientConnection clientConnection = new ClientConnection(NetworkSide.SERVERBOUND);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new ChannelHandler[]{clientConnection});
        this.getWorld().getServer().getPlayerManager().onPlayerConnect(clientConnection, serverPlayerEntity, connectedClientData);
        return serverPlayerEntity;
    }

    public void toggleLever(int x, int y, int z) {
        this.toggleLever(new BlockPos(x, y, z));
    }

    public void toggleLever(BlockPos pos) {
        this.expectBlock(Blocks.LEVER, pos);
        BlockPos blockPos = this.getAbsolutePos(pos);
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        LeverBlock leverBlock = (LeverBlock)blockState.getBlock();
        leverBlock.togglePower(blockState, this.getWorld(), blockPos, null);
    }

    public void putAndRemoveRedstoneBlock(BlockPos pos, long delay) {
        this.setBlockState(pos, Blocks.REDSTONE_BLOCK);
        this.waitAndRun(delay, () -> this.setBlockState(pos, Blocks.AIR));
    }

    public void removeBlock(BlockPos pos) {
        this.getWorld().breakBlock(this.getAbsolutePos(pos), false, null);
    }

    public void setBlockState(int x, int y, int z, Block block) {
        this.setBlockState(new BlockPos(x, y, z), block);
    }

    public void setBlockState(int x, int y, int z, BlockState state) {
        this.setBlockState(new BlockPos(x, y, z), state);
    }

    public void setBlockState(BlockPos pos, Block block) {
        this.setBlockState(pos, block.getDefaultState());
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        this.getWorld().setBlockState(this.getAbsolutePos(pos), state, 3);
    }

    public void setBlockFacing(BlockPos pos, Block block, Direction facing) {
        this.setBlockFacing(pos, block.getDefaultState(), facing);
    }

    public void setBlockFacing(BlockPos pos, BlockState block, Direction facing) {
        BlockState blockState = block;
        if (block.contains(HorizontalFacingBlock.FACING)) {
            blockState = (BlockState)block.with(HorizontalFacingBlock.FACING, facing);
        }
        if (block.contains(Properties.FACING)) {
            blockState = (BlockState)block.with(Properties.FACING, facing);
        }
        this.getWorld().setBlockState(this.getAbsolutePos(pos), blockState, 3);
    }

    public void expectBlock(Block block, int x, int y, int z) {
        this.expectBlock(block, new BlockPos(x, y, z));
    }

    public void expectBlock(Block block, BlockPos pos) {
        BlockState blockState = this.getBlockState(pos);
        this.checkBlock(pos, block1 -> blockState.isOf(block), actualBlock -> Text.translatable("test.error.expected_block", block.getName(), actualBlock.getName()));
    }

    public void dontExpectBlock(Block block, int x, int y, int z) {
        this.dontExpectBlock(block, new BlockPos(x, y, z));
    }

    public void dontExpectBlock(Block block, BlockPos pos) {
        this.checkBlock(pos, block1 -> !this.getBlockState(pos).isOf(block), actualBlock -> Text.translatable("test.error.unexpected_block", block.getName()));
    }

    public void expectBlockIn(TagKey<Block> tag, BlockPos pos) {
        this.checkBlockState(pos, state -> state.isIn(tag), state -> Text.translatable("test.error.expected_block_tag", Text.of(tag.id()), state.getBlock().getName()));
    }

    public void expectBlockAtEnd(Block block, int x, int y, int z) {
        this.expectBlockAtEnd(block, new BlockPos(x, y, z));
    }

    public void expectBlockAtEnd(Block block, BlockPos pos) {
        this.addInstantFinalTask(() -> this.expectBlock(block, pos));
    }

    public void checkBlock(BlockPos pos, Predicate<Block> predicate, Function<Block, Text> messageGetter) {
        this.checkBlockState(pos, state -> predicate.test(state.getBlock()), state -> (Text)messageGetter.apply(state.getBlock()));
    }

    public <T extends Comparable<T>> void expectBlockProperty(BlockPos pos, Property<T> property, T value) {
        BlockState blockState = this.getBlockState(pos);
        boolean bl = blockState.contains(property);
        if (!bl) {
            throw this.createError(pos, "test.error.block_property_missing", property.getName(), value);
        }
        if (!blockState.get(property).equals(value)) {
            throw this.createError(pos, "test.error.block_property_mismatch", property.getName(), value, blockState.get(property));
        }
    }

    public <T extends Comparable<T>> void checkBlockProperty(BlockPos pos, Property<T> property, Predicate<T> predicate, Text message) {
        this.checkBlockState(pos, state -> {
            if (!state.contains(property)) {
                return false;
            }
            Object comparable = state.get(property);
            return predicate.test(comparable);
        }, state -> message);
    }

    public void expectBlockState(BlockPos pos, BlockState state) {
        BlockState blockState = this.getBlockState(pos);
        if (!blockState.equals(state)) {
            throw this.createError(pos, "test.error.state_not_equal", state, blockState);
        }
    }

    public void checkBlockState(BlockPos pos, Predicate<BlockState> predicate, Function<BlockState, Text> messageGetter) {
        BlockState blockState = this.getBlockState(pos);
        if (!predicate.test(blockState)) {
            throw this.createError(pos, messageGetter.apply(blockState));
        }
    }

    public <T extends BlockEntity> void checkBlockEntity(BlockPos pos, Class<T> clazz, Predicate<T> predicate, Supplier<Text> messageGetter) {
        T blockEntity = this.getBlockEntity(pos, clazz);
        if (!predicate.test(blockEntity)) {
            throw this.createError(pos, messageGetter.get());
        }
    }

    public void expectRedstonePower(BlockPos pos, Direction direction, IntPredicate powerPredicate, Supplier<Text> messageGetter) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        ServerWorld serverWorld = this.getWorld();
        BlockState blockState = serverWorld.getBlockState(blockPos);
        int i = blockState.getWeakRedstonePower(serverWorld, blockPos, direction);
        if (!powerPredicate.test(i)) {
            throw this.createError(pos, messageGetter.get());
        }
    }

    public void expectEntity(EntityType<?> type) {
        if (!this.getWorld().hasEntities(type, this.getTestBox(), Entity::isAlive)) {
            throw this.createError("test.error.expected_entity_in_test", type.getName());
        }
    }

    public void expectEntityAt(EntityType<?> type, int x, int y, int z) {
        this.expectEntityAt(type, new BlockPos(x, y, z));
    }

    public void expectEntityAt(EntityType<?> type, BlockPos pos) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        if (!this.getWorld().hasEntities(type, new Box(blockPos), Entity::isAlive)) {
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
    }

    public void expectEntityInside(EntityType<?> type, Box box) {
        Box box2 = this.getAbsolute(box);
        if (!this.getWorld().hasEntities(type, box2, Entity::isAlive)) {
            throw this.createError(BlockPos.ofFloored(box.getCenter()), "test.error.expected_entity", type.getName());
        }
    }

    public void expectEntityIn(EntityType<?> type, Box box, Text message) {
        Box box2 = this.getAbsolute(box);
        if (!this.getWorld().hasEntities(type, box2, Entity::isAlive)) {
            throw this.createError(BlockPos.ofFloored(box.getCenter()), message);
        }
    }

    public void expectEntities(EntityType<?> type, int amount) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (list.size() != amount) {
            throw this.createError("test.error.expected_entity_count", amount, type.getName(), list.size());
        }
    }

    public void expectEntitiesAround(EntityType<?> type, BlockPos pos, int amount, double radius) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        List<?> list = this.getEntitiesAround(type, pos, radius);
        if (list.size() != amount) {
            throw this.createError(pos, "test.error.expected_entity_count", amount, type.getName(), list.size());
        }
    }

    public void expectEntityAround(EntityType<?> type, BlockPos pos, double radius) {
        List<?> list = this.getEntitiesAround(type, pos, radius);
        if (list.isEmpty()) {
            BlockPos blockPos = this.getAbsolutePos(pos);
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
    }

    public <T extends Entity> List<T> getEntitiesAround(EntityType<T> type, BlockPos pos, double radius) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        return this.getWorld().getEntitiesByType(type, new Box(blockPos).expand(radius), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityType<T> type) {
        return this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
    }

    public void expectEntityAt(Entity entity, int x, int y, int z) {
        this.expectEntityAt(entity, new BlockPos(x, y, z));
    }

    public void expectEntityAt(Entity entity, BlockPos pos) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(entity.getType(), new Box(blockPos), Entity::isAlive);
        list.stream().filter(e -> e == entity).findFirst().orElseThrow(() -> this.createError(pos, "test.error.expected_entity", entity.getType().getName()));
    }

    public void expectItemsAt(Item item, BlockPos pos, double radius, int amount) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        List<ItemEntity> list = this.getWorld().getEntitiesByType(EntityType.ITEM, new Box(blockPos).expand(radius), Entity::isAlive);
        int i = 0;
        for (ItemEntity itemEntity : list) {
            ItemStack itemStack = itemEntity.getStack();
            if (!itemStack.isOf(item)) continue;
            i += itemStack.getCount();
        }
        if (i != amount) {
            throw this.createError(pos, "test.error.expected_items_count", amount, item.getName(), i);
        }
    }

    public void expectItemAt(Item item, BlockPos pos, double radius) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (!this.getWorld().hasEntities(EntityType.ITEM, new Box(blockPos).expand(radius), predicate)) {
            throw this.createError(pos, "test.error.expected_item", item.getName());
        }
    }

    public void dontExpectItemAt(Item item, BlockPos pos, double radius) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (this.getWorld().hasEntities(EntityType.ITEM, new Box(blockPos).expand(radius), predicate)) {
            throw this.createError(pos, "test.error.unexpected_item", item.getName());
        }
    }

    public void expectItem(Item item) {
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (!this.getWorld().hasEntities(EntityType.ITEM, this.getTestBox(), predicate)) {
            throw this.createError("test.error.expected_item", item.getName());
        }
    }

    public void dontExpectItem(Item item) {
        Predicate<ItemEntity> predicate = entity -> entity.isAlive() && entity.getStack().isOf(item);
        if (this.getWorld().hasEntities(EntityType.ITEM, this.getTestBox(), predicate)) {
            throw this.createError("test.error.unexpected_item", item.getName());
        }
    }

    public void dontExpectEntity(EntityType<?> type) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getTestBox(), Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.createError(list.getFirst().getBlockPos(), "test.error.unexpected_entity", type.getName());
        }
    }

    public void dontExpectEntityAt(EntityType<?> type, int x, int y, int z) {
        this.dontExpectEntityAt(type, new BlockPos(x, y, z));
    }

    public void dontExpectEntityAt(EntityType<?> type, BlockPos pos) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        if (this.getWorld().hasEntities(type, new Box(blockPos), Entity::isAlive)) {
            throw this.createError(pos, "test.error.unexpected_entity", type.getName());
        }
    }

    public void dontExpectEntityBetween(EntityType<?> type, Box box) {
        Box box2 = this.getAbsolute(box);
        List<Entity> list = this.getWorld().getEntitiesByType(type, box2, Entity::isAlive);
        if (!list.isEmpty()) {
            throw this.createError(list.getFirst().getBlockPos(), "test.error.unexpected_entity", type.getName());
        }
    }

    public void expectEntityToTouch(EntityType<?> type, double x, double y, double z) {
        Vec3d vec3d = new Vec3d(x, y, z);
        Vec3d vec3d2 = this.getAbsolute(vec3d);
        Predicate<Entity> predicate = entity -> entity.getBoundingBox().intersects(vec3d2, vec3d2);
        if (!this.getWorld().hasEntities(type, this.getTestBox(), predicate)) {
            throw this.createError("test.error.expected_entity_touching", type.getName(), vec3d2.getX(), vec3d2.getY(), vec3d2.getZ(), x, y, z);
        }
    }

    public void dontExpectEntityToTouch(EntityType<?> type, double x, double y, double z) {
        Vec3d vec3d = new Vec3d(x, y, z);
        Vec3d vec3d2 = this.getAbsolute(vec3d);
        Predicate<Entity> predicate = entity -> !entity.getBoundingBox().intersects(vec3d2, vec3d2);
        if (!this.getWorld().hasEntities(type, this.getTestBox(), predicate)) {
            throw this.createError("test.error.expected_entity_not_touching", type.getName(), vec3d2.getX(), vec3d2.getY(), vec3d2.getZ(), x, y, z);
        }
    }

    public <E extends Entity, T> void expectEntity(BlockPos pos, EntityType<E> type, Predicate<E> predicate) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(type, new Box(blockPos), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", type.getName());
        }
        for (Entity entity : list) {
            if (predicate.test(entity)) continue;
            throw this.createError(entity.getBlockPos(), "test.error.expected_entity_data_predicate", entity.getName());
        }
    }

    public <E extends Entity, T> void expectEntityWithData(BlockPos pos, EntityType<E> type, Function<? super E, T> entityDataGetter, @Nullable T data) {
        this.expectEntityWithData(new Box(pos), type, entityDataGetter, data);
    }

    public <E extends Entity, T> void expectEntityWithData(Box box, EntityType<E> type, Function<? super E, T> entityDataGetter, @Nullable T data) {
        List<Entity> list = this.getWorld().getEntitiesByType(type, this.getAbsolute(box), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.createError(BlockPos.ofFloored(box.getHorizontalCenter()), "test.error.expected_entity", type.getName());
        }
        for (Entity entity : list) {
            T object = entityDataGetter.apply(entity);
            if (Objects.equals(object, data)) continue;
            throw this.createError(BlockPos.ofFloored(box.getHorizontalCenter()), "test.error.expected_entity_data", data, object);
        }
    }

    public <E extends LivingEntity> void expectEntityHoldingItem(BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        List<LivingEntity> list = this.getWorld().getEntitiesByType(entityType, new Box(blockPos), Entity::isAlive);
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", entityType.getName());
        }
        for (LivingEntity livingEntity : list) {
            if (!livingEntity.isHolding(item)) continue;
            return;
        }
        throw this.createError(pos, "test.error.expected_entity_holding", item.getName());
    }

    public <E extends Entity> void expectEntityWithItem(BlockPos pos, EntityType<E> entityType, Item item) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        List<Entity> list = this.getWorld().getEntitiesByType(entityType, new Box(blockPos), entity -> ((Entity)entity).isAlive());
        if (list.isEmpty()) {
            throw this.createError(pos, "test.error.expected_entity", entityType.getName());
        }
        for (Entity entity2 : list) {
            if (!((InventoryOwner)((Object)entity2)).getInventory().containsAny(stack -> stack.isOf(item))) continue;
            return;
        }
        throw this.createError(pos, "test.error.expected_entity_having", item.getName());
    }

    public void expectEmptyContainer(BlockPos pos) {
        LockableContainerBlockEntity lockableContainerBlockEntity = this.getBlockEntity(pos, LockableContainerBlockEntity.class);
        if (!lockableContainerBlockEntity.isEmpty()) {
            throw this.createError(pos, "test.error.expected_empty_container", new Object[0]);
        }
    }

    public void expectContainerWithSingle(BlockPos pos, Item item) {
        LockableContainerBlockEntity lockableContainerBlockEntity = this.getBlockEntity(pos, LockableContainerBlockEntity.class);
        if (lockableContainerBlockEntity.count(item) != 1) {
            throw this.createError(pos, "test.error.expected_container_contents_single", item.getName());
        }
    }

    public void expectContainerWith(BlockPos pos, Item item) {
        LockableContainerBlockEntity lockableContainerBlockEntity = this.getBlockEntity(pos, LockableContainerBlockEntity.class);
        if (lockableContainerBlockEntity.count(item) == 0) {
            throw this.createError(pos, "test.error.expected_container_contents", item.getName());
        }
    }

    public void expectSameStates(BlockBox checkedBlockBox, BlockPos correctStatePos) {
        BlockPos.stream(checkedBlockBox).forEach(checkedPos -> {
            BlockPos blockPos2 = correctStatePos.add(checkedPos.getX() - checkedBlockBox.getMinX(), checkedPos.getY() - checkedBlockBox.getMinY(), checkedPos.getZ() - checkedBlockBox.getMinZ());
            this.expectSameStates((BlockPos)checkedPos, blockPos2);
        });
    }

    public void expectSameStates(BlockPos checkedPos, BlockPos correctStatePos) {
        BlockState blockState2;
        BlockState blockState = this.getBlockState(checkedPos);
        if (blockState != (blockState2 = this.getBlockState(correctStatePos))) {
            throw this.createError(checkedPos, "test.error.state_not_equal", blockState2, blockState);
        }
    }

    public void expectContainerWith(long delay, BlockPos pos, Item item) {
        this.runAtTick(delay, () -> this.expectContainerWithSingle(pos, item));
    }

    public void expectEmptyContainer(long delay, BlockPos pos) {
        this.runAtTick(delay, () -> this.expectEmptyContainer(pos));
    }

    public <E extends Entity, T> void expectEntityWithDataEnd(BlockPos pos, EntityType<E> type, Function<E, T> entityDataGetter, T data) {
        this.addInstantFinalTask(() -> this.expectEntityWithData(pos, type, entityDataGetter, data));
    }

    public <E extends Entity> void testEntity(E entity, Predicate<E> predicate, Text message) {
        if (!predicate.test(entity)) {
            throw this.createError(entity.getBlockPos(), "test.error.entity_property", entity.getName(), message);
        }
    }

    public <E extends Entity, T> void testEntityProperty(E entity, Function<E, T> propertyGetter, T value, Text message) {
        T object = propertyGetter.apply(entity);
        if (!object.equals(value)) {
            throw this.createError(entity.getBlockPos(), "test.error.entity_property_details", entity.getName(), message, object, value);
        }
    }

    public void expectEntityHasEffect(LivingEntity entity, RegistryEntry<StatusEffect> effect, int amplifier) {
        StatusEffectInstance statusEffectInstance = entity.getStatusEffect(effect);
        if (statusEffectInstance == null || statusEffectInstance.getAmplifier() != amplifier) {
            throw this.createError("test.error.expected_entity_effect", entity.getName(), PotionContentsComponent.getEffectText(effect, amplifier));
        }
    }

    public void expectEntityAtEnd(EntityType<?> type, int x, int y, int z) {
        this.expectEntityAtEnd(type, new BlockPos(x, y, z));
    }

    public void expectEntityAtEnd(EntityType<?> type, BlockPos pos) {
        this.addInstantFinalTask(() -> this.expectEntityAt(type, pos));
    }

    public void dontExpectEntityAtEnd(EntityType<?> type, int x, int y, int z) {
        this.dontExpectEntityAtEnd(type, new BlockPos(x, y, z));
    }

    public void dontExpectEntityAtEnd(EntityType<?> type, BlockPos pos) {
        this.addInstantFinalTask(() -> this.dontExpectEntityAt(type, pos));
    }

    public void complete() {
        this.test.completeIfSuccessful();
    }

    private void markFinalCause() {
        if (this.hasFinalClause) {
            throw new IllegalStateException("This test already has final clause");
        }
        this.hasFinalClause = true;
    }

    public void addFinalTask(Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(0L, runnable).completeIfSuccessful();
    }

    public void addInstantFinalTask(Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(runnable).completeIfSuccessful();
    }

    public void addFinalTaskWithDuration(int duration, Runnable runnable) {
        this.markFinalCause();
        this.test.createTimedTaskRunner().createAndAdd(duration, runnable).completeIfSuccessful();
    }

    public void runAtTick(long tick, Runnable runnable) {
        this.test.runAtTick(tick, runnable);
    }

    public void waitAndRun(long ticks, Runnable runnable) {
        this.runAtTick((long)this.test.getTick() + ticks, runnable);
    }

    public void forceRandomTick(BlockPos pos) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        ServerWorld serverWorld = this.getWorld();
        serverWorld.getBlockState(blockPos).randomTick(serverWorld, blockPos, serverWorld.random);
    }

    public void forceScheduledTick(BlockPos pos) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        ServerWorld serverWorld = this.getWorld();
        serverWorld.getBlockState(blockPos).scheduledTick(serverWorld, blockPos, serverWorld.random);
    }

    public void forceTickIceAndSnow(BlockPos pos) {
        BlockPos blockPos = this.getAbsolutePos(pos);
        ServerWorld serverWorld = this.getWorld();
        serverWorld.tickIceAndSnow(blockPos);
    }

    public void forceTickIceAndSnow() {
        Box box = this.getRelativeTestBox();
        int i = (int)Math.floor(box.maxX);
        int j = (int)Math.floor(box.maxZ);
        int k = (int)Math.floor(box.maxY);
        for (int l = (int)Math.floor(box.minX); l < i; ++l) {
            for (int m = (int)Math.floor(box.minZ); m < j; ++m) {
                this.forceTickIceAndSnow(new BlockPos(l, k, m));
            }
        }
    }

    public int getRelativeTopY(Heightmap.Type heightmap, int x, int z) {
        BlockPos blockPos = this.getAbsolutePos(new BlockPos(x, 0, z));
        return this.getRelativePos(this.getWorld().getTopPosition(heightmap, blockPos)).getY();
    }

    public void throwPositionedException(Text message, BlockPos pos) {
        throw this.createError(pos, message);
    }

    public void throwPositionedException(Text message, Entity entity) {
        throw this.createError(entity.getBlockPos(), message);
    }

    public void throwGameTestException(Text message) {
        throw this.createError(message);
    }

    public void throwGameTestException(String message) {
        throw this.createError(Text.literal(message));
    }

    public void addTask(Runnable task) {
        this.test.createTimedTaskRunner().createAndAdd(task).fail(() -> this.createError("test.error.fail", new Object[0]));
    }

    public void runAtEveryTick(Runnable task) {
        LongStream.range(this.test.getTick(), this.test.getTickLimit()).forEach(tick -> this.test.runAtTick(tick, task::run));
    }

    public TimedTaskRunner createTimedTaskRunner() {
        return this.test.createTimedTaskRunner();
    }

    public BlockPos getAbsolutePos(BlockPos pos) {
        BlockPos blockPos = this.test.getOrigin();
        BlockPos blockPos2 = blockPos.add(pos);
        return StructureTemplate.transformAround(blockPos2, BlockMirror.NONE, this.test.getRotation(), blockPos);
    }

    public BlockPos getRelativePos(BlockPos pos) {
        BlockPos blockPos = this.test.getOrigin();
        BlockRotation blockRotation = this.test.getRotation().rotate(BlockRotation.CLOCKWISE_180);
        BlockPos blockPos2 = StructureTemplate.transformAround(pos, BlockMirror.NONE, blockRotation, blockPos);
        return blockPos2.subtract(blockPos);
    }

    public Box getAbsolute(Box box) {
        Vec3d vec3d = this.getAbsolute(box.getMinPos());
        Vec3d vec3d2 = this.getAbsolute(box.getMaxPos());
        return new Box(vec3d, vec3d2);
    }

    public Box getRelative(Box box) {
        Vec3d vec3d = this.getRelative(box.getMinPos());
        Vec3d vec3d2 = this.getRelative(box.getMaxPos());
        return new Box(vec3d, vec3d2);
    }

    public Vec3d getAbsolute(Vec3d pos) {
        Vec3d vec3d = Vec3d.of(this.test.getOrigin());
        return StructureTemplate.transformAround(vec3d.add(pos), BlockMirror.NONE, this.test.getRotation(), this.test.getOrigin());
    }

    public Vec3d getRelative(Vec3d pos) {
        Vec3d vec3d = Vec3d.of(this.test.getOrigin());
        return StructureTemplate.transformAround(pos.subtract(vec3d), BlockMirror.NONE, this.test.getRotation(), this.test.getOrigin());
    }

    public BlockRotation getRotation() {
        return this.test.getRotation();
    }

    public Direction getDirection() {
        return this.test.getRotation().rotate(Direction.SOUTH);
    }

    public Direction rotate(Direction direction) {
        return this.getRotation().rotate(direction);
    }

    public void assertTrue(boolean condition, Text message) {
        if (!condition) {
            throw this.createError(message);
        }
    }

    public void assertTrue(boolean condition, String message) {
        this.assertTrue(condition, Text.literal(message));
    }

    public <N> void assertEquals(N expected, N value, String message) {
        this.assertEquals(expected, value, Text.literal(message));
    }

    public <N> void assertEquals(N expected, N value, Text message) {
        if (!expected.equals(value)) {
            throw this.createError("test.error.value_not_equal", message, expected, value);
        }
    }

    public void assertFalse(boolean condition, Text message) {
        this.assertTrue(!condition, message);
    }

    public void assertFalse(boolean condition, String message) {
        this.assertFalse(condition, Text.literal(message));
    }

    public long getTick() {
        return this.test.getTick();
    }

    public Box getTestBox() {
        return this.test.getBoundingBox();
    }

    public Box getRelativeTestBox() {
        Box box = this.test.getBoundingBox();
        BlockRotation blockRotation = this.test.getRotation();
        switch (blockRotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new Box(0.0, 0.0, 0.0, box.getLengthZ(), box.getLengthY(), box.getLengthX());
            }
        }
        return new Box(0.0, 0.0, 0.0, box.getLengthX(), box.getLengthY(), box.getLengthZ());
    }

    public void forEachRelativePos(Consumer<BlockPos> posConsumer) {
        Box box = this.getRelativeTestBox().shrink(1.0, 1.0, 1.0);
        BlockPos.Mutable.stream(box).forEach(posConsumer);
    }

    public void forEachRemainingTick(Runnable runnable) {
        LongStream.range(this.test.getTick(), this.test.getTickLimit()).forEach(tick -> this.test.runAtTick(tick, runnable::run));
    }

    public void useStackOnBlock(PlayerEntity player, ItemStack stack, BlockPos pos, Direction direction) {
        BlockPos blockPos = this.getAbsolutePos(pos.offset(direction));
        BlockHitResult blockHitResult = new BlockHitResult(Vec3d.ofCenter(blockPos), direction, blockPos, false);
        ItemUsageContext itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, blockHitResult);
        stack.useOnBlock(itemUsageContext);
    }

    public void setBiome(RegistryKey<Biome> biome) {
        Box box = this.getTestBox();
        BlockPos blockPos = BlockPos.ofFloored(box.minX, box.minY, box.minZ);
        BlockPos blockPos2 = BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fillBiome(this.getWorld(), blockPos, blockPos2, this.getWorld().getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(biome));
        if (either.right().isPresent()) {
            throw this.createError("test.error.set_biome", new Object[0]);
        }
    }
}

