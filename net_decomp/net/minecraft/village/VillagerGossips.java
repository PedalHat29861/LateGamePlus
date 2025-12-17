/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 */
package net.minecraft.village;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.Uuids;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.VillagerGossipType;

public class VillagerGossips {
    public static final Codec<VillagerGossips> CODEC = GossipEntry.CODEC.listOf().xmap(VillagerGossips::new, gossips -> gossips.entries().toList());
    public static final int field_30236 = 2;
    private final Map<UUID, Reputation> entityReputation = new HashMap<UUID, Reputation>();

    public VillagerGossips() {
    }

    private VillagerGossips(List<GossipEntry> gossips) {
        gossips.forEach(gossip -> this.getReputationFor((UUID)gossip.target).associatedGossip.put((Object)gossip.type, gossip.value));
    }

    @Debug
    public Map<UUID, Object2IntMap<VillagerGossipType>> getEntityReputationAssociatedGossips() {
        HashMap map = Maps.newHashMap();
        this.entityReputation.keySet().forEach(uuid -> {
            Reputation reputation = this.entityReputation.get(uuid);
            map.put(uuid, reputation.associatedGossip);
        });
        return map;
    }

    public void decay() {
        Iterator<Reputation> iterator = this.entityReputation.values().iterator();
        while (iterator.hasNext()) {
            Reputation reputation = iterator.next();
            reputation.decay();
            if (!reputation.isObsolete()) continue;
            iterator.remove();
        }
    }

    private Stream<GossipEntry> entries() {
        return this.entityReputation.entrySet().stream().flatMap(entry -> ((Reputation)entry.getValue()).entriesFor((UUID)entry.getKey()));
    }

    private Collection<GossipEntry> pickGossips(Random random, int count) {
        List<GossipEntry> list = this.entries().toList();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] is = new int[list.size()];
        int i = 0;
        for (int j = 0; j < list.size(); ++j) {
            GossipEntry gossipEntry = list.get(j);
            is[j] = (i += Math.abs(gossipEntry.getValue())) - 1;
        }
        Set set = Sets.newIdentityHashSet();
        for (int k = 0; k < count; ++k) {
            int l = random.nextInt(i);
            int m = Arrays.binarySearch(is, l);
            set.add(list.get(m < 0 ? -m - 1 : m));
        }
        return set;
    }

    private Reputation getReputationFor(UUID target) {
        return this.entityReputation.computeIfAbsent(target, uuid -> new Reputation());
    }

    public void shareGossipFrom(VillagerGossips from, Random random, int count) {
        Collection<GossipEntry> collection = from.pickGossips(random, count);
        collection.forEach(gossip -> {
            int i = gossip.value - gossip.type.shareDecrement;
            if (i >= 2) {
                this.getReputationFor((UUID)gossip.target).associatedGossip.mergeInt((Object)gossip.type, i, VillagerGossips::max);
            }
        });
    }

    public int getReputationFor(UUID target, Predicate<VillagerGossipType> gossipTypeFilter) {
        Reputation reputation = this.entityReputation.get(target);
        return reputation != null ? reputation.getValueFor(gossipTypeFilter) : 0;
    }

    public long getReputationCount(VillagerGossipType type, DoublePredicate predicate) {
        return this.entityReputation.values().stream().filter(reputation -> predicate.test(reputation.associatedGossip.getOrDefault((Object)type, 0) * villagerGossipType.multiplier)).count();
    }

    public void startGossip(UUID target, VillagerGossipType type, int value) {
        Reputation reputation = this.getReputationFor(target);
        reputation.associatedGossip.mergeInt((Object)type, value, (left, right) -> this.mergeReputation(type, left, right));
        reputation.clamp(type);
        if (reputation.isObsolete()) {
            this.entityReputation.remove(target);
        }
    }

    public void removeGossip(UUID target, VillagerGossipType type, int value) {
        this.startGossip(target, type, -value);
    }

    public void remove(UUID target, VillagerGossipType type) {
        Reputation reputation = this.entityReputation.get(target);
        if (reputation != null) {
            reputation.remove(type);
            if (reputation.isObsolete()) {
                this.entityReputation.remove(target);
            }
        }
    }

    public void remove(VillagerGossipType type) {
        Iterator<Reputation> iterator = this.entityReputation.values().iterator();
        while (iterator.hasNext()) {
            Reputation reputation = iterator.next();
            reputation.remove(type);
            if (!reputation.isObsolete()) continue;
            iterator.remove();
        }
    }

    public void clear() {
        this.entityReputation.clear();
    }

    public void add(VillagerGossips gossips) {
        gossips.entityReputation.forEach((target, reputation) -> this.getReputationFor((UUID)target).associatedGossip.putAll(reputation.associatedGossip));
    }

    private static int max(int left, int right) {
        return Math.max(left, right);
    }

    private int mergeReputation(VillagerGossipType type, int left, int right) {
        int i = left + right;
        return i > type.maxValue ? Math.max(type.maxValue, left) : i;
    }

    public VillagerGossips copy() {
        VillagerGossips villagerGossips = new VillagerGossips();
        villagerGossips.add(this);
        return villagerGossips;
    }

    static class Reputation {
        final Object2IntMap<VillagerGossipType> associatedGossip = new Object2IntOpenHashMap();

        Reputation() {
        }

        public int getValueFor(Predicate<VillagerGossipType> gossipTypeFilter) {
            return this.associatedGossip.object2IntEntrySet().stream().filter(entry -> gossipTypeFilter.test((VillagerGossipType)entry.getKey())).mapToInt(entry -> entry.getIntValue() * ((VillagerGossipType)entry.getKey()).multiplier).sum();
        }

        public Stream<GossipEntry> entriesFor(UUID target) {
            return this.associatedGossip.object2IntEntrySet().stream().map(entry -> new GossipEntry(target, (VillagerGossipType)entry.getKey(), entry.getIntValue()));
        }

        public void decay() {
            ObjectIterator objectIterator = this.associatedGossip.object2IntEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
                int i = entry.getIntValue() - ((VillagerGossipType)entry.getKey()).decay;
                if (i < 2) {
                    objectIterator.remove();
                    continue;
                }
                entry.setValue(i);
            }
        }

        public boolean isObsolete() {
            return this.associatedGossip.isEmpty();
        }

        public void clamp(VillagerGossipType gossipType) {
            int i = this.associatedGossip.getInt((Object)gossipType);
            if (i > gossipType.maxValue) {
                this.associatedGossip.put((Object)gossipType, gossipType.maxValue);
            }
            if (i < 2) {
                this.remove(gossipType);
            }
        }

        public void remove(VillagerGossipType gossipType) {
            this.associatedGossip.removeInt((Object)gossipType);
        }
    }

    static final class GossipEntry
    extends Record {
        final UUID target;
        final VillagerGossipType type;
        final int value;
        public static final Codec<GossipEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Uuids.INT_STREAM_CODEC.fieldOf("Target").forGetter(GossipEntry::target), (App)VillagerGossipType.CODEC.fieldOf("Type").forGetter(GossipEntry::type), (App)Codecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipEntry::value)).apply((Applicative)instance, GossipEntry::new));

        GossipEntry(UUID target, VillagerGossipType type, int value) {
            this.target = target;
            this.type = type;
            this.value = value;
        }

        public int getValue() {
            return this.value * this.type.multiplier;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this, object);
        }

        public UUID target() {
            return this.target;
        }

        public VillagerGossipType type() {
            return this.type;
        }

        public int value() {
            return this.value;
        }
    }
}

