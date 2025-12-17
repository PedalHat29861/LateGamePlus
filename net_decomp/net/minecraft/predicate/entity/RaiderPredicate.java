/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public record RaiderPredicate(boolean hasRaid, boolean isCaptain) implements EntitySubPredicate
{
    public static final MapCodec<RaiderPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("has_raid", (Object)false).forGetter(RaiderPredicate::hasRaid), (App)Codec.BOOL.optionalFieldOf("is_captain", (Object)false).forGetter(RaiderPredicate::isCaptain)).apply((Applicative)instance, RaiderPredicate::new));
    public static final RaiderPredicate CAPTAIN_WITHOUT_RAID = new RaiderPredicate(false, true);

    public MapCodec<RaiderPredicate> getCodec() {
        return EntitySubPredicateTypes.RAIDER;
    }

    @Override
    public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
        if (entity instanceof RaiderEntity) {
            RaiderEntity raiderEntity = (RaiderEntity)entity;
            return raiderEntity.hasRaid() == this.hasRaid && raiderEntity.isCaptain() == this.isCaptain;
        }
        return false;
    }
}

