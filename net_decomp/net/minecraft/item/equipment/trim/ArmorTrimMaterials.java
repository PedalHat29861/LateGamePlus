/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.equipment.trim;

import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProvidesTrimMaterialComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrimAssets;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ArmorTrimMaterials {
    public static final RegistryKey<ArmorTrimMaterial> QUARTZ = ArmorTrimMaterials.of("quartz");
    public static final RegistryKey<ArmorTrimMaterial> IRON = ArmorTrimMaterials.of("iron");
    public static final RegistryKey<ArmorTrimMaterial> NETHERITE = ArmorTrimMaterials.of("netherite");
    public static final RegistryKey<ArmorTrimMaterial> REDSTONE = ArmorTrimMaterials.of("redstone");
    public static final RegistryKey<ArmorTrimMaterial> COPPER = ArmorTrimMaterials.of("copper");
    public static final RegistryKey<ArmorTrimMaterial> GOLD = ArmorTrimMaterials.of("gold");
    public static final RegistryKey<ArmorTrimMaterial> EMERALD = ArmorTrimMaterials.of("emerald");
    public static final RegistryKey<ArmorTrimMaterial> DIAMOND = ArmorTrimMaterials.of("diamond");
    public static final RegistryKey<ArmorTrimMaterial> LAPIS = ArmorTrimMaterials.of("lapis");
    public static final RegistryKey<ArmorTrimMaterial> AMETHYST = ArmorTrimMaterials.of("amethyst");
    public static final RegistryKey<ArmorTrimMaterial> RESIN = ArmorTrimMaterials.of("resin");

    public static void bootstrap(Registerable<ArmorTrimMaterial> registry) {
        ArmorTrimMaterials.register(registry, QUARTZ, Style.EMPTY.withColor(14931140), ArmorTrimAssets.QUARTZ);
        ArmorTrimMaterials.register(registry, IRON, Style.EMPTY.withColor(0xECECEC), ArmorTrimAssets.IRON);
        ArmorTrimMaterials.register(registry, NETHERITE, Style.EMPTY.withColor(6445145), ArmorTrimAssets.NETHERITE);
        ArmorTrimMaterials.register(registry, REDSTONE, Style.EMPTY.withColor(9901575), ArmorTrimAssets.REDSTONE);
        ArmorTrimMaterials.register(registry, COPPER, Style.EMPTY.withColor(11823181), ArmorTrimAssets.COPPER);
        ArmorTrimMaterials.register(registry, GOLD, Style.EMPTY.withColor(14594349), ArmorTrimAssets.GOLD);
        ArmorTrimMaterials.register(registry, EMERALD, Style.EMPTY.withColor(1155126), ArmorTrimAssets.EMERALD);
        ArmorTrimMaterials.register(registry, DIAMOND, Style.EMPTY.withColor(7269586), ArmorTrimAssets.DIAMOND);
        ArmorTrimMaterials.register(registry, LAPIS, Style.EMPTY.withColor(4288151), ArmorTrimAssets.LAPIS);
        ArmorTrimMaterials.register(registry, AMETHYST, Style.EMPTY.withColor(10116294), ArmorTrimAssets.AMETHYST);
        ArmorTrimMaterials.register(registry, RESIN, Style.EMPTY.withColor(16545810), ArmorTrimAssets.RESIN);
    }

    public static Optional<RegistryEntry<ArmorTrimMaterial>> get(RegistryWrapper.WrapperLookup registries, ItemStack stack) {
        ProvidesTrimMaterialComponent providesTrimMaterialComponent = stack.get(DataComponentTypes.PROVIDES_TRIM_MATERIAL);
        return providesTrimMaterialComponent != null ? providesTrimMaterialComponent.getMaterial(registries) : Optional.empty();
    }

    private static void register(Registerable<ArmorTrimMaterial> registry, RegistryKey<ArmorTrimMaterial> key, Style style, ArmorTrimAssets assets) {
        MutableText text = Text.translatable(Util.createTranslationKey("trim_material", key.getValue())).fillStyle(style);
        registry.register(key, new ArmorTrimMaterial(assets, text));
    }

    private static RegistryKey<ArmorTrimMaterial> of(String id) {
        return RegistryKey.of(RegistryKeys.TRIM_MATERIAL, Identifier.ofVanilla(id));
    }
}

