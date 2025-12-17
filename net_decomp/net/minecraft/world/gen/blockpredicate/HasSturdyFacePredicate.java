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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public class HasSturdyFacePredicate
implements BlockPredicate {
    private final Vec3i offset;
    private final Direction face;
    public static final MapCodec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3i.createOffsetCodec(16).optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(predicate -> predicate.offset), (App)Direction.CODEC.fieldOf("direction").forGetter(predicate -> predicate.face)).apply((Applicative)instance, HasSturdyFacePredicate::new));

    public HasSturdyFacePredicate(Vec3i offset, Direction face) {
        this.offset = offset;
        this.face = face;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.add(this.offset);
        return structureWorldAccess.getBlockState(blockPos2).isSideSolidFullSquare(structureWorldAccess, blockPos2, this.face);
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.HAS_STURDY_FACE;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

