/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry.tag;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

public class TagEntry {
    private static final Codec<TagEntry> ENTRY_CODEC = RecordCodecBuilder.create((T instance) -> instance.group((App)Codecs.TAG_ENTRY_ID.fieldOf("id").forGetter(TagEntry::getIdForCodec), (App)Codec.BOOL.optionalFieldOf("required", (Object)true).forGetter(entry -> entry.required)).apply((Applicative)instance, TagEntry::new));
    public static final Codec<TagEntry> CODEC = Codec.either(Codecs.TAG_ENTRY_ID, ENTRY_CODEC).xmap(either -> (TagEntry)either.map(id -> new TagEntry((Codecs.TagEntryId)id, true), entry -> entry), entry -> entry.required ? Either.left((Object)entry.getIdForCodec()) : Either.right((Object)entry));
    private final Identifier id;
    private final boolean tag;
    private final boolean required;

    private TagEntry(Identifier id, boolean tag, boolean required) {
        this.id = id;
        this.tag = tag;
        this.required = required;
    }

    private TagEntry(Codecs.TagEntryId id, boolean required) {
        this.id = id.id();
        this.tag = id.tag();
        this.required = required;
    }

    private Codecs.TagEntryId getIdForCodec() {
        return new Codecs.TagEntryId(this.id, this.tag);
    }

    public static TagEntry create(Identifier id) {
        return new TagEntry(id, false, true);
    }

    public static TagEntry createOptional(Identifier id) {
        return new TagEntry(id, false, false);
    }

    public static TagEntry createTag(Identifier id) {
        return new TagEntry(id, true, true);
    }

    public static TagEntry createOptionalTag(Identifier id) {
        return new TagEntry(id, true, false);
    }

    public <T> boolean resolve(ValueGetter<T> valueGetter, Consumer<T> idConsumer) {
        if (this.tag) {
            Collection<T> collection = valueGetter.tag(this.id);
            if (collection == null) {
                return !this.required;
            }
            collection.forEach(idConsumer);
        } else {
            T object = valueGetter.direct(this.id, this.required);
            if (object == null) {
                return !this.required;
            }
            idConsumer.accept(object);
        }
        return true;
    }

    public void forEachRequiredTagId(Consumer<Identifier> idConsumer) {
        if (this.tag && this.required) {
            idConsumer.accept(this.id);
        }
    }

    public void forEachOptionalTagId(Consumer<Identifier> idConsumer) {
        if (this.tag && !this.required) {
            idConsumer.accept(this.id);
        }
    }

    public boolean canAdd(Predicate<Identifier> directEntryPredicate, Predicate<Identifier> tagEntryPredicate) {
        return !this.required || (this.tag ? tagEntryPredicate : directEntryPredicate).test(this.id);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.tag) {
            stringBuilder.append('#');
        }
        stringBuilder.append(this.id);
        if (!this.required) {
            stringBuilder.append('?');
        }
        return stringBuilder.toString();
    }

    public static interface ValueGetter<T> {
        public @Nullable T direct(Identifier var1, boolean var2);

        public @Nullable Collection<T> tag(Identifier var1);
    }
}

