/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.inventory.StackReferenceGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ItemCommand {
    static final Dynamic3CommandExceptionType NOT_A_CONTAINER_TARGET_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> Text.stringifiedTranslatable("commands.item.target.not_a_container", x, y, z));
    static final Dynamic3CommandExceptionType NOT_A_CONTAINER_SOURCE_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> Text.stringifiedTranslatable("commands.item.source.not_a_container", x, y, z));
    static final DynamicCommandExceptionType NO_SUCH_SLOT_TARGET_EXCEPTION = new DynamicCommandExceptionType(slot -> Text.stringifiedTranslatable("commands.item.target.no_such_slot", slot));
    private static final DynamicCommandExceptionType NO_SUCH_SLOT_SOURCE_EXCEPTION = new DynamicCommandExceptionType(slot -> Text.stringifiedTranslatable("commands.item.source.no_such_slot", slot));
    private static final DynamicCommandExceptionType NO_CHANGES_EXCEPTION = new DynamicCommandExceptionType(slot -> Text.stringifiedTranslatable("commands.item.target.no_changes", slot));
    private static final Dynamic2CommandExceptionType KNOWN_ITEM_EXCEPTION = new Dynamic2CommandExceptionType((itemName, slot) -> Text.stringifiedTranslatable("commands.item.target.no_changed.known_item", itemName, slot));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("item").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((LiteralArgumentBuilder)CommandManager.literal("replace").then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.literal("with").then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> ItemCommand.executeBlockReplace((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false)))).then(CommandManager.argument("count", IntegerArgumentType.integer((int)1, (int)99)).executes(context -> ItemCommand.executeBlockReplace((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), true))))))).then(((LiteralArgumentBuilder)CommandManager.literal("from").then(CommandManager.literal("block").then(CommandManager.argument("source", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeBlockCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).executes(context -> ItemCommand.executeBlockCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), RegistryEntryArgumentType.getLootFunction((CommandContext<ServerCommandSource>)context, "modifier")))))))).then(CommandManager.literal("entity").then(CommandManager.argument("source", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeBlockCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).executes(context -> ItemCommand.executeBlockCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), RegistryEntryArgumentType.getLootFunction((CommandContext<ServerCommandSource>)context, "modifier")))))))))))).then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.literal("with").then(((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> ItemCommand.executeEntityReplace((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false)))).then(CommandManager.argument("count", IntegerArgumentType.integer((int)1, (int)99)).executes(context -> ItemCommand.executeEntityReplace((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), ItemStackArgumentType.getItemStackArgument(context, "item").createStack(IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), true))))))).then(((LiteralArgumentBuilder)CommandManager.literal("from").then(CommandManager.literal("block").then(CommandManager.argument("source", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeEntityCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).executes(context -> ItemCommand.executeEntityCopyBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), RegistryEntryArgumentType.getLootFunction((CommandContext<ServerCommandSource>)context, "modifier")))))))).then(CommandManager.literal("entity").then(CommandManager.argument("source", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)CommandManager.argument("sourceSlot", ItemSlotArgumentType.itemSlot()).executes(context -> ItemCommand.executeEntityCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot")))).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).executes(context -> ItemCommand.executeEntityCopyEntity((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "source"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "sourceSlot"), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), RegistryEntryArgumentType.getLootFunction((CommandContext<ServerCommandSource>)context, "modifier"))))))))))))).then(((LiteralArgumentBuilder)CommandManager.literal("modify").then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).executes(context -> ItemCommand.executeBlockModify((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), RegistryEntryArgumentType.getLootFunction((CommandContext<ServerCommandSource>)context, "modifier")))))))).then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()).then(CommandManager.argument("modifier", RegistryEntryArgumentType.lootFunction(commandRegistryAccess)).executes(context -> ItemCommand.executeEntityModify((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), ItemSlotArgumentType.getItemSlot((CommandContext<ServerCommandSource>)context, "slot"), RegistryEntryArgumentType.getLootFunction((CommandContext<ServerCommandSource>)context, "modifier")))))))));
    }

    private static int executeBlockModify(ServerCommandSource source, BlockPos pos, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        Inventory inventory = ItemCommand.getInventoryAtPos(source, pos, NOT_A_CONTAINER_TARGET_EXCEPTION);
        if (slot < 0 || slot >= inventory.size()) {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create((Object)slot);
        }
        ItemStack itemStack = ItemCommand.getStackWithModifier(source, lootFunction, inventory.getStack(slot));
        inventory.setStack(slot, itemStack);
        source.sendFeedback(() -> Text.translatable("commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), itemStack.toHoverableText()), true);
        return 1;
    }

    private static int executeEntityModify(ServerCommandSource source, Collection<? extends Entity> targets, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        HashMap map = Maps.newHashMapWithExpectedSize((int)targets.size());
        for (Entity entity : targets) {
            ItemStack itemStack;
            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference == null || !stackReference.set(itemStack = ItemCommand.getStackWithModifier(source, lootFunction, stackReference.get().copy()))) continue;
            map.put(entity, itemStack);
            if (!(entity instanceof ServerPlayerEntity)) continue;
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            serverPlayerEntity.currentScreenHandler.sendContentUpdates();
        }
        if (map.isEmpty()) {
            throw NO_CHANGES_EXCEPTION.create((Object)slot);
        }
        if (map.size() == 1) {
            Map.Entry entry = map.entrySet().iterator().next();
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.single", ((Entity)entry.getKey()).getDisplayName(), ((ItemStack)entry.getValue()).toHoverableText()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.multiple", map.size()), true);
        }
        return map.size();
    }

    private static int executeBlockReplace(ServerCommandSource source, BlockPos pos, int slot, ItemStack stack) throws CommandSyntaxException {
        Inventory inventory = ItemCommand.getInventoryAtPos(source, pos, NOT_A_CONTAINER_TARGET_EXCEPTION);
        if (slot < 0 || slot >= inventory.size()) {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create((Object)slot);
        }
        inventory.setStack(slot, stack);
        source.sendFeedback(() -> Text.translatable("commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), stack.toHoverableText()), true);
        return 1;
    }

    static Inventory getInventoryAtPos(ServerCommandSource source, BlockPos pos, Dynamic3CommandExceptionType exception) throws CommandSyntaxException {
        BlockEntity blockEntity = source.getWorld().getBlockEntity(pos);
        if (blockEntity instanceof Inventory) {
            Inventory inventory = (Inventory)((Object)blockEntity);
            return inventory;
        }
        throw exception.create((Object)pos.getX(), (Object)pos.getY(), (Object)pos.getZ());
    }

    private static int executeEntityReplace(ServerCommandSource source, Collection<? extends Entity> targets, int slot, ItemStack stack) throws CommandSyntaxException {
        ArrayList list = Lists.newArrayListWithCapacity((int)targets.size());
        for (Entity entity : targets) {
            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference == null || !stackReference.set(stack.copy())) continue;
            list.add(entity);
            if (!(entity instanceof ServerPlayerEntity)) continue;
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            serverPlayerEntity.currentScreenHandler.sendContentUpdates();
        }
        if (list.isEmpty()) {
            throw KNOWN_ITEM_EXCEPTION.create((Object)stack.toHoverableText(), (Object)slot);
        }
        if (list.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.single", ((Entity)list.getFirst()).getDisplayName(), stack.toHoverableText()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.item.entity.set.success.multiple", list.size(), stack.toHoverableText()), true);
        }
        return list.size();
    }

    private static int executeEntityCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, Collection<? extends Entity> targets, int slot) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot));
    }

    private static int executeEntityCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, Collection<? extends Entity> targets, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot)));
    }

    private static int executeBlockCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot));
    }

    private static int executeBlockCopyBlock(ServerCommandSource source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlotFromInventoryAt(source, sourcePos, sourceSlot)));
    }

    private static int executeBlockCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackInSlot(sourceEntity, sourceSlot));
    }

    private static int executeBlockCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeBlockReplace(source, pos, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlot(sourceEntity, sourceSlot)));
    }

    private static int executeEntityCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, Collection<? extends Entity> targets, int slot) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackInSlot(sourceEntity, sourceSlot));
    }

    private static int executeEntityCopyEntity(ServerCommandSource source, Entity sourceEntity, int sourceSlot, Collection<? extends Entity> targets, int slot, RegistryEntry<LootFunction> lootFunction) throws CommandSyntaxException {
        return ItemCommand.executeEntityReplace(source, targets, slot, ItemCommand.getStackWithModifier(source, lootFunction, ItemCommand.getStackInSlot(sourceEntity, sourceSlot)));
    }

    private static ItemStack getStackWithModifier(ServerCommandSource source, RegistryEntry<LootFunction> lootFunction, ItemStack stack) {
        ServerWorld serverWorld = source.getWorld();
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverWorld).add(LootContextParameters.ORIGIN, source.getPosition()).addOptional(LootContextParameters.THIS_ENTITY, source.getEntity()).build(LootContextTypes.COMMAND);
        LootContext lootContext = new LootContext.Builder(lootWorldContext).build(Optional.empty());
        lootContext.markActive(LootContext.itemModifier(lootFunction.value()));
        ItemStack itemStack = (ItemStack)lootFunction.value().apply(stack, lootContext);
        itemStack.capCount(itemStack.getMaxCount());
        return itemStack;
    }

    private static ItemStack getStackInSlot(StackReferenceGetter stackReferenceGetter, int slotId) throws CommandSyntaxException {
        StackReference stackReference = stackReferenceGetter.getStackReference(slotId);
        if (stackReference == null) {
            throw NO_SUCH_SLOT_SOURCE_EXCEPTION.create((Object)slotId);
        }
        return stackReference.get().copy();
    }

    private static ItemStack getStackInSlotFromInventoryAt(ServerCommandSource source, BlockPos pos, int slotId) throws CommandSyntaxException {
        Inventory inventory = ItemCommand.getInventoryAtPos(source, pos, NOT_A_CONTAINER_SOURCE_EXCEPTION);
        return ItemCommand.getStackInSlot(inventory, slotId);
    }
}

