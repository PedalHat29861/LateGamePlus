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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.entry.RegistryEntry;

public class SetItemLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetItemLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetItemLootFunction.addConditionsField(instance).and((App)Item.ENTRY_CODEC.fieldOf("item").forGetter(lootFunction -> lootFunction.item)).apply((Applicative)instance, SetItemLootFunction::new));
    private final RegistryEntry<Item> item;

    private SetItemLootFunction(List<LootCondition> conditions, RegistryEntry<Item> item) {
        super(conditions);
        this.item = item;
    }

    public LootFunctionType<SetItemLootFunction> getType() {
        return LootFunctionTypes.SET_ITEM;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        return stack.withItem(this.item.value());
    }
}

