/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Codec
 */
package net.minecraft.predicate.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public final class ComponentMapPredicate
implements Predicate<ComponentsAccess> {
    public static final Codec<ComponentMapPredicate> CODEC = ComponentType.TYPE_TO_VALUE_MAP_CODEC.xmap(map -> new ComponentMapPredicate(map.entrySet().stream().map(Component::of).collect(Collectors.toList())), predicate -> predicate.components.stream().filter(component -> !component.type().shouldSkipSerialization()).collect(Collectors.toMap(Component::type, Component::value)));
    public static final PacketCodec<RegistryByteBuf, ComponentMapPredicate> PACKET_CODEC = Component.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(ComponentMapPredicate::new, predicate -> predicate.components);
    public static final ComponentMapPredicate EMPTY = new ComponentMapPredicate(List.of());
    private final List<Component<?>> components;

    ComponentMapPredicate(List<Component<?>> components) {
        this.components = components;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static <T> ComponentMapPredicate of(ComponentType<T> type, T value) {
        return new ComponentMapPredicate(List.of(new Component<T>(type, value)));
    }

    public static ComponentMapPredicate of(ComponentMap components) {
        return new ComponentMapPredicate((List<Component<?>>)ImmutableList.copyOf((Iterable)components));
    }

    public static ComponentMapPredicate ofFiltered(ComponentMap components, ComponentType<?> ... types) {
        Builder builder = new Builder();
        for (ComponentType<?> componentType : types) {
            Component<?> component = components.getTyped(componentType);
            if (component == null) continue;
            builder.add(component);
        }
        return builder.build();
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (!(o instanceof ComponentMapPredicate)) return false;
        ComponentMapPredicate componentMapPredicate = (ComponentMapPredicate)o;
        if (!this.components.equals(componentMapPredicate.components)) return false;
        return true;
    }

    public int hashCode() {
        return this.components.hashCode();
    }

    public String toString() {
        return this.components.toString();
    }

    @Override
    public boolean test(ComponentsAccess componentsAccess) {
        for (Component<?> component : this.components) {
            Object object = componentsAccess.get(component.type());
            if (Objects.equals(component.value(), object)) continue;
            return false;
        }
        return true;
    }

    public boolean method_57867() {
        return this.components.isEmpty();
    }

    public ComponentChanges toChanges() {
        ComponentChanges.Builder builder = ComponentChanges.builder();
        for (Component<?> component : this.components) {
            builder.add(component);
        }
        return builder.build();
    }

    @Override
    public /* synthetic */ boolean test(Object components) {
        return this.test((ComponentsAccess)components);
    }

    public static class Builder {
        private final List<Component<?>> components = new ArrayList();

        Builder() {
        }

        public <T> Builder add(Component<T> component) {
            return this.add(component.type(), component.value());
        }

        public <T> Builder add(ComponentType<? super T> type, T value) {
            for (Component<?> component : this.components) {
                if (component.type() != type) continue;
                throw new IllegalArgumentException("Predicate already has component of type: '" + String.valueOf(type) + "'");
            }
            this.components.add(new Component<T>(type, value));
            return this;
        }

        public ComponentMapPredicate build() {
            return new ComponentMapPredicate(List.copyOf(this.components));
        }
    }
}

