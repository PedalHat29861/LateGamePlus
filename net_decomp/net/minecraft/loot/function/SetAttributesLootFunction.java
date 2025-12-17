/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

public class SetAttributesLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<SetAttributesLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetAttributesLootFunction.addConditionsField(instance).and(instance.group((App)Attribute.CODEC.listOf().fieldOf("modifiers").forGetter(function -> function.attributes), (App)Codec.BOOL.optionalFieldOf("replace", (Object)true).forGetter(lootFunction -> lootFunction.replace))).apply((Applicative)instance, SetAttributesLootFunction::new));
    private final List<Attribute> attributes;
    private final boolean replace;

    SetAttributesLootFunction(List<LootCondition> conditions, List<Attribute> attributes, boolean replace) {
        super(conditions);
        this.attributes = List.copyOf(attributes);
        this.replace = replace;
    }

    public LootFunctionType<SetAttributesLootFunction> getType() {
        return LootFunctionTypes.SET_ATTRIBUTES;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return (Set)this.attributes.stream().flatMap(attribute -> attribute.amount.getAllowedParameters().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (this.replace) {
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, this.applyTo(context, AttributeModifiersComponent.DEFAULT));
        } else {
            stack.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT, attributeModifiersComponent -> this.applyTo(context, (AttributeModifiersComponent)attributeModifiersComponent));
        }
        return stack;
    }

    private AttributeModifiersComponent applyTo(LootContext context, AttributeModifiersComponent attributeModifiersComponent) {
        Random random = context.getRandom();
        for (Attribute attribute : this.attributes) {
            AttributeModifierSlot attributeModifierSlot = Util.getRandom(attribute.slots, random);
            attributeModifiersComponent = attributeModifiersComponent.with(attribute.attribute, new EntityAttributeModifier(attribute.id, attribute.amount.nextFloat(context), attribute.operation), attributeModifierSlot);
        }
        return attributeModifiersComponent;
    }

    public static AttributeBuilder attributeBuilder(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amountRange) {
        return new AttributeBuilder(id, attribute, operation, amountRange);
    }

    public static Builder builder() {
        return new Builder();
    }

    static final class Attribute
    extends Record {
        final Identifier id;
        final RegistryEntry<EntityAttribute> attribute;
        final EntityAttributeModifier.Operation operation;
        final LootNumberProvider amount;
        final List<AttributeModifierSlot> slots;
        private static final Codec<List<AttributeModifierSlot>> EQUIPMENT_SLOT_LIST_CODEC = Codecs.nonEmptyList(Codecs.listOrSingle(AttributeModifierSlot.CODEC));
        public static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Identifier.CODEC.fieldOf("id").forGetter(Attribute::id), (App)EntityAttribute.CODEC.fieldOf("attribute").forGetter(Attribute::attribute), (App)EntityAttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Attribute::operation), (App)LootNumberProviderTypes.CODEC.fieldOf("amount").forGetter(Attribute::amount), (App)EQUIPMENT_SLOT_LIST_CODEC.fieldOf("slot").forGetter(Attribute::slots)).apply((Applicative)instance, Attribute::new));

        Attribute(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amount, List<AttributeModifierSlot> slots) {
            this.id = id;
            this.attribute = attribute;
            this.operation = operation;
            this.amount = amount;
            this.slots = slots;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Attribute.class, "id;attribute;operation;amount;slots", "id", "attribute", "operation", "amount", "slots"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Attribute.class, "id;attribute;operation;amount;slots", "id", "attribute", "operation", "amount", "slots"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Attribute.class, "id;attribute;operation;amount;slots", "id", "attribute", "operation", "amount", "slots"}, this, object);
        }

        public Identifier id() {
            return this.id;
        }

        public RegistryEntry<EntityAttribute> attribute() {
            return this.attribute;
        }

        public EntityAttributeModifier.Operation operation() {
            return this.operation;
        }

        public LootNumberProvider amount() {
            return this.amount;
        }

        public List<AttributeModifierSlot> slots() {
            return this.slots;
        }
    }

    public static class AttributeBuilder {
        private final Identifier id;
        private final RegistryEntry<EntityAttribute> attribute;
        private final EntityAttributeModifier.Operation operation;
        private final LootNumberProvider amount;
        private final Set<AttributeModifierSlot> slots = EnumSet.noneOf(AttributeModifierSlot.class);

        public AttributeBuilder(Identifier id, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amount) {
            this.id = id;
            this.attribute = attribute;
            this.operation = operation;
            this.amount = amount;
        }

        public AttributeBuilder slot(AttributeModifierSlot slot) {
            this.slots.add(slot);
            return this;
        }

        public Attribute build() {
            return new Attribute(this.id, this.attribute, this.operation, this.amount, List.copyOf(this.slots));
        }
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final boolean replace;
        private final List<Attribute> attributes = Lists.newArrayList();

        public Builder(boolean replace) {
            this.replace = replace;
        }

        public Builder() {
            this(false);
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        public Builder attribute(AttributeBuilder attribute) {
            this.attributes.add(attribute.build());
            return this;
        }

        @Override
        public LootFunction build() {
            return new SetAttributesLootFunction(this.getConditions(), this.attributes, this.replace);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }
}

