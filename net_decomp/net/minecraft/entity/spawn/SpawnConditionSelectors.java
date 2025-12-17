/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.entity.spawn;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnContext;

public record SpawnConditionSelectors(List<VariantSelectorProvider.Selector<SpawnContext, SpawnCondition>> selectors) {
    public static final SpawnConditionSelectors EMPTY = new SpawnConditionSelectors(List.of());
    public static final Codec<SpawnConditionSelectors> CODEC = VariantSelectorProvider.Selector.createCodec(SpawnCondition.CODEC).listOf().xmap(SpawnConditionSelectors::new, SpawnConditionSelectors::selectors);

    public static SpawnConditionSelectors createSingle(SpawnCondition condition, int priority) {
        return new SpawnConditionSelectors(VariantSelectorProvider.createSingle(condition, priority));
    }

    public static SpawnConditionSelectors createFallback(int priority) {
        return new SpawnConditionSelectors(VariantSelectorProvider.createFallback(priority));
    }
}

