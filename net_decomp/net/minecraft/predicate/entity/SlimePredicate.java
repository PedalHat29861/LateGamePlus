/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public record SlimePredicate(NumberRange.IntRange size) implements EntitySubPredicate
{
    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberRange.IntRange.CODEC.optionalFieldOf("size", (Object)NumberRange.IntRange.ANY).forGetter(SlimePredicate::size)).apply((Applicative)instance, SlimePredicate::new));

    public static SlimePredicate of(NumberRange.IntRange size) {
        return new SlimePredicate(size);
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (entity instanceof SlimeEntity) {
            SlimeEntity slimeEntity = (SlimeEntity)entity;
            return this.size.test(slimeEntity.getSize());
        }
        return false;
    }

    public MapCodec<SlimePredicate> getCodec() {
        return EntitySubPredicateTypes.SLIME;
    }
}

