/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record BeesComponent(List<BeehiveBlockEntity.BeeData> bees) implements TooltipAppender
{
    public static final Codec<BeesComponent> CODEC = BeehiveBlockEntity.BeeData.LIST_CODEC.xmap(BeesComponent::new, BeesComponent::bees);
    public static final PacketCodec<RegistryByteBuf, BeesComponent> PACKET_CODEC = BeehiveBlockEntity.BeeData.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(BeesComponent::new, BeesComponent::bees);
    public static final BeesComponent DEFAULT = new BeesComponent(List.of());

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable("container.beehive.bees", this.bees.size(), 3).formatted(Formatting.GRAY));
    }
}

