/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.entity;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class EquipmentSlot
extends Enum<EquipmentSlot>
implements StringIdentifiable {
    public static final /* enum */ EquipmentSlot MAINHAND = new EquipmentSlot(Type.HAND, 0, 0, "mainhand");
    public static final /* enum */ EquipmentSlot OFFHAND = new EquipmentSlot(Type.HAND, 1, 5, "offhand");
    public static final /* enum */ EquipmentSlot FEET = new EquipmentSlot(Type.HUMANOID_ARMOR, 0, 1, 1, "feet");
    public static final /* enum */ EquipmentSlot LEGS = new EquipmentSlot(Type.HUMANOID_ARMOR, 1, 1, 2, "legs");
    public static final /* enum */ EquipmentSlot CHEST = new EquipmentSlot(Type.HUMANOID_ARMOR, 2, 1, 3, "chest");
    public static final /* enum */ EquipmentSlot HEAD = new EquipmentSlot(Type.HUMANOID_ARMOR, 3, 1, 4, "head");
    public static final /* enum */ EquipmentSlot BODY = new EquipmentSlot(Type.ANIMAL_ARMOR, 0, 1, 6, "body");
    public static final /* enum */ EquipmentSlot SADDLE = new EquipmentSlot(Type.SADDLE, 0, 1, 7, "saddle");
    public static final int NO_MAX_COUNT = 0;
    public static final List<EquipmentSlot> VALUES;
    public static final IntFunction<EquipmentSlot> FROM_INDEX;
    public static final StringIdentifiable.EnumCodec<EquipmentSlot> CODEC;
    public static final PacketCodec<ByteBuf, EquipmentSlot> PACKET_CODEC;
    private final Type type;
    private final int entityId;
    private final int maxCount;
    private final int index;
    private final String name;
    private static final /* synthetic */ EquipmentSlot[] field_6176;

    public static EquipmentSlot[] values() {
        return (EquipmentSlot[])field_6176.clone();
    }

    public static EquipmentSlot valueOf(String string) {
        return Enum.valueOf(EquipmentSlot.class, string);
    }

    private EquipmentSlot(Type type, int entityId, int maxCount, int index, String name) {
        this.type = type;
        this.entityId = entityId;
        this.maxCount = maxCount;
        this.index = index;
        this.name = name;
    }

    private EquipmentSlot(Type type, int entityId, int index, String name) {
        this(type, entityId, 0, index, name);
    }

    public Type getType() {
        return this.type;
    }

    public int getEntitySlotId() {
        return this.entityId;
    }

    public int getOffsetEntitySlotId(int offset) {
        return offset + this.entityId;
    }

    public ItemStack split(ItemStack stack) {
        return this.maxCount > 0 ? stack.split(this.maxCount) : stack;
    }

    public int getIndex() {
        return this.index;
    }

    public int getOffsetIndex(int offset) {
        return this.index + offset;
    }

    public String getName() {
        return this.name;
    }

    public boolean isArmorSlot() {
        return this.type == Type.HUMANOID_ARMOR || this.type == Type.ANIMAL_ARMOR;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public boolean increasesDroppedExperience() {
        return this.type != Type.SADDLE;
    }

    public static EquipmentSlot byName(String name) {
        EquipmentSlot equipmentSlot = CODEC.byId(name);
        if (equipmentSlot != null) {
            return equipmentSlot;
        }
        throw new IllegalArgumentException("Invalid slot '" + name + "'");
    }

    private static /* synthetic */ EquipmentSlot[] method_36604() {
        return new EquipmentSlot[]{MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, BODY, SADDLE};
    }

    static {
        field_6176 = EquipmentSlot.method_36604();
        VALUES = List.of(EquipmentSlot.values());
        FROM_INDEX = ValueLists.createIndexToValueFunction(slot -> slot.index, EquipmentSlot.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(EquipmentSlot::values);
        PACKET_CODEC = PacketCodecs.indexed(FROM_INDEX, slot -> slot.index);
    }

    public static final class Type
    extends Enum<Type> {
        public static final /* enum */ Type HAND = new Type();
        public static final /* enum */ Type HUMANOID_ARMOR = new Type();
        public static final /* enum */ Type ANIMAL_ARMOR = new Type();
        public static final /* enum */ Type SADDLE = new Type();
        private static final /* synthetic */ Type[] field_6179;

        public static Type[] values() {
            return (Type[])field_6179.clone();
        }

        public static Type valueOf(String string) {
            return Enum.valueOf(Type.class, string);
        }

        private static /* synthetic */ Type[] method_36605() {
            return new Type[]{HAND, HUMANOID_ARMOR, ANIMAL_ARMOR, SADDLE};
        }

        static {
            field_6179 = Type.method_36605();
        }
    }
}

