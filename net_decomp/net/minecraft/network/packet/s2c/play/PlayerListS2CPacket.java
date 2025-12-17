/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.PropertyMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.jspecify.annotations.Nullable;

public class PlayerListS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, PlayerListS2CPacket> CODEC = Packet.createCodec(PlayerListS2CPacket::write, PlayerListS2CPacket::new);
    private final EnumSet<Action> actions;
    private final List<Entry> entries;

    public PlayerListS2CPacket(EnumSet<Action> actions, Collection<ServerPlayerEntity> players) {
        this.actions = actions;
        this.entries = players.stream().map(Entry::new).toList();
    }

    public PlayerListS2CPacket(Action action, ServerPlayerEntity player) {
        this.actions = EnumSet.of(action);
        this.entries = List.of(new Entry(player));
    }

    public static PlayerListS2CPacket entryFromPlayer(Collection<ServerPlayerEntity> players) {
        EnumSet<Action[]> enumSet = EnumSet.of(Action.ADD_PLAYER, new Action[]{Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_HAT, Action.UPDATE_LIST_ORDER});
        return new PlayerListS2CPacket(enumSet, players);
    }

    private PlayerListS2CPacket(RegistryByteBuf buf) {
        this.actions = buf.readEnumSet(Action.class);
        this.entries = buf.readList(buf2 -> {
            Serialized serialized = new Serialized(buf2.readUuid());
            for (Action action : this.actions) {
                action.reader.read(serialized, (RegistryByteBuf)((Object)buf2));
            }
            return serialized.toEntry();
        });
    }

    private void write(RegistryByteBuf buf) {
        buf.writeEnumSet(this.actions, Action.class);
        buf.writeCollection(this.entries, (buf2, entry) -> {
            buf2.writeUuid(entry.profileId());
            for (Action action : this.actions) {
                action.writer.write((RegistryByteBuf)((Object)buf2), (Entry)entry);
            }
        });
    }

    @Override
    public PacketType<PlayerListS2CPacket> getPacketType() {
        return PlayPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onPlayerList(this);
    }

    public EnumSet<Action> getActions() {
        return this.actions;
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public List<Entry> getPlayerAdditionEntries() {
        return this.actions.contains((Object)Action.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public static final class Entry
    extends Record {
        private final UUID profileId;
        private final @Nullable GameProfile profile;
        private final boolean listed;
        private final int latency;
        private final GameMode gameMode;
        private final @Nullable Text displayName;
        final boolean showHat;
        final int listOrder;
        final @Nullable PublicPlayerSession.Serialized chatSession;

        Entry(ServerPlayerEntity player) {
            this(player.getUuid(), player.getGameProfile(), true, player.networkHandler.getLatency(), player.getGameMode(), player.getPlayerListName(), player.isModelPartVisible(PlayerModelPart.HAT), player.getPlayerListOrder(), Nullables.map(player.getSession(), PublicPlayerSession::toSerialized));
        }

        public Entry(UUID profileId, @Nullable GameProfile profile, boolean listed, int latency, GameMode gameMode, @Nullable Text displayName, boolean showHat, int listOrder, @Nullable PublicPlayerSession.Serialized chatSession) {
            this.profileId = profileId;
            this.profile = profile;
            this.listed = listed;
            this.latency = latency;
            this.gameMode = gameMode;
            this.displayName = displayName;
            this.showHat = showHat;
            this.listOrder = listOrder;
            this.chatSession = chatSession;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;showHat;listOrder;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "showHat", "listOrder", "chatSession"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;showHat;listOrder;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "showHat", "listOrder", "chatSession"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;showHat;listOrder;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "showHat", "listOrder", "chatSession"}, this, object);
        }

        public UUID profileId() {
            return this.profileId;
        }

        public @Nullable GameProfile profile() {
            return this.profile;
        }

        public boolean listed() {
            return this.listed;
        }

        public int latency() {
            return this.latency;
        }

        public GameMode gameMode() {
            return this.gameMode;
        }

        public @Nullable Text displayName() {
            return this.displayName;
        }

        public boolean showHat() {
            return this.showHat;
        }

        public int listOrder() {
            return this.listOrder;
        }

        public @Nullable PublicPlayerSession.Serialized chatSession() {
            return this.chatSession;
        }
    }

    public static final class Action
    extends Enum<Action> {
        public static final /* enum */ Action ADD_PLAYER = new Action((serialized, buf) -> {
            String string = (String)PacketCodecs.PLAYER_NAME.decode(buf);
            PropertyMap propertyMap = (PropertyMap)PacketCodecs.PROPERTY_MAP.decode(buf);
            serialized.gameProfile = new GameProfile(serialized.profileId, string, propertyMap);
        }, (buf, entry) -> {
            GameProfile gameProfile = Objects.requireNonNull(entry.profile());
            PacketCodecs.PLAYER_NAME.encode(buf, gameProfile.name());
            PacketCodecs.PROPERTY_MAP.encode(buf, gameProfile.properties());
        });
        public static final /* enum */ Action INITIALIZE_CHAT = new Action((serialized, buf) -> {
            serialized.session = buf.readNullable(PublicPlayerSession.Serialized::fromBuf);
        }, (buf, entry) -> buf.writeNullable(entry.chatSession, PublicPlayerSession.Serialized::write));
        public static final /* enum */ Action UPDATE_GAME_MODE = new Action((serialized, buf) -> {
            serialized.gameMode = GameMode.byIndex(buf.readVarInt());
        }, (buf, entry) -> buf.writeVarInt(entry.gameMode().getIndex()));
        public static final /* enum */ Action UPDATE_LISTED = new Action((serialized, buf) -> {
            serialized.listed = buf.readBoolean();
        }, (buf, entry) -> buf.writeBoolean(entry.listed()));
        public static final /* enum */ Action UPDATE_LATENCY = new Action((serialized, buf) -> {
            serialized.latency = buf.readVarInt();
        }, (buf, entry) -> buf.writeVarInt(entry.latency()));
        public static final /* enum */ Action UPDATE_DISPLAY_NAME = new Action((serialized, buf) -> {
            serialized.displayName = PacketByteBuf.readNullable(buf, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC);
        }, (buf, entry) -> PacketByteBuf.writeNullable(buf, entry.displayName(), TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC));
        public static final /* enum */ Action UPDATE_LIST_ORDER = new Action((serialized, buf) -> {
            serialized.listOrder = buf.readVarInt();
        }, (buf, entry) -> buf.writeVarInt(entry.listOrder));
        public static final /* enum */ Action UPDATE_HAT = new Action((serialized, buf) -> {
            serialized.showHat = buf.readBoolean();
        }, (buf, entry) -> buf.writeBoolean(entry.showHat));
        final Reader reader;
        final Writer writer;
        private static final /* synthetic */ Action[] field_29141;

        public static Action[] values() {
            return (Action[])field_29141.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private Action(Reader reader, Writer writer) {
            this.reader = reader;
            this.writer = writer;
        }

        private static /* synthetic */ Action[] method_36951() {
            return new Action[]{ADD_PLAYER, INITIALIZE_CHAT, UPDATE_GAME_MODE, UPDATE_LISTED, UPDATE_LATENCY, UPDATE_DISPLAY_NAME, UPDATE_LIST_ORDER, UPDATE_HAT};
        }

        static {
            field_29141 = Action.method_36951();
        }

        public static interface Reader {
            public void read(Serialized var1, RegistryByteBuf var2);
        }

        public static interface Writer {
            public void write(RegistryByteBuf var1, Entry var2);
        }
    }

    static class Serialized {
        final UUID profileId;
        @Nullable GameProfile gameProfile;
        boolean listed;
        int latency;
        GameMode gameMode = GameMode.DEFAULT;
        @Nullable Text displayName;
        boolean showHat;
        int listOrder;
        @Nullable PublicPlayerSession.Serialized session;

        Serialized(UUID profileId) {
            this.profileId = profileId;
        }

        Entry toEntry() {
            return new Entry(this.profileId, this.gameProfile, this.listed, this.latency, this.gameMode, this.displayName, this.showHat, this.listOrder, this.session);
        }
    }
}

