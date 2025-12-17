/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandExceptionType
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.function.Tracer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.thread.FutureQueue;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class ServerCommandSource
implements AbstractServerCommandSource<ServerCommandSource>,
CommandSource {
    public static final SimpleCommandExceptionType REQUIRES_PLAYER_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("permissions.requires.player"));
    public static final SimpleCommandExceptionType REQUIRES_ENTITY_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("permissions.requires.entity"));
    private final CommandOutput output;
    private final Vec3d position;
    private final ServerWorld world;
    private final PermissionPredicate permissions;
    private final String name;
    private final Text displayName;
    private final MinecraftServer server;
    private final boolean silent;
    private final @Nullable Entity entity;
    private final ReturnValueConsumer returnValueConsumer;
    private final EntityAnchorArgumentType.EntityAnchor entityAnchor;
    private final Vec2f rotation;
    private final SignedCommandArguments signedArguments;
    private final FutureQueue messageChainTaskQueue;

    public ServerCommandSource(CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, PermissionPredicate permissions, String name, Text displayName, MinecraftServer server, @Nullable Entity entity) {
        this(output, pos, rot, world, permissions, name, displayName, server, entity, false, ReturnValueConsumer.EMPTY, EntityAnchorArgumentType.EntityAnchor.FEET, SignedCommandArguments.EMPTY, FutureQueue.immediate(server));
    }

    private ServerCommandSource(CommandOutput output, Vec3d pos, Vec2f rot, ServerWorld world, PermissionPredicate permissions, String name, Text displayName, MinecraftServer server, @Nullable Entity entity, boolean silent, ReturnValueConsumer resultStorer, EntityAnchorArgumentType.EntityAnchor entityAnchor, SignedCommandArguments signedArguments, FutureQueue messageChainTaskQueue) {
        this.output = output;
        this.position = pos;
        this.world = world;
        this.silent = silent;
        this.entity = entity;
        this.permissions = permissions;
        this.name = name;
        this.displayName = displayName;
        this.server = server;
        this.returnValueConsumer = resultStorer;
        this.entityAnchor = entityAnchor;
        this.rotation = rot;
        this.signedArguments = signedArguments;
        this.messageChainTaskQueue = messageChainTaskQueue;
    }

    public ServerCommandSource withOutput(CommandOutput output) {
        if (this.output == output) {
            return this;
        }
        return new ServerCommandSource(output, this.position, this.rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withEntity(Entity entity) {
        if (this.entity == entity) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.permissions, entity.getStringifiedName(), entity.getDisplayName(), this.server, entity, this.silent, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withPosition(Vec3d position) {
        if (this.position.equals(position)) {
            return this;
        }
        return new ServerCommandSource(this.output, position, this.rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withRotation(Vec2f rotation) {
        if (this.rotation.equals(rotation)) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    @Override
    public ServerCommandSource withReturnValueConsumer(ReturnValueConsumer returnValueConsumer) {
        if (Objects.equals(this.returnValueConsumer, returnValueConsumer)) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource mergeReturnValueConsumers(ReturnValueConsumer returnValueConsumer, BinaryOperator<ReturnValueConsumer> merger) {
        ReturnValueConsumer returnValueConsumer2 = (ReturnValueConsumer)merger.apply(this.returnValueConsumer, returnValueConsumer);
        return this.withReturnValueConsumer(returnValueConsumer2);
    }

    public ServerCommandSource withSilent() {
        if (this.silent || this.output.cannotBeSilenced()) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, true, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withPermissions(PermissionPredicate permissions) {
        if (permissions == this.permissions) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, this.rotation, this.world, permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withAdditionalPermissions(PermissionPredicate permissions) {
        return this.withPermissions(this.permissions.or(permissions));
    }

    public ServerCommandSource withEntityAnchor(EntityAnchorArgumentType.EntityAnchor anchor) {
        if (anchor == this.entityAnchor) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, anchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withWorld(ServerWorld world) {
        if (world == this.world) {
            return this;
        }
        double d = DimensionType.getCoordinateScaleFactor(this.world.getDimension(), world.getDimension());
        Vec3d vec3d = new Vec3d(this.position.x * d, this.position.y, this.position.z * d);
        return new ServerCommandSource(this.output, vec3d, this.rotation, world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, this.entityAnchor, this.signedArguments, this.messageChainTaskQueue);
    }

    public ServerCommandSource withLookingAt(Entity entity, EntityAnchorArgumentType.EntityAnchor anchor) {
        return this.withLookingAt(anchor.positionAt(entity));
    }

    public ServerCommandSource withLookingAt(Vec3d position) {
        Vec3d vec3d = this.entityAnchor.positionAt(this);
        double d = position.x - vec3d.x;
        double e = position.y - vec3d.y;
        double f = position.z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        float h = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(e, g) * 57.2957763671875)));
        float i = MathHelper.wrapDegrees((float)(MathHelper.atan2(f, d) * 57.2957763671875) - 90.0f);
        return this.withRotation(new Vec2f(h, i));
    }

    public ServerCommandSource withSignedArguments(SignedCommandArguments signedArguments, FutureQueue messageChainTaskQueue) {
        if (signedArguments == this.signedArguments && messageChainTaskQueue == this.messageChainTaskQueue) {
            return this;
        }
        return new ServerCommandSource(this.output, this.position, this.rotation, this.world, this.permissions, this.name, this.displayName, this.server, this.entity, this.silent, this.returnValueConsumer, this.entityAnchor, signedArguments, messageChainTaskQueue);
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public PermissionPredicate getPermissions() {
        return this.permissions;
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public @Nullable Entity getEntity() {
        return this.entity;
    }

    public Entity getEntityOrThrow() throws CommandSyntaxException {
        if (this.entity == null) {
            throw REQUIRES_ENTITY_EXCEPTION.create();
        }
        return this.entity;
    }

    public ServerPlayerEntity getPlayerOrThrow() throws CommandSyntaxException {
        Entity entity = this.entity;
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            return serverPlayerEntity;
        }
        throw REQUIRES_PLAYER_EXCEPTION.create();
    }

    public @Nullable ServerPlayerEntity getPlayer() {
        ServerPlayerEntity serverPlayerEntity;
        Entity entity = this.entity;
        return entity instanceof ServerPlayerEntity ? (serverPlayerEntity = (ServerPlayerEntity)entity) : null;
    }

    public boolean isExecutedByPlayer() {
        return this.entity instanceof ServerPlayerEntity;
    }

    public Vec2f getRotation() {
        return this.rotation;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public EntityAnchorArgumentType.EntityAnchor getEntityAnchor() {
        return this.entityAnchor;
    }

    public SignedCommandArguments getSignedArguments() {
        return this.signedArguments;
    }

    public FutureQueue getMessageChainTaskQueue() {
        return this.messageChainTaskQueue;
    }

    public boolean shouldFilterText(ServerPlayerEntity recipient) {
        ServerPlayerEntity serverPlayerEntity = this.getPlayer();
        if (recipient == serverPlayerEntity) {
            return false;
        }
        return serverPlayerEntity != null && serverPlayerEntity.shouldFilterText() || recipient.shouldFilterText();
    }

    public void sendChatMessage(SentMessage message, boolean filterMaskEnabled, MessageType.Parameters params) {
        if (this.silent) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = this.getPlayer();
        if (serverPlayerEntity != null) {
            serverPlayerEntity.sendChatMessage(message, filterMaskEnabled, params);
        } else {
            this.output.sendMessage(params.applyChatDecoration(message.content()));
        }
    }

    public void sendMessage(Text message) {
        if (this.silent) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = this.getPlayer();
        if (serverPlayerEntity != null) {
            serverPlayerEntity.sendMessage(message);
        } else {
            this.output.sendMessage(message);
        }
    }

    public void sendFeedback(Supplier<Text> feedbackSupplier, boolean broadcastToOps) {
        boolean bl2;
        boolean bl = this.output.shouldReceiveFeedback() && !this.silent;
        boolean bl3 = bl2 = broadcastToOps && this.output.shouldBroadcastConsoleToOps() && !this.silent;
        if (!bl && !bl2) {
            return;
        }
        Text text = feedbackSupplier.get();
        if (bl) {
            this.output.sendMessage(text);
        }
        if (bl2) {
            this.sendToOps(text);
        }
    }

    private void sendToOps(Text message) {
        MutableText text = Text.translatable("chat.type.admin", this.getDisplayName(), message).formatted(Formatting.GRAY, Formatting.ITALIC);
        GameRules gameRules = this.world.getGameRules();
        if (gameRules.getValue(GameRules.SEND_COMMAND_FEEDBACK).booleanValue()) {
            for (ServerPlayerEntity serverPlayerEntity : this.server.getPlayerManager().getPlayerList()) {
                if (serverPlayerEntity.getCommandOutput() == this.output || !this.server.getPlayerManager().isOperator(serverPlayerEntity.getPlayerConfigEntry())) continue;
                serverPlayerEntity.sendMessage(text);
            }
        }
        if (this.output != this.server && gameRules.getValue(GameRules.LOG_ADMIN_COMMANDS).booleanValue()) {
            this.server.sendMessage(text);
        }
    }

    public void sendError(Text message) {
        if (this.output.shouldTrackOutput() && !this.silent) {
            this.output.sendMessage(Text.empty().append(message).formatted(Formatting.RED));
        }
    }

    @Override
    public ReturnValueConsumer getReturnValueConsumer() {
        return this.returnValueConsumer;
    }

    @Override
    public Collection<String> getPlayerNames() {
        return Lists.newArrayList((Object[])this.server.getPlayerNames());
    }

    @Override
    public Collection<String> getTeamNames() {
        return this.server.getScoreboard().getTeamNames();
    }

    @Override
    public Stream<Identifier> getSoundIds() {
        return Registries.SOUND_EVENT.stream().map(SoundEvent::id);
    }

    @Override
    public CompletableFuture<Suggestions> getCompletions(CommandContext<?> context) {
        return Suggestions.empty();
    }

    @Override
    public CompletableFuture<Suggestions> listIdSuggestions(RegistryKey<? extends Registry<?>> registryRef, CommandSource.SuggestedIdType suggestedIdType, SuggestionsBuilder builder, CommandContext<?> context) {
        if (registryRef == RegistryKeys.RECIPE) {
            return CommandSource.suggestIdentifiers(this.server.getRecipeManager().values().stream().map(recipe -> recipe.id().getValue()), builder);
        }
        if (registryRef == RegistryKeys.ADVANCEMENT) {
            Collection<AdvancementEntry> collection = this.server.getAdvancementLoader().getAdvancements();
            return CommandSource.suggestIdentifiers(collection.stream().map(AdvancementEntry::id), builder);
        }
        return this.getRegistry(registryRef).map(registry -> {
            this.suggestIdentifiers((RegistryWrapper<?>)registry, suggestedIdType, builder);
            return builder.buildFuture();
        }).orElseGet(Suggestions::empty);
    }

    private Optional<? extends RegistryWrapper<?>> getRegistry(RegistryKey<? extends Registry<?>> registryRef) {
        Optional optional = this.getRegistryManager().getOptional(registryRef);
        if (optional.isPresent()) {
            return optional;
        }
        return this.server.getReloadableRegistries().createRegistryLookup().getOptional(registryRef);
    }

    @Override
    public Set<RegistryKey<World>> getWorldKeys() {
        return this.server.getWorldRegistryKeys();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        return this.server.getRegistryManager();
    }

    @Override
    public FeatureSet getEnabledFeatures() {
        return this.world.getEnabledFeatures();
    }

    @Override
    public CommandDispatcher<ServerCommandSource> getDispatcher() {
        return this.getServer().getCommandFunctionManager().getDispatcher();
    }

    @Override
    public void handleException(CommandExceptionType type, Message message, boolean silent, @Nullable Tracer tracer) {
        if (tracer != null) {
            tracer.traceError(message.getString());
        }
        if (!silent) {
            this.sendError(Texts.toText(message));
        }
    }

    @Override
    public boolean isSilent() {
        return this.silent;
    }

    @Override
    public /* synthetic */ AbstractServerCommandSource withReturnValueConsumer(ReturnValueConsumer returnValueConsumer) {
        return this.withReturnValueConsumer(returnValueConsumer);
    }
}

