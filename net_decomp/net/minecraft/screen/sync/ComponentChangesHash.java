/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen.sync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;

public record ComponentChangesHash(Map<ComponentType<?>, Integer> addedComponents, Set<ComponentType<?>> removedComponents) {
    public static final PacketCodec<RegistryByteBuf, ComponentChangesHash> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.map(HashMap::new, PacketCodecs.registryValue(RegistryKeys.DATA_COMPONENT_TYPE), PacketCodecs.INTEGER, 256), ComponentChangesHash::addedComponents, PacketCodecs.collection(HashSet::new, PacketCodecs.registryValue(RegistryKeys.DATA_COMPONENT_TYPE), 256), ComponentChangesHash::removedComponents, ComponentChangesHash::new);

    public static ComponentChangesHash fromComponents(ComponentChanges changes, ComponentHasher hasher) {
        ComponentChanges.AddedRemovedPair addedRemovedPair = changes.toAddedRemovedPair();
        IdentityHashMap map = new IdentityHashMap(addedRemovedPair.added().size());
        addedRemovedPair.added().forEach(component -> map.put(component.type(), (Integer)hasher.apply(component)));
        return new ComponentChangesHash(map, addedRemovedPair.removed());
    }

    public boolean hashEquals(ComponentChanges changes, ComponentHasher hasher) {
        ComponentChanges.AddedRemovedPair addedRemovedPair = changes.toAddedRemovedPair();
        if (!addedRemovedPair.removed().equals(this.removedComponents)) {
            return false;
        }
        if (this.addedComponents.size() != addedRemovedPair.added().size()) {
            return false;
        }
        for (Component<?> component : addedRemovedPair.added()) {
            Integer integer = this.addedComponents.get(component.type());
            if (integer == null) {
                return false;
            }
            Integer integer2 = (Integer)hasher.apply(component);
            if (integer2.equals(integer)) continue;
            return false;
        }
        return true;
    }

    @FunctionalInterface
    public static interface ComponentHasher
    extends Function<Component<?>, Integer> {
    }
}

