/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.loot.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.condition.LootCondition;

public record LootConditionType(MapCodec<? extends LootCondition> codec) {
}

