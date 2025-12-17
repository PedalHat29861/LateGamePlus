/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public record LocationCheckLootCondition(Optional<LocationPredicate> predicate, BlockPos offset) implements LootCondition
{
    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.optionalFieldOf("offsetX", (Object)0).forGetter(Vec3i::getX), (App)Codec.INT.optionalFieldOf("offsetY", (Object)0).forGetter(Vec3i::getY), (App)Codec.INT.optionalFieldOf("offsetZ", (Object)0).forGetter(Vec3i::getZ)).apply((Applicative)instance, BlockPos::new));
    public static final MapCodec<LocationCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheckLootCondition::predicate), (App)OFFSET_CODEC.forGetter(LocationCheckLootCondition::offset)).apply((Applicative)instance, LocationCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.LOCATION_CHECK;
    }

    @Override
    public boolean test(LootContext lootContext) {
        Vec3d vec3d = lootContext.get(LootContextParameters.ORIGIN);
        return vec3d != null && (this.predicate.isEmpty() || this.predicate.get().test(lootContext.getWorld(), vec3d.getX() + (double)this.offset.getX(), vec3d.getY() + (double)this.offset.getY(), vec3d.getZ() + (double)this.offset.getZ()));
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ORIGIN);
    }

    public static LootCondition.Builder builder(LocationPredicate.Builder predicateBuilder) {
        return () -> new LocationCheckLootCondition(Optional.of(predicateBuilder.build()), BlockPos.ORIGIN);
    }

    public static LootCondition.Builder builder(LocationPredicate.Builder predicateBuilder, BlockPos pos) {
        return () -> new LocationCheckLootCondition(Optional.of(predicateBuilder.build()), pos);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

