/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 */
package net.minecraft.component.type;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ChargedProjectilesComponent
implements TooltipAppender {
    public static final ChargedProjectilesComponent DEFAULT = new ChargedProjectilesComponent(List.of());
    public static final Codec<ChargedProjectilesComponent> CODEC = ItemStack.CODEC.listOf().xmap(ChargedProjectilesComponent::new, chargedProjectilesComponent -> chargedProjectilesComponent.projectiles);
    public static final PacketCodec<RegistryByteBuf, ChargedProjectilesComponent> PACKET_CODEC = ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(ChargedProjectilesComponent::new, component -> component.projectiles);
    private final List<ItemStack> projectiles;

    private ChargedProjectilesComponent(List<ItemStack> projectiles) {
        this.projectiles = projectiles;
    }

    public static ChargedProjectilesComponent of(ItemStack projectile) {
        return new ChargedProjectilesComponent(List.of(projectile.copy()));
    }

    public static ChargedProjectilesComponent of(List<ItemStack> projectiles) {
        return new ChargedProjectilesComponent(List.copyOf(Lists.transform(projectiles, ItemStack::copy)));
    }

    public boolean contains(Item item) {
        for (ItemStack itemStack : this.projectiles) {
            if (!itemStack.isOf(item)) continue;
            return true;
        }
        return false;
    }

    public List<ItemStack> getProjectiles() {
        return Lists.transform(this.projectiles, ItemStack::copy);
    }

    public boolean isEmpty() {
        return this.projectiles.isEmpty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChargedProjectilesComponent)) return false;
        ChargedProjectilesComponent chargedProjectilesComponent = (ChargedProjectilesComponent)o;
        if (!ItemStack.stacksEqual(this.projectiles, chargedProjectilesComponent.projectiles)) return false;
        return true;
    }

    public int hashCode() {
        return ItemStack.listHashCode(this.projectiles);
    }

    public String toString() {
        return "ChargedProjectiles[items=" + String.valueOf(this.projectiles) + "]";
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        ItemStack itemStack = null;
        int i = 0;
        for (ItemStack itemStack2 : this.projectiles) {
            if (itemStack == null) {
                itemStack = itemStack2;
                i = 1;
                continue;
            }
            if (ItemStack.areEqual(itemStack, itemStack2)) {
                ++i;
                continue;
            }
            ChargedProjectilesComponent.appendProjectileTooltip(context, textConsumer, itemStack, i);
            itemStack = itemStack2;
            i = 1;
        }
        if (itemStack != null) {
            ChargedProjectilesComponent.appendProjectileTooltip(context, textConsumer, itemStack, i);
        }
    }

    private static void appendProjectileTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, ItemStack projectile, int count) {
        if (count == 1) {
            textConsumer.accept(Text.translatable("item.minecraft.crossbow.projectile.single", projectile.toHoverableText()));
        } else {
            textConsumer.accept(Text.translatable("item.minecraft.crossbow.projectile.multiple", count, projectile.toHoverableText()));
        }
        TooltipDisplayComponent tooltipDisplayComponent = projectile.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
        projectile.appendTooltip(context, tooltipDisplayComponent, null, TooltipType.BASIC, tooltip -> textConsumer.accept(Text.literal("  ").append((Text)tooltip).formatted(Formatting.GRAY)));
    }
}

