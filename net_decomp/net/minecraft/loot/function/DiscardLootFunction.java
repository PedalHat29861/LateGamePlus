/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;

public class DiscardLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<DiscardLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> DiscardLootFunction.addConditionsField(instance).apply((Applicative)instance, DiscardLootFunction::new));

    protected DiscardLootFunction(List<LootCondition> list) {
        super(list);
    }

    public LootFunctionType<DiscardLootFunction> getType() {
        return LootFunctionTypes.DISCARD;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        return ItemStack.EMPTY;
    }

    public static ConditionalLootFunction.Builder<?> builder() {
        return DiscardLootFunction.builder(DiscardLootFunction::new);
    }
}

