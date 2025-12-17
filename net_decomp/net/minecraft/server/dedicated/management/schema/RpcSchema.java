/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dedicated.management.schema;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.dedicated.management.RpcDiscover;
import net.minecraft.server.dedicated.management.RpcKickReason;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.UriUtil;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.IpBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.OperatorsRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayerBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayersRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.ServerRpcDispatcher;
import net.minecraft.server.dedicated.management.schema.RpcSchemaEntry;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.rule.GameRuleType;

public record RpcSchema<T>(Optional<URI> reference, List<String> type, Optional<RpcSchema<?>> items, Map<String, RpcSchema<?>> properties, List<String> enumValues, Codec<T> codec) {
    public static final Codec<? extends RpcSchema<?>> CODEC = Codec.recursive((String)"Schema", codec -> RecordCodecBuilder.create(instance -> instance.group((App)UriUtil.URI_CODEC.optionalFieldOf("$ref").forGetter(RpcSchema::reference), (App)Codecs.listOrSingle(Codec.STRING).optionalFieldOf("type", List.of()).forGetter(RpcSchema::type), (App)codec.optionalFieldOf("items").forGetter(RpcSchema::items), (App)Codec.unboundedMap((Codec)Codec.STRING, (Codec)codec).optionalFieldOf("properties", Map.of()).forGetter(RpcSchema::properties), (App)Codec.STRING.listOf().optionalFieldOf("enum", List.of()).forGetter(RpcSchema::enumValues)).apply((Applicative)instance, (ref, type, items, properties, enumValues) -> null))).validate(schema -> {
        if (schema == null) {
            return DataResult.error(() -> "Should not deserialize schema");
        }
        return DataResult.success((Object)schema);
    });
    private static final List<RpcSchemaEntry<?>> REGISTERED_SCHEMAS = new ArrayList();
    public static final RpcSchema<Boolean> BOOLEAN = RpcSchema.ofLiteral("boolean", Codec.BOOL);
    public static final RpcSchema<Integer> INTEGER = RpcSchema.ofLiteral("integer", Codec.INT);
    public static final RpcSchema<Either<Boolean, Integer>> GAME_RULE_VALUE = RpcSchema.ofLiterals(List.of("boolean", "integer"), Codec.either((Codec)Codec.BOOL, (Codec)Codec.INT));
    public static final RpcSchema<Float> NUMBER = RpcSchema.ofLiteral("number", Codec.FLOAT);
    public static final RpcSchema<String> STRING = RpcSchema.ofLiteral("string", Codec.STRING);
    public static final RpcSchema<UUID> PLAYER_ID = RpcSchema.ofLiteral("string", Uuids.INT_STREAM_CODEC);
    public static final RpcSchema<RpcDiscover.Document> DOCUMENT = RpcSchema.ofLiteral("string", RpcDiscover.Document.CODEC.codec());
    public static final RpcSchemaEntry<Difficulty> DIFFICULTY = RpcSchema.registerEntry("difficulty", RpcSchema.ofEnum(Difficulty::values, Difficulty.CODEC));
    public static final RpcSchemaEntry<GameMode> GAME_MODE = RpcSchema.registerEntry("game_type", RpcSchema.ofEnum(GameMode::values, GameMode.CODEC));
    public static final RpcSchema<PermissionLevel> PERMISSION_LEVEL = RpcSchema.ofLiteral("integer", PermissionLevel.NUMERIC_CODEC);
    public static final RpcSchemaEntry<RpcPlayer> PLAYER = RpcSchema.registerEntry("player", RpcSchema.ofObject(RpcPlayer.CODEC.codec()).withProperty("id", PLAYER_ID).withProperty("name", STRING));
    public static final RpcSchemaEntry<RpcDiscover.Info> VERSION = RpcSchema.registerEntry("version", RpcSchema.ofObject(RpcDiscover.Info.CODEC.codec()).withProperty("name", STRING).withProperty("protocol", INTEGER));
    public static final RpcSchemaEntry<ServerRpcDispatcher.RpcStatus> SERVER_STATE = RpcSchema.registerEntry("server_state", RpcSchema.ofObject(ServerRpcDispatcher.RpcStatus.CODEC).withProperty("started", BOOLEAN).withProperty("players", PLAYER.ref().asArray()).withProperty("version", VERSION.ref()));
    public static final RpcSchema<GameRuleType> GAME_RULE_TYPE = RpcSchema.ofEnum(GameRuleType::values);
    public static final RpcSchemaEntry<GameRuleRpcDispatcher.RuleEntry<?>> TYPED_GAME_RULE = RpcSchema.registerEntry("typed_game_rule", RpcSchema.ofObject(GameRuleRpcDispatcher.RuleEntry.TYPED_CODEC).withProperty("key", STRING).withProperty("value", GAME_RULE_VALUE).withProperty("type", GAME_RULE_TYPE));
    public static final RpcSchemaEntry<GameRuleRpcDispatcher.RuleEntry<?>> UNTYPED_GAME_RULE = RpcSchema.registerEntry("untyped_game_rule", RpcSchema.ofObject(GameRuleRpcDispatcher.RuleEntry.UNTYPED_CODEC).withProperty("key", STRING).withProperty("value", GAME_RULE_VALUE));
    public static final RpcSchemaEntry<RpcKickReason> MESSAGE = RpcSchema.registerEntry("message", RpcSchema.ofObject(RpcKickReason.CODEC).withProperty("literal", STRING).withProperty("translatable", STRING).withProperty("translatableParams", STRING.asArray()));
    public static final RpcSchemaEntry<ServerRpcDispatcher.RpcSystemMessage> SYSTEM_MESSAGE = RpcSchema.registerEntry("system_message", RpcSchema.ofObject(ServerRpcDispatcher.RpcSystemMessage.CODEC).withProperty("message", MESSAGE.ref()).withProperty("overlay", BOOLEAN).withProperty("receivingPlayers", PLAYER.ref().asArray()));
    public static final RpcSchemaEntry<PlayersRpcDispatcher.RpcEntry> KICK_PLAYER = RpcSchema.registerEntry("kick_player", RpcSchema.ofObject(PlayersRpcDispatcher.RpcEntry.CODEC.codec()).withProperty("message", MESSAGE.ref()).withProperty("player", PLAYER.ref()));
    public static final RpcSchemaEntry<OperatorsRpcDispatcher.RpcEntry> OPERATOR = RpcSchema.registerEntry("operator", RpcSchema.ofObject(OperatorsRpcDispatcher.RpcEntry.CODEC.codec()).withProperty("player", PLAYER.ref()).withProperty("bypassesPlayerLimit", BOOLEAN).withProperty("permissionLevel", INTEGER));
    public static final RpcSchemaEntry<IpBansRpcDispatcher.IncomingRpcIpBanData> INCOMING_IP_BAN = RpcSchema.registerEntry("incoming_ip_ban", RpcSchema.ofObject(IpBansRpcDispatcher.IncomingRpcIpBanData.CODEC.codec()).withProperty("player", PLAYER.ref()).withProperty("ip", STRING).withProperty("reason", STRING).withProperty("source", STRING).withProperty("expires", STRING));
    public static final RpcSchemaEntry<IpBansRpcDispatcher.IpBanData> IP_BAN = RpcSchema.registerEntry("ip_ban", RpcSchema.ofObject(IpBansRpcDispatcher.IpBanData.CODEC.codec()).withProperty("ip", STRING).withProperty("reason", STRING).withProperty("source", STRING).withProperty("expires", STRING));
    public static final RpcSchemaEntry<PlayerBansRpcDispatcher.RpcEntry> USER_BAN = RpcSchema.registerEntry("user_ban", RpcSchema.ofObject(PlayerBansRpcDispatcher.RpcEntry.CODEC.codec()).withProperty("player", PLAYER.ref()).withProperty("reason", STRING).withProperty("source", STRING).withProperty("expires", STRING));

    public static <T> Codec<RpcSchema<T>> getCodec() {
        return CODEC;
    }

    public RpcSchema<T> copy() {
        return new RpcSchema<T>(this.reference, this.type, this.items.map(RpcSchema::copy), this.properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> ((RpcSchema)entry.getValue()).copy())), this.enumValues, this.codec);
    }

    private static <T> RpcSchemaEntry<T> registerEntry(String reference, RpcSchema<T> schema) {
        RpcSchemaEntry<T> rpcSchemaEntry = new RpcSchemaEntry<T>(reference, UriUtil.createSchemasUri(reference), schema);
        REGISTERED_SCHEMAS.add(rpcSchemaEntry);
        return rpcSchemaEntry;
    }

    public static List<RpcSchemaEntry<?>> getRegisteredSchemas() {
        return REGISTERED_SCHEMAS;
    }

    public static <T> RpcSchema<T> ofReference(URI reference, Codec<T> codec) {
        return new RpcSchema<T>(Optional.of(reference), List.of(), Optional.empty(), Map.of(), List.of(), codec);
    }

    public static <T> RpcSchema<T> ofLiteral(String literal, Codec<T> codec) {
        return RpcSchema.ofLiterals(List.of(literal), codec);
    }

    public static <T> RpcSchema<T> ofLiterals(List<String> literals, Codec<T> codec) {
        return new RpcSchema<T>(Optional.empty(), literals, Optional.empty(), Map.of(), List.of(), codec);
    }

    public static <E extends Enum<E>> RpcSchema<E> ofEnum(Supplier<E[]> enumValues) {
        return RpcSchema.ofEnum(enumValues, StringIdentifiable.createCodec(enumValues));
    }

    public static <E extends Enum<E>> RpcSchema<E> ofEnum(Supplier<E[]> values, Codec<E> codec) {
        List<String> list = Stream.of((Enum[])values.get()).map(value -> ((StringIdentifiable)value).asString()).toList();
        return RpcSchema.ofList(list, codec);
    }

    public static <T> RpcSchema<T> ofList(List<String> values, Codec<T> codec) {
        return new RpcSchema<T>(Optional.empty(), List.of("string"), Optional.empty(), Map.of(), values, codec);
    }

    public static <T> RpcSchema<List<T>> ofArray(RpcSchema<?> itemSchema, Codec<T> codec) {
        return new RpcSchema<List<T>>(Optional.empty(), List.of("array"), Optional.of(itemSchema), Map.of(), List.of(), codec.listOf());
    }

    public static <T> RpcSchema<T> ofObject(Codec<T> codec) {
        return new RpcSchema<T>(Optional.empty(), List.of("object"), Optional.empty(), Map.of(), List.of(), codec);
    }

    private static <T> RpcSchema<T> ofObjectWithProperties(Map<String, RpcSchema<?>> itemSchemaMap, Codec<T> codec) {
        return new RpcSchema<T>(Optional.empty(), List.of("object"), Optional.empty(), itemSchemaMap, List.of(), codec);
    }

    public RpcSchema<T> withProperty(String reference, RpcSchema<?> schema) {
        HashMap hashMap = new HashMap(this.properties);
        hashMap.put(reference, schema);
        return RpcSchema.ofObjectWithProperties(hashMap, this.codec);
    }

    public RpcSchema<List<T>> asArray() {
        return RpcSchema.ofArray(this, this.codec);
    }
}

