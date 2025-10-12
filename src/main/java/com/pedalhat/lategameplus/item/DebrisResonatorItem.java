package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.util.TimeBridge;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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
    private static final int  DEFAULT_COOLDOWN_SELF         = 25;
    private static final int  DEFAULT_COOLDOWN_OTHER        = 10;
    private static final int  DEFAULT_COOLDOWN_FAR          = 60;
    private static final long MODEL_RATE_LIMIT_MS           = 500;

    private static int maxBatterySeconds() {
        int value = ConfigManager.get().debrisResonatorMaxBatterySeconds;
        if (value < 0) {
            value = DEFAULT_MAX_BATTERY_SECONDS;
        }
        return MathHelper.clamp(value, 0, 24 * 60 * 60);
    }

    private static int cooldownSelfSeconds() {
        int value = ConfigManager.get().debrisResonatorCooldownSelfSeconds;
        if (value < 0) {
            value = DEFAULT_COOLDOWN_SELF;
        }
        return MathHelper.clamp(value, 0, 3600);
    }

    private static int cooldownOtherSeconds() {
        int value = ConfigManager.get().debrisResonatorCooldownOtherSeconds;
        if (value < 0) {
            value = DEFAULT_COOLDOWN_OTHER;
        }
        return MathHelper.clamp(value, 0, 3600);
    }

    private static int cooldownFarSeconds() {
        int value = ConfigManager.get().debrisResonatorCooldownFarSeconds;
        if (value < 0) {
            value = DEFAULT_COOLDOWN_FAR;
        }
        return MathHelper.clamp(value, 0, 3600);
    }

    private static int rangeY() {
        int value = ConfigManager.get().debrisResonatorRangeY;
        if (value < 0) {
            value = DEFAULT_RANGE_Y;
        }
        return MathHelper.clamp(value, 0, 64);
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

    private static void clearSoundCycle(PlayerEntity player) {
        UUID playerId = player.getUuid();
        soundCycleCache.remove(playerId);
        nextScanCache.remove(playerId);
        lastPingTierCache.remove(playerId);
    }

    public DebrisResonatorItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private enum State { OFF, SEARCHING, DEPLETED }

    

    private static State readState(ItemStack stack) {
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
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
        CustomModelDataComponent prev = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        List<Float> floats = prev != null ? prev.floats()  : List.of();
        List<Boolean> flags = prev != null ? prev.flags()  : List.of();
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(floats, flags, List.of(modelKey), colors));
    }

    private static void writeState(ItemStack stack, State state) {
        if (state == State.OFF) {
            stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
            return;
        }
        setModelString(stack, state == State.SEARCHING ? "searching" : "depleted");
    }

    private static void setCooldownVisual(ItemStack stack) {
        setModelString(stack, "cooldown");
    }

    private static String getModelStateString(ItemStack stack) {
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (cmd == null) return "off";
        String s = cmd.getString(CMD_INDEX);
        return s != null ? s : "off";
    }



    private static NbtComponent getComp(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
    }

    private static void mutateRoot(ItemStack stack, Consumer<net.minecraft.nbt.NbtCompound> updater) {
        NbtComponent comp = getComp(stack);
        net.minecraft.nbt.NbtCompound data = comp.copyNbt();
        net.minecraft.nbt.NbtCompound root = data.getCompound(ROOT_KEY).orElseGet(net.minecraft.nbt.NbtCompound::new);
        updater.accept(root);
        data.put(ROOT_KEY, root);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
    }

    private static int readInt(ItemStack stack, String key, int def) {
        var data = getComp(stack).copyNbt();
        var rootOpt = data.getCompound(ROOT_KEY);
        if (rootOpt.isEmpty()) return def;
        var v = rootOpt.get().getInt(key);
        return v.isPresent() ? v.get() : def;
    }

    private static long readLong(ItemStack stack, String key, long def) {
        var data = getComp(stack).copyNbt();
        var rootOpt = data.getCompound(ROOT_KEY);
        if (rootOpt.isEmpty()) return def;
        var v = rootOpt.get().getLong(key);
        return v.isPresent() ? v.get() : def;
    }

    private static boolean readBool(ItemStack stack, String key, boolean def) {
        var data = getComp(stack).copyNbt();
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
        writeInt(stack, KEY_BATTERY, MathHelper.clamp(seconds, 0, maxBatterySeconds()));
    }



    private static float getCmdFloat(ItemStack stack, int idx, float def) {
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (cmd == null) return def;
        List<Float> fs = cmd.floats();
        if (fs == null || fs.size() <= idx) return def;
        Float f = fs.get(idx);
        return f != null ? f : def;
    }

    private static boolean getCmdFlag(ItemStack stack, int idx, boolean def) {
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (cmd == null) return def;
        List<Boolean> fl = cmd.flags();
        if (fl == null || fl.size() <= idx) return def;
        Boolean b = fl.get(idx);
        return b != null ? b : def;
    }

    private static void setCmdStateFloatsFlags(ItemStack stack, String stateString, float baseSeconds, float sinceSeconds, boolean locked) {
        CustomModelDataComponent prev = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(
                        List.of(baseSeconds, sinceSeconds),
                        List.of(locked),
                        List.of(stateString),
                        colors
                ));
    }

    private static void clearCmdFloatsFlagsKeepState(ItemStack stack) {
        CustomModelDataComponent prev = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        String s = (prev != null && prev.getString(CMD_INDEX) != null) ? prev.getString(CMD_INDEX) : "off";
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(List.of(), List.of(), List.of(s), colors));
    }



    private static int calcEffectiveBatteryLive(ItemStack stack) {
        State st = readState(stack);
        if (st != State.SEARCHING) return readBattery(stack);
        float base  = getCmdFloat(stack, CMD_F_BASE,  readBattery(stack));
        float since = getCmdFloat(stack, CMD_F_SINCE, TimeBridge.nowSeconds());
        long now = TimeBridge.nowSeconds();
        long elapsed = Math.max(0L, now - (long) since);
        
        // Fix para salto temporal anormal (mundo recargado)
        // Si el tiempo transcurrido es mayor que el tiempo base + margen, resetear
        if (elapsed > base + 60) { // 60 segundos de margen
            // Auto-corregir: usar la batería guardada como referencia
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
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            long now = TimeBridge.nowSeconds();
            State st = readState(stack);

            if (st == State.OFF) {
                long cdUntil = readLong(stack, KEY_SCAN_COOLDOWN_UNTIL, 0L);
                if (now < cdUntil) {
                    long remainingSecs = cdUntil - now;
                    Text cooldownMessage = (remainingSecs >= 60)
                            ? Text.translatable("item.lategameplus.debris_resonator.cooldown_message_minutes", Math.round(remainingSecs / 60f))
                            : Text.translatable("item.lategameplus.debris_resonator.cooldown_message_seconds", remainingSecs);
                    user.sendMessage(cooldownMessage.copy().formatted(Formatting.GRAY), true);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 0.6f, 0.8f);
                    return ActionResult.SUCCESS;
                }
                int base = readBattery(stack);
                if (base > 0) {
                    setCmdStateFloatsFlags(stack, "searching", base, (float) now, true);
                    ensureSoundCycleStart(stack, (ServerWorld) world, user);
                    writeBool(stack, KEY_TARGET_LOCKED, false);
                    UUID userId = user.getUuid();
                    nextScanCache.put(userId, now);
                    lastPingTierCache.put(userId, 0);
                    writeInt(stack, KEY_MODEL_TIER, 0);
                    writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
                    removeKey(stack, KEY_FAR_SINCE_MS);
                    removeKey(stack, KEY_MISSING_SINCE_MS);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 0.8f, 1.0f);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANT, user.getX(), user.getBodyY(0.5), user.getZ(),
                            8, 0.2, 0.2, 0.2, 0.0);
                } else {
                    writeState(stack, State.DEPLETED);
                }
            } else if (st == State.SEARCHING) {
                if (getCmdFlag(stack, CMD_FLAG_LOCKED, true)) {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 0.6f, 0.8f);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getBodyY(0.5), user.getZ(),
                            5, 0.1, 0.1, 0.1, 0.0);
                    user.sendMessage(Text.translatable("item.lategameplus.debris_resonator.locked").formatted(Formatting.GRAY), true);
                }
            }
        }
        user.swingHand(hand);
        return ActionResult.SUCCESS;
    }



    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof PlayerEntity player)) return;

        // Verificar si está en cooldown y ya terminó
        State currentState = readState(stack);
        if (currentState == State.OFF) {
            long now = TimeBridge.nowSeconds();
            long cdUntil = readLong(stack, KEY_SCAN_COOLDOWN_UNTIL, 0L);
            // Si estaba en cooldown pero ya terminó, cambiar a estado apagado normal
            if (cdUntil > 0L && now >= cdUntil) {
                removeKey(stack, KEY_SCAN_COOLDOWN_UNTIL);
                stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA); // Estado OFF normal
            }
        }

        if (readState(stack) == State.SEARCHING) {
            // Verificar si hay datos de floats válidos para cálculo dinámico
            float base = getCmdFloat(stack, CMD_F_BASE, -1);
            float since = getCmdFloat(stack, CMD_F_SINCE, -1);
            
            // Si los floats están corruptos o faltantes, auto-corregir
            if (base < 0 || since < 0) {
                // Recuperar usando la batería guardada como base
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
                        SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.9f, 0.9f);
                world.spawnParticles(ParticleTypes.POOF, player.getX(), player.getBodyY(0.5), player.getZ(),
                        6, 0.2, 0.2, 0.2, 0.0);
                return;
            }
        } else {
            return;
        }

        long now = TimeBridge.nowSeconds();


        boolean locked = readBool(stack, KEY_TARGET_LOCKED, false);
        if (!locked) {
            if (!isNether(world)) { geigerPing(stack, world, player, 0); return; }
            
            UUID playerId = player.getUuid();
            long nextScanDeciseconds = nextScanCache.getOrDefault(playerId, 0L);
            long nowDeciseconds = now * 10; // Convertir segundos a décimas de segundo
            long scanPeriodDeciseconds = (long)(SCAN_PERIOD_SECONDS * 10); // 0.5 segundos = 5 décimas
            
            // Validación de salto temporal
            if (nextScanDeciseconds > nowDeciseconds + scanPeriodDeciseconds + 100) { // 10 segundos en décimas
                nextScanDeciseconds = 0L;
            }
            
            if (nowDeciseconds < nextScanDeciseconds) { geigerPing(stack, world, player, 0); return; }
            nextScanCache.put(playerId, nowDeciseconds + scanPeriodDeciseconds);

            BlockPos origin = player.getBlockPos();
            long seed = world.getSeed();
            BlockPos found = scanForDebris(world, origin, seed);
            if (found != null) {
                setTarget(stack, world, found);
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 0.6f, 1.2f);
                updateGuidanceModelIfNeeded(stack, player.getX(), player.getY(), player.getZ(), found);
            } else {
                geigerPing(stack, world, player, 0);
            }
            return;
        }

        BlockPos target = readTargetPos(stack);
        if (target == null) {
            writeBool(stack, KEY_TARGET_LOCKED, false);
            geigerPing(stack, world, player, 0);
            return;
        }


        updateGuidanceModelIfNeeded(stack, player.getX(), player.getY(), player.getZ(), target);


        int tier = computeTier(player.getPos().distanceTo(target.toCenterPos()));
        
        geigerPing(stack, world, player, tier);


        boolean isStillThere = world.getBlockState(target).isOf(Blocks.ANCIENT_DEBRIS);
        long nowMs = System.currentTimeMillis();

        if (!isStillThere) {
            long since = readLong(stack, KEY_MISSING_SINCE_MS, 0L);
            if (since == 0L) {
                writeLong(stack, KEY_MISSING_SINCE_MS, nowMs);
            } else if (nowMs - since >= MISSING_GRACE_MS) {
                // Desapareció por otros → cooldown 15s
                liberateToCooldown(stack, world, player, cooldownOtherSeconds());
            }
        } else {
            removeKey(stack, KEY_MISSING_SINCE_MS);
        }

        // Liberación por distancia (con gracia)
        double dist = player.getPos().distanceTo(target.toCenterPos());
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

    private static void liberateToCooldown(ItemStack stack, ServerWorld world, PlayerEntity player, int seconds) {
        // Confirmar el estado actual de la batería antes de entrar en cooldown
        commitBatteryFromFloats(stack);
        clearTarget(stack);
        clearSoundCycle(player);
        writeLong(stack, KEY_SCAN_COOLDOWN_UNTIL, TimeBridge.nowSeconds() + seconds);
        setCooldownVisual(stack); // modelo "cooldown", estado lógico OFF
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.7f, 0.7f);
        world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getBodyY(0.5), player.getZ(),
                6, 0.2, 0.2, 0.2, 0.0);
    }

    private static boolean isNether(ServerWorld world) {
        return world.getRegistryKey() == World.NETHER;
    }

    private static BlockPos scanForDebris(ServerWorld world, BlockPos origin, long worldSeed) {
        int verticalRange = rangeY();
        for (int dy = -verticalRange; dy <= verticalRange; dy++) {
            for (int dx = -RANGE_XZ; dx <= RANGE_XZ; dx++) {
                for (int dz = -RANGE_XZ; dz <= RANGE_XZ; dz++) {
                    BlockPos p = origin.add(dx, dy, dz);
                    if (!world.getBlockState(p).isOf(Blocks.ANCIENT_DEBRIS)) continue;
                    if (!isTrackable(world, p, worldSeed)) continue;
                    return p;
                }
            }
        }
        return null;
    }

    private static boolean isTrackable(ServerWorld world, BlockPos pos, long seed) {
        // Verificar si es natural usando el algoritmo de generación de Minecraft
        boolean isNatural = isNaturallyGenerated(world, pos, seed);

        
        if (!isNatural) {
            return false;
        }
        
        long dimHash = world.getRegistryKey().getValue().hashCode();
        long h = seed
                ^ (dimHash * 0x9E3779B97F4A7C15L)
                ^ (pos.getX() * 73428767L)
                ^ (pos.getY() * 912367L)
                ^ (pos.getZ() * 137L);
        long v = (h >>> 1) % 100L;
        boolean trackable = v < 50L;
        

        return trackable;
    }
    
    /**
     * Verifica si un Ancient Debris fue generado naturalmente.
     * Combina verificación de registro de jugadores y algoritmo de generación.
     */
    private static boolean isNaturallyGenerated(ServerWorld world, BlockPos pos, long seed) {
        // Primero verificar si está en el registro de bloques colocados por jugadores
        String key = world.getRegistryKey().getValue().toString() + ":" + pos.toShortString();
        boolean isPlayerPlaced = DebrisResonatorHooks.playerPlacedDebris.contains(key);
        
        if (isPlayerPlaced) {

            return false; // Fue colocado por un jugador
        }
        
        // Si no está en el registro de jugadores y estamos en el Nether con altura válida,
        // asumir que es natural (la mayoría de casos)
        boolean result = isNaturallyGeneratedByAlgorithm(world, pos, seed);

        return result;
    }
    
    /**
     * Verifica si un Ancient Debris fue generado naturalmente.
     * Enfoque simplificado: Si está en el Nether en altura válida y no fue colocado por jugador, es natural.
     */
    private static boolean isNaturallyGeneratedByAlgorithm(ServerWorld world, BlockPos pos, long seed) {
        // Ancient Debris solo se genera naturalmente en el Nether
        if (!isNether(world)) {
            return false;
        }
        
        // Verificar rango de altura válido para generación natural
        int y = pos.getY();
        if (y < 8 || y > 119) {
            return false;
        }
        
        // Si llegamos aquí y no está en el registro de jugadores, asumimos que es natural
        // Este enfoque es más permisivo pero efectivo para prevenir exploits
        return true;
    }

    private static void setTarget(ItemStack stack, ServerWorld world, BlockPos pos) {
        writeBool(stack, KEY_TARGET_LOCKED, true);
        writeInt(stack, KEY_TARGET_X, pos.getX());
        writeInt(stack, KEY_TARGET_Y, pos.getY());
        writeInt(stack, KEY_TARGET_Z, pos.getZ());
        writeInt(stack, KEY_MODEL_TIER, 0);
        writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
        mutateRoot(stack, root -> root.putString(KEY_TARGET_DIM, world.getRegistryKey().getValue().toString()));
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

    private static void updateGuidanceModelIfNeeded(ItemStack stack, double px, double py, double pz, BlockPos target) {
        long nowMs = System.currentTimeMillis();
        long last = readLong(stack, KEY_LAST_MODEL_UPDATE_MS, 0L);
        if (nowMs - last < MODEL_RATE_LIMIT_MS) return;

        double dist = Math.sqrt(target.toCenterPos().squaredDistanceTo(px, py, pz));
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
        writeInt(stack, KEY_MODEL_TIER, desiredTier);
        writeLong(stack, KEY_LAST_MODEL_UPDATE_MS, nowMs);
    }

    private static int computeTier(double dist) {
        if (dist <= 4.5)  return 4; // close
        if (dist <= 10.5) return 3; // mid
        if (dist <= 16.5) return 2; // far
        if (dist <= 32.5) return 1; // too_far (hasta RELEASE_DISTANCE)
        return 0; // searching
    }


    private static void ensureSoundCycleStart(ItemStack stack, ServerWorld world, PlayerEntity player) {
        // Los sonidos ahora se manejan automáticamente por AnimationSoundSynchronizer en el cliente
        // basándose en las animaciones CustomModelData
    }
    private static void geigerPing(ItemStack stack, ServerWorld world, PlayerEntity player, int tier) {
        // Los sonidos ahora se manejan completamente por el AnimationSoundSynchronizer del lado cliente
        // No reproducir sonidos desde el servidor para evitar conflictos
        // La sincronización se basa en las animaciones CustomModelData automáticamente
    }



    @Override
    public Text getName(ItemStack stack) {
        Text base = stack.getCustomName() != null ? stack.getName() : Text.translatable(getTranslationKey());
        State st = readState(stack);

        if (st == State.SEARCHING) {
            int secs = Math.max(0, calcEffectiveBatteryLive(stack));
            Text timeText = (secs >= 60)
                    ? Text.translatable("item.lategameplus.debris_resonator.searching_minutes_suffix", Math.round(secs / 60f))
                    : Text.translatable("item.lategameplus.debris_resonator.searching_seconds_suffix", secs);
            return Text.empty().append(base).append(" ").append(timeText.copy().formatted(Formatting.GRAY, Formatting.ITALIC));
        } else if (st == State.DEPLETED) {
            Text suffix = Text.translatable("item.lategameplus.debris_resonator.depleted_suffix");
            return Text.empty().append(base).append(" ").append(suffix.copy().formatted(Formatting.GRAY, Formatting.ITALIC));
        } else if (st == State.OFF) {
            // Check if it's in cooldown
            long now = TimeBridge.nowSeconds();
            long cdUntil = readLong(stack, KEY_SCAN_COOLDOWN_UNTIL, 0L);
            if (now < cdUntil) {
                long remainingSecs = cdUntil - now;
                Text cooldownText = (remainingSecs >= 60)
                        ? Text.translatable("item.lategameplus.debris_resonator.cooldown_minutes_suffix", Math.round(remainingSecs / 60f))
                        : Text.translatable("item.lategameplus.debris_resonator.cooldown_seconds_suffix", remainingSecs);
                return Text.empty().append(base).append(" ").append(cooldownText.copy().formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
        }
        return base;
    }



    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return calcEffectiveBatteryLive(stack) < maxBatterySeconds();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int bat = calcEffectiveBatteryLive(stack);
        return Math.round(13.0f * bat / (float) Math.max(1, maxBatterySeconds()));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, calcEffectiveBatteryLive(stack) / (float) Math.max(1, maxBatterySeconds()));
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
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
                if (world.isClient()) return net.minecraft.util.ActionResult.PASS;
                
                ItemStack stack = player.getStackInHand(hand);
                if (stack.getItem() == net.minecraft.item.Items.ANCIENT_DEBRIS.asItem()) {
                    BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());
                    String key = world.getRegistryKey().getValue().toString() + ":" + pos.toShortString();
                    playerPlacedDebris.add(key);
                    savePlayerPlacedDebris();
                }
                return net.minecraft.util.ActionResult.PASS;
            });
            
            PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
                if (world.isClient()) return;
                if (!state.isOf(Blocks.ANCIENT_DEBRIS)) return;
                PlayerInventory inventory = player.getInventory();
                for (int slot = 0; slot < inventory.size(); slot++) {
                    ItemStack stack = inventory.getStack(slot);
                    if (!(stack.getItem() instanceof DebrisResonatorItem)) continue;
                    if (readState(stack) != State.SEARCHING) continue;
                    if (readState(stack) != State.SEARCHING) continue;
                    if (!readBool(stack, KEY_TARGET_LOCKED, false)) continue;

                    BlockPos target = readTargetPos(stack);
                    if (target == null) continue;

                    String dim = world.getRegistryKey().getValue().toString();
                    String dimNbt = getComp(stack).copyNbt().getCompound(ROOT_KEY).flatMap(n -> n.getString(KEY_TARGET_DIM)).orElse("");
                    if (!dim.equals(dimNbt)) continue;
                    if (!target.equals(pos)) continue;

                    commitBatteryFromFloats(stack);
                    clearTarget(stack);
                    clearSoundCycle(player);
                    writeLong(stack, KEY_SCAN_COOLDOWN_UNTIL, TimeBridge.nowSeconds() + cooldownSelfSeconds());
                    setCooldownVisual(stack);

                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS, 0.7f, 1.1f);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                            8, 0.2, 0.2, 0.2, 0.0);
                    
                    String key = world.getRegistryKey().getValue().toString() + ":" + pos.toShortString();
                    if (playerPlacedDebris.remove(key)) {
                        savePlayerPlacedDebris();
                    }
                }
            });
        }
        

        public static void clearPlayerPlacedRegistry() {
            playerPlacedDebris.clear();
        }
 
        public static boolean isPlayerPlaced(ServerWorld world, BlockPos pos) {
            String key = world.getRegistryKey().getValue().toString() + ":" + pos.toShortString();
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
