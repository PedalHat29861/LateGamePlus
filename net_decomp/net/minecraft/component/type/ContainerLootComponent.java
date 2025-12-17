/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;

public record ContainerLootComponent(RegistryKey<LootTable> lootTable, long seed) implements TooltipAppender
{
    private static final Text UNKNOWN_LOOT_TABLE_TOOLTIP_TEXT = Text.translatable("item.container.loot_table.unknown");
    public static final Codec<ContainerLootComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)LootTable.TABLE_KEY.fieldOf("loot_table").forGetter(ContainerLootComponent::lootTable), (App)Codec.LONG.optionalFieldOf("seed", (Object)0L).forGetter(ContainerLootComponent::seed)).apply((Applicative)instance, ContainerLootComponent::new));

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(UNKNOWN_LOOT_TABLE_TOOLTIP_TEXT);
    }
}

