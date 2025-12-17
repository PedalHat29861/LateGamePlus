/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.message;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Decoration;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record MessageType(Decoration chat, Decoration narration) {
    public static final Codec<MessageType> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Decoration.CODEC.fieldOf("chat").forGetter(MessageType::chat), (App)Decoration.CODEC.fieldOf("narration").forGetter(MessageType::narration)).apply((Applicative)instance, MessageType::new));
    public static final PacketCodec<RegistryByteBuf, MessageType> PACKET_CODEC = PacketCodec.tuple(Decoration.PACKET_CODEC, MessageType::chat, Decoration.PACKET_CODEC, MessageType::narration, MessageType::new);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<MessageType>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.MESSAGE_TYPE, PACKET_CODEC);
    public static final Decoration CHAT_TEXT_DECORATION = Decoration.ofChat("chat.type.text");
    public static final RegistryKey<MessageType> CHAT = MessageType.register("chat");
    public static final RegistryKey<MessageType> SAY_COMMAND = MessageType.register("say_command");
    public static final RegistryKey<MessageType> MSG_COMMAND_INCOMING = MessageType.register("msg_command_incoming");
    public static final RegistryKey<MessageType> MSG_COMMAND_OUTGOING = MessageType.register("msg_command_outgoing");
    public static final RegistryKey<MessageType> TEAM_MSG_COMMAND_INCOMING = MessageType.register("team_msg_command_incoming");
    public static final RegistryKey<MessageType> TEAM_MSG_COMMAND_OUTGOING = MessageType.register("team_msg_command_outgoing");
    public static final RegistryKey<MessageType> EMOTE_COMMAND = MessageType.register("emote_command");

    private static RegistryKey<MessageType> register(String id) {
        return RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.ofVanilla(id));
    }

    public static void bootstrap(Registerable<MessageType> messageTypeRegisterable) {
        messageTypeRegisterable.register(CHAT, new MessageType(CHAT_TEXT_DECORATION, Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(SAY_COMMAND, new MessageType(Decoration.ofChat("chat.type.announcement"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(MSG_COMMAND_INCOMING, new MessageType(Decoration.ofIncomingMessage("commands.message.display.incoming"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(MSG_COMMAND_OUTGOING, new MessageType(Decoration.ofOutgoingMessage("commands.message.display.outgoing"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(TEAM_MSG_COMMAND_INCOMING, new MessageType(Decoration.ofTeamMessage("chat.type.team.text"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(TEAM_MSG_COMMAND_OUTGOING, new MessageType(Decoration.ofTeamMessage("chat.type.team.sent"), Decoration.ofChat("chat.type.text.narrate")));
        messageTypeRegisterable.register(EMOTE_COMMAND, new MessageType(Decoration.ofChat("chat.type.emote"), Decoration.ofChat("chat.type.emote")));
    }

    public static Parameters params(RegistryKey<MessageType> typeKey, Entity entity) {
        return MessageType.params(typeKey, entity.getEntityWorld().getRegistryManager(), entity.getDisplayName());
    }

    public static Parameters params(RegistryKey<MessageType> typeKey, ServerCommandSource source) {
        return MessageType.params(typeKey, source.getRegistryManager(), source.getDisplayName());
    }

    public static Parameters params(RegistryKey<MessageType> typeKey, DynamicRegistryManager registryManager, Text name) {
        RegistryWrapper.Impl registry = registryManager.getOrThrow(RegistryKeys.MESSAGE_TYPE);
        return new Parameters(registry.getOrThrow(typeKey), name);
    }

    public record Parameters(RegistryEntry<MessageType> type, Text name, Optional<Text> targetName) {
        public static final PacketCodec<RegistryByteBuf, Parameters> CODEC = PacketCodec.tuple(ENTRY_PACKET_CODEC, Parameters::type, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, Parameters::name, TextCodecs.OPTIONAL_UNLIMITED_REGISTRY_PACKET_CODEC, Parameters::targetName, Parameters::new);

        Parameters(RegistryEntry<MessageType> type, Text name) {
            this(type, name, Optional.empty());
        }

        public Text applyChatDecoration(Text content) {
            return this.type.value().chat().apply(content, this);
        }

        public Text applyNarrationDecoration(Text content) {
            return this.type.value().narration().apply(content, this);
        }

        public Parameters withTargetName(Text targetName) {
            return new Parameters(this.type, this.name, Optional.of(targetName));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Parameters.class, "chatType;name;targetName", "type", "name", "targetName"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Parameters.class, "chatType;name;targetName", "type", "name", "targetName"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Parameters.class, "chatType;name;targetName", "type", "name", "targetName"}, this, object);
        }
    }
}

