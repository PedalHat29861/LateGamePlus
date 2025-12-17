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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.blockpredicate.OffsetPredicate;

class MatchingBlocksBlockPredicate
extends OffsetPredicate {
    private final RegistryEntryList<Block> blocks;
    public static final MapCodec<MatchingBlocksBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> MatchingBlocksBlockPredicate.registerOffsetField(instance).and((App)RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("blocks").forGetter(predicate -> predicate.blocks)).apply((Applicative)instance, MatchingBlocksBlockPredicate::new));

    public MatchingBlocksBlockPredicate(Vec3i offset, RegistryEntryList<Block> blocks) {
        super(offset);
        this.blocks = blocks;
    }

    @Override
    protected boolean test(BlockState state) {
        return state.isIn(this.blocks);
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }
}

