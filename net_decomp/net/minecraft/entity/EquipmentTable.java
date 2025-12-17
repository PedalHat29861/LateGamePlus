/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

public record EquipmentTable(RegistryKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
    public static final Codec<Map<EquipmentSlot, Float>> SLOT_DROP_CHANCES_CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)Codec.unboundedMap(EquipmentSlot.CODEC, (Codec)Codec.FLOAT)).xmap(either -> (Map)either.map(EquipmentTable::createSlotDropChances, Function.identity()), map -> {
        boolean bl = map.values().stream().distinct().count() == 1L;
        boolean bl2 = map.keySet().containsAll(EquipmentSlot.VALUES);
        if (bl && bl2) {
            return Either.left((Object)map.values().stream().findFirst().orElse(Float.valueOf(0.0f)));
        }
        return Either.right((Object)map);
    });
    public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LootTable.TABLE_KEY.fieldOf("loot_table").forGetter(EquipmentTable::lootTable), (App)SLOT_DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)).apply((Applicative)instance, EquipmentTable::new));

    public EquipmentTable(RegistryKey<LootTable> lootTable, float slotDropChances) {
        this(lootTable, EquipmentTable.createSlotDropChances(slotDropChances));
    }

    private static Map<EquipmentSlot, Float> createSlotDropChances(float dropChance) {
        return EquipmentTable.createSlotDropChances(List.of(EquipmentSlot.values()), dropChance);
    }

    private static Map<EquipmentSlot, Float> createSlotDropChances(List<EquipmentSlot> slots, float dropChance) {
        HashMap map = Maps.newHashMap();
        for (EquipmentSlot equipmentSlot : slots) {
            map.put(equipmentSlot, Float.valueOf(dropChance));
        }
        return map;
    }
}

