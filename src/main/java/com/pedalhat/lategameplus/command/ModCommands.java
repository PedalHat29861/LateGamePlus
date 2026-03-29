package com.pedalhat.lategameplus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.registry.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                         CommandBuildContext access,
                                         Commands.CommandSelection env) {

        dispatcher.register(Commands.literal("lategameplus")
            .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    var server = ctx.getSource().getServer();
                    var src = ctx.getSource();
                    ConfigManager.load();
                    var opSource = server.createCommandSourceStack()
                        .withPermission(LevelBasedPermissionSet.OWNER);
                    server.getCommands()
                        .getDispatcher()
                        .execute("reload", opSource);

                    ctx.getSource().sendSuccess(
                        () -> Component.literal("LGP: config reloaded and datapacks /reload executed."),
                        true
                    );
                    int patchedElytras = patchElytras(src.getServer());

                    src.sendSuccess(
                        () -> Component.literal("LGP: Elytra config reloaded. Elytras patched: "
                            + patchedElytras),
                        true
                    );
                    return 1;
                })
            )
        );
    }

    private static int patchElytras(MinecraftServer server) {
        int level = ConfigManager.get().netheriteElytraProtectionLevel;
        ItemAttributeModifiers attrs = ModItems.getChestplateAttributesForLevel(level);

        int patched = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            Inventory inv = p.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (maybePatch(stack, attrs)) patched++;
            }
        }
        return patched;
    }

    private static boolean maybePatch(ItemStack stack, ItemAttributeModifiers attrMods) {
        if (stack.isEmpty() || !stack.is(ModItems.NETHERITE_ELYTRA)) return false;

        if (attrMods != null) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrMods);
        } else {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        }
        return true;
    }
}
