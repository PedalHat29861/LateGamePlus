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
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public record LightningBoltPredicate(NumberRange.IntRange blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate
{
    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberRange.IntRange.CODEC.optionalFieldOf("blocks_set_on_fire", (Object)NumberRange.IntRange.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire), (App)EntityPredicate.CODEC.optionalFieldOf("entity_struck").forGetter(LightningBoltPredicate::entityStruck)).apply((Applicative)instance, LightningBoltPredicate::new));

    public static LightningBoltPredicate of(NumberRange.IntRange blocksSetOnFire) {
        return new LightningBoltPredicate(blocksSetOnFire, Optional.empty());
    }

    public MapCodec<LightningBoltPredicate> getCodec() {
        return EntitySubPredicateTypes.LIGHTNING;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (!(entity instanceof LightningEntity)) {
            return false;
        }
        LightningEntity lightningEntity = (LightningEntity)entity;
        return this.blocksSetOnFire.test(lightningEntity.getBlocksSetOnFire()) && (this.entityStruck.isEmpty() || lightningEntity.getStruckEntities().anyMatch(struckEntity -> this.entityStruck.get().test(world, pos, (Entity)struckEntity)));
    }
}

