/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 */
package net.minecraft.screen.sync;

import com.mojang.datafixers.DataFixUtils;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.sync.ComponentChangesHash;

public interface ItemStackHash {
    public static final ItemStackHash EMPTY = new ItemStackHash(){

        public String toString() {
            return "<empty>";
        }

        @Override
        public boolean hashEquals(ItemStack stack, ComponentChangesHash.ComponentHasher hasher) {
            return stack.isEmpty();
        }
    };
    public static final PacketCodec<RegistryByteBuf, ItemStackHash> PACKET_CODEC = PacketCodecs.optional(Impl.PACKET_CODEC).xmap(hash -> (ItemStackHash)DataFixUtils.orElse((Optional)hash, (Object)EMPTY), hash -> {
        Optional<Object> optional;
        if (hash instanceof Impl) {
            Impl impl = (Impl)hash;
            optional = Optional.of(impl);
        } else {
            optional = Optional.empty();
        }
        return optional;
    });

    public boolean hashEquals(ItemStack var1, ComponentChangesHash.ComponentHasher var2);

    public static ItemStackHash fromItemStack(ItemStack stack, ComponentChangesHash.ComponentHasher hasher) {
        if (stack.isEmpty()) {
            return EMPTY;
        }
        return new Impl(stack.getRegistryEntry(), stack.getCount(), ComponentChangesHash.fromComponents(stack.getComponentChanges(), hasher));
    }

    public record Impl(RegistryEntry<Item> item, int count, ComponentChangesHash components) implements ItemStackHash
    {
        public static final PacketCodec<RegistryByteBuf, Impl> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.registryEntry(RegistryKeys.ITEM), Impl::item, PacketCodecs.VAR_INT, Impl::count, ComponentChangesHash.PACKET_CODEC, Impl::components, Impl::new);

        @Override
        public boolean hashEquals(ItemStack stack, ComponentChangesHash.ComponentHasher hasher) {
            if (this.count != stack.getCount()) {
                return false;
            }
            if (!this.item.equals(stack.getRegistryEntry())) {
                return false;
            }
            return this.components.hashEquals(stack.getComponentChanges(), hasher);
        }
    }
}

