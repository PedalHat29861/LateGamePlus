/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class EntitySelectorOptions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, SelectorOption> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType UNKNOWN_OPTION_EXCEPTION = new DynamicCommandExceptionType(option -> Text.stringifiedTranslatable("argument.entity.options.unknown", option));
    public static final DynamicCommandExceptionType INAPPLICABLE_OPTION_EXCEPTION = new DynamicCommandExceptionType(option -> Text.stringifiedTranslatable("argument.entity.options.inapplicable", option));
    public static final SimpleCommandExceptionType NEGATIVE_DISTANCE_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.options.distance.negative"));
    public static final SimpleCommandExceptionType NEGATIVE_LEVEL_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.options.level.negative"));
    public static final SimpleCommandExceptionType TOO_SMALL_LEVEL_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.options.limit.toosmall"));
    public static final DynamicCommandExceptionType IRREVERSIBLE_SORT_EXCEPTION = new DynamicCommandExceptionType(sortType -> Text.stringifiedTranslatable("argument.entity.options.sort.irreversible", sortType));
    public static final DynamicCommandExceptionType INVALID_MODE_EXCEPTION = new DynamicCommandExceptionType(gameMode -> Text.stringifiedTranslatable("argument.entity.options.mode.invalid", gameMode));
    public static final DynamicCommandExceptionType INVALID_TYPE_EXCEPTION = new DynamicCommandExceptionType(entity -> Text.stringifiedTranslatable("argument.entity.options.type.invalid", entity));

    private static void putOption(String id, SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
        OPTIONS.put(id, new SelectorOption(handler, condition, description));
    }

    public static void register() {
        if (!OPTIONS.isEmpty()) {
            return;
        }
        EntitySelectorOptions.putOption("name", reader -> {
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readString();
            if (reader.excludesName() && !bl) {
                reader.getReader().setCursor(i);
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)"name");
            }
            if (bl) {
                reader.setExcludesName(true);
            } else {
                reader.setSelectsName(true);
            }
            reader.addPredicate(entity -> entity.getStringifiedName().equals(string) != bl);
        }, reader -> !reader.selectsName(), Text.translatable("argument.entity.options.name.description"));
        EntitySelectorOptions.putOption("distance", reader -> {
            int i = reader.getReader().getCursor();
            NumberRange.DoubleRange doubleRange = NumberRange.DoubleRange.parse(reader.getReader());
            if (doubleRange.getMin().isPresent() && (Double)doubleRange.getMin().get() < 0.0 || doubleRange.getMax().isPresent() && (Double)doubleRange.getMax().get() < 0.0) {
                reader.getReader().setCursor(i);
                throw NEGATIVE_DISTANCE_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader());
            }
            reader.setDistance(doubleRange);
            reader.setLocalWorldOnly();
        }, reader -> reader.getDistance() == null, Text.translatable("argument.entity.options.distance.description"));
        EntitySelectorOptions.putOption("level", reader -> {
            int i = reader.getReader().getCursor();
            NumberRange.IntRange intRange = NumberRange.IntRange.parse(reader.getReader());
            if (intRange.getMin().isPresent() && (Integer)intRange.getMin().get() < 0 || intRange.getMax().isPresent() && (Integer)intRange.getMax().get() < 0) {
                reader.getReader().setCursor(i);
                throw NEGATIVE_LEVEL_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader());
            }
            reader.setLevelRange(intRange);
            reader.setIncludesNonPlayers(false);
        }, reader -> reader.getLevelRange() == null, Text.translatable("argument.entity.options.level.description"));
        EntitySelectorOptions.putOption("x", reader -> {
            reader.setLocalWorldOnly();
            reader.setX(reader.getReader().readDouble());
        }, reader -> reader.getX() == null, Text.translatable("argument.entity.options.x.description"));
        EntitySelectorOptions.putOption("y", reader -> {
            reader.setLocalWorldOnly();
            reader.setY(reader.getReader().readDouble());
        }, reader -> reader.getY() == null, Text.translatable("argument.entity.options.y.description"));
        EntitySelectorOptions.putOption("z", reader -> {
            reader.setLocalWorldOnly();
            reader.setZ(reader.getReader().readDouble());
        }, reader -> reader.getZ() == null, Text.translatable("argument.entity.options.z.description"));
        EntitySelectorOptions.putOption("dx", reader -> {
            reader.setLocalWorldOnly();
            reader.setDx(reader.getReader().readDouble());
        }, reader -> reader.getDx() == null, Text.translatable("argument.entity.options.dx.description"));
        EntitySelectorOptions.putOption("dy", reader -> {
            reader.setLocalWorldOnly();
            reader.setDy(reader.getReader().readDouble());
        }, reader -> reader.getDy() == null, Text.translatable("argument.entity.options.dy.description"));
        EntitySelectorOptions.putOption("dz", reader -> {
            reader.setLocalWorldOnly();
            reader.setDz(reader.getReader().readDouble());
        }, reader -> reader.getDz() == null, Text.translatable("argument.entity.options.dz.description"));
        EntitySelectorOptions.putOption("x_rotation", reader -> reader.setPitchRange(NumberRange.AngleRange.parse(reader.getReader())), reader -> reader.getPitchRange() == null, Text.translatable("argument.entity.options.x_rotation.description"));
        EntitySelectorOptions.putOption("y_rotation", reader -> reader.setYawRange(NumberRange.AngleRange.parse(reader.getReader())), reader -> reader.getYawRange() == null, Text.translatable("argument.entity.options.y_rotation.description"));
        EntitySelectorOptions.putOption("limit", reader -> {
            int i = reader.getReader().getCursor();
            int j = reader.getReader().readInt();
            if (j < 1) {
                reader.getReader().setCursor(i);
                throw TOO_SMALL_LEVEL_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader());
            }
            reader.setLimit(j);
            reader.setHasLimit(true);
        }, reader -> !reader.isSenderOnly() && !reader.hasLimit(), Text.translatable("argument.entity.options.limit.description"));
        EntitySelectorOptions.putOption("sort", reader -> {
            int i = reader.getReader().getCursor();
            String string = reader.getReader().readUnquotedString();
            reader.setSuggestionProvider((builder, consumer) -> CommandSource.suggestMatching(Arrays.asList("nearest", "furthest", "random", "arbitrary"), builder));
            reader.setSorter(switch (string) {
                case "nearest" -> EntitySelectorReader.NEAREST;
                case "furthest" -> EntitySelectorReader.FURTHEST;
                case "random" -> EntitySelectorReader.RANDOM;
                case "arbitrary" -> EntitySelector.ARBITRARY;
                default -> {
                    reader.getReader().setCursor(i);
                    throw IRREVERSIBLE_SORT_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)string);
                }
            });
            reader.setHasSorter(true);
        }, reader -> !reader.isSenderOnly() && !reader.hasSorter(), Text.translatable("argument.entity.options.sort.description"));
        EntitySelectorOptions.putOption("gamemode", reader -> {
            reader.setSuggestionProvider((builder, consumer) -> {
                String string = builder.getRemaining().toLowerCase(Locale.ROOT);
                boolean bl = !reader.excludesGameMode();
                boolean bl2 = true;
                if (!string.isEmpty()) {
                    if (string.charAt(0) == '!') {
                        bl = false;
                        string = string.substring(1);
                    } else {
                        bl2 = false;
                    }
                }
                for (GameMode gameMode : GameMode.values()) {
                    if (!gameMode.getId().toLowerCase(Locale.ROOT).startsWith(string)) continue;
                    if (bl2) {
                        builder.suggest("!" + gameMode.getId());
                    }
                    if (!bl) continue;
                    builder.suggest(gameMode.getId());
                }
                return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesGameMode() && !bl) {
                reader.getReader().setCursor(i);
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)"gamemode");
            }
            String string = reader.getReader().readUnquotedString();
            GameMode gameMode = GameMode.byId(string, null);
            if (gameMode == null) {
                reader.getReader().setCursor(i);
                throw INVALID_MODE_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)string);
            }
            reader.setIncludesNonPlayers(false);
            reader.addPredicate(entity -> {
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                    GameMode gameMode2 = serverPlayerEntity.getGameMode();
                    return gameMode2 == gameMode ^ bl;
                }
                return false;
            });
            if (bl) {
                reader.setExcludesGameMode(true);
            } else {
                reader.setSelectsGameMode(true);
            }
        }, reader -> !reader.selectsGameMode(), Text.translatable("argument.entity.options.gamemode.description"));
        EntitySelectorOptions.putOption("team", reader -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            reader.addPredicate(entity -> {
                Team abstractTeam = entity.getScoreboardTeam();
                String string2 = abstractTeam == null ? "" : ((AbstractTeam)abstractTeam).getName();
                return string2.equals(string) != bl;
            });
            if (bl) {
                reader.setExcludesTeam(true);
            } else {
                reader.setSelectsTeam(true);
            }
        }, reader -> !reader.selectsTeam(), Text.translatable("argument.entity.options.team.description"));
        EntitySelectorOptions.putOption("type", reader -> {
            reader.setSuggestionProvider((builder, consumer) -> {
                CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder, String.valueOf('!'));
                CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(tag -> tag.getTag().id()), builder, "!#");
                if (!reader.excludesEntityType()) {
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.getIds(), builder);
                    CommandSource.suggestIdentifiers(Registries.ENTITY_TYPE.streamTags().map(tag -> tag.getTag().id()), builder, String.valueOf('#'));
                }
                return builder.buildFuture();
            });
            int i = reader.getReader().getCursor();
            boolean bl = reader.readNegationCharacter();
            if (reader.excludesEntityType() && !bl) {
                reader.getReader().setCursor(i);
                throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)"type");
            }
            if (bl) {
                reader.setExcludesEntityType();
            }
            if (reader.readTagCharacter()) {
                TagKey<EntityType<?>> tagKey = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.fromCommandInput(reader.getReader()));
                reader.addPredicate(entity -> entity.getType().isIn(tagKey) != bl);
            } else {
                Identifier identifier = Identifier.fromCommandInput(reader.getReader());
                EntityType entityType = (EntityType)Registries.ENTITY_TYPE.getOptionalValue(identifier).orElseThrow(() -> {
                    reader.getReader().setCursor(i);
                    return INVALID_TYPE_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)identifier.toString());
                });
                if (Objects.equals(EntityType.PLAYER, entityType) && !bl) {
                    reader.setIncludesNonPlayers(false);
                }
                reader.addPredicate(entity -> Objects.equals(entityType, entity.getType()) != bl);
                if (!bl) {
                    reader.setEntityType(entityType);
                }
            }
        }, reader -> !reader.selectsEntityType(), Text.translatable("argument.entity.options.type.description"));
        EntitySelectorOptions.putOption("tag", reader -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            reader.addPredicate(entity -> {
                if ("".equals(string)) {
                    return entity.getCommandTags().isEmpty() != bl;
                }
                return entity.getCommandTags().contains(string) != bl;
            });
        }, reader -> true, Text.translatable("argument.entity.options.tag.description"));
        EntitySelectorOptions.putOption("nbt", reader -> {
            boolean bl = reader.readNegationCharacter();
            NbtCompound nbtCompound = StringNbtReader.readCompoundAsArgument(reader.getReader());
            reader.addPredicate(entity -> {
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(entity.getErrorReporterContext(), LOGGER);){
                    ServerPlayerEntity serverPlayerEntity;
                    ItemStack itemStack;
                    NbtWriteView nbtWriteView = NbtWriteView.create(logging, entity.getRegistryManager());
                    entity.writeData(nbtWriteView);
                    if (entity instanceof ServerPlayerEntity && !(itemStack = (serverPlayerEntity = (ServerPlayerEntity)entity).getInventory().getSelectedStack()).isEmpty()) {
                        nbtWriteView.put("SelectedItem", ItemStack.CODEC, itemStack);
                    }
                    boolean bl2 = NbtHelper.matches(nbtCompound, nbtWriteView.getNbt(), true) != bl;
                    return bl2;
                }
            });
        }, reader -> true, Text.translatable("argument.entity.options.nbt.description"));
        EntitySelectorOptions.putOption("scores", reader -> {
            StringReader stringReader = reader.getReader();
            HashMap map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                String string = stringReader.readUnquotedString();
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                NumberRange.IntRange intRange = NumberRange.IntRange.parse(stringReader);
                map.put(string, intRange);
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!map.isEmpty()) {
                reader.addPredicate(entity -> {
                    ServerScoreboard scoreboard = entity.getEntityWorld().getServer().getScoreboard();
                    for (Map.Entry entry : map.entrySet()) {
                        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective((String)entry.getKey());
                        if (scoreboardObjective == null) {
                            return false;
                        }
                        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore((ScoreHolder)entity, scoreboardObjective);
                        if (readableScoreboardScore == null) {
                            return false;
                        }
                        if (((NumberRange.IntRange)entry.getValue()).test(readableScoreboardScore.getScore())) continue;
                        return false;
                    }
                    return true;
                });
            }
            reader.setSelectsScores(true);
        }, reader -> !reader.selectsScores(), Text.translatable("argument.entity.options.scores.description"));
        EntitySelectorOptions.putOption("advancements", reader -> {
            StringReader stringReader = reader.getReader();
            HashMap map = Maps.newHashMap();
            stringReader.expect('{');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != '}') {
                stringReader.skipWhitespace();
                Identifier identifier = Identifier.fromCommandInput(stringReader);
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                if (stringReader.canRead() && stringReader.peek() == '{') {
                    HashMap map2 = Maps.newHashMap();
                    stringReader.skipWhitespace();
                    stringReader.expect('{');
                    stringReader.skipWhitespace();
                    while (stringReader.canRead() && stringReader.peek() != '}') {
                        stringReader.skipWhitespace();
                        String string = stringReader.readUnquotedString();
                        stringReader.skipWhitespace();
                        stringReader.expect('=');
                        stringReader.skipWhitespace();
                        boolean bl = stringReader.readBoolean();
                        map2.put(string, criterionProgress -> criterionProgress.isObtained() == bl);
                        stringReader.skipWhitespace();
                        if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                        stringReader.skip();
                    }
                    stringReader.skipWhitespace();
                    stringReader.expect('}');
                    stringReader.skipWhitespace();
                    map.put(identifier, advancementProgress -> {
                        for (Map.Entry entry : map2.entrySet()) {
                            CriterionProgress criterionProgress = advancementProgress.getCriterionProgress((String)entry.getKey());
                            if (criterionProgress != null && ((Predicate)entry.getValue()).test(criterionProgress)) continue;
                            return false;
                        }
                        return true;
                    });
                } else {
                    boolean bl2 = stringReader.readBoolean();
                    map.put(identifier, advancementProgress -> advancementProgress.isDone() == bl2);
                }
                stringReader.skipWhitespace();
                if (!stringReader.canRead() || stringReader.peek() != ',') continue;
                stringReader.skip();
            }
            stringReader.expect('}');
            if (!map.isEmpty()) {
                reader.addPredicate(entity -> {
                    if (!(entity instanceof ServerPlayerEntity)) {
                        return false;
                    }
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                    PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
                    ServerAdvancementLoader serverAdvancementLoader = serverPlayerEntity.getEntityWorld().getServer().getAdvancementLoader();
                    for (Map.Entry entry : map.entrySet()) {
                        AdvancementEntry advancementEntry = serverAdvancementLoader.get((Identifier)entry.getKey());
                        if (advancementEntry != null && ((Predicate)entry.getValue()).test(playerAdvancementTracker.getProgress(advancementEntry))) continue;
                        return false;
                    }
                    return true;
                });
                reader.setIncludesNonPlayers(false);
            }
            reader.setSelectsAdvancements(true);
        }, reader -> !reader.selectsAdvancements(), Text.translatable("argument.entity.options.advancements.description"));
        EntitySelectorOptions.putOption("predicate", reader -> {
            boolean bl = reader.readNegationCharacter();
            RegistryKey<LootCondition> registryKey = RegistryKey.of(RegistryKeys.PREDICATE, Identifier.fromCommandInput(reader.getReader()));
            reader.addPredicate(entity -> {
                World world = entity.getEntityWorld();
                if (!(world instanceof ServerWorld)) {
                    return false;
                }
                ServerWorld serverWorld = (ServerWorld)world;
                Optional<LootCondition> optional = serverWorld.getServer().getReloadableRegistries().createRegistryLookup().getOptionalEntry(registryKey).map(RegistryEntry::value);
                if (optional.isEmpty()) {
                    return false;
                }
                LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverWorld).add(LootContextParameters.THIS_ENTITY, entity).add(LootContextParameters.ORIGIN, entity.getEntityPos()).build(LootContextTypes.SELECTOR);
                LootContext lootContext = new LootContext.Builder(lootWorldContext).build(Optional.empty());
                lootContext.markActive(LootContext.predicate(optional.get()));
                return bl ^ optional.get().test(lootContext);
            });
        }, reader -> true, Text.translatable("argument.entity.options.predicate.description"));
    }

    public static SelectorHandler getHandler(EntitySelectorReader reader, String option, int restoreCursor) throws CommandSyntaxException {
        SelectorOption selectorOption = OPTIONS.get(option);
        if (selectorOption != null) {
            if (selectorOption.condition.test(reader)) {
                return selectorOption.handler;
            }
            throw INAPPLICABLE_OPTION_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)option);
        }
        reader.getReader().setCursor(restoreCursor);
        throw UNKNOWN_OPTION_EXCEPTION.createWithContext((ImmutableStringReader)reader.getReader(), (Object)option);
    }

    public static void suggestOptions(EntitySelectorReader reader, SuggestionsBuilder suggestionBuilder) {
        String string = suggestionBuilder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, SelectorOption> entry : OPTIONS.entrySet()) {
            if (!entry.getValue().condition.test(reader) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(string)) continue;
            suggestionBuilder.suggest(entry.getKey() + "=", (Message)entry.getValue().description);
        }
    }

    static final class SelectorOption
    extends Record {
        final SelectorHandler handler;
        final Predicate<EntitySelectorReader> condition;
        final Text description;

        SelectorOption(SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
            this.handler = handler;
            this.condition = condition;
            this.description = description;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SelectorOption.class, "modifier;canUse;description", "handler", "condition", "description"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SelectorOption.class, "modifier;canUse;description", "handler", "condition", "description"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SelectorOption.class, "modifier;canUse;description", "handler", "condition", "description"}, this, object);
        }

        public SelectorHandler handler() {
            return this.handler;
        }

        public Predicate<EntitySelectorReader> condition() {
            return this.condition;
        }

        public Text description() {
            return this.description;
        }
    }

    @FunctionalInterface
    public static interface SelectorHandler {
        public void handle(EntitySelectorReader var1) throws CommandSyntaxException;
    }
}

