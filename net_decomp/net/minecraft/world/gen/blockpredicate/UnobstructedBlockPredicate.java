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
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

record UnobstructedBlockPredicate(Vec3i offset) implements BlockPredicate
{
    public static MapCodec<UnobstructedBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3i.CODEC.optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(UnobstructedBlockPredicate::offset)).apply((Applicative)instance, UnobstructedBlockPredicate::new));

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.UNOBSTRUCTED;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        return structureWorldAccess.doesNotIntersectEntities(null, VoxelShapes.fullCube().offset(blockPos));
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

