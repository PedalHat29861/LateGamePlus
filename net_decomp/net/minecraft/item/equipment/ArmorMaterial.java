/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.equipment;

import java.util.Map;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record ArmorMaterial(int durability, Map<EquipmentType, Integer> defense, int enchantmentValue, RegistryEntry<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, RegistryKey<EquipmentAsset> assetId) {
    public AttributeModifiersComponent createAttributeModifiers(EquipmentType equipmentType) {
        int i = this.defense.getOrDefault(equipmentType, 0);
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        AttributeModifierSlot attributeModifierSlot = AttributeModifierSlot.forEquipmentSlot(equipmentType.getEquipmentSlot());
        Identifier identifier = Identifier.ofVanilla("armor." + equipmentType.getName());
        builder.add(EntityAttributes.ARMOR, new EntityAttributeModifier(identifier, i, EntityAttributeModifier.Operation.ADD_VALUE), attributeModifierSlot);
        builder.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(identifier, this.toughness, EntityAttributeModifier.Operation.ADD_VALUE), attributeModifierSlot);
        if (this.knockbackResistance > 0.0f) {
            builder.add(EntityAttributes.KNOCKBACK_RESISTANCE, new EntityAttributeModifier(identifier, this.knockbackResistance, EntityAttributeModifier.Operation.ADD_VALUE), attributeModifierSlot);
        }
        return builder.build();
    }
}

