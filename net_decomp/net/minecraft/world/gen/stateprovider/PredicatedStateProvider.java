/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.stateprovider;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record PredicatedStateProvider(BlockStateProvider fallback, List<Rule> rules) {
    public static final Codec<PredicatedStateProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.TYPE_CODEC.fieldOf("fallback").forGetter(PredicatedStateProvider::fallback), (App)Rule.CODEC.listOf().fieldOf("rules").forGetter(PredicatedStateProvider::rules)).apply((Applicative)instance, PredicatedStateProvider::new));

    public static PredicatedStateProvider of(BlockStateProvider stateProvider) {
        return new PredicatedStateProvider(stateProvider, List.of());
    }

    public static PredicatedStateProvider of(Block block) {
        return PredicatedStateProvider.of(BlockStateProvider.of(block));
    }

    public BlockState getBlockState(StructureWorldAccess world, Random random, BlockPos pos) {
        for (Rule rule : this.rules) {
            if (!rule.ifTrue().test(world, pos)) continue;
            return rule.then().get(random, pos);
        }
        return this.fallback.get(random, pos);
    }

    public record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPredicate.BASE_CODEC.fieldOf("if_true").forGetter(Rule::ifTrue), (App)BlockStateProvider.TYPE_CODEC.fieldOf("then").forGetter(Rule::then)).apply((Applicative)instance, Rule::new));
    }
}

