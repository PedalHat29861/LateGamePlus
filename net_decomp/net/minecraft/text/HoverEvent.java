/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import org.jspecify.annotations.Nullable;

public interface HoverEvent {
    public static final Codec<HoverEvent> CODEC = Action.CODEC.dispatch("action", HoverEvent::getAction, action -> action.codec);

    public Action getAction();

    public static final class Action
    extends Enum<Action>
    implements StringIdentifiable {
        public static final /* enum */ Action SHOW_TEXT = new Action("show_text", true, ShowText.CODEC);
        public static final /* enum */ Action SHOW_ITEM = new Action("show_item", true, ShowItem.CODEC);
        public static final /* enum */ Action SHOW_ENTITY = new Action("show_entity", true, ShowEntity.CODEC);
        public static final Codec<Action> UNVALIDATED_CODEC;
        public static final Codec<Action> CODEC;
        private final String name;
        private final boolean parsable;
        final MapCodec<? extends HoverEvent> codec;
        private static final /* synthetic */ Action[] field_55910;

        public static Action[] values() {
            return (Action[])field_55910.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private Action(String name, boolean parsable, MapCodec<? extends HoverEvent> codec) {
            this.name = name;
            this.parsable = parsable;
            this.codec = codec;
        }

        public boolean isParsable() {
            return this.parsable;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<Action> validate(Action action) {
            if (!action.isParsable()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success((Object)action, (Lifecycle)Lifecycle.stable());
        }

        private static /* synthetic */ Action[] method_66576() {
            return new Action[]{SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY};
        }

        static {
            field_55910 = Action.method_66576();
            UNVALIDATED_CODEC = StringIdentifiable.createBasicCodec(Action::values);
            CODEC = UNVALIDATED_CODEC.validate(Action::validate);
        }
    }

    public static class EntityContent {
        public static final MapCodec<EntityContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Registries.ENTITY_TYPE.getCodec().fieldOf("id").forGetter(content -> content.entityType), (App)Uuids.STRICT_CODEC.fieldOf("uuid").forGetter(content -> content.uuid), (App)TextCodecs.CODEC.optionalFieldOf("name").forGetter(content -> content.name)).apply((Applicative)instance, EntityContent::new));
        public final EntityType<?> entityType;
        public final UUID uuid;
        public final Optional<Text> name;
        private @Nullable List<Text> tooltip;

        public EntityContent(EntityType<?> entityType, UUID uuid, @Nullable Text name) {
            this(entityType, uuid, Optional.ofNullable(name));
        }

        public EntityContent(EntityType<?> entityType, UUID uuid, Optional<Text> name) {
            this.entityType = entityType;
            this.uuid = uuid;
            this.name = name;
        }

        public List<Text> asTooltip() {
            if (this.tooltip == null) {
                this.tooltip = new ArrayList<Text>();
                this.name.ifPresent(this.tooltip::add);
                this.tooltip.add(Text.translatable("gui.entity_tooltip.type", this.entityType.getName()));
                this.tooltip.add(Text.literal(this.uuid.toString()));
            }
            return this.tooltip;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            EntityContent entityContent = (EntityContent)o;
            return this.entityType.equals(entityContent.entityType) && this.uuid.equals(entityContent.uuid) && this.name.equals(entityContent.name);
        }

        public int hashCode() {
            int i = this.entityType.hashCode();
            i = 31 * i + this.uuid.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }

    public record ShowEntity(EntityContent entity) implements HoverEvent
    {
        public static final MapCodec<ShowEntity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EntityContent.CODEC.forGetter(ShowEntity::entity)).apply((Applicative)instance, ShowEntity::new));

        @Override
        public Action getAction() {
            return Action.SHOW_ENTITY;
        }
    }

    public record ShowItem(ItemStack item) implements HoverEvent
    {
        public static final MapCodec<ShowItem> CODEC = ItemStack.MAP_CODEC.xmap(ShowItem::new, ShowItem::item);

        public ShowItem(ItemStack stack) {
            this.item = stack = stack.copy();
        }

        @Override
        public Action getAction() {
            return Action.SHOW_ITEM;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ShowItem)) return false;
            ShowItem showItem = (ShowItem)o;
            if (!ItemStack.areEqual(this.item, showItem.item)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return ItemStack.hashCode(this.item);
        }
    }

    public record ShowText(Text value) implements HoverEvent
    {
        public static final MapCodec<ShowText> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextCodecs.CODEC.fieldOf("value").forGetter(ShowText::value)).apply((Applicative)instance, ShowText::new));

        @Override
        public Action getAction() {
            return Action.SHOW_TEXT;
        }
    }
}

