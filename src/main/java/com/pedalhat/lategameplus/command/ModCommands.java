package com.pedalhat.lategameplus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.registry.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess access,
                                         CommandManager.RegistrationEnvironment env) {

        dispatcher.register(CommandManager.literal("lategameplus")
            .requires(src -> src.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS))
            .then(CommandManager.literal("reload")
                .executes(ctx -> {
                    var server = ctx.getSource().getServer();
                    var src = ctx.getSource();
                    ConfigManager.load();
                    var opSource = server.getCommandSource()
                        .withPermissions(LeveledPermissionPredicate.OWNERS);
                    server.getCommandManager()
                        .getDispatcher()
                        .execute("reload", opSource);

                    ctx.getSource().sendFeedback(
                        () -> Text.literal("LGP: config reloaded and datapacks /reload executed."),
                        true
                    );
                    int patchedElytras = patchElytras(src.getServer());

                    src.sendFeedback(
                        () -> Text.literal("LGP: Elytra config reloaded. Elytras patched: "
                            + patchedElytras),
                        true
                    );
                    return 1;
                })
            )
        );
    }

    private static ComponentMap attrsFromLevel(int lvl) {
        int c = Math.max(0, Math.min(4, lvl));
        return switch (c) {
            case 0 -> ComponentMap.EMPTY;
            case 1 -> Items.GOLDEN_CHESTPLATE.getComponents();
            case 2 -> Items.IRON_CHESTPLATE.getComponents();
            case 3 -> Items.DIAMOND_CHESTPLATE.getComponents();
            case 4 -> Items.NETHERITE_CHESTPLATE.getComponents();
            default -> Items.IRON_CHESTPLATE.getComponents();
        };
    }

    private static int patchElytras(MinecraftServer server) {
        int level = ConfigManager.get().netheriteElytraProtectionLevel;
        AttributeModifiersComponent attrs =
            attrsFromLevel(level).get(DataComponentTypes.ATTRIBUTE_MODIFIERS);

        int patched = 0;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            PlayerInventory inv = p.getInventory();

            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (maybePatch(stack, attrs)) patched++;
            }
        }
        return patched;
    }

    private static boolean maybePatch(ItemStack stack, AttributeModifiersComponent attrMods) {
        if (stack.isEmpty() || !stack.isOf(ModItems.NETHERITE_ELYTRA)) return false;

        if (attrMods != null) {
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrMods);
        } else {
            stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        }
        return true;
    }
}
