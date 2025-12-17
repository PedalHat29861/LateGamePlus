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
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.util.context.ContextParameter;

public class LimitCountLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<LimitCountLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> LimitCountLootFunction.addConditionsField(instance).and((App)BoundedIntUnaryOperator.CODEC.fieldOf("limit").forGetter(function -> function.limit)).apply((Applicative)instance, LimitCountLootFunction::new));
    private final BoundedIntUnaryOperator limit;

    private LimitCountLootFunction(List<LootCondition> conditions, BoundedIntUnaryOperator limit) {
        super(conditions);
        this.limit = limit;
    }

    public LootFunctionType<LimitCountLootFunction> getType() {
        return LootFunctionTypes.LIMIT_COUNT;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.limit.getRequiredParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        int i = this.limit.apply(context, stack.getCount());
        stack.setCount(i);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(BoundedIntUnaryOperator limit) {
        return LimitCountLootFunction.builder((List<LootCondition> conditions) -> new LimitCountLootFunction((List<LootCondition>)conditions, limit));
    }
}

