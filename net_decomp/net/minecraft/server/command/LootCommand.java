/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LootCommand {
    private static final DynamicCommandExceptionType NO_HELD_ITEMS_EXCEPTION = new DynamicCommandExceptionType(entityName -> Text.stringifiedTranslatable("commands.drop.no_held_items", entityName));
    private static final DynamicCommandExceptionType NO_LOOT_TABLE_ENTITY_EXCEPTION = new DynamicCommandExceptionType(entityName -> Text.stringifiedTranslatable("commands.drop.no_loot_table.entity", entityName));
    private static final DynamicCommandExceptionType NO_LOOT_TABLE_BLOCK_EXCEPTION = new DynamicCommandExceptionType(blockName -> Text.stringifiedTranslatable("commands.drop.no_loot_table.block", blockName));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(LootCommand.addTargetArguments((LiteralArgumentBuilder)CommandManager.literal("loot").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)), (builder, constructor) -> builder.then(CommandManager.literal("fish").then(CommandManager.argument("loot_table", RegistryEntryArgumentType.lootTable(commandRegistryAccess)).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> LootCommand.executeFish((CommandContext<ServerCommandSource>)context, RegistryEntryArgumentType.getLootTable((CommandContext<ServerCommandSource>)context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemStack.EMPTY, constructor))).then(CommandManager.argument("tool", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> LootCommand.executeFish((CommandContext<ServerCommandSource>)context, RegistryEntryArgumentType.getLootTable((CommandContext<ServerCommandSource>)context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemStackArgumentType.getItemStackArgument(context, "tool").createStack(1, false), constructor)))).then(CommandManager.literal("mainhand").executes(context -> LootCommand.executeFish((CommandContext<ServerCommandSource>)context, RegistryEntryArgumentType.getLootTable((CommandContext<ServerCommandSource>)context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.MAINHAND), constructor)))).then(CommandManager.literal("offhand").executes(context -> LootCommand.executeFish((CommandContext<ServerCommandSource>)context, RegistryEntryArgumentType.getLootTable((CommandContext<ServerCommandSource>)context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.OFFHAND), constructor)))))).then(CommandManager.literal("loot").then(CommandManager.argument("loot_table", RegistryEntryArgumentType.lootTable(commandRegistryAccess)).executes(context -> LootCommand.executeLoot((CommandContext<ServerCommandSource>)context, RegistryEntryArgumentType.getLootTable((CommandContext<ServerCommandSource>)context, "loot_table"), constructor)))).then(CommandManager.literal("kill").then(CommandManager.argument("target", EntityArgumentType.entity()).executes(context -> LootCommand.executeKill((CommandContext<ServerCommandSource>)context, EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), constructor)))).then(CommandManager.literal("mine").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> LootCommand.executeMine((CommandContext<ServerCommandSource>)context, BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemStack.EMPTY, constructor))).then(CommandManager.argument("tool", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> LootCommand.executeMine((CommandContext<ServerCommandSource>)context, BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemStackArgumentType.getItemStackArgument(context, "tool").createStack(1, false), constructor)))).then(CommandManager.literal("mainhand").executes(context -> LootCommand.executeMine((CommandContext<ServerCommandSource>)context, BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.MAINHAND), constructor)))).then(CommandManager.literal("offhand").executes(context -> LootCommand.executeMine((CommandContext<ServerCommandSource>)context, BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.OFFHAND), constructor)))))));
    }

    private static <T extends ArgumentBuilder<ServerCommandSource, T>> T addTargetArguments(T rootArgument, SourceConstructor sourceConstructor) {
        return (T)rootArgument.then(((LiteralArgumentBuilder)CommandManager.literal("replace").then(CommandManager.literal("entity").then(CommandManager.argument("entities", EntityArgumentType.entities()).then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()), (context, stacks, messageSender) -> LootCommand.executeReplace(EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "entities"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), stacks.size(), stacks, messageSender)).then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("count", IntegerArgumentType.integer((int)0)), (context, stacks, messageSender) -> LootCommand.executeReplace(EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "entities"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), stacks, messageSender))))))).then(CommandManager.literal("block").then(CommandManager.argument("targetPos", BlockPosArgumentType.blockPos()).then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()), (context, stacks, messageSender) -> LootCommand.executeBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "targetPos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), stacks.size(), stacks, messageSender)).then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("count", IntegerArgumentType.integer((int)0)), (context, stacks, messageSender) -> LootCommand.executeBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "targetPos"), IntegerArgumentType.getInteger((CommandContext)context, (String)"slot"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), stacks, messageSender))))))).then(CommandManager.literal("insert").then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targetPos", BlockPosArgumentType.blockPos()), (context, stacks, messageSender) -> LootCommand.executeInsert((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "targetPos"), stacks, messageSender)))).then(CommandManager.literal("give").then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("players", EntityArgumentType.players()), (context, stacks, messageSender) -> LootCommand.executeGive(EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "players"), stacks, messageSender)))).then(CommandManager.literal("spawn").then(sourceConstructor.construct((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targetPos", Vec3ArgumentType.vec3()), (context, stacks, messageSender) -> LootCommand.executeSpawn((ServerCommandSource)context.getSource(), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "targetPos"), stacks, messageSender))));
    }

    private static Inventory getBlockInventory(ServerCommandSource source, BlockPos pos) throws CommandSyntaxException {
        BlockEntity blockEntity = source.getWorld().getBlockEntity(pos);
        if (!(blockEntity instanceof Inventory)) {
            throw ItemCommand.NOT_A_CONTAINER_TARGET_EXCEPTION.create((Object)pos.getX(), (Object)pos.getY(), (Object)pos.getZ());
        }
        return (Inventory)((Object)blockEntity);
    }

    private static int executeInsert(ServerCommandSource source, BlockPos targetPos, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        Inventory inventory = LootCommand.getBlockInventory(source, targetPos);
        ArrayList list = Lists.newArrayListWithCapacity((int)stacks.size());
        for (ItemStack itemStack : stacks) {
            if (!LootCommand.insert(inventory, itemStack.copy())) continue;
            inventory.markDirty();
            list.add(itemStack);
        }
        messageSender.accept(list);
        return list.size();
    }

    private static boolean insert(Inventory inventory, ItemStack stack) {
        boolean bl = false;
        for (int i = 0; i < inventory.size() && !stack.isEmpty(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!inventory.isValid(i, stack)) continue;
            if (itemStack.isEmpty()) {
                inventory.setStack(i, stack);
                bl = true;
                break;
            }
            if (!LootCommand.itemsMatch(itemStack, stack)) continue;
            int j = stack.getMaxCount() - itemStack.getCount();
            int k = Math.min(stack.getCount(), j);
            stack.decrement(k);
            itemStack.increment(k);
            bl = true;
        }
        return bl;
    }

    private static int executeBlock(ServerCommandSource source, BlockPos targetPos, int slot, int stackCount, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        Inventory inventory = LootCommand.getBlockInventory(source, targetPos);
        int i = inventory.size();
        if (slot < 0 || slot >= i) {
            throw ItemCommand.NO_SUCH_SLOT_TARGET_EXCEPTION.create((Object)slot);
        }
        ArrayList list = Lists.newArrayListWithCapacity((int)stacks.size());
        for (int j = 0; j < stackCount; ++j) {
            ItemStack itemStack;
            int k = slot + j;
            ItemStack itemStack2 = itemStack = j < stacks.size() ? stacks.get(j) : ItemStack.EMPTY;
            if (!inventory.isValid(k, itemStack)) continue;
            inventory.setStack(k, itemStack);
            list.add(itemStack);
        }
        messageSender.accept(list);
        return list.size();
    }

    private static boolean itemsMatch(ItemStack first, ItemStack second) {
        return first.getCount() <= first.getMaxCount() && ItemStack.areItemsAndComponentsEqual(first, second);
    }

    private static int executeGive(Collection<ServerPlayerEntity> players, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        ArrayList list = Lists.newArrayListWithCapacity((int)stacks.size());
        for (ItemStack itemStack : stacks) {
            for (ServerPlayerEntity serverPlayerEntity : players) {
                if (!serverPlayerEntity.getInventory().insertStack(itemStack.copy())) continue;
                list.add(itemStack);
            }
        }
        messageSender.accept(list);
        return list.size();
    }

    private static void replace(Entity entity, List<ItemStack> stacks, int slot, int stackCount, List<ItemStack> addedStacks) {
        for (int i = 0; i < stackCount; ++i) {
            ItemStack itemStack = i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY;
            StackReference stackReference = entity.getStackReference(slot + i);
            if (stackReference == null || !stackReference.set(itemStack.copy())) continue;
            addedStacks.add(itemStack);
        }
    }

    private static int executeReplace(Collection<? extends Entity> targets, int slot, int stackCount, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        ArrayList list = Lists.newArrayListWithCapacity((int)stacks.size());
        for (Entity entity : targets) {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                LootCommand.replace(entity, stacks, slot, stackCount, list);
                serverPlayerEntity.currentScreenHandler.sendContentUpdates();
                continue;
            }
            LootCommand.replace(entity, stacks, slot, stackCount, list);
        }
        messageSender.accept(list);
        return list.size();
    }

    private static int executeSpawn(ServerCommandSource source, Vec3d pos, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();
        stacks.forEach(stack -> {
            ItemEntity itemEntity = new ItemEntity(serverWorld, vec3d.x, vec3d.y, vec3d.z, stack.copy());
            itemEntity.setToDefaultPickupDelay();
            serverWorld.spawnEntity(itemEntity);
        });
        messageSender.accept(stacks);
        return stacks.size();
    }

    private static void sendDroppedFeedback(ServerCommandSource source, List<ItemStack> stacks) {
        if (stacks.size() == 1) {
            ItemStack itemStack = stacks.get(0);
            source.sendFeedback(() -> Text.translatable("commands.drop.success.single", itemStack.getCount(), itemStack.toHoverableText()), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.drop.success.multiple", stacks.size()), false);
        }
    }

    private static void sendDroppedFeedback(ServerCommandSource source, List<ItemStack> stacks, RegistryKey<LootTable> lootTable) {
        if (stacks.size() == 1) {
            ItemStack itemStack = stacks.get(0);
            source.sendFeedback(() -> Text.translatable("commands.drop.success.single_with_table", itemStack.getCount(), itemStack.toHoverableText(), Text.of(lootTable.getValue())), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.drop.success.multiple_with_table", stacks.size(), Text.of(lootTable.getValue())), false);
        }
    }

    private static ItemStack getHeldItem(ServerCommandSource source, EquipmentSlot slot) throws CommandSyntaxException {
        Entity entity = source.getEntityOrThrow();
        if (entity instanceof LivingEntity) {
            return ((LivingEntity)entity).getEquippedStack(slot);
        }
        throw NO_HELD_ITEMS_EXCEPTION.create((Object)entity.getDisplayName());
    }

    private static int executeMine(CommandContext<ServerCommandSource> context, BlockPos pos, ItemStack stack, Target constructor) throws CommandSyntaxException {
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        ServerWorld serverWorld = serverCommandSource.getWorld();
        BlockState blockState = serverWorld.getBlockState(pos);
        BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
        Optional<RegistryKey<LootTable>> optional = blockState.getBlock().getLootTableKey();
        if (optional.isEmpty()) {
            throw NO_LOOT_TABLE_BLOCK_EXCEPTION.create((Object)blockState.getBlock().getName());
        }
        LootWorldContext.Builder builder = new LootWorldContext.Builder(serverWorld).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).add(LootContextParameters.BLOCK_STATE, blockState).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity).addOptional(LootContextParameters.THIS_ENTITY, serverCommandSource.getEntity()).add(LootContextParameters.TOOL, stack);
        List<ItemStack> list = blockState.getDroppedStacks(builder);
        return constructor.accept(context, list, stacks -> LootCommand.sendDroppedFeedback(serverCommandSource, stacks, (RegistryKey)optional.get()));
    }

    private static int executeKill(CommandContext<ServerCommandSource> context, Entity entity, Target constructor) throws CommandSyntaxException {
        Optional<RegistryKey<LootTable>> optional = entity.getLootTableKey();
        if (optional.isEmpty()) {
            throw NO_LOOT_TABLE_ENTITY_EXCEPTION.create((Object)entity.getDisplayName());
        }
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        LootWorldContext.Builder builder = new LootWorldContext.Builder(serverCommandSource.getWorld());
        Entity entity2 = serverCommandSource.getEntity();
        if (entity2 instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity2;
            builder.add(LootContextParameters.LAST_DAMAGE_PLAYER, playerEntity);
        }
        builder.add(LootContextParameters.DAMAGE_SOURCE, entity.getDamageSources().magic());
        builder.addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, entity2);
        builder.addOptional(LootContextParameters.ATTACKING_ENTITY, entity2);
        builder.add(LootContextParameters.THIS_ENTITY, entity);
        builder.add(LootContextParameters.ORIGIN, serverCommandSource.getPosition());
        LootWorldContext lootWorldContext = builder.build(LootContextTypes.ENTITY);
        LootTable lootTable = serverCommandSource.getServer().getReloadableRegistries().getLootTable(optional.get());
        ObjectArrayList<ItemStack> list = lootTable.generateLoot(lootWorldContext);
        return constructor.accept(context, (List<ItemStack>)list, stacks -> LootCommand.sendDroppedFeedback(serverCommandSource, stacks, (RegistryKey)optional.get()));
    }

    private static int executeLoot(CommandContext<ServerCommandSource> context, RegistryEntry<LootTable> lootTable, Target constructor) throws CommandSyntaxException {
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverCommandSource.getWorld()).addOptional(LootContextParameters.THIS_ENTITY, serverCommandSource.getEntity()).add(LootContextParameters.ORIGIN, serverCommandSource.getPosition()).build(LootContextTypes.CHEST);
        return LootCommand.getFeedbackMessageSingle(context, lootTable, lootWorldContext, constructor);
    }

    private static int executeFish(CommandContext<ServerCommandSource> context, RegistryEntry<LootTable> lootTable, BlockPos pos, ItemStack stack, Target constructor) throws CommandSyntaxException {
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverCommandSource.getWorld()).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).add(LootContextParameters.TOOL, stack).addOptional(LootContextParameters.THIS_ENTITY, serverCommandSource.getEntity()).build(LootContextTypes.FISHING);
        return LootCommand.getFeedbackMessageSingle(context, lootTable, lootWorldContext, constructor);
    }

    private static int getFeedbackMessageSingle(CommandContext<ServerCommandSource> context, RegistryEntry<LootTable> lootTable, LootWorldContext lootContextParameters, Target constructor) throws CommandSyntaxException {
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        ObjectArrayList<ItemStack> list = lootTable.value().generateLoot(lootContextParameters);
        return constructor.accept(context, (List<ItemStack>)list, stacks -> LootCommand.sendDroppedFeedback(serverCommandSource, stacks));
    }

    @FunctionalInterface
    static interface SourceConstructor {
        public ArgumentBuilder<ServerCommandSource, ?> construct(ArgumentBuilder<ServerCommandSource, ?> var1, Target var2);
    }

    @FunctionalInterface
    static interface Target {
        public int accept(CommandContext<ServerCommandSource> var1, List<ItemStack> var2, FeedbackMessage var3) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface FeedbackMessage {
        public void accept(List<ItemStack> var1) throws CommandSyntaxException;
    }
}

