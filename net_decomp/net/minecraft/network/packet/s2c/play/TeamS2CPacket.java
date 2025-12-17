/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import org.jspecify.annotations.Nullable;

public class TeamS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, TeamS2CPacket> CODEC = Packet.createCodec(TeamS2CPacket::write, TeamS2CPacket::new);
    private static final int ADD = 0;
    private static final int REMOVE = 1;
    private static final int UPDATE = 2;
    private static final int ADD_PLAYERS = 3;
    private static final int REMOVE_PLAYERS = 4;
    private static final int FIRST_MAX_VISIBILITY_OR_COLLISION_RULE_LENGTH = 40;
    private static final int SECOND_MAX_VISIBILITY_OR_COLLISION_RULE_LENGTH = 40;
    private final int packetType;
    private final String teamName;
    private final Collection<String> playerNames;
    private final Optional<SerializableTeam> team;

    private TeamS2CPacket(String teamName, int packetType, Optional<SerializableTeam> team, Collection<String> playerNames) {
        this.teamName = teamName;
        this.packetType = packetType;
        this.team = team;
        this.playerNames = ImmutableList.copyOf(playerNames);
    }

    public static TeamS2CPacket updateTeam(Team team, boolean updatePlayers) {
        return new TeamS2CPacket(team.getName(), updatePlayers ? 0 : 2, Optional.of(new SerializableTeam(team)), updatePlayers ? team.getPlayerList() : ImmutableList.of());
    }

    public static TeamS2CPacket updateRemovedTeam(Team team) {
        return new TeamS2CPacket(team.getName(), 1, Optional.empty(), (Collection<String>)ImmutableList.of());
    }

    public static TeamS2CPacket changePlayerTeam(Team team, String playerName, Operation operation) {
        return new TeamS2CPacket(team.getName(), operation == Operation.ADD ? 3 : 4, Optional.empty(), (Collection<String>)ImmutableList.of((Object)playerName));
    }

    private TeamS2CPacket(RegistryByteBuf buf) {
        this.teamName = buf.readString();
        this.packetType = buf.readByte();
        this.team = TeamS2CPacket.containsTeamInfo(this.packetType) ? Optional.of(new SerializableTeam(buf)) : Optional.empty();
        this.playerNames = TeamS2CPacket.containsPlayers(this.packetType) ? buf.readList(PacketByteBuf::readString) : ImmutableList.of();
    }

    private void write(RegistryByteBuf buf) {
        buf.writeString(this.teamName);
        buf.writeByte(this.packetType);
        if (TeamS2CPacket.containsTeamInfo(this.packetType)) {
            this.team.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.packetType)).write(buf);
        }
        if (TeamS2CPacket.containsPlayers(this.packetType)) {
            buf.writeCollection(this.playerNames, PacketByteBuf::writeString);
        }
    }

    private static boolean containsPlayers(int packetType) {
        return packetType == 0 || packetType == 3 || packetType == 4;
    }

    private static boolean containsTeamInfo(int packetType) {
        return packetType == 0 || packetType == 2;
    }

    public @Nullable Operation getPlayerListOperation() {
        return switch (this.packetType) {
            case 0, 3 -> Operation.ADD;
            case 4 -> Operation.REMOVE;
            default -> null;
        };
    }

    public @Nullable Operation getTeamOperation() {
        return switch (this.packetType) {
            case 0 -> Operation.ADD;
            case 1 -> Operation.REMOVE;
            default -> null;
        };
    }

    @Override
    public PacketType<TeamS2CPacket> getPacketType() {
        return PlayPackets.SET_PLAYER_TEAM;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onTeam(this);
    }

    public String getTeamName() {
        return this.teamName;
    }

    public Collection<String> getPlayerNames() {
        return this.playerNames;
    }

    public Optional<SerializableTeam> getTeam() {
        return this.team;
    }

    public static class SerializableTeam {
        private final Text displayName;
        private final Text prefix;
        private final Text suffix;
        private final AbstractTeam.VisibilityRule nameTagVisibilityRule;
        private final AbstractTeam.CollisionRule collisionRule;
        private final Formatting color;
        private final int friendlyFlags;

        public SerializableTeam(Team team) {
            this.displayName = team.getDisplayName();
            this.friendlyFlags = team.getFriendlyFlagsBitwise();
            this.nameTagVisibilityRule = team.getNameTagVisibilityRule();
            this.collisionRule = team.getCollisionRule();
            this.color = team.getColor();
            this.prefix = team.getPrefix();
            this.suffix = team.getSuffix();
        }

        public SerializableTeam(RegistryByteBuf buf) {
            this.displayName = (Text)TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf);
            this.friendlyFlags = buf.readByte();
            this.nameTagVisibilityRule = (AbstractTeam.VisibilityRule)AbstractTeam.VisibilityRule.PACKET_CODEC.decode(buf);
            this.collisionRule = (AbstractTeam.CollisionRule)AbstractTeam.CollisionRule.PACKET_CODEC.decode(buf);
            this.color = buf.readEnumConstant(Formatting.class);
            this.prefix = (Text)TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf);
            this.suffix = (Text)TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf);
        }

        public Text getDisplayName() {
            return this.displayName;
        }

        public int getFriendlyFlagsBitwise() {
            return this.friendlyFlags;
        }

        public Formatting getColor() {
            return this.color;
        }

        public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
            return this.nameTagVisibilityRule;
        }

        public AbstractTeam.CollisionRule getCollisionRule() {
            return this.collisionRule;
        }

        public Text getPrefix() {
            return this.prefix;
        }

        public Text getSuffix() {
            return this.suffix;
        }

        public void write(RegistryByteBuf buf) {
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, this.displayName);
            buf.writeByte(this.friendlyFlags);
            AbstractTeam.VisibilityRule.PACKET_CODEC.encode(buf, this.nameTagVisibilityRule);
            AbstractTeam.CollisionRule.PACKET_CODEC.encode(buf, this.collisionRule);
            buf.writeEnumConstant(this.color);
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, this.prefix);
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, this.suffix);
        }
    }

    public static final class Operation
    extends Enum<Operation> {
        public static final /* enum */ Operation ADD = new Operation();
        public static final /* enum */ Operation REMOVE = new Operation();
        private static final /* synthetic */ Operation[] field_29157;

        public static Operation[] values() {
            return (Operation[])field_29157.clone();
        }

        public static Operation valueOf(String string) {
            return Enum.valueOf(Operation.class, string);
        }

        private static /* synthetic */ Operation[] method_36954() {
            return new Operation[]{ADD, REMOVE};
        }

        static {
            field_29157 = Operation.method_36954();
        }
    }
}

