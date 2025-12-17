/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.Vec3d;

public record DamageSourcePropertiesLootCondition(Optional<DamageSourcePredicate> predicate) implements LootCondition
{
    public static final MapCodec<DamageSourcePropertiesLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DamageSourcePredicate.CODEC.optionalFieldOf("predicate").forGetter(DamageSourcePropertiesLootCondition::predicate)).apply((Applicative)instance, DamageSourcePropertiesLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.DAMAGE_SOURCE_PROPERTIES;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ORIGIN, LootContextParameters.DAMAGE_SOURCE);
    }

    @Override
    public boolean test(LootContext lootContext) {
        DamageSource damageSource = lootContext.get(LootContextParameters.DAMAGE_SOURCE);
        Vec3d vec3d = lootContext.get(LootContextParameters.ORIGIN);
        if (vec3d == null || damageSource == null) {
            return false;
        }
        return this.predicate.isEmpty() || this.predicate.get().test(lootContext.getWorld(), vec3d, damageSource);
    }

    public static LootCondition.Builder builder(DamageSourcePredicate.Builder builder) {
        return () -> new DamageSourcePropertiesLootCondition(Optional.of(builder.build()));
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

