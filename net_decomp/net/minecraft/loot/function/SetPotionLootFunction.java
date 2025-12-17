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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

public class SetPotionLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetPotionLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetPotionLootFunction.addConditionsField(instance).and((App)Potion.CODEC.fieldOf("id").forGetter(function -> function.potion)).apply((Applicative)instance, SetPotionLootFunction::new));
    private final RegistryEntry<Potion> potion;

    private SetPotionLootFunction(List<LootCondition> conditions, RegistryEntry<Potion> potion) {
        super(conditions);
        this.potion = potion;
    }

    public LootFunctionType<SetPotionLootFunction> getType() {
        return LootFunctionTypes.SET_POTION;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT, this.potion, PotionContentsComponent::with);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(RegistryEntry<Potion> potion) {
        return SetPotionLootFunction.builder((List<LootCondition> conditions) -> new SetPotionLootFunction((List<LootCondition>)conditions, potion));
    }
}

