package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.util.TimeBridge;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DebrisResonatorItem extends Item {

    private static final int  DEFAULT_MAX_BATTERY_SECONDS   = 1800; // 30 mins
    private static final double  SCAN_PERIOD_SECONDS        = 0.5;
    private static final int  RANGE_XZ                      = 16;
    private static final int  DEFAULT_RANGE_Y               = 2;
    private static final int  RELEASE_DISTANCE              = 32;
    private static final long RELEASE_GRACE_MS              = 1500;
    private static final int  DEFAULT_COOLDOWN_SELF         = 5;
    private static final int  DEFAULT_COOLDOWN_OTHER        = 1;
    private static final int  DEFAULT_COOLDOWN_FAR          = 30;
    private static final long MODEL_RATE_LIMIT_MS           = 500;
    private static final float RANGE_TRANSITION_VOLUME       = 0.45f;

    private static int maxBatterySeconds() {
        int value = ConfigManager.get().debrisResonatorMaxBatterySeconds;
        if (value < 0) {
            value = DEFAULT_MAX_BATTERY_SECONDS;
        }
        return Mth.clamp(value, 0, 24 * 60 * 60);
    }

    private static int cooldownSelfSeconds() {
        int value = ConfigManager.get().debrisResonatorCooldownSelfSeconds;
        if (value < 0) {
            value = DEFAULT_COOLDOWN_SELF;
        }
        return Mth.clamp(value, 0, 3600);
    }

    private static int cooldownOtherSeconds() {
        int value = ConfigManager.get().debrisResonatorCooldownOtherSeconds;
        if (value < 0) {
            value = DEFAULT_COOLDOWN_OTHER;
        }
        return Mth.clamp(value, 0, 3600);
    }

    private static int cooldownFarSeconds() {
        int value = ConfigManager.get().debrisResonatorCooldownFarSeconds;
        if (value < 0) {
            value = DEFAULT_COOLDOWN_FAR;
        }
        return Mth.clamp(value, 0, 3600);
    }

    private static int rangeY() {
        int value = ConfigManager.get().debrisResonatorRangeY;
        if (value < 0) {
            value = DEFAULT_RANGE_Y;
        }
        return Mth.clamp(value, 0, 64);
    }



    private static final int CMD_INDEX       = 0;
    private static final int CMD_F_BASE      = 0;
    private static final int CMD_F_SINCE     = 1;
    private static final int CMD_FLAG_LOCKED = 0;

    private static final String ROOT_KEY                    = "lategameplus";
    private static final String KEY_BATTERY                 = "res_battery_secs";
    private static final String KEY_SCAN_COOLDOWN_UNTIL     = "res_scan_cooldown_until";
    private static final String KEY_TARGET_LOCKED           = "res_target_locked";
    private static final String KEY_TARGET_DIM              = "res_target_dim";
    private static final String KEY_TARGET_X                = "res_target_x";
    private static final String KEY_TARGET_Y                = "res_target_y";
    private static final String KEY_TARGET_Z                = "res_target_z";
    private static final String KEY_MODEL_TIER              = "res_model_tier";
    private static final String KEY_LAST_MODEL_UPDATE_MS    = "res_last_model_update_ms";
    private static final String KEY_FAR_SINCE_MS            = "res_far_since_ms";
    private static final String KEY_MISSING_SINCE_MS        = "res_missing_since_ms";
    private static final long   MISSING_GRACE_MS            = 100L;

    private static final Map<UUID, Long> soundCycleCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> nextScanCache = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lastPingTierCache = new ConcurrentHashMap<>();

    private static void clearSoundCycle(Player player) {
        UUID playerId = player.getUUID();
        soundCycleCache.remove(playerId);
        nextScanCache.remove(playerId);
        lastPingTierCache.remove(playerId);
    }

    public DebrisResonatorItem(Properties settings) {
        super(settings.stacksTo(1));
    }

    private enum State { OFF, SEARCHING, DEPLETED }

    

    private static State readState(ItemStack stack) {
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return State.OFF;
        String s = cmd.getString(CMD_INDEX);
        if (s == null) return State.OFF;
        return switch (s) {
            case "searching", "on_too_far", "on_far", "on_mid", "on_close" -> State.SEARCHING;
            case "depleted" -> State.DEPLETED;
            default -> State.OFF;
        };
    }

    private static void setModelString(ItemStack stack, String modelKey) {
        CustomModelData prev = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        List<Float> floats = prev != null ? prev.floats()  : List.of();
        List<Boolean> flags = prev != null ? prev.flags()  : List.of();
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(floats, flags, List.of(modelKey), colors));
    }

    private static void writeState(ItemStack stack, State state) {
        if (state == State.OFF) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
            return;
        }
        setModelString(stack, state == State.SEARCHING ? "searching" : "depleted");
    }

    private static void setCooldownVisual(ItemStack stack) {
        setModelString(stack, "cooldown");
    }

    private static String getModelStateString(ItemStack stack) {
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return "off";
        String s = cmd.getString(CMD_INDEX);
        return s != null ? s : "off";
    }



    private static CustomData getComp(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    }

    private static void mutateRoot(ItemStack stack, Consumer<net.minecraft.nbt.CompoundTag> updater) {
        CustomData comp = getComp(stack);
        net.minecraft.nbt.CompoundTag data = comp.copyTag();
        net.minecraft.nbt.CompoundTag root = data.getCompound(ROOT_KEY).orElseGet(net.minecraft.nbt.CompoundTag::new);
        updater.accept(root);
        data.put(ROOT_KEY, root);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    private static int readInt(ItemStack stack, String key, int def) {
        var data = getComp(stack).copyTag();
        var rootOpt = data.getCompound(ROOT_KEY);
        if (rootOpt.isEmpty()) return def;
        var v = rootOpt.get().getInt(key);
        return v.isPresent() ? v.get() : def;
    }

    private static long readLong(ItemStack stack, String key, long def) {
        var data = getComp(stack).copyTag();
        var rootOpt = data.getCompound(ROOT_KEY);
        if (rootOpt.isEmpty()) return def;
        var v = rootOpt.get().getLong(key);
        return v.isPresent() ? v.get() : def;
    }

    private static boolean readBool(ItemStack stack, String key, boolean def) {
        var data = getComp(stack).copyTag();
        var rootOpt = data.getCompound(ROOT_KEY);
        if (rootOpt.isEmpty()) return def;
        var v = rootOpt.get().getBoolean(key);
        return v.isPresent() ? v.get() : def;
    }

    private static void writeInt(ItemStack stack, String key, int value) {
        mutateRoot(stack, root -> root.putInt(key, value));
    }

    private static void writeLong(ItemStack stack, String key, long value) {
        mutateRoot(stack, root -> root.putLong(key, value));
    }

    private static void writeBool(ItemStack stack, String key, boolean value) {
        mutateRoot(stack, root -> root.putBoolean(key, value));
    }

    private static void removeKey(ItemStack stack, String key) {
        mutateRoot(stack, root -> root.remove(key));
    }



    private static int readBattery(ItemStack stack) {
        return readInt(stack, KEY_BATTERY, maxBatterySeconds());
    }

    private static void writeBattery(ItemStack stack, int seconds) {
        writeInt(stack, KEY_BATTERY, Mth.clamp(seconds, 0, maxBatterySeconds()));
    }

    public static int getBatterySeconds(ItemStack stack) {
        return readBattery(stack);
    }

    public static void setBatterySeconds(ItemStack stack, int seconds) {
        int clamped = Mth.clamp(seconds, 0, maxBatterySeconds());
        writeBattery(stack, clamped);
        State state = readState(stack);
        if (state == State.SEARCHING) {
            float nowSeconds = TimeBridge.nowSeconds();
            boolean locked = getCmdFlag(stack, CMD_FLAG_LOCKED, false);
            setCmdStateFloatsFlags(stack, getModelStateString(stack), clamped, nowSeconds, locked);
        } else {
            clearCmdFloatsFlagsKeepState(stack);
            if (state == State.DEPLETED && clamped > 0) {
                writeState(stack, State.OFF);
                writeBool(stack, KEY_TARGET_LOCKED, false);
            }
        }
    }

    public static int getMaxBatterySeconds() {
        return maxBatterySeconds();
    }

    public static void addBatterySeconds(ItemStack stack, int deltaSeconds) {
        if (deltaSeconds <= 0) return;
        int max = maxBatterySeconds();
        int current = getBatterySeconds(stack);
        setBatterySeconds(stack, Math.min(max, current + deltaSeconds));
    }



    private static float getCmdFloat(ItemStack stack, int idx, float def) {
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return def;
        List<Float> fs = cmd.floats();
        if (fs == null || fs.size() <= idx) return def;
        Float f = fs.get(idx);
        return f != null ? f : def;
    }

    private static boolean getCmdFlag(ItemStack stack, int idx, boolean def) {
        CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null) return def;
        List<Boolean> fl = cmd.flags();
        if (fl == null || fl.size() <= idx) return def;
        Boolean b = fl.get(idx);
        return b != null ? b : def;
    }

    private static void setCmdStateFloatsFlags(ItemStack stack, String stateString, float baseSeconds, float sinceSeconds, boolean locked) {
        CustomModelData prev = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(
                        List.of(baseSeconds, sinceSeconds),
                        List.of(locked),
                        List.of(stateString),
                        colors
                ));
    }

    private static void clearCmdFloatsFlagsKeepState(ItemStack stack) {
        CustomModelData prev = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        String s = (prev != null && prev.getString(CMD_INDEX) != null) ? prev.getString(CMD_INDEX) : "off";
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of(s), colors));
    }



    private static int calcEffectiveBatteryLive(ItemStack stack) {
        State st = readState(stack);
        if (st != State.SEARCHING) return readBattery(stack);
        float base  = getCmdFloat(stack, CMD_F_BASE,  readBattery(stack));
        float since = getCmdFloat(stack, CMD_F_SINCE, TimeBridge.nowSeconds());
        long now = TimeBridge.nowSeconds();
        long elapsed = Math.max(0L, now - (long) since);
        
        if (elapsed > base + 60) { 
            return readBattery(stack);
        }
        
        long eff = (long) base - elapsed;
        return (int) Math.max(0L, eff);
    }

    private static void commitBatteryFromFloats(ItemStack stack) {
        int eff = calcEffectiveBatteryLive(stack);
        writeBattery(stack, eff);
        clearCmdFloatsFlagsKeepState(stack);
    }



    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide()) {
            long now = TimeBridge.nowSeconds();
            State st = readState(stack);

            if (st == State.OFF) {
                if (world.dimension() != Level.NETHER) {
                    user.sendOverlayMessage(Component.translatable("item.lategameplus.debris_resonator.invalid_dimension").withStyle(ChatFormatting.GRAY));
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.PLAYERS, 0.6f, 0.8f);
                    return InteractionResult.SUCCESS;
                }

                long cdUntil = readLong(stack, KEY_SCAN_COOLDOWN_UNTIL, 0L);
                if (now < cdUntil) {
                    long remainingSecs = cdUntil - now;
                    Component cooldownMessage = (remainingSecs >= 60)
                            ? Component.translatable("item.lategameplus.debris_resonator.cooldown_message_minutes", Math.round(remainingSecs / 60f))
                            : Component.translatable("item.lategameplus.debris_resonator.cooldown_message_seconds", remainingSecs);
                    user.sendOverlayMessage(cooldownMessage.copy().withStyle(ChatFormatting.GRAY));
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.PLAYERS, 0.6f, 0.8f);
                    return InteractionResult.SUCCESS;
                }
                int base = readBattery(stack);
                if (base > 0) {
                    setCmdStateFloatsFlags(stack, "searching", base, (float) now, true);
                    writeBool(stack, KEY_TARGET_LOCKED, false);
                    UUID userId = user.getUUID();
                    nextScanCache.put(userId, now);
                    lastPingTierCache.put(userId, 0);
                    writeInt(stack, KEY_MODEL_TIER, 0);
                    writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
                    removeKey(stack, KEY_FAR_SINCE_MS);
                    removeKey(stack, KEY_MISSING_SINCE_MS);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.8f, 1.0f);
                    ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANT, user.getX(), user.getY(0.5), user.getZ(),
                            8, 0.2, 0.2, 0.2, 0.0);
                } else {
                    writeState(stack, State.DEPLETED);
                }
            } else if (st == State.SEARCHING) {
                boolean targetLocked = readBool(stack, KEY_TARGET_LOCKED, false);
                if (targetLocked) {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.PLAYERS, 0.6f, 0.8f);
                    ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, user.getX(), user.getY(0.5), user.getZ(),
                            5, 0.1, 0.1, 0.1, 0.0);
                    user.sendOverlayMessage(Component.translatable("item.lategameplus.debris_resonator.locked").withStyle(ChatFormatting.GRAY));
                } else {
                    commitBatteryFromFloats(stack);
                    clearTarget(stack);
                    removeKey(stack, KEY_FAR_SINCE_MS);
                    removeKey(stack, KEY_MISSING_SINCE_MS);
                    clearSoundCycle(user);
                    writeState(stack, State.OFF);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 0.95f);
                }
            }
        }
        user.swing(hand);
        return InteractionResult.SUCCESS;
    }



    @SuppressWarnings("null")
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof Player player)) return;

        State currentState = readState(stack);
        if (currentState == State.OFF) {
            long now = TimeBridge.nowSeconds();
            long cdUntil = readLong(stack, KEY_SCAN_COOLDOWN_UNTIL, 0L);
            if (cdUntil > 0L && now >= cdUntil) {
                removeKey(stack, KEY_SCAN_COOLDOWN_UNTIL);
                stack.remove(DataComponents.CUSTOM_MODEL_DATA);
            }
        }

        if (readState(stack) == State.SEARCHING) {
            float base = getCmdFloat(stack, CMD_F_BASE, -1);
            float since = getCmdFloat(stack, CMD_F_SINCE, -1);
            if (base < 0 || since < 0) {
                int savedBattery = readBattery(stack);
                float nowSeconds = TimeBridge.nowSeconds();
                setCmdStateFloatsFlags(stack, readState(stack) == State.SEARCHING ? getModelStateString(stack) : "searching", 
                                     savedBattery, nowSeconds, getCmdFlag(stack, CMD_FLAG_LOCKED, false));
            }
            
            int eff = calcEffectiveBatteryLive(stack);
            if (eff <= 0) {
                commitBatteryFromFloats(stack);
                writeState(stack, State.DEPLETED);
                clearSoundCycle(player);
                long now = TimeBridge.nowSeconds();
                writeLong(stack, KEY_SCAN_COOLDOWN_UNTIL, now + cooldownSelfSeconds());
                clearTarget(stack);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.9f, 0.9f);
                world.sendParticles(ParticleTypes.POOF, player.getX(), player.getY(0.5), player.getZ(),
                        6, 0.2, 0.2, 0.2, 0.0);
                return;
            }
        } else {
            return;
        }

        long now = TimeBridge.nowSeconds();


        boolean locked = readBool(stack, KEY_TARGET_LOCKED, false);
        if (!locked) {
            if (!isNether(world)) { return; }
            
            UUID playerId = player.getUUID();
            long nextScanDeciseconds = nextScanCache.getOrDefault(playerId, 0L);
            long nowDeciseconds = now * 10;
            long scanPeriodDeciseconds = (long)(SCAN_PERIOD_SECONDS * 10);
            if (nextScanDeciseconds > nowDeciseconds + scanPeriodDeciseconds + 100) {
                nextScanDeciseconds = 0L;
            }
            
            if (nowDeciseconds < nextScanDeciseconds) { return; }
            nextScanCache.put(playerId, nowDeciseconds + scanPeriodDeciseconds);

            BlockPos origin = player.blockPosition();
            long seed = world.getSeed();
            BlockPos found = scanForDebris(world, origin, seed);
            if (found != null) {
                setTarget(stack, world, found);
                playConnectionFeedback(world, player);
                updateGuidanceModelIfNeeded(stack, player.getX(), player.getY(), player.getZ(), found, world, player, false);
            }
            return;
        }

        BlockPos target = readTargetPos(stack);
        if (target == null) {
            writeBool(stack, KEY_TARGET_LOCKED, false);
            return;
        }


        updateGuidanceModelIfNeeded(stack, player.getX(), player.getY(), player.getZ(), target, world, player, true);
        boolean isStillThere = world.getBlockState(target).is(Blocks.ANCIENT_DEBRIS);
        long nowMs = System.currentTimeMillis();

        if (!isStillThere) {
            long since = readLong(stack, KEY_MISSING_SINCE_MS, 0L);
            if (since == 0L) {
                writeLong(stack, KEY_MISSING_SINCE_MS, nowMs);
            } else if (nowMs - since >= MISSING_GRACE_MS) {
                liberateToCooldown(stack, world, player, cooldownOtherSeconds());
            }
        } else {
            removeKey(stack, KEY_MISSING_SINCE_MS);
        }
        double dist = player.position().distanceTo(target.getCenter());
        if (dist <= RELEASE_DISTANCE) {
            removeKey(stack, KEY_FAR_SINCE_MS);
        } else {
            long farSince = readLong(stack, KEY_FAR_SINCE_MS, 0L);
            if (farSince == 0L) {
                writeLong(stack, KEY_FAR_SINCE_MS, nowMs);
            } else if (nowMs - farSince >= RELEASE_GRACE_MS) {
                liberateToCooldown(stack, world, player, cooldownFarSeconds());
            }
        }
    }

    private static void liberateToCooldown(ItemStack stack, ServerLevel world, Player player, int seconds) {
        commitBatteryFromFloats(stack);
        clearTarget(stack);
        clearSoundCycle(player);
        writeLong(stack, KEY_SCAN_COOLDOWN_UNTIL, TimeBridge.nowSeconds() + seconds);
        setCooldownVisual(stack);
        playDisconnectFeedback(world, player);
    }

    private static boolean isNether(ServerLevel world) {
        return world.dimension() == Level.NETHER;
    }

    private static BlockPos scanForDebris(ServerLevel world, BlockPos origin, long worldSeed) {
        int verticalRange = rangeY();
        for (int dy = -verticalRange; dy <= verticalRange; dy++) {
            for (int dx = -RANGE_XZ; dx <= RANGE_XZ; dx++) {
                for (int dz = -RANGE_XZ; dz <= RANGE_XZ; dz++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    if (!world.getBlockState(p).is(Blocks.ANCIENT_DEBRIS)) continue;
                    if (!isTrackable(world, p, worldSeed)) continue;
                    return p;
                }
            }
        }
        return null;
    }

    private static boolean isTrackable(ServerLevel world, BlockPos pos, long seed) {
        boolean isNatural = isNaturallyGenerated(world, pos, seed);

        
        if (!isNatural) {
            return false;
        }
        
        long dimHash = world.dimension().identifier().hashCode();
        long h = seed
                ^ (dimHash * 0x9E3779B97F4A7C15L)
                ^ (pos.getX() * 73428767L)
                ^ (pos.getY() * 912367L)
                ^ (pos.getZ() * 137L);
        long v = (h >>> 1) % 100L;
        boolean trackable = v < 50L;
        

        return trackable;
    }
    
    private static boolean isNaturallyGenerated(ServerLevel world, BlockPos pos, long seed) {
        String key = world.dimension().identifier().toString() + ":" + pos.toShortString();
        boolean isPlayerPlaced = DebrisResonatorHooks.playerPlacedDebris.contains(key);
        
        if (isPlayerPlaced) {

            return false;
        }

        boolean result = isNaturallyGeneratedByAlgorithm(world, pos, seed);

        return result;
    }

    private static boolean isNaturallyGeneratedByAlgorithm(ServerLevel world, BlockPos pos, long seed) {
        if (!isNether(world)) {
            return false;
        }
        
        int y = pos.getY();
        if (y < 8 || y > 119) {
            return false;
        }

        return true;
    }

    private static void setTarget(ItemStack stack, ServerLevel world, BlockPos pos) {
        writeBool(stack, KEY_TARGET_LOCKED, true);
        writeInt(stack, KEY_TARGET_X, pos.getX());
        writeInt(stack, KEY_TARGET_Y, pos.getY());
        writeInt(stack, KEY_TARGET_Z, pos.getZ());
        writeInt(stack, KEY_MODEL_TIER, 0);
        writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
        mutateRoot(stack, root -> root.putString(KEY_TARGET_DIM, world.dimension().identifier().toString()));
        removeKey(stack, KEY_FAR_SINCE_MS);
        removeKey(stack, KEY_MISSING_SINCE_MS);
    }

    private static void clearTarget(ItemStack stack) {
        writeBool(stack, KEY_TARGET_LOCKED, false);
        removeKey(stack, KEY_TARGET_X);
        removeKey(stack, KEY_TARGET_Y);
        removeKey(stack, KEY_TARGET_Z);
        removeKey(stack, KEY_TARGET_DIM);
        writeInt(stack, KEY_MODEL_TIER, 0);
        writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
        if (readState(stack) == State.SEARCHING) setModelString(stack, "searching");
    }

    private static BlockPos readTargetPos(ItemStack stack) {
        int x = readInt(stack, KEY_TARGET_X, Integer.MIN_VALUE);
        int y = readInt(stack, KEY_TARGET_Y, Integer.MIN_VALUE);
        int z = readInt(stack, KEY_TARGET_Z, Integer.MIN_VALUE);
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE) return null;
        return new BlockPos(x, y, z);
    }

    private static void updateGuidanceModelIfNeeded(
        ItemStack stack,
        double px,
        double py,
        double pz,
        BlockPos target,
        ServerLevel world,
        Player player,
        boolean playRangeTransitionSound
    ) {
        long nowMs = System.currentTimeMillis();
        long last = readLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
        if (nowMs - last < MODEL_RATE_LIMIT_MS) return;

        double dist = Math.sqrt(target.getCenter().distanceToSqr(px, py, pz));
        int desiredTier = computeTier(dist); // 0=searching,1=too_far,2=far,3=mid,4=close

        int currentTier = readInt(stack, KEY_MODEL_TIER, 0);
        if (currentTier == desiredTier) return;

        switch (desiredTier) {
            case 4 -> setModelString(stack, "on_close");
            case 3 -> setModelString(stack, "on_mid");
            case 2 -> setModelString(stack, "on_far");
            case 1 -> setModelString(stack, "on_too_far");
            default -> setModelString(stack, "searching");
        }

        if (playRangeTransitionSound && currentTier > 0 && desiredTier > 0) {
            if (desiredTier > currentTier) {
                world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.PLAYERS,
                    RANGE_TRANSITION_VOLUME,
                    1.28f
                );
            } else if (desiredTier < currentTier) {
                world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.RESPAWN_ANCHOR_DEPLETE,
                    SoundSource.PLAYERS,
                    RANGE_TRANSITION_VOLUME,
                    0.92f
                );
            }
        }

        writeInt(stack, KEY_MODEL_TIER, desiredTier);
        writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, nowMs);
    }

    private static void playConnectionFeedback(ServerLevel world, Player player) {
        world.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.BEACON_ACTIVATE,
            SoundSource.PLAYERS,
            0.9f,
            1.08f
        );
        double eyeY = player.getEyeY();
        world.sendParticles(ParticleTypes.END_ROD, player.getX(), eyeY, player.getZ(), 18, 0.35, 0.25, 0.35, 0.02);
        world.sendParticles(ParticleTypes.ENCHANT, player.getX(), eyeY, player.getZ(), 20, 0.4, 0.3, 0.4, 0.05);
    }

    private static void playDisconnectFeedback(ServerLevel world, Player player) {
        world.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.BEACON_DEACTIVATE,
            SoundSource.PLAYERS,
            0.9f,
            0.95f
        );
        double eyeY = player.getEyeY();
        world.sendParticles(ParticleTypes.END_ROD, player.getX(), eyeY, player.getZ(), 14, 0.35, 0.25, 0.35, 0.0);
        world.sendParticles(ParticleTypes.SMOKE, player.getX(), eyeY, player.getZ(), 16, 0.35, 0.2, 0.35, 0.01);
    }

    private static int computeTier(double dist) {
        if (dist <= 4.5)  return 4; // close
        if (dist <= 10.5) return 3; // mid
        if (dist <= 16.5) return 2; // far
        if (dist <= 32.5) return 1; // too_far (until RELEASE_DISTANCE)
        return 0; // searching
    }

    private enum TooltipState {
        SEARCHING,
        LOCATED,
        COOLDOWN,
        OFF
    }

    private static TooltipState getTooltipState(ItemStack stack) {
        long now = TimeBridge.nowSeconds();
        long cooldownUntil = readLong(stack, KEY_SCAN_COOLDOWN_UNTIL, 0L);
        if (now < cooldownUntil) {
            return TooltipState.COOLDOWN;
        }

        State state = readState(stack);
        if (state == State.SEARCHING) {
            return readBool(stack, KEY_TARGET_LOCKED, false) ? TooltipState.LOCATED : TooltipState.SEARCHING;
        }
        return TooltipState.OFF;
    }

    private static Component getTooltipDistanceText(ItemStack stack) {
        int tier = readInt(stack, KEY_MODEL_TIER, 1);
        return switch (tier) {
            case 4 -> Component.translatable("item.lategameplus.debris_resonator.tooltip.distance.close");
            case 3 -> Component.translatable("item.lategameplus.debris_resonator.tooltip.distance.medium");
            case 2 -> Component.translatable("item.lategameplus.debris_resonator.tooltip.distance.far");
            default -> Component.translatable("item.lategameplus.debris_resonator.tooltip.distance.very_far");
        };
    }

    private static Component getTooltipChargeText(ItemStack stack) {
        int secs = Math.max(0, calcEffectiveBatteryLive(stack));
        if (secs >= 60) {
            return Component.translatable("item.lategameplus.debris_resonator.tooltip.minutes", Math.round(secs / 60f));
        }
        return Component.translatable("item.lategameplus.debris_resonator.tooltip.seconds", secs);
    }

    private static Component formatTooltipEntry(String key, Component value) {
        return Component.translatable(key, value.copy().withStyle(ChatFormatting.WHITE))
            .withStyle(ChatFormatting.GOLD);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay displayComponent,
        Consumer<Component> textConsumer,
        TooltipFlag type
    ) {
        TooltipState tooltipState = getTooltipState(stack);
        Component stateText = switch (tooltipState) {
            case SEARCHING -> Component.translatable("item.lategameplus.debris_resonator.tooltip.state.searching");
            case LOCATED -> Component.translatable("item.lategameplus.debris_resonator.tooltip.state.located");
            case COOLDOWN -> Component.translatable("item.lategameplus.debris_resonator.tooltip.state.cooldown");
            case OFF -> Component.translatable("item.lategameplus.debris_resonator.tooltip.state.off");
        };
        textConsumer.accept(formatTooltipEntry("item.lategameplus.debris_resonator.tooltip.state", stateText));

        if (tooltipState == TooltipState.LOCATED) {
            textConsumer.accept(formatTooltipEntry(
                "item.lategameplus.debris_resonator.tooltip.distance",
                getTooltipDistanceText(stack)
            ));
        }

        textConsumer.accept(formatTooltipEntry("item.lategameplus.debris_resonator.tooltip.charge", getTooltipChargeText(stack)));
    }

    @Override
    public Component getName(ItemStack stack) {
        return stack.getCustomName() != null ? stack.getHoverName() : Component.translatable(getDescriptionId());
    }



    @Override
    public boolean isBarVisible(ItemStack stack) {
        return calcEffectiveBatteryLive(stack) < maxBatterySeconds();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int bat = calcEffectiveBatteryLive(stack);
        return Math.round(13.0f * bat / (float) Math.max(1, maxBatterySeconds()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, calcEffectiveBatteryLive(stack) / (float) Math.max(1, maxBatterySeconds()));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }



    public static final class DebrisResonatorHooks {
        private static final java.util.Set<String> playerPlacedDebris = new java.util.HashSet<>();
        private static final String PERSISTENCE_FILE = "config/lategameplus_player_debris.json";
        
        public static void init() {
            loadPlayerPlacedDebris();
            
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                DebrisResonatorHooks::savePlayerPlacedDebris, 
                300, 300, java.util.concurrent.TimeUnit.SECONDS
            );
            net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                if (world.isClientSide()) return net.minecraft.world.InteractionResult.PASS;
                
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() == net.minecraft.world.item.Items.ANCIENT_DEBRIS.asItem()) {
                    BlockPos pos = hitResult.getBlockPos().relative(hitResult.getDirection());
                    String key = world.dimension().identifier().toString() + ":" + pos.toShortString();
                    playerPlacedDebris.add(key);
                    savePlayerPlacedDebris();
                }
                return net.minecraft.world.InteractionResult.PASS;
            });
            
            PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                if (world.isClientSide()) return;
                if (!state.is(Blocks.ANCIENT_DEBRIS)) return;
                Inventory inventory = player.getInventory();
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                    ItemStack stack = inventory.getItem(slot);
                    if (!(stack.getItem() instanceof DebrisResonatorItem)) continue;
                    if (readState(stack) != State.SEARCHING) continue;
                    if (readState(stack) != State.SEARCHING) continue;
                    if (!readBool(stack, KEY_TARGET_LOCKED, false)) continue;

                    BlockPos target = readTargetPos(stack);
                    if (target == null) continue;

                    String dim = world.dimension().identifier().toString();
                    String dimNbt = getComp(stack).copyTag().getCompound(ROOT_KEY).flatMap(n -> n.getString(KEY_TARGET_DIM)).orElse("");
                    if (!dim.equals(dimNbt)) continue;
                    if (!target.equals(pos)) continue;

                    commitBatteryFromFloats(stack);
                    clearTarget(stack);
                    clearSoundCycle(player);
                    writeLong(stack, KEY_SCAN_COOLDOWN_UNTIL, TimeBridge.nowSeconds() + cooldownSelfSeconds());
                    setCooldownVisual(stack);

                    playDisconnectFeedback((ServerLevel) world, player);
                    
                    String key = world.dimension().identifier().toString() + ":" + pos.toShortString();
                    if (playerPlacedDebris.remove(key)) {
                        savePlayerPlacedDebris();
                    }
                }
            });
        }
        

        public static void clearPlayerPlacedRegistry() {
            playerPlacedDebris.clear();
        }
 
        public static boolean isPlayerPlaced(ServerLevel world, BlockPos pos) {
            String key = world.dimension().identifier().toString() + ":" + pos.toShortString();
            return playerPlacedDebris.contains(key);
        }

        private static void loadPlayerPlacedDebris() {
            try {
            java.nio.file.Path configPath = java.nio.file.Paths.get(PERSISTENCE_FILE);
            if (!java.nio.file.Files.exists(configPath)) {
                return; 
            }
            
            String jsonContent = java.nio.file.Files.readString(configPath);
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Set<String>>(){}.getType();
            java.util.Set<String> loadedData = gson.fromJson(jsonContent, type);
            
            if (loadedData != null) {
                playerPlacedDebris.addAll(loadedData);
                System.out.println("[LateGamePlus] Loaded " + loadedData.size() + " player-placed Ancient Debris blocks");
            }
            } catch (Exception e) {
            System.err.println("[LateGamePlus] Error loading persistence data: " + e.getMessage());
            }
        }

        private static void savePlayerPlacedDebris() {
            try {
            java.nio.file.Path configPath = java.nio.file.Paths.get(PERSISTENCE_FILE);
            java.nio.file.Files.createDirectories(configPath.getParent());
            
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(playerPlacedDebris);
            java.nio.file.Files.writeString(configPath, jsonContent);
            } catch (Exception e) {
            System.err.println("[LateGamePlus] Error saving persistence data: " + e.getMessage());
            }
        }
        
        public static void shutdown() {
            savePlayerPlacedDebris();
            System.out.println("[LateGamePlus] Ancient Debris data saved on server shutdown");
        }
    }
}
