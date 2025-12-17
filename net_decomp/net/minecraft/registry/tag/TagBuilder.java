/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry.tag;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.util.Identifier;

public class TagBuilder {
    private final List<TagEntry> entries = new ArrayList<TagEntry>();

    public static TagBuilder create() {
        return new TagBuilder();
    }

    public List<TagEntry> build() {
        return List.copyOf(this.entries);
    }

    public TagBuilder add(TagEntry entry) {
        this.entries.add(entry);
        return this;
    }

    public TagBuilder add(Identifier id) {
        return this.add(TagEntry.create(id));
    }

    public TagBuilder addOptional(Identifier id) {
        return this.add(TagEntry.createOptional(id));
    }

    public TagBuilder addTag(Identifier id) {
        return this.add(TagEntry.createTag(id));
    }

    public TagBuilder addOptionalTag(Identifier id) {
        return this.add(TagEntry.createOptionalTag(id));
    }
}

