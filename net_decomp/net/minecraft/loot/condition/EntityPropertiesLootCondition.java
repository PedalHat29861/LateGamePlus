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
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.Vec3d;

public record EntityPropertiesLootCondition(Optional<EntityPredicate> predicate, LootContext.EntityReference entity) implements LootCondition
{
    public static final MapCodec<EntityPropertiesLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EntityPredicate.CODEC.optionalFieldOf("predicate").forGetter(EntityPropertiesLootCondition::predicate), (App)LootContext.EntityReference.CODEC.fieldOf("entity").forGetter(EntityPropertiesLootCondition::entity)).apply((Applicative)instance, EntityPropertiesLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.ENTITY_PROPERTIES;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ORIGIN, this.entity.contextParam());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(this.entity.contextParam());
        Vec3d vec3d = lootContext.get(LootContextParameters.ORIGIN);
        return this.predicate.isEmpty() || this.predicate.get().test(lootContext.getWorld(), vec3d, entity);
    }

    public static LootCondition.Builder create(LootContext.EntityReference entity) {
        return EntityPropertiesLootCondition.builder(entity, EntityPredicate.Builder.create());
    }

    public static LootCondition.Builder builder(LootContext.EntityReference entity, EntityPredicate.Builder predicateBuilder) {
        return () -> new EntityPropertiesLootCondition(Optional.of(predicateBuilder.build()), entity);
    }

    public static LootCondition.Builder builder(LootContext.EntityReference entity, EntityPredicate predicate) {
        return () -> new EntityPropertiesLootCondition(Optional.of(predicate), entity);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntityPropertiesLootCondition.class, "predicate;entityTarget", "predicate", "entity"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntityPropertiesLootCondition.class, "predicate;entityTarget", "predicate", "entity"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntityPropertiesLootCondition.class, "predicate;entityTarget", "predicate", "entity"}, this, object);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

