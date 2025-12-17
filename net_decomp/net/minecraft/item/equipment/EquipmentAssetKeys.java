/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.equipment;

import java.util.Map;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface EquipmentAssetKeys {
    public static final RegistryKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("equipment_asset"));
    public static final RegistryKey<EquipmentAsset> LEATHER = EquipmentAssetKeys.register("leather");
    public static final RegistryKey<EquipmentAsset> COPPER = EquipmentAssetKeys.register("copper");
    public static final RegistryKey<EquipmentAsset> CHAINMAIL = EquipmentAssetKeys.register("chainmail");
    public static final RegistryKey<EquipmentAsset> IRON = EquipmentAssetKeys.register("iron");
    public static final RegistryKey<EquipmentAsset> GOLD = EquipmentAssetKeys.register("gold");
    public static final RegistryKey<EquipmentAsset> DIAMOND = EquipmentAssetKeys.register("diamond");
    public static final RegistryKey<EquipmentAsset> TURTLE_SCUTE = EquipmentAssetKeys.register("turtle_scute");
    public static final RegistryKey<EquipmentAsset> NETHERITE = EquipmentAssetKeys.register("netherite");
    public static final RegistryKey<EquipmentAsset> ARMADILLO_SCUTE = EquipmentAssetKeys.register("armadillo_scute");
    public static final RegistryKey<EquipmentAsset> ELYTRA = EquipmentAssetKeys.register("elytra");
    public static final RegistryKey<EquipmentAsset> SADDLE = EquipmentAssetKeys.register("saddle");
    public static final Map<DyeColor, RegistryKey<EquipmentAsset>> CARPET_FROM_COLOR = Util.mapEnum(DyeColor.class, color -> EquipmentAssetKeys.register(color.asString() + "_carpet"));
    public static final RegistryKey<EquipmentAsset> TRADER_LLAMA = EquipmentAssetKeys.register("trader_llama");
    public static final Map<DyeColor, RegistryKey<EquipmentAsset>> HARNESS_FROM_COLOR = Util.mapEnum(DyeColor.class, color -> EquipmentAssetKeys.register(color.asString() + "_harness"));

    public static RegistryKey<EquipmentAsset> register(String name) {
        return RegistryKey.of(REGISTRY_KEY, Identifier.ofVanilla(name));
    }
}

