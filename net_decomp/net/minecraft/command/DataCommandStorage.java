/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.command;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import org.jspecify.annotations.Nullable;

public class DataCommandStorage {
    private static final String COMMAND_STORAGE_PREFIX = "command_storage_";
    private final Map<String, PersistentState> storages = new HashMap<String, PersistentState>();
    private final PersistentStateManager stateManager;

    public DataCommandStorage(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public NbtCompound get(Identifier id) {
        PersistentState persistentState = this.getStorage(id.getNamespace());
        if (persistentState != null) {
            return persistentState.get(id.getPath());
        }
        return new NbtCompound();
    }

    private @Nullable PersistentState getStorage(String namespace) {
        PersistentState persistentState = this.storages.get(namespace);
        if (persistentState != null) {
            return persistentState;
        }
        PersistentState persistentState2 = this.stateManager.get(PersistentState.createStateType(namespace));
        if (persistentState2 != null) {
            this.storages.put(namespace, persistentState2);
        }
        return persistentState2;
    }

    private PersistentState getOrCreateStorage(String namespace) {
        PersistentState persistentState = this.storages.get(namespace);
        if (persistentState != null) {
            return persistentState;
        }
        PersistentState persistentState2 = this.stateManager.getOrCreate(PersistentState.createStateType(namespace));
        this.storages.put(namespace, persistentState2);
        return persistentState2;
    }

    public void set(Identifier id, NbtCompound nbt) {
        this.getOrCreateStorage(id.getNamespace()).set(id.getPath(), nbt);
    }

    public Stream<Identifier> getIds() {
        return this.storages.entrySet().stream().flatMap(entry -> ((PersistentState)entry.getValue()).getIds((String)entry.getKey()));
    }

    static String getSaveKey(String namespace) {
        return COMMAND_STORAGE_PREFIX + namespace;
    }

    static class PersistentState
    extends net.minecraft.world.PersistentState {
        public static final Codec<PersistentState> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.unboundedMap(Codecs.IDENTIFIER_PATH, NbtCompound.CODEC).fieldOf("contents").forGetter(state -> state.map)).apply((Applicative)instance, PersistentState::new));
        private final Map<String, NbtCompound> map;

        private PersistentState(Map<String, NbtCompound> map) {
            this.map = new HashMap<String, NbtCompound>(map);
        }

        private PersistentState() {
            this(new HashMap<String, NbtCompound>());
        }

        public static PersistentStateType<PersistentState> createStateType(String id) {
            return new PersistentStateType<PersistentState>(DataCommandStorage.getSaveKey(id), PersistentState::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
        }

        public NbtCompound get(String name) {
            NbtCompound nbtCompound = this.map.get(name);
            return nbtCompound != null ? nbtCompound : new NbtCompound();
        }

        public void set(String name, NbtCompound nbt) {
            if (nbt.isEmpty()) {
                this.map.remove(name);
            } else {
                this.map.put(name, nbt);
            }
            this.markDirty();
        }

        public Stream<Identifier> getIds(String namespace) {
            return this.map.keySet().stream().map(key -> Identifier.of(namespace, key));
        }
    }
}

