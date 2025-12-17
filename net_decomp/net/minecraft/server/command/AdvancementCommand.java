/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AdvancementCommand {
    private static final DynamicCommandExceptionType GENERIC_EXCEPTION = new DynamicCommandExceptionType(message -> (Text)message);
    private static final Dynamic2CommandExceptionType CRITERION_NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((advancement, criterion) -> Text.stringifiedTranslatable("commands.advancement.criterionNotFound", advancement, criterion));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("advancement").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("grant").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("only").then(((RequiredArgumentBuilder)CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.GRANT, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.ONLY)))).then(CommandManager.argument("criterion", StringArgumentType.greedyString()).suggests((context, builder) -> CommandSource.suggestMatching(RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement").value().criteria().keySet(), builder)).executes(context -> AdvancementCommand.executeCriterion((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.GRANT, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), StringArgumentType.getString((CommandContext)context, (String)"criterion"))))))).then(CommandManager.literal("from").then(CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.GRANT, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.FROM)))))).then(CommandManager.literal("until").then(CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.GRANT, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.UNTIL)))))).then(CommandManager.literal("through").then(CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.GRANT, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.THROUGH)))))).then(CommandManager.literal("everything").executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.GRANT, ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().getAdvancements(), false)))))).then(CommandManager.literal("revoke").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("only").then(((RequiredArgumentBuilder)CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.REVOKE, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.ONLY)))).then(CommandManager.argument("criterion", StringArgumentType.greedyString()).suggests((context, builder) -> CommandSource.suggestMatching(RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement").value().criteria().keySet(), builder)).executes(context -> AdvancementCommand.executeCriterion((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.REVOKE, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), StringArgumentType.getString((CommandContext)context, (String)"criterion"))))))).then(CommandManager.literal("from").then(CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.REVOKE, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.FROM)))))).then(CommandManager.literal("until").then(CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.REVOKE, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.UNTIL)))))).then(CommandManager.literal("through").then(CommandManager.argument("advancement", RegistryKeyArgumentType.registryKey(RegistryKeys.ADVANCEMENT)).executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.REVOKE, AdvancementCommand.select((CommandContext<ServerCommandSource>)context, RegistryKeyArgumentType.getAdvancementEntry((CommandContext<ServerCommandSource>)context, "advancement"), Selection.THROUGH)))))).then(CommandManager.literal("everything").executes(context -> AdvancementCommand.executeAdvancement((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Operation.REVOKE, ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().getAdvancements()))))));
    }

    private static int executeAdvancement(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Operation operation, Collection<AdvancementEntry> selection) throws CommandSyntaxException {
        return AdvancementCommand.executeAdvancement(source, targets, operation, selection, true);
    }

    private static int executeAdvancement(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Operation operation, Collection<AdvancementEntry> selection, boolean skipSync) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            i += operation.processAll(serverPlayerEntity, selection, skipSync);
        }
        if (i == 0) {
            if (selection.size() == 1) {
                if (targets.size() == 1) {
                    throw GENERIC_EXCEPTION.create((Object)Text.translatable(operation.getCommandPrefix() + ".one.to.one.failure", Advancement.getNameFromIdentity(selection.iterator().next()), targets.iterator().next().getDisplayName()));
                }
                throw GENERIC_EXCEPTION.create((Object)Text.translatable(operation.getCommandPrefix() + ".one.to.many.failure", Advancement.getNameFromIdentity(selection.iterator().next()), targets.size()));
            }
            if (targets.size() == 1) {
                throw GENERIC_EXCEPTION.create((Object)Text.translatable(operation.getCommandPrefix() + ".many.to.one.failure", selection.size(), targets.iterator().next().getDisplayName()));
            }
            throw GENERIC_EXCEPTION.create((Object)Text.translatable(operation.getCommandPrefix() + ".many.to.many.failure", selection.size(), targets.size()));
        }
        if (selection.size() == 1) {
            if (targets.size() == 1) {
                source.sendFeedback(() -> Text.translatable(operation.getCommandPrefix() + ".one.to.one.success", Advancement.getNameFromIdentity((AdvancementEntry)selection.iterator().next()), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
            } else {
                source.sendFeedback(() -> Text.translatable(operation.getCommandPrefix() + ".one.to.many.success", Advancement.getNameFromIdentity((AdvancementEntry)selection.iterator().next()), targets.size()), true);
            }
        } else if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable(operation.getCommandPrefix() + ".many.to.one.success", selection.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(operation.getCommandPrefix() + ".many.to.many.success", selection.size(), targets.size()), true);
        }
        return i;
    }

    private static int executeCriterion(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Operation operation, AdvancementEntry advancement, String criterion) throws CommandSyntaxException {
        int i = 0;
        Advancement advancement2 = advancement.value();
        if (!advancement2.criteria().containsKey(criterion)) {
            throw CRITERION_NOT_FOUND_EXCEPTION.create((Object)Advancement.getNameFromIdentity(advancement), (Object)criterion);
        }
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            if (!operation.processEachCriterion(serverPlayerEntity, advancement, criterion)) continue;
            ++i;
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GENERIC_EXCEPTION.create((Object)Text.translatable(operation.getCommandPrefix() + ".criterion.to.one.failure", criterion, Advancement.getNameFromIdentity(advancement), targets.iterator().next().getDisplayName()));
            }
            throw GENERIC_EXCEPTION.create((Object)Text.translatable(operation.getCommandPrefix() + ".criterion.to.many.failure", criterion, Advancement.getNameFromIdentity(advancement), targets.size()));
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable(operation.getCommandPrefix() + ".criterion.to.one.success", criterion, Advancement.getNameFromIdentity(advancement), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable(operation.getCommandPrefix() + ".criterion.to.many.success", criterion, Advancement.getNameFromIdentity(advancement), targets.size()), true);
        }
        return i;
    }

    private static List<AdvancementEntry> select(CommandContext<ServerCommandSource> context, AdvancementEntry advancement, Selection selection) {
        AdvancementManager advancementManager = ((ServerCommandSource)context.getSource()).getServer().getAdvancementLoader().getManager();
        PlacedAdvancement placedAdvancement = advancementManager.get(advancement);
        if (placedAdvancement == null) {
            return List.of(advancement);
        }
        ArrayList<AdvancementEntry> list = new ArrayList<AdvancementEntry>();
        if (selection.before) {
            for (PlacedAdvancement placedAdvancement2 = placedAdvancement.getParent(); placedAdvancement2 != null; placedAdvancement2 = placedAdvancement2.getParent()) {
                list.add(placedAdvancement2.getAdvancementEntry());
            }
        }
        list.add(advancement);
        if (selection.after) {
            AdvancementCommand.addChildrenRecursivelyToList(placedAdvancement, list);
        }
        return list;
    }

    private static void addChildrenRecursivelyToList(PlacedAdvancement parent, List<AdvancementEntry> childList) {
        for (PlacedAdvancement placedAdvancement : parent.getChildren()) {
            childList.add(placedAdvancement.getAdvancementEntry());
            AdvancementCommand.addChildrenRecursivelyToList(placedAdvancement, childList);
        }
    }

    static abstract sealed class Operation
    extends Enum<Operation> {
        public static final /* enum */ Operation GRANT = new Operation("grant"){

            @Override
            protected boolean processEach(ServerPlayerEntity player, AdvancementEntry advancement) {
                AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
                if (advancementProgress.isDone()) {
                    return false;
                }
                for (String string : advancementProgress.getUnobtainedCriteria()) {
                    player.getAdvancementTracker().grantCriterion(advancement, string);
                }
                return true;
            }

            @Override
            protected boolean processEachCriterion(ServerPlayerEntity player, AdvancementEntry advancement, String criterion) {
                return player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        };
        public static final /* enum */ Operation REVOKE = new Operation("revoke"){

            @Override
            protected boolean processEach(ServerPlayerEntity player, AdvancementEntry advancement) {
                AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
                if (!advancementProgress.isAnyObtained()) {
                    return false;
                }
                for (String string : advancementProgress.getObtainedCriteria()) {
                    player.getAdvancementTracker().revokeCriterion(advancement, string);
                }
                return true;
            }

            @Override
            protected boolean processEachCriterion(ServerPlayerEntity player, AdvancementEntry advancement, String criterion) {
                return player.getAdvancementTracker().revokeCriterion(advancement, criterion);
            }
        };
        private final String commandPrefix;
        private static final /* synthetic */ Operation[] field_13455;

        public static Operation[] values() {
            return (Operation[])field_13455.clone();
        }

        public static Operation valueOf(String string) {
            return Enum.valueOf(Operation.class, string);
        }

        Operation(String name) {
            this.commandPrefix = "commands.advancement." + name;
        }

        public int processAll(ServerPlayerEntity player, Iterable<AdvancementEntry> advancements, boolean skipSync) {
            int i = 0;
            if (!skipSync) {
                player.getAdvancementTracker().sendUpdate(player, true);
            }
            for (AdvancementEntry advancementEntry : advancements) {
                if (!this.processEach(player, advancementEntry)) continue;
                ++i;
            }
            if (!skipSync) {
                player.getAdvancementTracker().sendUpdate(player, false);
            }
            return i;
        }

        protected abstract boolean processEach(ServerPlayerEntity var1, AdvancementEntry var2);

        protected abstract boolean processEachCriterion(ServerPlayerEntity var1, AdvancementEntry var2, String var3);

        protected String getCommandPrefix() {
            return this.commandPrefix;
        }

        private static /* synthetic */ Operation[] method_36964() {
            return new Operation[]{GRANT, REVOKE};
        }

        static {
            field_13455 = Operation.method_36964();
        }
    }

    static final class Selection
    extends Enum<Selection> {
        public static final /* enum */ Selection ONLY = new Selection(false, false);
        public static final /* enum */ Selection THROUGH = new Selection(true, true);
        public static final /* enum */ Selection FROM = new Selection(false, true);
        public static final /* enum */ Selection UNTIL = new Selection(true, false);
        public static final /* enum */ Selection EVERYTHING = new Selection(true, true);
        final boolean before;
        final boolean after;
        private static final /* synthetic */ Selection[] field_13463;

        public static Selection[] values() {
            return (Selection[])field_13463.clone();
        }

        public static Selection valueOf(String string) {
            return Enum.valueOf(Selection.class, string);
        }

        private Selection(boolean before, boolean after) {
            this.before = before;
            this.after = after;
        }

        private static /* synthetic */ Selection[] method_36965() {
            return new Selection[]{ONLY, THROUGH, FROM, UNTIL, EVERYTHING};
        }

        static {
            field_13463 = Selection.method_36965();
        }
    }
}

