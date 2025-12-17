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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.collection.ListOperation;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;

public class SetCustomModelDataLootFunction
extends ConditionalLootFunction {
    private static final Codec<LootNumberProvider> COLOR_CODEC = Codec.withAlternative(LootNumberProviderTypes.CODEC, Codecs.RGB, ConstantLootNumberProvider::new);
    public static final MapCodec<SetCustomModelDataLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetCustomModelDataLootFunction.addConditionsField(instance).and(instance.group((App)ListOperation.Values.createCodec(LootNumberProviderTypes.CODEC, Integer.MAX_VALUE).optionalFieldOf("floats").forGetter(lootFunction -> lootFunction.floats), (App)ListOperation.Values.createCodec(Codec.BOOL, Integer.MAX_VALUE).optionalFieldOf("flags").forGetter(lootFunction -> lootFunction.flags), (App)ListOperation.Values.createCodec(Codec.STRING, Integer.MAX_VALUE).optionalFieldOf("strings").forGetter(lootFunction -> lootFunction.strings), (App)ListOperation.Values.createCodec(COLOR_CODEC, Integer.MAX_VALUE).optionalFieldOf("colors").forGetter(lootFunction -> lootFunction.colors))).apply((Applicative)instance, SetCustomModelDataLootFunction::new));
    private final Optional<ListOperation.Values<LootNumberProvider>> floats;
    private final Optional<ListOperation.Values<Boolean>> flags;
    private final Optional<ListOperation.Values<String>> strings;
    private final Optional<ListOperation.Values<LootNumberProvider>> colors;

    public SetCustomModelDataLootFunction(List<LootCondition> conditions, Optional<ListOperation.Values<LootNumberProvider>> floats, Optional<ListOperation.Values<Boolean>> flags, Optional<ListOperation.Values<String>> strings, Optional<ListOperation.Values<LootNumberProvider>> colors) {
        super(conditions);
        this.floats = floats;
        this.flags = flags;
        this.strings = strings;
        this.colors = colors;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Stream.concat(this.floats.stream(), this.colors.stream()).flatMap(operation -> operation.value().stream()).flatMap(value -> value.getAllowedParameters().stream()).collect(Collectors.toSet());
    }

    public LootFunctionType<SetCustomModelDataLootFunction> getType() {
        return LootFunctionTypes.SET_CUSTOM_MODEL_DATA;
    }

    private static <T> List<T> apply(Optional<ListOperation.Values<T>> values, List<T> current) {
        return values.map(operation -> operation.apply(current)).orElse(current);
    }

    private static <T, E> List<E> apply(Optional<ListOperation.Values<T>> values, List<E> current, Function<T, E> operationValueToAppliedValue) {
        return values.map(operation -> {
            List list2 = operation.value().stream().map(operationValueToAppliedValue).toList();
            return operation.operation().apply(current, list2);
        }).orElse(current);
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        CustomModelDataComponent customModelDataComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT);
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(SetCustomModelDataLootFunction.apply(this.floats, customModelDataComponent.floats(), (T provider) -> Float.valueOf(provider.nextFloat(context))), SetCustomModelDataLootFunction.apply(this.flags, customModelDataComponent.flags()), SetCustomModelDataLootFunction.apply(this.strings, customModelDataComponent.strings()), SetCustomModelDataLootFunction.apply(this.colors, customModelDataComponent.colors(), (T provider) -> provider.nextInt(context))));
        return stack;
    }
}

