/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMaps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkTicketManager
extends PersistentState {
    private static final int DEFAULT_TICKETS_MAP_SIZE = 4;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Pair<ChunkPos, ChunkTicket>> TICKET_POS_CODEC = Codec.mapPair((MapCodec)ChunkPos.CODEC.fieldOf("chunk_pos"), ChunkTicket.CODEC).codec();
    public static final Codec<ChunkTicketManager> CODEC = RecordCodecBuilder.create((T instance) -> instance.group((App)TICKET_POS_CODEC.listOf().optionalFieldOf("tickets", List.of()).forGetter(ChunkTicketManager::getTickets)).apply((Applicative)instance, ChunkTicketManager::create));
    public static final PersistentStateType<ChunkTicketManager> STATE_TYPE = new PersistentStateType<ChunkTicketManager>("chunks", ChunkTicketManager::new, CODEC, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    private final Long2ObjectOpenHashMap<List<ChunkTicket>> tickets;
    private final Long2ObjectOpenHashMap<List<ChunkTicket>> savedTickets;
    private LongSet forcedChunks = new LongOpenHashSet();
    private @Nullable LevelUpdater loadingLevelUpdater;
    private @Nullable LevelUpdater simulationLevelUpdater;

    private ChunkTicketManager(Long2ObjectOpenHashMap<List<ChunkTicket>> tickets, Long2ObjectOpenHashMap<List<ChunkTicket>> savedTickets) {
        this.tickets = tickets;
        this.savedTickets = savedTickets;
        this.recomputeForcedChunks();
    }

    public ChunkTicketManager() {
        this((Long2ObjectOpenHashMap<List<ChunkTicket>>)new Long2ObjectOpenHashMap(4), (Long2ObjectOpenHashMap<List<ChunkTicket>>)new Long2ObjectOpenHashMap());
    }

    private static ChunkTicketManager create(List<Pair<ChunkPos, ChunkTicket>> tickets) {
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap();
        for (Pair<ChunkPos, ChunkTicket> pair : tickets) {
            ChunkPos chunkPos = (ChunkPos)pair.getFirst();
            List list = (List)long2ObjectOpenHashMap.computeIfAbsent(chunkPos.toLong(), l -> new ObjectArrayList(4));
            list.add((ChunkTicket)pair.getSecond());
        }
        return new ChunkTicketManager((Long2ObjectOpenHashMap<List<ChunkTicket>>)new Long2ObjectOpenHashMap(4), (Long2ObjectOpenHashMap<List<ChunkTicket>>)long2ObjectOpenHashMap);
    }

    private List<Pair<ChunkPos, ChunkTicket>> getTickets() {
        ArrayList<Pair<ChunkPos, ChunkTicket>> list = new ArrayList<Pair<ChunkPos, ChunkTicket>>();
        this.forEachTicket((pos, ticket) -> {
            if (ticket.getType().shouldSerialize()) {
                list.add(new Pair(pos, ticket));
            }
        });
        return list;
    }

    private void forEachTicket(BiConsumer<ChunkPos, ChunkTicket> ticketConsumer) {
        ChunkTicketManager.forEachTicket(ticketConsumer, this.tickets);
        ChunkTicketManager.forEachTicket(ticketConsumer, this.savedTickets);
    }

    private static void forEachTicket(BiConsumer<ChunkPos, ChunkTicket> ticketConsumer, Long2ObjectOpenHashMap<List<ChunkTicket>> tickets) {
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(tickets)) {
            ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
            for (ChunkTicket chunkTicket : (List)entry.getValue()) {
                ticketConsumer.accept(chunkPos, chunkTicket);
            }
        }
    }

    public void promoteToRealTickets() {
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.savedTickets)) {
            for (ChunkTicket chunkTicket : (List)entry.getValue()) {
                this.addTicket(entry.getLongKey(), chunkTicket);
            }
        }
        this.savedTickets.clear();
    }

    public void setLoadingLevelUpdater(@Nullable LevelUpdater loadingLevelUpdater) {
        this.loadingLevelUpdater = loadingLevelUpdater;
    }

    public void setSimulationLevelUpdater(@Nullable LevelUpdater simulationLevelUpdater) {
        this.simulationLevelUpdater = simulationLevelUpdater;
    }

    public boolean hasTickets() {
        return !this.tickets.isEmpty();
    }

    public boolean shouldResetIdleTimeout() {
        for (List list : this.tickets.values()) {
            for (ChunkTicket chunkTicket : list) {
                if (!chunkTicket.getType().resetsIdleTimeout()) continue;
                return true;
            }
        }
        return false;
    }

    public List<ChunkTicket> getTickets(long pos) {
        return (List)this.tickets.getOrDefault(pos, List.of());
    }

    private List<ChunkTicket> getTicketsMutable(long pos) {
        return (List)this.tickets.computeIfAbsent(pos, chunkPos -> new ObjectArrayList(4));
    }

    public void addTicket(ChunkTicketType type, ChunkPos pos, int radius) {
        ChunkTicket chunkTicket = new ChunkTicket(type, ChunkLevels.getLevelFromType(ChunkLevelType.FULL) - radius);
        this.addTicket(pos.toLong(), chunkTicket);
    }

    public void addTicket(ChunkTicket ticket, ChunkPos pos) {
        this.addTicket(pos.toLong(), ticket);
    }

    public boolean addTicket(long pos, ChunkTicket ticket) {
        List<ChunkTicket> list = this.getTicketsMutable(pos);
        for (ChunkTicket chunkTicket : list) {
            if (!ChunkTicketManager.ticketsEqual(ticket, chunkTicket)) continue;
            chunkTicket.refreshExpiry();
            this.markDirty();
            return false;
        }
        int i = ChunkTicketManager.getLevel(list, true);
        int j = ChunkTicketManager.getLevel(list, false);
        list.add(ticket);
        if (SharedConstants.VERBOSE_SERVER_EVENTS) {
            LOGGER.debug("ATI {} {}", (Object)new ChunkPos(pos), (Object)ticket);
        }
        if (ticket.getType().isForSimulation() && ticket.getLevel() < i && this.simulationLevelUpdater != null) {
            this.simulationLevelUpdater.update(pos, ticket.getLevel(), true);
        }
        if (ticket.getType().isForLoading() && ticket.getLevel() < j && this.loadingLevelUpdater != null) {
            this.loadingLevelUpdater.update(pos, ticket.getLevel(), true);
        }
        if (ticket.getType().equals(ChunkTicketType.FORCED)) {
            this.forcedChunks.add(pos);
        }
        this.markDirty();
        return true;
    }

    private static boolean ticketsEqual(ChunkTicket a, ChunkTicket b) {
        return b.getType() == a.getType() && b.getLevel() == a.getLevel();
    }

    public int getLevel(long pos, boolean forSimulation) {
        return ChunkTicketManager.getLevel(this.getTickets(pos), forSimulation);
    }

    private static int getLevel(List<ChunkTicket> tickets, boolean forSimulation) {
        ChunkTicket chunkTicket = ChunkTicketManager.getActiveTicket(tickets, forSimulation);
        return chunkTicket == null ? ChunkLevels.INACCESSIBLE + 1 : chunkTicket.getLevel();
    }

    private static @Nullable ChunkTicket getActiveTicket(@Nullable List<ChunkTicket> tickets, boolean forSimulation) {
        if (tickets == null) {
            return null;
        }
        ChunkTicket chunkTicket = null;
        for (ChunkTicket chunkTicket2 : tickets) {
            if (chunkTicket != null && chunkTicket2.getLevel() >= chunkTicket.getLevel()) continue;
            if (forSimulation && chunkTicket2.getType().isForSimulation()) {
                chunkTicket = chunkTicket2;
                continue;
            }
            if (forSimulation || !chunkTicket2.getType().isForLoading()) continue;
            chunkTicket = chunkTicket2;
        }
        return chunkTicket;
    }

    public void removeTicket(ChunkTicketType type, ChunkPos pos, int radius) {
        ChunkTicket chunkTicket = new ChunkTicket(type, ChunkLevels.getLevelFromType(ChunkLevelType.FULL) - radius);
        this.removeTicket(pos.toLong(), chunkTicket);
    }

    public void removeTicket(ChunkTicket ticket, ChunkPos pos) {
        this.removeTicket(pos.toLong(), ticket);
    }

    public boolean removeTicket(long pos, ChunkTicket ticket) {
        List list = (List)this.tickets.get(pos);
        if (list == null) {
            return false;
        }
        boolean bl = false;
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            ChunkTicket chunkTicket = (ChunkTicket)iterator.next();
            if (!ChunkTicketManager.ticketsEqual(ticket, chunkTicket)) continue;
            iterator.remove();
            if (SharedConstants.VERBOSE_SERVER_EVENTS) {
                LOGGER.debug("RTI {} {}", (Object)new ChunkPos(pos), (Object)chunkTicket);
            }
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        if (list.isEmpty()) {
            this.tickets.remove(pos);
        }
        if (ticket.getType().isForSimulation() && this.simulationLevelUpdater != null) {
            this.simulationLevelUpdater.update(pos, ChunkTicketManager.getLevel(list, true), false);
        }
        if (ticket.getType().isForLoading() && this.loadingLevelUpdater != null) {
            this.loadingLevelUpdater.update(pos, ChunkTicketManager.getLevel(list, false), false);
        }
        if (ticket.getType().equals(ChunkTicketType.FORCED)) {
            this.recomputeForcedChunks();
        }
        this.markDirty();
        return true;
    }

    private void recomputeForcedChunks() {
        this.forcedChunks = this.getAllChunksMatching(ticket -> ticket.getType().equals(ChunkTicketType.FORCED));
    }

    public String getDebugString(long pos, boolean forSimulation) {
        List<ChunkTicket> list = this.getTickets(pos);
        ChunkTicket chunkTicket = ChunkTicketManager.getActiveTicket(list, forSimulation);
        return chunkTicket == null ? "no_ticket" : chunkTicket.toString();
    }

    public void tick(ServerChunkLoadingManager chunkLoadingManager) {
        this.removeTicketsIf((ticket, pos) -> {
            if (this.canTicketExpire(chunkLoadingManager, ticket, pos)) {
                ticket.tick();
                return ticket.isExpired();
            }
            return false;
        }, null);
        this.markDirty();
    }

    private boolean canTicketExpire(ServerChunkLoadingManager chunkLoadingManager, ChunkTicket ticket, long pos) {
        if (!ticket.getType().canExpire()) {
            return false;
        }
        if (ticket.getType().canExpireBeforeLoad()) {
            return true;
        }
        ChunkHolder chunkHolder = chunkLoadingManager.getCurrentChunkHolder(pos);
        return chunkHolder == null || chunkHolder.isSavable();
    }

    public void shutdown() {
        this.removeTicketsIf((ticket, pos) -> ticket.getType() != ChunkTicketType.UNKNOWN, this.savedTickets);
    }

    public void removeTicketsIf(TicketPredicate predicate, @Nullable Long2ObjectOpenHashMap<List<ChunkTicket>> transferTo) {
        ObjectIterator objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
        boolean bl = false;
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            Iterator iterator = ((List)entry.getValue()).iterator();
            long l = entry.getLongKey();
            boolean bl2 = false;
            boolean bl3 = false;
            while (iterator.hasNext()) {
                ChunkTicket chunkTicket = (ChunkTicket)iterator.next();
                if (!predicate.test(chunkTicket, l)) continue;
                if (transferTo != null) {
                    List list = (List)transferTo.computeIfAbsent(l, pos -> new ObjectArrayList(((List)entry.getValue()).size()));
                    list.add(chunkTicket);
                }
                iterator.remove();
                if (chunkTicket.getType().isForLoading()) {
                    bl3 = true;
                }
                if (chunkTicket.getType().isForSimulation()) {
                    bl2 = true;
                }
                if (!chunkTicket.getType().equals(ChunkTicketType.FORCED)) continue;
                bl = true;
            }
            if (!bl3 && !bl2) continue;
            if (bl3 && this.loadingLevelUpdater != null) {
                this.loadingLevelUpdater.update(l, ChunkTicketManager.getLevel((List)entry.getValue(), false), false);
            }
            if (bl2 && this.simulationLevelUpdater != null) {
                this.simulationLevelUpdater.update(l, ChunkTicketManager.getLevel((List)entry.getValue(), true), false);
            }
            this.markDirty();
            if (!((List)entry.getValue()).isEmpty()) continue;
            objectIterator.remove();
        }
        if (bl) {
            this.recomputeForcedChunks();
        }
    }

    public void updateLevel(int level, ChunkTicketType type) {
        ArrayList<Pair> list = new ArrayList<Pair>();
        for (Long2ObjectMap.Entry entry : this.tickets.long2ObjectEntrySet()) {
            for (ChunkTicket chunkTicket : (List)entry.getValue()) {
                if (chunkTicket.getType() != type) continue;
                list.add(Pair.of((Object)chunkTicket, (Object)entry.getLongKey()));
            }
        }
        for (Pair pair : list) {
            ChunkTicket chunkTicket;
            Long long_ = (Long)pair.getSecond();
            chunkTicket = (ChunkTicket)pair.getFirst();
            this.removeTicket(long_, chunkTicket);
            ChunkTicketType chunkTicketType = chunkTicket.getType();
            this.addTicket(long_, new ChunkTicket(chunkTicketType, level));
        }
    }

    public boolean setChunkForced(ChunkPos pos, boolean forced) {
        ChunkTicket chunkTicket = new ChunkTicket(ChunkTicketType.FORCED, ServerChunkLoadingManager.FORCED_CHUNK_LEVEL);
        if (forced) {
            return this.addTicket(pos.toLong(), chunkTicket);
        }
        return this.removeTicket(pos.toLong(), chunkTicket);
    }

    public LongSet getForcedChunks() {
        return this.forcedChunks;
    }

    private LongSet getAllChunksMatching(Predicate<ChunkTicket> predicate) {
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        block0: for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.tickets)) {
            for (ChunkTicket chunkTicket : (List)entry.getValue()) {
                if (!predicate.test(chunkTicket)) continue;
                longOpenHashSet.add(entry.getLongKey());
                continue block0;
            }
        }
        return longOpenHashSet;
    }

    @FunctionalInterface
    public static interface LevelUpdater {
        public void update(long var1, int var3, boolean var4);
    }

    public static interface TicketPredicate {
        public boolean test(ChunkTicket var1, long var2);
    }
}

