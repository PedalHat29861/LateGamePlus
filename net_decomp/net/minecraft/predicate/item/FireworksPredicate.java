/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.collection.CollectionPredicate;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.predicate.item.FireworkExplosionPredicate;

public record FireworksPredicate(Optional<CollectionPredicate<FireworkExplosionComponent, FireworkExplosionPredicate.Predicate>> explosions, NumberRange.IntRange flightDuration) implements ComponentSubPredicate<FireworksComponent>
{
    public static final Codec<FireworksPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.createCodec(FireworkExplosionPredicate.Predicate.CODEC).optionalFieldOf("explosions").forGetter(FireworksPredicate::explosions), (App)NumberRange.IntRange.CODEC.optionalFieldOf("flight_duration", (Object)NumberRange.IntRange.ANY).forGetter(FireworksPredicate::flightDuration)).apply((Applicative)instance, FireworksPredicate::new));

    @Override
    public ComponentType<FireworksComponent> getComponentType() {
        return DataComponentTypes.FIREWORKS;
    }

    @Override
    public boolean test(FireworksComponent fireworksComponent) {
        if (this.explosions.isPresent() && !this.explosions.get().test(fireworksComponent.explosions())) {
            return false;
        }
        return this.flightDuration.test(fireworksComponent.flightDuration());
    }
}

