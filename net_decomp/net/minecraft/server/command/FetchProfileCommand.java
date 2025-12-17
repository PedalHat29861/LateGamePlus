/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.server.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.GameProfileResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.text.object.PlayerTextObjectContents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class FetchProfileCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fetchprofile").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("name").then(CommandManager.argument("name", StringArgumentType.greedyString()).executes(context -> FetchProfileCommand.executeName((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"name")))))).then(CommandManager.literal("id").then(CommandManager.argument("id", UuidArgumentType.uuid()).executes(context -> FetchProfileCommand.executeId((ServerCommandSource)context.getSource(), UuidArgumentType.getUuid((CommandContext<ServerCommandSource>)context, "id"))))));
    }

    private static void sendResult(ServerCommandSource source, GameProfile profile, String successText, Text inputText) {
        ProfileComponent profileComponent2 = ProfileComponent.ofStatic(profile);
        ProfileComponent.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)profileComponent2).ifSuccess(profileComponent -> {
            String string2 = profileComponent.toString();
            MutableText mutableText = Text.object(new PlayerTextObjectContents(profileComponent2, true));
            TextCodecs.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)mutableText).ifSuccess(headText -> {
                String string3 = headText.toString();
                source.sendFeedback(() -> {
                    MutableText text2 = Texts.join(List.of(Text.translatable("commands.fetchprofile.copy_component").styled(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(string2))), Text.translatable("commands.fetchprofile.give_item").styled(style -> style.withClickEvent(new ClickEvent.RunCommand("give @s minecraft:player_head[profile=" + string2 + "]"))), Text.translatable("commands.fetchprofile.summon_mannequin").styled(style -> style.withClickEvent(new ClickEvent.RunCommand("summon minecraft:mannequin ~ ~ ~ {profile:" + string2 + "}"))), Text.translatable("commands.fetchprofile.copy_text", mutableText.formatted(Formatting.WHITE)).styled(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(string3)))), ScreenTexts.SPACE, style -> Texts.bracketed(style.formatted(Formatting.GREEN)));
                    return Text.translatable(successText, inputText, text2);
                }, false);
            }).ifError(error -> source.sendError(Text.translatable("commands.fetchprofile.failed_to_serialize", error.message())));
        }).ifError(error -> source.sendError(Text.translatable("commands.fetchprofile.failed_to_serialize", error.message())));
    }

    private static int executeName(ServerCommandSource source, String name) {
        MinecraftServer minecraftServer = source.getServer();
        GameProfileResolver gameProfileResolver = minecraftServer.getApiServices().profileResolver();
        Util.getDownloadWorkerExecutor().execute(() -> {
            MutableText text = Text.literal(name);
            Optional<GameProfile> optional = gameProfileResolver.getProfileByName(name);
            minecraftServer.execute(() -> optional.ifPresentOrElse(profile -> FetchProfileCommand.sendResult(source, profile, "commands.fetchprofile.name.success", text), () -> source.sendError(Text.translatable("commands.fetchprofile.name.failure", text))));
        });
        return 1;
    }

    private static int executeId(ServerCommandSource source, UUID id) {
        MinecraftServer minecraftServer = source.getServer();
        GameProfileResolver gameProfileResolver = minecraftServer.getApiServices().profileResolver();
        Util.getDownloadWorkerExecutor().execute(() -> {
            Text text = Text.of(id);
            Optional<GameProfile> optional = gameProfileResolver.getProfileById(id);
            minecraftServer.execute(() -> optional.ifPresentOrElse(profile -> FetchProfileCommand.sendResult(source, profile, "commands.fetchprofile.id.success", text), () -> source.sendError(Text.translatable("commands.fetchprofile.id.failure", text))));
        });
        return 1;
    }
}

