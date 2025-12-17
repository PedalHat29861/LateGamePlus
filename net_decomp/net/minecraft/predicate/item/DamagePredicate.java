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
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentPredicate;

public record DamagePredicate(NumberRange.IntRange durability, NumberRange.IntRange damage) implements ComponentPredicate
{
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NumberRange.IntRange.CODEC.optionalFieldOf("durability", (Object)NumberRange.IntRange.ANY).forGetter(DamagePredicate::durability), (App)NumberRange.IntRange.CODEC.optionalFieldOf("damage", (Object)NumberRange.IntRange.ANY).forGetter(DamagePredicate::damage)).apply((Applicative)instance, DamagePredicate::new));

    @Override
    public boolean test(ComponentsAccess components) {
        Integer integer = components.get(DataComponentTypes.DAMAGE);
        if (integer == null) {
            return false;
        }
        int i = components.getOrDefault(DataComponentTypes.MAX_DAMAGE, 0);
        if (!this.durability.test(i - integer)) {
            return false;
        }
        return this.damage.test(integer);
    }

    public static DamagePredicate durability(NumberRange.IntRange durability) {
        return new DamagePredicate(durability, NumberRange.IntRange.ANY);
    }
}

