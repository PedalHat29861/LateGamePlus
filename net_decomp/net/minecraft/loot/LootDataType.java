/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.loot;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContextAware;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ErrorReporter;

public record LootDataType<T>(RegistryKey<Registry<T>> registryKey, Codec<T> codec, Validator<T> validator) {
    public static final LootDataType<LootCondition> PREDICATES = new LootDataType<LootCondition>(RegistryKeys.PREDICATE, LootCondition.CODEC, LootDataType.simpleValidator());
    public static final LootDataType<LootFunction> ITEM_MODIFIERS = new LootDataType<LootFunction>(RegistryKeys.ITEM_MODIFIER, LootFunctionTypes.CODEC, LootDataType.simpleValidator());
    public static final LootDataType<LootTable> LOOT_TABLES = new LootDataType<LootTable>(RegistryKeys.LOOT_TABLE, LootTable.CODEC, LootDataType.tableValidator());

    public void validate(LootTableReporter reporter, RegistryKey<T> key, T value) {
        this.validator.run(reporter, key, value);
    }

    public static Stream<LootDataType<?>> stream() {
        return Stream.of(PREDICATES, ITEM_MODIFIERS, LOOT_TABLES);
    }

    private static <T extends LootContextAware> Validator<T> simpleValidator() {
        return (reporter, key, value) -> value.validate(reporter.makeChild(new ErrorReporter.LootTableContext(key), key));
    }

    private static Validator<LootTable> tableValidator() {
        return (reporter, key, value) -> value.validate(reporter.withContextType(value.getType()).makeChild(new ErrorReporter.LootTableContext(key), key));
    }

    @FunctionalInterface
    public static interface Validator<T> {
        public void run(LootTableReporter var1, RegistryKey<T> var2, T var3);
    }
}

