/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.ErrorReporter;

public abstract class AlternativeLootCondition
implements LootCondition {
    protected final List<LootCondition> terms;
    private final Predicate<LootContext> predicate;

    protected AlternativeLootCondition(List<LootCondition> terms, Predicate<LootContext> predicate) {
        this.terms = terms;
        this.predicate = predicate;
    }

    protected static <T extends AlternativeLootCondition> MapCodec<T> createCodec(Function<List<LootCondition>, T> termsToCondition) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootCondition.CODEC.listOf().fieldOf("terms").forGetter(condition -> condition.terms)).apply((Applicative)instance, termsToCondition));
    }

    protected static <T extends AlternativeLootCondition> Codec<T> createInlineCodec(Function<List<LootCondition>, T> termsToCondition) {
        return LootCondition.CODEC.listOf().xmap(termsToCondition, condition -> condition.terms);
    }

    @Override
    public final boolean test(LootContext lootContext) {
        return this.predicate.test(lootContext);
    }

    @Override
    public void validate(LootTableReporter reporter) {
        LootCondition.super.validate(reporter);
        for (int i = 0; i < this.terms.size(); ++i) {
            this.terms.get(i).validate(reporter.makeChild(new ErrorReporter.NamedListElementContext("terms", i)));
        }
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static abstract class Builder
    implements LootCondition.Builder {
        private final ImmutableList.Builder<LootCondition> terms = ImmutableList.builder();

        protected Builder(LootCondition.Builder ... terms) {
            for (LootCondition.Builder builder : terms) {
                this.terms.add((Object)builder.build());
            }
        }

        public void add(LootCondition.Builder builder) {
            this.terms.add((Object)builder.build());
        }

        @Override
        public LootCondition build() {
            return this.build((List<LootCondition>)this.terms.build());
        }

        protected abstract LootCondition build(List<LootCondition> var1);
    }
}

