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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootEntityValueSource;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Nameable;
import net.minecraft.util.context.ContextParameter;

public class CopyNameLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<CopyNameLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyNameLootFunction.addConditionsField(instance).and((App)LootEntityValueSource.ENTITY_OR_BLOCK_ENTITY_CODEC.fieldOf("source").forGetter(function -> function.source)).apply((Applicative)instance, CopyNameLootFunction::new));
    private final LootEntityValueSource<Object> source;

    private CopyNameLootFunction(List<LootCondition> conditions, LootEntityValueSource<?> source) {
        super(conditions);
        this.source = LootEntityValueSource.cast(source);
    }

    public LootFunctionType<CopyNameLootFunction> getType() {
        return LootFunctionTypes.COPY_NAME;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(this.source.contextParam());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        Object object = this.source.get(context);
        if (object instanceof Nameable) {
            Nameable nameable = (Nameable)object;
            stack.set(DataComponentTypes.CUSTOM_NAME, nameable.getCustomName());
        }
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(LootEntityValueSource<?> source) {
        return CopyNameLootFunction.builder((List<LootCondition> conditions) -> new CopyNameLootFunction((List<LootCondition>)conditions, source));
    }
}

