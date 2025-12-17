/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.registry.tag;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagPacketSerializer {
    public static Map<RegistryKey<? extends Registry<?>>, Serialized> serializeTags(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistryManager) {
        return SerializableRegistries.streamRegistryManagerEntries(dynamicRegistryManager).map(registry -> Pair.of(registry.key(), (Object)TagPacketSerializer.serializeTags(registry.value()))).filter(pair -> !((Serialized)pair.getSecond()).isEmpty()).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static <T> Serialized serializeTags(Registry<T> registry) {
        HashMap<Identifier, IntList> map = new HashMap<Identifier, IntList>();
        registry.streamTags().forEach(tag -> {
            IntArrayList intList = new IntArrayList(tag.size());
            for (RegistryEntry registryEntry : tag) {
                if (registryEntry.getType() != RegistryEntry.Type.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + String.valueOf(registryEntry));
                }
                intList.add(registry.getRawId(registryEntry.value()));
            }
            map.put(tag.getTag().id(), (IntList)intList);
        });
        return new Serialized(map);
    }

    static <T> TagGroupLoader.RegistryTags<T> toRegistryTags(Registry<T> registry, Serialized tags) {
        RegistryKey registryKey = registry.getKey();
        HashMap map = new HashMap();
        tags.contents.forEach((tagId, rawIds) -> {
            TagKey tagKey = TagKey.of(registryKey, tagId);
            List list = rawIds.intStream().mapToObj(registry::getEntry).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
            map.put(tagKey, list);
        });
        return new TagGroupLoader.RegistryTags<T>(registryKey, map);
    }

    public static final class Serialized {
        public static final Serialized NONE = new Serialized(Map.of());
        final Map<Identifier, IntList> contents;

        Serialized(Map<Identifier, IntList> contents) {
            this.contents = contents;
        }

        public void writeBuf(PacketByteBuf buf) {
            buf.writeMap(this.contents, PacketByteBuf::writeIdentifier, PacketByteBuf::writeIntList);
        }

        public static Serialized fromBuf(PacketByteBuf buf) {
            return new Serialized(buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readIntList));
        }

        public boolean isEmpty() {
            return this.contents.isEmpty();
        }

        public int size() {
            return this.contents.size();
        }

        public <T> TagGroupLoader.RegistryTags<T> toRegistryTags(Registry<T> registry) {
            return TagPacketSerializer.toRegistryTags(registry, this);
        }
    }
}

