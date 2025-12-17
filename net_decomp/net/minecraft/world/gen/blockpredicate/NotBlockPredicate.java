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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

class NotBlockPredicate
implements BlockPredicate {
    public static final MapCodec<NotBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockPredicate.BASE_CODEC.fieldOf("predicate").forGetter(predicate -> predicate.predicate)).apply((Applicative)instance, NotBlockPredicate::new));
    private final BlockPredicate predicate;

    public NotBlockPredicate(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        return !this.predicate.test(structureWorldAccess, blockPos);
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.NOT;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

