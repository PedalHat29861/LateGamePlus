/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.primitives.Floats
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.entity.TestBlockEntity;
import net.minecraft.block.entity.TestInstanceBlockEntity;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.SignedArgumentList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PiercingWeaponComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.message.AcknowledgmentValidator;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageChainTaskQueue;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedCommandArguments;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.BundleItemSelectedC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChangeGameModeC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandSignedC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.DebugSubscriptionRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.play.PickItemFromBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PickItemFromEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerSessionC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SetTestBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.TestInstanceBlockActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.TestInstanceBlockStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.GameModeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.test.TestInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Cooldown;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayNetworkHandler
extends ServerCommonNetworkHandler
implements PlayStateFactories.PacketCodecModifierContext,
ServerPlayPacketListener,
PlayerAssociatedNetworkHandler,
TickablePacketListener {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_SEQUENCE = -1;
    private static final int MAX_PENDING_ACKNOWLEDGMENTS = 4096;
    private static final int field_49027 = 80;
    private static final int field_63175 = 5;
    public static final int field_64603 = 60;
    private static final Text CHAT_VALIDATION_FAILED_TEXT = Text.translatable("multiplayer.disconnect.chat_validation_failed");
    private static final Text INVALID_COMMAND_SIGNATURE_TEXT = Text.translatable("chat.disabled.invalid_command_signature").formatted(Formatting.RED);
    private static final int field_49778 = 1000;
    public ServerPlayerEntity player;
    public final ChunkDataSender chunkDataSender;
    private int ticks;
    private int sequence = -1;
    private final Cooldown messageCooldown = new Cooldown(20, 200);
    private final Cooldown creativeItemDropCooldown = new Cooldown(20, 1480);
    private double lastTickX;
    private double lastTickY;
    private double lastTickZ;
    private double updatedX;
    private double updatedY;
    private double updatedZ;
    private @Nullable Entity topmostRiddenEntity;
    private double lastTickRiddenX;
    private double lastTickRiddenY;
    private double lastTickRiddenZ;
    private double updatedRiddenX;
    private double updatedRiddenY;
    private double updatedRiddenZ;
    private @Nullable Vec3d requestedTeleportPos;
    private int requestedTeleportId;
    private int lastTeleportCheckTicks;
    private boolean floating;
    private int floatingTicks;
    private boolean vehicleFloating;
    private int vehicleFloatingTicks;
    private int movePacketsCount;
    private int lastTickMovePacketsCount;
    private boolean movedThisTick;
    private @Nullable PublicPlayerSession session;
    private MessageChain.Unpacker messageUnpacker;
    private final AcknowledgmentValidator acknowledgmentValidator = new AcknowledgmentValidator(20);
    private int globalChatMessageIndex;
    private final MessageSignatureStorage signatureStorage = MessageSignatureStorage.create();
    private final MessageChainTaskQueue messageChainTaskQueue;
    private boolean requestedReconfiguration;
    private boolean dead;
    private int remainingLoadingTicks;

    public ServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        super(server, connection, clientData);
        this.markRespawned();
        this.chunkDataSender = new ChunkDataSender(connection.isLocal());
        this.player = player;
        player.networkHandler = this;
        player.getTextStream().onConnect();
        this.messageUnpacker = MessageChain.Unpacker.unsigned(player.getUuid(), server::shouldEnforceSecureProfile);
        this.messageChainTaskQueue = new MessageChainTaskQueue(server);
    }

    @Override
    public void tick() {
        if (this.sequence > -1) {
            this.sendPacket(new PlayerActionResponseS2CPacket(this.sequence));
            this.sequence = -1;
        }
        if (!this.server.isPaused() && this.tickMovement()) {
            return;
        }
        this.baseTick();
        this.messageCooldown.tick();
        this.creativeItemDropCooldown.tick();
        if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMeasuringTimeMs() - this.player.getLastActionTime() > TimeUnit.MINUTES.toMillis(this.server.getPlayerIdleTimeout()) && !this.player.notInAnyWorld) {
            this.disconnect(Text.translatable("multiplayer.disconnect.idling"));
        }
    }

    private boolean tickMovement() {
        this.syncWithPlayerPosition();
        this.player.lastX = this.player.getX();
        this.player.lastY = this.player.getY();
        this.player.lastZ = this.player.getZ();
        this.player.playerTick();
        this.player.updatePositionAndAngles(this.lastTickX, this.lastTickY, this.lastTickZ, this.player.getYaw(), this.player.getPitch());
        ++this.ticks;
        this.lastTickMovePacketsCount = this.movePacketsCount;
        if (this.floating && !this.player.isSleeping() && !this.player.hasVehicle() && !this.player.isDead()) {
            if (++this.floatingTicks > this.getMaxAllowedFloatingTicks(this.player)) {
                LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getStringifiedName());
                this.disconnect(Text.translatable("multiplayer.disconnect.flying"));
                return true;
            }
        } else {
            this.floating = false;
            this.floatingTicks = 0;
        }
        this.topmostRiddenEntity = this.player.getRootVehicle();
        if (this.topmostRiddenEntity == this.player || this.topmostRiddenEntity.getControllingPassenger() != this.player) {
            this.topmostRiddenEntity = null;
            this.vehicleFloating = false;
            this.vehicleFloatingTicks = 0;
        } else {
            this.lastTickRiddenX = this.topmostRiddenEntity.getX();
            this.lastTickRiddenY = this.topmostRiddenEntity.getY();
            this.lastTickRiddenZ = this.topmostRiddenEntity.getZ();
            this.updatedRiddenX = this.topmostRiddenEntity.getX();
            this.updatedRiddenY = this.topmostRiddenEntity.getY();
            this.updatedRiddenZ = this.topmostRiddenEntity.getZ();
            if (this.vehicleFloating && this.topmostRiddenEntity.getControllingPassenger() == this.player) {
                if (++this.vehicleFloatingTicks > this.getMaxAllowedFloatingTicks(this.topmostRiddenEntity)) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getStringifiedName());
                    this.disconnect(Text.translatable("multiplayer.disconnect.flying"));
                    return true;
                }
            } else {
                this.vehicleFloating = false;
                this.vehicleFloatingTicks = 0;
            }
        }
        return false;
    }

    private int getMaxAllowedFloatingTicks(Entity vehicle) {
        double d = vehicle.getFinalGravity();
        if (d < (double)1.0E-5f) {
            return Integer.MAX_VALUE;
        }
        double e = 0.08 / d;
        return MathHelper.ceil(80.0 * Math.max(e, 1.0));
    }

    public void resetFloatingTicks() {
        this.floatingTicks = 0;
        this.vehicleFloatingTicks = 0;
    }

    public void syncWithPlayerPosition() {
        this.lastTickX = this.player.getX();
        this.lastTickY = this.player.getY();
        this.lastTickZ = this.player.getZ();
        this.updatedX = this.player.getX();
        this.updatedY = this.player.getY();
        this.updatedZ = this.player.getZ();
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen() && !this.requestedReconfiguration;
    }

    @Override
    public boolean accepts(Packet<?> packet) {
        if (super.accepts(packet)) {
            return true;
        }
        return this.requestedReconfiguration && this.connection.isOpen() && packet instanceof AcknowledgeReconfigurationC2SPacket;
    }

    @Override
    protected GameProfile getProfile() {
        return this.player.getGameProfile();
    }

    private <T, R> CompletableFuture<R> filterText(T text, BiFunction<TextStream, T, CompletableFuture<R>> filterer) {
        return filterer.apply(this.player.getTextStream(), (TextStream)text).thenApply(filtered -> {
            if (!this.isConnectionOpen()) {
                LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            }
            return filtered;
        });
    }

    private CompletableFuture<FilteredMessage> filterText(String text) {
        return this.filterText(text, TextStream::filterText);
    }

    private CompletableFuture<List<FilteredMessage>> filterTexts(List<String> texts) {
        return this.filterText(texts, TextStream::filterTexts);
    }

    @Override
    public void onPlayerInput(PlayerInputC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.setPlayerInput(packet.input());
        if (this.canInteractWithGame()) {
            this.player.updateLastActionTime();
            this.player.setSneaking(packet.input().sneak());
        }
    }

    private static boolean isMovementInvalid(double x, double y, double z, float yaw, float pitch) {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || !Floats.isFinite((float)pitch) || !Floats.isFinite((float)yaw);
    }

    private static double clampHorizontal(double d) {
        return MathHelper.clamp(d, -3.0E7, 3.0E7);
    }

    private static double clampVertical(double d) {
        return MathHelper.clamp(d, -2.0E7, 2.0E7);
    }

    @Override
    public void onVehicleMove(VehicleMoveC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (ServerPlayNetworkHandler.isMovementInvalid(packet.position().getX(), packet.position().getY(), packet.position().getZ(), packet.yaw(), packet.pitch())) {
            this.disconnect(Text.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
            return;
        }
        if (this.handlePendingTeleport() || !this.canInteractWithGame()) {
            return;
        }
        Entity entity = this.player.getRootVehicle();
        if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.topmostRiddenEntity) {
            LivingEntity livingEntity;
            ServerWorld serverWorld = this.player.getEntityWorld();
            double d = entity.getX();
            double e = entity.getY();
            double f = entity.getZ();
            double g = ServerPlayNetworkHandler.clampHorizontal(packet.position().getX());
            double h = ServerPlayNetworkHandler.clampVertical(packet.position().getY());
            double i = ServerPlayNetworkHandler.clampHorizontal(packet.position().getZ());
            float j = MathHelper.wrapDegrees(packet.yaw());
            float k = MathHelper.wrapDegrees(packet.pitch());
            double l = g - this.lastTickRiddenX;
            double m = h - this.lastTickRiddenY;
            double n = i - this.lastTickRiddenZ;
            double p = l * l + m * m + n * n;
            double o = entity.getVelocity().lengthSquared();
            if (p - o > 100.0 && !this.isHost()) {
                LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{entity.getStringifiedName(), this.player.getStringifiedName(), l, m, n});
                this.sendPacket(VehicleMoveS2CPacket.fromVehicle(entity));
                return;
            }
            Box box = entity.getBoundingBox();
            l = g - this.updatedRiddenX;
            m = h - this.updatedRiddenY;
            n = i - this.updatedRiddenZ;
            boolean bl = entity.groundCollision;
            if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isClimbing()) {
                livingEntity.onLanding();
            }
            entity.move(MovementType.PLAYER, new Vec3d(l, m, n));
            double q = m;
            l = g - entity.getX();
            m = h - entity.getY();
            if (m > -0.5 || m < 0.5) {
                m = 0.0;
            }
            n = i - entity.getZ();
            p = l * l + m * m + n * n;
            boolean bl2 = false;
            if (p > 0.0625) {
                bl2 = true;
                LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", new Object[]{entity.getStringifiedName(), this.player.getStringifiedName(), Math.sqrt(p)});
            }
            if (bl2 && serverWorld.isSpaceEmpty(entity, box) || this.isEntityNotCollidingWithBlocks(serverWorld, entity, box, g, h, i)) {
                entity.updatePositionAndAngles(d, e, f, j, k);
                this.sendPacket(VehicleMoveS2CPacket.fromVehicle(entity));
                entity.popQueuedCollisionCheck();
                return;
            }
            entity.updatePositionAndAngles(g, h, i, j, k);
            this.player.getEntityWorld().getChunkManager().updatePosition(this.player);
            Vec3d vec3d = new Vec3d(entity.getX() - d, entity.getY() - e, entity.getZ() - f);
            this.handleMovement(vec3d);
            entity.setMovement(packet.onGround(), vec3d);
            entity.handleFall(vec3d.x, vec3d.y, vec3d.z, packet.onGround());
            this.player.increaseTravelMotionStats(vec3d.x, vec3d.y, vec3d.z);
            this.vehicleFloating = q >= -0.03125 && !bl && !this.server.isFlightEnabled() && !entity.isFlyingVehicle() && !entity.hasNoGravity() && this.isEntityOnAir(entity);
            this.updatedRiddenX = entity.getX();
            this.updatedRiddenY = entity.getY();
            this.updatedRiddenZ = entity.getZ();
        }
    }

    private boolean isEntityOnAir(Entity entity) {
        return entity.getEntityWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    @Override
    public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (packet.getTeleportId() == this.requestedTeleportId) {
            if (this.requestedTeleportPos == null) {
                this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }
            this.player.updatePositionAndAngles(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
            this.updatedX = this.requestedTeleportPos.x;
            this.updatedY = this.requestedTeleportPos.y;
            this.updatedZ = this.requestedTeleportPos.z;
            this.player.onTeleportationDone();
            this.requestedTeleportPos = null;
        }
    }

    @Override
    public void onPlayerLoaded(PlayerLoadedC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.markLoaded();
    }

    @Override
    public void onRecipeBookData(RecipeBookDataC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        ServerRecipeManager.ServerRecipe serverRecipe = this.server.getRecipeManager().get(packet.recipeId());
        if (serverRecipe != null) {
            this.player.getRecipeBook().unmarkHighlighted(serverRecipe.parent().id());
        }
    }

    @Override
    public void onBundleItemSelected(BundleItemSelectedC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.currentScreenHandler.selectBundleStack(packet.slotId(), packet.selectedItemIndex());
    }

    @Override
    public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.getRecipeBook().setCategoryOptions(packet.getCategory(), packet.isGuiOpen(), packet.isFilteringCraftable());
    }

    @Override
    public void onAdvancementTab(AdvancementTabC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (packet.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB) {
            Identifier identifier = Objects.requireNonNull(packet.getTabToOpen());
            AdvancementEntry advancementEntry = this.server.getAdvancementLoader().get(identifier);
            if (advancementEntry != null) {
                this.player.getAdvancementTracker().setDisplayTab(advancementEntry);
            }
        }
    }

    @Override
    public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        StringReader stringReader = new StringReader(packet.getPartialCommand());
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }
        ParseResults parseResults = this.server.getCommandManager().getDispatcher().parse(stringReader, (Object)this.player.getCommandSource());
        this.server.getCommandManager().getDispatcher().getCompletionSuggestions(parseResults).thenAccept(suggestions -> {
            Suggestions suggestions2 = suggestions.getList().size() <= 1000 ? suggestions : new Suggestions(suggestions.getRange(), suggestions.getList().subList(0, 1000));
            this.sendPacket(new CommandSuggestionsS2CPacket(packet.getCompletionId(), suggestions2));
        });
    }

    @Override
    public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            this.player.sendMessage(Text.translatable("advMode.notAllowed"));
            return;
        }
        CommandBlockExecutor commandBlockExecutor = null;
        CommandBlockBlockEntity commandBlockBlockEntity = null;
        BlockPos blockPos = packet.getPos();
        BlockEntity blockEntity = this.player.getEntityWorld().getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockBlockEntity) {
            CommandBlockBlockEntity commandBlockBlockEntity2;
            commandBlockBlockEntity = commandBlockBlockEntity2 = (CommandBlockBlockEntity)blockEntity;
            commandBlockExecutor = commandBlockBlockEntity.getCommandExecutor();
        }
        String string = packet.getCommand();
        boolean bl = packet.shouldTrackOutput();
        if (commandBlockExecutor != null) {
            CommandBlockBlockEntity.Type type = commandBlockBlockEntity.getCommandBlockType();
            BlockState blockState = this.player.getEntityWorld().getBlockState(blockPos);
            Direction direction = blockState.get(CommandBlock.FACING);
            BlockState blockState2 = switch (packet.getType()) {
                case CommandBlockBlockEntity.Type.SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                case CommandBlockBlockEntity.Type.AUTO -> Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                default -> Blocks.COMMAND_BLOCK.getDefaultState();
            };
            BlockState blockState3 = (BlockState)((BlockState)blockState2.with(CommandBlock.FACING, direction)).with(CommandBlock.CONDITIONAL, packet.isConditional());
            if (blockState3 != blockState) {
                this.player.getEntityWorld().setBlockState(blockPos, blockState3, 2);
                blockEntity.setCachedState(blockState3);
                this.player.getEntityWorld().getWorldChunk(blockPos).setBlockEntity(blockEntity);
            }
            commandBlockExecutor.setCommand(string);
            commandBlockExecutor.setTrackOutput(bl);
            if (!bl) {
                commandBlockExecutor.setLastOutput(null);
            }
            commandBlockBlockEntity.setAuto(packet.isAlwaysActive());
            if (type != packet.getType()) {
                commandBlockBlockEntity.updateCommandBlock();
            }
            if (this.player.getEntityWorld().areCommandBlocksEnabled()) {
                commandBlockExecutor.markDirty(this.player.getEntityWorld());
            }
            if (!StringHelper.isEmpty(string)) {
                this.player.sendMessage(Text.translatable(this.player.getEntityWorld().areCommandBlocksEnabled() ? "advMode.setCommand.success" : "advMode.setCommand.disabled", string));
            }
        }
    }

    @Override
    public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            this.player.sendMessage(Text.translatable("advMode.notAllowed"));
            return;
        }
        CommandBlockExecutor commandBlockExecutor = packet.getMinecartCommandExecutor(this.player.getEntityWorld());
        if (commandBlockExecutor != null) {
            boolean bl;
            String string = packet.getCommand();
            commandBlockExecutor.setCommand(string);
            commandBlockExecutor.setTrackOutput(packet.shouldTrackOutput());
            if (!packet.shouldTrackOutput()) {
                commandBlockExecutor.setLastOutput(null);
            }
            if (bl = this.player.getEntityWorld().areCommandBlocksEnabled()) {
                commandBlockExecutor.markDirty(this.player.getEntityWorld());
            }
            if (!StringHelper.isEmpty(string)) {
                this.player.sendMessage(Text.translatable(bl ? "advMode.setCommand.success" : "advMode.setCommand.disabled", string));
            }
        }
    }

    @Override
    public void onPickItemFromBlock(PickItemFromBlockC2SPacket packet) {
        boolean bl;
        ServerWorld serverWorld = this.player.getEntityWorld();
        NetworkThreadUtils.forceMainThread(packet, this, serverWorld);
        BlockPos blockPos = packet.pos();
        if (!this.player.canInteractWithBlockAt(blockPos, 1.0)) {
            return;
        }
        if (!serverWorld.isPosLoaded(blockPos)) {
            return;
        }
        BlockState blockState = serverWorld.getBlockState(blockPos);
        ItemStack itemStack = blockState.getPickStack(serverWorld, blockPos, bl = this.player.isInCreativeMode() && packet.includeData());
        if (itemStack.isEmpty()) {
            return;
        }
        if (bl) {
            ServerPlayNetworkHandler.copyBlockDataToStack(blockState, serverWorld, blockPos, itemStack);
        }
        this.onPickItem(itemStack);
    }

    private static void copyBlockDataToStack(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
        BlockEntity blockEntity;
        BlockEntity blockEntity2 = blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        if (blockEntity != null) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), LOGGER);){
                NbtWriteView nbtWriteView = NbtWriteView.create(logging, world.getRegistryManager());
                blockEntity.writeComponentlessData(nbtWriteView);
                blockEntity.removeFromCopiedStackData(nbtWriteView);
                BlockItem.setBlockEntityData(stack, blockEntity.getType(), nbtWriteView);
                stack.applyComponentsFrom(blockEntity.createComponentMap());
            }
        }
    }

    @Override
    public void onPickItemFromEntity(PickItemFromEntityC2SPacket packet) {
        ServerWorld serverWorld = this.player.getEntityWorld();
        NetworkThreadUtils.forceMainThread(packet, this, serverWorld);
        Entity entity = serverWorld.getEntityOrDragonPart(packet.id());
        if (entity == null || !this.player.canInteractWithEntity(entity, 3.0)) {
            return;
        }
        ItemStack itemStack = entity.getPickBlockStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            this.onPickItem(itemStack);
        }
    }

    private void onPickItem(ItemStack stack) {
        if (!stack.isItemEnabled(this.player.getEntityWorld().getEnabledFeatures())) {
            return;
        }
        PlayerInventory playerInventory = this.player.getInventory();
        int i = playerInventory.getSlotWithStack(stack);
        if (i != -1) {
            if (PlayerInventory.isValidHotbarIndex(i)) {
                playerInventory.setSelectedSlot(i);
            } else {
                playerInventory.swapSlotWithHotbar(i);
            }
        } else if (this.player.isInCreativeMode()) {
            playerInventory.swapStackWithHotbar(stack);
        }
        this.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.getSelectedSlot()));
        this.player.playerScreenHandler.sendContentUpdates();
    }

    @Override
    public void onRenameItem(RenameItemC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof AnvilScreenHandler) {
            AnvilScreenHandler anvilScreenHandler = (AnvilScreenHandler)screenHandler;
            if (!anvilScreenHandler.canUse(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)anvilScreenHandler);
                return;
            }
            anvilScreenHandler.setNewItemName(packet.getName());
        }
    }

    @Override
    public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof BeaconScreenHandler) {
            BeaconScreenHandler beaconScreenHandler = (BeaconScreenHandler)screenHandler;
            if (!this.player.currentScreenHandler.canUse(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
                return;
            }
            beaconScreenHandler.setEffects(packet.primary(), packet.secondary());
        }
    }

    @Override
    public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos blockPos = packet.getPos();
        BlockState blockState = this.player.getEntityWorld().getBlockState(blockPos);
        BlockEntity blockEntity = this.player.getEntityWorld().getBlockEntity(blockPos);
        if (blockEntity instanceof StructureBlockBlockEntity) {
            StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)blockEntity;
            structureBlockBlockEntity.setMode(packet.getMode());
            structureBlockBlockEntity.setTemplateName(packet.getTemplateName());
            structureBlockBlockEntity.setOffset(packet.getOffset());
            structureBlockBlockEntity.setSize(packet.getSize());
            structureBlockBlockEntity.setMirror(packet.getMirror());
            structureBlockBlockEntity.setRotation(packet.getRotation());
            structureBlockBlockEntity.setMetadata(packet.getMetadata());
            structureBlockBlockEntity.setIgnoreEntities(packet.shouldIgnoreEntities());
            structureBlockBlockEntity.setStrict(packet.isStrict());
            structureBlockBlockEntity.setShowAir(packet.shouldShowAir());
            structureBlockBlockEntity.setShowBoundingBox(packet.shouldShowBoundingBox());
            structureBlockBlockEntity.setIntegrity(packet.getIntegrity());
            structureBlockBlockEntity.setSeed(packet.getSeed());
            if (structureBlockBlockEntity.hasStructureName()) {
                String string = structureBlockBlockEntity.getTemplateName();
                if (packet.getAction() == StructureBlockBlockEntity.Action.SAVE_AREA) {
                    if (structureBlockBlockEntity.saveStructure()) {
                        this.player.sendMessage(Text.translatable("structure_block.save_success", string), false);
                    } else {
                        this.player.sendMessage(Text.translatable("structure_block.save_failure", string), false);
                    }
                } else if (packet.getAction() == StructureBlockBlockEntity.Action.LOAD_AREA) {
                    if (!structureBlockBlockEntity.isStructureAvailable()) {
                        this.player.sendMessage(Text.translatable("structure_block.load_not_found", string), false);
                    } else if (structureBlockBlockEntity.loadAndTryPlaceStructure(this.player.getEntityWorld())) {
                        this.player.sendMessage(Text.translatable("structure_block.load_success", string), false);
                    } else {
                        this.player.sendMessage(Text.translatable("structure_block.load_prepare", string), false);
                    }
                } else if (packet.getAction() == StructureBlockBlockEntity.Action.SCAN_AREA) {
                    if (structureBlockBlockEntity.detectStructureSize()) {
                        this.player.sendMessage(Text.translatable("structure_block.size_success", string), false);
                    } else {
                        this.player.sendMessage(Text.translatable("structure_block.size_failure"), false);
                    }
                }
            } else {
                this.player.sendMessage(Text.translatable("structure_block.invalid_structure_name", packet.getTemplateName()), false);
            }
            structureBlockBlockEntity.markDirty();
            this.player.getEntityWorld().updateListeners(blockPos, blockState, blockState, 3);
        }
    }

    @Override
    public void onSetTestBlock(SetTestBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos blockPos = packet.position();
        BlockState blockState = this.player.getEntityWorld().getBlockState(blockPos);
        BlockEntity blockEntity = this.player.getEntityWorld().getBlockEntity(blockPos);
        if (blockEntity instanceof TestBlockEntity) {
            TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
            testBlockEntity.setMode(packet.mode());
            testBlockEntity.setMessage(packet.message());
            testBlockEntity.markDirty();
            this.player.getEntityWorld().updateListeners(blockPos, blockState, testBlockEntity.getCachedState(), 3);
        }
    }

    @Override
    public void onTestInstanceBlockAction(TestInstanceBlockActionC2SPacket packet) {
        BlockEntity blockEntity;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        BlockPos blockPos = packet.pos();
        if (!this.player.isCreativeLevelTwoOp() || !((blockEntity = this.player.getEntityWorld().getBlockEntity(blockPos)) instanceof TestInstanceBlockEntity)) {
            return;
        }
        TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
        if (packet.action() == TestInstanceBlockActionC2SPacket.Action.QUERY || packet.action() == TestInstanceBlockActionC2SPacket.Action.INIT) {
            RegistryWrapper.Impl registry = this.player.getRegistryManager().getOrThrow(RegistryKeys.TEST_INSTANCE);
            Optional optional = packet.data().test().flatMap(((Registry)registry)::getOptional);
            Text text = optional.isPresent() ? ((TestInstance)((RegistryEntry.Reference)optional.get()).value()).getDescription() : Text.translatable("test_instance.description.no_test").formatted(Formatting.RED);
            Optional<Object> optional2 = packet.action() == TestInstanceBlockActionC2SPacket.Action.QUERY ? packet.data().test().flatMap(registryKey -> TestInstanceBlockEntity.getStructureSize(this.player.getEntityWorld(), registryKey)) : Optional.empty();
            this.connection.send(new TestInstanceBlockStatusS2CPacket(text, optional2));
        } else {
            testInstanceBlockEntity.setData(packet.data());
            if (packet.action() == TestInstanceBlockActionC2SPacket.Action.RESET) {
                testInstanceBlockEntity.reset(this.player::sendMessage);
            } else if (packet.action() == TestInstanceBlockActionC2SPacket.Action.SAVE) {
                testInstanceBlockEntity.saveStructure(this.player::sendMessage);
            } else if (packet.action() == TestInstanceBlockActionC2SPacket.Action.EXPORT) {
                testInstanceBlockEntity.export(this.player::sendMessage);
            } else if (packet.action() == TestInstanceBlockActionC2SPacket.Action.RUN) {
                testInstanceBlockEntity.start(this.player::sendMessage);
            }
            BlockState blockState = this.player.getEntityWorld().getBlockState(blockPos);
            this.player.getEntityWorld().updateListeners(blockPos, Blocks.AIR.getDefaultState(), blockState, 3);
        }
    }

    @Override
    public void onUpdateJigsaw(UpdateJigsawC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos blockPos = packet.getPos();
        BlockState blockState = this.player.getEntityWorld().getBlockState(blockPos);
        BlockEntity blockEntity = this.player.getEntityWorld().getBlockEntity(blockPos);
        if (blockEntity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
            jigsawBlockEntity.setName(packet.getName());
            jigsawBlockEntity.setTarget(packet.getTarget());
            jigsawBlockEntity.setPool(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, packet.getPool()));
            jigsawBlockEntity.setFinalState(packet.getFinalState());
            jigsawBlockEntity.setJoint(packet.getJointType());
            jigsawBlockEntity.setPlacementPriority(packet.getPlacementPriority());
            jigsawBlockEntity.setSelectionPriority(packet.getSelectionPriority());
            jigsawBlockEntity.markDirty();
            this.player.getEntityWorld().updateListeners(blockPos, blockState, blockState, 3);
        }
    }

    @Override
    public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.isCreativeLevelTwoOp()) {
            return;
        }
        BlockPos blockPos = packet.getPos();
        BlockEntity blockEntity = this.player.getEntityWorld().getBlockEntity(blockPos);
        if (blockEntity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
            jigsawBlockEntity.generate(this.player.getEntityWorld(), packet.getMaxDepth(), packet.shouldKeepJigsaws());
        }
    }

    @Override
    public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        int i = packet.getTradeId();
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof MerchantScreenHandler) {
            MerchantScreenHandler merchantScreenHandler = (MerchantScreenHandler)screenHandler;
            if (!merchantScreenHandler.canUse(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)merchantScreenHandler);
                return;
            }
            merchantScreenHandler.setRecipeIndex(i);
            merchantScreenHandler.switchTo(i);
        }
    }

    @Override
    public void onBookUpdate(BookUpdateC2SPacket packet) {
        int i = packet.slot();
        if (!PlayerInventory.isValidHotbarIndex(i) && i != 40) {
            return;
        }
        ArrayList list = Lists.newArrayList();
        Optional<String> optional = packet.title();
        optional.ifPresent(list::add);
        list.addAll(packet.pages());
        Consumer<List> consumer = optional.isPresent() ? texts -> this.addBook((FilteredMessage)texts.get(0), texts.subList(1, texts.size()), i) : texts -> this.updateBookContent((List<FilteredMessage>)texts, i);
        this.filterTexts(list).thenAcceptAsync(consumer, (Executor)this.server);
    }

    private void updateBookContent(List<FilteredMessage> pages, int slotId) {
        ItemStack itemStack = this.player.getInventory().getStack(slotId);
        if (!itemStack.contains(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
            return;
        }
        List<RawFilteredPair<String>> list = pages.stream().map(this::toRawFilteredPair).toList();
        itemStack.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, new WritableBookContentComponent(list));
    }

    private void addBook(FilteredMessage title, List<FilteredMessage> pages, int slotId) {
        ItemStack itemStack = this.player.getInventory().getStack(slotId);
        if (!itemStack.contains(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
            return;
        }
        ItemStack itemStack2 = itemStack.withItem(Items.WRITTEN_BOOK);
        itemStack2.remove(DataComponentTypes.WRITABLE_BOOK_CONTENT);
        List<RawFilteredPair<Text>> list = pages.stream().map(page -> this.toRawFilteredPair((FilteredMessage)page).map(Text::literal)).toList();
        itemStack2.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(this.toRawFilteredPair(title), this.player.getStringifiedName(), 0, list, true));
        this.player.getInventory().setStack(slotId, itemStack2);
    }

    private RawFilteredPair<String> toRawFilteredPair(FilteredMessage message) {
        if (this.player.shouldFilterText()) {
            return RawFilteredPair.of(message.getString());
        }
        return RawFilteredPair.of(message);
    }

    @Override
    public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS)) {
            return;
        }
        Entity entity = this.player.getEntityWorld().getEntityById(packet.getEntityId());
        if (entity != null) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
                NbtWriteView nbtWriteView = NbtWriteView.create(logging, entity.getRegistryManager());
                entity.writeData(nbtWriteView);
                NbtCompound nbtCompound = nbtWriteView.getNbt();
                this.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), nbtCompound));
            }
        }
    }

    @Override
    public void onSlotChangedState(SlotChangedStateC2SPacket packet) {
        CrafterScreenHandler crafterScreenHandler;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (this.player.isSpectator() || packet.screenHandlerId() != this.player.currentScreenHandler.syncId) {
            return;
        }
        Object object = this.player.currentScreenHandler;
        if (object instanceof CrafterScreenHandler && (object = (crafterScreenHandler = (CrafterScreenHandler)object).getInputInventory()) instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)object;
            crafterBlockEntity.setSlotEnabled(packet.slotId(), packet.newState());
        }
    }

    @Override
    public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS)) {
            return;
        }
        BlockEntity blockEntity = this.player.getEntityWorld().getBlockEntity(packet.getPos());
        NbtCompound nbtCompound = blockEntity != null ? blockEntity.createNbt(this.player.getRegistryManager()) : null;
        this.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), nbtCompound));
    }

    @Override
    public void onPlayerMove(PlayerMoveC2SPacket packet) {
        boolean bl2;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (ServerPlayNetworkHandler.isMovementInvalid(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYaw(0.0f), packet.getPitch(0.0f))) {
            this.disconnect(Text.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
        }
        ServerWorld serverWorld = this.player.getEntityWorld();
        if (this.player.notInAnyWorld) {
            return;
        }
        if (this.ticks == 0) {
            this.syncWithPlayerPosition();
        }
        if (!this.canInteractWithGame()) {
            return;
        }
        float f = MathHelper.wrapDegrees(packet.getYaw(this.player.getYaw()));
        float g = MathHelper.wrapDegrees(packet.getPitch(this.player.getPitch()));
        if (this.handlePendingTeleport()) {
            this.player.setAngles(f, g);
            return;
        }
        double d = ServerPlayNetworkHandler.clampHorizontal(packet.getX(this.player.getX()));
        double e = ServerPlayNetworkHandler.clampVertical(packet.getY(this.player.getY()));
        double h = ServerPlayNetworkHandler.clampHorizontal(packet.getZ(this.player.getZ()));
        if (this.player.hasVehicle()) {
            this.player.updatePositionAndAngles(this.player.getX(), this.player.getY(), this.player.getZ(), f, g);
            this.player.getEntityWorld().getChunkManager().updatePosition(this.player);
            return;
        }
        double i = this.player.getX();
        double j = this.player.getY();
        double k = this.player.getZ();
        double l = d - this.lastTickX;
        double m = e - this.lastTickY;
        double n = h - this.lastTickZ;
        double o = this.player.getVelocity().lengthSquared();
        double p = l * l + m * m + n * n;
        if (this.player.isSleeping()) {
            if (p > 1.0) {
                this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), f, g);
            }
            return;
        }
        boolean bl = this.player.isGliding();
        if (serverWorld.getTickManager().shouldTick()) {
            ++this.movePacketsCount;
            int q = this.movePacketsCount - this.lastTickMovePacketsCount;
            if (q > 5) {
                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", (Object)this.player.getStringifiedName(), (Object)q);
                q = 1;
            }
            if (this.shouldCheckMovement(bl)) {
                float r;
                float f2 = r = bl ? 300.0f : 100.0f;
                if (p - o > (double)(r * (float)q)) {
                    LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.player.getStringifiedName(), l, m, n});
                    this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYaw(), this.player.getPitch());
                    return;
                }
            }
        }
        Box box = this.player.getBoundingBox();
        l = d - this.updatedX;
        m = e - this.updatedY;
        n = h - this.updatedZ;
        boolean bl3 = bl2 = m > 0.0;
        if (this.player.isOnGround() && !packet.isOnGround() && bl2) {
            this.player.jump();
        }
        boolean bl32 = this.player.groundCollision;
        this.player.move(MovementType.PLAYER, new Vec3d(l, m, n));
        double s = m;
        l = d - this.player.getX();
        m = e - this.player.getY();
        if (m > -0.5 || m < 0.5) {
            m = 0.0;
        }
        n = h - this.player.getZ();
        p = l * l + m * m + n * n;
        boolean bl4 = false;
        if (!(this.player.isInTeleportationState() || !(p > 0.0625) || this.player.isSleeping() || this.player.isCreative() || this.player.isSpectator() || this.player.isInCurrentExplosionResetGraceTime())) {
            bl4 = true;
            LOGGER.warn("{} moved wrongly!", (Object)this.player.getStringifiedName());
        }
        if (!this.player.noClip && !this.player.isSleeping() && (bl4 && serverWorld.isSpaceEmpty(this.player, box) || this.isEntityNotCollidingWithBlocks(serverWorld, this.player, box, d, e, h))) {
            this.requestTeleport(i, j, k, f, g);
            this.player.handleFall(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k, packet.isOnGround());
            this.player.popQueuedCollisionCheck();
            return;
        }
        this.player.updatePositionAndAngles(d, e, h, f, g);
        boolean bl5 = this.player.isUsingRiptide();
        this.floating = s >= -0.03125 && !bl32 && !this.player.isSpectator() && !this.server.isFlightEnabled() && !this.player.getAbilities().allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !bl && !bl5 && this.isEntityOnAir(this.player);
        this.player.getEntityWorld().getChunkManager().updatePosition(this.player);
        Vec3d vec3d = new Vec3d(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k);
        this.player.setMovement(packet.isOnGround(), packet.horizontalCollision(), vec3d);
        this.player.handleFall(vec3d.x, vec3d.y, vec3d.z, packet.isOnGround());
        this.handleMovement(vec3d);
        if (bl2) {
            this.player.onLanding();
        }
        if (packet.isOnGround() || this.player.hasLandedInFluid() || this.player.isClimbing() || this.player.isSpectator() || bl || bl5) {
            this.player.tryClearCurrentExplosion();
        }
        this.player.increaseTravelMotionStats(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k);
        this.updatedX = this.player.getX();
        this.updatedY = this.player.getY();
        this.updatedZ = this.player.getZ();
    }

    private boolean shouldCheckMovement(boolean elytra) {
        if (this.isHost()) {
            return false;
        }
        if (this.player.isInTeleportationState()) {
            return false;
        }
        GameRules gameRules = this.player.getEntityWorld().getGameRules();
        if (!gameRules.getValue(GameRules.PLAYER_MOVEMENT_CHECK).booleanValue()) {
            return false;
        }
        return !elytra || gameRules.getValue(GameRules.ELYTRA_MOVEMENT_CHECK) != false;
    }

    private boolean handlePendingTeleport() {
        if (this.requestedTeleportPos != null) {
            if (this.ticks - this.lastTeleportCheckTicks > 20) {
                this.lastTeleportCheckTicks = this.ticks;
                this.requestTeleport(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.getYaw(), this.player.getPitch());
            }
            return true;
        }
        this.lastTeleportCheckTicks = this.ticks;
        return false;
    }

    private boolean isEntityNotCollidingWithBlocks(WorldView world, Entity entity, Box box, double newX, double newY, double newZ) {
        Box box2 = entity.getBoundingBox().offset(newX - entity.getX(), newY - entity.getY(), newZ - entity.getZ());
        Iterable<VoxelShape> iterable = world.getCollisions(entity, box2.contract(1.0E-5f), box.getHorizontalCenter());
        VoxelShape voxelShape = VoxelShapes.cuboid(box.contract(1.0E-5f));
        for (VoxelShape voxelShape2 : iterable) {
            if (VoxelShapes.matchesAnywhere(voxelShape2, voxelShape, BooleanBiFunction.AND)) continue;
            return true;
        }
        return false;
    }

    public void requestTeleport(double x, double y, double z, float yaw, float pitch) {
        this.requestTeleport(new EntityPosition(new Vec3d(x, y, z), Vec3d.ZERO, yaw, pitch), Collections.emptySet());
    }

    public void requestTeleport(EntityPosition pos, Set<PositionFlag> flags) {
        this.lastTeleportCheckTicks = this.ticks;
        if (++this.requestedTeleportId == Integer.MAX_VALUE) {
            this.requestedTeleportId = 0;
        }
        this.player.setPosition(pos, flags);
        this.requestedTeleportPos = this.player.getEntityPos();
        this.sendPacket(PlayerPositionLookS2CPacket.of(this.requestedTeleportId, pos, flags));
    }

    @Override
    public void onPlayerAction(PlayerActionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.canInteractWithGame()) {
            return;
        }
        BlockPos blockPos = packet.getPos();
        this.player.updateLastActionTime();
        PlayerActionC2SPacket.Action action = packet.getAction();
        switch (action) {
            case STAB: {
                if (this.player.isSpectator()) {
                    return;
                }
                ItemStack itemStack = this.player.getStackInHand(Hand.MAIN_HAND);
                if (this.player.isBelowMinimumAttackCharge(itemStack, 5)) {
                    return;
                }
                PiercingWeaponComponent piercingWeaponComponent = itemStack.get(DataComponentTypes.PIERCING_WEAPON);
                if (piercingWeaponComponent != null) {
                    piercingWeaponComponent.stab(this.player, EquipmentSlot.MAINHAND);
                }
                return;
            }
            case SWAP_ITEM_WITH_OFFHAND: {
                if (!this.player.isSpectator()) {
                    ItemStack itemStack2 = this.player.getStackInHand(Hand.OFF_HAND);
                    this.player.setStackInHand(Hand.OFF_HAND, this.player.getStackInHand(Hand.MAIN_HAND));
                    this.player.setStackInHand(Hand.MAIN_HAND, itemStack2);
                    this.player.clearActiveItem();
                }
                return;
            }
            case DROP_ITEM: {
                if (!this.player.isSpectator()) {
                    this.player.dropSelectedItem(false);
                }
                return;
            }
            case DROP_ALL_ITEMS: {
                if (!this.player.isSpectator()) {
                    this.player.dropSelectedItem(true);
                }
                return;
            }
            case RELEASE_USE_ITEM: {
                this.player.stopUsingItem();
                return;
            }
            case START_DESTROY_BLOCK: 
            case ABORT_DESTROY_BLOCK: 
            case STOP_DESTROY_BLOCK: {
                this.player.interactionManager.processBlockBreakingAction(blockPos, action, packet.getDirection(), this.player.getEntityWorld().getTopYInclusive(), packet.getSequence());
                this.updateSequence(packet.getSequence());
                return;
            }
        }
        throw new IllegalArgumentException("Invalid player action");
    }

    private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
        BucketItem bucketItem;
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return (item instanceof BlockItem || item instanceof BucketItem && (bucketItem = (BucketItem)item).getFluid() != Fluids.EMPTY) && !player.getItemCooldownManager().isCoolingDown(stack);
    }

    @Override
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.canInteractWithGame()) {
            return;
        }
        this.updateSequence(packet.getSequence());
        ServerWorld serverWorld = this.player.getEntityWorld();
        Hand hand = packet.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        if (!itemStack.isItemEnabled(serverWorld.getEnabledFeatures())) {
            return;
        }
        BlockHitResult blockHitResult = packet.getBlockHitResult();
        Vec3d vec3d = blockHitResult.getPos();
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!this.player.canInteractWithBlockAt(blockPos, 1.0)) {
            return;
        }
        Vec3d vec3d2 = vec3d.subtract(Vec3d.ofCenter(blockPos));
        double d = 1.0000001;
        if (!(Math.abs(vec3d2.getX()) < 1.0000001 && Math.abs(vec3d2.getY()) < 1.0000001 && Math.abs(vec3d2.getZ()) < 1.0000001)) {
            LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", new Object[]{this.player.getGameProfile().name(), vec3d, blockPos});
            return;
        }
        Direction direction = blockHitResult.getSide();
        this.player.updateLastActionTime();
        int i = this.player.getEntityWorld().getTopYInclusive();
        if (blockPos.getY() <= i) {
            if (this.requestedTeleportPos == null && serverWorld.canEntityModifyAt(this.player, blockPos)) {
                ActionResult.Success success;
                ActionResult actionResult = this.player.interactionManager.interactBlock(this.player, serverWorld, itemStack, hand, blockHitResult);
                if (actionResult.isAccepted()) {
                    Criteria.ANY_BLOCK_USE.trigger(this.player, blockHitResult.getBlockPos(), itemStack.copy());
                }
                if (direction == Direction.UP && !actionResult.isAccepted() && blockPos.getY() >= i && ServerPlayNetworkHandler.canPlace(this.player, itemStack)) {
                    MutableText text = Text.translatable("build.tooHigh", i).formatted(Formatting.RED);
                    this.player.sendMessageToClient(text, true);
                } else if (actionResult instanceof ActionResult.Success && (success = (ActionResult.Success)actionResult).swingSource() == ActionResult.SwingSource.SERVER) {
                    this.player.swingHand(hand, true);
                }
            }
        } else {
            MutableText text2 = Text.translatable("build.tooHigh", i).formatted(Formatting.RED);
            this.player.sendMessageToClient(text2, true);
        }
        this.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos));
        this.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos.offset(direction)));
    }

    @Override
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {
        ActionResult.Success success;
        ActionResult actionResult;
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.canInteractWithGame()) {
            return;
        }
        this.updateSequence(packet.getSequence());
        ServerWorld serverWorld = this.player.getEntityWorld();
        Hand hand = packet.getHand();
        ItemStack itemStack = this.player.getStackInHand(hand);
        this.player.updateLastActionTime();
        if (itemStack.isEmpty() || !itemStack.isItemEnabled(serverWorld.getEnabledFeatures())) {
            return;
        }
        float f = MathHelper.wrapDegrees(packet.getYaw());
        float g = MathHelper.wrapDegrees(packet.getPitch());
        if (g != this.player.getPitch() || f != this.player.getYaw()) {
            this.player.setAngles(f, g);
        }
        if ((actionResult = this.player.interactionManager.interactItem(this.player, serverWorld, itemStack, hand)) instanceof ActionResult.Success && (success = (ActionResult.Success)actionResult).swingSource() == ActionResult.SwingSource.SERVER) {
            this.player.swingHand(hand, true);
        }
    }

    @Override
    public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (this.player.isSpectator()) {
            for (ServerWorld serverWorld : this.server.getWorlds()) {
                Entity entity = packet.getTarget(serverWorld);
                if (entity == null) continue;
                this.player.teleport(serverWorld, entity.getX(), entity.getY(), entity.getZ(), Set.of(), entity.getYaw(), entity.getPitch(), true);
                return;
            }
        }
    }

    @Override
    public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        Entity entity = this.player.getControllingVehicle();
        if (entity instanceof AbstractBoatEntity) {
            AbstractBoatEntity abstractBoatEntity = (AbstractBoatEntity)entity;
            abstractBoatEntity.setPaddlesMoving(packet.isLeftPaddling(), packet.isRightPaddling());
        }
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        LOGGER.info("{} lost connection: {}", (Object)this.player.getStringifiedName(), (Object)info.reason().getString());
        this.cleanUp();
        super.onDisconnected(info);
    }

    private void cleanUp() {
        this.messageChainTaskQueue.close();
        this.server.forcePlayerSampleUpdate();
        this.server.getPlayerManager().broadcast(Text.translatable("multiplayer.player.left", this.player.getDisplayName()).formatted(Formatting.YELLOW), false);
        this.player.onDisconnect();
        this.server.getPlayerManager().remove(this.player);
        this.player.getTextStream().onDisconnect();
    }

    public void updateSequence(int sequence) {
        if (sequence < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        }
        this.sequence = Math.max(sequence, this.sequence);
    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (packet.getSelectedSlot() < 0 || packet.getSelectedSlot() >= PlayerInventory.getHotbarSize()) {
            LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getStringifiedName());
            return;
        }
        if (this.player.getInventory().getSelectedSlot() != packet.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) {
            this.player.clearActiveItem();
        }
        this.player.getInventory().setSelectedSlot(packet.getSelectedSlot());
        this.player.updateLastActionTime();
    }

    @Override
    public void onChatMessage(ChatMessageC2SPacket packet) {
        Optional<LastSeenMessageList> optional = this.validateAcknowledgment(packet.acknowledgment());
        if (optional.isEmpty()) {
            return;
        }
        this.validateMessage(packet.chatMessage(), false, () -> {
            SignedMessage signedMessage;
            try {
                signedMessage = this.getSignedMessage(packet, (LastSeenMessageList)optional.get());
            }
            catch (MessageChain.MessageChainException messageChainException) {
                this.handleMessageChainException(messageChainException);
                return;
            }
            CompletableFuture<FilteredMessage> completableFuture = this.filterText(signedMessage.getSignedContent());
            Text text = this.server.getMessageDecorator().decorate(this.player, signedMessage.getContent());
            this.messageChainTaskQueue.append(completableFuture, filtered -> {
                SignedMessage signedMessage2 = signedMessage.withUnsignedContent(text).withFilterMask(filtered.mask());
                this.handleDecoratedMessage(signedMessage2);
            });
        });
    }

    @Override
    public void onCommandExecution(CommandExecutionC2SPacket packet) {
        this.validateMessage(packet.command(), true, () -> {
            this.executeCommand(packet.command());
            this.checkForSpam();
        });
    }

    private void executeCommand(String command) {
        ParseResults<ServerCommandSource> parseResults = this.parse(command);
        if (this.server.shouldEnforceSecureProfile() && SignedArgumentList.isNotEmpty(parseResults)) {
            LOGGER.error("Received unsigned command packet from {}, but the command requires signable arguments: {}", (Object)this.player.getGameProfile().name(), (Object)command);
            this.player.sendMessage(INVALID_COMMAND_SIGNATURE_TEXT);
            return;
        }
        this.server.getCommandManager().execute(parseResults, command);
    }

    @Override
    public void onChatCommandSigned(ChatCommandSignedC2SPacket packet) {
        Optional<LastSeenMessageList> optional = this.validateAcknowledgment(packet.lastSeenMessages());
        if (optional.isEmpty()) {
            return;
        }
        this.validateMessage(packet.command(), true, () -> {
            this.handleCommandExecution(packet, (LastSeenMessageList)optional.get());
            this.checkForSpam();
        });
    }

    private void handleCommandExecution(ChatCommandSignedC2SPacket packet, LastSeenMessageList lastSeenMessages) {
        Map<String, SignedMessage> map;
        ParseResults<ServerCommandSource> parseResults = this.parse(packet.command());
        try {
            map = this.collectArgumentMessages(packet, SignedArgumentList.of(parseResults), lastSeenMessages);
        }
        catch (MessageChain.MessageChainException messageChainException) {
            this.handleMessageChainException(messageChainException);
            return;
        }
        SignedCommandArguments.Impl signedCommandArguments = new SignedCommandArguments.Impl(map);
        parseResults = CommandManager.withCommandSource(parseResults, source -> source.withSignedArguments(signedCommandArguments, this.messageChainTaskQueue));
        this.server.getCommandManager().execute(parseResults, packet.command());
    }

    private void handleMessageChainException(MessageChain.MessageChainException exception) {
        LOGGER.warn("Failed to update secure chat state for {}: '{}'", (Object)this.player.getGameProfile().name(), (Object)exception.getMessageText().getString());
        this.player.sendMessage(exception.getMessageText().copy().formatted(Formatting.RED));
    }

    private <S> Map<String, SignedMessage> collectArgumentMessages(ChatCommandSignedC2SPacket packet, SignedArgumentList<S> arguments, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException {
        List<ArgumentSignatureDataMap.Entry> list = packet.argumentSignatures().entries();
        List<SignedArgumentList.ParsedArgument<S>> list2 = arguments.arguments();
        if (list.isEmpty()) {
            return this.toUnsignedSignatures(list2);
        }
        Object2ObjectOpenHashMap map = new Object2ObjectOpenHashMap();
        for (ArgumentSignatureDataMap.Entry entry : list) {
            SignedArgumentList.ParsedArgument<S> parsedArgument = arguments.get(entry.name());
            if (parsedArgument == null) {
                this.messageUnpacker.setChainBroken();
                throw ServerPlayNetworkHandler.createInvalidCommandSignatureException(packet.command(), list, list2);
            }
            MessageBody messageBody = new MessageBody(parsedArgument.value(), packet.timestamp(), packet.salt(), lastSeenMessages);
            map.put(parsedArgument.getNodeName(), this.messageUnpacker.unpack(entry.signature(), messageBody));
        }
        for (SignedArgumentList.ParsedArgument parsedArgument : list2) {
            if (map.containsKey(parsedArgument.getNodeName())) continue;
            throw ServerPlayNetworkHandler.createInvalidCommandSignatureException(packet.command(), list, list2);
        }
        return map;
    }

    private <S> Map<String, SignedMessage> toUnsignedSignatures(List<SignedArgumentList.ParsedArgument<S>> arguments) throws MessageChain.MessageChainException {
        HashMap<String, SignedMessage> map = new HashMap<String, SignedMessage>();
        for (SignedArgumentList.ParsedArgument<S> parsedArgument : arguments) {
            MessageBody messageBody = MessageBody.ofUnsigned(parsedArgument.value());
            map.put(parsedArgument.getNodeName(), this.messageUnpacker.unpack(null, messageBody));
        }
        return map;
    }

    private static <S> MessageChain.MessageChainException createInvalidCommandSignatureException(String command, List<ArgumentSignatureDataMap.Entry> actual, List<SignedArgumentList.ParsedArgument<S>> expected) {
        String string = actual.stream().map(ArgumentSignatureDataMap.Entry::name).collect(Collectors.joining(", "));
        String string2 = expected.stream().map(SignedArgumentList.ParsedArgument::getNodeName).collect(Collectors.joining(", "));
        LOGGER.error("Signed command mismatch between server and client ('{}'): got [{}] from client, but expected [{}]", new Object[]{command, string, string2});
        return new MessageChain.MessageChainException(INVALID_COMMAND_SIGNATURE_TEXT);
    }

    private ParseResults<ServerCommandSource> parse(String command) {
        CommandDispatcher<ServerCommandSource> commandDispatcher = this.server.getCommandManager().getDispatcher();
        return commandDispatcher.parse(command, (Object)this.player.getCommandSource());
    }

    private void validateMessage(String message, boolean bl, Runnable runnable) {
        if (ServerPlayNetworkHandler.hasIllegalCharacter(message)) {
            this.disconnect(Text.translatable("multiplayer.disconnect.illegal_characters"));
            return;
        }
        if (!bl && this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
            this.sendPacket(new GameMessageS2CPacket(Text.translatable("chat.disabled.options").formatted(Formatting.RED), false));
            return;
        }
        this.player.updateLastActionTime();
        this.server.execute(runnable);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Optional<LastSeenMessageList> validateAcknowledgment(LastSeenMessageList.Acknowledgment acknowledgment) {
        AcknowledgmentValidator acknowledgmentValidator = this.acknowledgmentValidator;
        synchronized (acknowledgmentValidator) {
            try {
                LastSeenMessageList lastSeenMessageList = this.acknowledgmentValidator.validate(acknowledgment);
                return Optional.of(lastSeenMessageList);
            }
            catch (AcknowledgmentValidator.ValidationException validationException) {
                LOGGER.error("Failed to validate message acknowledgements from {}: {}", (Object)this.player.getStringifiedName(), (Object)validationException.getMessage());
                this.disconnect(CHAT_VALIDATION_FAILED_TEXT);
                return Optional.empty();
            }
        }
    }

    private static boolean hasIllegalCharacter(String message) {
        for (int i = 0; i < message.length(); ++i) {
            if (StringHelper.isValidChar(message.charAt(i))) continue;
            return true;
        }
        return false;
    }

    private SignedMessage getSignedMessage(ChatMessageC2SPacket packet, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException {
        MessageBody messageBody = new MessageBody(packet.chatMessage(), packet.timestamp(), packet.salt(), lastSeenMessages);
        return this.messageUnpacker.unpack(packet.signature(), messageBody);
    }

    private void handleDecoratedMessage(SignedMessage message) {
        this.server.getPlayerManager().broadcast(message, this.player, MessageType.params(MessageType.CHAT, this.player));
        this.checkForSpam();
    }

    private void checkForSpam() {
        this.messageCooldown.increment();
        if (!(this.messageCooldown.canUse() || this.server.getPlayerManager().isOperator(this.player.getPlayerConfigEntry()) || this.server.isHost(this.player.getPlayerConfigEntry()))) {
            this.disconnect(Text.translatable("disconnect.spam"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onMessageAcknowledgment(MessageAcknowledgmentC2SPacket packet) {
        AcknowledgmentValidator acknowledgmentValidator = this.acknowledgmentValidator;
        synchronized (acknowledgmentValidator) {
            try {
                this.acknowledgmentValidator.removeUntil(packet.offset());
            }
            catch (AcknowledgmentValidator.ValidationException validationException) {
                LOGGER.error("Failed to validate message acknowledgement offset from {}: {}", (Object)this.player.getStringifiedName(), (Object)validationException.getMessage());
                this.disconnect(CHAT_VALIDATION_FAILED_TEXT);
            }
        }
    }

    @Override
    public void onHandSwing(HandSwingC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.updateLastActionTime();
        this.player.swingHand(packet.getHand());
    }

    @Override
    public void onClientCommand(ClientCommandC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.canInteractWithGame()) {
            return;
        }
        this.player.updateLastActionTime();
        switch (packet.getMode()) {
            case START_SPRINTING: {
                this.player.setSprinting(true);
                break;
            }
            case STOP_SPRINTING: {
                this.player.setSprinting(false);
                break;
            }
            case STOP_SLEEPING: {
                if (!this.player.isSleeping()) break;
                this.player.wakeUp(false, true);
                this.requestedTeleportPos = this.player.getEntityPos();
                break;
            }
            case START_RIDING_JUMP: {
                Entity entity = this.player.getControllingVehicle();
                if (!(entity instanceof JumpingMount)) break;
                JumpingMount jumpingMount = (JumpingMount)((Object)entity);
                int i = packet.getMountJumpHeight();
                if (!jumpingMount.canJump() || i <= 0) break;
                jumpingMount.startJumping(i);
                break;
            }
            case STOP_RIDING_JUMP: {
                Entity entity = this.player.getControllingVehicle();
                if (!(entity instanceof JumpingMount)) break;
                JumpingMount jumpingMount = (JumpingMount)((Object)entity);
                jumpingMount.stopJumping();
                break;
            }
            case OPEN_INVENTORY: {
                Entity entity = this.player.getVehicle();
                if (!(entity instanceof RideableInventory)) break;
                RideableInventory rideableInventory = (RideableInventory)((Object)entity);
                rideableInventory.openInventory(this.player);
                break;
            }
            case START_FALL_FLYING: {
                if (this.player.checkGliding()) break;
                this.player.stopGliding();
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid client command!");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendChatMessage(SignedMessage message, MessageType.Parameters params) {
        int i;
        this.sendPacket(new ChatMessageS2CPacket(this.globalChatMessageIndex++, message.link().sender(), message.link().index(), message.signature(), message.signedBody().toSerialized(this.signatureStorage), message.unsignedContent(), message.filterMask(), params));
        MessageSignatureData messageSignatureData = message.signature();
        if (messageSignatureData == null) {
            return;
        }
        this.signatureStorage.add(message.signedBody(), message.signature());
        AcknowledgmentValidator acknowledgmentValidator = this.acknowledgmentValidator;
        synchronized (acknowledgmentValidator) {
            this.acknowledgmentValidator.addPending(messageSignatureData);
            i = this.acknowledgmentValidator.getMessageCount();
        }
        if (i > 4096) {
            this.disconnect(Text.translatable("multiplayer.disconnect.too_many_pending_chats"));
        }
    }

    public void sendProfilelessChatMessage(Text message, MessageType.Parameters params) {
        this.sendPacket(new ProfilelessChatMessageS2CPacket(message, params));
    }

    public SocketAddress getConnectionAddress() {
        return this.connection.getAddress();
    }

    public void reconfigure() {
        this.requestedReconfiguration = true;
        this.cleanUp();
        this.sendPacket(EnterReconfigurationS2CPacket.INSTANCE);
        this.connection.transitionOutbound(ConfigurationStates.S2C);
    }

    @Override
    public void onQueryPing(QueryPingC2SPacket packet) {
        this.connection.send(new PingResultS2CPacket(packet.getStartTime()));
    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.canInteractWithGame()) {
            return;
        }
        final ServerWorld serverWorld = this.player.getEntityWorld();
        final Entity entity = packet.getEntity(serverWorld);
        this.player.updateLastActionTime();
        this.player.setSneaking(packet.isPlayerSneaking());
        if (entity != null) {
            if (!serverWorld.getWorldBorder().contains(entity.getBlockPos())) {
                return;
            }
            Box box = entity.getBoundingBox();
            if (packet.canInteractWithEntityIn(this.player, box, 3.0)) {
                packet.handle(new PlayerInteractEntityC2SPacket.Handler(){

                    private void processInteract(Hand hand, Interaction action) {
                        ItemStack itemStack = ServerPlayNetworkHandler.this.player.getStackInHand(hand);
                        if (!itemStack.isItemEnabled(serverWorld.getEnabledFeatures())) {
                            return;
                        }
                        ItemStack itemStack2 = itemStack.copy();
                        ActionResult actionResult = action.run(ServerPlayNetworkHandler.this.player, entity, hand);
                        if (actionResult instanceof ActionResult.Success) {
                            ActionResult.Success success = (ActionResult.Success)actionResult;
                            ItemStack itemStack3 = success.shouldIncrementStat() ? itemStack2 : ItemStack.EMPTY;
                            Criteria.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerPlayNetworkHandler.this.player, itemStack3, entity);
                            if (success.swingSource() == ActionResult.SwingSource.SERVER) {
                                ServerPlayNetworkHandler.this.player.swingHand(hand, true);
                            }
                        }
                    }

                    @Override
                    public void interact(Hand hand) {
                        this.processInteract(hand, PlayerEntity::interact);
                    }

                    @Override
                    public void interactAt(Hand hand, Vec3d pos) {
                        this.processInteract(hand, (player, entity, handx) -> entity.interactAt(player, pos, handx));
                    }

                    @Override
                    public void attack() {
                        PersistentProjectileEntity persistentProjectileEntity;
                        if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity == ServerPlayNetworkHandler.this.player || entity instanceof PersistentProjectileEntity && !(persistentProjectileEntity = (PersistentProjectileEntity)entity).isAttackable()) {
                            ServerPlayNetworkHandler.this.disconnect(Text.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            LOGGER.warn("Player {} tried to attack an invalid entity", (Object)ServerPlayNetworkHandler.this.player.getStringifiedName());
                            return;
                        }
                        ItemStack itemStack = ServerPlayNetworkHandler.this.player.getStackInHand(Hand.MAIN_HAND);
                        if (!itemStack.isItemEnabled(serverWorld.getEnabledFeatures())) {
                            return;
                        }
                        if (ServerPlayNetworkHandler.this.player.isBelowMinimumAttackCharge(itemStack, 5)) {
                            return;
                        }
                        ServerPlayNetworkHandler.this.player.attack(entity);
                    }
                });
            }
        }
    }

    @Override
    public void onClientStatus(ClientStatusC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.updateLastActionTime();
        ClientStatusC2SPacket.Mode mode = packet.getMode();
        switch (mode) {
            case PERFORM_RESPAWN: {
                if (this.player.notInAnyWorld) {
                    this.player.notInAnyWorld = false;
                    this.player = this.server.getPlayerManager().respawnPlayer(this.player, true, Entity.RemovalReason.CHANGED_DIMENSION);
                    this.syncWithPlayerPosition();
                    this.markRespawned();
                    Criteria.CHANGED_DIMENSION.trigger(this.player, World.END, World.OVERWORLD);
                    break;
                }
                if (this.player.getHealth() > 0.0f) {
                    return;
                }
                this.player = this.server.getPlayerManager().respawnPlayer(this.player, false, Entity.RemovalReason.KILLED);
                this.syncWithPlayerPosition();
                this.markRespawned();
                if (!this.server.isHardcore()) break;
                this.player.changeGameMode(GameMode.SPECTATOR);
                this.player.getEntityWorld().getGameRules().setValue(GameRules.SPECTATORS_GENERATE_CHUNKS, false, this.server);
                break;
            }
            case REQUEST_STATS: {
                this.player.getStatHandler().sendStats(this.player);
            }
        }
    }

    @Override
    public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.onHandledScreenClosed();
    }

    @Override
    public void onClickSlot(ClickSlotC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.updateLastActionTime();
        if (this.player.currentScreenHandler.syncId != packet.syncId()) {
            return;
        }
        if (this.player.isSpectator()) {
            this.player.currentScreenHandler.syncState();
            return;
        }
        if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
            return;
        }
        short i = packet.slot();
        if (!this.player.currentScreenHandler.isValid(i)) {
            LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", new Object[]{this.player.getStringifiedName(), (int)i, this.player.currentScreenHandler.slots.size()});
            return;
        }
        boolean bl = packet.revision() != this.player.currentScreenHandler.getRevision();
        this.player.currentScreenHandler.disableSyncing();
        this.player.currentScreenHandler.onSlotClick(i, packet.button(), packet.actionType(), this.player);
        for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(packet.modifiedStacks())) {
            this.player.currentScreenHandler.setReceivedHash(entry.getIntKey(), (ItemStackHash)entry.getValue());
        }
        this.player.currentScreenHandler.setReceivedCursorHash(packet.cursor());
        this.player.currentScreenHandler.enableSyncing();
        if (bl) {
            this.player.currentScreenHandler.updateToClient();
        } else {
            this.player.currentScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public void onCraftRequest(CraftRequestC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.updateLastActionTime();
        if (this.player.isSpectator() || this.player.currentScreenHandler.syncId != packet.syncId()) {
            return;
        }
        if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
            return;
        }
        ServerRecipeManager.ServerRecipe serverRecipe = this.server.getRecipeManager().get(packet.recipeId());
        if (serverRecipe == null) {
            return;
        }
        RecipeEntry<?> recipeEntry = serverRecipe.parent();
        if (!this.player.getRecipeBook().isUnlocked(recipeEntry.id())) {
            return;
        }
        ScreenHandler screenHandler = this.player.currentScreenHandler;
        if (screenHandler instanceof AbstractRecipeScreenHandler) {
            AbstractRecipeScreenHandler abstractRecipeScreenHandler = (AbstractRecipeScreenHandler)screenHandler;
            if (recipeEntry.value().getIngredientPlacement().hasNoPlacement()) {
                LOGGER.debug("Player {} tried to place impossible recipe {}", (Object)this.player, (Object)recipeEntry.id().getValue());
                return;
            }
            AbstractRecipeScreenHandler.PostFillAction postFillAction = abstractRecipeScreenHandler.fillInputSlots(packet.craftAll(), this.player.isCreative(), recipeEntry, this.player.getEntityWorld(), this.player.getInventory());
            if (postFillAction == AbstractRecipeScreenHandler.PostFillAction.PLACE_GHOST_RECIPE) {
                this.sendPacket(new CraftFailedResponseS2CPacket(this.player.currentScreenHandler.syncId, serverRecipe.display().display()));
            }
        }
    }

    @Override
    public void onButtonClick(ButtonClickC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.updateLastActionTime();
        if (this.player.currentScreenHandler.syncId != packet.syncId() || this.player.isSpectator()) {
            return;
        }
        if (!this.player.currentScreenHandler.canUse(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.currentScreenHandler);
            return;
        }
        boolean bl = this.player.currentScreenHandler.onButtonClick(this.player, packet.buttonId());
        if (bl) {
            this.player.currentScreenHandler.sendContentUpdates();
        }
    }

    @Override
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (this.player.isInCreativeMode()) {
            boolean bl3;
            boolean bl = packet.slot() < 0;
            ItemStack itemStack = packet.stack();
            if (!itemStack.isItemEnabled(this.player.getEntityWorld().getEnabledFeatures())) {
                return;
            }
            boolean bl2 = packet.slot() >= 1 && packet.slot() <= 45;
            boolean bl4 = bl3 = itemStack.isEmpty() || itemStack.getCount() <= itemStack.getMaxCount();
            if (bl2 && bl3) {
                this.player.playerScreenHandler.getSlot(packet.slot()).setStack(itemStack);
                this.player.playerScreenHandler.setReceivedStack(packet.slot(), itemStack);
                this.player.playerScreenHandler.sendContentUpdates();
            } else if (bl && bl3) {
                if (this.creativeItemDropCooldown.canUse()) {
                    this.creativeItemDropCooldown.increment();
                    this.player.dropItem(itemStack, true);
                } else {
                    LOGGER.warn("Player {} was dropping items too fast in creative mode, ignoring.", (Object)this.player.getStringifiedName());
                }
            }
        }
    }

    @Override
    public void onUpdateSign(UpdateSignC2SPacket packet) {
        List<String> list = Stream.of(packet.getText()).map(Formatting::strip).collect(Collectors.toList());
        this.filterTexts(list).thenAcceptAsync(texts -> this.onSignUpdate(packet, (List<FilteredMessage>)texts), (Executor)this.server);
    }

    private void onSignUpdate(UpdateSignC2SPacket packet, List<FilteredMessage> signText) {
        this.player.updateLastActionTime();
        ServerWorld serverWorld = this.player.getEntityWorld();
        BlockPos blockPos = packet.getPos();
        if (serverWorld.isChunkLoaded(blockPos)) {
            BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
            if (!(blockEntity instanceof SignBlockEntity)) {
                return;
            }
            SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
            signBlockEntity.tryChangeText(this.player, packet.isFront(), signText);
        }
    }

    @Override
    public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.getAbilities().flying = packet.isFlying() && this.player.getAbilities().allowFlying;
    }

    @Override
    public void onClientOptions(ClientOptionsC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        boolean bl = this.player.isModelPartVisible(PlayerModelPart.HAT);
        this.player.setClientOptions(packet.options());
        if (this.player.isModelPartVisible(PlayerModelPart.HAT) != bl) {
            this.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_HAT, this.player));
        }
    }

    @Override
    public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS) && !this.isHost()) {
            LOGGER.warn("Player {} tried to change difficulty to {} without required permissions", (Object)this.player.getGameProfile().name(), (Object)packet.difficulty().getTranslatableName());
            return;
        }
        this.server.setDifficulty(packet.difficulty(), false);
    }

    @Override
    public void onChangeGameMode(ChangeGameModeC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!GameModeCommand.PERMISSION_CHECK.allows(this.player.getPermissions())) {
            LOGGER.warn("Player {} tried to change game mode to {} without required permissions", (Object)this.player.getGameProfile().name(), (Object)packet.mode().getSimpleTranslatableName().getString());
            return;
        }
        GameModeCommand.execute(this.player, packet.mode());
    }

    @Override
    public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS) && !this.isHost()) {
            return;
        }
        this.server.setDifficultyLocked(packet.isDifficultyLocked());
    }

    @Override
    public void onPlayerSession(PlayerSessionC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        PublicPlayerSession.Serialized serialized = packet.chatSession();
        PlayerPublicKey.PublicKeyData publicKeyData = this.session != null ? this.session.publicKeyData().data() : null;
        PlayerPublicKey.PublicKeyData publicKeyData2 = serialized.publicKeyData();
        if (Objects.equals(publicKeyData, publicKeyData2)) {
            return;
        }
        if (publicKeyData != null && publicKeyData2.expiresAt().isBefore(publicKeyData.expiresAt())) {
            this.disconnect(PlayerPublicKey.EXPIRED_PUBLIC_KEY_TEXT);
            return;
        }
        try {
            SignatureVerifier signatureVerifier = this.server.getApiServices().serviceSignatureVerifier();
            if (signatureVerifier == null) {
                LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)this.player.getGameProfile().name());
                return;
            }
            this.setSession(serialized.toSession(this.player.getGameProfile(), signatureVerifier));
        }
        catch (PlayerPublicKey.PublicKeyException publicKeyException) {
            LOGGER.error("Failed to validate profile key: {}", (Object)publicKeyException.getMessage());
            this.disconnect(publicKeyException.getMessageText());
        }
    }

    @Override
    public void onAcknowledgeReconfiguration(AcknowledgeReconfigurationC2SPacket packet) {
        if (!this.requestedReconfiguration) {
            throw new IllegalStateException("Client acknowledged config, but none was requested");
        }
        this.connection.transitionInbound(ConfigurationStates.C2S, new ServerConfigurationNetworkHandler(this.server, this.connection, this.createClientData(this.player.getClientOptions())));
    }

    @Override
    public void onAcknowledgeChunks(AcknowledgeChunksC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.chunkDataSender.onAcknowledgeChunks(packet.desiredChunksPerTick());
    }

    @Override
    public void onDebugSubscriptionRequest(DebugSubscriptionRequestC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        this.player.setSubscribedTypes(packet.subscriptions());
    }

    private void setSession(PublicPlayerSession session) {
        this.session = session;
        this.messageUnpacker = session.createUnpacker(this.player.getUuid());
        this.messageChainTaskQueue.append(() -> {
            this.player.setSession(session);
            this.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.INITIALIZE_CHAT), List.of(this.player)));
        });
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
    }

    @Override
    public void onClientTickEnd(ClientTickEndC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.player.getEntityWorld());
        if (!this.movedThisTick) {
            this.player.setMovement(Vec3d.ZERO);
        }
        this.movedThisTick = false;
    }

    private void handleMovement(Vec3d movement) {
        if (movement.lengthSquared() > (double)1.0E-5f) {
            this.player.updateLastActionTime();
        }
        this.player.setMovement(movement);
        this.movedThisTick = true;
    }

    @Override
    public boolean isInCreativeMode() {
        return this.player.isInCreativeMode();
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public boolean canInteractWithGame() {
        return !this.dead && this.remainingLoadingTicks <= 0;
    }

    public void tickLoading() {
        if (this.remainingLoadingTicks > 0) {
            --this.remainingLoadingTicks;
        }
    }

    private void markLoaded() {
        this.remainingLoadingTicks = 0;
    }

    public void markAsDead() {
        this.dead = true;
    }

    private void markRespawned() {
        this.dead = false;
        this.remainingLoadingTicks = 60;
    }

    @FunctionalInterface
    static interface Interaction {
        public ActionResult run(ServerPlayerEntity var1, Entity var2, Hand var3);
    }
}

