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
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public class WouldSurviveBlockPredicate
implements BlockPredicate {
    public static final MapCodec<WouldSurviveBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3i.createOffsetCodec(16).optionalFieldOf("offset", (Object)Vec3i.ZERO).forGetter(predicate -> predicate.offset), (App)BlockState.CODEC.fieldOf("state").forGetter(predicate -> predicate.state)).apply((Applicative)instance, WouldSurviveBlockPredicate::new));
    private final Vec3i offset;
    private final BlockState state;

    protected WouldSurviveBlockPredicate(Vec3i offset, BlockState state) {
        this.offset = offset;
        this.state = state;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        return this.state.canPlaceAt(structureWorldAccess, blockPos.add(this.offset));
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.WOULD_SURVIVE;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

