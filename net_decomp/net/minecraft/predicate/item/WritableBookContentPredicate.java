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
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.text.RawFilteredPair;

public record WritableBookContentPredicate(Optional<CollectionPredicate<RawFilteredPair<String>, RawStringPredicate>> pages) implements ComponentSubPredicate<WritableBookContentComponent>
{
    public static final Codec<WritableBookContentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.createCodec(RawStringPredicate.CODEC).optionalFieldOf("pages").forGetter(WritableBookContentPredicate::pages)).apply((Applicative)instance, WritableBookContentPredicate::new));

    @Override
    public ComponentType<WritableBookContentComponent> getComponentType() {
        return DataComponentTypes.WRITABLE_BOOK_CONTENT;
    }

    @Override
    public boolean test(WritableBookContentComponent writableBookContentComponent) {
        return !this.pages.isPresent() || this.pages.get().test(writableBookContentComponent.pages());
    }

    public record RawStringPredicate(String contents) implements Predicate<RawFilteredPair<String>>
    {
        public static final Codec<RawStringPredicate> CODEC = Codec.STRING.xmap(RawStringPredicate::new, RawStringPredicate::contents);

        @Override
        public boolean test(RawFilteredPair<String> rawFilteredPair) {
            return rawFilteredPair.raw().equals(this.contents);
        }

        @Override
        public /* synthetic */ boolean test(Object string) {
            return this.test((RawFilteredPair)string);
        }
    }
}

