/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class EntityAnchorArgumentType
implements ArgumentType<EntityAnchor> {
    private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
    private static final DynamicCommandExceptionType INVALID_ANCHOR_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("argument.anchor.invalid", name));

    public static EntityAnchor getEntityAnchor(CommandContext<ServerCommandSource> context, String name) {
        return (EntityAnchor)((Object)context.getArgument(name, EntityAnchor.class));
    }

    public static EntityAnchorArgumentType entityAnchor() {
        return new EntityAnchorArgumentType();
    }

    public EntityAnchor parse(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        String string = stringReader.readUnquotedString();
        EntityAnchor entityAnchor = EntityAnchor.fromId(string);
        if (entityAnchor == null) {
            stringReader.setCursor(i);
            throw INVALID_ANCHOR_EXCEPTION.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
        return entityAnchor;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(EntityAnchor.ANCHORS.keySet(), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }

    public static final class EntityAnchor
    extends Enum<EntityAnchor> {
        public static final /* enum */ EntityAnchor FEET = new EntityAnchor("feet", (pos, entity) -> pos);
        public static final /* enum */ EntityAnchor EYES = new EntityAnchor("eyes", (pos, entity) -> new Vec3d(pos.x, pos.y + (double)entity.getStandingEyeHeight(), pos.z));
        static final Map<String, EntityAnchor> ANCHORS;
        private final String id;
        private final BiFunction<Vec3d, Entity, Vec3d> offset;
        private static final /* synthetic */ EntityAnchor[] field_9850;

        public static EntityAnchor[] values() {
            return (EntityAnchor[])field_9850.clone();
        }

        public static EntityAnchor valueOf(String string) {
            return Enum.valueOf(EntityAnchor.class, string);
        }

        private EntityAnchor(String id, BiFunction<Vec3d, Entity, Vec3d> offset) {
            this.id = id;
            this.offset = offset;
        }

        public static @Nullable EntityAnchor fromId(String id) {
            return ANCHORS.get(id);
        }

        public Vec3d positionAt(Entity entity) {
            return this.offset.apply(entity.getEntityPos(), entity);
        }

        public Vec3d positionAt(ServerCommandSource source) {
            Entity entity = source.getEntity();
            if (entity == null) {
                return source.getPosition();
            }
            return this.offset.apply(source.getPosition(), entity);
        }

        private static /* synthetic */ EntityAnchor[] method_36814() {
            return new EntityAnchor[]{FEET, EYES};
        }

        static {
            field_9850 = EntityAnchor.method_36814();
            ANCHORS = Util.make(Maps.newHashMap(), map -> {
                for (EntityAnchor entityAnchor : EntityAnchor.values()) {
                    map.put(entityAnchor.id, entityAnchor);
                }
            });
        }
    }
}

