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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public record SuspiciousStewEffectsComponent(List<StewEffect> effects) implements Consumable,
TooltipAppender
{
    public static final SuspiciousStewEffectsComponent DEFAULT = new SuspiciousStewEffectsComponent(List.of());
    public static final int DEFAULT_DURATION = 160;
    public static final Codec<SuspiciousStewEffectsComponent> CODEC = StewEffect.CODEC.listOf().xmap(SuspiciousStewEffectsComponent::new, SuspiciousStewEffectsComponent::effects);
    public static final PacketCodec<RegistryByteBuf, SuspiciousStewEffectsComponent> PACKET_CODEC = StewEffect.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(SuspiciousStewEffectsComponent::new, SuspiciousStewEffectsComponent::effects);

    public SuspiciousStewEffectsComponent with(StewEffect stewEffect) {
        return new SuspiciousStewEffectsComponent(Util.withAppended(this.effects, stewEffect));
    }

    @Override
    public void onConsume(World world, LivingEntity user, ItemStack stack, ConsumableComponent consumable) {
        for (StewEffect stewEffect : this.effects) {
            user.addStatusEffect(stewEffect.createStatusEffectInstance());
        }
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        if (type.isCreative()) {
            ArrayList<StatusEffectInstance> list = new ArrayList<StatusEffectInstance>();
            for (StewEffect stewEffect : this.effects) {
                list.add(stewEffect.createStatusEffectInstance());
            }
            PotionContentsComponent.buildTooltip(list, textConsumer, 1.0f, context.getUpdateTickRate());
        }
    }

    public record StewEffect(RegistryEntry<StatusEffect> effect, int duration) {
        public static final Codec<StewEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)StatusEffect.ENTRY_CODEC.fieldOf("id").forGetter(StewEffect::effect), (App)Codec.INT.lenientOptionalFieldOf("duration", (Object)160).forGetter(StewEffect::duration)).apply((Applicative)instance, StewEffect::new));
        public static final PacketCodec<RegistryByteBuf, StewEffect> PACKET_CODEC = PacketCodec.tuple(StatusEffect.ENTRY_PACKET_CODEC, StewEffect::effect, PacketCodecs.VAR_INT, StewEffect::duration, StewEffect::new);

        public StatusEffectInstance createStatusEffectInstance() {
            return new StatusEffectInstance(this.effect, this.duration);
        }
    }
}

