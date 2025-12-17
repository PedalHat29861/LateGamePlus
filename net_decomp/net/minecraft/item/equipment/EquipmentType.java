/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.item.equipment;

import com.mojang.serialization.Codec;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.StringIdentifiable;

public final class EquipmentType
extends Enum<EquipmentType>
implements StringIdentifiable {
    public static final /* enum */ EquipmentType HELMET = new EquipmentType(EquipmentSlot.HEAD, 11, "helmet");
    public static final /* enum */ EquipmentType CHESTPLATE = new EquipmentType(EquipmentSlot.CHEST, 16, "chestplate");
    public static final /* enum */ EquipmentType LEGGINGS = new EquipmentType(EquipmentSlot.LEGS, 15, "leggings");
    public static final /* enum */ EquipmentType BOOTS = new EquipmentType(EquipmentSlot.FEET, 13, "boots");
    public static final /* enum */ EquipmentType BODY = new EquipmentType(EquipmentSlot.BODY, 16, "body");
    public static final Codec<EquipmentType> CODEC;
    private final EquipmentSlot equipmentSlot;
    private final String name;
    private final int baseMaxDamage;
    private static final /* synthetic */ EquipmentType[] field_41940;

    public static EquipmentType[] values() {
        return (EquipmentType[])field_41940.clone();
    }

    public static EquipmentType valueOf(String string) {
        return Enum.valueOf(EquipmentType.class, string);
    }

    private EquipmentType(EquipmentSlot equipmentSlot, int baseMaxDamage, String name) {
        this.equipmentSlot = equipmentSlot;
        this.name = name;
        this.baseMaxDamage = baseMaxDamage;
    }

    public int getMaxDamage(int multiplier) {
        return this.baseMaxDamage * multiplier;
    }

    public EquipmentSlot getEquipmentSlot() {
        return this.equipmentSlot;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ EquipmentType[] method_48401() {
        return new EquipmentType[]{HELMET, CHESTPLATE, LEGGINGS, BOOTS, BODY};
    }

    static {
        field_41940 = EquipmentType.method_48401();
        CODEC = StringIdentifiable.createBasicCodec(EquipmentType::values);
    }
}

