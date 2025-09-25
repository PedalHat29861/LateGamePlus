package com.pedalhat.lategameplus.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class DebrisResonatorItem extends Item {

    // Índice que usaremos dentro de las listas del CustomModelData
    private static final int CMD_INDEX = 0;

    public DebrisResonatorItem(Settings settings) {
        super(settings);
    }

    private enum State { OFF, ON, SEARCHING, COOLDOWN }

    private static State readState(ItemStack stack) {
        CustomModelDataComponent cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (cmd == null) return State.OFF;
        String s = cmd.getString(CMD_INDEX); // puede ser null si no hay entrada
        if (s == null) return State.OFF;
        return switch (s) {
            case "on" -> State.ON;
            case "searching" -> State.SEARCHING;
            case "cooldown" -> State.COOLDOWN;
            default -> State.OFF;
        };
    }

    private static void writeState(ItemStack stack, State state) {
        if (state == State.OFF) {
            // Sin componente -> el item model caerá al fallback "off"
            stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
            return;
        }
        String s = switch (state) {
            case ON -> "on";
            case SEARCHING -> "searching";
            case COOLDOWN -> "cooldown";
            default -> "off";
        };
        // Solo poblamos la lista de strings; las demás listas vacías
        List<Float> floats = Collections.emptyList();
        List<Boolean> flags = Collections.emptyList();
        List<String> strings = List.of(s);
        List<Integer> colors = Collections.emptyList();
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                new CustomModelDataComponent(floats, flags, strings, colors));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            State next = switch (readState(stack)) {
                case OFF -> State.ON;
                case ON -> State.SEARCHING;
                case SEARCHING -> State.COOLDOWN;
                case COOLDOWN -> State.OFF;
            };
            writeState(stack, next);
        }
        user.swingHand(hand);
        return ActionResult.SUCCESS;
    }
}
