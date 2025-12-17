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
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.util.context.ContextParameter;

public class SetCountLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetCountLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetCountLootFunction.addConditionsField(instance).and(instance.group((App)LootNumberProviderTypes.CODEC.fieldOf("count").forGetter(function -> function.countRange), (App)Codec.BOOL.fieldOf("add").orElse((Object)false).forGetter(function -> function.add))).apply((Applicative)instance, SetCountLootFunction::new));
    private final LootNumberProvider countRange;
    private final boolean add;

    private SetCountLootFunction(List<LootCondition> conditions, LootNumberProvider countRange, boolean add) {
        super(conditions);
        this.countRange = countRange;
        this.add = add;
    }

    public LootFunctionType<SetCountLootFunction> getType() {
        return LootFunctionTypes.SET_COUNT;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.countRange.getAllowedParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        int i = this.add ? stack.getCount() : 0;
        stack.setCount(i + this.countRange.nextInt(context));
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider countRange) {
        return SetCountLootFunction.builder((List<LootCondition> list) -> new SetCountLootFunction((List<LootCondition>)list, countRange, false));
    }

    public static ConditionalLootFunction.Builder<?> builder(LootNumberProvider countRange, boolean add) {
        return SetCountLootFunction.builder((List<LootCondition> list) -> new SetCountLootFunction((List<LootCondition>)list, countRange, add));
    }
}

