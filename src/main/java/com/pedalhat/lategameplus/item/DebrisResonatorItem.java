package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.util.TimeBridge;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebrisResonatorItem extends Item {

    // ===== Config =====
    private static final int MAX_BATTERY_SECONDS = 900; // 15 min
    private static final int CMD_INDEX = 0;             // strings[0]
    private static final int CMD_F_BASE = 0;            // floats[0] = batería al encender (s)
    private static final int CMD_F_SINCE = 1;           // floats[1] = timestamp inicio (s)
    private static final int CMD_FLAG_LOCKED = 0;       // flags[0] = true si NO permite apagado manual

    // Keys en CUSTOM_DATA (persistencia real de batería)
    private static final String ROOT_KEY    = "lategameplus";
    private static final String KEY_BATTERY = "res_battery_secs";  // int [0..MAX]

    public DebrisResonatorItem(Settings settings) {
        super(settings.maxCount(1)); // no stackeable
    }

    private enum State { OFF, SEARCHING, DEPLETED }

    /* ================== Estado visual (CustomModelData.strings[0]) ================== */

    private static State readState(ItemStack stack) {
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (cmd == null) return State.OFF;
        String s = cmd.getString(CMD_INDEX);
        if (s == null) return State.OFF;
        return switch (s) {
            case "searching" -> State.SEARCHING;
            case "depleted"  -> State.DEPLETED;
            default          -> State.OFF;
        };
    }

    private static void writeState(ItemStack stack, State state) {
        if (state == State.OFF) {
            stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA); // fallback → off
            return;
        }
        String s = (state == State.SEARCHING) ? "searching" : "depleted";
        CustomModelDataComponent prev = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        List<Float> floats = prev != null ? prev.floats() : List.of();
        List<Boolean> flags = prev != null ? prev.flags() : List.of();
        List<Integer> colors = prev != null ? prev.colors() : List.of();
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(floats, flags, List.of(s), colors));
    }

    /* ================== Helpers CUSTOM_DATA ================== */

    private static int readBattery(ItemStack stack) {
        NbtComponent comp = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        var data = comp.copyNbt();
        var rootOpt = data.getCompound(ROOT_KEY);
        if (rootOpt.isEmpty()) return MAX_BATTERY_SECONDS;
        var root = rootOpt.get();
        var v = root.getInt(KEY_BATTERY);
        return v.isPresent() ? MathHelper.clamp(v.get(), 0, MAX_BATTERY_SECONDS) : MAX_BATTERY_SECONDS;
    }

    private static void writeBattery(ItemStack stack, int seconds) {
        int clamped = MathHelper.clamp(seconds, 0, MAX_BATTERY_SECONDS);
        NbtComponent comp = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        var data = comp.copyNbt();
        var root = data.getCompound(ROOT_KEY).orElseGet(net.minecraft.nbt.NbtCompound::new);
        root.putInt(KEY_BATTERY, clamped);
        data.put(ROOT_KEY, root);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
    }

    /* ================== CustomModelData helpers (floats/flags para HUD y lock) ================== */

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
                        List.of(baseSeconds, sinceSeconds),   // floats
                        List.of(locked),                      // flags
                        List.of(stateString),                 // strings
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

    /* ================== Batería en vivo ================== */

    private static int calcEffectiveBatteryLive(ItemStack stack) {
        State st = readState(stack);
        if (st != State.SEARCHING) return readBattery(stack);
        float base = getCmdFloat(stack, CMD_F_BASE, readBattery(stack));
        float since = getCmdFloat(stack, CMD_F_SINCE, TimeBridge.nowSeconds());
        long now = TimeBridge.nowSeconds();
        long elapsed = Math.max(0L, now - (long) since);
        long eff = (long) base - elapsed;
        return (int) Math.max(0L, eff);
    }

    private static void commitBatteryFromFloats(ItemStack stack) {
        int eff = calcEffectiveBatteryLive(stack);
        writeBattery(stack, eff);
        clearCmdFloatsFlagsKeepState(stack);
    }

    /* ================== Interacción ================== */

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            State st = readState(stack);
            if (st == State.OFF) {
                int base = readBattery(stack);
                if (base > 0) {
                    // Activar y BLOQUEAR apagado manual
                    setCmdStateFloatsFlags(stack, "searching", base, (float) TimeBridge.nowSeconds(), true);
                    // Sonido + partículas de encendido
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 0.8f, 1.0f);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANT, user.getX(), user.getBodyY(0.5), user.getZ(),
                            8, 0.2, 0.2, 0.2, 0.0);
                } else {
                    writeState(stack, State.DEPLETED);
                }
            } else if (st == State.SEARCHING) {
                // Si está lockeado, ignora el click y da feedback
                boolean locked = getCmdFlag(stack, CMD_FLAG_LOCKED, true);
                if (locked) {
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 0.6f, 0.8f);
                    ((ServerWorld) world).spawnParticles(ParticleTypes.SMOKE, user.getX(), user.getBodyY(0.5), user.getZ(),
                            5, 0.1, 0.1, 0.1, 0.0);
                    user.sendMessage(Text.translatable("item.lategameplus.debris_resonator.locked").formatted(Formatting.GRAY), true);
                } else {
                    // Ruta futura si lo desbloqueas por minado/distancia:
                    commitBatteryFromFloats(stack);
                    writeState(stack, State.OFF);
                }
            } else {
                // DEPLETED: luego añadiremos recarga
            }
        }
        user.swingHand(hand);
        return ActionResult.SUCCESS;
    }

    /* ================== Tick inventario (server) ================== */

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof PlayerEntity player)) return;

        if (readState(stack) == State.SEARCHING) {
            int eff = calcEffectiveBatteryLive(stack);
            if (eff <= 0) {
                // Consolidar consumo y pasar a DEPLETED
                commitBatteryFromFloats(stack);
                writeState(stack, State.DEPLETED);
                // Feedback de agotado (una sola vez al hacer flip)
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.9f, 0.9f);
                world.spawnParticles(ParticleTypes.POOF, player.getX(), player.getBodyY(0.5), player.getZ(),
                        6, 0.2, 0.2, 0.2, 0.0);
            }
        }
    }

    /* ================== Nombre dinámico (sufijo con tiempo restante) ================== */

    @Override
    public Text getName(ItemStack stack) {
        Text base = stack.getCustomName() != null ? stack.getName() : Text.translatable(getTranslationKey());
        State st = readState(stack);

        if (st == State.SEARCHING) {
            int secs = Math.max(0, calcEffectiveBatteryLive(stack));
            if (secs >= 60) {
                int mins = Math.round(secs / 60f);
                return Text.translatable("item.lategameplus.debris_resonator.searching_minutes", base, mins);
            } else {
                return Text.translatable("item.lategameplus.debris_resonator.searching_seconds", base, secs);
            }
        } else if (st == State.DEPLETED) {
            return Text.translatable("item.lategameplus.debris_resonator.depleted", base);
        }
        return base;
    }

    /* ================== Barra tipo durabilidad ================== */

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return calcEffectiveBatteryLive(stack) < MAX_BATTERY_SECONDS;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int bat = calcEffectiveBatteryLive(stack);
        return Math.round(13.0f * bat / (float) MAX_BATTERY_SECONDS);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, calcEffectiveBatteryLive(stack) / (float) MAX_BATTERY_SECONDS);
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }
}
