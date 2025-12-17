/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;

public record DyedColorComponent(int rgb) implements TooltipAppender
{
    public static final Codec<DyedColorComponent> CODEC = Codecs.RGB.xmap(DyedColorComponent::new, DyedColorComponent::rgb);
    public static final PacketCodec<ByteBuf, DyedColorComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, DyedColorComponent::rgb, DyedColorComponent::new);
    public static final int DEFAULT_COLOR = -6265536;

    public static int getColor(ItemStack stack, int defaultColor) {
        DyedColorComponent dyedColorComponent = stack.get(DataComponentTypes.DYED_COLOR);
        return dyedColorComponent != null ? ColorHelper.fullAlpha(dyedColorComponent.rgb()) : defaultColor;
    }

    public static ItemStack setColor(ItemStack stack, List<DyeItem> dyes) {
        int s;
        int p;
        int o;
        if (!stack.isIn(ItemTags.DYEABLE)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = stack.copyWithCount(1);
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int m = 0;
        DyedColorComponent dyedColorComponent = itemStack.get(DataComponentTypes.DYED_COLOR);
        if (dyedColorComponent != null) {
            int n = ColorHelper.getRed(dyedColorComponent.rgb());
            o = ColorHelper.getGreen(dyedColorComponent.rgb());
            p = ColorHelper.getBlue(dyedColorComponent.rgb());
            l += Math.max(n, Math.max(o, p));
            i += n;
            j += o;
            k += p;
            ++m;
        }
        for (DyeItem dyeItem : dyes) {
            p = dyeItem.getColor().getEntityColor();
            int q = ColorHelper.getRed(p);
            int r = ColorHelper.getGreen(p);
            s = ColorHelper.getBlue(p);
            l += Math.max(q, Math.max(r, s));
            i += q;
            j += r;
            k += s;
            ++m;
        }
        int n = i / m;
        o = j / m;
        p = k / m;
        float f = (float)l / (float)m;
        float g = Math.max(n, Math.max(o, p));
        n = (int)((float)n * f / g);
        o = (int)((float)o * f / g);
        p = (int)((float)p * f / g);
        s = ColorHelper.getArgb(0, n, o, p);
        itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(s));
        return itemStack;
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        if (type.isAdvanced()) {
            textConsumer.accept(Text.translatable("item.color", String.format(Locale.ROOT, "#%06X", this.rgb)).formatted(Formatting.GRAY));
        } else {
            textConsumer.accept(Text.translatable("item.dyed").formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }
}

