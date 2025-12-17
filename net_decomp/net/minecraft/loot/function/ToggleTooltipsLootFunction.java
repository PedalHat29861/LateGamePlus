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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;

public class ToggleTooltipsLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<ToggleTooltipsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ToggleTooltipsLootFunction.addConditionsField(instance).and((App)Codec.unboundedMap(ComponentType.CODEC, (Codec)Codec.BOOL).fieldOf("toggles").forGetter(lootFunction -> lootFunction.toggles)).apply((Applicative)instance, ToggleTooltipsLootFunction::new));
    private final Map<ComponentType<?>, Boolean> toggles;

    private ToggleTooltipsLootFunction(List<LootCondition> conditions, Map<ComponentType<?>, Boolean> toggles) {
        super(conditions);
        this.toggles = toggles;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        stack.apply(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT, tooltipDisplayComponent -> {
            Iterator<Map.Entry<ComponentType<?>, Boolean>> iterator = this.toggles.entrySet().iterator();
            while (iterator.hasNext()) {
                boolean bl;
                Map.Entry<ComponentType<?>, Boolean> entry;
                tooltipDisplayComponent = tooltipDisplayComponent.with(entry.getKey(), !(bl = (entry = iterator.next()).getValue().booleanValue()));
            }
            return tooltipDisplayComponent;
        });
        return stack;
    }

    public LootFunctionType<ToggleTooltipsLootFunction> getType() {
        return LootFunctionTypes.TOGGLE_TOOLTIPS;
    }
}

