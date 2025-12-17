/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public class AttributeContainer {
    private final Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> custom = new Object2ObjectOpenHashMap();
    private final Set<EntityAttributeInstance> tracked = new ObjectOpenHashSet();
    private final Set<EntityAttributeInstance> pendingUpdate = new ObjectOpenHashSet();
    private final DefaultAttributeContainer defaultAttributes;

    public AttributeContainer(DefaultAttributeContainer defaultAttributes) {
        this.defaultAttributes = defaultAttributes;
    }

    private void updateTrackedStatus(EntityAttributeInstance instance) {
        this.pendingUpdate.add(instance);
        if (instance.getAttribute().value().isTracked()) {
            this.tracked.add(instance);
        }
    }

    public Set<EntityAttributeInstance> getTracked() {
        return this.tracked;
    }

    public Set<EntityAttributeInstance> getPendingUpdate() {
        return this.pendingUpdate;
    }

    public Collection<EntityAttributeInstance> getAttributesToSend() {
        return this.custom.values().stream().filter(attribute -> attribute.getAttribute().value().isTracked()).collect(Collectors.toList());
    }

    public @Nullable EntityAttributeInstance getCustomInstance(RegistryEntry<EntityAttribute> attribute2) {
        return this.custom.computeIfAbsent(attribute2, attribute -> this.defaultAttributes.createOverride(this::updateTrackedStatus, (RegistryEntry<EntityAttribute>)attribute));
    }

    public boolean hasAttribute(RegistryEntry<EntityAttribute> attribute) {
        return this.custom.get(attribute) != null || this.defaultAttributes.has(attribute);
    }

    public boolean hasModifierForAttribute(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getModifier(id) != null : this.defaultAttributes.hasModifier(attribute, id);
    }

    public double getValue(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getValue() : this.defaultAttributes.getValue(attribute);
    }

    public double getBaseValue(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getBaseValue() : this.defaultAttributes.getBaseValue(attribute);
    }

    public double getModifierValue(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getModifier(id).value() : this.defaultAttributes.getModifierValue(attribute, id);
    }

    public void addTemporaryModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiersMap) {
        modifiersMap.forEach((attribute, modifier) -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
            if (entityAttributeInstance != null) {
                entityAttributeInstance.removeModifier(modifier.id());
                entityAttributeInstance.addTemporaryModifier((EntityAttributeModifier)modifier);
            }
        });
    }

    public void removeModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiersMap) {
        modifiersMap.asMap().forEach((attribute, modifiers) -> {
            EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
            if (entityAttributeInstance != null) {
                modifiers.forEach(modifier -> entityAttributeInstance.removeModifier(modifier.id()));
            }
        });
    }

    public void setFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(attributeInstance.getAttribute());
            if (entityAttributeInstance != null) {
                entityAttributeInstance.setFrom((EntityAttributeInstance)attributeInstance);
            }
        });
    }

    public void setBaseFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(attributeInstance.getAttribute());
            if (entityAttributeInstance != null) {
                entityAttributeInstance.setBaseValue(attributeInstance.getBaseValue());
            }
        });
    }

    public void addPersistentModifiersFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(attributeInstance.getAttribute());
            if (entityAttributeInstance != null) {
                entityAttributeInstance.addPersistentModifiers(attributeInstance.getPersistentModifiers());
            }
        });
    }

    public boolean resetToBaseValue(RegistryEntry<EntityAttribute> attribute) {
        if (!this.defaultAttributes.has(attribute)) {
            return false;
        }
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.setBaseValue(this.defaultAttributes.getBaseValue(attribute));
        }
        return true;
    }

    public List<EntityAttributeInstance.Packed> pack() {
        ArrayList<EntityAttributeInstance.Packed> list = new ArrayList<EntityAttributeInstance.Packed>(this.custom.values().size());
        for (EntityAttributeInstance entityAttributeInstance : this.custom.values()) {
            list.add(entityAttributeInstance.pack());
        }
        return list;
    }

    public void unpack(List<EntityAttributeInstance.Packed> packedList) {
        for (EntityAttributeInstance.Packed packed : packedList) {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(packed.attribute());
            if (entityAttributeInstance == null) continue;
            entityAttributeInstance.unpack(packed);
        }
    }
}

