/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.component.type;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public record AttributeModifiersComponent(List<Entry> modifiers) {
    public static final AttributeModifiersComponent DEFAULT = new AttributeModifiersComponent(List.of());
    public static final Codec<AttributeModifiersComponent> CODEC = Entry.CODEC.listOf().xmap(AttributeModifiersComponent::new, AttributeModifiersComponent::modifiers);
    public static final PacketCodec<RegistryByteBuf, AttributeModifiersComponent> PACKET_CODEC = PacketCodec.tuple(Entry.PACKET_CODEC.collect(PacketCodecs.toList()), AttributeModifiersComponent::modifiers, AttributeModifiersComponent::new);
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static Builder builder() {
        return new Builder();
    }

    public AttributeModifiersComponent with(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)(this.modifiers.size() + 1));
        for (Entry entry : this.modifiers) {
            if (entry.matches(attribute, modifier.id())) continue;
            builder.add((Object)entry);
        }
        builder.add((Object)new Entry(attribute, modifier, slot));
        return new AttributeModifiersComponent((List<Entry>)builder.build());
    }

    public void applyModifiers(AttributeModifierSlot slot, TriConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier, Display> attributeConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.equals(slot)) continue;
            attributeConsumer.accept(entry.attribute, (Object)entry.modifier, (Object)entry.display);
        }
    }

    public void applyModifiers(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.equals(slot)) continue;
            attributeConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public void applyModifiers(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeConsumer) {
        for (Entry entry : this.modifiers) {
            if (!entry.slot.matches(slot)) continue;
            attributeConsumer.accept(entry.attribute, entry.modifier);
        }
    }

    public double applyOperations(RegistryEntry<EntityAttribute> attribute, double base, EquipmentSlot slot) {
        double d = base;
        for (Entry entry : this.modifiers) {
            if (!entry.slot.matches(slot) || entry.attribute != attribute) continue;
            double e = entry.modifier.value();
            d += (switch (entry.modifier.operation()) {
                default -> throw new MatchException(null, null);
                case EntityAttributeModifier.Operation.ADD_VALUE -> e;
                case EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE -> e * base;
                case EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL -> e * d;
            });
        }
        return d;
    }

    public static class Builder {
        private final ImmutableList.Builder<Entry> entries = ImmutableList.builder();

        Builder() {
        }

        public Builder add(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
            this.entries.add((Object)new Entry(attribute, modifier, slot));
            return this;
        }

        public Builder add(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot, Display display) {
            this.entries.add((Object)new Entry(attribute, modifier, slot, display));
            return this;
        }

        public AttributeModifiersComponent build() {
            return new AttributeModifiersComponent((List<Entry>)this.entries.build());
        }
    }

    public static final class Entry
    extends Record {
        final RegistryEntry<EntityAttribute> attribute;
        final EntityAttributeModifier modifier;
        final AttributeModifierSlot slot;
        final Display display;
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityAttribute.CODEC.fieldOf("type").forGetter(Entry::attribute), (App)EntityAttributeModifier.MAP_CODEC.forGetter(Entry::modifier), (App)AttributeModifierSlot.CODEC.optionalFieldOf("slot", (Object)AttributeModifierSlot.ANY).forGetter(Entry::slot), (App)Display.CODEC.optionalFieldOf("display", (Object)Display.Default.INSTANCE).forGetter(Entry::display)).apply((Applicative)instance, Entry::new));
        public static final PacketCodec<RegistryByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(EntityAttribute.PACKET_CODEC, Entry::attribute, EntityAttributeModifier.PACKET_CODEC, Entry::modifier, AttributeModifierSlot.PACKET_CODEC, Entry::slot, Display.PACKET_CODEC, Entry::display, Entry::new);

        public Entry(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) {
            this(attribute, modifier, slot, Display.getDefault());
        }

        public Entry(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot, Display display) {
            this.attribute = attribute;
            this.modifier = modifier;
            this.slot = slot;
            this.display = display;
        }

        public boolean matches(RegistryEntry<EntityAttribute> attribute, Identifier modifierId) {
            return attribute.equals(this.attribute) && this.modifier.idMatches(modifierId);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "attribute;modifier;slot;display", "attribute", "modifier", "slot", "display"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "attribute;modifier;slot;display", "attribute", "modifier", "slot", "display"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "attribute;modifier;slot;display", "attribute", "modifier", "slot", "display"}, this, object);
        }

        public RegistryEntry<EntityAttribute> attribute() {
            return this.attribute;
        }

        public EntityAttributeModifier modifier() {
            return this.modifier;
        }

        public AttributeModifierSlot slot() {
            return this.slot;
        }

        public Display display() {
            return this.display;
        }
    }

    public static interface Display {
        public static final Codec<Display> CODEC = Type.CODEC.dispatch("type", Display::getType, type -> type.codec);
        public static final PacketCodec<RegistryByteBuf, Display> PACKET_CODEC = Type.PACKET_CODEC.cast().dispatch(Display::getType, Type::getPacketCodec);

        public static Display getDefault() {
            return Default.INSTANCE;
        }

        public static Display getHidden() {
            return Hidden.INSTANCE;
        }

        public static Display createOverride(Text text) {
            return new Override(text);
        }

        public Type getType();

        public void addTooltip(Consumer<Text> var1, @Nullable PlayerEntity var2, RegistryEntry<EntityAttribute> var3, EntityAttributeModifier var4);

        public record Default() implements Display
        {
            static final Default INSTANCE = new Default();
            static final MapCodec<Default> CODEC = MapCodec.unit((Object)INSTANCE);
            static final PacketCodec<RegistryByteBuf, Default> PACKET_CODEC = PacketCodec.unit(INSTANCE);

            @java.lang.Override
            public Type getType() {
                return Type.DEFAULT;
            }

            @java.lang.Override
            public void addTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
                double d = modifier.value();
                boolean bl = false;
                if (player != null) {
                    if (modifier.idMatches(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
                        d += player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE);
                        bl = true;
                    } else if (modifier.idMatches(Item.BASE_ATTACK_SPEED_MODIFIER_ID)) {
                        d += player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED);
                        bl = true;
                    }
                }
                double e = modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE || modifier.operation() == EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? d * 100.0 : (attribute.matches(EntityAttributes.KNOCKBACK_RESISTANCE) ? d * 10.0 : d);
                if (bl) {
                    textConsumer.accept(ScreenTexts.space().append(Text.translatable("attribute.modifier.equals." + modifier.operation().getId(), DECIMAL_FORMAT.format(e), Text.translatable(attribute.value().getTranslationKey()))).formatted(Formatting.DARK_GREEN));
                } else if (d > 0.0) {
                    textConsumer.accept(Text.translatable("attribute.modifier.plus." + modifier.operation().getId(), DECIMAL_FORMAT.format(e), Text.translatable(attribute.value().getTranslationKey())).formatted(attribute.value().getFormatting(true)));
                } else if (d < 0.0) {
                    textConsumer.accept(Text.translatable("attribute.modifier.take." + modifier.operation().getId(), DECIMAL_FORMAT.format(-e), Text.translatable(attribute.value().getTranslationKey())).formatted(attribute.value().getFormatting(false)));
                }
            }
        }

        public record Hidden() implements Display
        {
            static final Hidden INSTANCE = new Hidden();
            static final MapCodec<Hidden> CODEC = MapCodec.unit((Object)INSTANCE);
            static final PacketCodec<RegistryByteBuf, Hidden> PACKET_CODEC = PacketCodec.unit(INSTANCE);

            @java.lang.Override
            public Type getType() {
                return Type.HIDDEN;
            }

            @java.lang.Override
            public void addTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
            }
        }

        public record Override(Text value) implements Display
        {
            static final MapCodec<Override> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("value").forGetter(Override::value)).apply((Applicative)instance, Override::new));
            static final PacketCodec<RegistryByteBuf, Override> PACKET_CODEC = PacketCodec.tuple(TextCodecs.REGISTRY_PACKET_CODEC, Override::value, Override::new);

            @java.lang.Override
            public Type getType() {
                return Type.OVERRIDE;
            }

            @java.lang.Override
            public void addTooltip(Consumer<Text> textConsumer, @Nullable PlayerEntity player, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
                textConsumer.accept(this.value);
            }

            @java.lang.Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Override.class, "component", "value"}, this);
            }

            @java.lang.Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Override.class, "component", "value"}, this);
            }

            @java.lang.Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Override.class, "component", "value"}, this, object);
            }
        }

        public static final class Type
        extends Enum<Type>
        implements StringIdentifiable {
            public static final /* enum */ Type DEFAULT = new Type("default", 0, Default.CODEC, Default.PACKET_CODEC);
            public static final /* enum */ Type HIDDEN = new Type("hidden", 1, Hidden.CODEC, Hidden.PACKET_CODEC);
            public static final /* enum */ Type OVERRIDE = new Type("override", 2, Override.CODEC, Override.PACKET_CODEC);
            static final Codec<Type> CODEC;
            private static final IntFunction<Type> INDEX_MAPPER;
            static final PacketCodec<ByteBuf, Type> PACKET_CODEC;
            private final String id;
            private final int index;
            final MapCodec<? extends Display> codec;
            private final PacketCodec<RegistryByteBuf, ? extends Display> packetCodec;
            private static final /* synthetic */ Type[] field_59749;

            public static Type[] values() {
                return (Type[])field_59749.clone();
            }

            public static Type valueOf(String string) {
                return Enum.valueOf(Type.class, string);
            }

            private Type(String id, int index, MapCodec<? extends Display> codec, PacketCodec<RegistryByteBuf, ? extends Display> packetCodec) {
                this.id = id;
                this.index = index;
                this.codec = codec;
                this.packetCodec = packetCodec;
            }

            @java.lang.Override
            public String asString() {
                return this.id;
            }

            private int getIndex() {
                return this.index;
            }

            private PacketCodec<RegistryByteBuf, ? extends Display> getPacketCodec() {
                return this.packetCodec;
            }

            private static /* synthetic */ Type[] method_70738() {
                return new Type[]{DEFAULT, HIDDEN, OVERRIDE};
            }

            static {
                field_59749 = Type.method_70738();
                CODEC = StringIdentifiable.createCodec(Type::values);
                INDEX_MAPPER = ValueLists.createIndexToValueFunction(Type::getIndex, Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
                PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Type::getIndex);
            }
        }
    }
}

