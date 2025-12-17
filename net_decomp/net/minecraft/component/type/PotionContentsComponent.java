/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.component.type;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

public record PotionContentsComponent(Optional<RegistryEntry<Potion>> potion, Optional<Integer> customColor, List<StatusEffectInstance> customEffects, Optional<String> customName) implements Consumable,
TooltipAppender
{
    private final List<StatusEffectInstance> customEffects;
    public static final PotionContentsComponent DEFAULT = new PotionContentsComponent(Optional.empty(), Optional.empty(), List.of(), Optional.empty());
    private static final Text NONE_TEXT = Text.translatable("effect.none").formatted(Formatting.GRAY);
    public static final int EFFECTLESS_COLOR = -13083194;
    private static final Codec<PotionContentsComponent> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Potion.CODEC.optionalFieldOf("potion").forGetter(PotionContentsComponent::potion), (App)Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContentsComponent::customColor), (App)StatusEffectInstance.CODEC.listOf().optionalFieldOf("custom_effects", List.of()).forGetter(PotionContentsComponent::customEffects), (App)Codec.STRING.optionalFieldOf("custom_name").forGetter(PotionContentsComponent::customName)).apply((Applicative)instance, PotionContentsComponent::new));
    public static final Codec<PotionContentsComponent> CODEC = Codec.withAlternative(BASE_CODEC, Potion.CODEC, PotionContentsComponent::new);
    public static final PacketCodec<RegistryByteBuf, PotionContentsComponent> PACKET_CODEC = PacketCodec.tuple(Potion.PACKET_CODEC.collect(PacketCodecs::optional), PotionContentsComponent::potion, PacketCodecs.INTEGER.collect(PacketCodecs::optional), PotionContentsComponent::customColor, StatusEffectInstance.PACKET_CODEC.collect(PacketCodecs.toList()), PotionContentsComponent::customEffects, PacketCodecs.STRING.collect(PacketCodecs::optional), PotionContentsComponent::customName, PotionContentsComponent::new);

    public PotionContentsComponent(RegistryEntry<Potion> potion) {
        this(Optional.of(potion), Optional.empty(), List.of(), Optional.empty());
    }

    public static ItemStack createStack(Item item, RegistryEntry<Potion> potion) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion));
        return itemStack;
    }

    public boolean matches(RegistryEntry<Potion> potion) {
        return this.potion.isPresent() && this.potion.get().matches(potion) && this.customEffects.isEmpty();
    }

    public Iterable<StatusEffectInstance> getEffects() {
        if (this.potion.isEmpty()) {
            return this.customEffects;
        }
        if (this.customEffects.isEmpty()) {
            return this.potion.get().value().getEffects();
        }
        return Iterables.concat(this.potion.get().value().getEffects(), this.customEffects);
    }

    public void forEachEffect(Consumer<StatusEffectInstance> effectConsumer, float durationMultiplier) {
        if (this.potion.isPresent()) {
            for (StatusEffectInstance statusEffectInstance : this.potion.get().value().getEffects()) {
                effectConsumer.accept(statusEffectInstance.withScaledDuration(durationMultiplier));
            }
        }
        for (StatusEffectInstance statusEffectInstance : this.customEffects) {
            effectConsumer.accept(statusEffectInstance.withScaledDuration(durationMultiplier));
        }
    }

    public PotionContentsComponent with(RegistryEntry<Potion> potion) {
        return new PotionContentsComponent(Optional.of(potion), this.customColor, this.customEffects, this.customName);
    }

    public PotionContentsComponent with(StatusEffectInstance customEffect) {
        return new PotionContentsComponent(this.potion, this.customColor, Util.withAppended(this.customEffects, customEffect), this.customName);
    }

    public int getColor() {
        return this.getColor(-13083194);
    }

    public int getColor(int defaultColor) {
        if (this.customColor.isPresent()) {
            return this.customColor.get();
        }
        return PotionContentsComponent.mixColors(this.getEffects()).orElse(defaultColor);
    }

    public Text getName(String prefix) {
        String string = this.customName.or(() -> this.potion.map(potionEntry -> ((Potion)potionEntry.value()).getBaseName())).orElse("empty");
        return Text.translatable(prefix + string);
    }

    public static OptionalInt mixColors(Iterable<StatusEffectInstance> effects) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        for (StatusEffectInstance statusEffectInstance : effects) {
            if (!statusEffectInstance.shouldShowParticles()) continue;
            int m = statusEffectInstance.getEffectType().value().getColor();
            int n = statusEffectInstance.getAmplifier() + 1;
            i += n * ColorHelper.getRed(m);
            j += n * ColorHelper.getGreen(m);
            k += n * ColorHelper.getBlue(m);
            l += n;
        }
        if (l == 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(ColorHelper.getArgb(i / l, j / l, k / l));
    }

    public boolean hasEffects() {
        if (!this.customEffects.isEmpty()) {
            return true;
        }
        return this.potion.isPresent() && !this.potion.get().value().getEffects().isEmpty();
    }

    public List<StatusEffectInstance> customEffects() {
        return Lists.transform(this.customEffects, StatusEffectInstance::new);
    }

    public void apply(LivingEntity user, float durationMultiplier) {
        PlayerEntity playerEntity;
        World world = user.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        PlayerEntity playerEntity2 = user instanceof PlayerEntity ? (playerEntity = (PlayerEntity)user) : null;
        this.forEachEffect(effect -> {
            if (effect.getEffectType().value().isInstant()) {
                effect.getEffectType().value().applyInstantEffect(serverWorld, playerEntity2, playerEntity2, user, effect.getAmplifier(), 1.0);
            } else {
                user.addStatusEffect((StatusEffectInstance)effect);
            }
        }, durationMultiplier);
    }

    public static void buildTooltip(Iterable<StatusEffectInstance> effects, Consumer<Text> textConsumer, float durationMultiplier, float tickRate) {
        ArrayList list = Lists.newArrayList();
        boolean bl = true;
        for (StatusEffectInstance statusEffectInstance : effects) {
            bl = false;
            RegistryEntry<StatusEffect> registryEntry = statusEffectInstance.getEffectType();
            int i = statusEffectInstance.getAmplifier();
            registryEntry.value().forEachAttributeModifier(i, (attribute, modifier) -> list.add(new Pair(attribute, modifier)));
            MutableText mutableText = PotionContentsComponent.getEffectText(registryEntry, i);
            if (!statusEffectInstance.isDurationBelow(20)) {
                mutableText = Text.translatable("potion.withDuration", mutableText, StatusEffectUtil.getDurationText(statusEffectInstance, durationMultiplier, tickRate));
            }
            textConsumer.accept(mutableText.formatted(registryEntry.value().getCategory().getFormatting()));
        }
        if (bl) {
            textConsumer.accept(NONE_TEXT);
        }
        if (!list.isEmpty()) {
            textConsumer.accept(ScreenTexts.EMPTY);
            textConsumer.accept(Text.translatable("potion.whenDrank").formatted(Formatting.DARK_PURPLE));
            for (Pair pair : list) {
                EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier)pair.getSecond();
                double d = entityAttributeModifier.value();
                double e = entityAttributeModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE || entityAttributeModifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? entityAttributeModifier.value() * 100.0 : entityAttributeModifier.value();
                if (d > 0.0) {
                    textConsumer.accept(Text.translatable("attribute.modifier.plus." + entityAttributeModifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e), Text.translatable(((EntityAttribute)((RegistryEntry)pair.getFirst()).value()).getTranslationKey())).formatted(Formatting.BLUE));
                    continue;
                }
                if (!(d < 0.0)) continue;
                textConsumer.accept(Text.translatable("attribute.modifier.take." + entityAttributeModifier.operation().getId(), AttributeModifiersComponent.DECIMAL_FORMAT.format(e *= -1.0), Text.translatable(((EntityAttribute)((RegistryEntry)pair.getFirst()).value()).getTranslationKey())).formatted(Formatting.RED));
            }
        }
    }

    public static MutableText getEffectText(RegistryEntry<StatusEffect> effect, int amplifier) {
        MutableText mutableText = Text.translatable(effect.value().getTranslationKey());
        if (amplifier > 0) {
            return Text.translatable("potion.withAmplifier", mutableText, Text.translatable("potion.potency." + amplifier));
        }
        return mutableText;
    }

    @Override
    public void onConsume(World world, LivingEntity user, ItemStack stack, ConsumableComponent consumable) {
        this.apply(user, stack.getOrDefault(DataComponentTypes.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue());
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        PotionContentsComponent.buildTooltip(this.getEffects(), textConsumer, components.getOrDefault(DataComponentTypes.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue(), context.getUpdateTickRate());
    }
}

