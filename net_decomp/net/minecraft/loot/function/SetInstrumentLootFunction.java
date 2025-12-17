/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.InstrumentComponent;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public class SetInstrumentLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetInstrumentLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetInstrumentLootFunction.addConditionsField(instance).and((App)TagKey.codec(RegistryKeys.INSTRUMENT).fieldOf("options").forGetter(function -> function.options)).apply((Applicative)instance, SetInstrumentLootFunction::new));
    private final TagKey<Instrument> options;

    private SetInstrumentLootFunction(List<LootCondition> conditions, TagKey<Instrument> options) {
        super(conditions);
        this.options = options;
    }

    public LootFunctionType<SetInstrumentLootFunction> getType() {
        return LootFunctionTypes.SET_INSTRUMENT;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        RegistryWrapper.Impl registry = context.getWorld().getRegistryManager().getOrThrow(RegistryKeys.INSTRUMENT);
        Optional optional = registry.getRandomEntry(this.options, context.getRandom());
        if (optional.isPresent()) {
            stack.set(DataComponentTypes.INSTRUMENT, new InstrumentComponent((RegistryEntry)optional.get()));
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(TagKey<Instrument> options) {
        return SetInstrumentLootFunction.builder((List<LootCondition> conditions) -> new SetInstrumentLootFunction((List<LootCondition>)conditions, options));
    }
}

