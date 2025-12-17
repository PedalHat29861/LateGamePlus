/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Doubles
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.command;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.permission.PermissionSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.function.Object2FloatFunction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class EntitySelectorReader {
    public static final char SELECTOR_PREFIX = '@';
    private static final char ARGUMENTS_OPENING = '[';
    private static final char ARGUMENTS_CLOSING = ']';
    public static final char ARGUMENT_DEFINER = '=';
    private static final char ARGUMENT_SEPARATOR = ',';
    public static final char INVERT_MODIFIER = '!';
    public static final char TAG_MODIFIER = '#';
    private static final char NEAREST_PLAYER = 'p';
    private static final char ALL_PLAYERS = 'a';
    private static final char RANDOM_PLAYER = 'r';
    private static final char SELF = 's';
    private static final char ALL_ENTITIES = 'e';
    private static final char NEAREST_ENTITY = 'n';
    public static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.invalid"));
    public static final DynamicCommandExceptionType UNKNOWN_SELECTOR_EXCEPTION = new DynamicCommandExceptionType(selectorType -> Text.stringifiedTranslatable("argument.entity.selector.unknown", selectorType));
    public static final SimpleCommandExceptionType NOT_ALLOWED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.selector.not_allowed"));
    public static final SimpleCommandExceptionType MISSING_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.selector.missing"));
    public static final SimpleCommandExceptionType UNTERMINATED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.entity.options.unterminated"));
    public static final DynamicCommandExceptionType VALUELESS_EXCEPTION = new DynamicCommandExceptionType(option -> Text.stringifiedTranslatable("argument.entity.options.valueless", option));
    public static final BiConsumer<Vec3d, List<? extends Entity>> NEAREST = (pos, entities) -> entities.sort((entity1, entity2) -> Doubles.compare((double)entity1.squaredDistanceTo((Vec3d)pos), (double)entity2.squaredDistanceTo((Vec3d)pos)));
    public static final BiConsumer<Vec3d, List<? extends Entity>> FURTHEST = (pos, entities) -> entities.sort((entity1, entity2) -> Doubles.compare((double)entity2.squaredDistanceTo((Vec3d)pos), (double)entity1.squaredDistanceTo((Vec3d)pos)));
    public static final BiConsumer<Vec3d, List<? extends Entity>> RANDOM = (pos, entities) -> Collections.shuffle(entities);
    public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> DEFAULT_SUGGESTION_PROVIDER = (builder, consumer) -> builder.buildFuture();
    private final StringReader reader;
    private final boolean atAllowed;
    private int limit;
    private boolean includesNonPlayers;
    private boolean localWorldOnly;
    private @Nullable NumberRange.DoubleRange distance;
    private @Nullable NumberRange.IntRange levelRange;
    private @Nullable Double x;
    private @Nullable Double y;
    private @Nullable Double z;
    private @Nullable Double dx;
    private @Nullable Double dy;
    private @Nullable Double dz;
    private @Nullable NumberRange.AngleRange pitchRange;
    private @Nullable NumberRange.AngleRange yawRange;
    private final List<Predicate<Entity>> predicates = new ArrayList<Predicate<Entity>>();
    private BiConsumer<Vec3d, List<? extends Entity>> sorter = EntitySelector.ARBITRARY;
    private boolean senderOnly;
    private @Nullable String playerName;
    private int startCursor;
    private @Nullable UUID uuid;
    private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionProvider = DEFAULT_SUGGESTION_PROVIDER;
    private boolean selectsName;
    private boolean excludesName;
    private boolean hasLimit;
    private boolean hasSorter;
    private boolean selectsGameMode;
    private boolean excludesGameMode;
    private boolean selectsTeam;
    private boolean excludesTeam;
    private @Nullable EntityType<?> entityType;
    private boolean excludesEntityType;
    private boolean selectsScores;
    private boolean selectsAdvancements;
    private boolean usesAt;

    public EntitySelectorReader(StringReader reader, boolean atAllowed) {
        this.reader = reader;
        this.atAllowed = atAllowed;
    }

    public static <S> boolean shouldAllowAtSelectors(S source) {
        PermissionSource permissionSource;
        return source instanceof PermissionSource && (permissionSource = (PermissionSource)source).getPermissions().hasPermission(DefaultPermissions.ENTITY_SELECTORS);
    }

    @Deprecated
    public static boolean canUseSelectors(PermissionSource source) {
        return source.getPermissions().hasPermission(DefaultPermissions.ENTITY_SELECTORS);
    }

    public EntitySelector build() {
        Box box;
        if (this.dx != null || this.dy != null || this.dz != null) {
            box = this.createBox(this.dx == null ? 0.0 : this.dx, this.dy == null ? 0.0 : this.dy, this.dz == null ? 0.0 : this.dz);
        } else if (this.distance != null && this.distance.getMax().isPresent()) {
            double d = (Double)this.distance.getMax().get();
            box = new Box(-d, -d, -d, d + 1.0, d + 1.0, d + 1.0);
        } else {
            box = null;
        }
        Function<Vec3d, Vec3d> function = this.x == null && this.y == null && this.z == null ? pos -> pos : pos -> new Vec3d(this.x == null ? pos.x : this.x, this.y == null ? pos.y : this.y, this.z == null ? pos.z : this.z);
        return new EntitySelector(this.limit, this.includesNonPlayers, this.localWorldOnly, List.copyOf(this.predicates), this.distance, function, box, this.sorter, this.senderOnly, this.playerName, this.uuid, this.entityType, this.usesAt);
    }

    private Box createBox(double x, double y, double z) {
        boolean bl = x < 0.0;
        boolean bl2 = y < 0.0;
        boolean bl3 = z < 0.0;
        double d = bl ? x : 0.0;
        double e = bl2 ? y : 0.0;
        double f = bl3 ? z : 0.0;
        double g = (bl ? 0.0 : x) + 1.0;
        double h = (bl2 ? 0.0 : y) + 1.0;
        double i = (bl3 ? 0.0 : z) + 1.0;
        return new Box(d, e, f, g, h, i);
    }

    private void buildPredicate() {
        if (this.pitchRange != null) {
            this.predicates.add(this.rotationPredicate(this.pitchRange, Entity::getPitch));
        }
        if (this.yawRange != null) {
            this.predicates.add(this.rotationPredicate(this.yawRange, Entity::getYaw));
        }
        if (this.levelRange != null) {
            this.predicates.add(entity -> {
                if (!(entity instanceof ServerPlayerEntity)) return false;
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                if (!this.levelRange.test(serverPlayerEntity.experienceLevel)) return false;
                return true;
            });
        }
    }

    private Predicate<Entity> rotationPredicate(NumberRange.AngleRange range, Object2FloatFunction<Entity> rotationGetter) {
        float f = MathHelper.wrapDegrees(range.getMin().orElse(Float.valueOf(0.0f)).floatValue());
        float g = MathHelper.wrapDegrees(range.getMax().orElse(Float.valueOf(359.0f)).floatValue());
        return entity -> {
            float h = MathHelper.wrapDegrees(rotationGetter.applyAsFloat((Entity)entity));
            if (f > g) {
                return h >= f || h <= g;
            }
            return h >= f && h <= g;
        };
    }

    protected void readAtVariable() throws CommandSyntaxException {
        this.usesAt = true;
        this.suggestionProvider = this::suggestSelectorRest;
        if (!this.reader.canRead()) {
            throw MISSING_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
        }
        int i = this.reader.getCursor();
        char c = this.reader.read();
        if (switch (c) {
            case 'p' -> {
                this.limit = 1;
                this.includesNonPlayers = false;
                this.sorter = NEAREST;
                this.setEntityType(EntityType.PLAYER);
                yield false;
            }
            case 'a' -> {
                this.limit = Integer.MAX_VALUE;
                this.includesNonPlayers = false;
                this.sorter = EntitySelector.ARBITRARY;
                this.setEntityType(EntityType.PLAYER);
                yield false;
            }
            case 'r' -> {
                this.limit = 1;
                this.includesNonPlayers = false;
                this.sorter = RANDOM;
                this.setEntityType(EntityType.PLAYER);
                yield false;
            }
            case 's' -> {
                this.limit = 1;
                this.includesNonPlayers = true;
                this.senderOnly = true;
                yield false;
            }
            case 'e' -> {
                this.limit = Integer.MAX_VALUE;
                this.includesNonPlayers = true;
                this.sorter = EntitySelector.ARBITRARY;
                yield true;
            }
            case 'n' -> {
                this.limit = 1;
                this.includesNonPlayers = true;
                this.sorter = NEAREST;
                yield true;
            }
            default -> {
                this.reader.setCursor(i);
                throw UNKNOWN_SELECTOR_EXCEPTION.createWithContext((ImmutableStringReader)this.reader, (Object)("@" + String.valueOf(c)));
            }
        }) {
            this.predicates.add(Entity::isAlive);
        }
        this.suggestionProvider = this::suggestOpen;
        if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.suggestionProvider = this::suggestOptionOrEnd;
            this.readArguments();
        }
    }

    protected void readRegular() throws CommandSyntaxException {
        if (this.reader.canRead()) {
            this.suggestionProvider = this::suggestNormal;
        }
        int i = this.reader.getCursor();
        String string = this.reader.readString();
        try {
            this.uuid = UUID.fromString(string);
            this.includesNonPlayers = true;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            if (string.isEmpty() || string.length() > 16) {
                this.reader.setCursor(i);
                throw INVALID_ENTITY_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
            }
            this.includesNonPlayers = false;
            this.playerName = string;
        }
        this.limit = 1;
    }

    protected void readArguments() throws CommandSyntaxException {
        this.suggestionProvider = this::suggestOption;
        this.reader.skipWhitespace();
        while (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String string = this.reader.readString();
            EntitySelectorOptions.SelectorHandler selectorHandler = EntitySelectorOptions.getHandler(this, string, i);
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
                this.reader.setCursor(i);
                throw VALUELESS_EXCEPTION.createWithContext((ImmutableStringReader)this.reader, (Object)string);
            }
            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestionProvider = DEFAULT_SUGGESTION_PROVIDER;
            selectorHandler.handle(this);
            this.reader.skipWhitespace();
            this.suggestionProvider = this::suggestEndNext;
            if (!this.reader.canRead()) continue;
            if (this.reader.peek() == ',') {
                this.reader.skip();
                this.suggestionProvider = this::suggestOption;
                continue;
            }
            if (this.reader.peek() == ']') break;
            throw UNTERMINATED_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
        }
        if (!this.reader.canRead()) {
            throw UNTERMINATED_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
        }
        this.reader.skip();
        this.suggestionProvider = DEFAULT_SUGGESTION_PROVIDER;
    }

    public boolean readNegationCharacter() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == '!') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        }
        return false;
    }

    public boolean readTagCharacter() {
        this.reader.skipWhitespace();
        if (this.reader.canRead() && this.reader.peek() == '#') {
            this.reader.skip();
            this.reader.skipWhitespace();
            return true;
        }
        return false;
    }

    public StringReader getReader() {
        return this.reader;
    }

    public void addPredicate(Predicate<Entity> predicate) {
        this.predicates.add(predicate);
    }

    public void setLocalWorldOnly() {
        this.localWorldOnly = true;
    }

    public @Nullable NumberRange.DoubleRange getDistance() {
        return this.distance;
    }

    public void setDistance(NumberRange.DoubleRange distance) {
        this.distance = distance;
    }

    public @Nullable NumberRange.IntRange getLevelRange() {
        return this.levelRange;
    }

    public void setLevelRange(NumberRange.IntRange levelRange) {
        this.levelRange = levelRange;
    }

    public @Nullable NumberRange.AngleRange getPitchRange() {
        return this.pitchRange;
    }

    public void setPitchRange(NumberRange.AngleRange pitchRange) {
        this.pitchRange = pitchRange;
    }

    public @Nullable NumberRange.AngleRange getYawRange() {
        return this.yawRange;
    }

    public void setYawRange(NumberRange.AngleRange yawRange) {
        this.yawRange = yawRange;
    }

    public @Nullable Double getX() {
        return this.x;
    }

    public @Nullable Double getY() {
        return this.y;
    }

    public @Nullable Double getZ() {
        return this.z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public void setDz(double dz) {
        this.dz = dz;
    }

    public @Nullable Double getDx() {
        return this.dx;
    }

    public @Nullable Double getDy() {
        return this.dy;
    }

    public @Nullable Double getDz() {
        return this.dz;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setIncludesNonPlayers(boolean includesNonPlayers) {
        this.includesNonPlayers = includesNonPlayers;
    }

    public BiConsumer<Vec3d, List<? extends Entity>> getSorter() {
        return this.sorter;
    }

    public void setSorter(BiConsumer<Vec3d, List<? extends Entity>> sorter) {
        this.sorter = sorter;
    }

    public EntitySelector read() throws CommandSyntaxException {
        this.startCursor = this.reader.getCursor();
        this.suggestionProvider = this::suggestSelector;
        if (this.reader.canRead() && this.reader.peek() == '@') {
            if (!this.atAllowed) {
                throw NOT_ALLOWED_EXCEPTION.createWithContext((ImmutableStringReader)this.reader);
            }
            this.reader.skip();
            this.readAtVariable();
        } else {
            this.readRegular();
        }
        this.buildPredicate();
        return this.build();
    }

    private static void suggestSelector(SuggestionsBuilder builder) {
        builder.suggest("@p", (Message)Text.translatable("argument.entity.selector.nearestPlayer"));
        builder.suggest("@a", (Message)Text.translatable("argument.entity.selector.allPlayers"));
        builder.suggest("@r", (Message)Text.translatable("argument.entity.selector.randomPlayer"));
        builder.suggest("@s", (Message)Text.translatable("argument.entity.selector.self"));
        builder.suggest("@e", (Message)Text.translatable("argument.entity.selector.allEntities"));
        builder.suggest("@n", (Message)Text.translatable("argument.entity.selector.nearestEntity"));
    }

    private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        consumer.accept(builder);
        if (this.atAllowed) {
            EntitySelectorReader.suggestSelector(builder);
        }
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestNormal(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder = builder.createOffset(this.startCursor);
        consumer.accept(suggestionsBuilder);
        return builder.add(suggestionsBuilder).buildFuture();
    }

    private CompletableFuture<Suggestions> suggestSelectorRest(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        SuggestionsBuilder suggestionsBuilder = builder.createOffset(builder.getStart() - 1);
        EntitySelectorReader.suggestSelector(suggestionsBuilder);
        builder.add(suggestionsBuilder);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOpen(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        builder.suggest(String.valueOf('['));
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOptionOrEnd(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        builder.suggest(String.valueOf(']'));
        EntitySelectorOptions.suggestOptions(this, builder);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestOption(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        EntitySelectorOptions.suggestOptions(this, builder);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestEndNext(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        builder.suggest(String.valueOf(','));
        builder.suggest(String.valueOf(']'));
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestDefinerNext(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        builder.suggest(String.valueOf('='));
        return builder.buildFuture();
    }

    public boolean isSenderOnly() {
        return this.senderOnly;
    }

    public void setSuggestionProvider(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestionProvider) {
        this.suggestionProvider = suggestionProvider;
    }

    public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) {
        return this.suggestionProvider.apply(builder.createOffset(this.reader.getCursor()), consumer);
    }

    public boolean selectsName() {
        return this.selectsName;
    }

    public void setSelectsName(boolean selectsName) {
        this.selectsName = selectsName;
    }

    public boolean excludesName() {
        return this.excludesName;
    }

    public void setExcludesName(boolean excludesName) {
        this.excludesName = excludesName;
    }

    public boolean hasLimit() {
        return this.hasLimit;
    }

    public void setHasLimit(boolean hasLimit) {
        this.hasLimit = hasLimit;
    }

    public boolean hasSorter() {
        return this.hasSorter;
    }

    public void setHasSorter(boolean hasSorter) {
        this.hasSorter = hasSorter;
    }

    public boolean selectsGameMode() {
        return this.selectsGameMode;
    }

    public void setSelectsGameMode(boolean selectsGameMode) {
        this.selectsGameMode = selectsGameMode;
    }

    public boolean excludesGameMode() {
        return this.excludesGameMode;
    }

    public void setExcludesGameMode(boolean excludesGameMode) {
        this.excludesGameMode = excludesGameMode;
    }

    public boolean selectsTeam() {
        return this.selectsTeam;
    }

    public void setSelectsTeam(boolean selectsTeam) {
        this.selectsTeam = selectsTeam;
    }

    public boolean excludesTeam() {
        return this.excludesTeam;
    }

    public void setExcludesTeam(boolean excludesTeam) {
        this.excludesTeam = excludesTeam;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public void setExcludesEntityType() {
        this.excludesEntityType = true;
    }

    public boolean selectsEntityType() {
        return this.entityType != null;
    }

    public boolean excludesEntityType() {
        return this.excludesEntityType;
    }

    public boolean selectsScores() {
        return this.selectsScores;
    }

    public void setSelectsScores(boolean selectsScores) {
        this.selectsScores = selectsScores;
    }

    public boolean selectsAdvancements() {
        return this.selectsAdvancements;
    }

    public void setSelectsAdvancements(boolean selectsAdvancements) {
        this.selectsAdvancements = selectsAdvancements;
    }
}

