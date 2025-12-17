/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.io.Files
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ServerConfigList<K, V extends ServerConfigEntry<K>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();
    protected final ManagementListener field_62420;

    public ServerConfigList(File file, ManagementListener managementListener) {
        this.file = file;
        this.field_62420 = managementListener;
    }

    public File getFile() {
        return this.file;
    }

    public boolean add(V serverConfigEntry) {
        String string = this.toString(((ServerConfigEntry)serverConfigEntry).getKey());
        ServerConfigEntry serverConfigEntry2 = (ServerConfigEntry)this.map.get(string);
        if (serverConfigEntry.equals(serverConfigEntry2)) {
            return false;
        }
        this.map.put(string, serverConfigEntry);
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after adding a user.", (Throwable)iOException);
        }
        return true;
    }

    public @Nullable V get(K key) {
        this.removeInvalidEntries();
        return (V)((ServerConfigEntry)this.map.get(this.toString(key)));
    }

    public boolean remove(K key) {
        ServerConfigEntry serverConfigEntry = (ServerConfigEntry)this.map.remove(this.toString(key));
        if (serverConfigEntry == null) {
            return false;
        }
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)iOException);
        }
        return true;
    }

    public boolean remove(ServerConfigEntry<K> entry) {
        return this.remove(Objects.requireNonNull(entry.getKey()));
    }

    public void clear() {
        this.map.clear();
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)iOException);
        }
    }

    public String[] getNames() {
        return this.map.keySet().toArray(new String[0]);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    protected String toString(K profile) {
        return profile.toString();
    }

    protected boolean contains(K object) {
        return this.map.containsKey(this.toString(object));
    }

    private void removeInvalidEntries() {
        ArrayList list = Lists.newArrayList();
        for (ServerConfigEntry serverConfigEntry : this.map.values()) {
            if (!serverConfigEntry.isInvalid()) continue;
            list.add(serverConfigEntry.getKey());
        }
        for (Object object : list) {
            this.map.remove(this.toString(object));
        }
    }

    protected abstract ServerConfigEntry<K> fromJson(JsonObject var1);

    public Collection<V> values() {
        return this.map.values();
    }

    public void save() throws IOException {
        JsonArray jsonArray = new JsonArray();
        this.map.values().stream().map(entry -> Util.make(new JsonObject(), entry::write)).forEach(arg_0 -> ((JsonArray)jsonArray).add(arg_0));
        try (BufferedWriter bufferedWriter = Files.newWriter((File)this.file, (Charset)StandardCharsets.UTF_8);){
            GSON.toJson((JsonElement)jsonArray, GSON.newJsonWriter((Writer)bufferedWriter));
        }
    }

    public void load() throws IOException {
        if (!this.file.exists()) {
            return;
        }
        try (BufferedReader bufferedReader = Files.newReader((File)this.file, (Charset)StandardCharsets.UTF_8);){
            this.map.clear();
            JsonArray jsonArray = (JsonArray)GSON.fromJson((Reader)bufferedReader, JsonArray.class);
            if (jsonArray == null) {
                return;
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = JsonHelper.asObject(jsonElement, "entry");
                ServerConfigEntry<K> serverConfigEntry = this.fromJson(jsonObject);
                if (serverConfigEntry.getKey() == null) continue;
                this.map.put(this.toString(serverConfigEntry.getKey()), serverConfigEntry);
            }
        }
    }
}

