/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity.boss;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Map;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class BossBarManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Map<Identifier, CommandBossBar.Serialized>> CODEC = Codec.unboundedMap(Identifier.CODEC, CommandBossBar.Serialized.CODEC);
    private final Map<Identifier, CommandBossBar> commandBossBars = Maps.newHashMap();

    public @Nullable CommandBossBar get(Identifier id) {
        return this.commandBossBars.get(id);
    }

    public CommandBossBar add(Identifier id, Text displayName) {
        CommandBossBar commandBossBar = new CommandBossBar(id, displayName);
        this.commandBossBars.put(id, commandBossBar);
        return commandBossBar;
    }

    public void remove(CommandBossBar bossBar) {
        this.commandBossBars.remove(bossBar.getId());
    }

    public Collection<Identifier> getIds() {
        return this.commandBossBars.keySet();
    }

    public Collection<CommandBossBar> getAll() {
        return this.commandBossBars.values();
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
        Map<Identifier, CommandBossBar.Serialized> map = Util.transformMapValues(this.commandBossBars, CommandBossBar::toSerialized);
        return (NbtCompound)CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), map).getOrThrow();
    }

    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Map<Identifier, CommandBossBar.Serialized> map = CODEC.parse(registries.getOps(NbtOps.INSTANCE), (Object)nbt).resultOrPartial(error -> LOGGER.error("Failed to parse boss bar events: {}", error)).orElse(Map.of());
        map.forEach((id, serialized) -> this.commandBossBars.put((Identifier)id, CommandBossBar.fromSerialized(id, serialized)));
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        for (CommandBossBar commandBossBar : this.commandBossBars.values()) {
            commandBossBar.onPlayerConnect(player);
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        for (CommandBossBar commandBossBar : this.commandBossBars.values()) {
            commandBossBar.onPlayerDisconnect(player);
        }
    }
}

