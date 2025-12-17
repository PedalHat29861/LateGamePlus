/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerLootComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class SetLootTableLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetLootTableLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetLootTableLootFunction.addConditionsField(instance).and(instance.group((App)LootTable.TABLE_KEY.fieldOf("name").forGetter(function -> function.lootTable), (App)Codec.LONG.optionalFieldOf("seed", (Object)0L).forGetter(function -> function.seed), (App)Registries.BLOCK_ENTITY_TYPE.getEntryCodec().fieldOf("type").forGetter(function -> function.type))).apply((Applicative)instance, SetLootTableLootFunction::new));
    private final RegistryKey<LootTable> lootTable;
    private final long seed;
    private final RegistryEntry<BlockEntityType<?>> type;

    private SetLootTableLootFunction(List<LootCondition> conditions, RegistryKey<LootTable> lootTable, long seed, RegistryEntry<BlockEntityType<?>> blockEntityType) {
        super(conditions);
        this.lootTable = lootTable;
        this.seed = seed;
        this.type = blockEntityType;
    }

    public LootFunctionType<SetLootTableLootFunction> getType() {
        return LootFunctionTypes.SET_LOOT_TABLE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (stack.isEmpty()) {
            return stack;
        }
        stack.set(DataComponentTypes.CONTAINER_LOOT, new ContainerLootComponent(this.lootTable, this.seed));
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        if (!reporter.canUseReferences()) {
            reporter.report(new LootTableReporter.ReferenceNotAllowedError(this.lootTable));
            return;
        }
        if (reporter.getDataLookup().getOptionalEntry(this.lootTable).isEmpty()) {
            reporter.report(new LootTableReporter.MissingElementError(this.lootTable));
        }
    }

    public static ConditionalLootFunction.Builder<?> builder(BlockEntityType<?> type, RegistryKey<LootTable> lootTable) {
        return SetLootTableLootFunction.builder(conditions -> new SetLootTableLootFunction((List<LootCondition>)conditions, lootTable, 0L, (RegistryEntry<BlockEntityType<?>>)type.getRegistryEntry()));
    }

    public static ConditionalLootFunction.Builder<?> builder(BlockEntityType<?> type, RegistryKey<LootTable> lootTable, long seed) {
        return SetLootTableLootFunction.builder(conditions -> new SetLootTableLootFunction((List<LootCondition>)conditions, lootTable, seed, (RegistryEntry<BlockEntityType<?>>)type.getRegistryEntry()));
    }
}

