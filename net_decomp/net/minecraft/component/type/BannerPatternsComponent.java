/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.component.type;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

public final class BannerPatternsComponent
extends Record
implements TooltipAppender {
    final List<Layer> layers;
    static final Logger LOGGER = LogUtils.getLogger();
    public static final BannerPatternsComponent DEFAULT = new BannerPatternsComponent(List.of());
    public static final Codec<BannerPatternsComponent> CODEC = Layer.CODEC.listOf().xmap(BannerPatternsComponent::new, BannerPatternsComponent::layers);
    public static final PacketCodec<RegistryByteBuf, BannerPatternsComponent> PACKET_CODEC = Layer.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(BannerPatternsComponent::new, BannerPatternsComponent::layers);

    public BannerPatternsComponent(List<Layer> layers) {
        this.layers = layers;
    }

    public BannerPatternsComponent withoutTopLayer() {
        return new BannerPatternsComponent(List.copyOf(this.layers.subList(0, this.layers.size() - 1)));
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        for (int i = 0; i < Math.min(this.layers().size(), 6); ++i) {
            textConsumer.accept(this.layers().get(i).getTooltipText().formatted(Formatting.GRAY));
        }
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{BannerPatternsComponent.class, "layers", "layers"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BannerPatternsComponent.class, "layers", "layers"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BannerPatternsComponent.class, "layers", "layers"}, this, object);
    }

    public List<Layer> layers() {
        return this.layers;
    }

    public record Layer(RegistryEntry<BannerPattern> pattern, DyeColor color) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BannerPattern.ENTRY_CODEC.fieldOf("pattern").forGetter(Layer::pattern), (App)DyeColor.CODEC.fieldOf("color").forGetter(Layer::color)).apply((Applicative)instance, Layer::new));
        public static final PacketCodec<RegistryByteBuf, Layer> PACKET_CODEC = PacketCodec.tuple(BannerPattern.ENTRY_PACKET_CODEC, Layer::pattern, DyeColor.PACKET_CODEC, Layer::color, Layer::new);

        public MutableText getTooltipText() {
            String string = this.pattern.value().translationKey();
            return Text.translatable(string + "." + this.color.getId());
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<Layer> entries = ImmutableList.builder();

        @Deprecated
        public Builder add(RegistryEntryLookup<BannerPattern> patternLookup, RegistryKey<BannerPattern> pattern, DyeColor color) {
            Optional<RegistryEntry.Reference<BannerPattern>> optional = patternLookup.getOptional(pattern);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find banner pattern with id: '{}'", (Object)pattern.getValue());
                return this;
            }
            return this.add((RegistryEntry<BannerPattern>)optional.get(), color);
        }

        public Builder add(RegistryEntry<BannerPattern> pattern, DyeColor color) {
            return this.add(new Layer(pattern, color));
        }

        public Builder add(Layer layer) {
            this.entries.add((Object)layer);
            return this;
        }

        public Builder addAll(BannerPatternsComponent patterns) {
            this.entries.addAll(patterns.layers);
            return this;
        }

        public BannerPatternsComponent build() {
            return new BannerPatternsComponent((List<Layer>)this.entries.build());
        }
    }
}

