/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.condition;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.random.Random;

public class SurvivesExplosionLootCondition
implements LootCondition {
    private static final SurvivesExplosionLootCondition INSTANCE = new SurvivesExplosionLootCondition();
    public static final MapCodec<SurvivesExplosionLootCondition> CODEC = MapCodec.unit((Object)INSTANCE);

    private SurvivesExplosionLootCondition() {
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.SURVIVES_EXPLOSION;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.EXPLOSION_RADIUS);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Float float_ = lootContext.get(LootContextParameters.EXPLOSION_RADIUS);
        if (float_ != null) {
            Random random = lootContext.getRandom();
            float f = 1.0f / float_.floatValue();
            return random.nextFloat() <= f;
        }
        return true;
    }

    public static LootCondition.Builder builder() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

