/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.entity.spawn;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MoonPhase;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.dimension.DimensionType;

public record MoonBrightnessSpawnCondition(NumberRange.DoubleRange range) implements SpawnCondition
{
    public static final MapCodec<MoonBrightnessSpawnCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)NumberRange.DoubleRange.CODEC.fieldOf("range").forGetter(MoonBrightnessSpawnCondition::range)).apply((Applicative)instance, MoonBrightnessSpawnCondition::new));

    @Override
    public boolean test(SpawnContext spawnContext) {
        MoonPhase moonPhase = spawnContext.environmentAttributes().getAttributeValue(EnvironmentAttributes.MOON_PHASE_VISUAL, Vec3d.ofCenter(spawnContext.pos()));
        float f = DimensionType.MOON_SIZES[moonPhase.getIndex()];
        return this.range.test(f);
    }

    public MapCodec<MoonBrightnessSpawnCondition> getCodec() {
        return CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((SpawnContext)context);
    }
}

