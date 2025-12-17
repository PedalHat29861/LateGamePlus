/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable.vanilla;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.loottable.LootTableProvider;
import net.minecraft.data.loottable.vanilla.VanillaArchaeologyLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaBarterLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaBlockLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaBrushLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaChargedCreeperLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaChestLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaEntityLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaEquipmentLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaFishingLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaGiftLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaHarvestLootTableGenerator;
import net.minecraft.data.loottable.vanilla.VanillaShearingLootTableGenerator;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryWrapper;

public class VanillaLootTableProviders {
    public static LootTableProvider createVanillaProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        return new LootTableProvider(output, LootTables.getAll(), List.of(new LootTableProvider.LootTypeGenerator(VanillaFishingLootTableGenerator::new, LootContextTypes.FISHING), new LootTableProvider.LootTypeGenerator(VanillaChestLootTableGenerator::new, LootContextTypes.CHEST), new LootTableProvider.LootTypeGenerator(VanillaEntityLootTableGenerator::new, LootContextTypes.ENTITY), new LootTableProvider.LootTypeGenerator(VanillaEquipmentLootTableGenerator::new, LootContextTypes.EQUIPMENT), new LootTableProvider.LootTypeGenerator(VanillaBlockLootTableGenerator::new, LootContextTypes.BLOCK), new LootTableProvider.LootTypeGenerator(VanillaBarterLootTableGenerator::new, LootContextTypes.BARTER), new LootTableProvider.LootTypeGenerator(VanillaGiftLootTableGenerator::new, LootContextTypes.GIFT), new LootTableProvider.LootTypeGenerator(VanillaArchaeologyLootTableGenerator::new, LootContextTypes.ARCHAEOLOGY), new LootTableProvider.LootTypeGenerator(VanillaShearingLootTableGenerator::new, LootContextTypes.SHEARING), new LootTableProvider.LootTypeGenerator(VanillaBrushLootTableGenerator::new, LootContextTypes.ENTITY_INTERACT), new LootTableProvider.LootTypeGenerator(VanillaHarvestLootTableGenerator::new, LootContextTypes.BLOCK_INTERACT), new LootTableProvider.LootTypeGenerator(VanillaChargedCreeperLootTableGenerator::new, LootContextTypes.ENTITY)), registriesFuture);
    }
}

