/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.boss;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;

public class CommandBossBar
extends ServerBossBar {
    private static final int DEFAULT_MAX_VALUE = 100;
    private final Identifier id;
    private final Set<UUID> playerUuids = Sets.newHashSet();
    private int value;
    private int maxValue = 100;

    public CommandBossBar(Identifier id, Text displayName) {
        super(displayName, BossBar.Color.WHITE, BossBar.Style.PROGRESS);
        this.id = id;
        this.setPercent(0.0f);
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayerEntity player) {
        super.addPlayer(player);
        this.playerUuids.add(player.getUuid());
    }

    public void addPlayer(UUID uuid) {
        this.playerUuids.add(uuid);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player) {
        super.removePlayer(player);
        this.playerUuids.remove(player.getUuid());
    }

    @Override
    public void clearPlayers() {
        super.clearPlayers();
        this.playerUuids.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public void setValue(int value) {
        this.value = value;
        this.setPercent(MathHelper.clamp((float)value / (float)this.maxValue, 0.0f, 1.0f));
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        this.setPercent(MathHelper.clamp((float)this.value / (float)maxValue, 0.0f, 1.0f));
    }

    public final Text toHoverableText() {
        return Texts.bracketed(this.getName()).styled(style -> style.withColor(this.getColor().getTextFormat()).withHoverEvent(new HoverEvent.ShowText(Text.literal(this.getId().toString()))).withInsertion(this.getId().toString()));
    }

    public boolean addPlayers(Collection<ServerPlayerEntity> players) {
        boolean bl;
        HashSet set = Sets.newHashSet();
        HashSet set2 = Sets.newHashSet();
        for (UUID uUID : this.playerUuids) {
            bl = false;
            for (ServerPlayerEntity serverPlayerEntity : players) {
                if (!serverPlayerEntity.getUuid().equals(uUID)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set.add(uUID);
        }
        for (ServerPlayerEntity serverPlayerEntity2 : players) {
            bl = false;
            for (UUID uUID2 : this.playerUuids) {
                if (!serverPlayerEntity2.getUuid().equals(uUID2)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            set2.add(serverPlayerEntity2);
        }
        for (UUID uUID : set) {
            for (ServerPlayerEntity serverPlayerEntity3 : this.getPlayers()) {
                if (!serverPlayerEntity3.getUuid().equals(uUID)) continue;
                this.removePlayer(serverPlayerEntity3);
                break;
            }
            this.playerUuids.remove(uUID);
        }
        for (ServerPlayerEntity serverPlayerEntity2 : set2) {
            this.addPlayer(serverPlayerEntity2);
        }
        return !set.isEmpty() || !set2.isEmpty();
    }

    public static CommandBossBar fromSerialized(Identifier id, Serialized serialized) {
        CommandBossBar commandBossBar = new CommandBossBar(id, serialized.name);
        commandBossBar.setVisible(serialized.visible);
        commandBossBar.setValue(serialized.value);
        commandBossBar.setMaxValue(serialized.max);
        commandBossBar.setColor(serialized.color);
        commandBossBar.setStyle(serialized.overlay);
        commandBossBar.setDarkenSky(serialized.darkenScreen);
        commandBossBar.setDragonMusic(serialized.playBossMusic);
        commandBossBar.setThickenFog(serialized.createWorldFog);
        serialized.players.forEach(commandBossBar::addPlayer);
        return commandBossBar;
    }

    public Serialized toSerialized() {
        return new Serialized(this.getName(), this.isVisible(), this.getValue(), this.getMaxValue(), this.getColor(), this.getStyle(), this.shouldDarkenSky(), this.hasDragonMusic(), this.shouldThickenFog(), Set.copyOf(this.playerUuids));
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        if (this.playerUuids.contains(player.getUuid())) {
            this.addPlayer(player);
        }
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        super.removePlayer(player);
    }

    public static final class Serialized
    extends Record {
        final Text name;
        final boolean visible;
        final int value;
        final int max;
        final BossBar.Color color;
        final BossBar.Style overlay;
        final boolean darkenScreen;
        final boolean playBossMusic;
        final boolean createWorldFog;
        final Set<UUID> players;
        public static final Codec<Serialized> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("Name").forGetter(Serialized::name), (App)Codec.BOOL.optionalFieldOf("Visible", (Object)false).forGetter(Serialized::visible), (App)Codec.INT.optionalFieldOf("Value", (Object)0).forGetter(Serialized::value), (App)Codec.INT.optionalFieldOf("Max", (Object)100).forGetter(Serialized::max), (App)BossBar.Color.CODEC.optionalFieldOf("Color", (Object)BossBar.Color.WHITE).forGetter(Serialized::color), (App)BossBar.Style.CODEC.optionalFieldOf("Overlay", (Object)BossBar.Style.PROGRESS).forGetter(Serialized::overlay), (App)Codec.BOOL.optionalFieldOf("DarkenScreen", (Object)false).forGetter(Serialized::darkenScreen), (App)Codec.BOOL.optionalFieldOf("PlayBossMusic", (Object)false).forGetter(Serialized::playBossMusic), (App)Codec.BOOL.optionalFieldOf("CreateWorldFog", (Object)false).forGetter(Serialized::createWorldFog), (App)Uuids.SET_CODEC.optionalFieldOf("Players", Set.of()).forGetter(Serialized::players)).apply((Applicative)instance, Serialized::new));

        public Serialized(Text name, boolean visible, int value, int max, BossBar.Color color, BossBar.Style overlay, boolean darkenScreen, boolean playBossMusic, boolean createWorldFog, Set<UUID> players) {
            this.name = name;
            this.visible = visible;
            this.value = value;
            this.max = max;
            this.color = color;
            this.overlay = overlay;
            this.darkenScreen = darkenScreen;
            this.playBossMusic = playBossMusic;
            this.createWorldFog = createWorldFog;
            this.players = players;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Serialized.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Serialized.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Serialized.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this, object);
        }

        public Text name() {
            return this.name;
        }

        public boolean visible() {
            return this.visible;
        }

        public int value() {
            return this.value;
        }

        public int max() {
            return this.max;
        }

        public BossBar.Color color() {
            return this.color;
        }

        public BossBar.Style overlay() {
            return this.overlay;
        }

        public boolean darkenScreen() {
            return this.darkenScreen;
        }

        public boolean playBossMusic() {
            return this.playBossMusic;
        }

        public boolean createWorldFog() {
            return this.createWorldFog;
        }

        public Set<UUID> players() {
            return this.players;
        }
    }
}

