/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.playsound.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder requiredArgumentBuilder = (RequiredArgumentBuilder)CommandManager.argument("sound", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), PlaySoundCommand.toList(((ServerCommandSource)context.getSource()).getPlayer()), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), SoundCategory.MASTER, ((ServerCommandSource)context.getSource()).getPosition(), 1.0f, 1.0f, 0.0f));
        for (SoundCategory soundCategory : SoundCategory.values()) {
            requiredArgumentBuilder.then(PlaySoundCommand.makeArgumentsForCategory(soundCategory));
        }
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("playsound").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then((ArgumentBuilder)requiredArgumentBuilder));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> makeArgumentsForCategory(SoundCategory category) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(category.getName()).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), PlaySoundCommand.toList(((ServerCommandSource)context.getSource()).getPlayer()), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), category, Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("volume", FloatArgumentType.floatArg((float)0.0f)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), category, Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), ((Float)context.getArgument("volume", Float.class)).floatValue(), 1.0f, 0.0f))).then(((RequiredArgumentBuilder)CommandManager.argument("pitch", FloatArgumentType.floatArg((float)0.0f, (float)2.0f)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), category, Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), ((Float)context.getArgument("volume", Float.class)).floatValue(), ((Float)context.getArgument("pitch", Float.class)).floatValue(), 0.0f))).then(CommandManager.argument("minVolume", FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(context -> PlaySoundCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "sound"), category, Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), ((Float)context.getArgument("volume", Float.class)).floatValue(), ((Float)context.getArgument("pitch", Float.class)).floatValue(), ((Float)context.getArgument("minVolume", Float.class)).floatValue())))))));
    }

    private static Collection<ServerPlayerEntity> toList(@Nullable ServerPlayerEntity player) {
        return player != null ? List.of(player) : List.of();
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {
        RegistryEntry<SoundEvent> registryEntry = RegistryEntry.of(SoundEvent.of(sound));
        double d = MathHelper.square(registryEntry.value().getDistanceToTravel(volume));
        ServerWorld serverWorld = source.getWorld();
        long l = serverWorld.getRandom().nextLong();
        ArrayList<ServerPlayerEntity> list = new ArrayList<ServerPlayerEntity>();
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            if (serverPlayerEntity.getEntityWorld() != serverWorld) continue;
            double e = pos.x - serverPlayerEntity.getX();
            double f = pos.y - serverPlayerEntity.getY();
            double g = pos.z - serverPlayerEntity.getZ();
            double h = e * e + f * f + g * g;
            Vec3d vec3d = pos;
            float i = volume;
            if (h > d) {
                if (minVolume <= 0.0f) continue;
                double j = Math.sqrt(h);
                vec3d = new Vec3d(serverPlayerEntity.getX() + e / j * 2.0, serverPlayerEntity.getY() + f / j * 2.0, serverPlayerEntity.getZ() + g / j * 2.0);
                i = minVolume;
            }
            serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(registryEntry, category, vec3d.getX(), vec3d.getY(), vec3d.getZ(), i, pitch, l));
            list.add(serverPlayerEntity);
        }
        int k = list.size();
        if (k == 0) {
            throw FAILED_EXCEPTION.create();
        }
        if (k == 1) {
            source.sendFeedback(() -> Text.translatable("commands.playsound.success.single", Text.of(sound), ((ServerPlayerEntity)list.getFirst()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.playsound.success.multiple", Text.of(sound), k), true);
        }
        return k;
    }
}

