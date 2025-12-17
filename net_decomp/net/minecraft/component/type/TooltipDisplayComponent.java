/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ReferenceSortedSets
 */
package net.minecraft.component.type;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import java.util.List;
import java.util.SequencedSet;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record TooltipDisplayComponent(boolean hideTooltip, SequencedSet<ComponentType<?>> hiddenComponents) {
    private static final Codec<SequencedSet<ComponentType<?>>> HIDDEN_COMPONENTS_CODEC = ComponentType.CODEC.listOf().xmap(ReferenceLinkedOpenHashSet::new, List::copyOf);
    public static final Codec<TooltipDisplayComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("hide_tooltip", (Object)false).forGetter(TooltipDisplayComponent::hideTooltip), (App)HIDDEN_COMPONENTS_CODEC.optionalFieldOf("hidden_components", (Object)ReferenceSortedSets.emptySet()).forGetter(TooltipDisplayComponent::hiddenComponents)).apply((Applicative)instance, TooltipDisplayComponent::new));
    public static final PacketCodec<RegistryByteBuf, TooltipDisplayComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, TooltipDisplayComponent::hideTooltip, ComponentType.PACKET_CODEC.collect(PacketCodecs.toCollection(ReferenceLinkedOpenHashSet::new)), TooltipDisplayComponent::hiddenComponents, TooltipDisplayComponent::new);
    public static final TooltipDisplayComponent DEFAULT = new TooltipDisplayComponent(false, (SequencedSet<ComponentType<?>>)ReferenceSortedSets.emptySet());

    public TooltipDisplayComponent with(ComponentType<?> component, boolean hidden) {
        if (this.hiddenComponents.contains(component) == hidden) {
            return this;
        }
        ReferenceLinkedOpenHashSet sequencedSet = new ReferenceLinkedOpenHashSet(this.hiddenComponents);
        if (hidden) {
            sequencedSet.add(component);
        } else {
            sequencedSet.remove(component);
        }
        return new TooltipDisplayComponent(this.hideTooltip, (SequencedSet<ComponentType<?>>)sequencedSet);
    }

    public boolean shouldDisplay(ComponentType<?> component) {
        return !this.hideTooltip && !this.hiddenComponents.contains(component);
    }
}

