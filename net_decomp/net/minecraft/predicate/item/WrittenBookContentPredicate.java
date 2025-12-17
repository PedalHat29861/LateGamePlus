/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record WrittenBookContentPredicate(Optional<CollectionPredicate<RawFilteredPair<Text>, RawTextPredicate>> pages, Optional<String> author, Optional<String> title, NumberRange.IntRange generation, Optional<Boolean> resolved) implements ComponentSubPredicate<WrittenBookContentComponent>
{
    public static final Codec<WrittenBookContentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.createCodec(RawTextPredicate.CODEC).optionalFieldOf("pages").forGetter(WrittenBookContentPredicate::pages), (App)Codec.STRING.optionalFieldOf("author").forGetter(WrittenBookContentPredicate::author), (App)Codec.STRING.optionalFieldOf("title").forGetter(WrittenBookContentPredicate::title), (App)NumberRange.IntRange.CODEC.optionalFieldOf("generation", (Object)NumberRange.IntRange.ANY).forGetter(WrittenBookContentPredicate::generation), (App)Codec.BOOL.optionalFieldOf("resolved").forGetter(WrittenBookContentPredicate::resolved)).apply((Applicative)instance, WrittenBookContentPredicate::new));

    @Override
    public ComponentType<WrittenBookContentComponent> getComponentType() {
        return DataComponentTypes.WRITTEN_BOOK_CONTENT;
    }

    @Override
    public boolean test(WrittenBookContentComponent writtenBookContentComponent) {
        if (this.author.isPresent() && !this.author.get().equals(writtenBookContentComponent.author())) {
            return false;
        }
        if (this.title.isPresent() && !this.title.get().equals(writtenBookContentComponent.title().raw())) {
            return false;
        }
        if (!this.generation.test(writtenBookContentComponent.generation())) {
            return false;
        }
        if (this.resolved.isPresent() && this.resolved.get().booleanValue() != writtenBookContentComponent.resolved()) {
            return false;
        }
        return !this.pages.isPresent() || this.pages.get().test(writtenBookContentComponent.pages());
    }

    public record RawTextPredicate(Text contents) implements Predicate<RawFilteredPair<Text>>
    {
        public static final Codec<RawTextPredicate> CODEC = TextCodecs.CODEC.xmap(RawTextPredicate::new, RawTextPredicate::contents);

        @Override
        public boolean test(RawFilteredPair<Text> rawFilteredPair) {
            return rawFilteredPair.raw().equals(this.contents);
        }

        @Override
        public /* synthetic */ boolean test(Object text) {
            return this.test((RawFilteredPair)text);
        }
    }
}

