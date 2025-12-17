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

public class KilledByPlayerLootCondition
implements LootCondition {
    private static final KilledByPlayerLootCondition INSTANCE = new KilledByPlayerLootCondition();
    public static final MapCodec<KilledByPlayerLootCondition> CODEC = MapCodec.unit((Object)INSTANCE);

    private KilledByPlayerLootCondition() {
    }

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.KILLED_BY_PLAYER;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.hasParameter(LootContextParameters.LAST_DAMAGE_PLAYER);
    }

    public static LootCondition.Builder builder() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

