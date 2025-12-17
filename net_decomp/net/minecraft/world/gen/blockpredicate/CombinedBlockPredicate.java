/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;

abstract class CombinedBlockPredicate
implements BlockPredicate {
    protected final List<BlockPredicate> predicates;

    protected CombinedBlockPredicate(List<BlockPredicate> predicates) {
        this.predicates = predicates;
    }

    public static <T extends CombinedBlockPredicate> MapCodec<T> buildCodec(Function<List<BlockPredicate>, T> combiner) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockPredicate.BASE_CODEC.listOf().fieldOf("predicates").forGetter(predicate -> predicate.predicates)).apply((Applicative)instance, combiner));
    }
}

